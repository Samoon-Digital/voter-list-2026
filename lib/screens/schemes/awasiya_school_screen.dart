import 'package:flutter/material.dart';

class AwasiyaSchoolScreen extends StatelessWidget {
  const AwasiyaSchoolScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('आवासीय विद्यालय योजना')),
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
                  _bullet(context, 'माता-पिता: निर्माण श्रमिक बोर्ड में पंजीकृत और पंजीयन के बाद कम से कम 5 वर्ष की सदस्यता पूर्ण होनी चाहिए।'),
                  _bullet(context, 'लाभार्थी बच्चे: अधिकतम दो बच्चे इस योजना के तहत पात्र होंगे।'),
                  _bullet(context, 'अनाथ बच्चे: अनाथ/विधवा/बेसहारा बच्चों को भी योजना में शामिल किया गया है।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // विद्यालय संचालन की रूपरेखा
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.school, 'विद्यालय संचालन की रूपरेखा (School Setup)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'पाठ्यक्रम CBSE (सीबीएसई) के अनुरूप होगा, माध्यम अंग्रेज़ी — नवोदय विद्यालय की तर्ज़ पर।'),
                  _bullet(context, 'प्रत्येक विद्यालय में कुल 1000 छात्र — 500 लड़के और 500 लड़कियाँ।'),
                  _bullet(context, 'विद्यालय पूर्णतः आवासीय (Boarding) होंगे — छात्र-छात्राएँ कैंपस में रहकर पढ़ेंगे।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // प्रवेश प्रक्रिया
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.how_to_reg, 'प्रवेश प्रक्रिया (Admission Process)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'हर वर्ष प्रवेश परीक्षा आयोजित होगी।'),
                  _bullet(context, 'परीक्षा उत्तीर्ण अभ्यर्थियों का चयन मेरिट (स्कोर) के आधार पर किया जाएगा।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // सुविधाएँ
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.check_circle_outline, 'सुविधाएँ (Facilities Provided)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'आरामदायक और सुरक्षित छात्रावास (Hostel)'),
                  _bullet(context, 'खान-पान, खेलकूद, चिकित्सा देखभाल, सुरक्षा'),
                  _bullet(context, 'स्कूल ड्रेस, कॉपी-किताबें, स्कूल बैग'),
                  _bullet(context, 'स्मार्ट क्लासेज, कंप्यूटर लैब, करियर काउंसलिंग'),
                  _bullet(context, 'मनोरंजन और खेलकूद की अन्य सुविधाएँ'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // संक्षेप
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.notes, 'संक्षेप में'),
                  const SizedBox(height: 8),
                  _bullet(context, 'माता-पिता: बोर्ड में कम से कम 5 वर्ष की सदस्यता अनिवार्य।'),
                  _bullet(context, 'अधिकतम दो बच्चे (या अनाथ) पात्र।'),
                  _bullet(context, 'सीबीएसई पाठ्यक्रम, अंग्रेज़ी माध्यम, नवोदय की तर्ज़ पर संचालन।'),
                  _bullet(context, 'प्रत्येक स्कूल की क्षमता: 1000 (500 लड़के + 500 लड़कियाँ)।'),
                  _bullet(context, 'प्रवेश: वार्षिक परीक्षा + मेरिट के आधार पर।'),
                  _bullet(context, 'सुविधाएँ: पूर्णतः मुफ्त बोर्डिंग, पढ़ाई, खान-पान, रहना, शिक्षा-सामग्री, चिकित्सा, सुरक्षा और मनोरंजन।'),
                ],
              ),
            ),
          ),
        ],
      ),
    );
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

  Widget _bullet(BuildContext context, String text, {double indent = 0}) {
    final theme = Theme.of(context);
    return Padding(
      padding: EdgeInsets.only(bottom: 6, left: indent),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('•', style: theme.textTheme.bodyMedium),
          const SizedBox(width: 8),
          Expanded(child: Text(text)),
        ],
      ),
    );
  }
}
