import {
  DUTY,
  Decision,
  Fault,
  LIMITS,
  LogKind,
  Mode,
  Opcode,
  Reason,
  Status,
  VERSION,
  decodeFrame,
  decodeScent,
  decodeSchedule,
  encodeFrame,
  encodeSchedule,
  encodeState,
  u32le,
  type Frame,
  type ScheduleRule,
  validateSchedule,
} from './protocol'

export type { ScheduleRule }

export type LogEntry = { t: number; kind: number; level: number; reason: number }

export type Proposal = { level: number; windowSec: number; reasons: number[] }

type Quiet = { enabled: number; start: number; end: number }

const SERIAL = 'SIM0001'

export class SimulatedFirmware {
  readonly id = 'neb-sim-1'
  bondToken: string | null = null
  pairingUntil = 0
  identifyUntil = 0

  private userRun = 0
  private level = 2
  private fault: number = Fault.CLOCK_LOST
  private present = 1
  private spraySeconds = 2000
  private sprayMsAcc = 0
  private timeValid = 0
  private readonly fwMajor = 1
  private readonly fwMinor = 0
  private readonly supply = 1
  private mode: number = Mode.AUTO
  private schedule: ScheduleRule[] = []
  private quiet: Quiet = { enabled: 1, start: 23 * 60, end: 7 * 60 }
  private scentSku = 1
  private scentName = '포근한 나무'
  private syncUnix = 0
  private syncMono = 0
  private tzMin = 0
  private lastSeq = -1
  private lastResponse: Uint8Array | null = null
  private logs: LogEntry[] = []
  private proposal: Proposal | null = null
  private snoozeUntil = 0
  private proposeAfter = 0
  private cooldownUntil = 0
  private origin: 'off' | 'user' | 'auto' = 'off'
  private autoOffAt = 0
  private pendingAuto = false
  private autoWindowSec = 0
  private scheduleRejectLatch = false
  private recentUntil: number
  private readonly listeners = new Set<(state: Uint8Array) => void>()
  private readonly watchers = new Set<() => void>()

  constructor(private readonly now: () => number = Date.now) {
    this.recentUntil = this.now() + 10 * 60 * 1000
  }

  onNotify(cb: (state: Uint8Array) => void): () => void {
    this.listeners.add(cb)
    return () => this.listeners.delete(cb)
  }

  watch(cb: () => void): () => void {
    this.watchers.add(cb)
    return () => this.watchers.delete(cb)
  }

  openPairingWindow(): void {
    this.pairingUntil = this.now() + 30_000
    this.emit()
  }

  get pairingMsLeft(): number {
    return Math.max(0, this.pairingUntil - this.now())
  }

  acceptLink(id: string, phoneToken: string | null): { ok: true; token: string } | { ok: false; message: string } {
    if (id !== this.id) return { ok: false, message: '기기가 없습니다.' }
    const windowOpen = this.now() < this.pairingUntil
    if (phoneToken && phoneToken === this.bondToken) return { ok: true, token: phoneToken }
    if (!windowOpen) {
      return { ok: false, message: '페어링 창이 닫혀 있습니다. 기기 버튼을 누르세요.' }
    }
    this.bondToken = `bond-${this.now().toString(16)}-${Math.floor(Math.random() * 0xffff).toString(16)}`
    this.pairingUntil = 0
    this.emit()
    return { ok: true, token: this.bondToken }
  }

  advertisements(phoneToken: string | null) {
    const windowOpen = this.now() < this.pairingUntil
    const known = Boolean(phoneToken && phoneToken === this.bondToken)
    if (!windowOpen && !known) return []
    return [
      {
        id: this.id,
        name: '네뷸라이저',
        serviceUuid: '8a1f0001-6e62-4c31-9a10-5d4e62756c31',
        protocolVersion: VERSION,
        faultNibble: this.fault & 0x0f,
        cartridgePresent: this.present === 1,
        rssiDbm: null as number | null,
      },
    ]
  }

  factoryReset(): void {
    const dry = this.fault & Fault.DRY
    this.bondToken = null
    this.pairingUntil = 0
    this.userRun = 0
    this.origin = 'off'
    this.level = 2
    this.mode = Mode.AUTO
    this.schedule = []
    this.timeValid = 0
    this.fault = Fault.CLOCK_LOST | dry
    this.proposal = null
    this.logs = []
    this.quiet = { enabled: 1, start: 23 * 60, end: 7 * 60 }
    this.cooldownUntil = 0
    this.identifyUntil = 0
    this.pendingAuto = false
    this.emit()
  }

  /** Simulator-only. A presence switch, not a radio command. */
  setPresent(present: boolean): void {
    if (present) {
      this.present = 1
      this.spraySeconds = 0
      this.sprayMsAcc = 0
      this.fault &= ~Fault.DRY
    } else {
      this.present = 0
      this.userRun = 0
      this.origin = 'off'
      this.proposal = null
    }
    this.emit()
  }

  /** Simulator-only. Drives the hour meter to the firmware life so dry lock engages. */
  forceEmpty(): void {
    this.present = 1
    this.spraySeconds = LIMITS.LIFE_SECONDS
    this.enterDry()
    this.emit()
  }

  loseClock(): void {
    this.timeValid = 0
    this.fault |= Fault.CLOCK_LOST
    this.emit()
  }

  pressPower(): void {
    if (this.actuatorRun()) {
      this.userRun = 0
      this.origin = 'off'
      this.pendingAuto = false
    } else {
      this.applyRun(1, 'user')
    }
    this.emit()
  }

  peek() {
    return {
      pairingMsLeft: this.pairingMsLeft,
      bonded: this.bondToken !== null,
      run: this.actuatorRun(),
      level: this.effectiveLevel(),
      present: this.present === 1,
      dry: (this.fault & Fault.DRY) !== 0,
      identify: this.now() < this.identifyUntil,
      ledOn: this.actuatorRun() === 1 || this.now() < this.identifyUntil,
      spraySeconds: this.spraySeconds,
      life: LIMITS.LIFE_SECONDS,
      timeValid: this.timeValid === 1,
      fault: this.fault,
    }
  }

  advance(ms: number): void {
    if (ms < 0) return
    const before = this.signature()
    if (this.pendingAuto && this.now() >= this.cooldownUntil && !this.blocked() && !this.inQuiet()) {
      this.userRun = 1
      this.origin = 'auto'
      this.autoOffAt = this.now() + this.autoWindowSec * 1000
      this.pendingAuto = false
    }
    if (this.origin === 'auto' && this.autoOffAt && this.now() >= this.autoOffAt) {
      this.userRun = 0
      this.origin = 'off'
    }
    if (this.actuatorRun()) {
      const row = DUTY[this.effectiveLevel() - 1]
      const onMs = (ms * row.on) / (row.on + row.off)
      this.addSprayMs(onMs)
    }
    this.maybePropose()
    if (this.signature() !== before) this.emit()
  }

  handle(raw: Uint8Array): Uint8Array {
    let frame: Frame
    try {
      frame = decodeFrame(raw)
    } catch (error) {
      const seq = raw.length > 2 ? raw[2] : 0
      const opcode = raw.length > 1 ? raw[1] : 0
      const message = error instanceof Error ? error.message : ''
      const status = message === 'length' || message === 'short' ? Status.BAD_LENGTH : Status.BAD_CRC
      return this.respond(opcode, seq, Uint8Array.of(status), false)
    }
    if (frame.version !== VERSION) {
      return this.respond(frame.opcode, frame.seq, Uint8Array.of(Status.BAD_RANGE), true)
    }
    if (frame.seq === this.lastSeq && this.lastResponse) return this.lastResponse
    const response = this.dispatch(frame)
    this.lastSeq = frame.seq
    this.lastResponse = response
    return response
  }

  writeScheduleBlob(data: Uint8Array): number {
    if (!this.bondToken) return Status.NOT_BONDED
    try {
      const rules = decodeSchedule(data)
      return this.applySchedule(rules)
    } catch {
      this.latchScheduleReject()
      return Status.BAD_LENGTH
    }
  }

  writeTimeBlob(data: Uint8Array): number {
    if (!this.bondToken) return Status.NOT_BONDED
    if (data.length !== 6) return Status.BAD_LENGTH
    return this.applyTime(data)
  }

  encodeStateBytes(consumeLatch = false): Uint8Array {
    const fault = this.fault
    if (consumeLatch && this.scheduleRejectLatch) {
      this.fault &= ~Fault.SCHEDULE_REJECTED
      this.scheduleRejectLatch = false
    }
    return encodeState({
      run: this.actuatorRun(),
      level: this.effectiveLevel(),
      fault,
      present: this.present,
      spraySeconds: this.spraySeconds,
      timeValid: this.timeValid,
      fwMajor: this.fwMajor,
      fwMinor: this.fwMinor,
      supply: this.supply,
    })
  }

  private dispatch(frame: Frame): Uint8Array {
    if (!this.bondToken) {
      return this.respond(frame.opcode, frame.seq, Uint8Array.of(Status.NOT_BONDED), false)
    }
    const { opcode, seq, payload } = frame
    switch (opcode) {
      case Opcode.GET_STATE:
        return this.respond(opcode, seq, this.encodeStateBytes(true), false)
      case Opcode.SET_RUN:
        return this.respond(opcode, seq, Uint8Array.of(this.setRun(payload)), true)
      case Opcode.SET_LEVEL:
        return this.respond(opcode, seq, Uint8Array.of(this.setLevel(payload)), true)
      case Opcode.SET_TIME:
        return this.respond(opcode, seq, Uint8Array.of(this.applyTime(payload)), true)
      case Opcode.PUT_SCHEDULE:
        try {
          return this.respond(opcode, seq, Uint8Array.of(this.applySchedule(decodeSchedule(payload))), true)
        } catch {
          this.latchScheduleReject()
          return this.respond(opcode, seq, Uint8Array.of(Status.BAD_LENGTH), true)
        }
      case Opcode.GET_SCHEDULE:
        return this.respond(opcode, seq, concatStatus(encodeSchedule(this.schedule)), false)
      case Opcode.SET_SCENT:
        return this.respond(opcode, seq, Uint8Array.of(this.setScent(payload)), true)
      case Opcode.IDENTIFY:
        this.identifyUntil = this.now() + 4000
        return this.respond(opcode, seq, Uint8Array.of(Status.OK), true)
      case Opcode.GET_INFO:
        return this.respond(opcode, seq, concatStatus(encodeInfoBody(1, this.fwMajor, this.fwMinor, SERIAL)), false)
      case Opcode.SET_MODE:
        return this.respond(opcode, seq, Uint8Array.of(this.setMode(payload)), true)
      case Opcode.GET_PROPOSAL:
        return this.respond(opcode, seq, concatStatus(this.encodeProposal()), false)
      case Opcode.APPLY_DECISION:
        return this.respond(opcode, seq, Uint8Array.of(this.applyDecision(payload)), true)
      case Opcode.GET_LOG:
        return this.respond(opcode, seq, concatStatus(this.encodeLog()), false)
      case Opcode.MANUAL_BURST:
        return this.respond(opcode, seq, this.manualBurst(payload), true)
      case Opcode.PUT_QUIET:
        return this.respond(opcode, seq, Uint8Array.of(this.putQuiet(payload)), true)
      case Opcode.GET_GUARD:
        return this.respond(opcode, seq, concatStatus(this.encodeGuard()), false)
      default:
        return this.respond(opcode, seq, Uint8Array.of(Status.BAD_RANGE), false)
    }
  }

  private setRun(payload: Uint8Array): number {
    if (payload.length !== 1 || (payload[0] !== 0 && payload[0] !== 1)) return Status.BAD_LENGTH
    return this.applyRun(payload[0], 'user')
  }

  private applyRun(run: number, origin: 'user' | 'auto'): number {
    if (run === 0) {
      this.userRun = 0
      this.origin = 'off'
      this.pendingAuto = false
      return Status.OK
    }
    if (this.present === 0) return Status.NO_CARTRIDGE
    if ((this.fault & Fault.DRY) !== 0 || this.spraySeconds >= LIMITS.LIFE_SECONDS) return Status.DRY_LOCK
    this.userRun = 1
    this.origin = origin
    this.pendingAuto = false
    return Status.OK
  }

  private setLevel(payload: Uint8Array): number {
    if (payload.length !== 1) return Status.BAD_LENGTH
    const level = payload[0]
    if (level < 1 || level > 5) return Status.BAD_RANGE
    this.level = level
    return Status.OK
  }

  private applyTime(payload: Uint8Array): number {
    if (payload.length !== 6) return Status.BAD_LENGTH
    const unix = payload[0] | (payload[1] << 8) | (payload[2] << 16) | (payload[3] << 24)
    const tz = (payload[4] | (payload[5] << 8)) << 16 >> 16
    if (tz < -720 || tz > 840) return Status.BAD_RANGE
    this.syncUnix = unix >>> 0
    this.syncMono = this.now()
    this.tzMin = tz
    this.timeValid = 1
    this.fault &= ~Fault.CLOCK_LOST
    if (this.mode === Mode.AUTO && this.now() >= this.snoozeUntil) {
      this.proposal = null
      this.proposeAfter = 0
      this.maybePropose()
    }
    return Status.OK
  }

  private applySchedule(rules: ScheduleRule[]): number {
    const problem = validateSchedule(rules)
    if (problem) {
      this.latchScheduleReject()
      return Status.BAD_RANGE
    }
    this.schedule = rules.map((rule) => ({ ...rule }))
    this.fault &= ~Fault.SCHEDULE_REJECTED
    return Status.OK
  }

  private setScent(payload: Uint8Array): number {
    const scent = decodeScent(payload)
    if (!scent) return Status.BAD_LENGTH
    this.scentSku = scent.sku
    this.scentName = scent.name
    return Status.OK
  }

  private setMode(payload: Uint8Array): number {
    if (payload.length !== 1 || (payload[0] !== Mode.AUTO && payload[0] !== Mode.HOLD)) return Status.BAD_RANGE
    this.mode = payload[0]
    if (this.mode === Mode.HOLD) this.proposal = null
    else {
      this.proposeAfter = 0
      this.maybePropose()
    }
    return Status.OK
  }

  private applyDecision(payload: Uint8Array): number {
    if (payload.length < 4) return Status.BAD_LENGTH
    const action = payload[0]
    const snoozeMin = payload[1] | (payload[2] << 8)
    const pinLevel = payload[3]
    if (action === Decision.ACCEPT) {
      if (!this.proposal) return Status.BAD_RANGE
      this.level = this.proposal.level
      this.pushLog(LogKind.ACCEPT, this.level, this.proposal.reasons[0] ?? 0)
      const windowSec = this.proposal.windowSec
      this.proposal = null
      this.proposeAfter = this.now() + 60_000
      if (!this.blocked() && !this.inQuiet()) {
        if (this.now() < this.cooldownUntil) {
          this.pendingAuto = true
          this.autoWindowSec = windowSec
        } else {
          this.userRun = 1
          this.origin = 'auto'
          this.autoOffAt = this.now() + windowSec * 1000
        }
      }
      return Status.OK
    }
    if (action === Decision.SNOOZE) {
      const minutes = Math.min(240, Math.max(1, snoozeMin || 30))
      this.snoozeUntil = this.now() + minutes * 60_000
      this.pushLog(LogKind.SNOOZE, this.proposal?.level ?? this.level, this.proposal?.reasons[0] ?? 0)
      this.proposal = null
      return Status.OK
    }
    if (action === Decision.PIN) {
      const level = pinLevel >= 1 && pinLevel <= 5 ? pinLevel : (this.proposal?.level ?? this.level)
      this.level = level
      this.mode = Mode.HOLD
      this.pushLog(LogKind.PIN, level, this.proposal?.reasons[0] ?? 0)
      this.proposal = null
      this.pendingAuto = false
      return Status.OK
    }
    return Status.BAD_RANGE
  }

  private manualBurst(payload: Uint8Array): Uint8Array {
    const fail = (status: number) =>
      Uint8Array.of(status, 0, 0, 0, 0, 0, this.cooldownLeft() & 0xff, (this.cooldownLeft() >> 8) & 0xff)
    if (payload.length !== 1) return fail(Status.BAD_LENGTH)
    const level = payload[0]
    if (level < 1 || level > 5) return fail(Status.BAD_RANGE)
    if (this.present === 0) return fail(Status.NO_CARTRIDGE)
    if (this.blocked()) return fail(Status.DRY_LOCK)
    if (this.now() < this.cooldownUntil) {
      this.pushLog(LogKind.MESH_BLOCK, level, Reason.MESH_COOLDOWN)
      return fail(Status.MESH_LOCK)
    }
    const row = DUTY[level - 1]
    const on = Math.min(row.on, LIMITS.MAX_ON_MS)
    const clamped = on < row.on ? 1 : 0
    this.level = level
    this.addSprayMs(on)
    this.cooldownUntil = this.now() + LIMITS.COOLDOWN_MS
    const left = this.cooldownLeft()
    return Uint8Array.of(Status.OK, on & 0xff, (on >> 8) & 0xff, row.off & 0xff, (row.off >> 8) & 0xff, clamped, left & 0xff, (left >> 8) & 0xff)
  }

  private putQuiet(payload: Uint8Array): number {
    if (payload.length !== 5) return Status.BAD_LENGTH
    const enabled = payload[0]
    const start = payload[1] | (payload[2] << 8)
    const end = payload[3] | (payload[4] << 8)
    if ((enabled !== 0 && enabled !== 1) || start < 0 || start > 1439 || end < 0 || end > 1439) return Status.BAD_RANGE
    this.quiet = { enabled, start, end }
    return Status.OK
  }

  private maybePropose(): void {
    if (this.mode !== Mode.AUTO) return
    if (!this.timeValid) return
    if (this.now() < this.snoozeUntil || this.now() < this.proposeAfter) return
    if (this.blocked() || this.present === 0) {
      this.proposal = null
      return
    }
    if (this.proposal) return
    this.proposal = this.buildProposal()
    this.pushLog(LogKind.PROPOSE, this.proposal.level, this.proposal.reasons[0] ?? 0)
  }

  private buildProposal(): Proposal {
    const hour = Math.floor(this.localMin() / 60)
    const reasons: number[] = [Reason.TIME_OF_DAY]
    let level = hour < 8 || hour >= 22 ? 1 : hour < 12 ? 2 : hour >= 18 ? 3 : 4
    let windowSec = 12 * 60
    if (this.now() < this.recentUntil) {
      reasons.push(Reason.RECENT_USE)
      level = Math.max(1, level - 1)
    }
    if (this.spraySeconds * 2 >= LIMITS.LIFE_SECONDS) {
      reasons.push(Reason.CARTRIDGE_LIFE)
      level = Math.max(1, level - 1)
      windowSec = Math.min(windowSec, 8 * 60)
    }
    if (this.now() < this.cooldownUntil) {
      reasons.push(Reason.MESH_COOLDOWN)
      windowSec = Math.min(windowSec, 5 * 60)
    }
    return { level, windowSec, reasons }
  }

  private encodeProposal(): Uint8Array {
    const pending = this.proposal ? 1 : 0
    const level = this.proposal?.level ?? 0
    const windowSec = this.proposal?.windowSec ?? 0
    const reasons = this.proposal?.reasons ?? []
    const snooze = Math.max(0, Math.ceil((this.snoozeUntil - this.now()) / 1000))
    const name = new TextEncoder().encode(this.scentName)
    const out = new Uint8Array(23 + name.length)
    out[0] = pending
    out[1] = level
    out[2] = windowSec & 0xff
    out[3] = (windowSec >> 8) & 0xff
    out[4] = Math.min(4, reasons.length)
    for (let i = 0; i < 4; i++) out[5 + i] = reasons[i] ?? 0
    out[9] = snooze & 0xff
    out[10] = (snooze >> 8) & 0xff
    out[11] = this.mode
    out[12] = this.inQuiet() ? 1 : 0
    out[13] = this.quiet.enabled
    out[14] = this.quiet.start & 0xff
    out[15] = (this.quiet.start >> 8) & 0xff
    out[16] = this.quiet.end & 0xff
    out[17] = (this.quiet.end >> 8) & 0xff
    out.set(u32le(this.scentSku), 18)
    out[22] = name.length
    out.set(name, 23)
    return out
  }

  private encodeLog(): Uint8Array {
    const entries = [...this.logs].reverse().slice(0, LIMITS.MAX_LOG)
    const out = new Uint8Array(1 + entries.length * 7)
    out[0] = entries.length
    entries.forEach((entry, index) => {
      const offset = 1 + index * 7
      const t = entry.t >>> 0
      out[offset] = t & 0xff
      out[offset + 1] = (t >> 8) & 0xff
      out[offset + 2] = (t >> 16) & 0xff
      out[offset + 3] = (t >> 24) & 0xff
      out[offset + 4] = entry.kind
      out[offset + 5] = entry.level
      out[offset + 6] = entry.reason
    })
    return out
  }

  private encodeGuard(): Uint8Array {
    const left = this.cooldownLeft()
    const out = new Uint8Array(2 + 2 + 2 + 1 + 4 + DUTY.length * 4)
    let offset = 0
    const write16 = (value: number) => {
      out[offset++] = value & 0xff
      out[offset++] = (value >> 8) & 0xff
    }
    write16(LIMITS.MAX_ON_MS)
    write16(LIMITS.COOLDOWN_MS)
    write16(left)
    out[offset++] = this.now() < this.cooldownUntil ? 1 : 0
    const life = LIMITS.LIFE_SECONDS >>> 0
    out[offset++] = life & 0xff
    out[offset++] = (life >> 8) & 0xff
    out[offset++] = (life >> 16) & 0xff
    out[offset++] = (life >> 24) & 0xff
    for (const row of DUTY) {
      write16(row.on)
      write16(row.off)
    }
    return out
  }

  encodeInfo(): Uint8Array {
    return encodeInfoBody(1, this.fwMajor, this.fwMinor, SERIAL)
  }

  encodeScheduleBlob(): Uint8Array {
    return encodeSchedule(this.schedule)
  }

  scent(): { sku: number; name: string } {
    return { sku: this.scentSku, name: this.scentName }
  }

  private addSprayMs(ms: number): void {
    if (this.present === 0) return
    this.sprayMsAcc += ms
    const whole = Math.floor(this.sprayMsAcc / 1000)
    this.sprayMsAcc -= whole * 1000
    this.spraySeconds += whole
    if (this.spraySeconds >= LIMITS.LIFE_SECONDS) {
      this.spraySeconds = LIMITS.LIFE_SECONDS
      this.enterDry()
    }
  }

  private enterDry(): void {
    if ((this.fault & Fault.DRY) === 0) this.pushLog(LogKind.DRY_STOP, this.level, 0)
    this.fault |= Fault.DRY
    this.userRun = 0
    this.origin = 'off'
    this.pendingAuto = false
    this.proposal = null
  }

  private blocked(): boolean {
    return this.present === 0 || (this.fault & Fault.DRY) !== 0 || this.spraySeconds >= LIMITS.LIFE_SECONDS
  }

  private actuatorRun(): number {
    if (this.blocked()) return 0
    if (this.scheduleMatch()) return 1
    return this.userRun ? 1 : 0
  }

  private effectiveLevel(): number {
    return this.scheduleMatch()?.level ?? this.level
  }

  private scheduleMatch(): ScheduleRule | null {
    if (!this.timeValid || this.inQuiet() || this.blocked()) return null
    const bit = 1 << this.weekday()
    const minute = this.localMin()
    for (const rule of this.schedule) {
      if ((rule.flags & 1) === 0) continue
      if ((rule.weekdayMask & bit) === 0) continue
      if (minute >= rule.startMin && minute < rule.endMin) return rule
    }
    return null
  }

  private inQuiet(): boolean {
    if (!this.quiet.enabled || !this.timeValid) return false
    const minute = this.localMin()
    const { start, end } = this.quiet
    if (start === end) return false
    if (start < end) return minute >= start && minute < end
    return minute >= start || minute < end
  }

  private localUnix(): number {
    if (!this.timeValid) return 0
    return this.syncUnix + Math.floor((this.now() - this.syncMono) / 1000) + this.tzMin * 60
  }

  private localMin(): number {
    const shifted = this.localUnix()
    const day = Math.floor(shifted / 60)
    return ((day % 1440) + 1440) % 1440
  }

  private weekday(): number {
    const days = Math.floor(this.localUnix() / 86400)
    return ((days + 4) % 7 + 7) % 7
  }

  private cooldownLeft(): number {
    return Math.max(0, this.cooldownUntil - this.now())
  }

  private pushLog(kind: number, level: number, reason: number): void {
    this.logs.push({ t: this.timeValid ? this.syncUnix + Math.floor((this.now() - this.syncMono) / 1000) : 0, kind, level, reason })
    if (this.logs.length > LIMITS.MAX_LOG) this.logs.shift()
  }

  private latchScheduleReject(): void {
    this.fault |= Fault.SCHEDULE_REJECTED
    this.scheduleRejectLatch = true
  }

  private respond(opcode: number, seq: number, payload: Uint8Array, notify: boolean): Uint8Array {
    const frame = encodeFrame(opcode | 0x80, seq, payload)
    if (notify) this.emit()
    return frame
  }

  private emit(): void {
    const state = this.encodeStateBytes()
    for (const listener of this.listeners) listener(state)
    for (const watcher of this.watchers) watcher()
  }

  private signature(): string {
    return [
      this.actuatorRun(),
      this.effectiveLevel(),
      this.fault,
      this.spraySeconds,
      this.proposal?.level ?? -1,
      this.mode,
      this.timeValid,
      this.present,
      this.userRun,
      Math.ceil(this.cooldownLeft() / 1000),
    ].join(',')
  }
}

function concatStatus(data: Uint8Array): Uint8Array {
  const out = new Uint8Array(1 + data.length)
  out[0] = Status.OK
  out.set(data, 1)
  return out
}

export function encodeInfoBody(hw: number, major: number, minor: number, serial: string): Uint8Array {
  const name = new TextEncoder().encode(serial)
  const out = new Uint8Array(4 + name.length)
  out[0] = hw
  out[1] = major
  out[2] = minor
  out[3] = name.length
  out.set(name, 4)
  return out
}
