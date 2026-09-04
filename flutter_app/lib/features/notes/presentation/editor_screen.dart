import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/app/router.dart';
import 'package:usman_notepad/core/theme/tokens.dart';
import 'package:usman_notepad/features/notes/application/edit_history.dart';
import 'package:usman_notepad/features/notes/application/note_editor_autosave.dart';
import 'package:usman_notepad/features/notes/domain/note.dart';
import 'package:usman_notepad/features/notes/domain/note_repository.dart';

class EditorScreen extends ConsumerStatefulWidget {
  const EditorScreen({
    required this.noteId,
    required this.autofocus,
    super.key,
  });

  final int noteId;
  final bool autofocus;

  @override
  ConsumerState<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends ConsumerState<EditorScreen>
    with WidgetsBindingObserver {
  final titleController = TextEditingController();
  final bodyController = TextEditingController();
  final bodyFocus = FocusNode();
  final Map<int, Timer> checklistTimers = <int, Timer>{};
  final Map<int, String> pendingChecklistText = <int, String>{};

  Note? note;
  List<ChecklistEntry> checklist = const <ChecklistEntry>[];
  NoteEditorAutosave? autosave;
  BoundedEditHistory<EditorSnapshot>? history;
  bool loading = true;
  bool applyingHistory = false;
  bool isPinned = false;
  bool isFavorite = false;

  NoteRepository get repository => ref.read(noteRepositoryProvider);

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    unawaited(_load());
  }

  Future<void> _load() async {
    final loaded = await repository.find(widget.noteId);
    if (loaded == null) {
      if (mounted) context.pop();
      return;
    }
    final draft = await repository.loadDraft(widget.noteId);
    final useDraft = draft != null && !draft.savedAt.isBefore(loaded.updatedAt);
    final initial = EditorSnapshot(
      noteId: loaded.id,
      title: useDraft ? draft.title : loaded.title,
      body: useDraft ? draft.body : loaded.body,
      type: loaded.type,
      checklistJson: useDraft ? draft.checklistJson : '[]',
    );

    titleController.text = initial.title;
    bodyController.text = initial.body;
    final initialChecklist = loaded.type == NoteType.checklist
        ? await repository.checklist(loaded.id)
        : const <ChecklistEntry>[];

    final saver = NoteEditorAutosave(
      initial: initial,
      draftDebounce: const Duration(milliseconds: 220),
      saveDebounce: const Duration(milliseconds: 550),
      writeDraft: (snapshot) => repository.saveDraft(
        NoteDraft(
          noteId: snapshot.noteId,
          title: snapshot.title,
          body: snapshot.body,
          type: snapshot.type,
          checklistJson: snapshot.checklistJson,
          baseRevision: loaded.revisionNumber,
          savedAt: DateTime.now(),
        ),
      ),
      saveCanonical: (snapshot) => repository.save(
        NoteEdit(
          id: snapshot.noteId,
          title: snapshot.title,
          body: snapshot.body,
          type: snapshot.type,
        ),
      ),
      clearDraft: () => repository.clearDraft(loaded.id),
    );

    final editHistory = BoundedEditHistory<EditorSnapshot>(
      initialValue: initial,
      estimateBytes: (snapshot) =>
          (snapshot.title.runes.length + snapshot.body.runes.length) * 2 + 64,
      maxEntries: 60,
      maxBytes: 2 * 1024 * 1024,
    );

    if (!mounted) {
      saver.dispose();
      return;
    }
    setState(() {
      note = loaded;
      checklist = initialChecklist;
      autosave = saver;
      history = editHistory;
      isPinned = loaded.isPinned;
      isFavorite = loaded.isFavorite;
      loading = false;
    });
    titleController.addListener(_textChanged);
    bodyController.addListener(_textChanged);
    if (widget.autofocus) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) bodyFocus.requestFocus();
      });
    }
  }

  void _textChanged() {
    if (applyingHistory || autosave == null || note == null) return;
    final snapshot = autosave!.current.copyWith(
      title: titleController.text,
      body: bodyController.text,
    );
    history?.record(snapshot);
    autosave!.update(snapshot);
    if (mounted) setState(() {});
  }

  void _applySnapshot(EditorSnapshot snapshot) {
    applyingHistory = true;
    titleController.value = titleController.value.copyWith(
      text: snapshot.title,
      selection: TextSelection.collapsed(offset: snapshot.title.length),
      composing: TextRange.empty,
    );
    bodyController.value = bodyController.value.copyWith(
      text: snapshot.body,
      selection: TextSelection.collapsed(offset: snapshot.body.length),
      composing: TextRange.empty,
    );
    applyingHistory = false;
    autosave?.update(snapshot);
    setState(() {});
  }

  Future<void> _flush() async {
    await _flushChecklistEdits();
    await autosave?.flush();
  }

  Future<void> _flushChecklistEdits() async {
    final pending = Map<int, String>.from(pendingChecklistText);
    for (final timer in checklistTimers.values) {
      timer.cancel();
    }
    checklistTimers.clear();
    pendingChecklistText.clear();
    for (final entry in pending.entries) {
      await repository.updateChecklistText(entry.key, entry.value);
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.inactive ||
        state == AppLifecycleState.paused ||
        state == AppLifecycleState.detached) {
      unawaited(_flush());
    }
  }

  Future<void> _close() async {
    await _flush();
    if (mounted) context.pop();
  }

  Future<void> _togglePin() async {
    final next = !isPinned;
    await repository.setPinned(widget.noteId, next);
    if (mounted) setState(() => isPinned = next);
  }

  Future<void> _toggleFavorite() async {
    final next = !isFavorite;
    await repository.setFavorite(widget.noteId, next);
    if (mounted) setState(() => isFavorite = next);
  }

  Future<void> _delete() async {
    await _flush();
    await repository.softDelete(widget.noteId);
    if (!mounted) return;
    context.pop();
    rootScaffoldMessengerKey.currentState?.showSnackBar(
      SnackBar(
        content: const Text('Note deleted'),
        action: SnackBarAction(
          label: 'Undo',
          onPressed: () => unawaited(repository.restore(widget.noteId)),
        ),
      ),
    );
  }

  Future<void> _addChecklistItem() async {
    final id = await repository.addChecklistItem(widget.noteId, '');
    final rows = await repository.checklist(widget.noteId);
    if (mounted) {
      setState(() => checklist = rows);
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) {
          final contextForRow = _rowKeys[id]?.currentContext;
          if (contextForRow != null) {
            Scrollable.ensureVisible(contextForRow, duration: AppMotion.standard);
          }
        }
      });
    }
  }

  final Map<int, GlobalKey> _rowKeys = <int, GlobalKey>{};

  void _scheduleChecklistText(int id, String text) {
    pendingChecklistText[id] = text;
    checklistTimers[id]?.cancel();
    checklistTimers[id] = Timer(const Duration(milliseconds: 350), () async {
      final value = pendingChecklistText.remove(id);
      checklistTimers.remove(id);
      if (value != null) await repository.updateChecklistText(id, value);
    });
  }

  Future<void> _toggleChecklist(ChecklistEntry item, bool value) async {
    await repository.setChecklistChecked(item.id, value);
    final rows = await repository.checklist(widget.noteId);
    ref.invalidate(tasksProvider);
    if (mounted) setState(() => checklist = rows);
  }

  Future<void> _removeChecklist(int id) async {
    checklistTimers.remove(id)?.cancel();
    pendingChecklistText.remove(id);
    await repository.deleteChecklistItem(id);
    final rows = await repository.checklist(widget.noteId);
    ref.invalidate(tasksProvider);
    if (mounted) setState(() => checklist = rows);
  }

  Future<void> _reorder(int oldIndex, int newIndex) async {
    if (newIndex > oldIndex) newIndex--;
    final next = List<ChecklistEntry>.from(checklist);
    final item = next.removeAt(oldIndex);
    next.insert(newIndex, item);
    setState(() => checklist = next);
    await repository.reorderChecklist(
      widget.noteId,
      next.map((entry) => entry.id).toList(growable: false),
    );
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    titleController.removeListener(_textChanged);
    bodyController.removeListener(_textChanged);
    for (final timer in checklistTimers.values) {
      timer.cancel();
    }
    autosave?.dispose();
    titleController.dispose();
    bodyController.dispose();
    bodyFocus.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    final currentNote = note;
    if (currentNote == null) return const SizedBox.shrink();
    final saver = autosave!;
    final editHistory = history!;
    final scheme = Theme.of(context).colorScheme;

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) unawaited(_close());
      },
      child: Scaffold(
        resizeToAvoidBottomInset: true,
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          leading: IconButton(
            tooltip: 'Back',
            onPressed: _close,
            icon: const Icon(Icons.arrow_back_rounded),
          ),
          actions: <Widget>[
            IconButton(
              tooltip: isPinned ? 'Unpin' : 'Pin',
              onPressed: _togglePin,
              icon: Icon(isPinned ? Icons.push_pin_rounded : Icons.push_pin_outlined),
            ),
            PopupMenuButton<String>(
              tooltip: 'More',
              icon: const Icon(Icons.more_vert_rounded),
              onSelected: (action) {
                if (action == 'favorite') unawaited(_toggleFavorite());
                if (action == 'delete') unawaited(_delete());
              },
              itemBuilder: (context) => <PopupMenuEntry<String>>[
                PopupMenuItem(
                  value: 'favorite',
                  child: ListTile(
                    leading: Icon(isFavorite ? Icons.star_rounded : Icons.star_outline_rounded),
                    title: Text(isFavorite ? 'Remove from favorites' : 'Add to favorites'),
                  ),
                ),
                const PopupMenuItem(
                  value: 'delete',
                  child: ListTile(
                    leading: Icon(Icons.delete_outline_rounded),
                    title: Text('Move to Trash'),
                  ),
                ),
              ],
            ),
          ],
        ),
        body: SafeArea(
          top: false,
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 880),
              child: Column(
                children: <Widget>[
                  Expanded(
                    child: SingleChildScrollView(
                      padding: const EdgeInsetsDirectional.fromSTEB(
                        AppSpacing.xl,
                        AppSpacing.md,
                        AppSpacing.xl,
                        AppSpacing.xxl,
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: <Widget>[
                          TextField(
                            controller: titleController,
                            minLines: 1,
                            maxLines: 4,
                            textCapitalization: TextCapitalization.sentences,
                            style: Theme.of(context).textTheme.headlineLarge,
                            decoration: const InputDecoration.collapsed(hintText: 'Title'),
                          ),
                          const SizedBox(height: AppSpacing.lg),
                          if (currentNote.type == NoteType.text)
                            TextField(
                              controller: bodyController,
                              focusNode: bodyFocus,
                              minLines: 14,
                              maxLines: null,
                              keyboardType: TextInputType.multiline,
                              textCapitalization: TextCapitalization.sentences,
                              style: Theme.of(context).textTheme.bodyLarge,
                              decoration: const InputDecoration.collapsed(
                                hintText: 'Start writing…',
                              ),
                            )
                          else
                            _ChecklistEditor(
                              entries: checklist,
                              rowKeys: _rowKeys,
                              onChanged: _scheduleChecklistText,
                              onToggle: _toggleChecklist,
                              onDelete: _removeChecklist,
                              onReorder: _reorder,
                            ),
                          if (currentNote.type == NoteType.checklist) ...<Widget>[
                            const SizedBox(height: AppSpacing.sm),
                            TextButton.icon(
                              onPressed: _addChecklistItem,
                              icon: const Icon(Icons.add_rounded),
                              label: const Text('Add item'),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),
                  Material(
                    color: scheme.surface.withValues(alpha: 0.98),
                    elevation: 1,
                    child: SafeArea(
                      top: false,
                      child: Padding(
                        padding: const EdgeInsetsDirectional.fromSTEB(
                          AppSpacing.sm,
                          AppSpacing.xs,
                          AppSpacing.md,
                          AppSpacing.xs,
                        ),
                        child: Row(
                          children: <Widget>[
                            IconButton(
                              tooltip: 'Undo',
                              onPressed: editHistory.canUndo
                                  ? () {
                                      final snapshot = editHistory.undo();
                                      if (snapshot != null) _applySnapshot(snapshot);
                                    }
                                  : null,
                              icon: const Icon(Icons.undo_rounded),
                            ),
                            IconButton(
                              tooltip: 'Redo',
                              onPressed: editHistory.canRedo
                                  ? () {
                                      final snapshot = editHistory.redo();
                                      if (snapshot != null) _applySnapshot(snapshot);
                                    }
                                  : null,
                              icon: const Icon(Icons.redo_rounded),
                            ),
                            const Spacer(),
                            ValueListenableBuilder<LocalSaveStatus>(
                              valueListenable: saver.statusListenable,
                              builder: (context, status, child) {
                                final label = switch (status) {
                                  LocalSaveStatus.clean => 'Local',
                                  LocalSaveStatus.saving => 'Saving…',
                                  LocalSaveStatus.saved => 'Saved locally',
                                  LocalSaveStatus.failed => 'Save failed · draft kept',
                                };
                                return Text(
                                  label,
                                  style: Theme.of(context).textTheme.labelMedium?.copyWith(
                                        color: status == LocalSaveStatus.failed
                                            ? scheme.error
                                            : scheme.onSurfaceVariant,
                                      ),
                                );
                              },
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _ChecklistEditor extends StatelessWidget {
  const _ChecklistEditor({
    required this.entries,
    required this.rowKeys,
    required this.onChanged,
    required this.onToggle,
    required this.onDelete,
    required this.onReorder,
  });

  final List<ChecklistEntry> entries;
  final Map<int, GlobalKey> rowKeys;
  final void Function(int id, String text) onChanged;
  final Future<void> Function(ChecklistEntry item, bool value) onToggle;
  final Future<void> Function(int id) onDelete;
  final Future<void> Function(int oldIndex, int newIndex) onReorder;

  @override
  Widget build(BuildContext context) {
    if (entries.isEmpty) {
      return Padding(
        padding: const EdgeInsetsDirectional.symmetric(vertical: AppSpacing.xl),
        child: Text(
          'Add your first checklist item.',
          style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
        ),
      );
    }
    return ReorderableListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      buildDefaultDragHandles: false,
      itemCount: entries.length,
      onReorder: onReorder,
      itemBuilder: (context, index) {
        final item = entries[index];
        final key = rowKeys.putIfAbsent(item.id, GlobalKey.new);
        return _ChecklistRow(
          key: ValueKey(item.id),
          anchorKey: key,
          item: item,
          index: index,
          onChanged: onChanged,
          onToggle: onToggle,
          onDelete: onDelete,
        );
      },
    );
  }
}

class _ChecklistRow extends StatelessWidget {
  const _ChecklistRow({
    required this.anchorKey,
    required this.item,
    required this.index,
    required this.onChanged,
    required this.onToggle,
    required this.onDelete,
    super.key,
  });

  final GlobalKey anchorKey;
  final ChecklistEntry item;
  final int index;
  final void Function(int id, String text) onChanged;
  final Future<void> Function(ChecklistEntry item, bool value) onToggle;
  final Future<void> Function(int id) onDelete;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      key: anchorKey,
      constraints: const BoxConstraints(minHeight: 52),
      child: Row(
        children: <Widget>[
          Checkbox(
            value: item.isChecked,
            onChanged: (value) {
              if (value != null) unawaited(onToggle(item, value));
            },
          ),
          Expanded(
            child: TextFormField(
              initialValue: item.text,
              onChanged: (value) => onChanged(item.id, value),
              maxLines: null,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    decoration: item.isChecked ? TextDecoration.lineThrough : null,
                    color: item.isChecked ? scheme.onSurfaceVariant : scheme.onSurface,
                  ),
              decoration: const InputDecoration.collapsed(hintText: 'List item'),
            ),
          ),
          IconButton(
            tooltip: 'Delete item',
            onPressed: () => unawaited(onDelete(item.id)),
            icon: const Icon(Icons.close_rounded, size: 20),
          ),
          ReorderableDragStartListener(
            index: index,
            child: const SizedBox(
              width: 48,
              height: 48,
              child: Icon(Icons.drag_handle_rounded),
            ),
          ),
        ],
      ),
    );
  }
}
