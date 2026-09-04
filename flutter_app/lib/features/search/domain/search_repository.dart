class SearchResult {
  const SearchResult({
    required this.noteId,
    required this.title,
    required this.snippet,
  });

  final int noteId;
  final String title;
  final String snippet;
}

abstract interface class SearchRepository {
  Future<List<SearchResult>> search(String userInput, {int limit = 100});
}
