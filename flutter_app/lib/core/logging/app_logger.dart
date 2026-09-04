import 'dart:developer' as developer;

abstract interface class AppLogger {
  void info(String event, {Map<String, Object?> fields = const {}});
  void warning(String event, {Object? error});
  void error(String event, {Object? error, StackTrace? stackTrace});
}

final class RedactingAppLogger implements AppLogger {
  const RedactingAppLogger();

  static const Set<String> _blockedKeys = <String>{
    'title',
    'body',
    'text',
    'content',
    'clipboard',
    'password',
    'pin',
    'token',
    'key',
  };

  @override
  void info(String event, {Map<String, Object?> fields = const {}}) {
    developer.log('$event ${_safe(fields)}', name: 'UsmanNotepad');
  }

  @override
  void warning(String event, {Object? error}) {
    developer.log(event, name: 'UsmanNotepad', error: error?.runtimeType);
  }

  @override
  void error(String event, {Object? error, StackTrace? stackTrace}) {
    developer.log(
      event,
      name: 'UsmanNotepad',
      error: error?.runtimeType,
      stackTrace: stackTrace,
    );
  }

  Map<String, Object?> _safe(Map<String, Object?> fields) {
    return <String, Object?>{
      for (final entry in fields.entries)
        if (!_blockedKeys.any(
          (key) => entry.key.toLowerCase().contains(key),
        ))
          entry.key: entry.value,
    };
  }
}
