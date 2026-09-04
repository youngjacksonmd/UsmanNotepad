import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:usman_notepad/core/theme/tokens.dart';

class AppShell extends StatelessWidget {
  const AppShell({required this.child, super.key});

  final Widget child;

  static const _paths = <String>['/home', '/notes', '/search', '/tasks', '/settings'];

  @override
  Widget build(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    final selected = _indexFor(location);
    return LayoutBuilder(
      builder: (context, constraints) {
        final expanded = constraints.maxWidth >= AppBreakpoints.expanded;
        if (expanded) {
          return Scaffold(
            body: Row(
              children: <Widget>[
                SafeArea(
                  child: NavigationRail(
                    selectedIndex: selected,
                    onDestinationSelected: (index) => context.go(_paths[index]),
                    labelType: NavigationRailLabelType.all,
                    destinations: const <NavigationRailDestination>[
                      NavigationRailDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home_rounded), label: Text('Home')),
                      NavigationRailDestination(icon: Icon(Icons.note_outlined), selectedIcon: Icon(Icons.note_rounded), label: Text('Notes')),
                      NavigationRailDestination(icon: Icon(Icons.search_rounded), label: Text('Search')),
                      NavigationRailDestination(icon: Icon(Icons.check_circle_outline_rounded), selectedIcon: Icon(Icons.check_circle_rounded), label: Text('Tasks')),
                      NavigationRailDestination(icon: Icon(Icons.settings_outlined), selectedIcon: Icon(Icons.settings_rounded), label: Text('Settings')),
                    ],
                  ),
                ),
                const VerticalDivider(width: 1),
                Expanded(child: child),
              ],
            ),
          );
        }
        return Scaffold(
          body: child,
          bottomNavigationBar: NavigationBar(
            selectedIndex: selected,
            onDestinationSelected: (index) => context.go(_paths[index]),
            destinations: const <NavigationDestination>[
              NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home_rounded), label: 'Home'),
              NavigationDestination(icon: Icon(Icons.note_outlined), selectedIcon: Icon(Icons.note_rounded), label: 'Notes'),
              NavigationDestination(icon: Icon(Icons.search_rounded), label: 'Search'),
              NavigationDestination(icon: Icon(Icons.check_circle_outline_rounded), selectedIcon: Icon(Icons.check_circle_rounded), label: 'Tasks'),
              NavigationDestination(icon: Icon(Icons.settings_outlined), selectedIcon: Icon(Icons.settings_rounded), label: 'Settings'),
            ],
          ),
        );
      },
    );
  }

  int _indexFor(String location) {
    for (var index = 0; index < _paths.length; index++) {
      if (location.startsWith(_paths[index])) return index;
    }
    return 0;
  }
}
