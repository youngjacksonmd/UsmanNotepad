import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/core/widgets/empty_state.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';

class TrashScreen extends ConsumerWidget {
  const TrashScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notes = ref.watch(notesProvider(NoteListFilter.trash));
    return Scaffold(
      appBar: AppBar(
        leading: const BackButton(),
        title: const Text('Trash'),
        backgroundColor: Colors.transparent,
      ),
      body: SafeArea(
        top: false,
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 900),
            child: notes.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (error, stack) => const CalmEmptyState(
                icon: Icons.error_outline_rounded,
                title: 'Trash could not be loaded',
                message: 'No note has been permanently removed by this error.',
              ),
              data: (items) {
                if (items.isEmpty) {
                  return const Center(
                    child: CalmEmptyState(
                      icon: Icons.delete_sweep_outlined,
                      title: 'Trash is empty',
                      message: 'Deleted notes stay here until you permanently remove them.',
                    ),
                  );
                }
                return ListView.separated(
                  padding: const EdgeInsetsDirectional.fromSTEB(AppSpacing.lg, AppSpacing.md, AppSpacing.lg, AppSpacing.hero),
                  itemCount: items.length,
                  separatorBuilder: (_, __) => const SizedBox(height: AppSpacing.xs),
                  itemBuilder: (context, index) {
                    final note = items[index];
                    return Card(
                      child: ListTile(
                        contentPadding: const EdgeInsetsDirectional.all(AppSpacing.md),
                        title: Text(note.title.trim().isEmpty ? 'Untitled' : note.title, maxLines: 2, overflow: TextOverflow.ellipsis),
                        subtitle: note.body.trim().isEmpty ? null : Text(note.body, maxLines: 2, overflow: TextOverflow.ellipsis),
                        trailing: PopupMenuButton<String>(
                          onSelected: (action) async {
                            if (action == 'restore') {
                              await ref.read(noteRepositoryProvider).restore(note.id);
                            } else if (action == 'delete') {
                              await _confirmPermanentDelete(context, ref, note.id);
                            }
                          },
                          itemBuilder: (context) => const <PopupMenuEntry<String>>[
                            PopupMenuItem(value: 'restore', child: ListTile(leading: Icon(Icons.restore_rounded), title: Text('Restore'))),
                            PopupMenuItem(value: 'delete', child: ListTile(leading: Icon(Icons.delete_forever_outlined), title: Text('Delete permanently'))),
                          ],
                        ),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _confirmPermanentDelete(BuildContext context, WidgetRef ref, int id) async {
    final confirmed = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Delete permanently?'),
            content: const Text('This note cannot be restored after permanent deletion.'),
            actions: <Widget>[
              TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
              FilledButton(onPressed: () => Navigator.pop(context, true), child: const Text('Delete permanently')),
            ],
          ),
        ) ??
        false;
    if (confirmed) await ref.read(noteRepositoryProvider).permanentlyDelete(id);
  }
}
