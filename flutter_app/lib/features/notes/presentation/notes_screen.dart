import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/core/widgets/empty_state.dart';
import 'package:usman_notepad/core/widgets/premium_note_card.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';
import 'package:usman_notepad/features/settings/domain/app_settings.dart';

class NotesScreen extends ConsumerStatefulWidget {
  const NotesScreen({super.key});

  @override
  ConsumerState<NotesScreen> createState() => _NotesScreenState();
}

class _NotesScreenState extends ConsumerState<NotesScreen> {
  NoteListFilter filter = NoteListFilter.active;

  @override
  Widget build(BuildContext context) {
    final notes = ref.watch(notesProvider(filter));
    final settings = ref.watch(appSettingsProvider).value ?? const AppSettings();
    return SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1200),
          child: Padding(
            padding: const EdgeInsetsDirectional.fromSTEB(AppSpacing.lg, AppSpacing.xl, AppSpacing.lg, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Expanded(child: Text('Notes', style: Theme.of(context).textTheme.headlineLarge)),
                    IconButton(
                      tooltip: settings.noteView == NoteViewMode.grid ? 'Use list view' : 'Use grid view',
                      onPressed: () => ref.read(appSettingsProvider.notifier).setNoteView(
                            settings.noteView == NoteViewMode.grid ? NoteViewMode.list : NoteViewMode.grid,
                          ),
                      icon: Icon(settings.noteView == NoteViewMode.grid ? Icons.view_agenda_outlined : Icons.grid_view_rounded),
                    ),
                    IconButton(
                      tooltip: 'Trash',
                      onPressed: () => context.push('/trash'),
                      icon: const Icon(Icons.delete_outline_rounded),
                    ),
                  ],
                ),
                const SizedBox(height: AppSpacing.md),
                Wrap(
                  spacing: AppSpacing.xs,
                  children: <Widget>[
                    _filterChip('All', NoteListFilter.active),
                    _filterChip('Pinned', NoteListFilter.pinned),
                    _filterChip('Favorites', NoteListFilter.favorites),
                  ],
                ),
                const SizedBox(height: AppSpacing.md),
                Expanded(
                  child: notes.when(
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (error, stack) => const CalmEmptyState(
                      icon: Icons.error_outline_rounded,
                      title: 'Notes could not be loaded',
                      message: 'Your local data has not been deleted. Reopen this screen to retry.',
                    ),
                    data: (items) {
                      if (items.isEmpty) {
                        return const Center(
                          child: CalmEmptyState(
                            icon: Icons.notes_rounded,
                            title: 'Nothing here yet',
                            message: 'Create a note from Home and it will appear here instantly.',
                          ),
                        );
                      }
                      if (settings.noteView == NoteViewMode.list) {
                        return ListView.separated(
                          padding: const EdgeInsets.only(bottom: AppSpacing.hero),
                          itemCount: items.length,
                          separatorBuilder: (_, __) => const SizedBox(height: AppSpacing.sm),
                          itemBuilder: (context, index) {
                            final note = items[index];
                            return PremiumNoteCard(note: note, onTap: () => context.push('/editor/${note.id}'));
                          },
                        );
                      }
                      return LayoutBuilder(
                        builder: (context, constraints) {
                          final count = constraints.maxWidth >= 1000 ? 4 : constraints.maxWidth >= 720 ? 3 : constraints.maxWidth >= 480 ? 2 : 1;
                          return MasonryGridView.count(
                            crossAxisCount: count,
                            mainAxisSpacing: AppSpacing.sm,
                            crossAxisSpacing: AppSpacing.sm,
                            padding: const EdgeInsets.only(bottom: AppSpacing.hero),
                            itemCount: items.length,
                            itemBuilder: (context, index) {
                              final note = items[index];
                              return PremiumNoteCard(note: note, onTap: () => context.push('/editor/${note.id}'));
                            },
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

  Widget _filterChip(String label, NoteListFilter value) {
    return ChoiceChip(
      label: Text(label),
      selected: filter == value,
      onSelected: (_) => setState(() => filter = value),
    );
  }
}
