import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import { SimulatedFirmware } from './firmware'
import { crc16, encodeFrame, decodeFrame, Opcode, Status, LIMITS } from './protocol'
import { REASON_KO } from './reasons'
import { Reason } from './protocol'
import { Session } from './session'
import { SimulatorTransport } from './transport'

const AFTERNOON = Math.floor(Date.UTC(2026, 9, 24, 15, 0, 0) / 1000)

function rig(unix = AFTERNOON) {
  let now = 5_000_000
  const firmware = new SimulatedFirmware(() => now)
  const link = new SimulatorTransport(firmware)
  const session = new Session(link)
  return {
    firmware,
    link,
    session,
    tick(ms: number) {
      now += ms
      firmware.advance(ms)
    },
    async pair() {
      assert.equal((await session.scan()).length, 0)
      firmware.openPairingWindow()
      const found = await session.scan()
      assert.equal(found.length, 1)
      assert.equal(found[0].rssiDbm, null)
      assert.equal(JSON.stringify(found[0]).includes('포근한'), false)
      await session.connect(found[0].id)
      await session.setTime(unix, 0)
    },
  }
}

describe('frames', () => {
  it('locks CRC-16/CCITT-FALSE', () => {
    assert.equal(crc16(new TextEncoder().encode('123456789')), 0x29b1)
  })

  it('round-trips a level command and rejects a bad crc', () => {
    const raw = encodeFrame(Opcode.SET_LEVEL, 3, Uint8Array.of(4))
    const frame = decodeFrame(raw)
    assert.equal(frame.opcode, Opcode.SET_LEVEL)
    assert.equal(frame.payload[0], 4)
    raw[raw.length - 1] ^= 0xff
    const firmware = new SimulatedFirmware(() => 10)
    firmware.bondToken = 'x'
    const response = decodeFrame(firmware.handle(raw))
    assert.equal(response.payload[0], Status.BAD_CRC)
  })
})

describe('simulator peripheral', () => {
  it('bonds only inside the button window and keeps a single slot', async () => {
    const box = rig()
    await box.pair()
    const stranger = new SimulatorTransport(box.firmware)
    assert.equal((await stranger.scan()).length, 0)
    await assert.rejects(() => stranger.connect(box.firmware.id), /페어링 창/)
    box.link.forgetBond()
    await assert.rejects(() => box.session.connect(box.firmware.id), /페어링 창/)
    box.firmware.openPairingWindow()
    await box.session.connect(box.firmware.id)
    assert.equal(box.session.view()?.serial, 'SIM0001')
  })

  it('rejects level 0, overlaps, and an empty cartridge', async () => {
    const box = rig()
    await box.pair()
    await assert.rejects(() => box.session.setLevel(0), /범위/)
    await box.session.setLevel(4)
    assert.equal(box.session.view()?.state.level, 4)
    const saturday = 1 << 6
    await box.session.putSchedule([{ weekdayMask: saturday, startMin: 14 * 60, endMin: 16 * 60, level: 5, flags: 1 }])
    assert.equal(box.session.view()?.state.run, 1)
    assert.equal(box.session.view()?.state.level, 5)
    await assert.rejects(
      () =>
        box.session.putSchedule([
          { weekdayMask: saturday, startMin: 14 * 60, endMin: 16 * 60, level: 5, flags: 1 },
          { weekdayMask: saturday, startMin: 15 * 60, endMin: 17 * 60, level: 2, flags: 1 },
        ]),
      /이전 저장/,
    )
    await box.session.identify()
    assert.equal(box.session.view()?.schedule.length, 1)
    await box.session.putSchedule([{ weekdayMask: saturday, startMin: 9 * 60, endMin: 10 * 60, level: 2, flags: 1 }])
    assert.equal(box.session.view()?.schedule.length, 1)
    assert.equal(box.session.view()?.schedule[0].level, 2)
    assert.equal(box.session.view()?.state.level, 4)
  })

  it('runs a saved schedule from the device clock and pauses it for quiet hours or a lost clock', async () => {
    const box = rig()
    await box.pair()
    const saturday = 1 << 6
    await box.session.putSchedule([{ weekdayMask: saturday, startMin: 14 * 60, endMin: 16 * 60, level: 3, flags: 1 }])
    assert.equal(box.session.view()?.state.timeValid, 1)
    assert.equal(box.session.view()?.state.run, 1)
    await box.session.putQuiet(true, 14 * 60, 18 * 60)
    assert.equal(box.session.view()?.state.run, 0)
    await box.session.setRun(true)
    assert.equal(box.session.view()?.state.run, 1)
    await box.session.setRun(false)
    box.firmware.loseClock()
    await box.session.identify()
    assert.equal(box.session.view()?.state.timeValid, 0)
    assert.equal(box.session.view()?.state.run, 0)
  })

  it('accepts then pins a proposal without letting the phone rewrite mesh limits', async () => {
    const box = rig()
    await box.pair()
    const proposal = box.session.view()?.proposal
    assert.ok(proposal)
    assert.ok(proposal.reasons.every((reason) => reason.text === REASON_KO[reason.code]))
    assert.ok(proposal.reasons.some((reason) => reason.code === Reason.CARTRIDGE_LIFE))
    await box.session.decide('accept', proposal.level)
    assert.equal(box.session.view()?.state.level, proposal.level)
    assert.ok(box.session.view()?.log.some((row) => row.text === '받아들임'))
    await box.session.decide('pin', proposal.level)
    assert.equal(box.session.view()?.mode, 'hold')
    assert.equal(box.session.view()?.proposal, null)
    assert.ok(box.session.view()?.log.some((row) => row.text === '고정'))
    await box.session.setLevel(5)
    await box.session.setMode('auto')
    assert.equal(box.session.view()?.state.level, 5)
    assert.equal(box.session.view()?.guard.maxOnMs, LIMITS.MAX_ON_MS)
    assert.ok(box.session.view()?.guard.duty.every((row) => row.on <= LIMITS.MAX_ON_MS))
    const burst = await box.session.burst(2)
    assert.equal(burst.clamped, false)
    assert.ok(burst.onMs <= LIMITS.MAX_ON_MS)
    await assert.rejects(() => box.session.burst(2), /식는/)
  })

  it('locks out a dry cartridge and starts a new life when it is reseated', async () => {
    const box = rig()
    await box.pair()
    box.firmware.forceEmpty()
    await box.session.identify()
    assert.equal(box.session.view()?.state.fault & 1, 1)
    assert.equal(box.session.view()?.remainingPercent, 0)
    assert.equal(box.session.view()?.state.run, 0)
    await assert.rejects(() => box.session.setRun(true), /잠겼/)
    await assert.rejects(() => box.session.burst(1), /잠겼/)
    box.firmware.setPresent(false)
    await box.session.identify()
    await assert.rejects(() => box.session.setRun(true), /꽂혀/)
    box.firmware.setPresent(true)
    await box.session.identify()
    assert.equal(box.session.view()?.state.present, 1)
    assert.equal(box.session.view()?.state.spraySeconds, 0)
    assert.equal(box.session.view()?.state.fault & 1, 0)
    await box.session.setRun(true)
    assert.equal(box.session.view()?.state.run, 1)
  })
})
