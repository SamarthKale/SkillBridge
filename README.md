# SkillBridge

A small Android app that helps **college students** manage the freelance projects they
take on. Whoever hands out the work can be anyone; the person doing it is a student, so
every account here is a student account.

- **Kotlin + Jetpack Compose** UI (monochrome, minimal)
- **Firebase Authentication** (email/password) for login / signup / logout
- **Cloud Firestore** for all project data (collection `projects`, one doc per project)
- No custom backend, no other Firebase products

## Screens

Login → Signup → Home (dashboard) → Projects → Add / Edit Project → Project Details → Profile

Every Firestore document has a `userId` field equal to the signed-in Firebase UID, and the
app only ever queries `where("userId", "==", uid)`, so each account sees only its own work.
The `firestore.rules` file enforces the same ownership on the server.

## Prerequisites

- Android Studio (Giraffe or newer), JDK 17+ (21 is fine)
- An emulator or device on API 24+
- Node.js (for the Firebase CLI via `npx`)

## Firebase setup

The Firebase project is **`freelancing-fcc16`** (already wired in `.firebaserc`). The app
**will not build** until `app/google-services.json` is present. Steps:

```bash
# 1. Log in with the Google account that owns the freelancing-fcc16 project
npx -y firebase-tools@latest login          # add --reauth if it says already logged in

# 2. Point the CLI at the project
npx -y firebase-tools@latest use freelancing-fcc16

# 3. Register the Android app (skip if it already exists — check apps:list)
npx -y firebase-tools@latest apps:create ANDROID "SkillBridge" \
  --package-name "com.skillbridge.app" --project freelancing-fcc16

# 4. Download the config file (APP_ID is printed by the command above / apps:list)
npx -y firebase-tools@latest apps:sdkconfig ANDROID <APP_ID> --project freelancing-fcc16 \
  > app/google-services.json

# 5. Enable Email/Password sign-in and deploy the Firestore security rules
npx -y firebase-tools@latest deploy --only auth
npx -y firebase-tools@latest deploy --only firestore:rules
```

If `deploy --only auth` is unavailable on your CLI, enable it in the console instead:
**Authentication → Sign-in method → Email/Password → Enable**.

A Firestore database must exist for the project. Check with
`npx -y firebase-tools@latest firestore:databases:list`; if there is none, create one:
`npx -y firebase-tools@latest firestore:databases:create "(default)" --location <region>`.

## Run

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # install on a running emulator/device
```

Or open the folder in Android Studio and press **Run ▶**.

## Manual test walkthrough

1. Launch → **Create Account** → Name / Email / Password / Confirm → lands on **Home**.
2. Home shows `Welcome, <name>` and Total / Active / Completed all `0`.
3. Tap **+** → fill the form → **Save Project** → it appears (live) in **Projects** and in
   Home → *Recent Projects*; the counts update. Confirm the doc in the Firestore console.
4. Open a project → **Edit** (change status) → **Mark Completed** → counts move.
5. **Delete** → confirm dialog → the document is removed from Firestore.
6. **Profile** (person icon on Home) shows name + email → **Logout** → back to Login.
7. Kill and reopen the app while logged in → it opens straight to Home.
8. Log in with a second account → it sees none of the first account's projects (rules deny it).

## Project layout

```
firebase.json, .firebaserc, firestore.rules, firestore.indexes.json   # Firebase config
app/src/main/java/com/skillbridge/app/
├── MainActivity.kt
├── data/           Project.kt, ProjectRepository.kt   (Firestore)
├── auth/           AuthManager.kt                       (Firebase Auth wrapper)
├── navigation/     Routes.kt, AppNavigation.kt          (navigation-compose)
└── ui/             theme/, components/, one *Screen.kt per screen
```

See `CLAUDE.md` for the conventions this project sticks to.

> **Security rules note:** `firestore.rules` is a solid prototype — owner-only access with
> per-field validation. Review it carefully and harden it before opening the app to a wide
> audience.
