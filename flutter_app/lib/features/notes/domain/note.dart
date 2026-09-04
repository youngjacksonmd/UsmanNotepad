enum NoteType { text, checklist }

enum NoteListFilter { active, trash, pinned, favorites }

extension NoteTypeStorage on NoteType {
  String get value => name;

  static NoteType parse(String value) {
    return value == NoteType.checklist.name ? NoteType.checklist : NoteType.text;
  }
}

class Note {
  const Note({
    required this.id,
    required this.title,
    required this.body,
    required this.type,
    required this.createdAt,
    required this.updatedAt,
    required this.isPinned,
    required this.isFavorite,
    required this.isArchived,
    required this.isDeleted,
    required this.revisionNumber,
  });

  final int id;
  final String title;
  final String body;
  final NoteType type;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isPinned;
  final bool isFavorite;
  final bool isArchived;
  final bool isDeleted;
  final int revisionNumber;
}

class NoteEdit {
  const NoteEdit({
    required this.id,
    required this.title,
    required this.body,
    required this.type,
  });

  final int id;
  final String title;
  final String body;
  final NoteType type;
}

class ChecklistEntry {
  const ChecklistEntry({
    required this.id,
    required this.noteId,
    required this.text,
    required this.isChecked,
    required this.sortOrder,
  });

  final int id;
  final int noteId;
  final String text;
  final bool isChecked;
  final int sortOrder;
}
