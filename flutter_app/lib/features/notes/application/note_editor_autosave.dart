import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';

enum LocalSaveStatus { clean, saving, saved, failed }

class EditorSnapshot {
  const EditorSnapshot({
    required this.noteId,
    required this.title,
    required this.body,
    required this.type,
    this.checklistJson = '[]',
  });

  final int noteId;
  final String title;
  final String body;
  final NoteType type;
  final String checklistJson;

  EditorSnapshot copyWith({
    String? title,
    String? body,
    NoteType? type,
    String? checklistJson,
  }) {
    return EditorSnapshot(
      noteId: noteId,
      title: title ?? this.title,
      body: body ?? this.body,
      type: type ?? this.type,
      checklistJson: checklistJson ?? this.checklistJson,
    );
  }
}

typedef SnapshotWriter = Future<void> Function(EditorSnapshot snapshot);
typedef AsyncVoidCallback = Future<void> Function();

class NoteEditorAutosave {
  NoteEditorAutosave({
    required EditorSnapshot initial,
    required this.draftDebounce,
    required this.saveDebounce,
    required this.writeDraft,
    required this.saveCanonical,
    required this.clearDraft,
  }) : _current = initial;

  final Duration draftDebounce;
  final Duration saveDebounce;
  final SnapshotWriter writeDraft;
  final SnapshotWriter saveCanonical;
  final AsyncVoidCallback clearDraft;

  final ValueNotifier<LocalSaveStatus> statusListenable =
      ValueNotifier<LocalSaveStatus>(LocalSaveStatus.clean);

  EditorSnapshot _current;
  Timer? _draftTimer;
  Timer? _saveTimer;
  int _generation = 0;
  int _lastDraftedGeneration = -1;
  int _lastEnqueuedGeneration = -1;
  Future<void> _saveTail = Future<void>.value();
  bool _disposed = false;

  EditorSnapshot get current => _current;
  LocalSaveStatus get status => statusListenable.value;

  void update(EditorSnapshot snapshot) {
    if (_disposed) return;
    _current = snapshot;
    _generation++;
    _setStatus(LocalSaveStatus.saving);

    final generation = _generation;
    final captured = snapshot;
    _draftTimer?.cancel();
    _saveTimer?.cancel();
    _draftTimer = Timer(
      draftDebounce,
      () => unawaited(_writeDraft(generation, captured)),
    );
    _saveTimer = Timer(
      saveDebounce,
      () => unawaited(_enqueueCanonical(generation, captured)),
    );
  }

  Future<void> flush() async {
    if (_disposed) return;
    _draftTimer?.cancel();
    _saveTimer?.cancel();
    _draftTimer = null;
    _saveTimer = null;

    final generation = _generation;
    final captured = _current;
    await _writeDraft(generation, captured);
    await _enqueueCanonical(generation, captured);
    await _saveTail;
  }

  Future<void> _writeDraft(int generation, EditorSnapshot snapshot) async {
    if (_disposed || generation < _lastDraftedGeneration) return;
    try {
      await writeDraft(snapshot);
      if (generation > _lastDraftedGeneration) {
        _lastDraftedGeneration = generation;
      }
    } catch (_) {
      if (generation == _generation) {
        _setStatus(LocalSaveStatus.failed);
      }
    }
  }

  Future<void> _enqueueCanonical(
    int generation,
    EditorSnapshot snapshot,
  ) async {
    if (_disposed) return;
    if (generation <= _lastEnqueuedGeneration) {
      await _saveTail;
      return;
    }

    _lastEnqueuedGeneration = generation;
    _saveTail = _saveTail.then((_) async {
      try {
        await saveCanonical(snapshot);
        if (!_disposed && generation == _generation) {
          await clearDraft();
          if (!_disposed && generation == _generation) {
            _setStatus(LocalSaveStatus.saved);
          }
        }
      } catch (_) {
        if (!_disposed && generation == _generation) {
          _setStatus(LocalSaveStatus.failed);
        }
      }
    });
    await _saveTail;
  }

  void _setStatus(LocalSaveStatus value) {
    if (!_disposed && statusListenable.value != value) {
      statusListenable.value = value;
    }
  }

  void dispose() {
    if (_disposed) return;
    _disposed = true;
    _draftTimer?.cancel();
    _saveTimer?.cancel();
    statusListenable.dispose();
  }
}
