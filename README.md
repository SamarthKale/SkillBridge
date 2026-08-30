# SkillBridge

An Android app that connects **college students** with freelance opportunities. A student
signs up, builds a profile, browses and searches opportunities, applies with an
introduction and a resume link, tracks each application through a status pipeline
(**Applied → Under Review → Shortlisted → Accepted**, or **Rejected**), and — once
shortlisted or accepted — chats with the client.

- **Kotlin + Jetpack Compose** (Material 3), single-Activity, Navigation-Compose.
- **Firebase Authentication** — Email/Password only.
- **Cloud Firestore** — opportunities, profiles, applications, messages, notifications.
- No custom backend. No Cloud Storage (resume is a link). No extra libraries.

## Screens

Splash → Login / Sign Up → Profile Setup → Home → Explore → Project Details → Apply →
Application Submitted → My Applications → Application Status → Messages → Conversation →
Profile.

Bottom navigation: **Home · Explore · Applications · Messages · Profile**.

## Run it

See **[HANDOVER.md](HANDOVER.md)** for the full setup guide — prerequisites, the one file
you must add (`app/google-services.json`), building in Android Studio or from the command
line, a demo walkthrough, the Firestore data model, and how to point the app at your own
Firebase project.

Quick version (once `app/google-services.json` is in place):

```bash
./gradlew clean :app:assembleDebug
./gradlew installDebug
```

Demo login (shared `freelancing-fcc16` project): `aarav.mehta@skillbridge.dev` / `Demo@12345`.

## Layout

```
app/src/main/java/com/skillbridge/app/
├── auth/         AuthManager, SignupPrefs
├── data/         Models.kt, Repo.kt              (all Firestore access)
├── navigation/   Routes.kt, AppNavigation.kt
└── ui/           theme/, components/, one *Screen.kt per screen
firestore.rules · firebase.json · scripts/seed_firestore.py · CLAUDE.md
```

`CLAUDE.md` has the architecture and conventions in more detail.
