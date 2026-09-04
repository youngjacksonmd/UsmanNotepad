import 'package:usman_notepad/features/notes/domain/note.dart';

abstract interface class NoteRepository {
  Future<int> create({required NoteType type});
  Future<Note?> find(int id);
  Future<List<Note>> list({NoteListFilter filter = NoteListFilter.active});
  Stream<List<Note>> watch({NoteListFilter filter = NoteListFilter.active});
  Future<void> save(NoteEdit edit);
  Future<void> setPinned(int id, bool value);
  Future<void> setFavorite(int id, bool value);
  Future<void> softDelete(int id);
  Future<void> restore(int id);
  Future<void> permanentlyDelete(int id);
  Future<List<ChecklistEntry>> checklist(int noteId);
  Future<int> addChecklistItem(int noteId, String text);
  Future<void> updateChecklistText(int itemId, String text);
  Future<void> setChecklistChecked(int itemId, bool value);
  Future<void> deleteChecklistItem(int itemId);
  Future<void> reorderChecklist(int noteId, List<int> orderedIds);
  Future<List<ChecklistEntry>> listIncompleteChecklistItems();
}
