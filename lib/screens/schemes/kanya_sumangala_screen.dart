import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/tts/tts_reader.dart';
import 'package:yojna_plus/tts/highlight_text.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

class KanyaSumangalaScreen extends StatefulWidget {
  const KanyaSumangalaScreen({super.key});

  @override
  State<KanyaSumangalaScreen> createState() => _KanyaSumangalaScreenState();
}

class _KanyaSumangalaScreenState extends State<KanyaSumangalaScreen> {
  final TtsReaderController _tts = TtsReaderController();

  late final List<String> _segments = [
    'यह उत्तर प्रदेश सरकार की एक पहल है, जिसका मकसद बेटियों के जन्म से लेकर उनकी पढ़ाई और आगे के जीवन तक उन्हें आर्थिक और सामाजिक सहयोग देना है।',
    'राशन कार्ड अथवा फॅमिली आइडी, माता पिता का आधार कार्ड, बैंक पासबुक, आय प्रमाणपत्र, जॉइन्ट फोटो माता पिता और बच्चे का साथ मैं , हर स्टेज का प्रमाण पत्र (जन्म प्रमाणपत्र /टीकाकरण/स्कूल-कॉलेज एडमिशन मोबाईल नंबर )',
    'नोट: आवेदन केवल उन बेटियों के लिए मान्य है जिनका जन्म 1 अप्रैल 2019 या उसके बाद हुआ है।',
    'आपको नया आवेदन किसी भी नजदीकी जनसेवा केंद्र से कराना चाहिए। स्वयं करने पर गलती हो सकती है जिससे आप लाभ नहीं पाएंगे।',
    '1) पहला चरण (जन्म पर) — बेटी के जन्म पर ₹2,000 मिलते हैं।',
    '2) दूसरा चरण (1 साल टीकाकरण पूरा होने पर) — बच्ची का पूरा टीकाकरण होने पर ₹1,000 मिलते हैं।',
    '3) तीसरा चरण (कक्षा 1 में प्रवेश पर) — बेटी जब कक्षा 1 में दाखिला लेती है तो ₹2,000 मिलते हैं।',
    '4) चौथा चरण (कक्षा 6 में प्रवेश पर) — कक्षा 6 में दाखिले पर ₹2,000 मिलते हैं।',
    '5) पाँचवाँ चरण (कक्षा 9 में प्रवेश पर) — कक्षा 9 में एडमिशन पर ₹3,000 मिलते हैं।',
    '6) छठा चरण (12वीं के बाद डिग्री/डिप्लोमा में प्रवेश पर) — ग्रेजुएशन या डिप्लोमा कोर्स में दाखिला लेने पर ₹5,000 मिलते हैं।',
    'हर चरण को मिलाकर ₹15,000 की आर्थिक सहायता बेटी को दी जाती है और हर चरण पर आपको आवेदन कराना होता है',
    'इस लिंक से आप योजना की आधिकारिक वेबसाइट पर विवरण देख सकते हैं।',
  ];

  @override
  void initState() {
    super.initState();
    _tts.setSegments(_segments);
  }

  @override
  void dispose() {
    _tts.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('मुख्यमंत्री कन्या सुमंगला योजना'),
        actions: [
          AnimatedBuilder(
            animation: _tts,
            builder: (context, _) {
              final color = _tts.isSpeaking ? Theme.of(context).colorScheme.secondary : null;
              return IconButton(
                tooltip: 'आवाज़',
                icon: Icon(Icons.volume_up_rounded, color: color),
                onPressed: _tts.toggleSpeak,
              );
            },
          ),
        ],
      ),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          // क्या है कन्या सुमंगला योजना?
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'क्या है कन्या सुमंगला योजना?'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 0,
                    text:
                        'यह उत्तर प्रदेश सरकार की एक पहल है, जिसका मकसद बेटियों के जन्म से लेकर उनकी पढ़ाई और आगे के जीवन तक उन्हें आर्थिक और सामाजिक सहयोग देना है।',
                  ),
                  const SizedBox(height: 6),
                
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/1708799616',
          ),
          const SizedBox(height: 8),
          // आवेदन हेतु आवश्यक दस्तावेज़ और पात्रता
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवेदन हेतु आवश्यक दस्तावेज़ और पात्रता'),
                  const SizedBox(height: 8),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(Icons.check_circle_outline, color: Theme.of(context).colorScheme.primary, size: 18),
                      const SizedBox(width: 8),
                      Expanded(
                        child: HighlightableText(
                          controller: _tts,
                          segmentIndex: 1,
                          text:
                              'राशन कार्ड अथवा फॅमिली आइडी, माता पिता का आधार कार्ड, बैंक पासबुक, आय प्रमाणपत्र, जॉइन्ट फोटो माता पिता और बच्चे का साथ मैं , हर स्टेज का प्रमाण पत्र (जन्म प्रमाणपत्र /टीकाकरण/स्कूल-कॉलेज एडमिशन मोबाईल नंबर )',
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(Icons.info_outline, color: Theme.of(context).colorScheme.primary),
                      const SizedBox(width: 8),
                      Expanded(
                        child: HighlightableText(
                          controller: _tts,
                          segmentIndex: 2,
                          text: 'नोट: आवेदन केवल उन बेटियों के लिए मान्य है जिनका जन्म 1 अप्रैल 2019 या उसके बाद हुआ है।',
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          // सुझाव: आवेदन कैसे कराएँ
          Stack(
            clipBehavior: Clip.none,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _sectionHeader(context, Icons.lightbulb_outline, 'आवेदन कैसे कराएँ'),
                      const SizedBox(height: 8),
                      HighlightableText(
                        controller: _tts,
                        segmentIndex: 3,
                        text:
                            'आपको नया आवेदन किसी भी नजदीकी जनसेवा केंद्र से कराना चाहिए। स्वयं करने पर गलती हो सकती है जिससे आप लाभ नहीं पाएंगे।',
                      ),
                    ],
                  ),
                ),
              ),
              Positioned(
                top: -2,
                right: 6,
                child: _RibbonBadge('जरूरी सुझाव'),
              ),
            ],
          ),
          const SizedBox(height: 8),
          // चरणवार (स्टेज वाइज) मिलने वाली आर्थिक मदद
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.payments_outlined, 'चरणवार मिलने वाली आर्थिक मदद'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 4,
                    text: '1) पहला चरण (जन्म पर) — बेटी के जन्म पर ₹2,000 मिलते हैं।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 5,
                    text:
                        '2) दूसरा चरण (1 साल टीकाकरण पूरा होने पर) — बच्ची का पूरा टीकाकरण होने पर ₹1,000 मिलते हैं।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 6,
                    text:
                        '3) तीसरा चरण (कक्षा 1 में प्रवेश पर) — बेटी जब कक्षा 1 में दाखिला लेती है तो ₹2,000 मिलते हैं।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 7,
                    text: '4) चौथा चरण (कक्षा 6 में प्रवेश पर) — कक्षा 6 में दाखिले पर ₹2,000 मिलते हैं।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 8,
                    text: '5) पाँचवाँ चरण (कक्षा 9 में प्रवेश पर) — कक्षा 9 में एडमिशन पर ₹3,000 मिलते हैं।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 9,
                    text:
                        '6) छठा चरण (12वीं के बाद डिग्री/डिप्लोमा में प्रवेश पर) — ग्रेजुएशन या डिप्लोमा कोर्स में दाखिला लेने पर ₹5,000 मिलते हैं।',
                  ),
                  const SizedBox(height: 10),
                  _sectionHeader(context, Icons.key, '🔑 कुल लाभ'),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 10,
                    text:
                        'हर चरण को मिलाकर ₹15,000 की आर्थिक सहायता बेटी को दी जाती है और हर चरण पर आपको आवेदन कराना होता है',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          // आधिकारिक वेबसाइट (इन-ऐप वेबव्यू)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.public, 'आधिकारिक वेबसाइट पर देखें'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 11,
                    text: 'इस लिंक से आप योजना की आधिकारिक वेबसाइट पर विवरण देख सकते हैं।',
                  ),
                  const SizedBox(height: 10),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'कन्या सुमंगला - आधिकारिक वेबसाइट',
                              initialUrl: 'https://mksy.up.gov.in/women_welfare/citizen/guest_login.php',
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.open_in_new),
                      label: const Text('आधिकारिक साइट खोलें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
        ],
      ),
    );
  }
}

Widget _sectionHeader(BuildContext context, IconData icon, String title) {
  final theme = Theme.of(context);
  return Row(
    crossAxisAlignment: CrossAxisAlignment.center,
    children: [
      Icon(icon, color: theme.colorScheme.primary),
      const SizedBox(width: 8),
      Expanded(child: Text(title, style: theme.textTheme.titleMedium)),
    ],
  );
}

class _RibbonBadge extends StatelessWidget {
  const _RibbonBadge(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: Colors.transparent,
      elevation: 2,
      borderRadius: BorderRadius.circular(6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: theme.colorScheme.secondary,
          borderRadius: BorderRadius.circular(6),
        ),
        child: Text(
          text,
          style: theme.textTheme.labelSmall?.copyWith(
            color: theme.colorScheme.onSecondary,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }
}
