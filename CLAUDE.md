# CLAUDE.md

## Firebase agent skills

**Always use the Firebase agent skills for Firebase tasks.** Installed at
`~/.claude/plugins/marketplaces/firebase/skills/`. Use `npx -y firebase-tools@latest …`.

## Project

**SkillBridge** — an Android app connecting **college students** with freelance
opportunities. A student signs up, completes a profile, browses/searches opportunities,
applies (intro + resume link), tracks each application through a status pipeline, and — once
shortlisted/accepted — chats with the client.

**Flow:** Splash → Login / Sign Up → Profile Setup → Home → Explore → Project Details →
Apply → Application Submitted → My Applications → Application Status → Messages →
Conversation → Profile.
**Bottom nav (5 tabs):** Home | Explore | Applications | Messages | Profile.
**Status pipeline:** Applied → Under Review → Shortlisted → Accepted  (or Applied → Rejected).
Messaging unlocks at Shortlisted / Accepted.

Design language: monochrome kit (`ui/theme/Theme.kt`: Ink `#111`, Paper `#FFF`, Smoke
`#F4F4F4`, Line `#E0E0E0`, Mute `#666`) + one accent (`Accent #2563EB`, `AccentSoft
#EAF0FE`). A Stitch design was referenced but is a private Google project with no accessible
export — the UI was built on this kit; retune to Stitch later from screenshots.

## Backend (Firebase project `freelancing-fcc16`, free Spark plan)

- **Auth:** Firebase Authentication, Email/Password only. `auth/AuthManager.kt` (login,
  signUp+displayName, logout, `sendPasswordReset`). `auth/SignupPrefs.role` carries the
  Student/Client choice to Profile Setup.
- **Database:** Cloud Firestore (`asia-south1`). Single access point: `data/Repo.kt`
  (callbacks + snapshot listeners; lists sorted in memory, no composite indexes).
  Models in `data/Models.kt`.
  | collection | doc id | purpose |
  |---|---|---|
  | `projects` | auto | client-posted **opportunities** (title, company, category, budget, location, remote, skills[], description, responsibilities, requirements, experienceRequired, deadline, postedByUid, postedByName, postedAt). Readable by any signed-in user. |
  | `profiles` | `{uid}` | one per user: role, name, email, phone, location, photoUrl, degree, college, branch, graduationYear, skills[], experience, bio, **resumeUrl** (link, not a file), timestamps. `uid` is a plain field (NOT `@DocumentId` — the seed + rules use it). |
  | `applications` | `{studentUid}_{projectId}` | one per student+project (dedupe): projectId, projectTitle, company, budget, studentUid, studentName, intro, resumeUrl, status, appliedAt |
  | `messages` | auto | applicationId, projectTitle, company, studentUid, clientName, fromClient(bool), text, sentAt |
  | `notifications` | auto | uid, type, text, createdAt, read(bool) |
- **Rules:** `firestore.rules` (deployed). Opportunities: any signed-in read. Profiles:
  any signed-in read, owner write. Applications: `get` allowed on a missing doc (so
  "already applied?" checks work), `list`/writes student-scoped. Messages: student-scoped,
  student can only create `fromClient:false`. Notifications: owner read, owner create
  (for self), owner may flip only `read`.
- **Cloud Storage: NOT used.** Spark plan; Storage needs Blaze. Resume = a `resumeUrl`
  link field. Do not add paid infra.
- No RTDB, Functions, Analytics, custom backend.

## Demo accounts (disposable — password `Demo@12345` for all)

`aarav.mehta` is the **hero** — 5+ applications (one in every status), message threads on
the Shortlisted/Accepted ones, notifications. Use it for the live demo.

| email | name |
|---|---|
| `aarav.mehta@skillbridge.dev` | Aarav Mehta *(hero)* |
| `diya.sharma@skillbridge.dev` | Diya Sharma |
| `rohan.iyer@skillbridge.dev` | Rohan Iyer |
| `ananya.rao@skillbridge.dev` | Ananya Rao |
| `kabir.singh@skillbridge.dev` | Kabir Singh |
| `isha.nair@skillbridge.dev` | Isha Nair |
| `vivaan.patel@skillbridge.dev` | Vivaan Patel |
| `meera.krishnan@skillbridge.dev` | Meera Krishnan |
| `arjun.deshpande@skillbridge.dev` | Arjun Deshpande |
| `saanvi.gupta@skillbridge.dev` | Saanvi Gupta |

Firestore has **50 seeded opportunities** across Development / Design / Writing / Marketing
/ Data / Other, plus a `profiles` doc for each demo account.

## Build / run

- Android Studio, or `./gradlew clean :app:assembleDebug` (JDK 17+; 21 works).
- Needs `app/google-services.json` for `freelancing-fcc16` (present).
- `applicationId` = `com.skillbridge.app`, minSdk 24 / target 34, Firebase BoM 33.5.1.
- Emulator on this machine: AVD `Pixel_8`. `adb` at `C:/Android/Sdk/platform-tools/adb.exe`;
  prefix adb shell/pull with `export MSYS_NO_PATHCONV=1` in Git Bash.

## Architecture (flat, no new libraries)

- `navigation/AppNavigation.kt` — one `NavHost` inside a `Scaffold`; the `NavigationBar`
  shows only when the current route is one of the 5 tabs. Splash / auth / Profile Setup /
  detail / apply / status / conversation / notifications are full-screen routes.
  Routes in `navigation/Routes.kt` (string ids).
- `ui/*Screen.kt` — one file per screen, local `remember` state, calls `Repo` / `AuthManager`
  directly.
- `ui/components/` — `Common.kt` (fields, buttons, chips, `SearchField`, `Avatar`),
  `Cards.kt` (`OpportunityCard`, `ApplicationCard`, `MessageBubble`, `PipelineStepper`,
  date helpers), `Live.kt` (`rememberMyProfile/MyApplications/Opportunities/Notifications`
  snapshot-listener hooks), `ScreenScaffold.kt`.
- No Room, DI, repository interfaces, or shared ViewModels.

## Do NOT

Add Storage/paid infra, chat complexity, payments, admin dashboards, notification infra,
advanced animations, or extra libraries. Extend the existing kit; don't rebuild the UI.
