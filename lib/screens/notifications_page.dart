import 'package:flutter/material.dart';
class NotificationsPage extends StatelessWidget {
  const NotificationsPage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
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
        const SizedBox(height: 4),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Icon(Icons.agriculture, size: 64, color: theme.colorScheme.primary),
                const SizedBox(height: 16),
                Text(
                  'कार्य जारी है…',
                  style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 12),
                Text(
                  'इस टैब का नया कंटेंट बहुत जल्द जोड़ा जाएगा।',
                  style: theme.textTheme.bodyLarge,
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
