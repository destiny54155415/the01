/** BLE-independent command frames. Little-endian. CRC-16/CCITT-FALSE. */

export const VERSION = 1

export const Opcode = {
  GET_STATE: 0x01,
  SET_RUN: 0x02,
  SET_LEVEL: 0x03,
  SET_TIME: 0x04,
  PUT_SCHEDULE: 0x05,
  GET_SCHEDULE: 0x06,
  SET_SCENT: 0x07,
  IDENTIFY: 0x08,
  GET_INFO: 0x10,
  SET_MODE: 0x20,
  GET_PROPOSAL: 0x21,
  APPLY_DECISION: 0x22,
  GET_LOG: 0x23,
  MANUAL_BURST: 0x24,
  PUT_QUIET: 0x25,
  GET_GUARD: 0x26,
} as const

export const Status = {
  OK: 0x00,
  BAD_LENGTH: 0x01,
  BAD_CRC: 0x02,
  BAD_RANGE: 0x03,
  NO_CARTRIDGE: 0x04,
  BUSY_OTA: 0x05,
  NOT_BONDED: 0x06,
  MESH_LOCK: 0x07,
  DRY_LOCK: 0x08,
} as const

export const Fault = {
  DRY: 1 << 0,
  DRIVER: 1 << 1,
  OVERTEMP: 1 << 2,
  CLOCK_LOST: 1 << 3,
  SCHEDULE_REJECTED: 1 << 4,
} as const

export const Reason = {
  TIME_OF_DAY: 0x01,
  RECENT_USE: 0x02,
  CARTRIDGE_LIFE: 0x03,
  MESH_COOLDOWN: 0x04,
} as const

export const Decision = {
  ACCEPT: 1,
  SNOOZE: 2,
  PIN: 3,
} as const

export const LogKind = {
  PROPOSE: 1,
  ACCEPT: 2,
  SNOOZE: 3,
  PIN: 4,
  MESH_BLOCK: 5,
  DRY_STOP: 6,
} as const

export const Mode = { AUTO: 0, HOLD: 1 } as const

export const LIMITS = {
  MAX_ON_MS: 2500,
  COOLDOWN_MS: 15_000,
  MAX_RULES: 16,
  LIFE_SECONDS: 3600,
  MAX_NAME_BYTES: 32,
  STATE_BYTES: 12,
  MAX_LOG: 8,
} as const

/** Burst duty owned by firmware. On-time never exceeds MAX_ON_MS. */
export const DUTY: readonly { on: number; off: number }[] = [
  { on: 400, off: 9000 },
  { on: 700, off: 7000 },
  { on: 1100, off: 5000 },
  { on: 1600, off: 4000 },
  { on: 2200, off: 3200 },
]

for (const row of DUTY) {
  if (row.on > LIMITS.MAX_ON_MS) {
    throw new Error('duty table exceeds mesh max on-time')
  }
}

export type ScheduleRule = {
  weekdayMask: number
  startMin: number
  endMin: number
  level: number
  flags: number
}

export type DeviceState = {
  run: number
  level: number
  fault: number
  present: number
  spraySeconds: number
  timeValid: number
  fwMajor: number
  fwMinor: number
  supply: number
}

export type Frame = {
  version: number
  opcode: number
  seq: number
  payload: Uint8Array
}

export function crc16(data: Uint8Array): number {
  let crc = 0xffff
  for (let i = 0; i < data.length; i++) {
    crc ^= data[i] << 8
    for (let bit = 0; bit < 8; bit++) {
      crc = crc & 0x8000 ? ((crc << 1) ^ 0x1021) & 0xffff : (crc << 1) & 0xffff
    }
  }
  return crc
}

export function concat(parts: Uint8Array[]): Uint8Array {
  const out = new Uint8Array(parts.reduce((sum, part) => sum + part.length, 0))
  let offset = 0
  for (const part of parts) {
    out.set(part, offset)
    offset += part.length
  }
  return out
}

export function u16le(value: number): Uint8Array {
  return Uint8Array.of(value & 0xff, (value >> 8) & 0xff)
}

export function u32le(value: number): Uint8Array {
  const n = value >>> 0
  return Uint8Array.of(n & 0xff, (n >> 8) & 0xff, (n >> 16) & 0xff, (n >> 24) & 0xff)
}

export function i16le(value: number): Uint8Array {
  const n = value < 0 ? value + 0x10000 : value
  return u16le(n)
}

export function readU16(data: Uint8Array, offset: number): number {
  return data[offset] | (data[offset + 1] << 8)
}

export function readI16(data: Uint8Array, offset: number): number {
  const value = readU16(data, offset)
  return value & 0x8000 ? value - 0x10000 : value
}

export function readU32(data: Uint8Array, offset: number): number {
  return (
    (data[offset] |
      (data[offset + 1] << 8) |
      (data[offset + 2] << 16) |
      (data[offset + 3] << 24)) >>>
    0
  )
}

export function encodeFrame(opcode: number, seq: number, payload: Uint8Array): Uint8Array {
  if (payload.length > 255) throw new Error('payload too long')
  const body = new Uint8Array(4 + payload.length)
  body[0] = VERSION
  body[1] = opcode & 0xff
  body[2] = seq & 0xff
  body[3] = payload.length
  body.set(payload, 4)
  const crc = crc16(body)
  const out = new Uint8Array(body.length + 2)
  out.set(body)
  out[body.length] = crc & 0xff
  out[body.length + 1] = (crc >> 8) & 0xff
  return out
}

export function decodeFrame(buf: Uint8Array): Frame {
  if (buf.length < 6) throw new Error('short')
  const payloadLen = buf[3]
  if (buf.length !== 4 + payloadLen + 2) throw new Error('length')
  const body = buf.subarray(0, 4 + payloadLen)
  const got = buf[4 + payloadLen] | (buf[5 + payloadLen] << 8)
  if (crc16(body) !== got) throw new Error('crc')
  return {
    version: buf[0],
    opcode: buf[1],
    seq: buf[2],
    payload: buf.subarray(4, 4 + payloadLen),
  }
}

export function encodeState(state: DeviceState): Uint8Array {
  const out = new Uint8Array(LIMITS.STATE_BYTES)
  out[0] = state.run & 0xff
  out[1] = state.level & 0xff
  out[2] = state.fault & 0xff
  out[3] = state.present & 0xff
  out.set(u32le(state.spraySeconds), 4)
  out[8] = state.timeValid & 0xff
  out[9] = state.fwMajor & 0xff
  out[10] = state.fwMinor & 0xff
  out[11] = state.supply & 0xff
  return out
}

export function decodeState(data: Uint8Array): DeviceState {
  if (data.length !== LIMITS.STATE_BYTES) throw new Error('state length')
  return {
    run: data[0],
    level: data[1],
    fault: data[2],
    present: data[3],
    spraySeconds: readU32(data, 4),
    timeValid: data[8],
    fwMajor: data[9],
    fwMinor: data[10],
    supply: data[11],
  }
}

export function encodeSchedule(rules: ScheduleRule[]): Uint8Array {
  const out = new Uint8Array(1 + rules.length * 8)
  out[0] = rules.length
  rules.forEach((rule, index) => {
    const offset = 1 + index * 8
    out[offset] = rule.weekdayMask & 0xff
    out[offset + 1] = rule.startMin & 0xff
    out[offset + 2] = (rule.startMin >> 8) & 0xff
    out[offset + 3] = rule.endMin & 0xff
    out[offset + 4] = (rule.endMin >> 8) & 0xff
    out[offset + 5] = rule.level & 0xff
    out[offset + 6] = rule.flags & 0xff
    out[offset + 7] = 0
  })
  return out
}

export function decodeSchedule(data: Uint8Array): ScheduleRule[] {
  if (data.length < 1) throw new Error('schedule length')
  const count = data[0]
  if (data.length !== 1 + count * 8) throw new Error('schedule length')
  const rules: ScheduleRule[] = []
  for (let index = 0; index < count; index++) {
    const offset = 1 + index * 8
    rules.push({
      weekdayMask: data[offset],
      startMin: readU16(data, offset + 1),
      endMin: readU16(data, offset + 3),
      level: data[offset + 5],
      flags: data[offset + 6],
    })
  }
  return rules
}

export function validateSchedule(rules: ScheduleRule[]): 'count' | 'day' | 'time' | 'level' | 'overlap' | null {
  if (rules.length > LIMITS.MAX_RULES) return 'count'
  for (const rule of rules) {
    if (rule.weekdayMask === 0 || rule.weekdayMask > 0x7f) return 'day'
    if (rule.startMin < 0 || rule.startMin >= 1440 || rule.endMin <= rule.startMin || rule.endMin > 1440) {
      return 'time'
    }
    if (rule.level < 1 || rule.level > 5) return 'level'
  }
  for (let i = 0; i < rules.length; i++) {
    for (let j = i + 1; j < rules.length; j++) {
      const a = rules[i]
      const b = rules[j]
      if ((a.flags & 1) === 0 || (b.flags & 1) === 0) continue
      if ((a.weekdayMask & b.weekdayMask) === 0) continue
      if (a.startMin < b.endMin && b.startMin < a.endMin) return 'overlap'
    }
  }
  return null
}

export function encodeScent(sku: number, name: string): Uint8Array | null {
  const bytes = new TextEncoder().encode(name)
  if (bytes.length === 0 || bytes.length > LIMITS.MAX_NAME_BYTES) return null
  const out = new Uint8Array(5 + bytes.length)
  out.set(u32le(sku), 0)
  out[4] = bytes.length
  out.set(bytes, 5)
  return out
}

export function decodeScent(data: Uint8Array): { sku: number; name: string } | null {
  if (data.length < 5) return null
  const sku = readU32(data, 0)
  const length = data[4]
  if (data.length !== 5 + length || length > LIMITS.MAX_NAME_BYTES) return null
  const name = new TextDecoder().decode(data.subarray(5, 5 + length))
  return { sku, name }
}

export function statusMessage(status: number): string {
  switch (status) {
    case Status.BAD_LENGTH:
      return '길이가 맞지 않습니다.'
    case Status.BAD_CRC:
      return '프레임 검사가 맞지 않습니다.'
    case Status.BAD_RANGE:
      return '범위 밖입니다. 겹치는 예약은 기기가 거절하고 이전 저장을 유지합니다.'
    case Status.NO_CARTRIDGE:
      return '카트리지가 꽂혀 있지 않습니다.'
    case Status.BUSY_OTA:
      return '업데이트 자리에서는 이 명령을 받지 않습니다.'
    case Status.NOT_BONDED:
      return '본딩이 없습니다. 기기 버튼을 눌러 페어링 창을 여세요.'
    case Status.MESH_LOCK:
      return '메시가 식는 중입니다. 이 휴식은 앱에서 건너뛸 수 없습니다.'
    case Status.DRY_LOCK:
      return '잔량 추정이 0입니다. 메시 보호로 분사가 잠겼습니다.'
    default:
      return '기기가 명령을 처리하지 못했습니다.'
  }
}

export function faultLabels(fault: number): string[] {
  const labels: string[] = []
  if (fault & Fault.DRY) labels.push('건조')
  if (fault & Fault.DRIVER) labels.push('구동')
  if (fault & Fault.OVERTEMP) labels.push('과열')
  if (fault & Fault.CLOCK_LOST) labels.push('시계 상실')
  if (fault & Fault.SCHEDULE_REJECTED) labels.push('예약 거절')
  return labels
}
