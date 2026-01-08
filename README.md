Music Player — simple, private audio player

Hey — this is my personal lightweight music player. It scans your device for audio files, shows all songs and folders, supports hiding songs (long-press), has a shuffle toggle, a search/filter box, and a tab to view hidden tracks. No ads, no tracking, no branding.

How I publish builds

1) I push code to this repo (`main`). GitHub Actions builds a release APK automatically.
2) The workflow creates a GitHub Release and attaches the signed APK so anyone can download it from the Releases page.

Install the APK from Releases

- Go to the repository Releases: https://github.com/Arpit22338/music-player/releases
- Download the latest `app-release-signed.apk` from the most recent release.
- Transfer the APK to your phone (USB file transfer, SD card, Bluetooth) or download directly on your phone.
- On the phone, open the APK with a file manager and install. If you see a block, enable "Install unknown apps" for that file manager in Settings.

Developer notes

- Workflow: `.github/workflows/build-release.yml` builds and signs an APK and publishes it as a Release asset.
- To rebuild from your machine (requires JDK + Android SDK + Gradle):
```bash
./gradlew :app:assembleRelease
```
- If you want a stable keystore under your control, create one locally and update the workflow to use secrets for `KEYSTORE`, `KEYSTORE_PASS`, and `KEY_ALIAS`.

If you want any UI tweaks or background playback added, tell me what you want next.
idk idk