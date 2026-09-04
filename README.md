# UsmanNotepad

UsmanNotepad is an Android-first, privacy-first, offline-first notes application focused on instant capture, reliable local persistence, and a calm premium writing experience.

## Active implementation track

The source-of-truth product documents require Flutter, Dart, Material 3, Riverpod, `go_router`, and Drift. The existing native Android application remains intact on `main`. The production Flutter rebuild is isolated under `flutter_app/` on `phase0-1-flutter-core` so the existing application and user data are not destructively replaced while the mandated architecture is verified.

The Flutter Android build keeps application id `com.usman.notepad` and migrates the existing `usman_notepad_v2.db` database in place.

## Current scope

Only Phase 0 and Phase 1 are active:

- Phase 0: project foundation, architecture, routing, design system, database, migrations, repositories, typed errors/logging, tests, CI.
- Phase 1: create/edit/autosave, delete/Trash/restore/permanent delete, undo/redo, title/body search, pin, favorite, basic checklist, notes list/grid, light/dark/system theme, and basic settings.

Later-phase UI is not exposed as fake functionality.

## Flutter prerequisites

- Flutter 3.47.x
- Dart 3.12+
- Android SDK 35
- Java 17

## Run

```bash
cd flutter_app
flutter pub get
dart run build_runner build --delete-conflicting-outputs
flutter run
```

## Verify

```bash
cd flutter_app
dart format --output=none --set-exit-if-changed lib test integration_test
flutter analyze
flutter test
flutter build apk --debug
flutter build apk --release
```

The branch CI executes the same quality gates and records the exact build result.

## Documentation

- `ARCHITECTURE.md` — production architecture and data flow
- `DATABASE.md` — schema, relationships, indexes, migration rules
- `SECURITY.md` — current security boundary and future encryption boundary
- `PRIVACY.md` — privacy defaults and data handling
- `ROADMAP.md` — phased roadmap and Phase 0/1 execution checklist
- `docs/superpowers/specs/2026-09-03-phase0-1-flutter-core-design.md` — implementation design
- `docs/superpowers/plans/2026-09-03-phase0-1-flutter-core.md` — executable implementation plan
