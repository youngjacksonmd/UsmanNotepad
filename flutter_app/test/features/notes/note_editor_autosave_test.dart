import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:usman_notepad/features/notes/application/note_editor_autosave.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';

void main() {
  test('typing updates memory immediately, then draft, then canonical save', () async {
    final drafts = <EditorSnapshot>[];
    final saves = <EditorSnapshot>[];
    var clears = 0;
    final autosave = NoteEditorAutosave(
      initial: const EditorSnapshot(
        noteId: 7,
        title: '',
        body: '',
        type: NoteType.text,
      ),
      draftDebounce: const Duration(milliseconds: 15),
      saveDebounce: const Duration(milliseconds: 35),
      writeDraft: (snapshot) async => drafts.add(snapshot),
      saveCanonical: (snapshot) async => saves.add(snapshot),
      clearDraft: () async => clears++,
    );

    autosave.update(autosave.current.copyWith(body: 'Client ki payment ابھی pending ہے.'));
    expect(autosave.current.body, 'Client ki payment ابھی pending ہے.');
    expect(autosave.status, LocalSaveStatus.saving);
    expect(drafts, isEmpty);
    expect(saves, isEmpty);

    await Future<void>.delayed(const Duration(milliseconds: 25));
    expect(drafts.single.body, 'Client ki payment ابھی pending ہے.');
    expect(saves, isEmpty);

    await autosave.flush();
    expect(saves.single.body, 'Client ki payment ابھی pending ہے.');
    expect(clears, 1);
    expect(autosave.status, LocalSaveStatus.saved);
    autosave.dispose();
  });

  test('canonical writes are serialized and the newest edit wins', () async {
    final completed = <String>[];
    final firstStarted = Completer<void>();
    final releaseFirst = Completer<void>();
    final autosave = NoteEditorAutosave(
      initial: const EditorSnapshot(noteId: 1, title: '', body: '', type: NoteType.text),
      draftDebounce: const Duration(milliseconds: 1),
      saveDebounce: const Duration(milliseconds: 5),
      writeDraft: (_) async {},
      saveCanonical: (snapshot) async {
        if (snapshot.body == 'first') {
          firstStarted.complete();
          await releaseFirst.future;
        }
        completed.add(snapshot.body);
      },
      clearDraft: () async {},
    );

    autosave.update(autosave.current.copyWith(body: 'first'));
    await firstStarted.future;
    autosave.update(autosave.current.copyWith(body: 'second'));
    await Future<void>.delayed(const Duration(milliseconds: 10));
    expect(completed, isEmpty);

    releaseFirst.complete();
    await autosave.flush();
    expect(completed, <String>['first', 'second']);
    expect(autosave.current.body, 'second');
    expect(autosave.status, LocalSaveStatus.saved);
    autosave.dispose();
  });

  test('failed canonical save keeps durable draft and exposes retry state', () async {
    var clears = 0;
    var draftWrites = 0;
    final autosave = NoteEditorAutosave(
      initial: const EditorSnapshot(noteId: 9, title: '', body: '', type: NoteType.text),
      draftDebounce: const Duration(milliseconds: 1),
      saveDebounce: const Duration(milliseconds: 5),
      writeDraft: (_) async => draftWrites++,
      saveCanonical: (_) async => throw StateError('disk full'),
      clearDraft: () async => clears++,
    );

    autosave.update(autosave.current.copyWith(body: 'never lose this'));
    await autosave.flush();

    expect(draftWrites, greaterThan(0));
    expect(clears, 0);
    expect(autosave.status, LocalSaveStatus.failed);
    expect(autosave.current.body, 'never lose this');
    autosave.dispose();
  });
}
