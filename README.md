# CNG मित्र — Development Source

Android development source for the CNG मित्र driver companion.

## Looking for the maintained project?

Visit **[CNG-Mitra](https://github.com/ayushkapure26/CNG-Mitra)** for the documented build setup, automated checks, and debug APK downloads.

This repository contains an earlier project source snapshot. Build and service configuration instructions are maintained in the linked repository.

## Source layout

- `app/` — Android application source and resources
- `gradle/` — Dependency version catalog
- `.env.example` — Placeholder configuration for optional API integrations

Do not commit privileged API keys or private signing files. The Supabase public anon key is a client identifier protected by database RLS.

## Supabase backup

Manual vehicle/refill backup and restore is available in Settings. See
[Supabase setup](docs/SUPABASE_SETUP.md) for the database SQL, Supabase email login
integration and required environment configuration.
