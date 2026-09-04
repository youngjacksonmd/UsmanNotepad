import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:usman_notepad/app/app.dart';
import 'package:usman_notepad/app/providers.dart';
import 'package:usman_notepad/core/database/app_database.dart';

void main() {
  testWidgets('home exposes the Phase 1 writing-first surface', (tester) async {
    SharedPreferences.setMockInitialValues(<String, Object>{});
    final database = AppDatabase.forTesting(NativeDatabase.memory());
    addTearDown(database.close);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [databaseProvider.overrideWithValue(database)],
        child: const UsmanNotepadApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Sukoon Notes'), findsOneWidget);
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
