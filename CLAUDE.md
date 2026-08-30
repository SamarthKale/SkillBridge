# CLAUDE.md

## Firebase agent skills

**Always look for and use the Firebase agent skills for any Firebase task.** They are
installed at `~/.claude/plugins/marketplaces/firebase/skills/` (firebase-basics,
firebase-auth-basics, firebase-firestore, firebase-security-rules-auditor, …). Prefer
`npx -y firebase-tools@latest ...` over a bare `firebase` command.

## Project

**SkillBridge** — a small Android app that connects freelance project work with **college
students**. A student signs up / logs in with Firebase Auth (email/password), then tracks
the freelance projects they have taken on. Projects live in **Cloud Firestore**.

Product framing: whoever gives out the work ("job giver") can be anyone; the person doing
the work ("job getter") is a college student. Every account is a student account — the copy
reflects that; there is no separate client role or marketplace.

Deliberately small mini project — favour speed and simplicity over sophistication.

## Backend

- **Firebase project:** `freelancing-fcc16` (see `.firebaserc`).
- **Auth:** Firebase Authentication, Email/Password only. `auth/AuthManager.kt` wraps it.
- **Database:** Cloud Firestore, one collection `projects`. `data/ProjectRepository.kt` is
  the only Firestore access point. Every document has a `userId` field = the Firebase UID;
  every read is filtered by it so a student only sees their own projects.
- **Security rules:** `firestore.rules` (owner-only access + field validation). Deploy with
  `npx -y firebase-tools@latest deploy --only firestore:rules`.
- No SQLite. No Realtime Database, Storage, Functions, Analytics. No custom backend.

## Build / run

- Open in Android Studio or run `./gradlew assembleDebug`.
- Requires `app/google-services.json` for project `freelancing-fcc16` — build fails without
  it. Fetch it with
  `npx -y firebase-tools@latest apps:sdkconfig ANDROID <APP_ID> --project freelancing-fcc16`.
- JDK 17+ (21 works). `applicationId` = `com.skillbridge.app`. minSdk 24 / target 34.

## Architecture (intentionally flat)

- Screens (`ui/*Screen.kt`) hold their own state with `remember` / `mutableStateOf`.
- Screens call `data/ProjectRepository.kt` and `auth/AuthManager.kt` **directly**.
- Firestore is async: repository methods take callbacks. Lists use a live snapshot listener
  via `ui/components/ProjectsListener.kt` (`rememberProjects()`).
- `navigation/AppNavigation.kt` wires all screens with `navigation-compose`. Project ids are
  Firestore document ids (`String`).
- No Room, no DI framework, no repository interfaces, no shared ViewModels.

## Conventions

- Monochrome UI: black / white / gray only. Minimal cards, plain typography, whitespace.
  No gradients, no animations, no charts.
- Keep dependencies minimal. Keep functions short and readable — easy to present.

## Do NOT add

Features beyond the 7 screens (Login, Signup, Home, Projects, Add Project, Project Details,
Profile). No chat, payments, notifications, offline sync config, search/filtering,
dark-mode system, client/marketplace side, or extra libraries / Firebase products.
