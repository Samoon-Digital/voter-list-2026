import 'package:flutter/material.dart';

typedef FeatureTap = void Function(BuildContext context);
typedef GradientBuilder = List<Color> Function(ThemeData theme);

class FeatureCardConfig {
  const FeatureCardConfig({
    required this.icon,
    required this.title,
    required this.description,
    required this.gradient,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String description;
  final GradientBuilder gradient;
  final FeatureTap onTap;
}

class FeatureSection extends StatelessWidget {
  const FeatureSection({super.key, required this.title, required this.cards});

  final String title;
  final List<FeatureCardConfig> cards;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bottomPadding = 50 + MediaQuery.of(context).padding.bottom;
    return ListView(
      padding: EdgeInsets.fromLTRB(12, 12, 12, bottomPadding),
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 6.0),
          child: Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
          ),
        ),
        const SizedBox(height: 12),
        for (var i = 0; i < cards.length; i++) ...[
          FeatureCard(config: cards[i]),
          if (i != cards.length - 1) const SizedBox(height: 16),
        ],
      ],
    );
  }
}

class FeatureCard extends StatelessWidget {
  const FeatureCard({super.key, required this.config});

  final FeatureCardConfig config;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = config.gradient(theme);
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(colors: colors),
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: colors.first.withOpacity(0.18),
            blurRadius: 18,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        borderRadius: BorderRadius.circular(18),
        child: InkWell(
          borderRadius: BorderRadius.circular(18),
          onTap: () => config.onTap(context),
          child: Container(
            margin: const EdgeInsets.all(2),
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(18),
            ),
            padding: const EdgeInsets.all(18),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: LinearGradient(
                      colors: [colors.first, colors.last],
                    ),
                  ),
                  child: Icon(
                    config.icon,
                    color: theme.colorScheme.onPrimary,
                    size: 28,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        config.title,
                        style: theme.textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        config.description,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.w600,
                          height: 1.3,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
