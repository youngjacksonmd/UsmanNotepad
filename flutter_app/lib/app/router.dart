import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/app/shell.dart';
import 'package:usman_notepad/features/home/presentation/home_screen.dart';
import 'package:usman_notepad/features/notes/presentation/editor_screen.dart';
import 'package:usman_notepad/features/notes/presentation/notes_screen.dart';
import 'package:usman_notepad/features/notes/presentation/trash_screen.dart';
import 'package:usman_notepad/features/search/presentation/search_screen.dart';
import 'package:usman_notepad/features/settings/presentation/settings_screen.dart';
import 'package:usman_notepad/features/tasks/presentation/tasks_screen.dart';

final rootNavigatorKey = GlobalKey<NavigatorState>();
final rootScaffoldMessengerKey = GlobalKey<ScaffoldMessengerState>();

final appRouter = GoRouter(
  navigatorKey: rootNavigatorKey,
  initialLocation: '/home',
  routes: <RouteBase>[
    ShellRoute(
      builder: (context, state, child) => AppShell(child: child),
      routes: <RouteBase>[
        GoRoute(path: '/home', builder: (context, state) => const HomeScreen()),
        GoRoute(path: '/notes', builder: (context, state) => const NotesScreen()),
        GoRoute(path: '/search', builder: (context, state) => const SearchScreen()),
        GoRoute(path: '/tasks', builder: (context, state) => const TasksScreen()),
        GoRoute(path: '/settings', builder: (context, state) => const SettingsScreen()),
      ],
    ),
    GoRoute(
      parentNavigatorKey: rootNavigatorKey,
      path: '/editor/:id',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['id'] ?? '');
        if (id == null) return const HomeScreen();
        return EditorScreen(
          noteId: id,
          autofocus: state.uri.queryParameters['new'] == '1',
        );
      },
    ),
    GoRoute(
      parentNavigatorKey: rootNavigatorKey,
      path: '/trash',
      builder: (context, state) => const TrashScreen(),
    ),
  ],
);
