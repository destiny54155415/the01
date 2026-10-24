import {
  Decision,
  LIMITS,
  Opcode,
  Status,
  decodeFrame,
  decodeSchedule,
  decodeState,
  encodeFrame,
  encodeScent,
  encodeSchedule,
  faultLabels,
  i16le,
  readU16,
  readU32,
  statusMessage,
  u32le,
  type DeviceState,
  type ScheduleRule,
} from './protocol'
import { reasonText } from './reasons'
import type { NebulizerTransport } from './transport'
import { UUID } from './uuids'

export type Guard = {
  maxOnMs: number
  cooldownMs: number
  cooldownLeftMs: number
  hot: boolean
  lifeSeconds: number
  duty: { on: number; off: number }[]
}

export type ProposalView = {
  level: number
  windowSec: number
  reasons: { code: number; text: string }[]
  quietNow: boolean
}

export type LogView = { t: number; kind: number; level: number; reason: number; text: string }

export type SessionView = {
  radio: boolean
  state: DeviceState
  faults: string[]
  schedule: ScheduleRule[]
  proposal: ProposalView | null
  mode: 'auto' | 'hold'
  quiet: { enabled: boolean; start: number; end: number; now: boolean }
  guard: Guard
  log: LogView[]
  scentName: string
  scentSku: number
  serial: string
  hw: number
  remainingPercent: number | null
}

const LOG_KO: Record<number, string> = {
  1: '제안',
  2: '받아들임',
  3: '미룸',
  4: '고정',
  5: '메시가 막음',
  6: '건조로 정지',
}

export class Session {
  private seq = 1
  private unsub: (() => void) | null = null
  private pullGen = 0
  private listener: (() => void) | null = null
  private cache: SessionView | null = null

  constructor(readonly transport: NebulizerTransport) {}

  listen(cb: () => void): () => void {
    this.listener = cb
    return () => {
      if (this.listener === cb) this.listener = null
    }
  }

  view(): SessionView | null {
    return this.cache
  }

  async scan() {
    return this.transport.scan()
  }

  async connect(id: string): Promise<void> {
    await this.transport.connect(id)
    this.unsub?.()
    this.unsub = this.transport.subscribe(UUID.state, () => {
      void this.pull().catch(() => undefined)
    })
    await this.syncClock()
    await this.pull()
  }

  async disconnect(): Promise<void> {
    this.unsub?.()
    this.unsub = null
    await this.transport.disconnect()
  }

  async syncClock(): Promise<void> {
    const now = new Date()
    await this.setTime(Math.floor(now.getTime() / 1000), -now.getTimezoneOffset())
  }

  async setTime(unix: number, tzMin: number): Promise<void> {
    await this.command(Opcode.SET_TIME, concat([u32le(unix), i16le(tzMin)]))
    await this.pull()
  }

  async setRun(on: boolean): Promise<void> {
    await this.command(Opcode.SET_RUN, Uint8Array.of(on ? 1 : 0))
    await this.pull()
  }

  async setLevel(level: number): Promise<void> {
    await this.command(Opcode.SET_LEVEL, Uint8Array.of(level))
    await this.pull()
  }

  async putSchedule(rules: ScheduleRule[]): Promise<void> {
    await this.command(Opcode.PUT_SCHEDULE, encodeSchedule(rules))
    await this.pull()
  }

  async putQuiet(enabled: boolean, start: number, end: number): Promise<void> {
    const payload = new Uint8Array(5)
    payload[0] = enabled ? 1 : 0
    payload[1] = start & 0xff
    payload[2] = (start >> 8) & 0xff
    payload[3] = end & 0xff
    payload[4] = (end >> 8) & 0xff
    await this.command(Opcode.PUT_QUIET, payload)
    await this.pull()
  }

  async setScent(name: string): Promise<void> {
    const payload = encodeScent(1, name)
    if (!payload) throw new Error('향 이름은 1~32바이트입니다.')
    await this.command(Opcode.SET_SCENT, payload)
    await this.pull()
  }

  async identify(): Promise<void> {
    await this.command(Opcode.IDENTIFY, new Uint8Array())
    await this.pull()
  }

  async setMode(mode: 'auto' | 'hold'): Promise<void> {
    await this.command(Opcode.SET_MODE, Uint8Array.of(mode === 'auto' ? 0 : 1))
    await this.pull()
  }

  async decide(action: 'accept' | 'snooze' | 'pin', level = 0, snoozeMin = 30): Promise<void> {
    const code = action === 'accept' ? Decision.ACCEPT : action === 'snooze' ? Decision.SNOOZE : Decision.PIN
    const payload = new Uint8Array(4)
    payload[0] = code
    payload[1] = snoozeMin & 0xff
    payload[2] = (snoozeMin >> 8) & 0xff
    payload[3] = level & 0xff
    await this.command(Opcode.APPLY_DECISION, payload)
    await this.pull()
  }

  async burst(level: number): Promise<{ onMs: number; offMs: number; clamped: boolean }> {
    const data = await this.command(Opcode.MANUAL_BURST, Uint8Array.of(level))
    await this.pull()
    return {
      onMs: readU16(data, 0),
      offMs: readU16(data, 2),
      clamped: data[4] === 1,
    }
  }

  private async command(opcode: number, payload: Uint8Array): Promise<Uint8Array> {
    const seq = this.seq
    this.seq = (this.seq + 1) & 0xff
    if (this.seq === 0) this.seq = 1
    const raw = await this.transport.writeCharacteristic(UUID.cmd, encodeFrame(opcode, seq, payload))
    if (!raw) throw new Error('응답이 없습니다.')
    const frame = decodeFrame(raw)
    if (frame.seq !== seq || frame.opcode !== (opcode | 0x80)) {
      throw new Error('응답 프레임이 요청과 맞지 않습니다.')
    }
    if (opcode === Opcode.GET_STATE) {
      if (frame.payload.length === LIMITS.STATE_BYTES) return frame.payload
      throw new Error(statusMessage(frame.payload[0] ?? 0xff))
    }
    const status = frame.payload[0]
    if (status !== Status.OK) throw new Error(statusMessage(status))
    return frame.payload.subarray(1)
  }

  private async pull(): Promise<void> {
    const gen = ++this.pullGen
    const state = decodeState(await this.command(Opcode.GET_STATE, new Uint8Array()))
    const schedule = decodeSchedule(await this.command(Opcode.GET_SCHEDULE, new Uint8Array()))
    const proposalRaw = await this.command(Opcode.GET_PROPOSAL, new Uint8Array())
    const guardRaw = await this.command(Opcode.GET_GUARD, new Uint8Array())
    const logRaw = await this.command(Opcode.GET_LOG, new Uint8Array())
    const info = await this.command(Opcode.GET_INFO, new Uint8Array())
    if (gen !== this.pullGen) return
    const guard = decodeGuard(guardRaw)
    const proposal = decodeProposal(proposalRaw)
    const meta = decodeMeta(proposalRaw)
    this.cache = {
      radio: this.transport.radio,
      state,
      faults: faultLabels(state.fault),
      schedule,
      proposal,
      mode: proposalRaw[11] === 1 ? 'hold' : 'auto',
      quiet: meta.quiet,
      guard,
      log: decodeLog(logRaw),
      scentName: meta.name,
      scentSku: meta.sku,
      serial: new TextDecoder().decode(info.subarray(4, 4 + info[3])),
      hw: info[0],
      remainingPercent:
        guard.lifeSeconds > 0
          ? Math.max(0, Math.round(((guard.lifeSeconds - state.spraySeconds) / guard.lifeSeconds) * 100))
          : null,
    }
    this.listener?.()
  }
}

function concat(parts: Uint8Array[]): Uint8Array {
  const out = new Uint8Array(parts.reduce((sum, part) => sum + part.length, 0))
  let offset = 0
  for (const part of parts) {
    out.set(part, offset)
    offset += part.length
  }
  return out
}

function decodeProposal(data: Uint8Array): ProposalView | null {
  if (data.length < 13 || data[0] !== 1) return null
  const count = data[4]
  const reasons = []
  for (let i = 0; i < count; i++) {
    const code = data[5 + i]
    if (!code) continue
    reasons.push({ code, text: reasonText(code) })
  }
  return {
    level: data[1],
    windowSec: readU16(data, 2),
    reasons,
    quietNow: data[12] === 1,
  }
}

function decodeGuard(data: Uint8Array): Guard {
  const duty = []
  for (let i = 0; i < 5; i++) {
    const offset = 11 + i * 4
    duty.push({ on: readU16(data, offset), off: readU16(data, offset + 2) })
  }
  return {
    maxOnMs: readU16(data, 0),
    cooldownMs: readU16(data, 2),
    cooldownLeftMs: readU16(data, 4),
    hot: data[6] === 1,
    lifeSeconds: readU32(data, 7),
    duty,
  }
}

function decodeLog(data: Uint8Array): LogView[] {
  const count = data[0] ?? 0
  const rows: LogView[] = []
  for (let i = 0; i < count; i++) {
    const offset = 1 + i * 7
    const kind = data[offset + 4]
    rows.push({
      t: readU32(data, offset),
      kind,
      level: data[offset + 5],
      reason: data[offset + 6],
      text: LOG_KO[kind] ?? '기록',
    })
  }
  return rows
}

function decodeMeta(data: Uint8Array): {
  quiet: { enabled: boolean; start: number; end: number; now: boolean }
  sku: number
  name: string
} {
  const nameLen = data[22] ?? 0
  return {
    quiet: {
      enabled: data[13] === 1,
      start: readU16(data, 14),
      end: readU16(data, 16),
      now: data[12] === 1,
    },
    sku: readU32(data, 18),
    name: new TextDecoder().decode(data.subarray(23, 23 + nameLen)),
  }
}
