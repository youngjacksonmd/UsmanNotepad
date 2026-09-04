import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:usman_notepad/features/settings/data/preferences_settings_repository.dart';
import 'package:usman_notepad/features/settings/domain/app_settings.dart';

void main() {
  setUp(() => SharedPreferences.setMockInitialValues(<String, Object>{}));

  test('theme and note view preferences persist', () async {
    final repository = PreferencesSettingsRepository();
    expect((await repository.load()).theme, AppThemePreference.system);

    await repository.save(
      const AppSettings(
        theme: AppThemePreference.dark,
        noteView: NoteViewMode.list,
      ),
    );

    final reloaded = await PreferencesSettingsRepository().load();
    expect(reloaded.theme, AppThemePreference.dark);
    expect(reloaded.noteView, NoteViewMode.list);
  });
}
