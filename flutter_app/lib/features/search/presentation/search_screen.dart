import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/core/widgets/empty_state.dart';
import 'package:usman_notepad/core/widgets/soft_search_bar.dart';
import 'package:usman_notepad/features/search/domain/search_repository.dart';

class SearchScreen extends ConsumerStatefulWidget {
  const SearchScreen({super.key});

  @override
  ConsumerState<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends ConsumerState<SearchScreen> {
  final controller = TextEditingController();
  Timer? debounce;
  bool loading = false;
  List<SearchResult> results = const <SearchResult>[];

  @override
  void dispose() {
    debounce?.cancel();
    controller.dispose();
    super.dispose();
  }

  void _changed(String value) {
    debounce?.cancel();
    if (value.trim().isEmpty) {
      setState(() {
        loading = false;
        results = const <SearchResult>[];
      });
      return;
    }
    setState(() => loading = true);
    debounce = Timer(const Duration(milliseconds: 180), () async {
      final found = await ref.read(searchRepositoryProvider).search(value);
      if (mounted && controller.text == value) {
        setState(() {
          results = found;
          loading = false;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 900),
          child: Padding(
            padding: const EdgeInsetsDirectional.fromSTEB(AppSpacing.lg, AppSpacing.xl, AppSpacing.lg, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text('Search', style: Theme.of(context).textTheme.headlineLarge),
                const SizedBox(height: AppSpacing.md),
                SoftSearchBar(
                  onTap: null,
                  controller: controller,
                  onChanged: _changed,
                  autofocus: false,
                ),
                const SizedBox(height: AppSpacing.md),
                Expanded(
                  child: loading
                      ? const Center(child: CircularProgressIndicator())
                      : controller.text.trim().isEmpty
                          ? const Center(
                              child: CalmEmptyState(
                                icon: Icons.search_rounded,
                                title: 'Find a thought fast',
                                message: 'Search note titles, note text, and checklist items entirely on this device.',
                              ),
                            )
                          : results.isEmpty
                              ? const Center(
                                  child: CalmEmptyState(
                                    icon: Icons.manage_search_rounded,
                                    title: 'No matching notes',
                                    message: 'Try another word or a shorter phrase.',
                                  ),
                                )
                              : ListView.separated(
                                  padding: const EdgeInsets.only(bottom: AppSpacing.hero),
                                  itemCount: results.length,
                                  separatorBuilder: (_, __) => const SizedBox(height: AppSpacing.xs),
                                  itemBuilder: (context, index) => _SearchResultTile(
                                    result: results[index],
                                    onTap: () => context.push('/editor/${results[index].noteId}'),
                                  ),
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

class _SearchResultTile extends StatelessWidget {
  const _SearchResultTile({required this.result, required this.onTap});
  final SearchResult result;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Card(
      child: ListTile(
        contentPadding: const EdgeInsetsDirectional.all(AppSpacing.md),
        onTap: onTap,
        title: Text(result.title.trim().isEmpty ? 'Untitled' : result.title, style: Theme.of(context).textTheme.titleMedium),
        subtitle: result.snippet.trim().isEmpty
            ? null
            : Padding(
                padding: const EdgeInsets.only(top: AppSpacing.xs),
                child: Text(
                  result.snippet.replaceAll('[', '').replaceAll(']', ''),
                  maxLines: 3,
                  overflow: TextOverflow.ellipsis,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: scheme.onSurfaceVariant),
                ),
              ),
        trailing: const Icon(Icons.chevron_right_rounded),
      ),
    );
  }
}
