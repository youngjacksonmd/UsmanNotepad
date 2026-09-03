# UsmanNotepad V2.00 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver UsmanNotepad V2.00 as a stable, offline-first Android APK that preserves V1 notes and adds organization, recovery, editor power features, privacy, reminders, linked notes, graph, capture/media, widgets, and import/export without external AI APIs or cloud dependencies.

**Architecture:** Move the Android project out of `UsmanNotepad_Project.zip` into checked-in source, keep Android Views for low migration risk, replace JSON SharedPreferences storage with a relational SQLite database, and isolate features into focused managers/helpers. Keep the app single-module (`:app`) but split responsibilities across data, editor, library, privacy, reminders, links, capture, and backup packages.

**Tech Stack:** Android Views, Java 17, Android SQLite APIs, Android Keystore, Biometric APIs, AlarmManager/notifications, AppWidget, platform share intents, camera/gallery intents, Android speech recognition, custom Canvas View.

**Spec:** `docs/superpowers/specs/2026-09-03-usmannotepad-v2-design.md`

## Global Constraints
- versionName `2.0.0`; versionCode `2`.
- JDK 17; AGP 8.9.2; Gradle 8.11.1.
- compileSdk 35; targetSdk 35; minSdk 23; Build Tools 35.0.0.
- No OpenAI API, external AI API, cloud account, analytics SDK, or mandatory network dependency.
- Existing V1 notes in SharedPreferences key `notepad_data` / `notes` must migrate transactionally and remain recoverable.
- Permission-gated features must fail safely when permission is denied.
- CI must build checked-in source, run tests, assemble debug APK, and upload artifact exactly named `UsmanNotepad-APK`.

---

## File Structure to Create/Normalize

- `settings.gradle.kts` — repositories and `:app` include.
- `build.gradle.kts` — AGP 8.9.2 plugin declaration.
- `gradle.properties` — JVM/Android settings.
- `app/build.gradle.kts` — Android config, test dependencies, version 2.0.0.
- `app/src/main/AndroidManifest.xml` — activities, receivers, share target, widget, notification permissions.
- `app/src/main/java/com/usman/notepad/data/NotepadDbHelper.java` — schema and DB lifecycle.
- `app/src/main/java/com/usman/notepad/data/NoteRepository.java` — CRUD/search/filter API.
- `app/src/main/java/com/usman/notepad/data/V1Migration.java` — SharedPreferences JSON migration.
- `app/src/main/java/com/usman/notepad/model/*.java` — Note, Folder, Tag, Revision, Reminder, Attachment.
- `app/src/main/java/com/usman/notepad/ui/MainActivity.java` — library/navigation.
- `app/src/main/java/com/usman/notepad/ui/EditorActivity.java` — editor shell.
- `app/src/main/java/com/usman/notepad/ui/HistoryActivity.java` — revision restore.
- `app/src/main/java/com/usman/notepad/ui/GraphActivity.java` — graph view.
- `app/src/main/java/com/usman/notepad/editor/CommandParser.java` — slash commands.
- `app/src/main/java/com/usman/notepad/editor/WikiLinkParser.java` — `[[links]]` parsing.
- `app/src/main/java/com/usman/notepad/editor/SmartExtractors.java` — related/summary/action heuristics.
- `app/src/main/java/com/usman/notepad/privacy/CryptoManager.java` — Keystore AES-GCM operations.
- `app/src/main/java/com/usman/notepad/privacy/AppLockManager.java` — PIN/biometric gate state.
- `app/src/main/java/com/usman/notepad/reminders/ReminderScheduler.java` — alarms and notifications.
- `app/src/main/java/com/usman/notepad/reminders/ReminderReceiver.java` — notification receiver.
- `app/src/main/java/com/usman/notepad/capture/ShareCaptureActivity.java` — share-to-inbox.
- `app/src/main/java/com/usman/notepad/capture/DrawingView.java` — drawing pad.
- `app/src/main/java/com/usman/notepad/widget/QuickNoteWidgetProvider.java` — quick actions widget.
- `app/src/main/java/com/usman/notepad/backup/BackupManager.java` — txt/md/backup import-export.
- `app/src/test/java/com/usman/notepad/...` — unit tests.
- `.github/workflows/main.yml` — test/build/upload V2 APK.

---

### Task 1: Check in the Android project and lock the V2 build toolchain

**Files:** create/normalize root Gradle files, `app/`, manifest, existing V1 Java sources, resources, and `.github/workflows/main.yml`.

**Produces:** a checked-in V1-equivalent app that builds directly from repository source with version 2.0.0 metadata.

- [ ] Extract the existing ZIP source faithfully and commit it as normal repository files before feature refactors.
- [ ] Change root plugin to `com.android.application` 8.9.2 and app config to SDK 35 / Build Tools 35.0.0 / versionCode 2 / versionName 2.0.0.
- [ ] Replace CI unzip/patch logic with direct `gradle testDebugUnitTest :app:assembleDebug` against checked-in source.
- [ ] Run CI. Expected: unit-test task succeeds (even if zero tests initially), `assembleDebug` succeeds, `UsmanNotepad-APK` artifact uploads.
- [ ] Commit message: `build: check in V2 Android source and stable toolchain`.

### Task 2: SQLite foundation and V1 migration

**Interfaces:**
- `V1Migration.Result migrateIfNeeded(Context, SQLiteDatabase)` returns parsed, inserted, skipped counts and success flag.
- `NoteRepository long createNote(String title, String body)`.
- `NoteRepository Note getNote(long id)`.
- `NoteRepository void updateNote(Note note)`.
- `NoteRepository void softDelete(long id)`.
- `NoteRepository List<Note> listNotes(NoteQuery query)`.

- [ ] Write `V1MigrationTest` covering valid JSON, malformed entry skip, idempotent rerun, and transaction rollback behavior.
- [ ] Run `gradle testDebugUnitTest --tests '*V1MigrationTest'`; expected failing tests before implementation.
- [ ] Implement `NotepadDbHelper` schema from spec and `V1Migration` transaction logic; keep old preferences untouched.
- [ ] Implement `NoteRepository` basic CRUD and soft-delete.
- [ ] Rewire MainActivity/EditorActivity create/edit/save/delete to repository without changing user-visible behavior.
- [ ] Run migration + repository tests, then full unit tests and `assembleDebug`.
- [ ] Commit message: `feat: add SQLite storage and V1 note migration`.

### Task 3: Search, folders, tags, pin/favorite, archive, recycle bin

**Interfaces:**
- `NoteQuery` fields: text, folderId, tagIds, pinnedOnly, favoriteOnly, archivedOnly, deletedOnly, inboxOnly.
- `FolderRepository createFolder(String name, Long parentId)`, `moveFolder(long id, Long parentId)`, `deleteFolderMoveNotesToRoot(long id)`.
- `TagRepository setTagsForNote(long noteId, List<String> names)`.

- [ ] Write tests for title/body search, nested folders, folder deletion behavior, multi-tag filtering, pin/favorite ordering, and recycle-bin restore/purge.
- [ ] Implement repositories and SQL queries/indexes.
- [ ] Update MainActivity with search box, filters, folder/tag navigation, pin/favorite/archive/delete actions.
- [ ] Add recycle-bin screen/actions with 30-day purge logic executed on maintenance/app launch.
- [ ] Run all tests and build APK.
- [ ] Commit message: `feat: add organization search and recycle bin`.

### Task 4: Editor power features, templates, scratch, daily notes, quick copy

**Interfaces:**
- `CommandParser.Command parse(String token)` supporting check/date/time/heading/divider/quote/reminder/link.
- `RevisionRepository snapshotIfNeeded(long noteId, String title, String body, long now)`.
- `DailyNoteService long getOrCreate(LocalDate date)`.
- `TemplateRepository long createFromTemplate(long templateId)`.

- [ ] Write tests for command parsing, revision coalescing/pruning, deterministic daily-note identity, and template cloning.
- [ ] Implement debounced auto-save plus visible Saving/Saved state and save-on-pause.
- [ ] Implement history snapshots + HistoryActivity preview/restore.
- [ ] Implement note modes: Text, Checklist, Meeting, Journal, Shopping.
- [ ] Implement built-in/custom templates, scratch pad promotion, daily note, focus mode, per-note theme key, and Quick Copy explicit clipboard action.
- [ ] Add swipe gestures for pin/archive and overflow delete.
- [ ] Run tests and APK build.
- [ ] Commit message: `feat: add V2 editor productivity features`.

### Task 5: Privacy, time capsules, expiry, reminders

**Interfaces:**
- `CryptoManager.EncryptedPayload encrypt(byte[] plaintext)` and `byte[] decrypt(EncryptedPayload payload)` using Android Keystore AES-GCM.
- `AppLockManager boolean isUnlocked()` and `authenticate(Activity, AuthCallback)`.
- `ReminderScheduler schedule(Reminder reminder)` and `cancel(long reminderId)`.

- [ ] Write pure unit tests for lock-state decisions, time-capsule visibility, expiry transition logic, and reminder intent payload construction.
- [ ] Implement Keystore-backed encryption for protected note body payloads and optional hidden titles.
- [ ] Implement app lock and per-note authentication using biometric/device credential where supported plus optional app PIN fallback.
- [ ] Ensure locked bodies are excluded from normal search while encrypted/locked.
- [ ] Implement future unlock UI enforcement and opportunistic expiry-to-bin maintenance.
- [ ] Implement date/time reminders with notification channels and runtime notification permission handling; avoid exact-alarm permission by default.
- [ ] Run tests, lint-critical compile checks, and APK build.
- [ ] Commit message: `feat: add note privacy and reminders`.

### Task 6: Wiki links, backlinks, graph, offline smart extraction

**Interfaces:**
- `WikiLinkParser List<String> extractTitles(String body)`.
- `LinkRepository rebuildLinksForNote(long noteId, String body)`.
- `SmartExtractors List<Long> relatedNotes(long noteId, int limit)`.
- `SmartExtractors String quickSummary(Note note)`.
- `SmartExtractors List<String> actionItems(Note note)`.

- [ ] Write parser tests for multiple links, duplicates, malformed brackets, and renamed target handling.
- [ ] Write scoring tests for direct link > shared tags > keyword overlap > recency tie-break.
- [ ] Implement relationship persistence and backlinks panel in editor.
- [ ] Implement GraphActivity with custom view: nodes, edges, pan/zoom, tap-to-open; cap/filter large graphs.
- [ ] Implement rule-based Quick Summary and Action Items with labels that do not claim AI.
- [ ] Run tests and APK build.
- [ ] Commit message: `feat: add linked notes graph and offline smart tools`.

### Task 7: Share inbox, voice, image notes, drawing pad

**Interfaces:**
- `AttachmentRepository add(long noteId, String type, String localPath)`.
- `ShareCaptureActivity` accepts `ACTION_SEND` text and supported image URIs.

- [ ] Write tests for share-intent parsing and attachment metadata validation.
- [ ] Add share target flow that always shows confirmation/edit screen and saves with `is_inbox=1`.
- [ ] Add voice-to-note via Android speech recognition; wording must say availability/offline behavior depends on device provider.
- [ ] Add optional raw audio attachment path if user explicitly records/saves audio.
- [ ] Add camera/gallery image attachment flow using app-safe URIs.
- [ ] Add DrawingView with pen, eraser, undo, clear, save-to-local-image attachment.
- [ ] Verify permission denial paths do not crash.
- [ ] Run tests and APK build.
- [ ] Commit message: `feat: add capture media and drawing tools`.

### Task 8: Widget, import/export/backup, final UI integration

**Interfaces:**
- `BackupManager exportNote(long id, Format format, OutputStream out)`.
- `BackupManager exportAll(OutputStream out)`.
- `BackupManager ImportResult importBackup(InputStream in)`.
- Widget intents: `ACTION_NEW_NOTE`, `ACTION_SCRATCH`, `ACTION_DAILY`.

- [ ] Write backup round-trip tests including ID conflict handling and corrupt archive rejection.
- [ ] Implement single-note `.txt` / `.md` export and local structured all-notes backup/restore.
- [ ] Implement `.txt` / `.md` import as new notes.
- [ ] Implement QuickNoteWidgetProvider with New Note, Scratch, Daily Note actions and no locked-body exposure.
- [ ] Finalize navigation destinations: All Notes, Favorites, Pinned, Inbox, Folders, Tags, Templates, Recycle Bin, Settings.
- [ ] Run full test suite and debug APK build.
- [ ] Commit message: `feat: finish V2 backup widget and navigation`.

### Task 9: Final CI hardening and release verification

**Files:** `.github/workflows/main.yml`, optional `README.md` release notes.

- [ ] Ensure CI uses `actions/checkout@v5`, `actions/setup-java@v5`, `gradle/actions/setup-gradle@v6`, and a currently supported artifact upload action already proven on the runner.
- [ ] CI command must run unit tests before `:app:assembleDebug` and fail on either error.
- [ ] Verify artifact is named exactly `UsmanNotepad-APK` and contains the generated debug APK.
- [ ] Fetch completed job logs and confirm every build/test/upload step conclusion is `success`.
- [ ] Download the final workflow artifact and inspect archive contents to verify an APK exists.
- [ ] Report commit SHA, workflow run URL, artifact name, APK download, known optional/deferred features (location reminders, overlay, after-call, cloud sync, real on-device LLM/OCR).
- [ ] Commit message if CI-only changes are required: `ci: finalize UsmanNotepad V2 APK pipeline`.

## Final Acceptance Checklist

- Existing create/edit/save/delete behavior works.
- V1 notes migrate once without data loss and legacy prefs remain intact.
- Search/folders/tags/pin/favorite/archive/recycle bin work locally.
- Auto-save/history/templates/modes/scratch/daily/quick-copy/focus/themes work.
- App/per-note lock and encrypted protected payloads work without network.
- Time capsule/expiry/reminders behave safely.
- Wiki links/backlinks/graph/related/summary/action extraction work offline.
- Share inbox/voice/image/drawing paths function or fail gracefully when unavailable/denied.
- Widget and import/export/backup are present.
- No OpenAI/external AI API/cloud account requirement exists.
- GitHub Actions passes and publishes `UsmanNotepad-APK`.
