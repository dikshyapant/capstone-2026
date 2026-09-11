# Lupus Butterfly — Android App

Team Monarch | Capstone II | Fall 2026

## What's in here

A native Android app (Kotlin + Jetpack Compose) backed by a real Supabase project (Auth + Postgres database).

**Patient**
- Login / Sign Up (real Supabase Auth, email + password)
- Dashboard — today's medications with "Mark Taken," adherence halo, consecutive-day streak, Double Monarch Day bonus (CR-02)
- Adherence Calendar — halo history by day, with a ⭐ marker on past Double Monarch Days
- Butterfly Bank — Butterfly Bucks balance and game-stage progress (Chrysalis 🥚 → Caterpillar 🐛 → Butterfly 🦋)
- Customise Butterfly — pick the halo/accent color
- Account: Account Info (edit name/email/phone, change password, delete account), Notification Settings, Data & Privacy, Help & Support, Reduce Motion toggle

**Clinician**
- Dashboard — patient list, live non-adherence alerts preview
- Non-Adherence Alerts — full list of patients with no dose logged today
- Account: same Account Info / Notification Settings / Data & Privacy / Help & Support menu as patients

## CR-02: Adherence Halo & Double Monarch Days

A dose logged within 30 minutes of its scheduled time (either side) counts as on-time and earns the halo. Consecutive on-time days build a streak; every 2nd consecutive day is a Double Monarch Day and pays $4 instead of $2 (per the approved Project Plan / CR-02 — not the SRS's "3 days," which was an unconfirmed placeholder). Double Monarch history is derived from the existing on-time log dates, so it's visible any time on the Adherence Calendar rather than needing its own notification system.

## Setup

**All you need is Android Studio.** No Supabase account, no setup — the app already points at a live, shared Supabase backend.

1. Open **Android Studio** → **Open** → select this `ButterflyApp` folder. Let it sync (it may prompt to upgrade AGP/Gradle — accept that).
2. Run ▶ on an emulator (Pixel + API 34 recommended) or a physical device.

## Notes

- Package name: `com.teammonarch.butterfly`
- Min Android version: Android 8.0 (API 26)
- No `gradlew` wrapper scripts are checked in — open the project directly in Android Studio rather than building from the command line.
