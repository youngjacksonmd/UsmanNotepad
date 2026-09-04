# UsmanNotepad Privacy

## Default posture

Phase 0/1 stores notes locally on the device. Core create/edit/search/pin/favorite/checklist/Trash workflows require no account and no internet.

## Network

The Phase 0/1 application contains no note-sync or AI request path. Search uses local SQLite FTS5. Normal note content is not sent to a server.

## Analytics and diagnostics

No analytics SDK is required for Phase 0/1. If diagnostics are added later, note titles, note text, checklist content, attachments, clipboard content, credentials, encryption material and private paths must be excluded. Users must be able to disable non-essential telemetry.

## Settings

Only non-secret preferences such as theme and note view style use SharedPreferences/settings storage. Future tokens, PIN-derived secrets, encryption keys and recovery metadata require OS-backed secure storage.

## Deletion

Normal delete moves a note to Trash so accidental deletion is reversible. Permanent deletion is explicit and irreversible at the application layer.

## Future cloud/AI

Cloud sync and AI are optional later phases. Neither is a prerequisite for local notes. Their data flows, retention, provider visibility and consent controls must be documented before release.
