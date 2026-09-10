# Lupus Butterfly — Android Prototype

Team Monarch | Capstone II | Fall 2026

## What's in here

A basic native Android app (Kotlin + Jetpack Compose) with four working screens:

- **Login** — email/password fields, buttons to log in as Patient or Clinician (any input works, no real backend yet)
- **Sign Up** — name/email/password + role selector
- **Patient Dashboard** — Butterfly Bucks balance, game stage (Chrysalis → Caterpillar → Butterfly), today's medications with "Mark Taken" (awards +2 BB and updates your stage live)
- **Clinician Dashboard** — patient list with adherence %, non-adherence alerts, search

All data is currently mocked/in-memory (see `MockData.kt`) — nothing is wired to a real backend yet. That's the next step once this shell is working and approved by the team.

## How to open it

1. Open **Android Studio**.
2. On the Welcome screen, choose **Open** (not "New Project").
3. Select this `ButterflyApp` folder.
4. Let it sync — it may prompt you to **upgrade the Android Gradle Plugin / Gradle version** to match your Android Studio version. That's normal and expected; click **Upgrade** / **Accept** when it asks.
5. Once synced, click the green **Run ▶** button at the top. Android Studio will ask you to select a device — pick (or create) a virtual device (emulator) and it will launch there.

## Notes

- Package name: `com.teammonarch.butterfly`
- Min Android version supported: Android 8.0 (API 26)
- Nothing here has been pushed to GitHub yet — this is a local-only build for the team to review first.
