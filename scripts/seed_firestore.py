#!/usr/bin/env python3
"""
SkillBridge - one-shot Firestore/Auth seeder.

WHAT IT DOES
  1. Creates 10 demo student accounts in Firebase Auth (password: Demo@12345).
  2. Writes a `profiles/{uid}` document for each.
  3. Writes 50 `projects` documents (client-posted opportunities).
  4. Gives the first account ("Aarav Mehta") demo applications (one per status),
     message threads for the shortlisted/accepted ones, and notifications.

WHEN YOU NEED IT
  Only if you are pointing the app at a BRAND-NEW Firebase project. The shared
  project `freelancing-fcc16` is already seeded - if you use that one, skip this.

PREREQUISITES
  - Python 3.8+ (standard library only, no pip installs).
  - Node.js, then:  npx -y firebase-tools@latest login
        (this script reads that CLI's cached OAuth token to write via the
         Firestore REST API, which bypasses security rules for the seed)
  - `app/google-services.json` present for the target project.
  - In that Firebase project: Email/Password auth enabled, a Firestore
    database created, and `firestore.rules` deployed
    (`npx -y firebase-tools@latest deploy --only firestore:rules`).

RUN
  python scripts/seed_firestore.py           # from the repo root

Re-running is safe: profiles/applications are upserted by id; opportunities are
appended (you'll get duplicates), so only run the opportunity part once.
"""

import json
import os
import random
import sys
import time
import urllib.error
import urllib.request

random.seed(42)
HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
PASSWORD = "Demo@12345"


def load_google_services():
    path = os.path.join(REPO, "app", "google-services.json")
    if not os.path.exists(path):
        sys.exit("app/google-services.json not found - add it first (see HANDOVER.md).")
    d = json.load(open(path, encoding="utf-8"))
    project_id = d["project_info"]["project_id"]
    api_key = d["client"][0]["api_key"][0]["current_key"]
    return project_id, api_key


def load_cli_token():
    path = os.path.expanduser("~/.config/configstore/firebase-tools.json")
    if not os.path.exists(path):
        sys.exit("Firebase CLI not logged in - run: npx -y firebase-tools@latest login")
    return json.load(open(path, encoding="utf-8"))["tokens"]["access_token"]


PROJECT, API_KEY = load_google_services()
TOKEN = load_cli_token()
BASE = f"https://firestore.googleapis.com/v1/projects/{PROJECT}/databases/(default)/documents"
AUTH_HDR = {"Authorization": f"Bearer {TOKEN}", "Content-Type": "application/json"}


def _post(url, body, headers=None):
    req = urllib.request.Request(url, data=json.dumps(body).encode(), method="POST",
                                headers=headers or {"Content-Type": "application/json"})
    return json.loads(urllib.request.urlopen(req).read())


# ---- Firestore typed-value helpers ---------------------------------

def val(v):
    if isinstance(v, bool):
        return {"booleanValue": v}
    if isinstance(v, int):
        return {"integerValue": str(v)}
    if isinstance(v, list):
        return {"arrayValue": {"values": [val(x) for x in v]}}
    return {"stringValue": str(v)}


def fields(d):
    return {"fields": {k: val(v) for k, v in d.items()}}


def put(coll, doc_id, d):
    url = f"{BASE}/{coll}?documentId={doc_id}"
    req = urllib.request.Request(url, data=json.dumps(fields(d)).encode(), method="POST", headers=AUTH_HDR)
    try:
        urllib.request.urlopen(req)
    except urllib.error.HTTPError as e:
        if e.code == 409:  # already exists -> patch
            purl = f"{BASE}/{coll}/{doc_id}"
            req = urllib.request.Request(purl, data=json.dumps(fields(d)).encode(), method="PATCH", headers=AUTH_HDR)
            urllib.request.urlopen(req)
        else:
            raise


def add(coll, d):
    req = urllib.request.Request(f"{BASE}/{coll}", data=json.dumps(fields(d)).encode(), method="POST", headers=AUTH_HDR)
    return json.loads(urllib.request.urlopen(req).read())["name"].split("/")[-1]


# ---- 1. demo accounts -------------------------------------------

STUDENTS = [
    ("aarav.mehta", "Aarav Mehta"), ("diya.sharma", "Diya Sharma"),
    ("rohan.iyer", "Rohan Iyer"), ("ananya.rao", "Ananya Rao"),
    ("kabir.singh", "Kabir Singh"), ("isha.nair", "Isha Nair"),
    ("vivaan.patel", "Vivaan Patel"), ("meera.krishnan", "Meera Krishnan"),
    ("arjun.deshpande", "Arjun Deshpande"), ("saanvi.gupta", "Saanvi Gupta"),
]


def create_accounts():
    out = []
    for handle, name in STUDENTS:
        email = f"{handle}@skillbridge.dev"
        try:
            r = _post(f"https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={API_KEY}",
                      {"email": email, "password": PASSWORD, "returnSecureToken": True})
            uid, id_token = r["localId"], r["idToken"]
            _post(f"https://identitytoolkit.googleapis.com/v1/accounts:update?key={API_KEY}",
                  {"idToken": id_token, "displayName": name})
            print("created", email)
        except urllib.error.HTTPError as e:
            msg = e.read().decode()
            if "EMAIL_EXISTS" in msg:
                r = _post(f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={API_KEY}",
                          {"email": email, "password": PASSWORD, "returnSecureToken": True})
                uid = r["localId"]
                print("exists ", email)
            else:
                sys.exit(f"account create failed for {email}: {msg}")
        out.append({"email": email, "uid": uid, "name": name})
        time.sleep(0.3)
    return out


# ---- 2-4. seed data --------------------------------------------

NOW = int(time.time() * 1000)
DAY = 86_400_000

COMPANIES = ["Nova Labs", "BrightByte", "Kanha Studios", "FinNest", "GreenCart", "Zephyr Tech",
             "Peppermint Media", "Trailhead Analytics", "Craftly", "BlueOrbit", "Sattva Foods",
             "PixelForge", "LoopWear", "Quanta AI", "Rootstock", "MetroDesk", "Chai and Code",
             "Vayu Mobility", "Lumen Health", "Orbit Books"]
CITIES = ["Remote", "Remote", "Remote", "Bengaluru", "Pune", "Hybrid - Mumbai",
          "Remote", "Hyderabad", "Remote", "Delhi NCR"]
EXP = ["Beginner friendly", "0-1 years", "1-2 years", "Student / fresher", "0-2 years"]

OPPS = [
    ("Android App for Local Grocery Delivery", "Development", "Rs 18,000", ["Kotlin", "Jetpack Compose", "Firebase"]),
    ("Flutter Cross-platform MVP", "Development", "Rs 25,000", ["Flutter", "Dart", "REST API"]),
    ("React Marketing Website Revamp", "Development", "Rs 15,000", ["React", "JavaScript", "CSS"]),
    ("Node.js REST API for Inventory", "Development", "Rs 20,000", ["Node.js", "Express", "MongoDB"]),
    ("WordPress Site + Blog Setup", "Development", "Rs 9,000", ["WordPress", "PHP", "SEO"]),
    ("Firebase Auth + Firestore Integration", "Development", "Rs 12,000", ["Firebase", "Android", "Kotlin"]),
    ("Bug Fixes in Existing Android App", "Development", "Rs 7,000", ["Kotlin", "Debugging"]),
    ("Python Automation Script for Reports", "Development", "Rs 8,500", ["Python", "Pandas", "Automation"]),
    ("Chrome Extension for Tab Management", "Development", "Rs 11,000", ["JavaScript", "Chrome APIs"]),
    ("Landing Page with Tailwind", "Development", "Rs 6,500", ["HTML", "Tailwind", "JavaScript"]),
    ("REST API Integration in Android App", "Development", "Rs 13,000", ["Kotlin", "Retrofit", "Coroutines"]),
    ("Django Backend for Event Portal", "Development", "Rs 22,000", ["Python", "Django", "PostgreSQL"]),
    ("Kotlin Multiplatform Shared Module", "Development", "Rs 28,000", ["Kotlin", "KMP"]),
    ("Small E-commerce Cart in React", "Development", "Rs 16,000", ["React", "Redux", "Stripe"]),
    ("Firebase Push Notifications Setup", "Development", "Rs 5,500", ["Firebase", "FCM", "Android"]),
    ("UI/UX for a Fitness Tracking App", "Design", "Rs 14,000", ["Figma", "UI/UX", "Prototyping"]),
    ("Logo + Brand Kit for Coffee Startup", "Design", "Rs 8,000", ["Illustrator", "Branding", "Logo Design"]),
    ("Social Media Poster Set (10 posts)", "Design", "Rs 6,000", ["Canva", "Photoshop", "Layout"]),
    ("Figma Prototype for Onboarding Flow", "Design", "Rs 9,500", ["Figma", "Wireframing", "UX"]),
    ("App Icon + Play Store Graphics", "Design", "Rs 4,500", ["Illustrator", "Icon Design"]),
    ("Design System Components in Figma", "Design", "Rs 17,000", ["Figma", "Design Systems"]),
    ("Redesign Dashboard Screens", "Design", "Rs 12,500", ["UI/UX", "Figma", "Data Viz"]),
    ("Pitch Deck Visual Design", "Design", "Rs 7,500", ["PowerPoint", "Visual Design"]),
    ("Illustrations for Blog (5 pieces)", "Design", "Rs 10,000", ["Illustration", "Procreate"]),
    ("Blog Articles on Personal Finance (5)", "Writing", "Rs 5,000", ["Content Writing", "SEO", "Research"]),
    ("Product Descriptions for Store (30)", "Writing", "Rs 4,000", ["Copywriting", "E-commerce"]),
    ("Technical Docs for a REST API", "Writing", "Rs 9,000", ["Technical Writing", "Markdown", "APIs"]),
    ("Resume and LinkedIn Rewrite", "Writing", "Rs 3,500", ["Copywriting", "Editing"]),
    ("Newsletter Content (4 issues)", "Writing", "Rs 6,500", ["Content Writing", "Email"]),
    ("Case Study Write-ups (3)", "Writing", "Rs 7,000", ["Content Writing", "Interviewing"]),
    ("Scriptwriting for 3 Explainer Videos", "Writing", "Rs 8,000", ["Scriptwriting", "Storytelling"]),
    ("Instagram Growth Campaign (1 month)", "Marketing", "Rs 10,000", ["Social Media", "Instagram", "Analytics"]),
    ("Basic SEO Audit + Fixes", "Marketing", "Rs 8,000", ["SEO", "Google Analytics", "Keyword Research"]),
    ("Email Marketing Setup in Mailchimp", "Marketing", "Rs 6,000", ["Email Marketing", "Mailchimp"]),
    ("Google Ads Campaign Setup", "Marketing", "Rs 9,000", ["Google Ads", "PPC"]),
    ("Content Calendar + 15 Captions", "Marketing", "Rs 5,500", ["Social Media", "Copywriting"]),
    ("Influencer Outreach List (50)", "Marketing", "Rs 4,500", ["Research", "Outreach"]),
    ("Data Entry: 500 Product Records", "Data", "Rs 4,000", ["Data Entry", "Excel", "Accuracy"]),
    ("Excel Dashboard for Sales Data", "Data", "Rs 9,500", ["Excel", "Pivot Tables", "Charts"]),
    ("Python Web Scraper for Listings", "Data", "Rs 11,000", ["Python", "BeautifulSoup", "Requests"]),
    ("Clean and Normalise a Messy Dataset", "Data", "Rs 7,000", ["Python", "Pandas", "Data Cleaning"]),
    ("Power BI Report for KPIs", "Data", "Rs 13,000", ["Power BI", "DAX", "Data Modeling"]),
    ("Simple ML Model Prototype", "Data", "Rs 16,000", ["Python", "scikit-learn", "Jupyter"]),
    ("SQL Queries + Reporting Views", "Data", "Rs 8,500", ["SQL", "PostgreSQL", "Reporting"]),
    ("Survey Data Analysis + Summary", "Data", "Rs 6,500", ["Statistics", "Excel", "Visualization"]),
    ("Edit 8 Short-form Videos (Reels)", "Other", "Rs 7,000", ["Video Editing", "Premiere Pro", "CapCut"]),
    ("Voiceover for 2-min Explainer", "Other", "Rs 3,000", ["Voiceover", "Audacity"]),
    ("Research Assistant: Market Landscape", "Other", "Rs 8,000", ["Research", "Report Writing"]),
    ("Transcribe 3 Hours of Interviews", "Other", "Rs 4,500", ["Transcription", "Attention to Detail"]),
    ("QA Testing for Android App", "Other", "Rs 6,000", ["Manual Testing", "Bug Reporting"]),
    ("Set Up Notion Workspace for Team", "Other", "Rs 5,000", ["Notion", "Productivity", "Templates"]),
]

DESC = {
    "Development": "We need a reliable student developer to build and ship this feature. The codebase is small and well-scoped, and mentorship is available. You will own the module end to end.",
    "Design": "Looking for a design-minded student to deliver clean, usable screens. Brand assets and references will be shared. Iterative feedback happens over a shared Figma file.",
    "Writing": "We want clear, well-researched writing in a friendly tone. Briefs and examples are provided. Expect light editing rounds before publishing.",
    "Marketing": "Help us reach the right audience on a small budget. You will plan, execute and report on results. A good chance to show measurable impact in a portfolio.",
    "Data": "Turn raw data into something useful. Inputs and the expected output format are shared up front. Accuracy and a tidy final deliverable matter most.",
    "Other": "A short, well-defined task, ideal for a student building a portfolio. A clear checklist is provided with a quick feedback loop.",
}
RESP = {
    "Development": ["Implement the agreed scope", "Write clean, readable code", "Test on a real device or environment", "Share progress every few days"],
    "Design": ["Deliver screens in Figma", "Follow the provided brand guide", "Incorporate one to two feedback rounds", "Export assets for handoff"],
    "Writing": ["Research each topic briefly", "Write to the provided brief and word count", "Run a self-edit pass", "Deliver in Google Docs"],
    "Marketing": ["Plan the campaign calendar", "Execute posts and ads as scheduled", "Track key metrics", "Share a short results summary"],
    "Data": ["Clean and structure the data", "Follow the output template", "Document any assumptions", "Deliver the final file and a short note"],
    "Other": ["Complete the checklist", "Flag anything unclear early", "Deliver on the agreed date"],
}
REQS = {
    "Development": ["Comfortable with the listed stack", "Can read existing code", "Basic Git workflow"],
    "Design": ["Portfolio or one to two samples", "Figma proficiency", "Understands mobile layout basics"],
    "Writing": ["Strong written English", "One to two writing samples", "Can meet deadlines"],
    "Marketing": ["Familiar with the listed platforms", "Basic analytics literacy"],
    "Data": ["Comfortable with spreadsheets or Python", "Careful and detail-oriented"],
    "Other": ["Reliable and responsive", "Delivers on time"],
}


def seed_opportunities():
    ids = []
    for title, cat, budget, skills in OPPS:
        comp = random.choice(COMPANIES)
        loc = random.choice(CITIES)
        posted = NOW - random.randint(0, 20) * DAY - random.randint(0, DAY)
        deadline_days = random.choice([7, 10, 14, 21, 30])
        d = {
            "title": title, "company": comp, "category": cat, "budget": budget,
            "location": loc, "remote": loc.startswith("Remote"), "skills": skills,
            "description": DESC[cat], "responsibilities": " | ".join(RESP[cat]),
            "requirements": " | ".join(REQS[cat]), "experienceRequired": random.choice(EXP),
            "deadline": time.strftime("%Y-%m-%d", time.localtime((posted + deadline_days * DAY) / 1000)),
            "postedByUid": "", "postedByName": comp, "postedAt": int(posted),
        }
        ids.append((add("projects", d), d))
    print(f"seeded {len(ids)} opportunities")
    return ids


def seed_profiles(accts):
    colleges = ["VJTI Mumbai", "COEP Pune", "PES University", "RV College of Engineering", "SRM Institute",
                "Manipal Institute of Technology", "VIT Vellore", "BITS Pilani", "NIT Trichy", "IIIT Hyderabad"]
    branches = ["Computer Science", "Information Technology", "Electronics and Comm.", "Computer Engineering", "AI and Data Science"]
    degrees = ["B.Tech", "B.E.", "BCA", "B.Sc CS", "MCA"]
    pool = ["Android Development", "Web Development", "UI/UX", "Python", "Java", "Kotlin", "Firebase",
            "React", "Figma", "Data Analysis", "Machine Learning", "Content Writing", "Node.js", "SQL"]
    bios = ["Final-year CS student who loves shipping small, polished apps.",
            "Design-focused developer. I care about clean UI and clear code.",
            "I build Android apps with Kotlin and Jetpack Compose in my free time.",
            "Data enthusiast - Python, pandas, and tidy dashboards.",
            "Frontend developer learning full-stack. Fast learner, reliable.",
            "I enjoy turning rough ideas into working prototypes.",
            "Comfortable across the stack; strongest in mobile.",
            "UI/UX student with a small freelance portfolio.",
            "Backend-leaning developer; APIs and databases are my thing.",
            "Generalist who likes short, well-defined projects."]
    for i, a in enumerate(accts):
        put("profiles", a["uid"], {
            "uid": a["uid"], "role": "student", "name": a["name"], "email": a["email"],
            "phone": f"+91 9{random.randint(100000000, 999999999)}",
            "location": random.choice(["Mumbai", "Pune", "Bengaluru", "Hyderabad", "Chennai", "Delhi"]),
            "photoUrl": "", "degree": random.choice(degrees), "college": colleges[i],
            "branch": random.choice(branches), "graduationYear": random.choice([2026, 2027, 2028]),
            "skills": random.sample(pool, random.randint(3, 6)),
            "experience": "Built two to three academic and personal projects. " + ("One summer internship." if i % 2 else "Freelanced on small campus gigs."),
            "bio": bios[i], "resumeUrl": "", "createdAt": NOW, "updatedAt": NOW,
        })
    print(f"seeded {len(accts)} profiles")


def seed_hero(accts, opp_ids):
    hero = accts[0]
    chosen = random.sample(opp_ids, 5)
    statuses = ["Applied", "Under Review", "Shortlisted", "Accepted", "Rejected"]
    app_ids = []
    for (did, od), st in zip(chosen, statuses):
        appid = f'{hero["uid"]}_{did}'
        put("applications", appid, {
            "projectId": did, "projectTitle": od["title"], "company": od["company"], "budget": od["budget"],
            "studentUid": hero["uid"], "studentName": hero["name"],
            "intro": "I have built similar work for coursework and personal projects, and I can start this week.",
            "resumeUrl": "", "status": st, "appliedAt": NOW - random.randint(1, 10) * DAY,
        })
        app_ids.append((appid, od, st))
    for appid, od, st in app_ids:
        if st in ("Shortlisted", "Accepted"):
            first = hero["name"].split()[0]
            thread = [
                (True, f'Hi {first}, we liked your application for "{od["title"]}". Do you have time this week to talk?'),
                (False, "Hi! Yes, I am available. I have worked with " + ", ".join(od["skills"][:2]) + " before and can share samples."),
                (True, "Great. Could you share a short portfolio link and your expected timeline?"),
            ]
            if st == "Accepted":
                thread += [(False, "Sent. I can start Monday and deliver a first version within a week."),
                           (True, "Perfect, we would like to move forward. Welcome aboard!")]
            t0 = NOW - 3 * DAY
            for j, (from_client, text) in enumerate(thread):
                add("messages", {
                    "applicationId": appid, "projectTitle": od["title"], "company": od["company"],
                    "studentUid": hero["uid"], "clientName": od["company"],
                    "fromClient": from_client, "text": text, "sentAt": int(t0 + j * 3_600_000),
                })
    for i, (ty, tx) in enumerate([
        ("application_submitted", "Your application for a project was submitted."),
        ("under_review", "An application is now Under Review."),
        ("shortlisted", "You have been Shortlisted for a project. You can now message the client."),
        ("accepted", "Congratulations - an application was Accepted."),
        ("message", "New message from a client."),
    ]):
        add("notifications", {"uid": hero["uid"], "type": ty, "text": tx,
                              "createdAt": NOW - i * 7_200_000, "read": i > 2})
    print(f"seeded hero demo content for {hero['email']}")


if __name__ == "__main__":
    print(f"Seeding project: {PROJECT}\n")
    accounts = create_accounts()
    opportunity_ids = seed_opportunities()
    seed_profiles(accounts)
    seed_hero(accounts, opportunity_ids)
    print("\nDONE. Log in as aarav.mehta@skillbridge.dev / Demo@12345 for the full demo.")
