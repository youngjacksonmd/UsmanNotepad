import 'package:drift/drift.dart';
import 'package:usman_notepad/core/database/app_database.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';
import 'package:usman_notepad/features/notes/domain/note_repository.dart';
import 'package:uuid/uuid.dart';

final class DriftNoteRepository implements NoteRepository {
  DriftNoteRepository(this._db, {Uuid uuid = const Uuid()}) : _uuid = uuid;

  final AppDatabase _db;
  final Uuid _uuid;

  @override
  Future<int> create({required NoteType type}) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    return _db.into(_db.notes).insert(
          NotesCompanion.insert(
            syncId: Value(_uuid.v4()),
            mode: Value(type.value),
            createdAt: now,
            updatedAt: now,
          ),
        );
  }

  @override
  Future<Note?> find(int id) async {
    final query = _db.select(_db.notes)..where((row) => row.id.equals(id));
    final row = await query.getSingleOrNull();
    return row == null ? null : _mapNote(row);
  }

  @override
  Future<List<Note>> list({NoteListFilter filter = NoteListFilter.active}) async {
    final query = _baseListQuery(filter);
    final rows = await query.get();
    return rows.map(_mapNote).toList(growable: false);
  }

  @override
  Stream<List<Note>> watch({NoteListFilter filter = NoteListFilter.active}) {
    final query = _baseListQuery(filter);
    return query.watch().map((rows) => rows.map(_mapNote).toList(growable: false));
  }

  SimpleSelectStatement<$NotesTable, NoteRow> _baseListQuery(NoteListFilter filter) {
    final query = _db.select(_db.notes);
    switch (filter) {
      case NoteListFilter.active:
        query.where((row) => row.isDeleted.equals(false) & row.isArchived.equals(false));
      case NoteListFilter.trash:
        query.where((row) => row.isDeleted.equals(true));
      case NoteListFilter.pinned:
        query.where(
          (row) => row.isDeleted.equals(false) & row.isArchived.equals(false) & row.isPinned.equals(true),
        );
      case NoteListFilter.favorites:
        query.where(
          (row) => row.isDeleted.equals(false) & row.isArchived.equals(false) & row.isFavorite.equals(true),
        );
    }
    query.orderBy(<OrderingTerm Function($NotesTable)>[
      (row) => OrderingTerm(expression: row.isPinned, mode: OrderingMode.desc),
      (row) => OrderingTerm(expression: row.updatedAt, mode: OrderingMode.desc),
    ]);
    return query;
  }

  @override
  Future<void> save(NoteEdit edit) async {
    final body = edit.type == NoteType.checklist ? await _checklistBody(edit.id) : edit.body;
    final now = DateTime.now().millisecondsSinceEpoch;
    final words = body.trim().isEmpty ? 0 : body.trim().split(RegExp(r'\s+', unicode: true)).length;
    final updated = await (_db.update(_db.notes)..where((row) => row.id.equals(edit.id))).write(
      NotesCompanion(
        title: Value(edit.title),
        body: Value(body),
        mode: Value(edit.type.value),
        updatedAt: Value(now),
        wordCount: Value(words),
        characterCount: Value(body.characters.length),
        readingTimeSeconds: Value(words == 0 ? 0 : ((words / 200) * 60).ceil()),
        revisionNumber: const Value.absent(),
      ),
    );
    if (updated == 0) throw StateError('Note ${edit.id} was not found.');
    await _db.customStatement(
      'UPDATE notes SET revision_number = revision_number + 1 WHERE id = ?',
      <Object?>[edit.id],
    );
  }

  @override
  Future<void> setPinned(int id, bool value) async {
    await (_db.update(_db.notes)..where((row) => row.id.equals(id))).write(
      NotesCompanion(
        isPinned: Value(value),
        updatedAt: Value(DateTime.now().millisecondsSinceEpoch),
      ),
    );
  }

  @override
  Future<void> setFavorite(int id, bool value) async {
    await (_db.update(_db.notes)..where((row) => row.id.equals(id))).write(
      NotesCompanion(
        isFavorite: Value(value),
        updatedAt: Value(DateTime.now().millisecondsSinceEpoch),
      ),
    );
  }

  @override
  Future<void> softDelete(int id) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    await (_db.update(_db.notes)..where((row) => row.id.equals(id))).write(
      NotesCompanion(isDeleted: const Value(true), deletedAt: Value(now), updatedAt: Value(now)),
    );
  }

  @override
  Future<void> restore(int id) async {
    await (_db.update(_db.notes)..where((row) => row.id.equals(id))).write(
      NotesCompanion(
        isDeleted: const Value(false),
        deletedAt: const Value(0),
        updatedAt: Value(DateTime.now().millisecondsSinceEpoch),
      ),
    );
  }

  @override
  Future<void> permanentlyDelete(int id) async {
    await _db.transaction(() async {
      await (_db.delete(_db.editorDrafts)..where((row) => row.noteId.equals(id))).go();
      await (_db.delete(_db.checklistItems)..where((row) => row.noteId.equals(id))).go();
      await (_db.delete(_db.notes)..where((row) => row.id.equals(id))).go();
    });
  }

  @override
  Future<List<ChecklistEntry>> checklist(int noteId) async {
    final query = _db.select(_db.checklistItems)
      ..where((row) => row.noteId.equals(noteId))
      ..orderBy(<OrderingTerm Function($ChecklistItemsTable)>[
        (row) => OrderingTerm.asc(row.sortOrder),
        (row) => OrderingTerm.asc(row.id),
      ]);
    return (await query.get()).map(_mapChecklist).toList(growable: false);
  }

  @override
  Future<int> addChecklistItem(int noteId, String text) async {
    return _db.transaction(() async {
      final maxOrder = _db.checklistItems.sortOrder.max();
      final maxQuery = _db.selectOnly(_db.checklistItems)
        ..addColumns(<Expression<Object>>[maxOrder])
        ..where(_db.checklistItems.noteId.equals(noteId));
      final current = (await maxQuery.getSingle()).read(maxOrder) ?? -1;
      final now = DateTime.now().millisecondsSinceEpoch;
      final id = await _db.into(_db.checklistItems).insert(
            ChecklistItemsCompanion.insert(
              syncId: Value(_uuid.v4()),
              noteId: noteId,
              itemText: Value(text),
              sortOrder: current + 1,
              createdAt: now,
              updatedAt: now,
            ),
          );
      await _mirrorChecklistBody(noteId);
      return id;
    });
  }

  @override
  Future<void> updateChecklistText(int itemId, String text) async {
    await _db.transaction(() async {
      final row = await (_db.select(_db.checklistItems)..where((item) => item.id.equals(itemId))).getSingle();
      await (_db.update(_db.checklistItems)..where((item) => item.id.equals(itemId))).write(
        ChecklistItemsCompanion(
          itemText: Value(text),
          updatedAt: Value(DateTime.now().millisecondsSinceEpoch),
        ),
      );
      await _mirrorChecklistBody(row.noteId);
    });
  }

  @override
  Future<void> setChecklistChecked(int itemId, bool value) async {
    await _db.transaction(() async {
      final row = await (_db.select(_db.checklistItems)..where((item) => item.id.equals(itemId))).getSingle();
      await (_db.update(_db.checklistItems)..where((item) => item.id.equals(itemId))).write(
        ChecklistItemsCompanion(
          isChecked: Value(value),
          updatedAt: Value(DateTime.now().millisecondsSinceEpoch),
        ),
      );
      await _touchNote(row.noteId);
    });
  }

  @override
  Future<void> deleteChecklistItem(int itemId) async {
    await _db.transaction(() async {
      final row = await (_db.select(_db.checklistItems)..where((item) => item.id.equals(itemId))).getSingleOrNull();
      if (row == null) return;
      await (_db.delete(_db.checklistItems)..where((item) => item.id.equals(itemId))).go();
      await _mirrorChecklistBody(row.noteId);
    });
  }

  @override
  Future<void> reorderChecklist(int noteId, List<int> orderedIds) async {
    await _db.transaction(() async {
      for (var index = 0; index < orderedIds.length; index++) {
        await (_db.update(_db.checklistItems)
              ..where((item) => item.noteId.equals(noteId) & item.id.equals(orderedIds[index])))
            .write(ChecklistItemsCompanion(sortOrder: Value(index)));
      }
      await _touchNote(noteId);
    });
  }

  @override
  Future<List<ChecklistEntry>> listIncompleteChecklistItems() async {
    final query = _db.select(_db.checklistItems).join(<Join<HasResultSet, dynamic>>[
      innerJoin(
        _db.notes,
        _db.notes.id.equalsExp(_db.checklistItems.noteId),
        useColumns: false,
      ),
    ])
      ..where(
        _db.checklistItems.isChecked.equals(false) &
            _db.notes.isDeleted.equals(false) &
            _db.notes.isArchived.equals(false),
      )
      ..orderBy(<OrderingTerm Function()>[
        () => OrderingTerm.desc(_db.checklistItems.updatedAt),
      ]);
    final rows = await query.get();
    return rows.map((row) => _mapChecklist(row.readTable(_db.checklistItems))).toList(growable: false);
  }

  Future<String> _checklistBody(int noteId) async {
    final rows = await checklist(noteId);
    return rows.map((row) => row.text).where((value) => value.trim().isNotEmpty).join('\n');
  }

  Future<void> _mirrorChecklistBody(int noteId) async {
    final body = await _checklistBody(noteId);
    final now = DateTime.now().millisecondsSinceEpoch;
    await (_db.update(_db.notes)..where((row) => row.id.equals(noteId))).write(
      NotesCompanion(body: Value(body), updatedAt: Value(now)),
    );
  }

  Future<void> _touchNote(int noteId) async {
    await (_db.update(_db.notes)..where((row) => row.id.equals(noteId))).write(
      NotesCompanion(updatedAt: Value(DateTime.now().millisecondsSinceEpoch)),
    );
  }

  Note _mapNote(NoteRow row) {
    return Note(
      id: row.id,
      title: row.title,
      body: row.body,
      type: NoteTypeStorage.parse(row.mode),
      createdAt: DateTime.fromMillisecondsSinceEpoch(row.createdAt),
      updatedAt: DateTime.fromMillisecondsSinceEpoch(row.updatedAt),
      isPinned: row.isPinned,
      isFavorite: row.isFavorite,
      isArchived: row.isArchived,
      isDeleted: row.isDeleted,
      revisionNumber: row.revisionNumber,
    );
  }

  ChecklistEntry _mapChecklist(ChecklistItemRow row) {
    return ChecklistEntry(
      id: row.id,
      noteId: row.noteId,
      text: row.itemText,
      isChecked: row.isChecked,
      sortOrder: row.sortOrder,
    );
  }
}
