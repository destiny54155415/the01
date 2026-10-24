import { SimulatedFirmware } from './firmware'
import { UUID } from './uuids'

export type Advertisement = {
  id: string
  name: string
  serviceUuid: string
  protocolVersion: number
  faultNibble: number
  cartridgePresent: boolean
  /** Null when the link is not a radio. Do not invent RSSI. */
  rssiDbm: number | null
}

/**
 * The phone speaks only this surface.
 * `simulator` is an in-process peripheral. `gatt` is the seam for a real radio.
 */
export interface NebulizerTransport {
  readonly kind: 'simulator' | 'gatt'
  readonly radio: boolean
  scan(): Promise<Advertisement[]>
  connect(id: string): Promise<void>
  disconnect(): Promise<void>
  forgetBond(): void
  writeCharacteristic(uuid: string, data: Uint8Array): Promise<Uint8Array | null>
  readCharacteristic(uuid: string): Promise<Uint8Array>
  subscribe(uuid: string, cb: (data: Uint8Array) => void): () => void
}

export class SimulatorTransport implements NebulizerTransport {
  readonly kind = 'simulator' as const
  readonly radio = false
  private phoneToken: string | null = null
  private connected = false

  constructor(readonly firmware: SimulatedFirmware) {}

  async scan(): Promise<Advertisement[]> {
    return this.firmware.advertisements(this.phoneToken)
  }

  async connect(id: string): Promise<void> {
    const result = this.firmware.acceptLink(id, this.phoneToken)
    if (!result.ok) throw new Error(result.message)
    this.phoneToken = result.token
    this.connected = true
  }

  async disconnect(): Promise<void> {
    this.connected = false
  }

  forgetBond(): void {
    this.phoneToken = null
    this.connected = false
  }

  async writeCharacteristic(uuid: string, data: Uint8Array): Promise<Uint8Array | null> {
    this.assertSession()
    if (uuid === UUID.cmd) return this.firmware.handle(data)
    if (uuid === UUID.schedule) return Uint8Array.of(this.firmware.writeScheduleBlob(data))
    if (uuid === UUID.time) return Uint8Array.of(this.firmware.writeTimeBlob(data))
    if (uuid === UUID.ota) {
      throw new Error('OTA 특성은 주소만 예약되어 있습니다.')
    }
    throw new Error('쓸 수 없는 특성입니다.')
  }

  async readCharacteristic(uuid: string): Promise<Uint8Array> {
    this.assertSession()
    if (uuid === UUID.state) return this.firmware.encodeStateBytes()
    if (uuid === UUID.schedule) return this.firmware.encodeScheduleBlob()
    if (uuid === UUID.info) return this.firmware.encodeInfo()
    throw new Error('읽을 수 없는 특성입니다.')
  }

  subscribe(uuid: string, cb: (data: Uint8Array) => void): () => void {
    if (uuid !== UUID.state) return () => undefined
    return this.firmware.onNotify(cb)
  }

  private assertSession(): void {
    if (!this.connected || !this.phoneToken || this.firmware.bondToken !== this.phoneToken) {
      this.connected = false
      throw new Error('본딩이 없습니다. 기기 버튼을 눌러 페어링 창을 여세요.')
    }
  }
}

/**
 * Placeholder for Core Bluetooth / Android GATT.
 * Scan filter is the service UUID. Pairing UI belongs to AccessorySetupKit
 * and Companion Device Manager; this class only does the GATT exchange after that.
 * v1 permissions, when a native project is generated: Android BLUETOOTH_SCAN
 * with neverForLocation, BLUETOOTH_CONNECT, and legacy Bluetooth up to API 30.
 * iOS needs the Bluetooth usage strings. Foreground only — no background relaunch claim.
 */
export class GattTransport implements NebulizerTransport {
  readonly kind = 'gatt' as const
  readonly radio = true

  async scan(): Promise<Advertisement[]> {
    throw new Error('이 빌드에는 GATT 라디오 모듈이 없습니다. 시뮬레이터 링크로 동작합니다.')
  }

  async connect(_id: string): Promise<void> {
    throw new Error('이 빌드에는 GATT 라디오 모듈이 없습니다.')
  }

  async disconnect(): Promise<void> {}

  forgetBond(): void {}

  async writeCharacteristic(): Promise<Uint8Array | null> {
    throw new Error('이 빌드에는 GATT 라디오 모듈이 없습니다.')
  }

  async readCharacteristic(): Promise<Uint8Array> {
    throw new Error('이 빌드에는 GATT 라디오 모듈이 없습니다.')
  }

  subscribe(): () => void {
    return () => undefined
  }
}

export function createTransport(firmware: SimulatedFirmware): NebulizerTransport {
  if (process.env.EXPO_PUBLIC_LINK === 'gatt') return new GattTransport()
  return new SimulatorTransport(firmware)
}
