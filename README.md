# 🦋 The Butterfly Project — Lupus Medication Adherence App
**Team 5 | Team Monarch | Capstone II | Fall 2026**

---

## Project Overview
The Butterfly Project (Lupus Butterfly) is a gamified medication adherence app for Lupus patients. Patients log doses and earn Butterfly Bucks for taking hydroxychloroquine (HCQ) on time; clinicians monitor adherence and get alerted when a patient falls behind. It's a native Android app backed by a real Supabase database.

---

## Project Status
**Android app is live.** Real Supabase authentication and database, working patient and clinician dashboards, and CR-02 (Adherence Halo, streaks, Double Monarch Days) are all implemented and testable end to end.

---

## Team Information
**Team Name:** Team Monarch | **Project Name:** The Butterfly Project | **Team Number:** 5

| Name | Role |
|------|------|
| Jinh Nguyen | Team Lead, Mobile App Architecture |
| Dikshya Pant | Backend Development, Cloud Deployment |
| Aniya Taylor | UI/UX Design, Mobile Screens |
| Remonda Ayad | Testing & QA Documentation |
| Movika Tamang | Documentation, Design Diagrams |

See [CONTRIBUTIONS.md](./CONTRIBUTIONS.md) for a full breakdown of individual work.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Mobile App | Native Android — Kotlin + Jetpack Compose (Material 3) |
| Backend / Database | Supabase (Postgres, Auth, Row Level Security) |
| Navigation | Jetpack Navigation Compose |
| Version Control | GitHub |
| Project Management | Trello (Kanban) |

---

## Folder Structure

```
capstone-2026/
├── README.md
├── CONTRIBUTIONS.md
│
├── Source_Code/
│   └── Frontend/
│       └── mobile/
│           └── ButterflyApp/     # Native Android app (Kotlin + Jetpack Compose)
│
├── Testing/
│   ├── test_cases/
│   └── test_results/
│
└── Documentation/
    ├── SRS/
    ├── design_diagrams/
    └── wireframes/
```

---

## How to Run the App

**All you need is Android Studio installed.** No Supabase account, no setup — the app already points at a live, shared Supabase backend, so cloning this repo and hitting Run is enough.

1. Open **Android Studio** → **Open** → select `Source_Code/Frontend/mobile/ButterflyApp`. Let it sync (accept any AGP/Gradle upgrade prompt).
2. Hit **Run ▶** on an emulator (Pixel + API 34 recommended) or a physical device.

Full details, including exactly what each screen does, are in [`ButterflyApp/README.md`](./Source_Code/Frontend/mobile/ButterflyApp/README.md).

---

## Features

### Patient
- Sign up / log in with real Supabase authentication
- Dashboard: today's medications, "Mark Taken," adherence halo, consecutive-day streak
- **Double Monarch Days (CR-02):** every 2nd consecutive on-time day pays double Butterfly Bucks
- Adherence Calendar: full halo history by day, with a ⭐ marker on past Double Monarch Days
- Butterfly Bank: Butterfly Bucks balance and game-stage progress
- Customise Butterfly: choose the halo/accent color
- Account: edit info, change password, delete (deactivate) account, notification settings, data & privacy, help & support, reduce-motion toggle

### Clinician
- Dashboard: patient list, live non-adherence alerts preview
- Full Non-Adherence Alerts screen
- Same Account menu as patients (info, notifications, privacy, help, logout)

### CR-02: Adherence Halo & Double Monarch Days
A dose logged within 30 minutes of its scheduled time (either side) counts as on-time and earns the halo. Consecutive on-time days build a streak; **every 2nd consecutive day** is a Double Monarch Day and pays $4 instead of $2 — per the approved Project Plan / CR-02.

---

## Butterfly Bucks Reward System
| Action | Reward |
|--------|--------|
| Medication taken on time | +2 BB (**+4 BB on a Double Monarch Day**) |
| Medication taken late | +1 BB |
| Medication missed | 0 BB |

**Game Stages:**
- 🥚 Chrysalis: 0–49 BB
- 🐛 Caterpillar: 50–149 BB
- 🦋 Butterfly: 150+ BB

---

## Patient User Flow
1. User creates account or logs in
2. Dashboard shows today's medications, BB balance, streak, and game stage
3. User marks a medication taken → on-time doses glow with the adherence halo
4. Every 2nd consecutive on-time day triggers a Double Monarch Day (2x Butterfly Bucks)
5. User checks the Adherence Calendar to see halo history and past Double Monarch Days
6. User customises their butterfly's color and tracks progress in the Butterfly Bank

## Clinician User Flow
1. Clinician logs into their dashboard
2. Sees a live preview of patients with no dose logged today
3. Opens the full Non-Adherence Alerts screen for details
4. Reviews the patient list and individual adherence

---

## Testing
Test cases and results are tracked in `/Testing`.

---

## Roadmap
- Bluetooth smart bottle cap hardware integration
- Live push notifications (notification *preference* is already stored per-user; delivery isn't wired up yet)
- iOS app

---

## UI Design / Wireframes
UI mockups are located in `Documentation/wireframes`.
