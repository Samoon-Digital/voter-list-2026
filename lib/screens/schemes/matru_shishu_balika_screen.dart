import 'package:flutter/material.dart';

class MatruShishuBalikaScreen extends StatelessWidget {
  const MatruShishuBalikaScreen({super.key});

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

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('मातृत्व, शिशु एवं बालिका मदद योजना'),
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
                  _sectionHeader(context, Icons.verified_user, 'पात्रता (Eligibility)'),
                  _bullet(context, 'निर्माण श्रमिक, जिन्होंने पंजीकरण के बाद कम से कम 365 दिन (1 वर्ष) बोर्ड में सदस्यता पूरी कर ली हो।'),
                  _bullet(context, 'मातृत्व व शिशु सहायता लाभ पहली दो बार (दो प्रसव तक) ही दिए जाएंगे।'),
                  _bullet(context, 'महिला होने पर मातृत्व लाभ केवल संस्थागत प्रसव (अस्पताल/मातृत्व केंद्र) में ही मिलेगा।'),
                  _bullet(context, 'बालिका सहायता योजना में लाभ तब मिलेगा:'),
                  _bullet(context, 'यदि पहली संतान लड़की हो,', indent: 18),
                  _bullet(context, 'या दूसरी संतान भी लड़की हो,', indent: 18),
                  _bullet(context, 'या दंपत्ति के निःसंतान होने पर कानूनी रूप से गोद ली गयी लड़की हो।', indent: 18),
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
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवश्यक दस्तावेज़ (Required Documents)'),
                  _bullet(context, 'अद्यतन पंजीकरण (Updated registration)'),
                  _bullet(context, 'सरकारी अस्पताल में संस्थागत प्रसव / गर्भपात / नसबंदी का प्रमाणपत्र'),
                  _bullet(context, 'ऑनलाइन जारी किया गया जन्म प्रमाण-पत्र'),
                  _bullet(context, 'वैधानिक गोदनामा (अगर गोद ली गयी हो)'),
                  _bullet(context, 'परिवार रजिस्टर, आधार कार्ड और बैंक पासबुक की प्रतियाँ'),
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
                  _sectionHeader(context, Icons.card_giftcard, 'मिलने वाले लाभ (Benefits)'),
                  _subTitle(context, 'मातृत्व लाभ (Maternity Benefits)'),
                  _bullet(context, 'पुरुष मजदूर (जो पंजीकृत हैं): एकमुश्त ₹6,000 मिलेगा।'),
                  _bullet(context, 'महिला मजदूर:'),
                  _bullet(context, 'संस्थागत प्रसव पर: तीन महीने के न्यूनतम वेतन के बराबर + ₹1,000 चिकित्सा बोनस।', indent: 18),
                  _bullet(context, 'गर्भपात (मिसकैरेज) पर: छह सप्ताह के बराबर न्यूनतम वेतन।', indent: 18),
                  _bullet(context, 'नसबंदी पर: दो सप्ताह के बराबर न्यूनतम वेतन।', indent: 18),
                  const SizedBox(height: 8),
                  _subTitle(context, 'शिशु (बच्चे) लाभ'),
                  _bullet(context, 'यदि शिशु लड़का हो: ₹20,000 एकमुश्त।'),
                  _bullet(context, 'यदि शिशु लड़की हो: ₹25,000 एकमुश्त।'),
                  const SizedBox(height: 8),
                  _subTitle(context, 'बालिका सहायता (Savings for Girl Child)'),
                  _bullet(context, 'निम्न स्थितियों में ₹25,000 सावधि जमा (Fixed Deposit) के रूप में मिलेगा:'),
                  _bullet(context, 'यदि पहला संतान लड़की हो,', indent: 18),
                  _bullet(context, 'या दूसरी संतान भी लड़की हो,', indent: 18),
                  _bullet(context, 'या दंपत्ति ने कानूनी रूप से एक लड़की को गोद लिया हो।', indent: 18),
                  _bullet(context, 'यदि जन्म से दिव्यांग लड़की हो: ₹50,000 सावधि जमा।'),
                  const SizedBox(height: 8),
                  Text(
                    'ध्यान दें: यह राशि केवल तब मिलेगी जब बच्ची अविवाहित रहेगी और 18 वर्ष की उम्र तक सावधि पूरी हो। यदि शर्तें पूरी नहीं होतीं, तो कोई धनराशि नहीं दी जाएगी।',
                    style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.error),
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
                  _sectionHeader(context, Icons.notes, 'सारांश'),
                  _bullet(context, 'कब मिलता है लाभ: पंजीकरण + 365 दिन बोर्ड सदस्यता पूरी होना ज़रूरी।'),
                  _bullet(context, 'मातृत्व लाभ: पहली दो बार — पुरुष को ₹6,000; महिला को संस्थागत प्रसव पर तीन माह का न्यूनतम वेतन + ₹1,000 चिकित्सा बोनस।'),
                  _bullet(context, 'शिशु का लाभ: लड़के को ₹20,000, लड़की को ₹25,000।'),
                  _bullet(context, 'बालिका बचत राशि: पहली/दूसरी/गोद ली गयी लड़की के लिए ₹25,000 सावधि जमा; जन्म से दिव्यांग लड़की के लिए ₹50,000 — 18 वर्ष तक अविवाहित रहने व सावधि पूरी होने पर ही देय।'),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
