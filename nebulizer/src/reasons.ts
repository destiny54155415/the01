import { Reason } from './protocol'

/** Local map from device reason codes. No network, no model. */
export const REASON_KO: Record<number, string> = {
  [Reason.TIME_OF_DAY]: '하루 중 시각을 보고 단수를 골랐습니다.',
  [Reason.RECENT_USE]: '직전 분사가 있어 한 단 낮췄습니다.',
  [Reason.CARTRIDGE_LIFE]: '카트리지 잔량이 적어 더 짧은 창을 제안합니다.',
  [Reason.MESH_COOLDOWN]: '메시 냉각 중이라 연속으로 켜지 않습니다.',
}

export function reasonText(code: number): string {
  return REASON_KO[code] ?? `알 수 없는 이유 코드 ${code.toString(16).padStart(2, '0')}`
}
