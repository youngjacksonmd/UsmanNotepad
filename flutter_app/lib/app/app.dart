import 'package:flutter/material.dart';

class UsmanNotepadApp extends StatelessWidget {
  const UsmanNotepadApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'UsmanNotepad',
      theme: ThemeData(useMaterial3: true),
      home: const Scaffold(
        body: SafeArea(
          child: Center(
            child: Text('UsmanNotepad'),
          ),
        ),
      ),
    );
  }
}
