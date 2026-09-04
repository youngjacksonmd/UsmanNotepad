# UsmanNotepad Phase 0 + Phase 1 Flutter Core Design

## Goal

Deliver the first production milestone from the three UsmanNotepad source-of-truth documents: a real Flutter-based, offline-first core notepad that preserves existing Android data and passes the V1 acceptance flow before any later-phase feature is exposed.

## Scope boundary

In scope: foundation; create/edit/autosave; soft delete/Trash/restore/permanent delete; undo/redo; FTS title/body search; pin; favorite; basic checklist; real checklist-backed Tasks view; local database; light/dark/system theme; notes list/grid; basic settings; migration/tests/CI.

Out of scope: folders/tags UI, archive UI, colors, Scratch Pad, reminders, history UI, backup/export, app/note locks, encryption, media, links/graph, sync, AI, platform widgets. Future schema may reserve these concepts but UI will not pretend they work.

## Existing-code constraint

The repository currently ships a native Android implementation with application id `com.usman.notepad` and SQLite database `usman_notepad_v2.db`. Replacing that code on `main` before the Flutter path is verified risks user data and rollback safety. The Flutter implementation therefore lives in `flutter_app/` on an isolated branch and preserves package/database identity for eventual upgrade testing.

## Architecture

Riverpod is the only state-management approach. `go_router` owns navigation. Drift owns SQLite. Domain repository contracts separate UI from persistence. Typed failures and redacted logging live in core. The editor keeps typing synchronous and treats local state as authoritative.

## Data preservation

Flutter schema version is 2. Android resolves the native database location by deriving the app data parent from the Flutter documents directory and opening `databases/usman_notepad_v2.db`. Upgrade from native schema version 1 adds columns/tables/indexes/FTS in place and never deletes note rows. Migration tests construct the v1 shape, insert multilingual note data, upgrade, and assert exact survival.

## Autosave

Each editor has one controller. It owns title/body/checklist state, edit history, save generation, and timers. Typing immediately changes state. A short draft debounce writes `editor_drafts`; a slightly longer canonical debounce serializes repository saves. Lifecycle or navigation flush cancels timers and awaits both writes. Canonical success clears the matching draft; failure leaves it intact.

## Search

FTS5 external-content table indexes note `title` and `body`. Triggers keep it synchronized. Checklist repository writes a plain-text body mirror in the same transaction. Search escapes terms, runs MATCH against FTS, joins notes, excludes deleted/archived rows, and returns rank/snippet.

## UI system

Soft Canvas uses a warm off-white light canvas, white/paper-like cards, layered warm-charcoal dark surfaces, restrained indigo accent, subtle tonal depth, centralized spacing/radius/type/motion tokens, and content-first composition. Routine motion remains roughly 120–180 ms; larger expansion about 180–220 ms. Reduced-motion preference removes non-essential transitions.

Home contains greeting, premium search affordance, quick Text/Checklist capture, Pinned and Recent. Notes provides All/Pinned/Favorites and a persisted list/grid preference. Editor is page-like with Back/Pin/More, optional title, body/checklist, subtle save state and reachable minimal editing tools. Search emphasizes query/results. Trash is visually neutral and reversible. Settings exposes only working Phase-1 preferences.

## Error behavior

Database/storage failures never silently discard draft state. Delete failure leaves note visible. Search syntax errors are prevented by query escaping. Missing notes route back with an actionable message. User-facing errors name the failed action where possible instead of generic `Something went wrong`.

## Testing

TDD covers pure edit history/query escaping first, then migration/repositories, then controllers/widgets. CI generates Drift code, formats, analyzes, runs unit/widget tests, builds debug and release APKs, and publishes logs/artifacts. A milestone is only reported as verified when the corresponding command has actually passed.

## Risks and controls

- **Legacy migration**: highest risk; migrate in place, preserve columns, dedicated fixture test, no destructive fallback.
- **Editor data loss**: immediate local state + durable draft + serialized save + lifecycle flush.
- **Large-note memory**: bounded/coalesced history and no full-database reload.
- **FTS query syntax**: escaped terms and tests for punctuation/quotes/Unicode.
- **Flutter/native coexistence**: isolated branch/directory until verified; same app/database identity for eventual upgrade tests.
- **Encryption**: deliberately not faked in Phase 1; audited Phase 4 migration boundary documented.
- **Cross-platform path differences**: Android legacy path adapter; other platforms use normal app-owned DB path.
- **Android background behavior**: save on lifecycle transitions; no network dependency.
- **Visual overreach**: centralized Soft Canvas tokens, advanced controls hidden until functional.
