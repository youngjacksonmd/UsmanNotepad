# UsmanNotepad Phase 0 + Phase 1 Flutter Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and verify the production Flutter foundation and excellent offline core notepad while preserving the existing Android database.

**Architecture:** Flutter presentation/application/domain/data layers with Riverpod, `go_router`, Drift/SQLite FTS5, durable autosave safety drafts, and centralized Soft Canvas design tokens. The Flutter track is isolated under `flutter_app/` until CI verifies it, and Android keeps `com.usman.notepad` plus the legacy database identity.

**Tech Stack:** Flutter 3.47.x, Dart 3.12+, Material 3, Riverpod, go_router, Drift, drift_flutter, SharedPreferences, flutter_staggered_grid_view, UUID.

**Spec:** `docs/superpowers/specs/2026-09-03-phase0-1-flutter-core-design.md`

## Global Constraints

- Never lose user data.
- No network dependency in core note workflows.
- No fake controls for unfinished later phases.
- Existing Android application id remains `com.usman.notepad`.
- Existing `usman_notepad_v2.db` rows must survive migration.
- No custom cryptography or misleading encryption claims.
- UI uses centralized Soft Canvas tokens and respects text scaling/reduced motion.
- Every behavior task follows RED -> verify failure -> GREEN -> verify pass -> refactor.

---

### Task 1: Flutter project + verification workflow

**Files:** create `flutter_app/pubspec.yaml`, `flutter_app/analysis_options.yaml`, `flutter_app/lib/main.dart`, `.github/workflows/flutter-phase0-1.yml`.

**Produces:** a dependency-resolvable project and CI commands for codegen, format, analyze, tests, debug/release build.

- [ ] Add a smoke widget test that imports `UsmanNotepadApp` before app implementation exists.
- [ ] Run CI test job and confirm the expected missing-symbol failure.
- [ ] Add minimal app bootstrap/providers/router.
- [ ] Run codegen/analyze/test until green.
- [ ] Commit foundation.

### Task 2: Edit history and FTS query escaping

**Files:** create `features/notes/application/edit_history.dart`, `features/search/domain/fts_query.dart` and focused tests.

**Produces:** `BoundedEditHistory<T>` and `FtsQuery.fromUserInput(String)`.

- [ ] Test undo/redo, branch-after-undo, coalescing boundary, entry count and byte-budget eviction.
- [ ] Verify RED.
- [ ] Implement bounded history.
- [ ] Test punctuation, quotes, whitespace, Urdu/Arabic Unicode and empty search.
- [ ] Verify RED.
- [ ] Implement escaped prefix-term FTS query.
- [ ] Run tests green and commit.

### Task 3: Drift schema + native-v1 migration

**Files:** create core database table/schema/migration/path files plus `test/core/database/app_database_migration_test.dart`.

**Produces:** `AppDatabase`, schemaVersion 2, idempotent FTS/index setup, v1 upgrade.

- [ ] Build a native-v1 SQLite fixture matching existing `NotepadDb.java`.
- [ ] Insert English + mixed Urdu content and legacy pin/favorite/deleted values.
- [ ] Open with v2 migration and verify fixture test fails before migration implementation.
- [ ] Implement in-place column/table/index/FTS migration.
- [ ] Verify exact note text survives and FTS returns it.
- [ ] Verify foreign keys and cascade behavior for new Phase-1 tables.
- [ ] Run migration tests green and commit.

### Task 4: Repository contracts and Drift implementations

**Files:** create notes/search/settings domain contracts and data adapters; add repository tests.

**Produces:** note CRUD/watch/save/delete/restore/permanent-delete/pin/favorite/checklist/task/search operations.

- [ ] Write failing CRUD/persistence tests.
- [ ] Implement create/load/watch/save.
- [ ] Write failing soft-delete/restore/permanent-delete tests.
- [ ] Implement transactional Trash behavior.
- [ ] Write failing pin/favorite tests and implement.
- [ ] Write failing checklist create/toggle/reorder/body-mirror tests and implement transactionally.
- [ ] Write failing title/body/checklist FTS tests and implement ranked snippets.
- [ ] Run all repository tests green and commit.

### Task 5: Durable autosave controller

**Files:** create `note_editor_controller.dart`, `note_editor_state.dart`; add fake-clock/repository controller tests.

**Produces:** immediate local edits, debounced durable draft, serialized canonical saves, lifecycle flush and save status.

- [ ] Write failing immediate-state test.
- [ ] Implement synchronous editor mutations.
- [ ] Write failing debounce test.
- [ ] Implement timers.
- [ ] Write failing out-of-order-write test.
- [ ] Implement serialized save generation.
- [ ] Write failing flush-on-close test and failed-save-draft-retention test.
- [ ] Implement flush and draft clearing rules.
- [ ] Run tests green and commit.

### Task 6: Settings/theme controller and Soft Canvas system

**Files:** create settings repository/controller, theme tokens/theme, tests.

**Produces:** persisted System/Light/Dark and persisted List/Grid view mode.

- [ ] Write failing settings persistence tests.
- [ ] Implement SharedPreferences repository.
- [ ] Create token tests for expected semantic contrast assignments where practical.
- [ ] Implement warm light and layered-charcoal dark Material 3 themes.
- [ ] Run tests green and commit.

### Task 7: Reusable note UI components

**Files:** create premium note card, quick capture, search surface, snackbar, empty state, checklist row; add widget tests.

**Produces:** reusable Soft Canvas primitives with semantics and large touch targets.

- [ ] Write widget tests for card title/body metadata semantics.
- [ ] Implement card.
- [ ] Test Quick Capture exposes only Text and Checklist actions.
- [ ] Implement quick capture.
- [ ] Test checklist checked/unchecked semantics and strike styling.
- [ ] Implement checklist row.
- [ ] Run widget tests green and commit.

### Task 8: Home + Notes + routing shell

**Files:** create router, shell, Home, Notes, notes-list controller; widget tests.

**Produces:** Home/Notes primary flows and one-tap note creation.

- [ ] Test bottom navigation routes.
- [ ] Test quick Text creates and opens editor route.
- [ ] Test quick Checklist creates and opens editor route.
- [ ] Test All/Pinned/Favorites filters.
- [ ] Implement shell/screens/controllers.
- [ ] Verify responsive list/grid layouts and empty state.
- [ ] Run widget tests green and commit.

### Task 9: Editor

**Files:** create editor screen/checklist editor and tests.

**Produces:** optional title, body/checklist editing, Back/Pin/More, undo/redo, subtle save state, favorite/delete actions.

- [ ] Test new note autofocus.
- [ ] Test visible top controls are only Back/Pin/More.
- [ ] Test typing updates controller without awaiting persistence.
- [ ] Test undo/redo UI.
- [ ] Test checklist add/toggle/reorder.
- [ ] Test delete soft-deletes and returns with Undo snackbar signal.
- [ ] Implement editor and lifecycle flush.
- [ ] Run widget/controller tests green and commit.

### Task 10: Search + Tasks + Trash + Settings

**Files:** create the four screens/controllers and widget tests.

**Produces:** functional remaining V1 destinations with no fake later-phase controls.

- [ ] Search test: input -> FTS result -> open editor.
- [ ] Tasks test: incomplete checklist row -> toggle -> source note.
- [ ] Trash test: restore and permanent-delete confirmation.
- [ ] Settings test: theme/view persistence and Trash navigation.
- [ ] Implement screens.
- [ ] Run widget tests green and commit.

### Task 11: V1 acceptance integration test

**Files:** create `integration_test/v1_core_flow_test.dart`.

**Produces:** automated create -> edit -> reopen-state -> search -> pin/favorite -> checklist -> delete -> undo/restore journey.

- [ ] Write acceptance journey against an in-memory/temporary database.
- [ ] Run and fix only behavior defects exposed by the test.
- [ ] Add mixed Urdu/English editor/search case.
- [ ] Add 1,000-note search/list smoke dataset.
- [ ] Run green and commit.

### Task 12: Release verification and documentation closure

**Files:** update `ROADMAP.md`, `README.md` only with observed verification.

**Produces:** evidence-based Phase 0/1 status.

- [ ] Run generated-code freshness check.
- [ ] Run formatter.
- [ ] Run static analysis.
- [ ] Run unit/widget/integration tests.
- [ ] Build debug APK.
- [ ] Build release APK.
- [ ] Scan TODO/FIXME and unused code warnings.
- [ ] Verify migration test output.
- [ ] Record exact pass/fail results in roadmap; never claim unrun checks.
- [ ] Open a PR from `phase0-1-flutter-core` to `main` only after branch verification is green.
