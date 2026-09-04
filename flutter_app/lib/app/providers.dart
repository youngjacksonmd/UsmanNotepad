import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:usman_notepad/core/database/app_database.dart';
import 'package:usman_notepad/features/notes/data/drift_note_repository.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';
import 'package:usman_notepad/features/notes/domain/note_repository.dart';
import 'package:usman_notepad/features/search/data/drift_search_repository.dart';
import 'package:usman_notepad/features/search/domain/search_repository.dart';
import 'package:usman_notepad/features/settings/data/preferences_settings_repository.dart';
import 'package:usman_notepad/features/settings/domain/app_settings.dart';
import 'package:usman_notepad/features/settings/domain/settings_repository.dart';

final databaseProvider = Provider<AppDatabase>((ref) {
  final database = AppDatabase();
  ref.onDispose(() => unawaited(database.close()));
  return database;
});

final noteRepositoryProvider = Provider<NoteRepository>((ref) {
  return DriftNoteRepository(ref.watch(databaseProvider));
});

final searchRepositoryProvider = Provider<SearchRepository>((ref) {
  return DriftSearchRepository(ref.watch(databaseProvider));
});

final settingsRepositoryProvider = Provider<SettingsRepository>((ref) {
  return PreferencesSettingsRepository();
});

final notesProvider = StreamProvider.family<List<Note>, NoteListFilter>((ref, filter) {
  return ref.watch(noteRepositoryProvider).watch(filter: filter);
});

final tasksProvider = FutureProvider<List<ChecklistEntry>>((ref) {
  return ref.watch(noteRepositoryProvider).listIncompleteChecklistItems();
});

final appSettingsProvider = AsyncNotifierProvider<AppSettingsController, AppSettings>(
  AppSettingsController.new,
);

class AppSettingsController extends AsyncNotifier<AppSettings> {
  @override
  Future<AppSettings> build() {
    return ref.watch(settingsRepositoryProvider).load();
  }

  Future<void> setTheme(AppThemePreference theme) async {
    final current = state.value ?? const AppSettings();
    final next = current.copyWith(theme: theme);
    state = AsyncData(next);
    await ref.read(settingsRepositoryProvider).save(next);
  }

  Future<void> setNoteView(NoteViewMode view) async {
    final current = state.value ?? const AppSettings();
    final next = current.copyWith(noteView: view);
    state = AsyncData(next);
    await ref.read(settingsRepositoryProvider).save(next);
  }
}
