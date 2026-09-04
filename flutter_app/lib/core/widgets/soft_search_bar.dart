import 'package:flutter/material.dart';
import 'package:usman_notepad/core/theme/tokens.dart';

class SoftSearchBar extends StatelessWidget {
  const SoftSearchBar({
    required this.onTap,
    this.controller,
    this.onChanged,
    this.autofocus = false,
    super.key,
  });

  final VoidCallback? onTap;
  final TextEditingController? controller;
  final ValueChanged<String>? onChanged;
  final bool autofocus;

  @override
  Widget build(BuildContext context) {
    final interactive = controller != null;
    return Material(
      color: Theme.of(context).colorScheme.surface,
      elevation: 1,
      shadowColor: Colors.black.withValues(alpha: 0.08),
      borderRadius: BorderRadius.circular(AppRadius.floating),
      child: interactive
          ? TextField(
              controller: controller,
              autofocus: autofocus,
              onChanged: onChanged,
              textInputAction: TextInputAction.search,
              decoration: const InputDecoration(
                hintText: 'Search your notes',
                prefixIcon: Icon(Icons.search_rounded),
              ),
            )
          : InkWell(
              borderRadius: BorderRadius.circular(AppRadius.floating),
              onTap: onTap,
              child: const SizedBox(
                height: 56,
                child: Padding(
                  padding: EdgeInsetsDirectional.symmetric(horizontal: AppSpacing.md),
                  child: Row(
                    children: <Widget>[
                      Icon(Icons.search_rounded),
                      SizedBox(width: AppSpacing.sm),
                      Text('Search your notes'),
                    ],
                  ),
                ),
              ),
            ),
    );
  }
}
