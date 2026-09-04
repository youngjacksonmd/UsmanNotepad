import 'package:flutter/material.dart';

/// UsmanNotepad's Soft Canvas brand mark.
///
/// A folded paper card forms a subtle U so the mark stays recognizable at small
/// sizes, works in monochrome, and remains visually related to notes.
class UsmanNotepadMark extends StatelessWidget {
  const UsmanNotepadMark({super.key, this.size = 44});

  final double size;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Semantics(
      label: 'UsmanNotepad logo',
      image: true,
      child: SizedBox.square(
        dimension: size,
        child: CustomPaint(
          painter: _UsmanNotepadMarkPainter(
            ink: colorScheme.onSurface,
            paper: colorScheme.surfaceContainerHighest,
            accent: colorScheme.primary,
          ),
        ),
      ),
    );
  }
}

class _UsmanNotepadMarkPainter extends CustomPainter {
  const _UsmanNotepadMarkPainter({
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
      const Rect.fromLTWH(6, 5, 52, 54),
      const Radius.circular(15),
    );
    canvas.drawRRect(card, Paint()..color = paper);

    final uPath = Path()
      ..moveTo(19, 19)
      ..lineTo(19, 34)
      ..cubicTo(19, 45, 25, 50, 32, 50)
      ..cubicTo(39, 50, 45, 45, 45, 34)
      ..lineTo(45, 19);

    canvas.drawPath(
      uPath,
      Paint()
        ..color = ink
        ..style = PaintingStyle.stroke
        ..strokeWidth = 4.4
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round,
    );

    final fold = Path()
      ..moveTo(43, 42)
      ..lineTo(51, 34)
      ..lineTo(52, 45)
      ..close();
    canvas.drawPath(fold, Paint()..color = accent);

    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant _UsmanNotepadMarkPainter oldDelegate) {
    return oldDelegate.ink != ink ||
        oldDelegate.paper != paper ||
        oldDelegate.accent != accent;
  }
}
