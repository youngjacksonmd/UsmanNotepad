import 'package:shared_preferences/shared_preferences.dart';
import 'package:usman_notepad/features/settings/domain/app_settings.dart';
import 'package:usman_notepad/features/settings/domain/settings_repository.dart';

final class PreferencesSettingsRepository implements SettingsRepository {
  static const _themeKey = 'appearance.theme';
  static const _viewKey = 'notes.view';

  @override
  Future<AppSettings> load() async {
    final preferences = await SharedPreferences.getInstance();
    return AppSettings(
      theme: _parseTheme(preferences.getString(_themeKey)),
      noteView: preferences.getString(_viewKey) == NoteViewMode.list.name
          ? NoteViewMode.list
          : NoteViewMode.grid,
    );
  }

  @override
  Future<void> save(AppSettings settings) async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString(_themeKey, settings.theme.name);
    await preferences.setString(_viewKey, settings.noteView.name);
  }

  AppThemePreference _parseTheme(String? value) {
    return switch (value) {
      'light' => AppThemePreference.light,
      'dark' => AppThemePreference.dark,
      _ => AppThemePreference.system,
    };
  }
}
