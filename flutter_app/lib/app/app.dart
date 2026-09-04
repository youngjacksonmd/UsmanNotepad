import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/app/router.dart';
import 'package:usman_notepad/core/theme/app_theme.dart';
import 'package:usman_notepad/features/settings/domain/app_settings.dart';

class UsmanNotepadApp extends ConsumerWidget {
  const UsmanNotepadApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(appSettingsProvider).value ?? const AppSettings();
    return MaterialApp.router(
      debugShowCheckedModeBanner: false,
      title: 'Sukoon Notes',
      scaffoldMessengerKey: rootScaffoldMessengerKey,
      routerConfig: appRouter,
      theme: AppTheme.light(),
      darkTheme: AppTheme.dark(),
      themeMode: switch (settings.theme) {
        AppThemePreference.system => ThemeMode.system,
        AppThemePreference.light => ThemeMode.light,
        AppThemePreference.dark => ThemeMode.dark,
      },
    );
  }
}
