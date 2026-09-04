enum AppThemePreference { system, light, dark }

enum NoteViewMode { grid, list }

class AppSettings {
  const AppSettings({
    this.theme = AppThemePreference.system,
    this.noteView = NoteViewMode.grid,
  });

  final AppThemePreference theme;
  final NoteViewMode noteView;

  AppSettings copyWith({
    AppThemePreference? theme,
    NoteViewMode? noteView,
  }) {
    return AppSettings(
      theme: theme ?? this.theme,
      noteView: noteView ?? this.noteView,
    );
  }
}
