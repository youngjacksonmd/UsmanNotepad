# UsmanNotepad Database

## Storage contract

Database: SQLite via Drift. Android path intentionally resolves to the existing application database `usman_notepad_v2.db`; other native platforms use an app-owned SQLite file. Flutter schema version begins at **2** because the current Android SQLite schema is version 1.

Timestamps are Unix milliseconds. Boolean values are SQLite integers. IDs are local integer primary keys for native compatibility; `sync_id` is a nullable UUID on migrated rows and non-null for new rows, allowing future sync without changing local IDs.

## Tables

### `notes`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `sync_id TEXT NULL UNIQUE`
- `title TEXT NOT NULL DEFAULT ''`
- `body TEXT NOT NULL DEFAULT ''`
- `mode TEXT NOT NULL DEFAULT 'text'` — `text|checklist` in Phase 1
- `content_format TEXT NOT NULL DEFAULT 'plain'`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`
- `last_opened_at INTEGER NULL`
- `folder_id INTEGER NULL`
- `is_pinned INTEGER NOT NULL DEFAULT 0`
- `pin_order INTEGER NULL`
- `is_favorite INTEGER NOT NULL DEFAULT 0`
- `is_archived INTEGER NOT NULL DEFAULT 0`
- `is_deleted INTEGER NOT NULL DEFAULT 0`
- `deleted_at INTEGER NOT NULL DEFAULT 0`
- `is_locked INTEGER NOT NULL DEFAULT 0`
- `color_key TEXT NULL`
- `background_key TEXT NULL`
- `word_count INTEGER NOT NULL DEFAULT 0`
- `character_count INTEGER NOT NULL DEFAULT 0`
- `reading_time_seconds INTEGER NOT NULL DEFAULT 0`
- `reminder_id INTEGER NULL`
- `metadata_json TEXT NOT NULL DEFAULT '{}'
- `sync_status TEXT NOT NULL DEFAULT 'local'`
- `revision_number INTEGER NOT NULL DEFAULT 0`
- legacy compatibility: `theme_key`, `unlock_at`, `expires_at`, `is_inbox`, `is_quick_copy`, `is_scratch`, `is_daily`

Indexes: `updated_at DESC`; `(is_deleted, updated_at DESC)`; `(is_archived, updated_at DESC)`; `(is_pinned, pin_order, updated_at DESC)`; `(is_favorite, updated_at DESC)`; `folder_id`; unique partial/normal `sync_id` where supported.

### `checklist_items`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `sync_id TEXT NULL UNIQUE`
- `note_id INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE`
- `text TEXT NOT NULL DEFAULT ''`
- `is_checked INTEGER NOT NULL DEFAULT 0`
- `sort_order INTEGER NOT NULL`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`

Indexes: `(note_id, sort_order)`; `(note_id, is_checked)`.

### `editor_drafts`

- `note_id INTEGER PRIMARY KEY REFERENCES notes(id) ON DELETE CASCADE`
- `title TEXT NOT NULL`
- `body TEXT NOT NULL`
- `mode TEXT NOT NULL`
- `checklist_json TEXT NOT NULL DEFAULT '[]'`
- `base_revision INTEGER NOT NULL`
- `saved_at INTEGER NOT NULL`

One durable safety snapshot per actively edited note. Cleared only after a canonical save succeeds.

### `folders`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `name TEXT NOT NULL`
- `parent_id INTEGER NULL` — repository-enforced no recursive descendants; FK added when legacy table can be safely rebuilt
- `color_key TEXT NULL`
- `icon_key TEXT NULL`
- `sort_order INTEGER NOT NULL DEFAULT 0`
- `is_favorite INTEGER NOT NULL DEFAULT 0`
- `is_archived INTEGER NOT NULL DEFAULT 0`
- `is_locked INTEGER NOT NULL DEFAULT 0`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`

Indexes: `parent_id`; `(is_archived, sort_order)`.

### `tags`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `name TEXT NOT NULL UNIQUE COLLATE NOCASE`
- `color_key TEXT NULL`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`

### `note_tags`

- `note_id INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE`
- `tag_id INTEGER NOT NULL REFERENCES tags(id) ON DELETE CASCADE`
- primary/unique pair `(note_id, tag_id)`

Indexes: `tag_id`, `note_id`.

### `attachments`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `note_id INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE`
- `type TEXT NOT NULL`
- `local_path TEXT NOT NULL`
- `display_name TEXT NULL`
- `mime_type TEXT NULL`
- `size_bytes INTEGER NOT NULL DEFAULT 0`
- `sha256 TEXT NULL`
- `metadata_json TEXT NOT NULL DEFAULT '{}'
- `created_at INTEGER NOT NULL`

Attachment bytes stay in app-owned files; the database stores metadata and paths only. Clear-cache operations must never remove source attachments.

### `tasks`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `sync_id TEXT NULL UNIQUE`
- `note_id INTEGER NULL REFERENCES notes(id) ON DELETE SET NULL`
- `checklist_item_id INTEGER NULL REFERENCES checklist_items(id) ON DELETE SET NULL`
- `title TEXT NOT NULL`
- `due_at INTEGER NULL`
- `priority INTEGER NOT NULL DEFAULT 0`
- `status TEXT NOT NULL DEFAULT 'open'`
- `repeat_rule TEXT NULL`
- `completed_at INTEGER NULL`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`

The table is future-ready. Phase 1 Tasks UI reads checklist items directly and does not expose due dates or repeat rules.

### `reminders`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `note_id INTEGER NULL REFERENCES notes(id) ON DELETE CASCADE`
- `task_id INTEGER NULL REFERENCES tasks(id) ON DELETE CASCADE`
- `trigger_at INTEGER NOT NULL`
- `repeat_rule TEXT NULL`
- `snoozed_until INTEGER NULL`
- `latitude REAL NULL`
- `longitude REAL NULL`
- `radius_m INTEGER NULL`
- `enabled INTEGER NOT NULL DEFAULT 1`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`

Index: `(enabled, trigger_at)`.

### `note_links`

- `source_note_id INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE`
- `target_note_id INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE`
- `link_text TEXT NOT NULL`
- unique `(source_note_id, target_note_id, link_text)`

### `note_versions`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `note_id INTEGER NOT NULL REFERENCES notes(id) ON DELETE CASCADE`
- `revision_number INTEGER NOT NULL`
- `title TEXT NOT NULL`
- `body TEXT NOT NULL`
- `mode TEXT NOT NULL`
- `checklist_json TEXT NOT NULL DEFAULT '[]'`
- `created_at INTEGER NOT NULL`
- unique `(note_id, revision_number)`

Full snapshot/coalescing UX is Phase 3; the table exists from the foundation.

### `templates`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `name TEXT NOT NULL UNIQUE`
- `title TEXT NOT NULL DEFAULT ''`
- `body TEXT NOT NULL`
- `mode TEXT NOT NULL DEFAULT 'text'`
- `is_built_in INTEGER NOT NULL DEFAULT 0`
- `is_favorite INTEGER NOT NULL DEFAULT 0`
- `created_at INTEGER NOT NULL`
- `updated_at INTEGER NOT NULL`

### `settings`

- `key TEXT PRIMARY KEY`
- `value_json TEXT NOT NULL`
- `updated_at INTEGER NOT NULL`

Non-secret settings only. Encryption keys, future auth tokens, PIN-derived material, and recovery secrets must never be stored here.

### `sync_queue`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `entity_type TEXT NOT NULL`
- `entity_sync_id TEXT NOT NULL`
- `operation TEXT NOT NULL`
- `payload_json TEXT NOT NULL`
- `attempt_count INTEGER NOT NULL DEFAULT 0`
- `next_attempt_at INTEGER NULL`
- `created_at INTEGER NOT NULL`

Indexes: `(next_attempt_at, id)`; `(entity_type, entity_sync_id)`.

### `sync_metadata`

- `entity_type TEXT NOT NULL`
- `entity_sync_id TEXT NOT NULL`
- `remote_revision TEXT NULL`
- `last_synced_at INTEGER NULL`
- `last_local_revision INTEGER NOT NULL DEFAULT 0`
- primary key `(entity_type, entity_sync_id)`

## Full-text search

`note_fts` is an FTS5 virtual table with external content from `notes`:

```sql
CREATE VIRTUAL TABLE note_fts USING fts5(
  title,
  body,
  content='notes',
  content_rowid='id',
  tokenize='unicode61 remove_diacritics 2'
);
```

Insert/update/delete triggers keep FTS synchronized. Migration finishes with `INSERT INTO note_fts(note_fts) VALUES('rebuild')`.

V1 query joins FTS rowid back to notes, excludes deleted/archived rows, orders by `bm25(note_fts)` then `updated_at`, and returns `snippet(...)` for matched context.

## Delete behavior

Normal delete is soft delete: `is_deleted=1`, `deleted_at=now`. Undo/restore clears both fields. Permanent deletion runs a transaction and relies on cascading relations where present. FTS is updated by triggers. Irreversible deletion requires an explicit confirmation in UI.

## Migration policy

- Every schema change increments `schemaVersion` and has an explicit migration.
- Migration runs in a transaction.
- Existing note rows are never dropped during v1 -> v2.
- Legacy tables are extended in place where user data may exist.
- New tables use `CREATE TABLE IF NOT EXISTS` during legacy upgrade.
- FTS/index creation is idempotent.
- Automated tests create a representative v1 database, upgrade it, verify original note text byte-for-byte, verify indexes/FTS, and exercise CRUD after migration.
- No release may require uninstall/reinstall.

## Future encryption

Phase 4 may replace the native query executor with an audited SQLCipher-backed executor. That migration must be separately tested with power-loss and rollback scenarios. No custom cryptography is permitted.
