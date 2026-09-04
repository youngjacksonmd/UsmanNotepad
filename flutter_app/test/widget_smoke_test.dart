import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:usman_notepad/app/app.dart';

void main() {
  testWidgets('home exposes the Phase 1 writing-first surface', (tester) async {
    await tester.pumpWidget(
      const ProviderScope(child: UsmanNotepadApp()),
    );
    await tester.pumpAndSettle();

    expect(find.textContaining('Usman'), findsWidgets);
    expect(find.text('Search your notes'), findsOneWidget);
    expect(find.text('Take a note…'), findsOneWidget);
    expect(find.text('Text'), findsWidgets);
    expect(find.text('Checklist'), findsWidgets);
    expect(find.text('Pinned'), findsOneWidget);
    expect(find.text('Recent'), findsOneWidget);
    expect(find.text('Home'), findsOneWidget);
    expect(find.text('Notes'), findsOneWidget);
    expect(find.text('Search'), findsOneWidget);
    expect(find.text('Tasks'), findsOneWidget);
    expect(find.text('Settings'), findsOneWidget);
  });
}
