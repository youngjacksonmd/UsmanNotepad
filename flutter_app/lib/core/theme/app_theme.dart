import 'package:flutter/material.dart';
import 'package:usman_notepad/core/theme/tokens.dart';

abstract final class AppTheme {
  static ThemeData light() {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.indigo,
      brightness: Brightness.light,
      surface: AppColors.lightSurface,
    ).copyWith(
      primary: AppColors.indigo,
      surface: AppColors.lightSurface,
      surfaceContainer: AppColors.lightVariant,
      onSurface: AppColors.lightText,
      onSurfaceVariant: AppColors.lightSecondaryText,
      error: AppColors.error,
    );
    return _base(scheme, AppColors.lightCanvas);
  }

  static ThemeData dark() {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.darkIndigo,
      brightness: Brightness.dark,
      surface: AppColors.darkSurface,
    ).copyWith(
      primary: AppColors.darkIndigo,
      surface: AppColors.darkSurface,
      surfaceContainer: AppColors.darkVariant,
      surfaceContainerLow: AppColors.darkSurface,
      surfaceContainerHigh: AppColors.darkVariant,
      onSurface: AppColors.darkText,
      onSurfaceVariant: AppColors.darkSecondaryText,
      error: const Color(0xFFFFB4AB),
    );
    return _base(scheme, AppColors.darkCanvas);
  }

  static ThemeData _base(ColorScheme scheme, Color canvas) {
    final textTheme = Typography.material2021().black.apply(
          bodyColor: scheme.onSurface,
          displayColor: scheme.onSurface,
        );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: canvas,
      canvasColor: canvas,
      splashFactory: InkSparkle.splashFactory,
      textTheme: textTheme.copyWith(
        headlineLarge: textTheme.headlineLarge?.copyWith(
          fontSize: 30,
          height: 1.12,
          fontWeight: FontWeight.w700,
          letterSpacing: -0.6,
        ),
        titleLarge: textTheme.titleLarge?.copyWith(
          fontSize: 20,
          fontWeight: FontWeight.w700,
          letterSpacing: -0.2,
        ),
        titleMedium: textTheme.titleMedium?.copyWith(
          fontSize: 17,
          fontWeight: FontWeight.w600,
        ),
        bodyLarge: textTheme.bodyLarge?.copyWith(fontSize: 16, height: 1.45),
        bodyMedium: textTheme.bodyMedium?.copyWith(fontSize: 15, height: 1.4),
        labelLarge: textTheme.labelLarge?.copyWith(fontWeight: FontWeight.w600),
      ),
      cardTheme: CardThemeData(
        color: scheme.surface,
        elevation: 0.7,
        shadowColor: Colors.black.withValues(alpha: 0.10),
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.card),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: scheme.surface.withValues(alpha: 0.97),
        indicatorColor: scheme.primary.withValues(alpha: 0.12),
        elevation: 0,
        height: 72,
      ),
      navigationRailTheme: NavigationRailThemeData(
        backgroundColor: scheme.surface.withValues(alpha: 0.96),
        indicatorColor: scheme.primary.withValues(alpha: 0.12),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: scheme.surface,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.floating),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.floating),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.floating),
          borderSide: BorderSide(color: scheme.primary.withValues(alpha: 0.35)),
        ),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.input),
        ),
      ),
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: <TargetPlatform, PageTransitionsBuilder>{
          TargetPlatform.android: FadeForwardsPageTransitionsBuilder(),
        },
      ),
    );
  }
}
