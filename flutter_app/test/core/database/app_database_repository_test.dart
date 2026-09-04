import 'dart:io';

import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:usman_notepad/core/database/app_database.dart';
import 'package:usman_notepad/features/notes/data/drift_note_repository.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';
import 'package:usman_notepad/features/search/data/drift_search_repository.dart';

void main() {
  late AppDatabase database;
  late DriftNoteRepository notes;
  late DriftSearchRepository search;

  setUp(() {
    database = AppDatabase.forTesting(NativeDatabase.memory());
    notes = DriftNoteRepository(database);
    search = DriftSearchRepository(database);
  });

  tearDown(() => database.close());

  test('create edit pin favorite delete restore and permanent delete persist', () async {
    final id = await notes.create(type: NoteType.text);
    await notes.save(
      NoteEdit(
        id: id,
        title: 'Claim follow-up',
        body: 'Client ki payment ابھی pending ہے.',
        type: NoteType.text,
      ),
    );

    await notes.setPinned(id, true);
    await notes.setFavorite(id, true);
    final note = await notes.find(id);
    expect(note, isNotNull);
    expect(note!.title, 'Claim follow-up');
    expect(note.body, 'Client ki payment ابھی pending ہے.');
    expect(note.isPinned, isTrue);
    expect(note.isFavorite, isTrue);

    await notes.softDelete(id);
    expect(await notes.list(filter: NoteListFilter.active), isEmpty);
    expect((await notes.list(filter: NoteListFilter.trash)).single.id, id);

    await notes.restore(id);
    expect((await notes.list(filter: NoteListFilter.active)).single.id, id);

    await notes.softDelete(id);
    await notes.permanentlyDelete(id);
    expect(await notes.find(id), isNull);
  });

  test('FTS searches title body and mixed Unicode without loading all notes', () async {
    final first = await notes.create(type: NoteType.text);
    await notes.save(
      NoteEdit(
        id: first,
        title: 'Invoice review',
        body: 'Client ki payment ابھی pending ہے.',
        type: NoteType.text,
      ),
    );
    final second = await notes.create(type: NoteType.text);
    await notes.save(
      NoteEdit(
        id: second,
        title: 'Shopping',
        body: 'Milk and bread',
        type: NoteType.text,
      ),
    );

    final english = await search.search('invoice');
    expect(english.map((result) => result.noteId), contains(first));

    final urdu = await search.search('ابھی');
    expect(urdu.map((result) => result.noteId), contains(first));
    expect(urdu.map((result) => result.noteId), isNot(contains(second)));
  });

  test('checklist rows persist, reorder, mirror into search, and power tasks', () async {
    final noteId = await notes.create(type: NoteType.checklist);
    final milk = await notes.addChecklistItem(noteId, 'Buy milk');
    final claim = await notes.addChecklistItem(noteId, 'Submit insurance claim');

    await notes.setChecklistChecked(milk, true);
    await notes.reorderChecklist(noteId, <int>[claim, milk]);

    final rows = await notes.checklist(noteId);
    expect(rows.map((row) => row.id).toList(), <int>[claim, milk]);
    expect(rows.last.isChecked, isTrue);

    final tasks = await notes.listIncompleteChecklistItems();
    expect(tasks.map((row) => row.id), contains(claim));
    expect(tasks.map((row) => row.id), isNot(contains(milk)));

    final results = await search.search('insurance');
    expect(results.map((result) => result.noteId), contains(noteId));
  });

  test('native v1 database upgrades in place without changing note text', () async {
    final directory = await Directory.systemTemp.createTemp('usman_notepad_v1_');
    final file = File('${directory.path}/usman_notepad_v2.db');
    final legacy = NativeDatabase(file);
    await legacy.runCustom('''
      CREATE TABLE notes (
        id INTEGER PRIMARY KEY,
        title TEXT NOT NULL DEFAULT '',
        body TEXT NOT NULL DEFAULT '',
        mode TEXT NOT NULL DEFAULT 'text',
        created_at INTEGER NOT NULL,
        updated_at INTEGER NOT NULL,
        folder_id INTEGER,
        is_pinned INTEGER NOT NULL DEFAULT 0,
        is_favorite INTEGER NOT NULL DEFAULT 0,
        is_archived INTEGER NOT NULL DEFAULT 0,
        is_deleted INTEGER NOT NULL DEFAULT 0,
        deleted_at INTEGER NOT NULL DEFAULT 0,
        is_locked INTEGER NOT NULL DEFAULT 0,
        theme_key TEXT NOT NULL DEFAULT 'system',
        unlock_at INTEGER NOT NULL DEFAULT 0,
        expires_at INTEGER NOT NULL DEFAULT 0,
        is_inbox INTEGER NOT NULL DEFAULT 0,
        is_quick_copy INTEGER NOT NULL DEFAULT 0,
        is_scratch INTEGER NOT NULL DEFAULT 0,
        is_daily INTEGER NOT NULL DEFAULT 0
      )
    ''');
    await legacy.runCustom('''
      INSERT INTO notes(
        id,title,body,mode,created_at,updated_at,is_pinned,is_favorite
      ) VALUES(
        42,'Legacy note','Client ki payment ابھی pending ہے.','text',100,200,1,1
      )
    ''');
    await legacy.runCustom('PRAGMA user_version = 1');
    await legacy.close();

    final migrated = AppDatabase.forTesting(NativeDatabase(file));
    final repository = DriftNoteRepository(migrated);
    final note = await repository.find(42);

    expect(note, isNotNull);
    expect(note!.title, 'Legacy note');
    expect(note.body, 'Client ki payment ابھی pending ہے.');
    expect(note.isPinned, isTrue);
    expect(note.isFavorite, isTrue);
    expect((await DriftSearchRepository(migrated).search('pending')).single.noteId, 42);

    await migrated.close();
    await directory.delete(recursive: true);
  });
}
