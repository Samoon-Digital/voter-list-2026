import 'package:flutter/material.dart';

class HomeDetailCard extends StatelessWidget {
  const HomeDetailCard({
    super.key,
    required this.icon,
    required this.title,
    required this.children,
    this.badge,
    this.gradientColors,
    this.padding,
    this.cornerRadius,
  });

  final IconData icon;
  final String title;
  final List<Widget> children;
  final String? badge;
  final List<Color>? gradientColors;
  final EdgeInsetsGeometry? padding;
  final double? cornerRadius;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = gradientColors ?? [theme.colorScheme.primary, theme.colorScheme.secondary];
    final double radius = (cornerRadius ?? 20).clamp(0, double.infinity);
    final double innerRadius = radius > 2 ? radius - 2 : 0;

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(colors: colors),
        borderRadius: BorderRadius.circular(radius),
        boxShadow: [
          BoxShadow(
            color: colors.first.withOpacity(0.2),
            blurRadius: 18,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Container(
        margin: const EdgeInsets.all(1.6),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(innerRadius),
        ),
        padding: padding ?? const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: LinearGradient(colors: [colors.first, colors.last]),
              ),
              child: Icon(
                icon,
                color: theme.colorScheme.onPrimary,
                size: 26,
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w800,
                      height: 1.15,
                    ),
                  ),
                  if (badge != null && badge!.trim().isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.secondaryContainer,
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        badge!,
                        style: theme.textTheme.labelSmall?.copyWith(
                          fontWeight: FontWeight.w700,
                          color: theme.colorScheme.onSecondaryContainer,
                        ),
                      ),
                    ),
                  ],
                  if (children.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    ...children,
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
