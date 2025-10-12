import 'package:flutter/material.dart';

class GradientFolderIcon extends StatefulWidget {
  const GradientFolderIcon({super.key, this.size = 24});

  final double size;

  @override
  State<GradientFolderIcon> createState() => _GradientFolderIconState();
}

class _GradientFolderIconState extends State<GradientFolderIcon>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 3200),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        final t = _controller.value;
        final scale = 1 + 0.06 * (0.5 - (t - 0.5).abs());
        final opacity = 0.92 + 0.08 * (0.5 - (t - 0.5).abs());
        final gradient = LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            colorScheme.primary.withOpacity(0.85),
            colorScheme.secondary.withOpacity(0.9),
            colorScheme.tertiary.withOpacity(0.95),
          ],
          transform: GradientRotation(t * 2 * 3.14159),
        );

        return Transform.scale(
          scale: scale,
          child: Opacity(
            opacity: opacity,
            child: ShaderMask(
              shaderCallback: (bounds) => gradient.createShader(bounds),
              blendMode: BlendMode.srcIn,
              child: Icon(
                Icons.folder_rounded,
                size: widget.size,
              ),
            ),
          ),
        );
      },
    );
  }
}
