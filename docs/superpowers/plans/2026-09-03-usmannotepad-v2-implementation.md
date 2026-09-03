# UsmanNotepad V2.00 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver UsmanNotepad V2.00 as a buildable, installable, local-first Android APK with V1 note migration and the approved Complete Local Edition feature set.

**Architecture:** Replace the ZIP-at-build layout with checked-in Android source. Keep the app lightweight with Android Views, SQLiteOpenHelper, Android Keystore, AlarmManager/notifications, custom Views for drawing/graph, and platform intents for share/voice/image capture. Core note data is relational SQLite; protected note bodies are encrypted locally and decrypted only after explicit unlock.

**Tech Stack:** Java 17, Android SDK 35, AGP 8.9.2, Gradle 8.11.1, Build Tools 35.0.0, SQLite, Android platform APIs, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-09-03-usmannotepad-v2-design.md`

## Global Constraints
- versionName `2.0.0`, versionCode `2`.
- minSdk 23, compileSdk/targetSdk 35.
- No OpenAI API, cloud backend, analytics SDK, or required account.
- Existing V1 SharedPreferences notes must migrate transactionally and remain recoverable.
- CI builds checked-in source directly and uploads `UsmanNotepad-APK`.
- Permission-gated features must fail safely when denied.
- Do not claim heuristic smart tools are AI.

---

### Task 1: Source Tree, Database, V1 Migration, CI

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`, `app/src/main/res/values/styles.xml`, `app/src/main/res/values/strings.xml`
- Create: `app/src/main/java/com/usman/notepad/model/Note.java`
- Create: `app/src/main/java/com/usman/notepad/data/NotepadDb.java`
- Create: `app/src/main/java/com/usman/notepad/data/NoteRepository.java`
- Create: `app/src/main/java/com/usman/notepad/data/V1Migrator.java`
- Create: `app/src/test/java/com/usman/notepad/V1MigratorTest.java`
- Modify: `.github/workflows/main.yml`

**Interfaces:**
- `NoteRepository(Context)` is the single UI-facing data access object.
- `long save(Note note, boolean snapshot)` upserts and optionally records a revision.
- `List<Note> list(String query, String filter)` returns non-expired notes.
- `void runMaintenance()` expires notes and purges old trash.
- `V1Migrator.migrateIfNeeded(Context, SQLiteDatabase)` preserves V1 IDs/timestamps.

- [ ] Write migration/parser tests using representative V1 JSON with empty, Unicode, and malformed entries.
- [ ] Run `gradle :app:testDebugUnitTest` and verify tests initially fail before parser/data helpers exist.
- [ ] Implement schema, repository, transactional migration, and maintenance.
- [ ] Run unit tests until green.
- [ ] Run `gradle :app:assembleDebug` and verify APK exists.
- [ ] Commit source-tree/CI foundation atomically.

### Task 2: Library Organization and Recovery

**Files:**
- Create: `app/src/main/java/com/usman/notepad/MainActivity.java`
- Create: `app/src/main/java/com/usman/notepad/ui/LibraryActions.java`
- Create: `app/src/main/java/com/usman/notepad/FoldersActivity.java`
- Create: `app/src/main/java/com/usman/notepad/TagsActivity.java`
- Create: `app/src/main/java/com/usman/notepad/TrashActivity.java`
- Create: `app/src/test/java/com/usman/notepad/RepositoryRulesTest.java`

**Interfaces:**
- Repository folder/tag CRUD plus note-folder/tag relations.
- `togglePin`, `toggleFavorite`, `archive`, `trash`, `restore`, `purge` mutations.
- Search matches note title/body plus tag/folder names.

- [ ] Add rule tests for nested folders, soft delete/restore, trash retention, pin/favorite/filter behavior.
- [ ] Implement local search, filters, folders, nested folders, tags, pin/favorite, archive, trash, and quick-copy flag.
- [ ] Implement library UI and gesture/overflow actions with permission-free behavior.
- [ ] Run unit tests and debug build.
- [ ] Commit only after CI build/test passes.

### Task 3: Editor Power Features

**Files:**
- Create: `app/src/main/java/com/usman/notepad/EditorActivity.java`
- Create: `app/src/main/java/com/usman/notepad/TemplatesActivity.java`
- Create: `app/src/main/java/com/usman/notepad/HistoryActivity.java`
- Create: `app/src/main/java/com/usman/notepad/util/CommandEngine.java`
- Create: `app/src/main/java/com/usman/notepad/util/SmartTools.java`
- Create: `app/src/test/java/com/usman/notepad/CommandEngineTest.java`
- Create: `app/src/test/java/com/usman/notepad/SmartToolsTest.java`

**Interfaces:**
- Slash commands transform text using `CommandEngine.apply(command, body, now)`.
- `SmartTools.quickSummary`, `SmartTools.actionItems`, `SmartTools.relatedScore` are deterministic/offline.
- Revision restore creates a new revision rather than deleting history.

- [ ] Add tests for slash commands, summary/action extraction, related scoring, and revision pruning.
- [ ] Implement auto-save, current-session undo, note modes, templates, focus mode, per-note themes, history, Scratch Pad, Daily Note, Quick Copy, time capsule, and expiry settings.
- [ ] Add visible `Saving…` / `Saved` states and safe back-navigation save.
- [ ] Run tests and build, then commit.

### Task 4: Privacy and Reminders

**Files:**
- Create: `app/src/main/java/com/usman/notepad/security/CryptoManager.java`
- Create: `app/src/main/java/com/usman/notepad/security/AppLock.java`
- Create: `app/src/main/java/com/usman/notepad/ReminderReceiver.java`
- Create: `app/src/main/java/com/usman/notepad/ReminderScheduler.java`
- Create: `app/src/main/java/com/usman/notepad/SettingsActivity.java`
- Create: `app/src/test/java/com/usman/notepad/TimeRulesTest.java`

**Interfaces:**
- `CryptoManager.encrypt/decrypt` uses AES/GCM key material stored in Android Keystore.
- Locked body text is never returned to normal search/preview calls until unlocked.
- `ReminderScheduler.schedule/cancel` owns AlarmManager behavior.

- [ ] Add pure time-rule tests for capsule/expiry/reminder state.
- [ ] Implement app PIN plus platform biometric/device credential where available, per-note lock, configurable title hiding, Keystore encryption, reminder scheduling and notifications.
- [ ] Ensure permission denial cannot crash editor/library.
- [ ] Run tests/build and commit.

### Task 5: Links, Backlinks, Graph, Capture and Media

**Files:**
- Create: `app/src/main/java/com/usman/notepad/util/WikiLinkParser.java`
- Create: `app/src/main/java/com/usman/notepad/GraphActivity.java`
- Create: `app/src/main/java/com/usman/notepad/ui/GraphView.java`
- Create: `app/src/main/java/com/usman/notepad/ShareCaptureActivity.java`
- Create: `app/src/main/java/com/usman/notepad/DrawingActivity.java`
- Create: `app/src/main/java/com/usman/notepad/ui/DrawingView.java`
- Create: `app/src/test/java/com/usman/notepad/WikiLinkParserTest.java`

**Interfaces:**
- Parser emits normalized `[[title]]` tokens; repository resolves/stores durable note IDs.
- Graph consumes repository note/link snapshots and renders a capped/filterable local graph.
- Share capture only imports explicitly shared text/image payloads.

- [ ] Add wiki-link parser/backlink tests.
- [ ] Implement links/backlinks, related-notes panel, graph view, voice-to-note intent, image attachment picker/camera path, drawing pad, and share-to-inbox.
- [ ] Verify missing/denied providers fail with user-visible messages rather than crashes.
- [ ] Run tests/build and commit.

### Task 6: Widget, Import/Export/Backup, Final CI/APK

**Files:**
- Create: `app/src/main/java/com/usman/notepad/QuickNoteWidget.java`
- Create: `app/src/main/res/xml/notepad_widget_info.xml`
- Create: `app/src/main/res/layout/notepad_widget.xml`
- Create: `app/src/main/java/com/usman/notepad/io/BackupManager.java`
- Create: `app/src/main/res/xml/file_paths.xml`
- Create: `app/src/test/java/com/usman/notepad/BackupCodecTest.java`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `.github/workflows/main.yml`

**Interfaces:**
- Backup codec round-trips notes/folders/tags/links/revisions/reminders metadata without silently overwriting conflicts.
- Widget actions open New Note, Scratch, and Daily Note; locked bodies are never rendered in RemoteViews.

- [ ] Add backup encode/decode round-trip tests.
- [ ] Implement txt/md import, single-note export, structured ZIP backup/restore, widget actions, and final settings links.
- [ ] Run full `:app:testDebugUnitTest :app:assembleDebug`.
- [ ] Verify workflow artifact name exactly `UsmanNotepad-APK` and APK is non-empty/installable-format ZIP/APK.
- [ ] Commit final V2.00 delivery and download final artifact for handoff.

## Explicit V2.00 Non-Baseline Items
Per approved spec, true cloud/E2E sync, self-host server, real on-device LLM, after-call popup, mandatory overlay, and always-on background location tracking are not part of V2.00. Optional location reminders may remain disabled if reliable implementation would require a new Google Play Services/background-location subsystem.
