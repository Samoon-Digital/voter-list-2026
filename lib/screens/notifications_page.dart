import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/bhulekh_info_screen.dart';
import 'package:yojna_plus/screens/uttaradhikar_varasat_screen.dart';
import 'package:yojna_plus/widgets/feature_section.dart';
class NotificationsPage extends StatelessWidget {
  const NotificationsPage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final cards = [
      FeatureCardConfig(
        icon: Icons.map_rounded,
        title: 'भूलेख - खतौनी',
        description:
            'अपनी खतौनी देखें — नाम, गाटा संख्या या खाता संख्या से खोजें।',
        gradient: (theme) => [
          theme.colorScheme.primary,
          theme.colorScheme.secondary,
        ],
        onTap: (context) {
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const BhulekhInfoScreen(),
            ),
          );
        },
      ),
      FeatureCardConfig(
        icon: Icons.family_restroom_rounded,
        title: 'उत्तराधिकार वरासत ऑनलाइन',
        description:
            'अपनी वरासत ऑनलाइन आवेदन करें अपने मोबाइल से और उसकी स्थिति भी चेक करें।',
        gradient: (theme) => [
          theme.colorScheme.secondary,
          theme.colorScheme.primary,
        ],
        onTap: (context) {
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const UttaradhikarVarasatScreen(),
            ),
          );
        },
      ),
    ];

    return ListView(
      padding: EdgeInsets.fromLTRB(
        12,
        12,
        12,
        50 + MediaQuery.of(context).padding.bottom,
      ),
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 6.0),
          child: Text('किसान', style: theme.textTheme.titleMedium),
        ),
        const SizedBox(height: 12),
        for (var i = 0; i < cards.length; i++) ...[
          FeatureCard(config: cards[i]),
          if (i != cards.length - 1) const SizedBox(height: 16),
        ],
        const SizedBox(height: 20),
      ],
    );
  }
}

