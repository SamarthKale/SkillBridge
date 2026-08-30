# SkillBridge — Handover

An Android app that connects **college students** with freelance opportunities.
A student signs up, fills a profile, browses/searches opportunities, applies (intro +
resume link), tracks each application through a status pipeline, and — once shortlisted or
accepted — chats with the client.

- **Language / UI:** Kotlin + Jetpack Compose (Material 3), single-Activity.
- **Auth:** Firebase Authentication (Email/Password only).
- **Data:** Cloud Firestore. No custom backend, no Cloud Storage (resume is a link).
- **Min SDK 24, Target SDK 34, `applicationId = com.skillbridge.app`.**

---

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| **JDK** | 17 or newer (21 is fine) | `java -version` |
| **Android Studio** | Giraffe (2022.3) or newer | Ships its own Gradle-compatible JDK |
| **Android SDK** | Platform **34**, Build-Tools 34.0.0 | Installed via Android Studio SDK Manager |
| **Android device or emulator** | API 24+ | An emulator image (e.g. Pixel 8, API 34/35) works |
| **Node.js** | 18+ | **Only** needed if you touch Firebase from the CLI (`npx firebase-tools …`) |
| **Python** | 3.8+ | **Only** needed to run `scripts/seed_firestore.py` against a fresh Firebase project |

The Gradle wrapper (`gradlew` / `gradlew.bat` / `gradle/wrapper/gradle-wrapper.jar`) **is
committed** — you do not need Gradle installed. It downloads Gradle 8.9 on first run.

Dependencies (all resolved automatically by Gradle): AndroidX Activity-Compose,
Navigation-Compose, Compose BOM 2024.09.00, Firebase BoM 33.5.1 (`firebase-auth`,
`firebase-firestore`), Google Services Gradle plugin. **No other libraries.**

---

## 2. The one file you must add: `app/google-services.json`

This file is **git-ignored on purpose** (it is per-project config), so a fresh clone does
**not** contain it. The build **fails without it** with:
`File google-services.json is missing. The Google Services Plugin cannot function without it.`

You have two ways to get it:

### Option A — Use the existing shared Firebase project `freelancing-fcc16` (recommended)

Everything is already set up in this project: Email/Password auth, a Firestore database in
`asia-south1`, deployed security rules, **50 seeded opportunities**, and **10 demo student
accounts** with profiles. You only need the config file.

1. Ask the repo owner (**SamarthKale**) for either:
   - the `app/google-services.json` file directly (it is client config — an API key that is
     already shipped in the APK — safe to share privately), **or**
   - **Editor** access to the `freelancing-fcc16` Firebase project.
2. If you got project access instead of the file:
   `https://console.firebase.google.com/project/freelancing-fcc16/settings/general` →
   *Your apps* → Android app `com.skillbridge.app` → **Download google-services.json**.
3. Put the file at `app/google-services.json` (repo path — same folder as `app/build.gradle.kts`).

That's it. Skip to section 4.

### Option B — Point the app at your own Firebase project

Do this only if you cannot get access to `freelancing-fcc16`. See section 7.

---

## 3. Get the code

```bash
git clone https://github.com/SamarthKale/Android-Freelancing-Mobile-App.git
cd Android-Freelancing-Mobile-App
git checkout stitch        # current working branch (also on: main, update)
```

Then add `app/google-services.json` (section 2).

`local.properties` (which points Gradle at your Android SDK) is git-ignored and created
**automatically** by Android Studio when you open the project. If you build from the command
line without ever opening Android Studio, create it yourself in the repo root:

```
sdk.dir=/absolute/path/to/Android/Sdk      # e.g. C:/Android/Sdk  or  /Users/you/Library/Android/sdk
```

---

## 4. Build & run

### Android Studio (easiest)

1. **File → Open** the repo folder. Let it sync Gradle.
2. Pick a device/emulator in the toolbar.
3. Press **Run ▶**.

### Command line

```bash
# from the repo root
./gradlew clean :app:assembleDebug          # build the APK
./gradlew installDebug                       # install on the connected device/emulator
# launch it:
adb shell monkey -p com.skillbridge.app -c android.intent.category.LAUNCHER 1
```

The debug APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

> On Windows Git Bash, `adb` may not be on PATH — use the full path, e.g.
> `"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"` or `C:/Android/Sdk/platform-tools/adb.exe`.

---

## 5. Demo walkthrough

Use the **hero account** — it already has applications in every status, chat threads, and
notifications, so every screen has content.

> **Email:** `aarav.mehta@skillbridge.dev` **Password:** `Demo@12345`

1. **Splash** (SkillBridge logo + tagline) → auto-advances.
2. **Login** — enter the hero credentials → **Login**.
   (New users: **Sign Up** → fill the form incl. *Student/Client* → **Profile Setup** →
   **Save & Continue** → Home.)
3. **Home / Dashboard** — "Hello, Aarav 👋", search bar, category chips, **My Progress**
   counters (Applied / Review / Shortlisted / Accepted), **Recent Applications**,
   **Recommended Projects**, notification bell (tap it → **Notifications**).
4. **Explore** (bottom-nav tab 2) — 50 opportunities. Try the search box and the
   **Category** / **Budget** chips. Tap any card.
5. **Project Details** — full brief (skills, description, responsibilities, requirements,
   experience, deadline). Tap **Apply for Project**.
6. **Apply** — shows your profile summary. Type an **Introduction**, optionally a resume
   link → **Submit Application**. (Applying to the same project twice is blocked.)
7. **Application Submitted** → **View My Applications**.
8. **My Applications** (bottom-nav tab 3) — every application with a coloured status chip.
   Tap one.
9. **Application Status** — the **Applied → Under Review → Shortlisted → Accepted** stepper.
   For a **Shortlisted/Accepted** one, a **Message Client** button appears.
10. **Messages** (bottom-nav tab 4) — only conversations for shortlisted/accepted
    applications (Nova Labs, Chai and Code for the hero). Open one.
11. **Conversation** — chat bubbles; type in the box and hit send (writes to Firestore
    live).
12. **Profile** (bottom-nav tab 5) — photo/initials, name, role, skills, education,
    experience, about, resume, **Edit Profile**, Logout.

**Bottom navigation (5 tabs):** `Home | Explore | Applications | Messages | Profile`.

### All demo accounts (password `Demo@12345` for every one)

| Email | Name | |
|---|---|---|
| `aarav.mehta@skillbridge.dev` | Aarav Mehta | **hero — has applications, chats, notifications** |
| `diya.sharma@skillbridge.dev` | Diya Sharma | fresh (profile only) |
| `rohan.iyer@skillbridge.dev` | Rohan Iyer | fresh |
| `ananya.rao@skillbridge.dev` | Ananya Rao | fresh |
| `kabir.singh@skillbridge.dev` | Kabir Singh | fresh |
| `isha.nair@skillbridge.dev` | Isha Nair | fresh |
| `vivaan.patel@skillbridge.dev` | Vivaan Patel | fresh |
| `meera.krishnan@skillbridge.dev` | Meera Krishnan | fresh |
| `arjun.deshpande@skillbridge.dev` | Arjun Deshpande | fresh |
| `saanvi.gupta@skillbridge.dev` | Saanvi Gupta | fresh |

---

## 6. Project structure

```
app/src/main/java/com/skillbridge/app/
├── MainActivity.kt                 # setContent { SkillBridgeTheme { AppNavigation() } }
├── auth/
│   ├── AuthManager.kt              # FirebaseAuth wrapper: login / signUp / logout / reset
│   └── SignupPrefs.kt              # carries Student/Client choice into Profile Setup
├── data/
│   ├── Models.kt                   # Opportunity, Profile, Application, Message, AppNotification
│   │                              # + Category / AppStatus / Skills constant sets
│   └── Repo.kt                     # the ONLY Firestore access point (callbacks + listeners)
├── navigation/
│   ├── Routes.kt                   # route string constants
│   └── AppNavigation.kt            # one NavHost; bottom bar shows only on the 5 tab routes
└── ui/
    ├── theme/Theme.kt             # colour tokens (monochrome + one accent) + typography
    ├── components/
    │   ├── Common.kt              # text fields, buttons, chips, SearchField, Avatar, StatCard
    │   ├── Cards.kt               # OpportunityCard, ApplicationCard, MessageBubble,
    │   │                         # PipelineStepper, date helpers
    │   ├── Live.kt                # rememberMyProfile / MyApplications / Opportunities /
    │   │                         # Notifications  (snapshot-listener hooks)
    │   └── ScreenScaffold.kt      # shared top-bar frame
    ├── SplashScreen.kt
    ├── LoginScreen.kt   SignupScreen.kt   ProfileSetupScreen.kt
    ├── HomeScreen.kt    ExploreScreen.kt  ProjectDetailsScreen.kt
    ├── ApplyScreen.kt   ApplicationSubmittedScreen.kt
    ├── ApplicationsScreen.kt      ApplicationStatusScreen.kt
    ├── MessagesScreen.kt          ConversationScreen.kt
    ├── ProfileScreen.kt           NotificationsScreen.kt
```

Repo root: `firebase.json`, `.firebaserc`, `firestore.rules`, `firestore.indexes.json`,
`scripts/seed_firestore.py`, `CLAUDE.md` (design/architecture notes).

**Architecture is deliberately flat:** screens hold their own state with `remember`, call
`Repo` and `AuthManager` directly. No Room, no DI framework, no repository interfaces, no
shared ViewModels.

---

## 7. Backend details

### Firestore collections

| Collection | Doc id | What it holds |
|---|---|---|
| `projects` | auto | Client-posted **opportunities** (title, company, category, budget, location, remote, skills[], description, responsibilities, requirements, experienceRequired, deadline, postedByUid, postedByName, postedAt). Readable by any signed-in user. |
| `profiles` | `{uid}` | One per user: role, name, email, phone, location, degree, college, branch, graduationYear, skills[], experience, bio, **resumeUrl** (a link — no file upload), timestamps. |
| `applications` | `{studentUid}_{projectId}` | One per student+project (this composite id is how re-applying is blocked): projectId, projectTitle, company, budget, studentUid, studentName, intro, resumeUrl, status, appliedAt. |
| `messages` | auto | applicationId, projectTitle, company, studentUid, clientName, `fromClient` (bool), text, sentAt. |
| `notifications` | auto | uid, type, text, createdAt, `read` (bool). |

**Status pipeline:** `Applied → Under Review → Shortlisted → Accepted`, or `Applied →
Rejected`. Messaging is unlocked only for `Shortlisted` / `Accepted`. Advancing a status
(e.g. to `Shortlisted`) is a "client" action — for this mini-project you do it by editing
the doc in the Firebase console (or re-running the seed script).

### Security rules (`firestore.rules`)

Prototype rules, already deployed to `freelancing-fcc16`. Summary: opportunities are
readable by any signed-in user; profiles are readable by any signed-in user and writable
only by their owner; applications / messages / notifications are scoped to the student.
Redeploy after any change:

```bash
npx -y firebase-tools@latest deploy --only firestore:rules --project <your-project-id>
```

### Option B — bring your own Firebase project

1. Create a project at <https://console.firebase.google.com>.
2. **Add an Android app** with package name **`com.skillbridge.app`**, download its
   `google-services.json` into `app/`.
3. **Authentication → Sign-in method → Email/Password → Enable.**
4. **Firestore Database → Create database** (Production mode; pick a region — it is
   permanent).
5. Point the CLI at it and deploy the rules:
   ```bash
   npx -y firebase-tools@latest login
   npx -y firebase-tools@latest use <your-project-id>
   npx -y firebase-tools@latest deploy --only firestore:rules
   ```
6. Seed demo data + accounts (uses the CLI login token; standard library only):
   ```bash
   python scripts/seed_firestore.py
   ```
   This creates the 10 demo accounts, 10 profiles, 50 opportunities, and the hero's demo
   applications/messages/notifications.

---

## 8. Known limitations (by design — this is a mini-project)

- **No resume file upload.** Firebase Storage needs the paid Blaze plan; resume is a URL
  field (`resumeUrl`) shown on the profile and in Apply.
- **No "client" app.** Every account is a student. Opportunities come from the seed script;
  status changes are done in the Firebase console.
- **Chat is one-sided in-app.** You can send messages; the "client" replies are seed data.
- **Notifications are in-app only** (a Firestore collection + a list screen) — no push (FCM).
- **Visual design** was built on the app's own component kit (monochrome + one accent
  `#2563EB`). A Stitch design exists but is a private Google project with no exportable
  link; retune colours/spacing from screenshots if needed.
- Firestore composite indexes are avoided — lists are sorted in memory (fine for the seed
  data volume).

---

## 9. Troubleshooting

| Symptom | Fix |
|---|---|
| `File google-services.json is missing` | Add `app/google-services.json` (section 2). |
| `SDK location not found` | Open the project once in Android Studio, or create `local.properties` with `sdk.dir=…` (section 3). |
| App opens on Login even though you were logged in | You were signed in as an account that was later deleted server-side; just log in again. |
| `PERMISSION_DENIED` in Logcat after changing rules | Re-run `firebase deploy --only firestore:rules`; wait ~30 s for propagation. |
| Explore is empty / no opportunities | You are on a fresh Firebase project — run `python scripts/seed_firestore.py`. |
| Emulator has no Google Play / sign-in issues | Not required — the app uses Firebase Auth REST, any emulator image works. |
| Gradle sync fails on first run | Ensure network access (it downloads Gradle 8.9 + dependencies) and JDK 17+. |

---

## 10. Handy commands

```bash
./gradlew clean :app:assembleDebug            # build
./gradlew installDebug                         # install on device/emulator
./gradlew :app:assembleDebug --stacktrace      # verbose build errors
adb logcat --pid=$(adb shell pidof com.skillbridge.app)   # app logs only
npx -y firebase-tools@latest deploy --only firestore:rules --project freelancing-fcc16
python scripts/seed_firestore.py               # (re)seed a fresh Firebase project
```
