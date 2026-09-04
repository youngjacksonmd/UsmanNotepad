import 'package:flutter_test/flutter_test.dart';
import 'package:usman_notepad/app/app.dart';

void main() {
  testWidgets('app renders UsmanNotepad home shell', (tester) async {
    await tester.pumpWidget(const UsmanNotepadApp());
    await tester.pumpAndSettle();
    expect(find.text('UsmanNotepad'), findsWidgets);
  });
}
