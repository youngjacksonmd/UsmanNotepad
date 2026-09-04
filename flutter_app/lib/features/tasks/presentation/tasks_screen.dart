import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/core/widgets/empty_state.dart';

class TasksScreen extends ConsumerWidget {
  const TasksScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tasks = ref.watch(tasksProvider);
    return SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 900),
          child: Padding(
            padding: const EdgeInsetsDirectional.fromSTEB(AppSpacing.lg, AppSpacing.xl, AppSpacing.lg, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text('Tasks', style: Theme.of(context).textTheme.headlineLarge),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  'Unchecked checklist items from your notes.',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
                ),
                const SizedBox(height: AppSpacing.xl),
                Expanded(
                  child: tasks.when(
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (error, stack) => const CalmEmptyState(
                      icon: Icons.error_outline_rounded,
                      title: 'Tasks could not be loaded',
                      message: 'Your checklist notes are still stored locally.',
                    ),
                    data: (items) {
                      if (items.isEmpty) {
                        return const Center(
                          child: CalmEmptyState(
                            icon: Icons.task_alt_rounded,
                            title: 'Nothing waiting on you',
                            message: 'Unchecked checklist items will appear here automatically.',
                          ),
                        );
                      }
                      return ListView.separated(
                        padding: const EdgeInsets.only(bottom: AppSpacing.hero),
                        itemCount: items.length,
                        separatorBuilder: (_, __) => const SizedBox(height: AppSpacing.xs),
                        itemBuilder: (context, index) {
                          final item = items[index];
                          return Card(
                            child: ListTile(
                              minTileHeight: 58,
                              leading: Checkbox(
                                value: false,
                                onChanged: (_) async {
                                  await ref.read(noteRepositoryProvider).setChecklistChecked(item.id, true);
                                  ref.invalidate(tasksProvider);
                                },
                              ),
                              title: Text(item.text),
                              onTap: () => context.push('/editor/${item.noteId}'),
                              trailing: const Icon(Icons.chevron_right_rounded),
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
