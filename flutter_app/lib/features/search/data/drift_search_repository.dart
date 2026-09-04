import 'package:drift/drift.dart';
import 'package:usman_notepad/core/database/app_database.dart';
import 'package:usman_notepad/features/search/domain/fts_query.dart';
import 'package:usman_notepad/features/search/domain/search_repository.dart';

final class DriftSearchRepository implements SearchRepository {
  DriftSearchRepository(this._db);

  final AppDatabase _db;

  @override
  Future<List<SearchResult>> search(String userInput, {int limit = 100}) async {
    final query = FtsQuery.fromUserInput(userInput);
    if (query == null) return const <SearchResult>[];
    final safeLimit = limit.clamp(1, 200);
    final rows = await _db.customSelect(
      '''
      SELECT n.id AS note_id,
             n.title AS title,
             snippet(note_fts, 1, '[', ']', ' … ', 24) AS matched_snippet
      FROM note_fts
      JOIN notes n ON n.id = note_fts.rowid
      WHERE note_fts MATCH ?
        AND n.is_deleted = 0
        AND n.is_archived = 0
      ORDER BY bm25(note_fts), n.updated_at DESC
      LIMIT ?
      ''',
      variables: <Variable<Object>>[
        Variable<String>(query),
        Variable<int>(safeLimit),
      ],
      readsFrom: <ResultSetImplementation<Object, DataClass>>{_db.notes},
    ).get();
    return rows
        .map(
          (row) => SearchResult(
            noteId: row.read<int>('note_id'),
            title: row.read<String>('title'),
            snippet: row.read<String>('matched_snippet'),
          ),
        )
        .toList(growable: false);
  }
}
