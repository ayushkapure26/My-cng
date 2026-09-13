# Supabase backups for My-cng

Settings now offers manual Supabase backup and restore for vehicles and refill
history. Existing Firebase login, Firestore synchronization, and offline Room
storage continue to work as before. This is a backup integration, not a migration
of all Firestore collections or a live pump feed.

## Backend activation

1. Use Supabase project `poujucgmlmelayrneduv` once its restore completes.
2. Apply `database/supabase_backup.sql` once through the Supabase SQL editor if
   the `public.cng_mitra_backups` table has not already been installed. The SQL
   intentionally fails if the table exists; inspect an existing table instead
   of dropping it. The table has RLS and only grants authenticated users SELECT
   and INSERT of their own backups. Backups are append-only.
3. In Supabase **Authentication → Third-party Auth**, add the Firebase project ID
   used by this Android app. Find it in Firebase project settings or the local
   `google-services.json`. Never substitute an unrelated Firebase project.
4. On a trusted Firebase Admin server, preserve existing custom claims and add
   `role: 'authenticated'` for accounts using this feature. Arrange the same for
   new accounts with a Firebase Auth trigger. Never assign claims in Android code.
   For example, using an already initialized Firebase Admin SDK:

   ```javascript
   const user = await getAuth().getUser(uid);
   await getAuth().setCustomUserClaims(uid, {
     ...user.customClaims,
     role: 'authenticated',
   });
   ```

5. Set `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` in the local `.env`/AI Studio
   secrets. The example URL identifies the connected project. Use a Supabase
   publishable key, or its existing legacy anon key for compatibility. Never put
   a service-role key, `sb_secret_` key, or Firebase Admin credentials in the app.
6. Rebuild the APK. Sign in and select **Settings → Supabase backup → Back up to
   Supabase**. The app refreshes the Firebase token if the role is missing, and
   clearly reports incomplete setup. No guest backup is allowed.

Official reference: [Firebase Auth with Supabase](https://supabase.com/docs/guides/auth/third-party/firebase-auth).

## Restore behavior

- Reads the newest backup visible to the signed-in user under RLS, across devices.
- Validates the format and vehicle references before writing anything locally.
- Merges in a single Room transaction. Existing cars are matched by normalized
  registration; their fields remain unchanged. New local IDs replace old IDs.
- Refill identity uses vehicle, timestamp, odometer, quantity and amount. Repeated
  restore skips matching entries; conflicting existing entries are not overwritten.
- Pump display names are retained but local pump IDs are cleared because IDs may
  refer to different pumps on another installation.
- Restoring does not automatically copy records into Firestore.
- Account changes detected before/during restore abort the transaction.

## Limits and validation

- Manual backups cover cars and refills only. They do not include reviews, pump
  status, profile details, preferences, authentication accounts or uploaded files.
- Each backup is a snapshot of the current device, not a cross-device sync merge.
  The local database is shared by the existing app across sign-ins; sign in to the
  intended account before backing up the device's records.
- Backups are limited to 4 MB of serialized JSON in the app and 5 MB in Postgres.
  Snapshots accumulate; retention must be managed by the project owner.
- Missing registrations and duplicate registrations are rejected to avoid
  ambiguous restore. Existing local data is never deleted by this integration.
- Tests in `SupabaseBackupTest` cover malformed relationships, duplicate
  registrations, unsupported formats, invalid amounts and deduplication keys.
- This source snapshot has no Gradle wrapper. Build and run the tests in an Android
  environment compatible with the repository's existing AGP 9.1.1 configuration.
  An APK build and a signed-in device backup/restore must pass before release.

For an end-to-end check, back up two vehicles and several refills, restore into a
fresh installation using the same account, and repeat restore to check no
duplicates are added. A second account must not read the first account's backup.

## Backend verification performed

The connected Supabase project was resumed and the table installed with migration
`cng_mitra_manual_backups`. Transactional SQL checks passed for owner reads,
cross-account read/insert isolation, blocked anonymous reads, and blocked client
updates/deletes. All verification rows were rolled back.

The security advisor reports that authenticated accounts can discover this table
in the GraphQL schema because they have SELECT access. This is expected for the
backup client; row visibility remains restricted by RLS. See the
[advisor explanation](https://supabase.com/docs/guides/database/database-linter?lint=0027_pg_graphql_authenticated_table_exposed).

The Android tests and APK build have not been executed in this workspace: Gradle,
the Gradle wrapper and an Android SDK are unavailable. Firebase third-party Auth,
role claims and the local publishable-key setting still require configuration;
live end-to-end backup has not yet been verified.
