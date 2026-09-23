# CNG Mitra security review — 2026-09-14
Scope: current My-cng source, reachable Git history, Android auth/backup/AI network paths and connected Supabase public schema. Baseline hardening, not penetration-test certification.

## Implemented in this change
- Replace automatic .env-to-BuildConfig export with an explicit public configuration allowlist. Reject known privileged Supabase key types during builds.
- Remove the direct Gemini credential/request path. Online generation remains disabled pending an authenticated server proxy with server-stored credentials and quotas. Existing guidance is labeled offline.
- Require server-confirmed email when accepting authentication. Tokens stay in memory. Refresh preserves a 12-hour app session cap measured with a monotonic clock; this is not server revocation.
- Explicit Android HTTPS-only traffic; exclude private app data from OS backup/device transfer. Manual user-selected backups/exports remain available.
- Log only auth HTTP status, without bodies/tokens/passwords/emails. Local logs are not centralized monitoring.
- Add a redacted heuristic secret scan and mandatory reusable project checklist.

## Database evidence
RLS is enabled for cng_mitra_backups. SELECT/INSERT require owner user_id to equal the signed JWT subject. Rollback-only testing confirmed own insertion/read, hidden cross-user records and denied cross-user insert. Policies provide no UPDATE/DELETE path. No real user data was read or changed.
Advisor warning: table schema discoverable to authenticated GraphQL users. This is not an RLS bypass; SELECT is required by the REST backup app. Review unused GraphQL exposure rather than blindly revoking SELECT:
https://supabase.com/docs/guides/database/database-linter?lint=0027_pg_graphql_authenticated_table_exposed

## Pending production controls
- Current tools cannot configure hosted Auth settings: verify password policy, email verification, recovery-token expiry, absolute/inactivity server sessions, refresh rotation, SMTP, CAPTCHA and rate-limit values. Supabase owns password hashing; do not hash again on Android.
- Password recovery is explicitly unavailable. Configure/test a complete recovery/deep-link flow before enabling it.
- Backup API server quotas, global storage/retention bounds and stronger server payload constraints remain pending. RLS is not rate limiting.
- No authenticated AI proxy/provider secret is configured. Online AI stays disabled.
- Raw DB network restrictions, Maps key restrictions, central log retention/alerts and deployed revocation tests are unverified.
- Local Room data is shared by device, not isolated per cloud account. Account switching does not isolate local records. A separate database/ownership migration is needed for shared-device use.
- CI/build results must be recorded separately; physical-device and full end-to-end flows remain untested. Existing APK installations do not update themselves. Production requires a stable protected release signing key.

References:
https://supabase.com/docs/guides/auth/password-security
https://supabase.com/docs/guides/auth/rate-limits
https://supabase.com/docs/guides/auth/sessions
https://developer.android.com/identity/data/autobackup
