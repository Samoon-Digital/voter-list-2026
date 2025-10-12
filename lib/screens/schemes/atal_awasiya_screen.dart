import 'package:flutter/material.dart';
import 'package:yojna_plus/tts/tts_reader.dart';
import 'package:yojna_plus/tts/highlight_text.dart';

class AtalAwasiyaScreen extends StatefulWidget {
  const AtalAwasiyaScreen({super.key});

  @override
  State<AtalAwasiyaScreen> createState() => _AtalAwasiyaScreenState();
}

class _AtalAwasiyaScreenState extends State<AtalAwasiyaScreen> {
  final TtsReaderController _tts = TtsReaderController();

  // Ordered list of Hindi-only segments to be spoken and highlighted
  late final List<String> _segments = [
    // उद्देश्य
    'निर्माण श्रमिकों के बच्चों को उनकी पढ़ाई में आर्थिक सहायता प्रदान करना।',

    // पात्रता
    'माता-पिता: निर्माण श्रमिक बोर्ड में पंजीकृत और कम से कम 1 वर्ष (365 दिन) सक्रिय सदस्यता होनी चाहिए।',
    'बच्चों की संख्या: अधिकतम दो ही बच्चे इस सुविधा के पात्र होंगे।',
    'कक्षा 1–2: 6–8 वर्ष',
    'कक्षा 3–5: 8–11 वर्ष',
    'कक्षा 6–8: 11–14 वर्ष',
    'कक्षा 9–12: 14–18 वर्ष',
    'स्नातक / स्नातकोत्तर: 18–25 वर्ष',
    'MBBS / शोध / व्यावसायिक कोर्स: अधिकतम 35 वर्ष',
    'बच्चे का आधार प्रमाणीकरण (Aadhaar) अनिवार्य।',
    'जिस स्कूल / कॉलेज में पढ़ रहे हैं, वह मान्यता प्राप्त होना चाहिए।',

    // आवश्यक दस्तावेज़
    'पिछले वर्ष की मार्कशीट (Marksheet)।',
    'अगली कक्षा में प्रवेश की फीस रसीद।',
    '(यदि कक्षा 1–8) स्कूल का स्वप्रमाणित अंक पत्र।',
    '(कक्षा 9–12/व्यावसायिक) स्कूल/कॉलेज का अधिकृत हस्ताक्षरित बाउचर या Dean/Principal द्वारा प्रमाणित दस्तावेज़।',
    'पिछले 12 महीनों में कम से कम 90 दिनों का निर्माण कार्य का प्रमाण (स्वघोषणा या नियोजन प्रमाणपत्र)।',
    'घोषणा कि किसी समान योजना का लाभ पहले से नहीं लिया जा रहा।',

    // मिलने वाले लाभ
    'कक्षा 1–5: ₹ 2,000 (एकमुश्त)',
    'कक्षा 6–10: ₹ 2,500',
    'कक्षा 11–12: ₹ 3,000',
    'कक्षा 9–12 उत्तीर्ण करने के बाद अगली कक्षा में प्रवेश पर एक बार के लिए साइकिल खरीद में सब्सिडी।',
    'स्नातक / वैकल्पिक पाठ्यक्रम: ₹ 12,000 एकमुश्त।',
    'आईटीआई / पॉलिटेक्निक / व्यावसायिक पाठ्यक्रम: ₹ 12,000।',
    'प्रोफेशनल कोर्स (जैसे MBA, Engineering आदि): फीस या ₹ 60,000 में जो कम हो — वह प्रदान किया जाएगा।',
    'मास्टर डिग्री (स्नातकोत्तर): ₹ 24,000 एकमुश्त।',
    'MBBS / अन्य मेडिकल / IIM / IIT / NIT / NIFT / NLU आदि शीर्ष संस्थानों में पढ़ाई: फीस की 100% प्रतिपूर्ति (आयु सीमा 35 वर्ष तक शिथिल)।',
    'व्यावसायिक/शैक्षणिक शोध हेतु: ₹ 1,00,000 एकमुश्त।',
    'कक्षा 10/12 में ≥ 70% अंक: लड़के ₹ 5,000, लड़कियाँ ₹ 8,000।',
    'स्नातक/पोस्टग्रेजुएशन में ≥ 60% अंक: लड़के ₹ 10,000, लड़कियाँ ₹ 12,000 (अगली कक्षा में प्रवेश अनिवार्य नहीं)।',

    // महत्वपूर्ण नोट्स
    'लाभ केवल उन बच्चों को मिलेगा जिनके माता-पिता निर्माण श्रमिक बोर्ड में पंजीकृत हैं और कम से कम एक वर्ष सदस्यता पूरी कर चुके हैं।',
    'बच्चा उसी कक्षा/कोर्स में अध्ययनरत होना चाहिए जिसके लिए सहायता मांगी गई है।',
    'प्रति परिवार अधिकतम दो बच्चों को ही इस योजना का लाभ मिलेगा।',
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
        title: const Text('अटल आवासीय विद्यालय योजना'),
        actions: [
          AnimatedBuilder(
            animation: _tts,
            builder: (context, _) {
              final color = _tts.isSpeaking ? Theme.of(context).colorScheme.secondary : null;
              return Padding(
                padding: const EdgeInsetsDirectional.only(end: 20),
                child: IconButton(
                  tooltip: 'आवाज़',
                  icon: Icon(Icons.volume_up_rounded, color: color),
                  onPressed: _tts.toggleSpeak,
                ),
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
          // उद्देश्य
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'योजना का उद्देश्य'),
                  _bullet(context, 'निर्माण श्रमिकों के बच्चों को उनकी पढ़ाई में आर्थिक सहायता प्रदान करना।', controller: _tts, segmentIndex: 0),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // पात्रता
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.verified_user, 'पात्रता (Eligibility)'),
                  _bullet(context, 'माता-पिता: निर्माण श्रमिक बोर्ड में पंजीकृत और कम से कम 1 वर्ष (365 दिन) सक्रिय सदस्यता होनी चाहिए।', controller: _tts, segmentIndex: 1),
                  _bullet(context, 'बच्चों की संख्या: अधिकतम दो ही बच्चे इस सुविधा के पात्र होंगे।', controller: _tts, segmentIndex: 2),
                  _subTitle(context, 'बच्चों की आयु (01 जुलाई 기준)'),
                  _bullet(context, 'कक्षा 1–2: 6–8 वर्ष', indent: 18, controller: _tts, segmentIndex: 3),
                  _bullet(context, 'कक्षा 3–5: 8–11 वर्ष', indent: 18, controller: _tts, segmentIndex: 4),
                  _bullet(context, 'कक्षा 6–8: 11–14 वर्ष', indent: 18, controller: _tts, segmentIndex: 5),
                  _bullet(context, 'कक्षा 9–12: 14–18 वर्ष', indent: 18, controller: _tts, segmentIndex: 6),
                  _bullet(context, 'स्नातक / स्नातकोत्तर: 18–25 वर्ष', indent: 18, controller: _tts, segmentIndex: 7),
                  _bullet(context, 'MBBS / शोध / व्यावसायिक कोर्स: अधिकतम 35 वर्ष', indent: 18, controller: _tts, segmentIndex: 8),
                  _subTitle(context, 'अन्य शर्तें'),
                  _bullet(context, 'बच्चे का आधार प्रमाणीकरण (Aadhaar) अनिवार्य।', controller: _tts, segmentIndex: 9),
                  _bullet(context, 'जिस स्कूल / कॉलेज में पढ़ रहे हैं, वह मान्यता प्राप्त होना चाहिए।', controller: _tts, segmentIndex: 10),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आवश्यक दस्तावेज़
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवश्यक दस्तावेज़ (Required Documents)'),
                  _bullet(context, 'पिछले वर्ष की मार्कशीट (Marksheet)।', controller: _tts, segmentIndex: 11),
                  _bullet(context, 'अगली कक्षा में प्रवेश की फीस रसीद।', controller: _tts, segmentIndex: 12),
                  _bullet(context, '(यदि कक्षा 1–8) स्कूल का स्वप्रमाणित अंक पत्र।', controller: _tts, segmentIndex: 13),
                  _bullet(context, '(कक्षा 9–12/व्यावसायिक) स्कूल/कॉलेज का अधिकृत हस्ताक्षरित बाउचर या Dean/Principal द्वारा प्रमाणित दस्तावेज़।', controller: _tts, segmentIndex: 14),
                  _bullet(context, 'पिछले 12 महीनों में कम से कम 90 दिनों का निर्माण कार्य का प्रमाण (स्वघोषणा या नियोजन प्रमाणपत्र)।', controller: _tts, segmentIndex: 15),
                  _bullet(context, 'घोषणा कि किसी समान योजना का लाभ पहले से नहीं लिया जा रहा।', controller: _tts, segmentIndex: 16),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // मिलने वाले लाभ
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.card_giftcard, 'मिलने वाले लाभ (Benefits)'),
                  _subTitle(context, 'विद्यालय के लिए'),
                  _bullet(context, 'कक्षा 1–5: ₹ 2,000 (एकमुश्त)', controller: _tts, segmentIndex: 17),
                  _bullet(context, 'कक्षा 6–10: ₹ 2,500', controller: _tts, segmentIndex: 18),
                  _bullet(context, 'कक्षा 11–12: ₹ 3,000', controller: _tts, segmentIndex: 19),

                  _subTitle(context, 'साइकिल सहायता'),
                  _bullet(context, 'कक्षा 9–12 उत्तीर्ण करने के बाद अगली कक्षा में प्रवेश पर एक बार के लिए साइकिल खरीद में सब्सिडी।', controller: _tts, segmentIndex: 20),

                  _subTitle(context, 'उच्च शिक्षा'),
                  _bullet(context, 'स्नातक / वैकल्पिक पाठ्यक्रम: ₹ 12,000 एकमुश्त।', controller: _tts, segmentIndex: 21),
                  _bullet(context, 'आईटीआई / पॉलिटेक्निक / व्यावसायिक पाठ्यक्रम: ₹ 12,000।', controller: _tts, segmentIndex: 22),
                  _bullet(context, 'प्रोफेशनल कोर्स (जैसे MBA, Engineering आदि): फीस या ₹ 60,000 में जो कम हो — वह प्रदान किया जाएगा।', controller: _tts, segmentIndex: 23),
                  _bullet(context, 'मास्टर डिग्री (स्नातकोत्तर): ₹ 24,000 एकमुश्त।', controller: _tts, segmentIndex: 24),
                  _bullet(context, 'MBBS / अन्य मेडिकल / IIM / IIT / NIT / NIFT / NLU आदि शीर्ष संस्थानों में पढ़ाई: फीस की 100% प्रतिपूर्ति (आयु सीमा 35 वर्ष तक शिथिल)।', controller: _tts, segmentIndex: 25),
                  _bullet(context, 'व्यावसायिक/शैक्षणिक शोध हेतु: ₹ 1,00,000 एकमुश्त।', controller: _tts, segmentIndex: 26),

                  _subTitle(context, 'प्रोत्साहन राशि (Merit Reward)'),
                  _bullet(context, 'कक्षा 10/12 में ≥ 70% अंक: लड़के ₹ 5,000, लड़कियाँ ₹ 8,000।', controller: _tts, segmentIndex: 27),
                  _bullet(context, 'स्नातक/पोस्टग्रेजुएशन में ≥ 60% अंक: लड़के ₹ 10,000, लड़कियाँ ₹ 12,000 (अगली कक्षा में प्रवेश अनिवार्य नहीं)।', controller: _tts, segmentIndex: 28),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // महत्वपूर्ण नोट्स
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'महत्वपूर्ण नोट्स'),
                  _bullet(context, 'लाभ केवल उन बच्चों को मिलेगा जिनके माता-पिता निर्माण श्रमिक बोर्ड में पंजीकृत हैं और कम से कम एक वर्ष सदस्यता पूरी कर चुके हैं।', controller: _tts, segmentIndex: 29),
                  _bullet(context, 'बच्चा उसी कक्षा/कोर्स में अध्ययनरत होना चाहिए जिसके लिए सहायता मांगी गई है।', controller: _tts, segmentIndex: 30),
                  _bullet(context, 'प्रति परिवार अधिकतम दो बच्चों को ही इस योजना का लाभ मिलेगा।', controller: _tts, segmentIndex: 31),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

Widget _subTitle(BuildContext context, String text) {
  final theme = Theme.of(context);
  return Padding(
    padding: const EdgeInsets.only(top: 8, bottom: 6),
    child: Text(
      text,
      style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
    ),
  );
}

Widget _sectionHeader(BuildContext context, IconData icon, String title) {
  final theme = Theme.of(context);
  return Padding(
    padding: const EdgeInsets.only(bottom: 6),
    child: Row(
      children: [
        Icon(icon, size: 20, color: theme.colorScheme.primary),
        const SizedBox(width: 8),
        Text(
          title,
          style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
        ),
      ],
    ),
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
