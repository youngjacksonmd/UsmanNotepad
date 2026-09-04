# UsmanNotepad Security

## Current Phase 0/1 boundary

UsmanNotepad is local-first and makes no network request for normal note workflows. Phase 0/1 does **not** claim database-at-rest encryption, biometric protection, per-note encryption, or certified security.

The existing Android database must remain readable during the Flutter migration, so Phase 0/1 keeps compatibility with the current unencrypted SQLite file. This is an explicit transition boundary, not a security claim.

## What is protected now

- Normal note operations work without an account or network.
- Application code does not transmit note content.
- Production logging must never contain note titles, bodies, checklist text, clipboard content, credentials, or cryptographic material.
- Soft delete protects against accidental deletion; permanent delete is separately confirmed.
- Database writes use transactions where multiple records must stay consistent.
- Durable editor drafts reduce loss from lifecycle interruption or write failure.

## What is not yet protected

Until Phase 4 is implemented and verified:

- a device/root/file-system attacker may be able to read the SQLite database;
- there is no production PIN or biometric gate in the Flutter track;
- individual notes are not cryptographically locked;
- screenshots/recent-app previews are not yet suppressed;
- clipboard hardening is not yet active;
- local backups are not yet encrypted because backup is not yet exposed.

The UI and marketing must not imply otherwise.

## Key storage

No encryption key is created in Phase 0/1. When encryption arrives, keys and biometric-related secrets must live in OS-backed secure storage/keystore and never in SQLite, SharedPreferences, logs, source code, or analytics.

## Cryptography policy

No home-made cryptography. Phase 4 must use audited libraries/algorithms and a vetted SQLCipher-compatible SQLite solution or equivalent reviewed implementation.

## Biometric boundary

Future biometrics authenticate access to key material/app state; biometric templates remain owned by the operating system. Locked content must never be rendered before successful authentication.

## Cloud visibility

There is no cloud sync in Phase 0/1. Future sync must be optional and designed so local edits remain authoritative. Encryption semantics and provider visibility must be documented before sync ships.

## Backup security

No user-facing backup feature is exposed in Phase 0/1. Future backup must use consistent snapshots, integrity validation, atomic file replacement, optional audited encryption, and authentication before exporting sensitive protected content.

## Threat assumptions

The application protects primarily against accidental loss and accidental disclosure during normal app use in Phase 0/1. It does not yet claim to resist a compromised/rooted OS, forensic access to an unlocked device, malicious accessibility services, or a user who deliberately exports/copies content.

## Security testing gate for Phase 4

Before privacy-lock functionality ships, tests must cover: locked-preview leakage, restart persistence, biometric cancellation, app-background privacy, failed PIN attempts, unavailable secure storage, encrypted-backup validation, and authentication before sensitive export.
