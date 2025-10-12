import 'package:flutter/material.dart';
import 'package:yojna_plus/tts/tts_reader.dart';
import 'package:yojna_plus/tts/highlight_text.dart';

class AapdaRahatScreen extends StatefulWidget {
  const AapdaRahatScreen({super.key});

  @override
  State<AapdaRahatScreen> createState() => _AapdaRahatScreenState();
}

class _AapdaRahatScreenState extends State<AapdaRahatScreen> {
  final TtsReaderController _tts = TtsReaderController();

  late final List<String> _segments = [
    // पात्रता
    'यह योजना केवल अद्यतन रूप से पंजीकृत निर्माण श्रमिकों के लिए है।',
    'यह योजना COVID-19 महामारी को ध्यान में रखते हुए बनाई गई थी।',
    'इसके लिए किसी आवेदन पत्र की आवश्यकता नहीं है — यह पूरी तरह पेपर-लेस स्कीम है।',
    'लाभार्थी का आधार नंबर और बैंक खाता विवरण पहले से डेटाबेस में होना चाहिए — कोई नया आवेदन आवश्यक नहीं।',
    // प्रक्रिया और दस्तावेज़
    'यह एक पूर्ण रूप से डिजिटल और स्वचालित योजना है — कोई फॉर्म नहीं भरना, कोई दस्तावेज़ जमा नहीं करना।',
    'आवेदन पत्र की ज़रूरत नहीं — डेटाबेस में मौजूद जानकारी ही आधार होती है।',
    'यह सुनिश्चित करें कि आधार से आपका बैंक खाता लिंक हो और यह विवरण पहले से दर्ज हो।',
    // लाभ राशि
    'अद्यतन पंजीकृत निर्माण श्रमिकों को एकमुश्त ₹1,000 की मदद मिलती है।',
    'यह राशि सालाना/अर्धवार्षिक/त्रैमासिक/मासिक के रूप में, जैसा कि केंद्र/राज्य सरकार या बोर्ड तय करे, सीधे बैंक खाते में भेजी जाती है।',
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
        title: const Text('आपदा राहत सहायता योजना'),
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
          // पात्रता
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.verified_user, 'पात्रता (Eligibility)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'यह योजना केवल अद्यतन रूप से पंजीकृत निर्माण श्रमिकों के लिए है।', controller: _tts, segmentIndex: 0),
                  _bullet(context, 'यह योजना COVID-19 महामारी को ध्यान में रखते हुए बनाई गई थी।', controller: _tts, segmentIndex: 1),
                  _bullet(context, 'इसके लिए किसी आवेदन पत्र की आवश्यकता नहीं है — यह पूरी तरह पेपर-लेस स्कीम है।', controller: _tts, segmentIndex: 2),
                  _bullet(context, 'लाभार्थी का आधार नंबर और बैंक खाता विवरण पहले से डेटाबेस में होना चाहिए — कोई नया आवेदन आवश्यक नहीं।', controller: _tts, segmentIndex: 3),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // प्रक्रिया और दस्तावेज़
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.settings_suggest, 'प्रक्रिया और दस्तावेज़ (Process & Documents)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'यह एक पूर्ण रूप से डिजिटल और स्वचालित योजना है — कोई फॉर्म नहीं भरना, कोई दस्तावेज़ जमा नहीं करना।', controller: _tts, segmentIndex: 4),
                  _bullet(context, 'आवेदन पत्र की ज़रूरत नहीं — डेटाबेस में मौजूद जानकारी ही आधार होती है।', controller: _tts, segmentIndex: 5),
                  _bullet(context, 'यह सुनिश्चित करें कि आधार से आपका बैंक खाता लिंक हो और यह विवरण पहले से दर्ज हो।', controller: _tts, segmentIndex: 6),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // लाभ राशि
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.account_balance_wallet_outlined, 'लाभ राशि (Financial Benefit)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'अद्यतन पंजीकृत निर्माण श्रमिकों को एकमुश्त ₹1,000 की मदद मिलती है।', controller: _tts, segmentIndex: 7),
                  _bullet(context, 'यह राशि सालाना/अर्धवार्षिक/त्रैमासिक/मासिक के रूप में, जैसा कि केंद्र/राज्य सरकार या बोर्ड तय करे, सीधे बैंक खाते में भेजी जाती है।', controller: _tts, segmentIndex: 8),
                ],
              ),
            ),
          ),
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

Widget _bullet(BuildContext context, String text, {double indent = 0, TtsReaderController? controller, int? segmentIndex}) {
  final theme = Theme.of(context);
  return Padding(
    padding: EdgeInsets.only(bottom: 6, left: indent),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('•', style: theme.textTheme.bodyMedium),
        const SizedBox(width: 8),
        Expanded(
          child: (controller != null && segmentIndex != null)
              ? HighlightableText(
                  controller: controller,
                  segmentIndex: segmentIndex,
                  text: text,
                )
              : Text(text),
        ),
      ],
    ),
  );
}

