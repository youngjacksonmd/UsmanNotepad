import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:usman_notepad/core/database/tables.dart';

part 'app_database.g.dart';

@DriftDatabase(
  tables: <Type>[
    Notes,
    ChecklistItems,
    EditorDrafts,
    Folders,
    Tags,
    NoteTags,
    Attachments,
    Tasks,
    Reminders,
    NoteLinks,
    NoteVersions,
    Templates,
    Settings,
    SyncQueue,
    SyncMetadata,
  ],
)
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  AppDatabase.forTesting(super.executor);

  @override
  int get schemaVersion => 2;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (migrator) async {
          await migrator.createAll();
          await _createIndexes();
          await _createFts(rebuild: true);
        },
        onUpgrade: (migrator, from, to) async {
          if (from > to) {
            throw StateError('Database downgrade from $from to $to is not supported.');
          }
          if (from < 2) {
            await customStatement('PRAGMA foreign_keys = OFF');
            await transaction(() async {
              await _upgradeNativeV1ToV2(migrator);
              await _createIndexes();
              await _createFts(rebuild: true);
            });
            await customStatement('PRAGMA foreign_keys = ON');
          }
        },
        beforeOpen: (details) async {
          await customStatement('PRAGMA foreign_keys = ON');
          if (!Platform.environment.containsKey('FLUTTER_TEST')) {
            await customStatement('PRAGMA journal_mode = WAL');
            await customStatement('PRAGMA synchronous = NORMAL');
          }
        },
      );

  Future<void> _upgradeNativeV1ToV2(Migrator migrator) async {
    if (!await _tableExists('notes')) {
      await migrator.createTable(notes);
    } else {
      await _addColumnIfMissing('notes', 'sync_id', 'TEXT');
      await _addColumnIfMissing('notes', 'content_format', "TEXT NOT NULL DEFAULT 'plain'");
      await _addColumnIfMissing('notes', 'last_opened_at', 'INTEGER');
      await _addColumnIfMissing('notes', 'pin_order', 'INTEGER');
      await _addColumnIfMissing('notes', 'color_key', 'TEXT');
      await _addColumnIfMissing('notes', 'background_key', 'TEXT');
      await _addColumnIfMissing('notes', 'word_count', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('notes', 'character_count', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('notes', 'reading_time_seconds', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('notes', 'reminder_id', 'INTEGER');
      await _addColumnIfMissing('notes', 'metadata_json', "TEXT NOT NULL DEFAULT '{}'");
      await _addColumnIfMissing('notes', 'sync_status', "TEXT NOT NULL DEFAULT 'local'");
      await _addColumnIfMissing('notes', 'revision_number', 'INTEGER NOT NULL DEFAULT 0');
    }

    if (!await _tableExists('folders')) {
      await migrator.createTable(folders);
    } else {
      await _addColumnIfMissing('folders', 'color_key', 'TEXT');
      await _addColumnIfMissing('folders', 'icon_key', 'TEXT');
      await _addColumnIfMissing('folders', 'sort_order', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('folders', 'is_favorite', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('folders', 'is_archived', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('folders', 'is_locked', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('folders', 'updated_at', 'INTEGER NOT NULL DEFAULT 0');
    }

    if (!await _tableExists('tags')) {
      await migrator.createTable(tags);
    } else {
      await _addColumnIfMissing('tags', 'color_key', 'TEXT');
      await _addColumnIfMissing('tags', 'created_at', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('tags', 'updated_at', 'INTEGER NOT NULL DEFAULT 0');
    }

    if (!await _tableExists('note_tags')) {
      await migrator.createTable(noteTags);
    }

    if (!await _tableExists('attachments')) {
      await migrator.createTable(attachments);
    } else {
      await _addColumnIfMissing('attachments', 'display_name', 'TEXT');
      await _addColumnIfMissing('attachments', 'mime_type', 'TEXT');
      await _addColumnIfMissing('attachments', 'size_bytes', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('attachments', 'sha256', 'TEXT');
      await _addColumnIfMissing('attachments', 'metadata_json', "TEXT NOT NULL DEFAULT '{}'");
    }

    if (!await _tableExists('reminders')) {
      await migrator.createTable(reminders);
    } else {
      await _addColumnIfMissing('reminders', 'task_id', 'INTEGER');
      await _addColumnIfMissing('reminders', 'repeat_rule', 'TEXT');
      await _addColumnIfMissing('reminders', 'snoozed_until', 'INTEGER');
      await _addColumnIfMissing('reminders', 'created_at', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('reminders', 'updated_at', 'INTEGER NOT NULL DEFAULT 0');
    }

    if (!await _tableExists('note_links')) {
      await migrator.createTable(noteLinks);
    }

    if (!await _tableExists('templates')) {
      await migrator.createTable(templates);
    } else {
      await _addColumnIfMissing('templates', 'title', "TEXT NOT NULL DEFAULT ''");
      await _addColumnIfMissing('templates', 'is_built_in', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('templates', 'is_favorite', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('templates', 'created_at', 'INTEGER NOT NULL DEFAULT 0');
      await _addColumnIfMissing('templates', 'updated_at', 'INTEGER NOT NULL DEFAULT 0');
    }

    if (!await _tableExists('checklist_items')) {
      await migrator.createTable(checklistItems);
    }
    if (!await _tableExists('editor_drafts')) {
      await migrator.createTable(editorDrafts);
    }
    if (!await _tableExists('tasks')) {
      await migrator.createTable(tasks);
    }
    if (!await _tableExists('note_versions')) {
      await migrator.createTable(noteVersions);
    }
    if (!await _tableExists('settings')) {
      await migrator.createTable(settings);
    }
    if (!await _tableExists('sync_queue')) {
      await migrator.createTable(syncQueue);
    }
    if (!await _tableExists('sync_metadata')) {
      await migrator.createTable(syncMetadata);
    }
  }

  Future<bool> _tableExists(String table) async {
    final rows = await customSelect(
      "SELECT 1 FROM sqlite_master WHERE type='table' AND name = ? LIMIT 1",
      variables: <Variable<Object>>[Variable<String>(table)],
    ).get();
    return rows.isNotEmpty;
  }

  Future<bool> _columnExists(String table, String column) async {
    final rows = await customSelect('PRAGMA table_info($table)').get();
    return rows.any((row) => row.read<String>('name') == column);
  }

  Future<void> _addColumnIfMissing(String table, String column, String definition) async {
    if (!await _columnExists(table, column)) {
      await customStatement('ALTER TABLE $table ADD COLUMN $column $definition');
    }
  }

  Future<void> _createIndexes() async {
    const statements = <String>[
      'CREATE INDEX IF NOT EXISTS idx_notes_updated ON notes(updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_notes_deleted_updated ON notes(is_deleted, updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_notes_archived_updated ON notes(is_archived, updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_notes_pinned_order ON notes(is_pinned, pin_order, updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_notes_favorite_updated ON notes(is_favorite, updated_at DESC)',
      'CREATE INDEX IF NOT EXISTS idx_notes_folder ON notes(folder_id)',
      'CREATE UNIQUE INDEX IF NOT EXISTS idx_notes_sync_id ON notes(sync_id) WHERE sync_id IS NOT NULL',
      'CREATE INDEX IF NOT EXISTS idx_checklist_note_order ON checklist_items(note_id, sort_order)',
      'CREATE INDEX IF NOT EXISTS idx_checklist_note_checked ON checklist_items(note_id, is_checked)',
      'CREATE UNIQUE INDEX IF NOT EXISTS idx_checklist_sync_id ON checklist_items(sync_id) WHERE sync_id IS NOT NULL',
      'CREATE INDEX IF NOT EXISTS idx_folders_parent ON folders(parent_id)',
      'CREATE INDEX IF NOT EXISTS idx_folders_archived_order ON folders(is_archived, sort_order)',
      'CREATE INDEX IF NOT EXISTS idx_note_tags_tag ON note_tags(tag_id)',
      'CREATE INDEX IF NOT EXISTS idx_attachments_note ON attachments(note_id)',
      'CREATE INDEX IF NOT EXISTS idx_reminders_enabled_trigger ON reminders(enabled, trigger_at)',
      'CREATE UNIQUE INDEX IF NOT EXISTS idx_note_versions_revision ON note_versions(note_id, revision_number)',
      'CREATE INDEX IF NOT EXISTS idx_sync_queue_retry ON sync_queue(next_attempt_at, id)',
      'CREATE INDEX IF NOT EXISTS idx_sync_queue_entity ON sync_queue(entity_type, entity_sync_id)',
    ];
    for (final statement in statements) {
      await customStatement(statement);
    }
  }

  Future<void> _createFts({required bool rebuild}) async {
    await customStatement('''
      CREATE VIRTUAL TABLE IF NOT EXISTS note_fts USING fts5(
        title,
        body,
        content='notes',
        content_rowid='id',
        tokenize='unicode61 remove_diacritics 2'
      )
    ''');
    await customStatement('''
      CREATE TRIGGER IF NOT EXISTS notes_fts_insert AFTER INSERT ON notes BEGIN
        INSERT INTO note_fts(rowid, title, body) VALUES (new.id, new.title, new.body);
      END
    ''');
    await customStatement('''
      CREATE TRIGGER IF NOT EXISTS notes_fts_delete AFTER DELETE ON notes BEGIN
        INSERT INTO note_fts(note_fts, rowid, title, body)
        VALUES ('delete', old.id, old.title, old.body);
      END
    ''');
    await customStatement('''
      CREATE TRIGGER IF NOT EXISTS notes_fts_update AFTER UPDATE OF title, body ON notes BEGIN
        INSERT INTO note_fts(note_fts, rowid, title, body)
        VALUES ('delete', old.id, old.title, old.body);
        INSERT INTO note_fts(rowid, title, body) VALUES (new.id, new.title, new.body);
      END
    ''');
    if (rebuild) {
      await customStatement("INSERT INTO note_fts(note_fts) VALUES('rebuild')");
    }
  }

  static QueryExecutor _openConnection() {
    return LazyDatabase(() async {
      final documents = await getApplicationDocumentsDirectory();
      final File file;
      if (Platform.isAndroid) {
        final databasesDirectory = Directory(p.join(documents.parent.path, 'databases'));
        await databasesDirectory.create(recursive: true);
        file = File(p.join(databasesDirectory.path, 'usman_notepad_v2.db'));
      } else {
        file = File(p.join(documents.path, 'usman_notepad_v2.db'));
      }
      return NativeDatabase.createInBackground(file);
    });
  }
}
