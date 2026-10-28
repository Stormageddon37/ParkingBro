# ParkingBro
The user-friendly Android app that helps you remember where you parked

## Releasing

Publishing a GitHub Release builds a signed release APK and attaches it as `ParkingBro-<tag>.apk`.

One-time setup:

1. Generate a keystore:
   `keytool -genkeypair -v -keystore release.keystore -alias parkingbro -keyalg RSA -keysize 2048 -validity 10000`
2. Base64-encode it: `base64 -w0 release.keystore`
3. Add four GitHub repo secrets: `KEYSTORE_BASE64` (the output above), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (`parkingbro`), `KEY_PASSWORD`.

Keep `release.keystore` backed up and out of git. The first signed release can't be installed over an existing debug build; uninstall the debug app first.
