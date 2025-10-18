import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';

class UttaradhikarVarasatScreen extends StatelessWidget {
  const UttaradhikarVarasatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bottomPadding = 50 + MediaQuery.of(context).padding.bottom;
    return Scaffold(
      appBar: AppBar(
        title: const Text('वरासत ऑनलाइन'),
      ),
      body: ListView(
        padding: EdgeInsets.fromLTRB(12, 12, 12, bottomPadding),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.info_outline_rounded,
                        color: theme.colorScheme.primary,
                        size: 30,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'उत्तराधिकार वरासत वीडियो गाइड',
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'भूलेख में दर्ज भूमि का उत्तराधिकार कराने की प्रक्रिया, ज़रूरी कागज़ात और आवेदन की सावधानियाँ जानें।',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 16),
                  Center(
                    child: ElevatedButton.icon(
                      icon: const Icon(Icons.play_circle_fill_rounded),
                      label: const Text('वीडियो देखें'),
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('वीडियो लिंक जल्द जोड़ा जाएगा।'),
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.assignment_turned_in_outlined,
                        color: theme.colorScheme.primary,
                        size: 30,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'आवेदन हेतु ज़रूरी कागज़ात',
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    '• मृतक का आधार कार्ड',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '• मृतक का डेथ सर्टिफिकेट',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '• परिवार की नकल',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '• सभी वरासत पाने वाले के आधार कार्ड की फोटो कॉपी',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'नोट: इस ऑनलाइन आवेदन में कोई भी दस्तावेज़ अपलोड नहीं करना है। आवेदन सबमिट करने के बाद लेखपाल पोर्टल पर जाएगा जहाँ सभी कागज़ात आपको लेखपाल को देने होंगे, जिसके बाद वरासत प्रक्रिया पूरी होगी।',
                    style: theme.textTheme.bodyMedium?.copyWith(height: 1.4),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.family_restroom_outlined,
                        color: theme.colorScheme.primary,
                        size: 30,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'उत्तराधिकार वरासत ऑनलाइन',
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'अपने मोबाइल से उत्तराधिकार वरासत का नया आवेदन करें और पहले से जमा किए आवेदन की स्थिति देखें।',
                    style: theme.textTheme.bodyLarge?.copyWith(height: 1.4),
                  ),
                  const SizedBox(height: 16),
                  Align(
                    alignment: Alignment.center,
                    child: Wrap(
                      alignment: WrapAlignment.center,
                      spacing: 12,
                      runSpacing: 12,
                      children: [
                        ElevatedButton.icon(
                          icon: const Icon(Icons.note_add_outlined),
                          label: const Text('आवेदन करें'),
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'उत्तराधिकार वरासत आवेदन',
                                  initialUrl: 'https://vaad.up.nic.in/',
                                  disableViewportFix: true,
                                  disableContentBlockers: true,
                                ),
                              ),
                            );
                          },
                        ),
                        ElevatedButton.icon(
                          icon: const Icon(Icons.verified_outlined),
                          label: const Text('स्थिति देखें'),
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'उत्तराधिकार वरासत स्थिति',
                                  initialUrl:
                                      'https://vaad.up.nic.in/search_p11_application.aspx',
                                  disableViewportFix: true,
                                  disableContentBlockers: true,
                                ),
                              ),
                            );
                          },
                        ),
                      ],
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
