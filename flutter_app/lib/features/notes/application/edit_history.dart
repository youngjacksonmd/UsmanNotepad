class BoundedEditHistory<T> {
  BoundedEditHistory({
    required T initialValue,
    required int Function(T value) estimateBytes,
    this.maxEntries = 50,
    this.maxBytes = 2 * 1024 * 1024,
    this.coalesceWindow = const Duration(milliseconds: 700),
  }) : _estimate = estimateBytes,
       assert(maxEntries > 0),
       assert(maxBytes > 0) {
    _entries.add(initialValue);
    _sizes.add(_estimate(initialValue));
  }

  final int Function(T value) _estimate;
  final int maxEntries;
  final int maxBytes;
  final Duration coalesceWindow;

  final List<T> _entries = <T>[];
  final List<int> _sizes = <int>[];
  int _index = 0;
  DateTime? _lastRecordAt;

  T get current => _entries[_index];
  bool get canUndo => _index > 0;
  bool get canRedo => _index < _entries.length - 1;
  int get entryCount => _entries.length;
  int get estimatedBytes => _sizes.fold<int>(0, (sum, size) => sum + size);

  void record(
    T value, {
    bool forceBoundary = false,
    DateTime? at,
  }) {
    if (value == current) return;

    final now = at ?? DateTime.now();
    if (canRedo) {
      _entries.removeRange(_index + 1, _entries.length);
      _sizes.removeRange(_index + 1, _sizes.length);
    }

    final shouldCoalesce =
        !forceBoundary &&
        _lastRecordAt != null &&
        now.difference(_lastRecordAt!) <= coalesceWindow &&
        _entries.length > 1;

    if (shouldCoalesce) {
      _entries[_index] = value;
      _sizes[_index] = _estimate(value);
    } else {
      _entries.add(value);
      _sizes.add(_estimate(value));
      _index = _entries.length - 1;
    }
    _lastRecordAt = now;
    _trim();
  }

  T? undo() {
    if (!canUndo) return null;
    _index--;
    _lastRecordAt = null;
    return current;
  }

  T? redo() {
    if (!canRedo) return null;
    _index++;
    _lastRecordAt = null;
    return current;
  }

  void reset(T value) {
    _entries
      ..clear()
      ..add(value);
    _sizes
      ..clear()
      ..add(_estimate(value));
    _index = 0;
    _lastRecordAt = null;
  }

  void _trim() {
    while (_entries.length > 1 &&
        (_entries.length > maxEntries || estimatedBytes > maxBytes)) {
      _entries.removeAt(0);
      _sizes.removeAt(0);
      _index--;
    }
    if (_index < 0) _index = 0;
  }
}
