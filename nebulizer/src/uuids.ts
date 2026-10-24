/** Custom 128-bit service. Advertisements carry this UUID, never a scent name. */
export const UUID = {
  service: '8a1f0001-6e62-4c31-9a10-5d4e62756c31',
  cmd: '8a1f0002-6e62-4c31-9a10-5d4e62756c31',
  state: '8a1f0003-6e62-4c31-9a10-5d4e62756c31',
  schedule: '8a1f0004-6e62-4c31-9a10-5d4e62756c31',
  time: '8a1f0005-6e62-4c31-9a10-5d4e62756c31',
  info: '8a1f0006-6e62-4c31-9a10-5d4e62756c31',
  /** Reserved. No chunks, no URL pull, no user-facing update in v1. */
  ota: '8a1f0007-6e62-4c31-9a10-5d4e62756c31',
} as const
