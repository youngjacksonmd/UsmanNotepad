import 'package:flutter/material.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';

class PremiumNoteCard extends StatelessWidget {
  const PremiumNoteCard({
    required this.note,
    required this.onTap,
    super.key,
  });

  final Note note;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Semantics(
      button: true,
      label: note.title.isEmpty ? 'Untitled note' : note.title,
      child: Card(
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Expanded(
                      child: Text(
                        note.title.trim().isEmpty ? 'Untitled' : note.title.trim(),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                    ),
                    if (note.isPinned)
                      Icon(Icons.push_pin_rounded, size: 17, color: scheme.primary),
                  ],
                ),
                if (note.body.trim().isNotEmpty) ...<Widget>[
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    note.body.trim(),
                    maxLines: 7,
                    overflow: TextOverflow.ellipsis,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: scheme.onSurfaceVariant,
                        ),
                  ),
                ],
                const SizedBox(height: AppSpacing.sm),
                Row(
                  children: <Widget>[
                    if (note.type == NoteType.checklist)
                      Icon(Icons.checklist_rounded, size: 16, color: scheme.onSurfaceVariant),
                    if (note.isFavorite) ...<Widget>[
                      const SizedBox(width: AppSpacing.xs),
                      const Icon(Icons.star_rounded, size: 16, color: AppColors.favorite),
                    ],
                    const Spacer(),
                    Text(
                      _relative(note.updatedAt),
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(
                            color: scheme.onSurfaceVariant,
                          ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  String _relative(DateTime date) {
    final difference = DateTime.now().difference(date);
    if (difference.inMinutes < 1) return 'Now';
    if (difference.inHours < 1) return '${difference.inMinutes}m';
    if (difference.inDays < 1) return '${difference.inHours}h';
    if (difference.inDays < 7) return '${difference.inDays}d';
    return '${date.day}/${date.month}/${date.year}';
  }
}
