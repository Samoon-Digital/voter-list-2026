import 'package:flutter/material.dart';

class MGPensionScreen extends StatelessWidget {
  const MGPensionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('महात्मा गाँधी पेन्शन योजना')),
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
                  _bullet(context, 'इस योजना के लिए उत्तर प्रदेश में स्थायी रूप से रहने वाले पंजीकृत निर्माण श्रमिकों को ही चुना जाएगा, जिनकी आयु कम से कम 60 वर्ष हो चुकी हो।'),
                  _bullet(context, 'श्रमिक का पंजीकरण कम से कम 10 वर्ष पहले होना चाहिए।'),
                  _bullet(context, 'यदि कोई व्यक्ति राज्य या केन्द्र सरकार की किसी अन्य पेंशन योजना का लाभ उठा रहा है (राज्य कर्मचारी बीमा निगम और मुख्यमंत्री योजना छोड़कर), तो वह इसमें पात्र नहीं होगा।'),
                  _bullet(context, 'पेंशन का निर्णय जिलाधिकारी की अध्यक्षता वाली समिति करेगी, और भुगतान सीधे बोर्ड द्वारा किया जाएगा।'),
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
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवश्यक दस्तावेज़ (Documents Needed)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'यह दिखाने वाला साक्ष्य कि आपने 60 वर्ष की आयु होने तक अपने पंजीकरण के अनुसार किश्तें जमा कर रखी हैं।'),
                  _bullet(context, 'आधार कार्ड, बैंक पासबुक (पहला पेज) और निवास प्रमाण पत्र की साफ-स्वच्छ कॉपियाँ लिए जाने जरूरी हैं।'),
                  _bullet(context, 'यह सत्यापित करने के लिए कि आप किसी अन्य पेंशन योजना का लाभ नहीं ले रहे हैं, शपथ-पत्र देना अनिवार्य होगा।'),
                  _bullet(context, 'हर साल अप्रैल महीने में आपको जीवित प्रमाण पत्र जमा कराना होगा।'),
                  _bullet(context, 'यदि पेंशनधारी का निधन हो जाता है, तो उसके परिवार वालों को एक माह के भीतर जिले के श्रम कार्यालय को सूचना देनी होगी।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // पेंशन लाभ
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.account_balance_wallet_outlined, 'पेंशन लाभ (Pension Benefits)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'हर महीने ₹1,000 की पेंशन दी जाएगी।'),
                  _bullet(context, 'यदि श्रमिक की मृत्यु हो जाती है, तो यह पेंशन उसकी पत्नी या पति को जारी रहेगी — स्थिति चाहे जैसी भी हो।'),
                  _bullet(context, 'पेंशन राशि में हर दो वर्षों बाद ₹50 की वृद्धि होगी, लेकिन अधिकतम सीमा ₹1,250 तक ही होगी।'),
                  _bullet(context, 'जो श्रमिक इस पेंशन योजना के साथ प्रधानमंत्री श्रमयोगी मानधन योजना में भी पंजीकृत हैं, उनका अंशदान बोर्ड द्वारा वहन किया जाएगा।'),
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
