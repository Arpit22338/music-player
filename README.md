Build instructions (CI) — produce a signed release APK

This project includes a GitHub Actions workflow that builds and signs a release APK automatically.

How to use (quick):

1. Push this repository to GitHub (recommended branch `main`).
2. Open the repository on GitHub → Actions → run the "Build Release APK" workflow (or push to `main`).
3. After the workflow finishes, download the artifact `app-release-signed.apk` from the workflow run.

Notes:
- The workflow generates a temporary keystore inside the runner (password `androidpass`) and signs the release APK with `jarsigner` (v1 signature). The produced APK should be installable on most devices.
- If you prefer a stable key you control, create a keystore locally and add it as an encrypted GitHub secret and modify the workflow to use it.
- If the build fails due to SDK/Gradle differences, open the workflow run logs for details. I can iterate on the workflow if needed.

If you want me to instead create a Docker build that runs entirely here (requires Docker on this machine), I can add a `Dockerfile` and build script next.
