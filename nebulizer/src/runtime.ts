import { SimulatedFirmware } from './firmware'
import { Session } from './session'
import { createTransport } from './transport'

export const firmware = new SimulatedFirmware()
export const transport = createTransport(firmware)
export const session = new Session(transport)
