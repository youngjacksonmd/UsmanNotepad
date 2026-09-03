# UsmanNotepad V2.00 Complete Local Edition — Design

## Goal
Upgrade the current offline UsmanNotepad into a private, local-first Android notes app that preserves the existing create/edit/save/delete behavior while adding strong organization, recovery, capture, privacy, linking, reminders, media, and lightweight offline smart features without requiring an account, cloud backend, OpenAI key, or external AI API.

## Product Principles
- Offline-first by default.
- No account required.
- No external API keys.
- Existing notes must survive the upgrade.
- Sensitive features are permission-gated and default-off where appropriate.
- Core note editing must remain fast even on low/mid-range Android devices.
- Features must degrade safely if permissions are denied.
- Prefer Android platform capabilities and small dependencies over heavyweight frameworks.
- Do not label heuristic features as “AI” unless a real on-device model is added later.

## Version Target
- App versionName: `2.0.0`
- App versionCode: `2`
- Java/JDK: `17`
- Android Gradle Plugin: `8.9.2`
- Gradle: `8.11.1`
- compileSdk: `35`
- targetSdk: `35`
- minSdk: `23`
- Android Build Tools: `35.0.0`

## Repository Restructure
The current repository stores the Android project inside `UsmanNotepad_Project.zip`, and the workflow unzips/patches it during CI. V2 will move the Android source tree into normal version-controlled repository paths:

- `app/`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `gradle/`
- `.github/workflows/main.yml`

The legacy ZIP may remain temporarily as a migration/reference artifact, but CI must build the checked-in source tree directly. This removes build-time source mutation and makes V2 maintainable.

## Architecture Overview
Use a small modular Android app with Java/Kotlin-compatible Android APIs and SQLite-backed local storage. UI may remain primarily Android Views to minimize dependency and migration risk.

Primary modules/responsibilities:

1. **Data Store** — notes, folders, tags, revisions, reminders, attachments, links, deleted state.
2. **Editor** — note editing, modes, commands, auto-save, focus mode, themes.
3. **Library** — notes list, search, filters, folders, tags, pin/favorite, recycle bin.
4. **Knowledge Links** — `[[Wiki Links]]`, backlinks, related-note suggestions, graph data.
5. **Capture** — scratch pad, share-to-inbox, quick-copy, daily notes, voice/image/drawing capture.
6. **Privacy** — app lock, per-note lock, Android Keystore-backed encryption for protected content.
7. **Automation** — reminders, time capsules, expiry cleanup, optional location-triggered reminders.
8. **Widgets/Quick Access** — home-screen quick note and recent/pinned note entry points.

## Data Model
SQLite is the source of truth. The schema is intentionally relational so features do not depend on parsing one large JSON blob.

### `notes`
- `id INTEGER PRIMARY KEY`
- `title TEXT NOT NULL DEFAULT ''`
- `body TEXT NOT NULL DEFAULT ''`
- `mode TEXT NOT NULL DEFAULT 'text'`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`
- `folder_id INTEGER NULL`
- `is_pinned INTEGER NOT NULL DEFAULT 0`
- `is_favorite INTEGER NOT NULL DEFAULT 0`
- `is_archived INTEGER NOT NULL DEFAULT 0`
- `is_deleted INTEGER NOT NULL DEFAULT 0`
- `deleted_at INTEGER NULL`
- `is_locked INTEGER NOT NULL DEFAULT 0`
- `theme_key TEXT NULL`
- `unlock_at INTEGER NULL`
- `expires_at INTEGER NULL`
- `is_inbox INTEGER NOT NULL DEFAULT 0`

### `folders`
- `id INTEGER PRIMARY KEY`
- `name TEXT NOT NULL`
- `parent_id INTEGER NULL`
- `created_at INTEGER NOT NULL`

Supports nested folders through `parent_id`.

### `tags`
- `id INTEGER PRIMARY KEY`
- `name TEXT UNIQUE NOT NULL`

### `note_tags`
- `note_id INTEGER NOT NULL`
- `tag_id INTEGER NOT NULL`
- composite unique key `(note_id, tag_id)`

### `note_revisions`
- `id INTEGER PRIMARY KEY`
- `note_id INTEGER NOT NULL`
- `title TEXT NOT NULL`
- `body TEXT NOT NULL`
- `created_at INTEGER NOT NULL`

Revision snapshots are rate-limited/coalesced to avoid creating a row on every keystroke.

### `note_links`
- `source_note_id INTEGER NOT NULL`
- `target_note_id INTEGER NOT NULL`
- `link_text TEXT NOT NULL`
- unique source/target/link combination as appropriate

### `attachments`
- `id INTEGER PRIMARY KEY`
- `note_id INTEGER NOT NULL`
- `type TEXT NOT NULL` (`image`, `drawing`, `audio`)
- `local_path TEXT NOT NULL`
- `created_at INTEGER NOT NULL`

### `reminders`
- `id INTEGER PRIMARY KEY`
- `note_id INTEGER NOT NULL`
- `trigger_at INTEGER NULL`
- `latitude REAL NULL`
- `longitude REAL NULL`
- `radius_m INTEGER NULL`
- `enabled INTEGER NOT NULL DEFAULT 1`

## Existing Note Migration
The current V1 app stores notes in `SharedPreferences` under `notepad_data` / `notes` as JSON.

On first V2 launch:
1. Open the new SQLite database.
2. If migration marker is absent, read the old JSON array.
3. Insert all valid notes with original IDs, titles, bodies, and timestamps.
4. Verify inserted count against parsed valid note count.
5. Set a migration-complete marker only after successful transaction commit.
6. Leave the legacy SharedPreferences data intact for one release as a safety fallback.

Migration must be idempotent and transaction-protected.

## Core Library Features

### Search
- Instant title/body search.
- Search tags and folder names.
- Filters: pinned, favorites, locked, inbox, archived, deleted.
- Search runs locally only.

### Folders + Nested Folders
- Create, rename, move, and delete folders.
- A folder can contain child folders.
- Deleting a folder does not delete notes by default; notes move to root unless user explicitly chooses otherwise.

### Tags
- Multiple tags per note.
- Create tags inline from editor or library.
- Filter by one or more tags.

### Pin + Favorite
- Pinned notes float to top within current view.
- Favorites are a dedicated filter/view.

### Swipe Actions
Configurable defaults:
- Swipe right: pin/unpin.
- Swipe left: archive.
- Long swipe or overflow menu: delete.

### Recycle Bin
- Soft-delete first.
- Default retention: 30 days.
- Restore or permanently delete manually.
- Automatic purge can run opportunistically on app launch/maintenance rather than requiring continuous background execution.

## Editor Features

### Note Modes
Supported V2 modes:
- Text
- Checklist
- Meeting
- Journal
- Shopping

Modes are templates/editor behaviors, not separate incompatible storage formats.

### Auto-Save
- Debounced local save after changes.
- Visible `Saved` / `Saving…` status.
- Save on app background/Activity pause.
- Back navigation must not lose edits.

### Version History + Undo
- Create revision snapshots on meaningful save boundaries.
- Keep recent history with pruning limits to control storage.
- User can preview and restore a revision.
- Normal editor undo remains available for current-session text edits.

### Command Palette
Typing `/` at the start of a command token opens actions such as:
- `/check`
- `/date`
- `/time`
- `/heading`
- `/divider`
- `/quote`
- `/reminder`
- `/link`

Commands insert or transform content without requiring a toolbar full of buttons.

### Focus Mode
- Hide library/navigation chrome.
- Keep only editor content and minimal save/exit controls.

### Per-Note Theme
- Store a small theme key, not arbitrary styling blobs.
- Initial themes: system, light paper, dark, warm, high-contrast.

## Scratch Pad
A dedicated quick-capture note surface:
- Opens quickly from app home and widget.
- Auto-saves temporary content.
- User can promote scratch content into a normal note.
- Scratch content is not silently discarded.

## Daily Notes
- One deterministic note per calendar date.
- Opening today’s daily note creates it if missing.
- Reopening the same date returns the same note.

## Quick Copy
- Any note can be marked `Quick Copy`.
- Library shows a compact copy action.
- Tapping copies note body (or configured snippet) to clipboard without opening the editor.
- Clipboard copy must never happen automatically in background.

## Templates
Built-in templates:
- Meeting
- Journal
- Shopping
- Project
- Daily log

Users can save a note as a custom template. Template application creates a new note; it does not mutate the template itself.

## Time Capsule Notes
- User chooses a future unlock time.
- Before unlock time, list may show a locked placeholder/title depending on privacy option.
- Note body cannot be opened through normal UI before unlock time.
- This is not cryptographic time-locking; device clock/root access can defeat it. UI must not claim otherwise.

## Expiring Notes
- Optional expiry time.
- At/after expiry, note moves to recycle bin during next maintenance opportunity.
- No promise of exact-second deletion while device/app is offline.

## Privacy and Encryption

### App Lock
- Optional biometric/device-credential gate using Android Biometric APIs where supported.
- Fallback to app PIN if configured.

### Per-Note Lock
- Locked notes require app authentication before body access.
- Protected note title visibility is configurable.

### Encryption
- Protected note payloads are encrypted locally with keys protected by Android Keystore.
- Do not invent “zero-knowledge cloud encryption” because V2 has no cloud server.
- Unlocked/non-protected notes may remain normal SQLite text for search performance.
- Locked note bodies should not be placed into search indexes while encrypted/locked.

## Linked Notes + Backlinks

### Wiki Links
Typing `[[Note Title]]` creates a resolvable reference.

Behavior:
- Existing title: link to note.
- Missing title: user may create a new note with that title.
- Renaming a target note does not destroy the stored relationship; display text may remain user-entered.

### Backlinks
Each note displays notes that reference it.

### Graph View
- Render nodes for notes and edges for links.
- Initial graph should prioritize usability over advanced force-directed physics.
- Provide tap-to-open note and simple zoom/pan.
- Large libraries may cap or filter visible nodes.

## Offline Smart Features
V2 includes useful local heuristics without pretending they are an LLM.

### Related Notes
Calculate similarity using local tokens such as:
- shared tags
- title/body keyword overlap
- direct links/backlinks
- folder proximity
- recency weighting

### Lightweight Summary
Rule-based extraction for long notes:
- first meaningful sentence/lines
- headings
- bullet highlights

Label this `Quick summary`, not `AI summary`.

### Action Items
Detect explicit checklist syntax and common action prefixes such as `TODO`, `Action:`, `Next:`. Present extracted items in a side panel/filter.

A real on-device LLM is explicitly deferred to a later optional module.

## Voice Capture
- Use Android speech recognition intent/service when available.
- If the recognition provider requires network on the device, the app must not claim guaranteed offline transcription.
- Voice capture remains optional and the rest of the app works without it.
- Raw audio recording may be attached locally if user chooses.

## Image Notes
- Attach existing image or capture via camera intent.
- Store app-owned/local URI/path metadata.
- Images remain local unless user explicitly exports/shares them.
- OCR is not required for V2.00 baseline; local OCR may be a future enhancement.

## Drawing Pad
- Simple canvas with pen, eraser, undo/clear.
- Save drawing as a local image attachment linked to note.

## Share-to-Inbox
Register an Android share target for text and supported images.

Flow:
1. User shares from another app.
2. UsmanNotepad opens lightweight capture screen.
3. User confirms/edit content.
4. Saved note is marked `Inbox`.

No background scraping or reading other apps.

## Reminders
- Date/time reminders use Android alarm/notification mechanisms compatible with targetSdk 35.
- Request notification permission only when needed on supported Android versions.
- Exact alarm permission is avoided unless genuinely required; normal reminders can use inexact scheduling where appropriate.
- Reminder tap opens the associated note.

## Location Reminders
Optional feature, default-off:
- User explicitly selects a location and radius.
- Request location permission only when user enables this feature.
- Prefer foreground/limited mechanisms; avoid persistent tracking.
- If reliable background geofencing cannot be provided without additional Google Play Services dependency or permission complexity, V2 may ship location reminders as an optional module after core reminder functionality.

## Widget + Quick Capture
Initial widget capabilities:
- New note
- Scratch pad
- Daily note
- Open pinned/favorite note shortcut if practical

Widget must not expose locked note bodies.

## Floating Overlay / Lock-Screen Capture
These are not baseline-required because they introduce special permissions and platform restrictions.

V2 design permits them only as optional experiments after the core widget/share capture is stable:
- Overlay requires explicit `SYSTEM_ALERT_WINDOW` user approval.
- Lock-screen behavior varies by Android version/device policy.
- The app must remain fully useful without either permission.

## After-Call Popup
Not part of V2.00 baseline. Modern Android call-state/background restrictions and sensitive phone permissions make this a poor default feature for a private lightweight notepad. It may be investigated later as a separate optional module, but V2 must not request phone permissions solely for novelty.

## Import / Export / Backup
Baseline V2 data ownership features:
- Export single note as `.txt` or `.md`.
- Export all notes as a structured local backup archive.
- Import `.txt` / `.md` as notes.
- Restore app-created backup with conflict-safe IDs.

No cloud sync is included in V2.00.

## Markdown
- Plain text remains the canonical note body.
- Common Markdown syntax is supported for export and lightweight rendering where practical.
- `[[Wiki Links]]` remain an UsmanNotepad extension.

## Self-Hosted / Cloud Sync
Deferred from V2.00. A real sync system needs conflict resolution, authentication, transport, encryption protocol, device identity, retry behavior, and server/storage design. It will be designed as a separate subsystem later rather than bolted onto local notes.

## Error Handling
- Database writes use transactions where multi-table consistency matters.
- Failed attachment writes must not create dangling database rows.
- Corrupt imported files are rejected with user-visible explanation.
- Migration failure leaves V1 data untouched and allows retry.
- Permission denial never crashes the app; the feature remains disabled.
- Failed reminder scheduling shows state/error rather than silently pretending success.

## Security Boundaries
- No secrets or API keys committed to repository.
- No analytics SDK in V2 baseline.
- No background collection of call content, clipboard contents, location, or other-app data.
- Clipboard is accessed only through explicit copy actions.
- Exported unencrypted backups are clearly labeled as such; encrypted backup can be a later enhancement unless included in the implementation plan.

## UI Structure
Main navigation should remain simple:

### Home / Library
- Search bar
- New note
- Scratch
- Daily note
- Notes list
- Filter/sort
- Folder/tag navigation

### Navigation destinations
- All Notes
- Favorites
- Pinned
- Inbox
- Folders
- Tags
- Templates
- Recycle Bin
- Settings

### Note Editor
- Title
- Body/mode content
- Save state
- Overflow actions: lock, theme, reminder, history, move folder, tags, export, delete
- Contextual backlinks/related notes area

Avoid a dashboard full of cards that slows quick capture.

## Testing Strategy

### Unit Tests
- V1 JSON migration parser/migrator
- Database CRUD
- folder nesting rules
- tag relations
- recycle-bin retention logic
- revision pruning
- wiki-link parser
- backlink generation
- related-note scoring
- time capsule visibility logic
- expiry logic
- backup serialization/import

### Instrumentation / Android Tests
- create/edit/save/delete note
- migration preserves notes
- search finds title/body
- locked note authentication gate
- reminder intent opens note
- share-to-inbox receives text
- widget intents create/open correct destinations where feasible

### CI Verification
GitHub Actions must:
1. Build from checked-in Android source, not the ZIP.
2. Run unit tests.
3. Build `:app:assembleDebug`.
4. Upload artifact named exactly `UsmanNotepad-APK`.
5. Fail if APK is missing.

## Delivery Decomposition
V2 is too large to implement safely as one giant change. Implementation must be split into independently testable milestones while keeping one V2.00 product target.

### Milestone 1 — Source + Storage Foundation
- Extract source into repository.
- SQLite data layer.
- V1 migration.
- Version 2.0.0.
- Existing CRUD preserved.
- CI builds checked-in source.

### Milestone 2 — Library + Recovery
- Search.
- Folders/nested folders.
- Tags.
- Pin/favorite/archive.
- Recycle bin.
- Revision history.
- Auto-save.

### Milestone 3 — Editor Power Features
- Note modes/checklists.
- Templates.
- Command palette.
- Focus mode.
- Themes.
- Scratch pad.
- Daily notes.
- Quick copy.

### Milestone 4 — Privacy + Automation
- App lock.
- Per-note lock/encryption.
- Reminders.
- Time capsule.
- Expiring notes.

### Milestone 5 — Knowledge Network
- `[[Wiki Links]]`.
- Backlinks.
- Related-note heuristics.
- Quick summary/action-item extraction.
- Graph view.

### Milestone 6 — Capture + Ownership
- Share-to-inbox.
- Voice capture.
- Image attachments.
- Drawing pad.
- Home-screen widget.
- Import/export/backup.
- Optional location reminder only if platform/dependency trade-off remains acceptable after core V2 is stable.

Each milestone must leave the app buildable and usable. A milestone cannot be considered complete until CI builds successfully and existing core note behavior still works.

## Acceptance Criteria for V2.00
- Existing V1 notes migrate without loss.
- User can create, edit, auto-save, manually save where exposed, and delete/restore notes.
- Search, folders, tags, pin/favorite, recycle bin, and history work locally.
- Scratch, daily notes, templates, quick copy, focus mode, command palette, and themes work.
- Locked notes require authentication and protected payloads use Android Keystore-backed encryption.
- Reminders, time capsules, and expiry behavior function within documented Android scheduling limitations.
- `[[Wiki Links]]`, backlinks, related-note suggestions, and graph view work locally.
- Voice/image/drawing capture and share-to-inbox do not require an external API key.
- User can export/import notes and create a local backup.
- App requests sensitive permissions only at point of use.
- App works without an account and without network access for all core note/library/privacy/linking features.
- GitHub Actions completes successfully and uploads `UsmanNotepad-APK`.

## Explicit Non-Goals for V2.00
- Cloud account system.
- Real-time multi-device sync.
- Self-hosted sync server.
- OpenAI or other external AI APIs.
- Bundled heavyweight on-device LLM.
- Guaranteed offline speech transcription on every Android device.
- Guaranteed exact-second expiry while app/device is inactive.
- Default call monitoring / after-call popup.
- Mandatory overlay permission.

## Future V2.x / V3 Candidates
- Optional on-device ML/LLM pack.
- Local OCR.
- Encrypted portable backups.
- Self-hosted encrypted sync.
- Optional richer graph layouts.
- Advanced task/reminder dashboard.
- Opt-in location automation.
