import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class AwasCardScreen extends StatelessWidget {
  const AwasCardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('आवास योजना 2025-26'),
      ),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.ondemand_video,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'आवास योजना वीडियो गाइड',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'आवास सूची में अपना नाम कैसे देखें? पूरी वीडियो देखें और समझें।',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('वीडियो लिंक जल्द जोड़ा जाएगा।'),
                          ),
                        );
                      },
                      icon: const Icon(Icons.play_circle_fill_outlined),
                      label: const Text('वीडियो देखें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.fact_check_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'आवास सूची देखें ग्रामीण',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'यहां से आप अपना नाम सूची में देख सकते हैं।',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () async {
                        final messenger = ScaffoldMessenger.of(context);
                        final uris = [
                          Uri.parse('https://rhreporting.nic.in/netiay/EFMSReport/BenAccountFrozenReport.aspx'),
                          Uri.parse('https://rhreporting.nic.in/netiay/Home.aspx'),
                        ];
                        bool launched = false;
                        for (final uri in uris) {
                          if (await launchUrl(uri, mode: LaunchMode.externalApplication)) {
                            launched = true;
                            break;
                          }
                        }
                        if (!launched) {
                          messenger.showSnackBar(
                            const SnackBar(
                              content: Text('लिंक नहीं खुल पाया, कृपया कुछ देर बाद पुनः प्रयास करें।'),
                            ),
                          );
                        }
                      },
                      icon: const Icon(Icons.link_outlined),
                      label: const Text('सूची देखें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.list_alt_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'आवास प्लस सूची',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'जिन लाभार्थियों का आवेदन आवास प्लस ऐप के द्वारा हुआ है वो लोग अपना सूची में नाम यहां से देखें।',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () async {
                        final messenger = ScaffoldMessenger.of(context);
                        final uri = Uri.parse(
                          'https://rhreporting.nic.in/netiay/AwaasPlus2018Reports/AwaasPlusCategoryWiseVerReport.aspx',
                        );
                        if (!await launchUrl(
                          uri,
                          mode: LaunchMode.externalApplication,
                        )) {
                          messenger.showSnackBar(
                            const SnackBar(
                              content: Text('लिंक नहीं खुल पाया, कृपया कुछ देर बाद पुनः प्रयास करें।'),
                            ),
                          );
                        }
                      },
                      icon: const Icon(Icons.open_in_new),
                      label: const Text('सूची में देखें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

