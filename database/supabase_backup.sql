-- Supabase Auth verifies JWTs; the owner is the token subject.
-- Owner IDs remain text for compatibility with the installed backup schema.
create table public.cng_mitra_backups (
    id uuid primary key default gen_random_uuid(),
    user_id text not null default (auth.jwt()->>'sub'),
    created_at timestamptz not null default now(),
    payload jsonb not null,
    constraint backup_user_nonempty check (length(user_id) between 1 and 128),
    constraint backup_version check ((payload->>'version')::integer = 1),
    constraint backup_shape check (
        jsonb_typeof(payload) = 'object'
        and payload ?& array['version', 'cars', 'refills']
        and jsonb_typeof(payload->'cars') = 'array'
        and jsonb_typeof(payload->'refills') = 'array'
    ),
    constraint backup_size check (octet_length(payload::text) <= 5000000)
);
create index cng_mitra_backups_user_latest
    on public.cng_mitra_backups (user_id, created_at desc, id desc);
alter table public.cng_mitra_backups enable row level security;
revoke all on public.cng_mitra_backups from public, anon, authenticated;
grant select on public.cng_mitra_backups to authenticated;
grant insert (user_id, payload) on public.cng_mitra_backups to authenticated;
create policy "Read own CNG backups" on public.cng_mitra_backups
    for select to authenticated using (user_id = (select auth.jwt()->>'sub'));
create policy "Create own CNG backups" on public.cng_mitra_backups
    for insert to authenticated with check (user_id = (select auth.jwt()->>'sub'));
comment on table public.cng_mitra_backups is
    'Append-only manual vehicle/refill backups; no client update or delete access.';
