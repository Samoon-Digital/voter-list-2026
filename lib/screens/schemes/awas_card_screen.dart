import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/tts/highlight_text.dart';
import 'package:yojna_plus/tts/tts_reader.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

class AwasCardScreen extends StatefulWidget {
  const AwasCardScreen({super.key});

  @override
  State<AwasCardScreen> createState() => _AwasCardScreenState();
}

class _AwasCardScreenState extends State<AwasCardScreen> {
  final TtsReaderController _tts = TtsReaderController();

  late final List<String> _segments = [
    'प्रधानमंत्री आवास योजना (शहरी) का आवास कार्ड लाभार्थियों को स्वीकृति, किश्त और आकलन की स्थिति एक जगह देखने की सुविधा देता है।',
    'कार्ड उन्हीं परिवारों को मिलता है जिनकी वार्षिक आय शहरी क्षेत्रों में ₹3 लाख (ईडब्ल्यूएस) या ₹6 लाख (एलआईजी) से कम है तथा परिवार के पास पक्का घर नहीं होना चाहिए।',
    'आवेदन के लिए आधार कार्ड, परिवार के सभी सदस्यों की पहचान, बैंक पासबुक, मोबाइल नंबर, आय प्रमाणपत्र और निवास प्रमाणपत्र तैयार रखें।',
    'आधिकारिक पोर्टल पर लॉगिन करें, फॉर्म में पारिवारिक विवरण भरें, दस्तावेज़ अपलोड करें और अंतिम सबमिशन से पहले पूर्वावलोकन अवश्य जाँचें।',
    'सबमिशन के बाद आवास कार्ड डाउनलोड करें; इसमें आवेदन आईडी, स्वीकृति चरण और किस्त की जानकारी शामिल होती है।',
    'यदि ऑनलाइन आवेदन संभव नहीं है तो नगर पालिका या सीएससी सेंटर पर जाकर बायोमेट्रिक सत्यापन के साथ फॉर्म भरें।',
    'कार्ड की स्थिति 30 से 45 दिनों के अंतराल पर जाँचते रहें; यदि अपडेट न मिले तो स्थानीय आवास मिशन कार्यालय से संपर्क करें।',
    'नीचे दिए गए लिंक से आप आधिकारिक वेबसाइट पर सीधे कार्ड की स्थिति देख सकते हैं।',
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
        title: const Text('प्रधानमंत्री आवास कार्ड'),
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
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'आवास कार्ड क्या है?'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 0,
                    text:
                        'प्रधानमंत्री आवास योजना (शहरी) का आवास कार्ड लाभार्थियों को स्वीकृति, किश्त और आकलन की स्थिति एक जगह देखने की सुविधा देता है।',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/1708799616',
          ),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.verified_user_outlined, 'पात्रता और आवश्यक दस्तावेज़'),
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
                              'कार्ड उन्हीं परिवारों को मिलता है जिनकी वार्षिक आय शहरी क्षेत्रों में ₹3 लाख (ईडब्ल्यूएस) या ₹6 लाख (एलआईजी) से कम है तथा परिवार के पास पक्का घर नहीं होना चाहिए।',
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(Icons.assignment_outlined, color: Theme.of(context).colorScheme.primary, size: 18),
                      const SizedBox(width: 8),
                      Expanded(
                        child: HighlightableText(
                          controller: _tts,
                          segmentIndex: 2,
                          text:
                              'आवेदन के लिए आधार कार्ड, परिवार के सभी सदस्यों की पहचान, बैंक पासबुक, मोबाइल नंबर, आय प्रमाणपत्र और निवास प्रमाणपत्र तैयार रखें।',
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.laptop_chromebook, 'ऑनलाइन आवेदन प्रक्रिया'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 3,
                    text:
                        'आधिकारिक पोर्टल पर लॉगिन करें, फॉर्म में पारिवारिक विवरण भरें, दस्तावेज़ अपलोड करें और अंतिम सबमिशन से पहले पूर्वावलोकन अवश्य जाँचें।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 4,
                    text:
                        'सबमिशन के बाद आवास कार्ड डाउनलोड करें; इसमें आवेदन आईडी, स्वीकृति चरण और किस्त की जानकारी शामिल होती है।',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.support_agent, 'ऑफ़लाइन सहायता और ट्रैकिंग'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 5,
                    text:
                        'यदि ऑनलाइन आवेदन संभव नहीं है तो नगर पालिका या सीएससी सेंटर पर जाकर बायोमेट्रिक सत्यापन के साथ फॉर्म भरें।',
                  ),
                  const SizedBox(height: 6),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 6,
                    text:
                        'कार्ड की स्थिति 30 से 45 दिनों के अंतराल पर जाँचते रहें; यदि अपडेट न मिले तो स्थानीय आवास मिशन कार्यालय से संपर्क करें।',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.public, 'आधिकारिक वेबसाइट पर जाएँ'),
                  const SizedBox(height: 8),
                  HighlightableText(
                    controller: _tts,
                    segmentIndex: 7,
                    text:
                        'नीचे दिए गए लिंक से आप आधिकारिक वेबसाइट पर सीधे कार्ड की स्थिति देख सकते हैं।',
                  ),
                  const SizedBox(height: 10),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'PMAY कार्ड स्थिति',
                              initialUrl: 'https://pmaymis.gov.in/',
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.open_in_new),
                      label: const Text('वेबसाइट खोलें'),
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
