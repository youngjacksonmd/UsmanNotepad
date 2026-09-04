import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
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
            padding: const EdgeInsetsDirectional.fromSTEB(
              AppSpacing.lg,
              AppSpacing.xl,
              AppSpacing.lg,
              AppSpacing.hero,
            ),
            children: <Widget>[
              Text('Settings', style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'Appearance'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: RadioGroup<AppThemePreference>(
                  groupValue: settings.theme,
                  onChanged: (next) {
                    if (next != null) {
                      ref.read(appSettingsProvider.notifier).setTheme(next);
                    }
                  },
                  child: Column(
                    children: const <Widget>[
                      RadioListTile<AppThemePreference>(
                        value: AppThemePreference.system,
                        title: Text('System'),
                        secondary: Icon(Icons.brightness_auto_rounded),
                      ),
                      RadioListTile<AppThemePreference>(
                        value: AppThemePreference.light,
                        title: Text('Light'),
                        secondary: Icon(Icons.light_mode_outlined),
                      ),
                      RadioListTile<AppThemePreference>(
                        value: AppThemePreference.dark,
                        title: Text('Dark'),
                        secondary: Icon(Icons.dark_mode_outlined),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.xxl),
              _section(context, 'Notes'),
              const SizedBox(height: AppSpacing.sm),
              Card(
                child: RadioGroup<NoteViewMode>(
                  groupValue: settings.noteView,
                  onChanged: (next) {
                    if (next != null) {
                      ref.read(appSettingsProvider.notifier).setNoteView(next);
                    }
                  },
                  child: const Column(
                    children: <Widget>[
                      RadioListTile<NoteViewMode>(
                        value: NoteViewMode.grid,
                        title: Text('Grid'),
                        secondary: Icon(Icons.grid_view_rounded),
                      ),
                      RadioListTile<NoteViewMode>(
                        value: NoteViewMode.list,
                        title: Text('List'),
                        secondary: Icon(Icons.view_agenda_outlined),
                      ),
                    ],
                  ),
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
                          Icon(
                            Icons.shield_outlined,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                          const SizedBox(width: AppSpacing.sm),
                          Text(
                            'Local-first',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
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
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: Theme.of(context).colorScheme.onSurfaceVariant,
                            ),
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
                      Container(
                        width: 52,
                        height: 52,
                        decoration: BoxDecoration(
                          color: Theme.of(context).colorScheme.primaryContainer,
                          borderRadius: BorderRadius.circular(AppRadius.input),
                        ),
                        child: Icon(
                          Icons.edit_note_rounded,
                          color: Theme.of(context).colorScheme.onPrimaryContainer,
                        ),
                      ),
                      const SizedBox(width: AppSpacing.md),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Text(
                              'UsmanNotepad',
                              style: Theme.of(context).textTheme.titleLarge,
                            ),
                            const SizedBox(height: AppSpacing.xs),
                            Text(
                              'Created by Usman',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    color: Theme.of(context).colorScheme.primary,
                                  ),
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
    return Text(
      label,
      style: Theme.of(context).textTheme.titleMedium?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
    );
  }
}
