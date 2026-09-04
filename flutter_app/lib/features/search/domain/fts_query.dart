abstract final class FtsQuery {
  static final RegExp _punctuation = RegExp(
    r'''["'`~!@#$%^&*()+={}\[\]|\\:;,.<>/?\-]+''',
    unicode: true,
  );

  static String? fromUserInput(String input) {
    final normalized = input.replaceAll(_punctuation, ' ').trim();
    if (normalized.isEmpty) return null;

    final terms = normalized
        .split(RegExp(r'\s+', unicode: true))
        .map((term) => term.trim())
        .where((term) => term.isNotEmpty)
        .take(12)
        .toList(growable: false);
    if (terms.isEmpty) return null;

    return terms.map((term) => '"${term.replaceAll('"', '""')}"*').join(' AND ');
  }
}
