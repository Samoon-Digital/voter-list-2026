import 'package:flutter/material.dart';

class KaushalVikasScreen extends StatelessWidget {
  const KaushalVikasScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('कौशल विकास, तकनीकी उन्नयन एवं प्रमाणन योजना')),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          // क्या है यह योजना?
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'क्या है यह योजना?'),
                  const SizedBox(height: 8),
                  _bullet(
                    context,
                    'यदि कोई निर्माण मजदूर (दीवार, सड़क आदि बनाने वाला) या उसका परिवार नया हुनर, कंप्यूटर या कोई तकनीकी कोर्स सीखना चाहे — तो यह योजना प्रशिक्षण और सहायता प्रदान करती है।',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // पात्रता: कौन-कौन शामिल हो सकते हैं
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.people_outline, 'कौन-कौन इसमें शामिल हो सकते हैं?'),
                  const SizedBox(height: 8),
                  _bullet(context, 'निर्माण श्रमिक या उनका पति/पत्नी/पिता निर्माण श्रमिक बोर्ड में पंजीकृत हो और अंशदान नियमित रूप से जमा हो।'),
                  _bullet(context, 'यदि श्रमिक स्वयं सीखना चाहता है: आयु 18–35 वर्ष के बीच।'),
                  _bullet(context, 'पत्नी: किसी भी आयु में पात्र (कोई आयु सीमा नहीं)।'),
                  _bullet(context, 'अविवाहित बेटी: किसी भी आयु में पात्र (कोई आयु सीमा नहीं)।'),
                  _bullet(context, 'बेटा: आयु 21 वर्ष से अधिक नहीं होनी चाहिए।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आवश्यक साक्ष्य / दस्तावेज़
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'कौन-से साक्ष्य देने होंगे?'),
                  const SizedBox(height: 8),
                  _bullet(context, 'रजिस्ट्रेशन कार्ड / पंजीयन प्रमाण पत्र।'),
                  _bullet(context, 'अंशदान जमा होने का प्रमाण (योगदान अद्यतन/नियमित)।'),
                  _bullet(context, 'सीखने वाले कार्य/कोर्स का आवेदन पत्र।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // लाभ / सहायता
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.card_giftcard, 'आपको क्या मिलता है?'),
                  const SizedBox(height: 8),
                  _bullet(context, 'मुफ्त प्रशिक्षण — यानी प्रशिक्षण फीस नहीं लगेगी।'),
                  _bullet(context, 'प्रशिक्षण के बाद न्यूनतम मजदूरी के बराबर मानदेय/स्टाइपेंड (काम जैसा वेतन)।'),
                  _bullet(context, 'प्रशिक्षण के दौरान आवश्यक किताबें, फीस और लेखन सामग्री बोर्ड द्वारा उपलब्ध।'),
                  _bullet(context, 'प्रशिक्षण समाप्ति पर आकलन/मूल्यांकन परीक्षा (Assessment Exam) देनी होगी।'),
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
