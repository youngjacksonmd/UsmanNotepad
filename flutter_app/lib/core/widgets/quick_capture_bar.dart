import 'package:flutter/material.dart';
import 'package:usman_notepad/core/theme/tokens.dart';

class QuickCaptureBar extends StatelessWidget {
  const QuickCaptureBar({
    required this.onText,
    required this.onChecklist,
    super.key,
  });

  final VoidCallback onText;
  final VoidCallback onChecklist;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Theme.of(context).colorScheme.surface,
      elevation: 0.8,
      borderRadius: BorderRadius.circular(AppRadius.card),
      child: Padding(
        padding: const EdgeInsetsDirectional.fromSTEB(
          AppSpacing.md,
          AppSpacing.sm,
          AppSpacing.xs,
          AppSpacing.sm,
        ),
        child: Row(
          children: <Widget>[
            Expanded(
              child: InkWell(
                onTap: onText,
                borderRadius: BorderRadius.circular(AppRadius.input),
                child: const SizedBox(
                  height: 48,
                  child: Align(
                    alignment: AlignmentDirectional.centerStart,
                    child: Text('Take a note…'),
                  ),
                ),
              ),
            ),
            TextButton.icon(
              onPressed: onText,
              icon: const Icon(Icons.text_fields_rounded, size: 20),
              label: const Text('Text'),
            ),
            TextButton.icon(
              onPressed: onChecklist,
              icon: const Icon(Icons.check_box_outlined, size: 20),
              label: const Text('Checklist'),
            ),
          ],
        ),
      ),
    );
  }
}
