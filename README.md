# HomeLandscapingMeasure

Native Android (Kotlin) app for home landscaping measurement with dual AR backends (Google ARCore and Huawei AR Engine).

## Build locally

Requirements: JDK 17, Android SDK (set `ANDROID_HOME` or create `local.properties` with `sdk.dir`).

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
```

On Windows:

```bat
gradlew.bat assembleDebug
```

## GitHub Actions

Pushes and PRs to `main` run `.github/workflows/android.yaml` to build debug and release APKs.

## Push to GitHub

```bash
git init
git add .
git commit -m "Initial HomeLandscapingMeasure Android project"
git branch -M main
git remote add origin https://github.com/YOUR_USER/HomeLandscapingMeasure.git
git push -u origin main
```

Replace `YOUR_USER` with your GitHub username or organization.
