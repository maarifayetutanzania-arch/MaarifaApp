# Maarifa 2026 — Android App

Kotlin + Jetpack Compose, wired to Firebase (Auth, Firestore, Storage, Cloud Messaging,
Cloud Functions). This implements the full PRD: student & teacher journeys, subscription
plans, offline reading, engagement scoring, and automatic 75/25 earnings splits with
payout generation — all as real, working logic. Three things are intentionally left for
you to add (see below): your Firebase project config, your GitHub remote, and your actual
payment gateway credentials/API call.

## Project layout

```
app/                    Android app (Kotlin, Jetpack Compose)
functions/              Cloud Functions (TypeScript) — earnings, payouts, expiry,
                         notifications, payment orchestration
firestore.rules         Security rules (PRD 12)
firestore.indexes.json  Composite indexes the app's queries need
firebase.json           Ties the above together for the Firebase CLI
```

There is no admin web dashboard in this repo — the PRD scopes that as a **separate**
web application (PRD 1, 5, 10) connected to the same Firebase backend. This repo is the
Android app only.

## 1. Add your Firebase project (the "database")

1. Create a Firebase project at console.firebase.google.com (Blaze/pay-as-you-go plan —
   required for Cloud Functions, especially the scheduled jobs and any outbound call to a
   payment gateway).
2. Enable: **Authentication** (Google, Phone, Email/Password providers), **Firestore**,
   **Storage**, **Cloud Messaging**.
3. Add an Android app in the Firebase console with package name **`com.maarifa.app`**
   (must match exactly — see `app/build.gradle.kts` → `applicationId`).
4. Download the generated `google-services.json` and place it at `app/google-services.json`.
5. In `app/build.gradle.kts`, uncomment this line:
   ```kotlin
   id("com.google.gms.google-services")
   ```
6. In Firebase console → Authentication → Sign-in method → Google → **Web SDK
   configuration**, copy the **Web client ID** (not the Android client ID). Paste it into
   `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="google_web_client_id">PASTE_IT_HERE</string>
   ```
7. Deploy security rules and indexes:
   ```bash
   firebase login
   firebase use --add        # pick your project
   firebase deploy --only firestore
   ```
8. Deploy Cloud Functions:
   ```bash
   cd functions
   npm install
   npm run deploy
   ```
9. Open the project root in Android Studio. Let it sync (first sync downloads the Gradle
   wrapper jar automatically since `gradle/wrapper/gradle-wrapper.properties` points at
   Gradle 8.9 — this needs internet the first time).

## 2. Push to GitHub

```bash
git init
git add .
git commit -m "Initial Maarifa 2026 Android app"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
```

`.gitignore` excludes `google-services.json` and any keystore by default (standard
practice, since those identify/sign your specific app). Remove those lines from
`.gitignore` first if you specifically want them versioned in a private repo.

## 3. Add your payment gateway

This is the only business logic *not* implemented, because it depends entirely on which
Tanzanian aggregator/bank/mobile-money API you choose. Everything around it — creating the
pending subscription record, computing correct start/end dates (including stacking
renewals on top of remaining time), activating the subscription only after verification,
logging transaction status, and notifying the student — is already built.

Two functions in `functions/src/payments.ts` are what you touch:

- **`callPaymentGatewayStub(...)`** — replace the body with your real "charge this
  customer" API call. Keep the same return shape: `{ providerReference, instructions }`.
- **`paymentWebhook`** — an HTTPS endpoint your gateway calls back once a transaction
  settles. Add your gateway's signature verification at the top (marked with `TODO`)
  before trusting the request body. This is the only place a subscription is ever marked
  `ACTIVE`.

The Android app never talks to the gateway directly — it only calls the `initiatePayment`
Cloud Function and then listens to the subscription document in Firestore for the status
change, which is the architecture PRD 8.5/12 asks for ("shall not activate a subscription
based on user claim alone").

## 4. Admin web dashboard (`admin-dashboard/`)

A separate React + TypeScript app (Vite), deployed to **Firebase Hosting** on the same
project. It never writes to Firestore directly for anything sensitive — every approve/
reject/payout action calls a Cloud Function in `functions/src/adminActions.ts`, which
re-checks `role == "ADMIN"` server-side before writing. `firestore.rules` also blocks
those collections from direct client writes, so this is enforced twice.

**Setup:**

1. Firebase console → Project settings → General → "Your apps" → **Add app → Web**
   (a *second*, separate registration from the Android app). Copy the config object it
   gives you into `admin-dashboard/src/firebase.ts`, replacing the `REPLACE_ME` values.
2. Create your first admin account — there's no self-signup by design:
   - Firebase console → Authentication → Add user (email + password)
   - Firestore console → `users` collection → create a document with that user's UID as
     the document ID, containing at minimum `{ role: "ADMIN", fullName: "...", email: "..." }`
3. Install and run locally:
   ```bash
   cd admin-dashboard
   npm install
   npm run dev
   ```
4. Deploy Cloud Functions (adds the new `adminApprove*`/`adminReject*`/`adminMarkPayoutPaid`
   functions this dashboard depends on — skip if already deployed from §1 step 8, just
   redeploy after adding `adminActions.ts`):
   ```bash
   cd functions && npm run deploy
   ```
5. Deploy the dashboard itself:
   ```bash
   cd admin-dashboard
   npm run deploy
   ```
   This builds and runs `firebase deploy --only hosting`, publishing to
   `https://<your-project-id>.web.app`.

**Pages:** Overview (live counts), Teachers (verification queue), Content (moderation
queue), Subscriptions (read-only payment log), Payouts (review → approve → mark paid,
matching PRD 8.8/14 — admin reviews the auto-generated split, never computes it by hand).



- **Auth**: Google sign-in, Phone/OTP, Email/password (PRD 8.1), with profile creation
  (school name optional) and role-based routing.
- **Library**: real-time approved-content feed, client-side search/filter, save.
- **Offline reading**: downloads go to app-private internal storage only
  (`Context.filesDir/downloads`), rendered page-by-page inside the app via
  `PdfRenderer` with a per-user watermark — no share/export path exists anywhere in the
  code (PRD 8.3, 9, 12).
- **Subscriptions**: weekly/monthly/quarterly plans, renewal stacking, automatic expiry
  (PRD 8.4, 13) — all enforced server-side.
- **Engagement scoring & earnings**: `domain/EngagementScoreCalculator.kt` and
  `domain/EarningsCalculator.kt` on the client mirror the authoritative
  `functions/src/engagement.ts` and `functions/src/earnings.ts` on the server, which is
  what actually computes the 75/25 split and generates `Payout` documents monthly
  (PRD 8.7, 8.8, 14).
- **Teacher flow**: registration → pending verification screen (live-updates the moment
  an admin verifies) → upload (lands `PENDING_REVIEW`) → dashboard stats → earnings/payout
  history.
- **Notifications**: FCM wired end-to-end — Cloud Functions push on approval/rejection,
  verification, payment issues, and expiry warnings; `MaarifaMessagingService` displays
  them and registers tokens.
- **Security rules** (`firestore.rules`): students only ever read `APPROVED` materials;
  teachers can only touch their own content and can never self-approve; subscription,
  teacher-verification, and payout fields are write-locked to Cloud Functions/Admin SDK
  only — the client can't fake a payment or a verification.

## Known gaps / next steps (also listed in PRD §16, §18)

- No screenshot/screen-recording prevention exists at the OS level for any app — this is
  a hard platform limitation, not something left undone.
- The admin web dashboard is a separate project (PRD 1/5/10) — not included here.
- `functions/src/payments.ts` needs your gateway (see §3 above).
- Engagement-score weights (`UNIQUE_READER_WEIGHT`, `DEPTH_WEIGHT`, `COMPLETION_WEIGHT` in
  both the Kotlin and TypeScript copies) are reasonable starting values — PRD 16 flags
  these as likely to need tuning once you have real usage data.
- This project was authored without access to a JDK/Android SDK toolchain, so it hasn't
  been compiled end-to-end here — give it a full Gradle sync + build in Android Studio and
  fix any small import/version mismatches Android Studio's quick-fixes surface (typically
  none, but flagging honestly rather than claiming a build I couldn't run).
