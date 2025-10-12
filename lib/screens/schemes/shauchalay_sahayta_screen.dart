import 'package:flutter/material.dart';

class ShauchalaySahaytaScreen extends StatelessWidget {
  const ShauchalaySahaytaScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('शौचालय सहायता योजना')),
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
                  _bullet(context, 'योजना केवल अद्यतन पंजीकृत निर्माण श्रमिकों के लिए है।'),
                  _bullet(context, 'श्रमिक का अपने नाम पर अपना आवास होना चाहिए, जहाँ शौचालय की सुविधा नहीं हो।'),
                  _bullet(context, 'यदि किसी अन्य सरकारी योजना के अंतर्गत शौचालय निर्माण की सहायता पहले मिल चुकी हो, तो लाभार्थी पात्र नहीं होगा।'),
                  _bullet(context, 'परिवार को एक इकाई मानकर इस योजना में शामिल किया जाएगा।'),
                  _bullet(context, 'लाभार्थी का बैंक खाता राष्ट्र्रीकृत बैंक की CBS (Core Banking System) शाखा में होना अनिवार्य है।'),
                  _bullet(context, 'बैंक खाता आधार से लिंक होना भी आवश्यक है।'),
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
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवश्यक दस्तावेज़ (Documents)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'पंजीकरण की अद्यतन प्रमाण-पत्र।'),
                  _bullet(context, 'घोषणा पत्र: आपने किसी अन्य योजना से शौचालय निर्माण का लाभ नहीं लिया है और आपके पास पक्का मकान नहीं है।'),
                  _bullet(context, 'आधार कार्ड और बैंक पासबुक (पहला पेज) की कॉपी — बैंक खाते की जानकारी स्पष्ट हो।'),
                  _bullet(context, 'ये सभी दस्तावेज़ जिला पंचायत राज अधिकारी (DPRO) के माध्यम से योजना में लागू होंगे।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // लाभ राशि और भुगतान
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.account_balance_wallet_outlined, 'लाभ राशि और भुगतान (Financial Benefit & Payment)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'कुल राशि ₹12,000 — दो बराबर किस्तों में देय।'),
                  _bullet(context, 'पहली किस्त: ₹6,000 — प्रोत्साहन अग्रिम के रूप में।'),
                  _bullet(context, 'दूसरी किस्त: शौचालय का निर्माण पूरा होने और उपयोग प्रारम्भ होने पर, जिला पंचायत राज अधिकारी के माध्यम से।'),
                  _bullet(context, 'लाभार्थियों का चयन: DPRO द्वारा पंजीकृत श्रमिकों की सूची की पुष्टि (baseline survey) के आधार पर।'),
                  _bullet(context, 'भुगतान का तरीका: राशि सीधे लाभार्थी के बैंक खाते में हस्तान्तरित की जाएगी।'),
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
