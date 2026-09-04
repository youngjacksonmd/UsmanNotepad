import 'package:flutter/material.dart';

abstract final class AppSpacing {
  static const double xxs = 4;
  static const double xs = 8;
  static const double sm = 12;
  static const double md = 16;
  static const double lg = 20;
  static const double xl = 24;
  static const double xxl = 32;
  static const double xxxl = 40;
  static const double huge = 48;
  static const double hero = 64;
}

abstract final class AppRadius {
  static const double control = 8;
  static const double input = 12;
  static const double card = 18;
  static const double floating = 22;
  static const double sheet = 26;
}

abstract final class AppMotion {
  static const Duration quick = Duration(milliseconds: 120);
  static const Duration standard = Duration(milliseconds: 180);
  static const Duration expressive = Duration(milliseconds: 220);
}

abstract final class AppBreakpoints {
  static const double compact = 600;
  static const double expanded = 840;
  static const double desktop = 1200;
}

abstract final class AppColors {
  static const Color lightCanvas = Color(0xFFF8F7F4);
  static const Color lightSurface = Color(0xFFFFFEFC);
  static const Color lightVariant = Color(0xFFF0EEE9);
  static const Color lightText = Color(0xFF232321);
  static const Color lightSecondaryText = Color(0xFF6D6B67);
  static const Color indigo = Color(0xFF5667A8);

  static const Color darkCanvas = Color(0xFF171716);
  static const Color darkSurface = Color(0xFF211F1E);
  static const Color darkVariant = Color(0xFF2A2826);
  static const Color darkText = Color(0xFFF2F0EC);
  static const Color darkSecondaryText = Color(0xFFAAA7A1);
  static const Color darkIndigo = Color(0xFFAEBBFA);

  static const Color error = Color(0xFFB3261E);
  static const Color favorite = Color(0xFFC18B19);
  static const Color pinned = Color(0xFF6675B2);
}
