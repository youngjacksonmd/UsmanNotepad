# UsmanNotepad Roadmap

## Phase 0 — Foundation

Phase 0 must compile and be testable before Phase 1 is considered complete.

- [x] Preserve existing native Android implementation on `main`.
- [x] Create isolated Flutter implementation branch.
- [ ] Create Flutter project under `flutter_app/` with Android-first constraints.
- [ ] Lock package/application id to `com.usman.notepad` for upgrade continuity.
- [ ] Add Riverpod as the only application state-management system.
- [ ] Add `go_router` declarative routing.
- [ ] Create Presentation/Application/Domain/Data boundaries.
- [ ] Create reusable Soft Canvas design tokens and light/dark/system themes.
- [ ] Create Drift database schema version 2.
- [ ] Open existing Android `usman_notepad_v2.db` path instead of creating a disconnected store.
- [ ] Implement v1 native -> v2 Flutter migration without deleting legacy rows.
- [ ] Enable foreign keys/WAL where supported.
- [ ] Add normalized future-ready schema plus Phase-1 checklist/draft tables.
- [ ] Add FTS5 index + synchronization triggers.
- [ ] Add NoteRepository, SearchRepository and SettingsRepository contracts.
- [ ] Implement Drift repository adapters.
- [ ] Add typed domain failures.
- [ ] Add redacting production logger.
- [ ] Add database migration tests.
- [ ] Add repository tests.
- [ ] Add theme/settings tests.
- [ ] Add CI for format, code generation, analyze, test, debug/release APK.
- [ ] Verify clean compile in CI.

## Phase 1 — Excellent Core Notepad

### Create and edit

- [ ] Text note can be created in one tap with stable id.
- [ ] Checklist note can be created in one tap.
- [ ] Newly created note focuses writing immediately.
- [ ] Title is optional and multiline within sensible limits.
- [ ] Body supports Unicode and mixed English/Urdu/Arabic text.
- [ ] Editor remains visually page-like with no form-box styling.
- [ ] Editor top bar contains Back, Pin and contextual More only.

### Autosave and loss prevention

- [ ] Keystrokes update local state synchronously.
- [ ] Durable editor draft uses short debounce.
- [ ] Canonical note auto-save uses short debounce without blocking typing.
- [ ] Writes are serialized; stale completion cannot overwrite newer text.
- [ ] Save flushes before editor close/navigation.
- [ ] Save flushes on app inactive/paused/detached.
- [ ] Draft remains when canonical save fails.
- [ ] Save state is subtle: Saving / Saved locally / actionable failure.

### Undo/redo and checklist

- [ ] Text undo works.
- [ ] Text redo works.
- [ ] History is coalesced and memory-bounded.
- [ ] Checklist item create works.
- [ ] Complete/uncomplete works.
- [ ] Completed text uses subtle strike/fade styling.
- [ ] Reorder works and persists transactionally.
- [ ] Add/remove checklist item updates searchable body mirror.

### Notes/Home

- [ ] Home has greeting, prominent search, quick capture, Pinned and Recent.
- [ ] Quick capture exposes only working Text and Checklist actions.
- [ ] Note cards use Soft Canvas surfaces, restrained depth, balanced padding and strong typography.
- [ ] Notes screen supports All, Pinned and Favorites.
- [ ] Responsive note presentation is lazy and stable; grid/list setting persists.
- [ ] Pinned notes surface prominently without overwhelming Home.
- [ ] Empty states are calm and useful.

### Search

- [ ] Search title via FTS5.
- [ ] Search body via FTS5.
- [ ] Search checklist text via mirrored body.
- [ ] Search does not load entire notes table into memory.
- [ ] Search excludes Trash/Archive from normal results.
- [ ] Matched snippet is returned and highlighted softly.
- [ ] Empty query does not execute an expensive full FTS scan.
- [ ] Query escaping prevents malformed MATCH expressions.

### Pin/favorite

- [ ] Pin/unpin persists.
- [ ] Favorite/unfavorite persists.
- [ ] State changes use subtle visual/haptic feedback where platform permits.

### Delete / Trash

- [ ] Normal delete sets soft-delete state and shows `Note deleted — Undo`.
- [ ] Undo restores the exact note.
- [ ] Trash lists deleted notes.
- [ ] Restore works.
- [ ] Permanent delete requires explicit confirmation.
- [ ] Permanent delete cascades dependent Phase-1 rows and removes FTS entry.

### Tasks destination

- [ ] Tasks screen is not fake: it lists unchecked checklist rows from real notes.
- [ ] Tapping a task opens its source note.
- [ ] Completing/uncompleting from Tasks persists to the checklist note.
- [ ] No due-date/reminder UI appears before its phase.

### Basic settings

- [ ] System theme works.
- [ ] Light theme works.
- [ ] Dark theme uses layered charcoal rather than black inversion.
- [ ] Theme persists.
- [ ] List/grid note view setting persists.
- [ ] Trash entry is reachable.
- [ ] Privacy copy accurately states current local/un-encrypted boundary.

### Quality gates

- [ ] Formatter passes.
- [ ] Static analysis passes with no errors.
- [ ] Unit tests pass.
- [ ] Widget tests pass.
- [ ] Migration preserves representative native-v1 note text byte-for-byte.
- [ ] Repository CRUD/search/Trash tests pass.
- [ ] Debug APK builds.
- [ ] Release APK builds where CI environment permits.
- [ ] No Phase-2+ unfinished controls are visible.
- [ ] TODO/FIXME scan reviewed.
- [ ] Normal create -> edit -> leave -> reopen -> search -> pin/favorite -> checklist -> delete -> undo/restore flow verified.

## V1 screen map

```text
Brief native splash
  -> App gate boundary (no visible lock UI until Phase 4 is real)
  -> Shell
       Home
         -> Quick Text Note -> Editor
         -> Quick Checklist -> Editor
         -> Pinned/Recent Note -> Editor
       Notes
         -> All | Pinned | Favorites
         -> Note -> Editor
         -> Trash -> Trash screen -> Restore/Permanent delete
       Search
         -> FTS result -> Editor
       Tasks
         -> Real incomplete checklist rows -> Source Editor
       Settings
         -> Appearance (System/Light/Dark)
         -> Note view (Grid/List)
         -> Trash
         -> Privacy/About

Editor
  Back | Pin | More
  Title
  Body OR Checklist
  Undo | Redo | list controls
  More: Favorite | Delete
```

## Later phases — do not expose unfinished UI

- Phase 2: folders, nested folders, tags, filtering/sorting, archive, smart collections, bulk actions, improved search, note colors.
- Phase 3: Scratch Pad, Quick Copy, templates, reminders, Daily Notes, Note History, export, local backup, full crash-recovery UX.
- Phase 4: app PIN, biometrics, individual locks, private previews, screenshot protection, secure clipboard, encryption infrastructure.
- Phase 5: images, attachments, voice, speech-to-text, scanner, OCR.
- Phase 6: note links, backlinks, related notes, split/merge, graph, Time Capsule, Expiring Notes.
- Phase 7: optional encrypted sync, devices, queue/retry/conflict resolution/backup.
- Phase 8: optional AI.
- Phase 9: platform integrations.
