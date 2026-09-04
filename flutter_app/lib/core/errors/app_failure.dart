sealed class AppFailure implements Exception {
  const AppFailure(this.message, [this.cause]);

  final String message;
  final Object? cause;

  @override
  String toString() => '$runtimeType: $message';
}

final class DatabaseFailure extends AppFailure {
  const DatabaseFailure(super.message, [super.cause]);
}

final class StorageFullFailure extends AppFailure {
  const StorageFullFailure(super.message, [super.cause]);
}

final class NoteNotFoundFailure extends AppFailure {
  const NoteNotFoundFailure(int id) : super('Note $id was not found.');
}

final class InvalidInputFailure extends AppFailure {
  const InvalidInputFailure(super.message);
}

final class MigrationFailure extends AppFailure {
  const MigrationFailure(super.message, [super.cause]);
}

final class UnexpectedFailure extends AppFailure {
  const UnexpectedFailure(super.message, [super.cause]);
}
