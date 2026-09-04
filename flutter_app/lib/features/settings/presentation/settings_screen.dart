import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/core/widgets/sukoon_mark.dart';
import 'package:usman_notepad/features/settings/domain/app_settings.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(appSettingsProvider).value ?? const AppSettings();
    return SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 760),
          child: ListView(
            padding: const EdgeInsetsDirectional.fromSTEB(AppSpacing.lg, AppSpacing.xl, AppSpacing.lg, AppSpacing.hero),
            children: <Widget>[
              Text('Settings', style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'Appearance'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: Column(
                  children: <Widget>[
                    _themeTile(ref, settings, AppThemePreference.system, 'System', Icons.brightness_auto_rounded),
                    _themeTile(ref, settings, AppThemePreference.light, 'Light', Icons.light_mode_outlined),
                    _themeTile(ref, settings, AppThemePreference.dark, 'Dark', Icons.dark_mode_outlined),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'Notes'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: Column(
                  children: <Widget>[
                    RadioListTile<NoteViewMode>(
                      value: NoteViewMode.grid,
                      groupValue: settings.noteView,
                      onChanged: (value) {
                        if (value != null) ref.read(appSettingsProvider.notifier).setNoteView(value);
                      },
                      title: const Text('Grid'),
                      secondary: const Icon(Icons.grid_view_rounded),
                    ),
                    RadioListTile<NoteViewMode>(
                      value: NoteViewMode.list,
                      groupValue: settings.noteView,
                      onChanged: (value) {
                        if (value != null) ref.read(appSettingsProvider.notifier).setNoteView(value);
                      },
                      title: const Text('List'),
                      secondary: const Icon(Icons.view_agenda_outlined),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'Data'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: ListTile(
                  minTileHeight: 64,
                  leading: const Icon(Icons.delete_outline_rounded),
                  title: const Text('Trash'),
                  subtitle: const Text('Restore notes or permanently delete them.'),
                  trailing: const Icon(Icons.chevron_right_rounded),
                  onTap: () => context.push('/trash'),
                ),
              ),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'Privacy'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Row(
                        children: <Widget>[
                          Icon(Icons.shield_outlined, color: Theme.of(context).colorScheme.primary),
                          const SizedBox(width: AppSpacing.sm),
                          Text('Local-first', style: Theme.of(context).textTheme.titleMedium),
                        ],
                      ),
                      const SizedBox(height: AppSpacing.sm),
                      Text(
                        'Core notes, search, checklists, and tasks stay on this device and do not require an account or internet connection.',
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                      const SizedBox(height: AppSpacing.xs),
                      Text(
                        'Phase 1 does not yet provide database-at-rest encryption. The app does not claim that local notes are cryptographically protected.',
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'About'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  child: Row(
                    children: <Widget>[
                      const SukoonMark(size: 52),
                      const SizedBox(width: AppSpacing.md),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Text('Sukoon Notes', style: Theme.of(context).textTheme.titleLarge),
                            const SizedBox(height: AppSpacing.xs),
                            Text(
                              'Created by Usman',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.primary),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _section(BuildContext context, String label) {
    return Text(label, style: Theme.of(context).textTheme.titleMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant));
  }

  Widget _themeTile(WidgetRef ref, AppSettings settings, AppThemePreference value, String label, IconData icon) {
    return RadioListTile<AppThemePreference>(
      value: value,
      groupValue: settings.theme,
      onChanged: (next) {
        if (next != null) ref.read(appSettingsProvider.notifier).setTheme(next);
      },
      title: Text(label),
      secondary: Icon(icon),
    );
  }
}
