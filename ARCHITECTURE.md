# UsmanNotepad Architecture

## 1. Non-negotiable order

Every implementation decision uses this order: never lose user data; privacy/security; fast writing; reliability; offline-first behavior; simple UX; search/organization; accessibility; performance; advanced features; AI; visual novelty.

## 2. Runtime architecture

UsmanNotepad uses a feature-oriented clean architecture without enterprise ceremony:

`Presentation -> Application -> Domain <- Data`

- **Presentation**: Flutter screens and reusable Soft Canvas widgets. No SQL or persistence code.
- **Application**: Riverpod controllers coordinate autosave, search, filters, settings, and navigation-facing state.
- **Domain**: immutable models, repository contracts, value objects, typed failures.
- **Data**: Drift database, migrations, FTS5 queries, SharedPreferences settings adapter, platform storage bridge.

Dependencies point inward. UI receives repository abstractions through Riverpod providers.

## 3. Flutter source map

```text
flutter_app/lib/
  main.dart
  app/
    app.dart
    router.dart
    providers.dart
  core/
    database/
      app_database.dart
      tables.dart
      migration.dart
      legacy_database_path.dart
    errors/app_failure.dart
    logging/app_logger.dart
    theme/
      app_theme.dart
      tokens.dart
    widgets/
      premium_note_card.dart
      quick_capture_bar.dart
      soft_search_bar.dart
      premium_snackbar.dart
      empty_state.dart
  features/
    home/presentation/home_screen.dart
    notes/
      domain/note.dart
      domain/note_repository.dart
      data/drift_note_repository.dart
      application/note_list_controller.dart
      application/note_editor_controller.dart
      application/edit_history.dart
      presentation/notes_screen.dart
      presentation/editor_screen.dart
      presentation/trash_screen.dart
      presentation/widgets/checklist_row.dart
    search/
      domain/search_repository.dart
      data/drift_search_repository.dart
      application/search_controller.dart
      presentation/search_screen.dart
    tasks/presentation/tasks_screen.dart
    settings/
      domain/app_settings.dart
      domain/settings_repository.dart
      data/preferences_settings_repository.dart
      application/settings_controller.dart
      presentation/settings_screen.dart
```

Files stay focused; generated Drift code is the only intentionally large generated artifact.

## 4. State management

Riverpod is the single state-management system.

- Repository providers are long-lived infrastructure providers.
- Notes/search/settings controllers are `Notifier`/`AsyncNotifier` style application state.
- Editor state is scoped by note id and keeps typing synchronous in memory.
- Screens never block text entry on a database write.

## 5. Routing

`go_router` owns declarative navigation.

Primary mobile destinations: Home, Notes, Search, Tasks, Settings. Editor and Trash are pushed routes outside the bottom-navigation shell. Tasks is real Phase-1 functionality: it aggregates unchecked checklist rows and can complete/uncomplete them; no due-date/reminder features are exposed yet.

On larger widths the same routes can be composed into 2-pane/3-pane layouts later without changing repository contracts.

## 6. Database approach

Drift owns SQLite. Android opens the existing native database path for `usman_notepad_v2.db`, keeping the same package id `com.usman.notepad`. Flutter schema version 2 upgrades native schema version 1 transactionally and never requires uninstall/reinstall.

All timestamps are integer Unix milliseconds to stay compatible with the native database. Existing columns are preserved. New normalized tables and indexes are added without deleting legacy note data.

Foreign keys are enabled for new tables. WAL is enabled on native platforms for responsive reads during saves.

## 7. Search architecture

SQLite FTS5 is the source of truth for V1 search.

- FTS indexes note title and body.
- Triggers keep FTS synchronized with notes.
- Checklist body text is mirrored into the note body in the same transaction so checklist content is searchable.
- Queries are escaped/tokenized before `MATCH`.
- Deleted and archived notes are excluded from normal search.
- Result snippets are produced in SQL; the UI does not load all notes into memory.

Semantic/AI search is a later optional layer and must never replace local FTS.

## 8. Autosave and data-loss prevention

Typing path:

`keystroke -> synchronous editor state -> durable draft debounce -> canonical note save debounce -> UI save state`

Rules:

1. UI state updates immediately.
2. Draft persistence uses a short debounce and stores the latest editor snapshot in `editor_drafts`.
3. Canonical note save is serialized per note; an older write cannot overwrite a newer revision.
4. Editor flushes on app inactive/paused/detached, route close, and explicit note-changing actions.
5. Canonical save and checklist rows update inside one transaction.
6. Only after canonical save succeeds is the durable draft cleared.
7. Save failure leaves the draft intact and shows an actionable non-blocking error.

Full user-facing crash-recovery/version-history UX remains Phase 3, but Phase 1 already keeps a durable safety copy because data-loss prevention outranks roadmap convenience.

## 9. Undo/redo

A bounded edit-history component records coalesced text/checklist snapshots. It caps history by count and byte budget so large notes cannot exhaust memory. Undo/redo modifies local editor state first and then follows normal autosave.

## 10. Error handling and logging

Domain failures are typed: database unavailable, storage full, invalid input, note missing, migration failure, and unexpected failure. User messages state the failed action and recovery path.

Production logs never include note title/body, checklist text, clipboard data, credentials, future encryption keys, or attachment contents.

## 11. Security boundary

Phase 0/1 is local-first but **not marketed as encrypted**. The current database remains compatible with the existing unencrypted native database. No home-made cryptography is introduced.

The data layer has a future executor boundary for audited SQLCipher-backed storage. Phase 4 will introduce app lock, biometric/PIN gates, individual-note locks, screenshot protection, secure clipboard controls, and key storage using OS-backed secure storage.

Until Phase 4 is complete, the UI must not imply that local notes are cryptographically protected at rest.

## 12. Backup and sync boundaries

Phase 0/1 performs no cloud calls.

Future backup reads a consistent database snapshot, validates integrity, writes atomically, and optionally encrypts with audited crypto. Future sync consumes an append-only `sync_queue`; local persistence is authoritative and the editor never waits for network acknowledgement.

## 13. Design architecture

Soft Canvas is centralized in theme tokens: warm near-white canvas, layered charcoal dark surfaces, restrained indigo accent, 4/8/12/16/20/24/32/40/48/64 spacing, restrained radii, subtle depth, 120–220 ms purposeful motion, and large touch targets.

No random screen-level styling constants. Note cards, search, quick capture, snackbars, empty states and checklist rows are reusable components.

## 14. Accessibility and international text

- Minimum practical touch targets are 48 logical pixels.
- Text respects system scaling.
- State is never communicated by color alone.
- Reduced-motion preference disables non-essential motion.
- Text fields use Unicode-safe Flutter editing and permit mixed English/Urdu/Arabic content.
- Directional layout primitives are used so later full Urdu localization can switch RTL safely.

## 15. Performance budgets

- No full database reload for a minor edit.
- Notes are observed with targeted Drift streams.
- Search uses FTS5.
- Grid/list rendering is lazy.
- Editor keeps network out of the write path.
- History is memory-bounded.
- Performance tests will cover 100/1,000/10,000 note datasets as the core stabilizes.
