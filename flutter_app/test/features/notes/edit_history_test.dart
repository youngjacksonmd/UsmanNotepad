import 'package:flutter_test/flutter_test.dart';
import 'package:usman_notepad/features/notes/application/edit_history.dart';

void main() {
  test('undo and redo restore prior values', () {
    final history = BoundedEditHistory<String>(
      initialValue: 'a',
      estimateBytes: (value) => value.length * 2,
      maxEntries: 10,
      maxBytes: 1024,
    );

    history.record('ab', forceBoundary: true);
    history.record('abc', forceBoundary: true);

    expect(history.undo(), 'ab');
    expect(history.undo(), 'a');
    expect(history.redo(), 'ab');
  });

  test('record after undo discards redo branch', () {
    final history = BoundedEditHistory<String>(
      initialValue: 'a',
      estimateBytes: (value) => value.length * 2,
      maxEntries: 10,
      maxBytes: 1024,
    );
    history.record('b', forceBoundary: true);
    history.record('c', forceBoundary: true);
    expect(history.undo(), 'b');
    history.record('d', forceBoundary: true);
    expect(history.canRedo, isFalse);
    expect(history.current, 'd');
  });

  test('history obeys entry and byte bounds', () {
    final history = BoundedEditHistory<String>(
      initialValue: '0',
      estimateBytes: (value) => value.length,
      maxEntries: 3,
      maxBytes: 5,
    );
    history.record('11', forceBoundary: true);
    history.record('22', forceBoundary: true);
    history.record('333', forceBoundary: true);
    expect(history.entryCount, lessThanOrEqualTo(3));
    expect(history.estimatedBytes, lessThanOrEqualTo(5));
  });
}
