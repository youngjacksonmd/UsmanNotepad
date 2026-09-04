import 'package:flutter_test/flutter_test.dart';
import 'package:usman_notepad/features/search/domain/fts_query.dart';

void main() {
  test('empty input produces no FTS query', () {
    expect(FtsQuery.fromUserInput('   '), isNull);
  });

  test('words become quoted prefix terms', () {
    expect(
      FtsQuery.fromUserInput('client invoice'),
      '"client"* AND "invoice"*',
    );
  });

  test('quotes and punctuation cannot break MATCH syntax', () {
    expect(
      FtsQuery.fromUserInput('say "hello" + paid'),
      '"say"* AND "hello"* AND "paid"*',
    );
  });

  test('Unicode words remain searchable', () {
    expect(
      FtsQuery.fromUserInput('payment ابھی pending'),
      '"payment"* AND "ابھی"* AND "pending"*',
    );
  });
}
