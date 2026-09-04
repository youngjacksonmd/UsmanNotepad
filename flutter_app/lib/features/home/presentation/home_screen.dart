import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/core/widgets/empty_state.dart';
import 'package:usman_notepad/core/widgets/premium_note_card.dart';
import 'package:usman_notepad/core/widgets/quick_capture_bar.dart';
import 'package:usman_notepad/core/widgets/soft_search_bar.dart';
import 'package:usman_notepad/core/widgets/sukoon_mark.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notes = ref.watch(notesProvider(NoteListFilter.active));
    return SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1100),
          child: notes.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (error, stack) => const Center(
              child: CalmEmptyState(
                icon: Icons.cloud_off_rounded,
                title: 'Your notes are still local',
                message: 'Sukoon Notes could not open the local note list. Reopen the app to retry.',
              ),
            ),
            data: (items) {
              final pinned = items.where((note) => note.isPinned).take(6).toList();
              final recent = items.where((note) => !note.isPinned).take(10).toList();
              return ListView(
                padding: const EdgeInsetsDirectional.fromSTEB(
                  AppSpacing.lg,
                  AppSpacing.xl,
                  AppSpacing.lg,
                  AppSpacing.hero,
                ),
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      const SukoonMark(size: 42),
                      const SizedBox(width: AppSpacing.sm),
                      Text(
                        'Sukoon Notes',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                    ],
                  ),
                  const SizedBox(height: AppSpacing.lg),
                  Text(
                    _greeting(),
                    style: Theme.of(context).textTheme.labelLarge?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                        ),
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    'Your thoughts, beautifully organized.',
                    style: Theme.of(context).textTheme.headlineLarge,
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  SoftSearchBar(onTap: () => context.go('/search')),
                  const SizedBox(height: AppSpacing.md),
                  QuickCaptureBar(
                    onText: () => _create(context, ref, NoteType.text),
                    onChecklist: () => _create(context, ref, NoteType.checklist),
                  ),
                  const SizedBox(height: AppSpacing.xxl),
                  _SectionTitle(title: 'Pinned', count: pinned.length),
                  const SizedBox(height: AppSpacing.sm),
                  if (pinned.isEmpty)
                    _quietHint(context, 'Pin important notes and they will stay close.')
                  else
                    _CardWrap(notes: pinned),
                  const SizedBox(height: AppSpacing.xxl),
                  _SectionTitle(title: 'Recent', count: items.length),
                  const SizedBox(height: AppSpacing.sm),
                  if (items.isEmpty)
                    const CalmEmptyState(
                      icon: Icons.note_add_outlined,
                      title: 'A quiet place for your first thought',
                      message: 'Tap “Take a note…” and start writing. Everything saves locally as you type.',
                    )
                  else
                    _CardWrap(notes: recent.isEmpty ? pinned : recent),
                ],
              );
            },
          ),
        ),
      ),
    );
  }

  Future<void> _create(BuildContext context, WidgetRef ref, NoteType type) async {
    final id = await ref.read(noteRepositoryProvider).create(type: type);
    if (context.mounted) {
      context.push('/editor/$id?new=1');
    }
  }

  String _greeting() {
    final hour = DateTime.now().hour;
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  }

  Widget _quietHint(BuildContext context, String text) {
    return Padding(
      padding: const EdgeInsetsDirectional.symmetric(vertical: AppSpacing.sm),
      child: Text(
        text,
        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.count});
  final String title;
  final int count;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: <Widget>[
        Text(title, style: Theme.of(context).textTheme.titleLarge),
        const SizedBox(width: AppSpacing.xs),
        if (count > 0)
          Text(
            '$count',
            style: Theme.of(context).textTheme.labelMedium?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
          ),
      ],
    );
  }
}

class _CardWrap extends ConsumerWidget {
  const _CardWrap({required this.notes});
  final List<Note> notes;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final columns = constraints.maxWidth >= 900
            ? 3
            : constraints.maxWidth >= 560
                ? 2
                : 1;
        final width =
            (constraints.maxWidth - (columns - 1) * AppSpacing.sm) / columns;
        return Wrap(
          spacing: AppSpacing.sm,
          runSpacing: AppSpacing.sm,
          children: <Widget>[
            for (final note in notes)
              SizedBox(
                width: width,
                child: PremiumNoteCard(
                  note: note,
                  onTap: () => context.push('/editor/${note.id}'),
                ),
              ),
          ],
        );
      },
    );
  }
}
