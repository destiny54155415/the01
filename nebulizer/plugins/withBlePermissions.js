const { withAndroidManifest } = require('expo/config-plugins')

function upsert(manifest, name, attrs) {
  const list = manifest['uses-permission'] || []
  const next = list.filter((item) => item.$['android:name'] !== name)
  next.push({ $: { 'android:name': name, ...attrs } })
  manifest['uses-permission'] = next
}

/** Foreground GATT only. Scan must not request location. */
module.exports = function withBlePermissions(config) {
  return withAndroidManifest(config, (cfg) => {
    const manifest = cfg.modResults.manifest
    upsert(manifest, 'android.permission.BLUETOOTH', { 'android:maxSdkVersion': '30' })
    upsert(manifest, 'android.permission.BLUETOOTH_ADMIN', { 'android:maxSdkVersion': '30' })
    upsert(manifest, 'android.permission.BLUETOOTH_CONNECT', {})
    upsert(manifest, 'android.permission.BLUETOOTH_SCAN', {
      'android:usesPermissionFlags': 'neverForLocation',
    })
    return cfg
  })
}
