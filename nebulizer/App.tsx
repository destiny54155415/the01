import { StatusBar } from 'expo-status-bar'
import { useEffect, useState } from 'react'
import { Pressable, ScrollView, StyleSheet, Text, TextInput, useWindowDimensions, View } from 'react-native'
import { Fault, type ScheduleRule } from './src/protocol'
import { firmware, session } from './src/runtime'
import type { ProposalView, SessionView } from './src/session'
import type { Advertisement } from './src/transport'

const DAYS = ['일', '월', '화', '수', '목', '금', '토']
const SCENES = [
  { level: 1, name: '옅은 아침', note: '짧게, 띄엄띄엄' },
  { level: 2, name: '맑은 낮', note: '가벼운 반복' },
  { level: 3, name: '거실', note: '기본 버스트' },
  { level: 4, name: '손님 맞이', note: '조금 더 자주' },
  { level: 5, name: '진한 저녁', note: '단수는 높아도 연속은 아님' },
]

type Tab = 'home' | 'schedule' | 'scenes' | 'log'

export default function App() {
  const { width } = useWindowDimensions()
  const wide = width >= 980
  const [rev, setRev] = useState(0)
  const [, setClock] = useState(0)

  useEffect(() => {
    if (typeof document !== 'undefined') document.title = '네뷸라이저'
    const stopWatch = firmware.watch(() => setRev((value) => value + 1))
    const stopSession = session.listen(() => setRev((value) => value + 1))
    const clock = setInterval(() => setClock((value) => value + 1), 250)
    const tick = setInterval(() => firmware.advance(1000), 1000)
    return () => {
      stopWatch()
      stopSession()
      clearInterval(clock)
      clearInterval(tick)
    }
  }, [])

  return (
    <View style={[styles.desk, wide ? styles.row : styles.stack]} testID="desk">
      <SimulatorPane rev={rev} />
      <Phone rev={rev} />
      <StatusBar style="dark" />
    </View>
  )
}

function SimulatorPane(_props: { rev: number }) {
  const peek = firmware.peek()
  const seconds = Math.ceil(peek.pairingMsLeft / 1000)
  return (
    <View style={styles.sim}>
      <Text style={styles.simKicker}>시뮬레이터</Text>
      <Text style={styles.simTitle}>같은 명령 모델</Text>
      <Text style={styles.simCopy}>이 상자는 전파를 내지 않습니다. 블루투스 라디오가 아닙니다.</Text>
      <View style={styles.chassis}>
        <View style={[styles.led, peek.ledOn ? styles.ledOn : null]} />
        <View style={styles.mist}>
          <Text style={styles.mistText}>{peek.run ? '버스트' : '대기'}</Text>
          <Text style={styles.mistSub}>{peek.level}단</Text>
        </View>
        <Text style={styles.chassisName}>네뷸라이저</Text>
      </View>
      <Text style={styles.simMeta} testID="pair-countdown">
        {seconds > 0 ? `페어링 창 ${seconds}초` : '페어링 창 닫힘'}
        {peek.bonded ? ' · 본딩 슬롯 사용' : ' · 본딩 없음'}
      </Text>
      <SimButton testID="sim-pair" label="페어링 버튼" onPress={() => firmware.openPairingWindow()} />
      <SimButton testID="sim-power" label="본체 전원" onPress={() => firmware.pressPower()} />
      <SimButton
        testID="sim-present"
        label={peek.present ? '카트리지 빼기' : '카트리지 꽂기'}
        onPress={() => firmware.setPresent(!peek.present)}
      />
      <SimButton testID="sim-empty" label="잔량 소진" onPress={() => firmware.forceEmpty()} />
      <SimButton testID="sim-clock" label="시계 상실" onPress={() => firmware.loseClock()} />
      <SimButton testID="sim-reset" label="공장 초기화" onPress={() => firmware.factoryReset()} />
      <Text style={styles.simFoot}>
        {peek.present ? '장착됨' : '비어 있음'}
        {peek.dry ? ' · 건조 잠금' : ''}
        {peek.timeValid ? ' · 시각 유효' : ' · 시각 없음'}
      </Text>
    </View>
  )
}

function SimButton({ testID, label, onPress }: { testID: string; label: string; onPress: () => void }) {
  return (
    <Pressable testID={testID} style={styles.simBtn} onPress={onPress}>
      <Text style={styles.simBtnText}>{label}</Text>
    </Pressable>
  )
}

function Phone({ rev }: { rev: number }) {
  const [phase, setPhase] = useState<'pair' | 'app'>('pair')
  const [tab, setTab] = useState<Tab>('home')
  const [found, setFound] = useState<Advertisement | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [held, setHeld] = useState<ProposalView | null>(null)
  const [rules, setRules] = useState<ScheduleRule[] | null>(null)
  const [days, setDays] = useState(0)
  const [startText, setStartText] = useState('09:00')
  const [endText, setEndText] = useState('11:00')
  const [draftLevel, setDraftLevel] = useState(3)
  const [quietOn, setQuietOn] = useState(true)
  const [quietStart, setQuietStart] = useState('23:00')
  const [quietEnd, setQuietEnd] = useState('07:00')
  const [quietReady, setQuietReady] = useState(false)
  const [saved, setSaved] = useState(false)
  const [scentDraft, setScentDraft] = useState('')
  const [burstNote, setBurstNote] = useState<string | null>(null)
  const view = session.view()
  const bonded = firmware.peek().bonded

  useEffect(() => {
    if (phase === 'app' && !bonded) {
      setPhase('pair')
      setFound(null)
      setHeld(null)
      setError('기기에서 본딩이 지워졌습니다. 페어링 버튼을 다시 누르세요.')
    }
  }, [bonded, phase, rev])

  useEffect(() => {
    if (!view || quietReady) return
    setQuietOn(view.quiet.enabled)
    setQuietStart(formatMin(view.quiet.start))
    setQuietEnd(formatMin(view.quiet.end))
    setScentDraft(view.scentName)
    setQuietReady(true)
  }, [view, quietReady])

  async function run(action: () => Promise<void>) {
    setError(null)
    setBusy(true)
    try {
      await action()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '명령을 보내지 못했습니다.')
    } finally {
      setBusy(false)
    }
  }

  const shownRules: ScheduleRule[] = rules ?? view?.schedule ?? []
  const dry = Boolean(view && ((view.state.fault & Fault.DRY) !== 0 || view.remainingPercent === 0))

  return (
    <View style={styles.phone} testID="phone">
      <ScrollView contentContainerStyle={styles.phoneInner}>
        <Text style={styles.brand}>네뷸라이저</Text>
        <Text style={styles.tag}>생활 방향 · 같은 방 · 기기 시계가 실행</Text>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>로컬 전용 · 계정 없음</Text>
        </View>
        <Text style={styles.disclaimer}>생활 방향입니다. 치료 기기나 가습기가 아닙니다.</Text>
        {error ? (
          <Text style={styles.error} testID="error-banner">
            {error}
          </Text>
        ) : null}
        {phase === 'pair' ? (
          <Pairing
            found={found}
            busy={busy}
            onScan={() =>
              run(async () => {
                const ads = await session.scan()
                if (!ads.length) {
                  setFound(null)
                  throw new Error('페어링 창이 닫혀 있습니다. 기기 버튼을 누르세요.')
                }
                setFound(ads[0])
              })
            }
            onBond={() =>
              run(async () => {
                if (!found) return
                await session.connect(found.id)
                setPhase('app')
                setTab('home')
              })
            }
          />
        ) : view ? (
          <>
            <View style={styles.tabs}>
              <TabButton id="home" label="홈" tab={tab} setTab={setTab} />
              <TabButton id="schedule" label="예약" tab={tab} setTab={setTab} />
              <TabButton id="scenes" label="장면" tab={tab} setTab={setTab} />
              <TabButton id="log" label="기록" tab={tab} setTab={setTab} />
            </View>
            {tab === 'home' ? (
              <Home
                view={view}
                dry={dry}
                busy={busy}
                held={held}
                onRun={(on) => run(() => session.setRun(on))}
                onLevel={(level) => run(() => session.setLevel(level))}
                onAccept={() =>
                  run(async () => {
                    const proposal = view.proposal
                    if (!proposal) return
                    setHeld(proposal)
                    await session.decide('accept', proposal.level)
                  })
                }
                onSnooze={() => run(() => session.decide('snooze', view.proposal?.level ?? 0, 30))}
                onPin={(level) =>
                  run(async () => {
                    await session.decide('pin', level)
                    setHeld(null)
                  })
                }
                onMode={(mode) => run(() => session.setMode(mode))}
              />
            ) : null}
            {tab === 'schedule' ? (
              <Schedule
                view={view}
                rules={shownRules}
                days={days}
                startText={startText}
                endText={endText}
                draftLevel={draftLevel}
                quietOn={quietOn}
                quietStart={quietStart}
                quietEnd={quietEnd}
                saved={saved}
                busy={busy}
                setDays={setDays}
                setStartText={setStartText}
                setEndText={setEndText}
                setDraftLevel={setDraftLevel}
                setQuietOn={setQuietOn}
                setQuietStart={setQuietStart}
                setQuietEnd={setQuietEnd}
                onAdd={() => {
                  if (!days) {
                    setError('요일을 고르세요.')
                    return
                  }
                  const startMin = parseClock(startText)
                  const endMin = parseClock(endText)
                  if (startMin === null || endMin === null || endMin <= startMin) {
                    setError('끝 시각은 시작보다 뒤여야 합니다. 자정을 넘기면 예약을 둘로 나누세요.')
                    return
                  }
                  setError(null)
                  setSaved(false)
                  setRules([...shownRules, { weekdayMask: days, startMin, endMin, level: draftLevel, flags: 1 }])
                  setDays(0)
                }}
                onRemove={(index) => {
                  setSaved(false)
                  setRules(shownRules.filter((_, item) => item !== index))
                }}
                onSave={() =>
                  run(async () => {
                    if (!view.state.timeValid) throw new Error('시각이 없습니다. 먼저 기기 시계를 맞추세요.')
                    const startMin = parseClock(quietStart)
                    const endMin = parseClock(quietEnd)
                    if (startMin === null || endMin === null) throw new Error('조용한 시간 형식이 아닙니다.')
                    await session.putSchedule(shownRules)
                    await session.putQuiet(quietOn, startMin, endMin)
                    setRules(session.view()?.schedule ?? shownRules)
                    setSaved(true)
                  })
                }
                onSync={() => run(() => session.syncClock())}
              />
            ) : null}
            {tab === 'scenes' ? (
              <Scenes
                view={view}
                note={burstNote}
                busy={busy}
                onScene={(level) => run(() => session.setLevel(level))}
                onBurst={(level) =>
                  run(async () => {
                    const result = await session.burst(level)
                    setBurstNote(
                      result.clamped
                        ? `기기가 연속 켜짐을 ${msText(result.onMs)}로 잘랐습니다. 이어서 ${msText(result.offMs)} 쉽니다.`
                        : `한 번 분사 ${msText(result.onMs)} · 쉼 ${msText(result.offMs)}. 연속 상한은 기기가 갖고 있습니다.`,
                    )
                  })
                }
              />
            ) : null}
            {tab === 'log' ? (
              <Log
                view={view}
                scentDraft={scentDraft}
                setScentDraft={setScentDraft}
                busy={busy}
                onScent={() => run(() => session.setScent(scentDraft))}
                onIdentify={() => run(() => session.identify())}
              />
            ) : null}
          </>
        ) : null}
      </ScrollView>
    </View>
  )
}

function Pairing({
  found,
  busy,
  onScan,
  onBond,
}: {
  found: Advertisement | null
  busy: boolean
  onScan: () => void
  onBond: () => void
}) {
  return (
    <View style={styles.card} testID="pairing-screen">
      <Text style={styles.simChip}>시뮬레이터 · 블루투스 라디오 아님</Text>
      <Text style={styles.h2}>페어링</Text>
      <Text style={styles.body}>
        본체 버튼을 누르면 30초만 본딩할 수 있습니다. 창이 닫히면 다른 폰은 붙지 않습니다. 슬롯은 하나입니다.
      </Text>
      <Pressable testID="app-scan" style={styles.primary} onPress={onScan} disabled={busy}>
        <Text style={styles.primaryText}>창 안에서 찾기</Text>
      </Pressable>
      {found ? (
        <View style={styles.found}>
          <Text style={styles.h2}>{found.name}</Text>
          <Text style={styles.body}>프로토콜 {found.protocolVersion}</Text>
          <Text style={styles.body}>{found.cartridgePresent ? '카트리지 장착' : '카트리지 없음'}</Text>
          <Text style={styles.body}>고장 니블 {found.faultNibble.toString(16)}</Text>
          <Text style={styles.meta}>신호 세기 없음 · 시뮬레이터 광고</Text>
          <Text style={styles.meta}>{found.serviceUuid}</Text>
          <Pressable testID="bond-button" style={styles.primary} onPress={onBond} disabled={busy}>
            <Text style={styles.primaryText}>이 기기에 본딩</Text>
          </Pressable>
        </View>
      ) : null}
    </View>
  )
}

function Home({
  view,
  dry,
  busy,
  held,
  onRun,
  onLevel,
  onAccept,
  onSnooze,
  onPin,
  onMode,
}: {
  view: SessionView
  dry: boolean
  busy: boolean
  held: ProposalView | null
  onRun: (on: boolean) => void
  onLevel: (level: number) => void
  onAccept: () => void
  onSnooze: () => void
  onPin: (level: number) => void
  onMode: (mode: 'auto' | 'hold') => void
}) {
  const level = view.state.level
  const duty = view.guard.duty[level - 1]
  const proposal = view.proposal ?? held
  const pending = Boolean(view.proposal)
  return (
    <View testID="home-screen">
      <View testID="lockout-shot">
        {dry ? (
          <View style={styles.lockout} testID="lockout-banner">
            <Text style={styles.lockTitle}>분사가 잠겼습니다</Text>
            <Text style={styles.lockBody}>
              잔량 추정 {view.remainingPercent ?? 0}%. 건조이거나 수명이 끝나 기기가 켜기를 거절합니다. 이 잠금을 푸는 명령은 앱에
              없습니다.
            </Text>
          </View>
        ) : null}
        <View style={styles.card} testID="control-panel">
          <Text style={styles.simChip}>시뮬레이터 · 블루투스 라디오 아님</Text>
          <View style={styles.rowBetween}>
            <Text style={styles.h2}>{view.state.run ? '동작 중' : '멈춤'}</Text>
            <Text style={styles.mode} testID="mode-label">
              {view.mode === 'hold' ? '고정' : '자동'}
            </Text>
          </View>
          <Pressable
            testID="run-toggle"
            style={[styles.primary, dry || view.state.present === 0 ? styles.disabled : null]}
            disabled={busy || dry || view.state.present === 0}
            onPress={() => onRun(view.state.run === 0)}
          >
            <Text style={styles.primaryText}>{dry ? '잠김' : view.state.run ? '끄기' : '켜기'}</Text>
          </Pressable>
          <Text style={styles.label}>강도 1–5 · 버스트 간격 · 전압 아님</Text>
          <View style={styles.levels}>
            {[1, 2, 3, 4, 5].map((item) => (
              <Pressable
                key={item}
                testID={`intensity-${item}`}
                style={[styles.level, level === item ? styles.levelOn : null]}
                onPress={() => onLevel(item)}
                disabled={busy}
              >
                <Text style={[styles.levelText, level === item ? styles.levelTextOn : null]}>{item}</Text>
              </Pressable>
            ))}
          </View>
          {duty ? (
            <Text style={styles.body}>
              지금 단수는 한 번에 {msText(duty.on)}, 이어서 {msText(duty.off)} 쉽니다. 연속 상한 {msText(view.guard.maxOnMs)}은
              기기 표입니다.
            </Text>
          ) : null}
          <Text style={styles.meta}>자동에서는 기기가 단수를 다시 제안할 수 있습니다. 유지하려면 고정하세요.</Text>
          <View style={styles.chips}>
            <Chip text={view.state.run ? '동작' : '정지'} />
            <Chip text={`${level}단`} />
            <Chip text={view.faults.length ? view.faults.join(' ') : '고장 없음'} />
            <Chip text={view.state.present ? '장착됨' : '없음'} />
            <Chip text={view.state.timeValid ? '시각 유효' : '시각 없음'} />
            <Chip text={`펌웨어 ${view.state.fwMajor}.${view.state.fwMinor}`} />
            <Chip text={view.state.supply === 1 ? 'USB 상시' : '전원 기타'} />
          </View>
        </View>
        <View style={styles.card} testID="cartridge-card">
          <Text style={styles.h2}>{view.scentName || '이름 없는 향'}</Text>
          <Text style={styles.body}>
            약 {view.remainingPercent ?? 0}% 남음 · 추정. 수위 센서가 아니라 누적 분사 {view.state.spraySeconds}초입니다.
          </Text>
        </View>
        <View style={styles.card} testID="mesh-card">
          <Text style={styles.h2}>메시 보호</Text>
          <Text style={styles.body}>연속 켜짐 최대 {msText(view.guard.maxOnMs)}</Text>
          <Text style={styles.body}>
            수동 분사 뒤 휴식 {msText(view.guard.cooldownMs)}
            {view.guard.cooldownLeftMs > 0 ? ` · 남은 ${msText(view.guard.cooldownLeftMs)}` : ''}
          </Text>
          <Text style={styles.meta}>이 값은 읽을 수만 있습니다. 우회 명령이 없습니다.</Text>
        </View>
      </View>
      {dry ? null : (
        <View style={styles.card} testID="decision-card">
          <Text style={styles.simChip}>기기 코드 · 문장은 이 폰에 있음 · 클라우드 없음</Text>
          <View style={styles.rowBetween}>
            <Text style={styles.h2}>PCB 제안</Text>
            <View style={styles.modeSwitch}>
              <Pressable testID="mode-auto" onPress={() => onMode('auto')}>
                <Text style={[styles.modeMini, view.mode === 'auto' ? styles.modeMiniOn : null]}>자동</Text>
              </Pressable>
              <Pressable testID="mode-hold" onPress={() => onMode('hold')}>
                <Text style={[styles.modeMini, view.mode === 'hold' ? styles.modeMiniOn : null]}>고정</Text>
              </Pressable>
            </View>
          </View>
          {proposal ? (
            <>
              <Text style={styles.body}>
                {proposal.level}단 · {Math.round(proposal.windowSec / 60)}분 창. 이 시간은 연속 분사가 아닙니다.
              </Text>
              {proposal.quietNow ? (
                <Text style={styles.meta}>지금은 조용한 시간입니다. 받아들이면 단수만 반영되고 자동 분사는 쉬어 있습니다.</Text>
              ) : null}
              <View testID="reason-list">
                {proposal.reasons.map((reason) => (
                  <Text key={reason.code} style={styles.reason}>
                    코드 {reason.code.toString(16).padStart(2, '0')} · {reason.text}
                  </Text>
                ))}
              </View>
              {pending ? (
                <View style={styles.actions}>
                  <Pressable testID="decision-accept" style={styles.primary} onPress={onAccept} disabled={busy}>
                    <Text style={styles.primaryText}>받아들이기</Text>
                  </Pressable>
                  <Pressable testID="decision-snooze" style={styles.secondary} onPress={onSnooze} disabled={busy}>
                    <Text style={styles.secondaryText}>30분 미루기</Text>
                  </Pressable>
                  <Pressable testID="decision-pin" style={styles.secondary} onPress={() => onPin(proposal.level)} disabled={busy}>
                    <Text style={styles.secondaryText}>이 단수로 고정</Text>
                  </Pressable>
                </View>
              ) : (
                <View style={styles.actions}>
                  <Text style={styles.body} testID="decision-accepted">
                    제안을 받아들였습니다.
                  </Text>
                  <Pressable testID="decision-pin" style={styles.secondary} onPress={() => onPin(proposal.level)} disabled={busy}>
                    <Text style={styles.secondaryText}>받아들인 단수로 고정</Text>
                  </Pressable>
                </View>
              )}
            </>
          ) : (
            <Text style={styles.body}>
              {view.mode === 'hold' ? '고정 중입니다. 기기 제안을 받지 않습니다.' : '대기 중인 제안이 없습니다.'}
            </Text>
          )}
        </View>
      )}
    </View>
  )
}

function Schedule(props: {
  view: SessionView
  rules: ScheduleRule[]
  days: number
  startText: string
  endText: string
  draftLevel: number
  quietOn: boolean
  quietStart: string
  quietEnd: string
  saved: boolean
  busy: boolean
  setDays: (days: number) => void
  setStartText: (value: string) => void
  setEndText: (value: string) => void
  setDraftLevel: (level: number) => void
  setQuietOn: (on: boolean) => void
  setQuietStart: (value: string) => void
  setQuietEnd: (value: string) => void
  onAdd: () => void
  onRemove: (index: number) => void
  onSave: () => void
  onSync: () => void
}) {
  return (
    <View style={styles.card} testID="schedule-screen">
      <Text style={styles.simChip}>시뮬레이터 · 블루투스 라디오 아님</Text>
      <Text style={styles.h2}>주간 예약</Text>
      <Text style={styles.body}>
        저장은 항상 전체 교체입니다. 폰을 꺼도 기기 시계가 실행합니다. 창 안의 실제 켜짐과 쉼은 기기 표입니다.
      </Text>
      {!props.view.state.timeValid ? (
        <Pressable style={styles.secondary} onPress={props.onSync}>
          <Text style={styles.secondaryText}>시각 맞추기</Text>
        </Pressable>
      ) : null}
      <Text style={styles.label}>조용한 시간 · 예약과 자동만 멈춤</Text>
      <Pressable testID="quiet-toggle" onPress={() => props.setQuietOn(!props.quietOn)}>
        <Text style={styles.body}>{props.quietOn ? '켜짐' : '꺼짐'}</Text>
      </Pressable>
      <View style={styles.inline}>
        <Field testID="quiet-start" value={props.quietStart} onChange={props.setQuietStart} />
        <Field testID="quiet-end" value={props.quietEnd} onChange={props.setQuietEnd} />
      </View>
      {props.view.quiet.now ? <Text style={styles.meta}>지금 조용한 시간입니다.</Text> : null}
      <Text style={styles.label}>새 예약</Text>
      <View style={styles.levels}>
        {DAYS.map((label, index) => {
          const on = (props.days & (1 << index)) !== 0
          return (
            <Pressable
              key={label}
              testID={`day-${index}`}
              style={[styles.day, on ? styles.levelOn : null]}
              onPress={() => props.setDays(props.days ^ (1 << index))}
            >
              <Text style={[styles.levelText, on ? styles.levelTextOn : null]}>{label}</Text>
            </Pressable>
          )
        })}
      </View>
      <View style={styles.inline}>
        <Field testID="start-time" value={props.startText} onChange={props.setStartText} />
        <Field testID="end-time" value={props.endText} onChange={props.setEndText} />
      </View>
      <View style={styles.levels}>
        {[1, 2, 3, 4, 5].map((item) => (
          <Pressable
            key={item}
            testID={`sched-level-${item}`}
            style={[styles.level, props.draftLevel === item ? styles.levelOn : null]}
            onPress={() => props.setDraftLevel(item)}
          >
            <Text style={[styles.levelText, props.draftLevel === item ? styles.levelTextOn : null]}>{item}</Text>
          </Pressable>
        ))}
      </View>
      <Pressable testID="add-rule" style={styles.secondary} onPress={props.onAdd}>
        <Text style={styles.secondaryText}>목록에 추가</Text>
      </Pressable>
      <View testID="rule-list">
        {props.rules.length === 0 ? <Text style={styles.meta}>저장된 예약 없음</Text> : null}
        {props.rules.map((rule, index) => (
          <View key={`${rule.weekdayMask}-${rule.startMin}-${index}`} style={styles.rule}>
            <Text style={styles.body}>
              {dayText(rule.weekdayMask)} {formatMin(rule.startMin)}–{formatMin(rule.endMin)} · {rule.level}단
            </Text>
            <Pressable onPress={() => props.onRemove(index)}>
              <Text style={styles.link}>빼기</Text>
            </Pressable>
          </View>
        ))}
      </View>
      <Pressable
        testID="schedule-save"
        style={[styles.primary, !props.view.state.timeValid ? styles.disabled : null]}
        disabled={props.busy || !props.view.state.timeValid}
        onPress={props.onSave}
      >
        <Text style={styles.primaryText}>기기에 전체 교체</Text>
      </Pressable>
      {props.saved ? (
        <Text testID="schedule-saved" style={styles.saved}>
          기기에 전체 교체했습니다.
        </Text>
      ) : null}
    </View>
  )
}

function Scenes({
  view,
  note,
  busy,
  onScene,
  onBurst,
}: {
  view: SessionView
  note: string | null
  busy: boolean
  onScene: (level: number) => void
  onBurst: (level: number) => void
}) {
  return (
    <View style={styles.card} testID="scenes-screen">
      <Text style={styles.h2}>장면</Text>
      <Text style={styles.body}>장면은 이름 붙은 버스트 단수입니다. 켜지는 시간과 쉬는 시간은 기기 표가 정합니다.</Text>
      {SCENES.map((scene) => {
        const duty = view.guard.duty[scene.level - 1]
        return (
          <View key={scene.level} style={styles.rule}>
            <View style={styles.flex}>
              <Text style={styles.body}>
                {scene.name} · {scene.level}단
              </Text>
              <Text style={styles.meta}>
                {scene.note}
                {duty ? ` · ${msText(duty.on)} / ${msText(duty.off)}` : ''}
              </Text>
            </View>
            <Pressable testID={`scene-${scene.level}`} onPress={() => onScene(scene.level)} disabled={busy}>
              <Text style={styles.link}>적용</Text>
            </Pressable>
          </View>
        )
      })}
      <Pressable testID="manual-burst" style={styles.primary} disabled={busy} onPress={() => onBurst(view.state.level)}>
        <Text style={styles.primaryText}>지금 단수로 한 번 분사</Text>
      </Pressable>
      {note ? <Text style={styles.body}>{note}</Text> : null}
    </View>
  )
}

function Log({
  view,
  scentDraft,
  setScentDraft,
  busy,
  onScent,
  onIdentify,
}: {
  view: SessionView
  scentDraft: string
  setScentDraft: (value: string) => void
  busy: boolean
  onScent: () => void
  onIdentify: () => void
}) {
  return (
    <View style={styles.card} testID="log-screen">
      <Text style={styles.h2}>기기 안의 기록</Text>
      <Text style={styles.body}>짧은 결정은 기기가 기억하고, 붙었을 때 읽습니다. 계정으로 올리지 않습니다.</Text>
      {view.log.length === 0 ? <Text style={styles.meta}>기록 없음</Text> : null}
      {view.log.map((entry, index) => (
        <Text key={`${entry.t}-${index}`} style={styles.reason}>
          {entry.text} · {entry.level}단
        </Text>
      ))}
      <Text style={styles.label}>향 이름</Text>
      <TextInput
        testID="scent-name"
        style={styles.input}
        value={scentDraft}
        onChangeText={setScentDraft}
        maxLength={32}
      />
      <Pressable testID="scent-save" style={styles.secondary} disabled={busy} onPress={onScent}>
        <Text style={styles.secondaryText}>향 이름 기록</Text>
      </Pressable>
      <Pressable testID="identify" style={styles.secondary} disabled={busy} onPress={onIdentify}>
        <Text style={styles.secondaryText}>불만 깜빡이기</Text>
      </Pressable>
      <Text style={styles.meta}>
        보드 {view.hw} · 식별 {view.serial} · 시뮬레이터 메모리. 초기화는 기기에서만 본딩을 지웁니다.
      </Text>
    </View>
  )
}

function TabButton({ id, label, tab, setTab }: { id: Tab; label: string; tab: Tab; setTab: (tab: Tab) => void }) {
  const on = tab === id
  return (
    <Pressable testID={`tab-${id}`} style={[styles.tab, on ? styles.tabOn : null]} onPress={() => setTab(id)}>
      <Text style={[styles.tabText, on ? styles.tabTextOn : null]}>{label}</Text>
    </Pressable>
  )
}

function Chip({ text }: { text: string }) {
  return (
    <View style={styles.chip}>
      <Text style={styles.chipText}>{text}</Text>
    </View>
  )
}

function Field({ testID, value, onChange }: { testID: string; value: string; onChange: (value: string) => void }) {
  return <TextInput testID={testID} style={styles.input} value={value} onChangeText={onChange} />
}

function formatMin(min: number): string {
  const hour = Math.floor(min / 60)
  const minute = min % 60
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

function parseClock(value: string): number | null {
  const match = /^(\d{1,2}):(\d{2})$/.exec(value.trim())
  if (!match) return null
  const hour = Number(match[1])
  const minute = Number(match[2])
  if (hour > 23 || minute > 59) return null
  return hour * 60 + minute
}

function dayText(mask: number): string {
  return DAYS.filter((_, index) => (mask & (1 << index)) !== 0).join('')
}

function msText(ms: number): string {
  const seconds = ms / 1000
  const text = Number.isInteger(seconds) ? String(seconds) : seconds.toFixed(1)
  return `${text}초`
}

const font = 'WenQuanYi Micro Hei'

const styles = StyleSheet.create({
  desk: { flex: 1, backgroundColor: '#cfc6b6', padding: 20, gap: 20 },
  row: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'center' },
  stack: { flexDirection: 'column', alignItems: 'center' },
  sim: { width: 300, backgroundColor: '#1b2420', borderRadius: 24, padding: 16, gap: 8 },
  simKicker: { fontFamily: font, color: '#d6ff6b', fontSize: 12, letterSpacing: 1 },
  simTitle: { fontFamily: font, color: '#f4efe6', fontSize: 22 },
  simCopy: { fontFamily: font, color: '#d5ddd6', fontSize: 13, lineHeight: 18 },
  chassis: {
    marginTop: 8,
    backgroundColor: '#101714',
    borderRadius: 20,
    padding: 16,
    alignItems: 'center',
    gap: 8,
  },
  led: { width: 14, height: 14, borderRadius: 7, backgroundColor: '#3d4a40' },
  ledOn: { backgroundColor: '#d6ff6b' },
  mist: {
    width: 120,
    height: 72,
    borderRadius: 36,
    backgroundColor: '#24352c',
    alignItems: 'center',
    justifyContent: 'center',
  },
  mistText: { fontFamily: font, color: '#f4efe6', fontSize: 14 },
  mistSub: { fontFamily: font, color: '#d6ff6b', fontSize: 12 },
  chassisName: { fontFamily: font, color: '#f4efe6', fontSize: 16 },
  simMeta: { fontFamily: font, color: '#d5ddd6', fontSize: 12 },
  simBtn: { backgroundColor: '#314239', borderRadius: 12, paddingVertical: 10, paddingHorizontal: 12 },
  simBtnText: { fontFamily: font, color: '#f4efe6', fontSize: 14, textAlign: 'center' },
  simFoot: { fontFamily: font, color: '#b7c4bb', fontSize: 12 },
  phone: {
    width: 400,
    maxWidth: '100%',
    minHeight: 760,
    backgroundColor: '#f3eee4',
    borderRadius: 28,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#c9bfb0',
  },
  phoneInner: { padding: 18, gap: 10, paddingBottom: 28 },
  brand: { fontFamily: font, fontSize: 32, color: '#1c1915' },
  tag: { fontFamily: font, color: '#5c564c', fontSize: 13 },
  badge: {
    alignSelf: 'flex-start',
    backgroundColor: '#123828',
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  badgeText: { fontFamily: font, color: '#f3eee4', fontSize: 12 },
  disclaimer: { fontFamily: font, color: '#5c564c', fontSize: 12 },
  error: { fontFamily: font, color: '#9c3b24', backgroundColor: '#f8e6de', padding: 10, borderRadius: 12 },
  card: { backgroundColor: '#fffaf3', borderRadius: 18, padding: 14, gap: 8, borderWidth: 1, borderColor: '#e4dacb' },
  simChip: { fontFamily: font, color: '#1e6b45', fontSize: 12 },
  h2: { fontFamily: font, fontSize: 18, color: '#1c1915' },
  body: { fontFamily: font, fontSize: 14, lineHeight: 20, color: '#1c1915' },
  meta: { fontFamily: font, fontSize: 12, lineHeight: 17, color: '#5c564c' },
  label: { fontFamily: font, fontSize: 12, color: '#5c564c' },
  primary: { backgroundColor: '#1e6b45', borderRadius: 12, paddingVertical: 12, alignItems: 'center' },
  primaryText: { fontFamily: font, color: '#f6f3ea', fontSize: 15 },
  secondary: { backgroundColor: '#efe6d6', borderRadius: 12, paddingVertical: 10, paddingHorizontal: 12, alignItems: 'center' },
  secondaryText: { fontFamily: font, color: '#1c1915', fontSize: 14 },
  disabled: { opacity: 0.45 },
  found: { gap: 6, marginTop: 8 },
  tabs: { flexDirection: 'row', gap: 6 },
  tab: { flex: 1, paddingVertical: 8, borderRadius: 999, backgroundColor: '#efe6d6', alignItems: 'center' },
  tabOn: { backgroundColor: '#123828' },
  tabText: { fontFamily: font, color: '#1c1915', fontSize: 13 },
  tabTextOn: { color: '#f3eee4' },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  mode: { fontFamily: font, color: '#1e6b45', fontSize: 14 },
  levels: { flexDirection: 'row', gap: 6, flexWrap: 'wrap' },
  level: {
    minWidth: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#efe6d6',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 8,
  },
  levelOn: { backgroundColor: '#1e6b45' },
  levelText: { fontFamily: font, color: '#1c1915' },
  levelTextOn: { color: '#f6f3ea' },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  chip: { backgroundColor: '#efe6d6', borderRadius: 999, paddingHorizontal: 8, paddingVertical: 4 },
  chipText: { fontFamily: font, fontSize: 12, color: '#1c1915' },
  lockout: { backgroundColor: '#f8e6de', borderRadius: 18, padding: 14, gap: 6, marginBottom: 8 },
  lockTitle: { fontFamily: font, fontSize: 20, color: '#9c3b24' },
  lockBody: { fontFamily: font, fontSize: 14, lineHeight: 20, color: '#6a2a1c' },
  reason: { fontFamily: font, fontSize: 14, lineHeight: 20, color: '#1c1915' },
  actions: { gap: 8 },
  modeSwitch: { flexDirection: 'row', gap: 8 },
  modeMini: { fontFamily: font, color: '#5c564c', fontSize: 13 },
  modeMiniOn: { color: '#1e6b45' },
  inline: { flexDirection: 'row', gap: 8 },
  input: {
    fontFamily: font,
    flex: 1,
    minWidth: 90,
    borderWidth: 1,
    borderColor: '#e4dacb',
    backgroundColor: '#fff',
    borderRadius: 10,
    paddingHorizontal: 10,
    paddingVertical: 8,
    color: '#1c1915',
  },
  day: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#efe6d6',
    alignItems: 'center',
    justifyContent: 'center',
  },
  rule: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 8 },
  link: { fontFamily: font, color: '#1e6b45', fontSize: 14 },
  saved: { fontFamily: font, color: '#1e6b45', fontSize: 14 },
  flex: { flex: 1 },
})
