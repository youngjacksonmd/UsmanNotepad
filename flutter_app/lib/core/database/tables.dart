import 'package:drift/drift.dart';

@DataClassName('NoteRow')
class Notes extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get syncId => text().nullable()();
  TextColumn get title => text().withDefault(const Constant(''))();
  TextColumn get body => text().withDefault(const Constant(''))();
  TextColumn get mode => text().withDefault(const Constant('text'))();
  TextColumn get contentFormat => text().withDefault(const Constant('plain'))();
  IntColumn get createdAt => integer()();
  IntColumn get updatedAt => integer()();
  IntColumn get lastOpenedAt => integer().nullable()();
  IntColumn get folderId => integer().nullable()();
  BoolColumn get isPinned => boolean().withDefault(const Constant(false))();
  IntColumn get pinOrder => integer().nullable()();
  BoolColumn get isFavorite => boolean().withDefault(const Constant(false))();
  BoolColumn get isArchived => boolean().withDefault(const Constant(false))();
  BoolColumn get isDeleted => boolean().withDefault(const Constant(false))();
  IntColumn get deletedAt => integer().withDefault(const Constant(0))();
  BoolColumn get isLocked => boolean().withDefault(const Constant(false))();
  TextColumn get colorKey => text().nullable()();
  TextColumn get backgroundKey => text().nullable()();
  IntColumn get wordCount => integer().withDefault(const Constant(0))();
  IntColumn get characterCount => integer().withDefault(const Constant(0))();
  IntColumn get readingTimeSeconds => integer().withDefault(const Constant(0))();
  IntColumn get reminderId => integer().nullable()();
  TextColumn get metadataJson => text().withDefault(const Constant('{}'))();
  TextColumn get syncStatus => text().withDefault(const Constant('local'))();
  IntColumn get revisionNumber => integer().withDefault(const Constant(0))();

  // Native-v1 compatibility columns. They remain until explicit later migrations.
  TextColumn get themeKey => text().withDefault(const Constant('system'))();
  IntColumn get unlockAt => integer().withDefault(const Constant(0))();
  IntColumn get expiresAt => integer().withDefault(const Constant(0))();
  BoolColumn get isInbox => boolean().withDefault(const Constant(false))();
  BoolColumn get isQuickCopy => boolean().withDefault(const Constant(false))();
  BoolColumn get isScratch => boolean().withDefault(const Constant(false))();
  BoolColumn get isDaily => boolean().withDefault(const Constant(false))();
}

@DataClassName('ChecklistItemRow')
class ChecklistItems extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get syncId => text().nullable()();
  IntColumn get noteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  TextColumn get text => text().withDefault(const Constant(''))();
  BoolColumn get isChecked => boolean().withDefault(const Constant(false))();
  IntColumn get sortOrder => integer()();
  IntColumn get createdAt => integer()();
  IntColumn get updatedAt => integer()();
}

@DataClassName('EditorDraftRow')
class EditorDrafts extends Table {
  IntColumn get noteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  TextColumn get title => text()();
  TextColumn get body => text()();
  TextColumn get mode => text()();
  TextColumn get checklistJson => text().withDefault(const Constant('[]'))();
  IntColumn get baseRevision => integer()();
  IntColumn get savedAt => integer()();

  @override
  Set<Column<Object>> get primaryKey => <Column<Object>>{noteId};
}

@DataClassName('FolderRow')
class Folders extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text()();
  IntColumn get parentId => integer().nullable()();
  TextColumn get colorKey => text().nullable()();
  TextColumn get iconKey => text().nullable()();
  IntColumn get sortOrder => integer().withDefault(const Constant(0))();
  BoolColumn get isFavorite => boolean().withDefault(const Constant(false))();
  BoolColumn get isArchived => boolean().withDefault(const Constant(false))();
  BoolColumn get isLocked => boolean().withDefault(const Constant(false))();
  IntColumn get createdAt => integer()();
  IntColumn get updatedAt => integer().withDefault(const Constant(0))();
}

@DataClassName('TagRow')
class Tags extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text().unique()();
  TextColumn get colorKey => text().nullable()();
  IntColumn get createdAt => integer().withDefault(const Constant(0))();
  IntColumn get updatedAt => integer().withDefault(const Constant(0))();
}

class NoteTags extends Table {
  IntColumn get noteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  IntColumn get tagId => integer().references(
    Tags,
    #id,
    onDelete: KeyAction.cascade,
  )();

  @override
  Set<Column<Object>> get primaryKey => <Column<Object>>{noteId, tagId};
}

@DataClassName('AttachmentRow')
class Attachments extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get noteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  TextColumn get type => text()();
  TextColumn get localPath => text()();
  TextColumn get displayName => text().nullable()();
  TextColumn get mimeType => text().nullable()();
  IntColumn get sizeBytes => integer().withDefault(const Constant(0))();
  TextColumn get sha256 => text().nullable()();
  TextColumn get metadataJson => text().withDefault(const Constant('{}'))();
  IntColumn get createdAt => integer()();
}

@DataClassName('TaskRow')
class Tasks extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get syncId => text().nullable()();
  IntColumn get noteId => integer().nullable().references(
    Notes,
    #id,
    onDelete: KeyAction.setNull,
  )();
  IntColumn get checklistItemId => integer().nullable().references(
    ChecklistItems,
    #id,
    onDelete: KeyAction.setNull,
  )();
  TextColumn get title => text()();
  IntColumn get dueAt => integer().nullable()();
  IntColumn get priority => integer().withDefault(const Constant(0))();
  TextColumn get status => text().withDefault(const Constant('open'))();
  TextColumn get repeatRule => text().nullable()();
  IntColumn get completedAt => integer().nullable()();
  IntColumn get createdAt => integer()();
  IntColumn get updatedAt => integer()();
}

@DataClassName('ReminderRow')
class Reminders extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get noteId => integer().nullable().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  IntColumn get taskId => integer().nullable().references(
    Tasks,
    #id,
    onDelete: KeyAction.cascade,
  )();
  IntColumn get triggerAt => integer().withDefault(const Constant(0))();
  TextColumn get repeatRule => text().nullable()();
  IntColumn get snoozedUntil => integer().nullable()();
  RealColumn get latitude => real().nullable()();
  RealColumn get longitude => real().nullable()();
  IntColumn get radiusM => integer().nullable()();
  BoolColumn get enabled => boolean().withDefault(const Constant(true))();
  IntColumn get createdAt => integer().withDefault(const Constant(0))();
  IntColumn get updatedAt => integer().withDefault(const Constant(0))();
}

class NoteLinks extends Table {
  IntColumn get sourceNoteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  IntColumn get targetNoteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  TextColumn get linkText => text()();

  @override
  Set<Column<Object>> get primaryKey => <Column<Object>>{
    sourceNoteId,
    targetNoteId,
    linkText,
  };
}

@DataClassName('NoteVersionRow')
class NoteVersions extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get noteId => integer().references(
    Notes,
    #id,
    onDelete: KeyAction.cascade,
  )();
  IntColumn get revisionNumber => integer()();
  TextColumn get title => text()();
  TextColumn get body => text()();
  TextColumn get mode => text()();
  TextColumn get checklistJson => text().withDefault(const Constant('[]'))();
  IntColumn get createdAt => integer()();
}

@DataClassName('TemplateRow')
class Templates extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text().unique()();
  TextColumn get title => text().withDefault(const Constant(''))();
  TextColumn get body => text()();
  TextColumn get mode => text().withDefault(const Constant('text'))();
  BoolColumn get isBuiltIn => boolean().withDefault(const Constant(false))();
  BoolColumn get isFavorite => boolean().withDefault(const Constant(false))();
  IntColumn get createdAt => integer().withDefault(const Constant(0))();
  IntColumn get updatedAt => integer().withDefault(const Constant(0))();
}

@DataClassName('SettingRow')
class Settings extends Table {
  TextColumn get key => text()();
  TextColumn get valueJson => text()();
  IntColumn get updatedAt => integer()();

  @override
  Set<Column<Object>> get primaryKey => <Column<Object>>{key};
}

@DataClassName('SyncQueueRow')
class SyncQueue extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get entityType => text()();
  TextColumn get entitySyncId => text()();
  TextColumn get operation => text()();
  TextColumn get payloadJson => text()();
  IntColumn get attemptCount => integer().withDefault(const Constant(0))();
  IntColumn get nextAttemptAt => integer().nullable()();
  IntColumn get createdAt => integer()();
}

@DataClassName('SyncMetadataRow')
class SyncMetadata extends Table {
  TextColumn get entityType => text()();
  TextColumn get entitySyncId => text()();
  TextColumn get remoteRevision => text().nullable()();
  IntColumn get lastSyncedAt => integer().nullable()();
  IntColumn get lastLocalRevision => integer().withDefault(const Constant(0))();

  @override
  Set<Column<Object>> get primaryKey => <Column<Object>>{
    entityType,
    entitySyncId,
  };
}
