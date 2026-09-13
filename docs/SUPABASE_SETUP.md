# Supabase connection for My-cng

This branch uses Supabase Auth directly. No Firebase project, Google services JSON,
Firebase custom claims, or third-party Auth integration is needed.

## Connected backend

- Project: `poujucgmlmelayrneduv` (restored and healthy).
- Email/password signup and login are enabled; email confirmation is required.
- The URL and existing public anon key are included in `.env.example`, which the
  existing Secrets Gradle plugin loads as defaults. This key grants no privileged
  access. Never substitute a service-role key or `sb_secret_` key.
- `public.cng_mitra_backups` is installed. RLS permits authenticated owners to read
  and insert only their own snapshots; anonymous reads and client update/delete
  access are denied. The SQL is recorded in `database/supabase_backup.sql`.

## Use after building

1. Build the Android app from this branch.
2. Register with an email and a password of at least 12 characters.
3. Confirm the email using Supabase's email, then sign in to the app.
4. Settings → Supabase backup → Back up to Supabase.
5. On the same account, Restore latest Supabase backup merges missing entries.

No account has been created or confirmation email sent by this change. Email
confirmation delivery, sender configuration and the confirmation redirect should
be checked on the owner's device before release. Supabase's default email service
has delivery/rate limits; configure production SMTP before onboarding users.

## Security and scope

- Removed the previous fake offline password login and fabricated Google account.
  A successful server response and `/auth/v1/user` verification are required.
- Auth tokens remain only in process memory, never in SharedPreferences, logs or
  Android backups. They refresh during the process lifetime; after process death,
  the user signs in again. Stored legacy login flags do not grant access.
- Logout clears local tokens and requests revocation of the current server session.
  If the server request fails, the UI reports that revocation was not confirmed.
  Already issued JWTs can remain valid until expiry under standard Supabase Auth.
- Google sign-in is visibly disabled because the backend provider is not configured.
  Password recovery is explicitly unavailable in this build; it does not pretend
  to send an email. These flows need configuration and device testing before release.
- Firestore auto-backup and community-write wiring are disconnected. Offline Room
  data remains on the device. This change covers manual vehicle/refill backups,
  not a live pump feed or automatic cross-device synchronization.
- Each snapshot represents the current device. The existing Room database is
  device-local and shared across app sign-ins; do not treat sign-out as erasing or
  hiding local records. Manual backup uploads that device's records to the selected
  account. This app does not yet support isolated multi-user local profiles.
- Restore validates relationships and merges inside a Room transaction. Vehicles
  match by normalized registration. Refill identity uses vehicle, timestamp,
  odometer, quantity and amount. Existing entries are not overwritten. Local pump
  IDs are cleared on restore while display names are kept.
- Backups are append-only and accumulate. App payload limit is 4 MB; database limit
  is 5 MB. Retention is controlled by the project owner.

## Verification

- Live Auth settings returned HTTP 200: email enabled, signup enabled, email
  confirmation required, Google and anonymous sign-in disabled.
- Anonymous REST access to backups returned HTTP 401.
- SQL owner-read, cross-account read/insert isolation, and denied update/delete
  tests passed in a transaction; all verification rows were rolled back.
- Security advisor reports authenticated GraphQL schema discoverability. This is
  expected for owner SELECT access; RLS still restricts rows.
- `git diff --check` passed. `SupabaseBackupTest` covers invalid payloads and
  deduplication keys. Android tests and APK build remain unexecuted: this snapshot
  has no Gradle wrapper and this workspace has no Gradle/Android SDK.
- A real account signup, email confirmation, login, refresh, logout and repeated
  device restore must pass before release. This is a draft, not a released APK.

References: [Supabase password authentication](https://supabase.com/docs/guides/auth/passwords),
[session behavior](https://supabase.com/docs/guides/auth/sessions),
[RLS](https://supabase.com/docs/guides/database/postgres/row-level-security).
