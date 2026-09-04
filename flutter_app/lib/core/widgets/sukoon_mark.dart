import 'package:flutter/material.dart';

/// Sukoon Notes' original Soft Canvas mark.
///
/// Two flowing paper strokes form a quiet, abstract S while the amber fold
/// suggests a saved thought. It is drawn in code so it stays sharp offline at
/// every density and in both light and dark themes.
class SukoonMark extends StatelessWidget {
  const SukoonMark({super.key, this.size = 44});

  final double size;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Semantics(
      label: 'Sukoon Notes logo',
      image: true,
      child: SizedBox.square(
        dimension: size,
        child: CustomPaint(
          painter: _SukoonMarkPainter(
            ink: colorScheme.onSurface,
            paper: colorScheme.surfaceContainerHighest,
            accent: colorScheme.primary,
          ),
        ),
      ),
    );
  }
}

class _SukoonMarkPainter extends CustomPainter {
  const _SukoonMarkPainter({
    required this.ink,
    required this.paper,
    required this.accent,
  });

  final Color ink;
  final Color paper;
  final Color accent;

  @override
  void paint(Canvas canvas, Size size) {
    final scale = size.shortestSide / 64;
    canvas.save();
    canvas.scale(scale, scale);

    final card = RRect.fromRectAndRadius(
      const Rect.fromLTWH(5, 5, 54, 54),
      const Radius.circular(17),
    );
    canvas.drawRRect(card, Paint()..color = paper);

    final upper = Path()
      ..moveTo(18, 22)
      ..cubicTo(24, 13, 41, 13, 47, 20)
      ..cubicTo(43, 18, 37, 18, 33, 20)
      ..cubicTo(29, 22, 28, 25, 31, 27)
      ..cubicTo(34, 29, 40, 29, 44, 31);

    final lower = Path()
      ..moveTo(46, 41)
      ..cubicTo(40, 50, 23, 50, 17, 43)
      ..cubicTo(21, 45, 27, 46, 31, 44)
      ..cubicTo(35, 42, 36, 39, 33, 37)
      ..cubicTo(30, 35, 24, 35, 20, 33);

    final stroke = Paint()
      ..color = ink
      ..style = PaintingStyle.stroke
      ..strokeWidth = 4.2
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;
    canvas.drawPath(upper, stroke);
    canvas.drawPath(lower, stroke);

    final fold = Path()
      ..moveTo(43, 42)
      ..lineTo(50, 35)
      ..lineTo(51, 45)
      ..close();
    canvas.drawPath(fold, Paint()..color = accent);

    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant _SukoonMarkPainter oldDelegate) {
    return oldDelegate.ink != ink ||
        oldDelegate.paper != paper ||
        oldDelegate.accent != accent;
  }
}
