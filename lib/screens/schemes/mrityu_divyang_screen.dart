import 'package:flutter/material.dart';

class MrityuDivyangScreen extends StatelessWidget {
  const MrityuDivyangScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('निर्माण कामगार मृत्यु व दिव्यांगता सहायता योजना')),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          // योजना का उद्देश्य
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'योजना का उद्देश्य (Objective)'),
                  const SizedBox(height: 8),
                  _bullet(
                    context,
                    'यह योजना उत्तर प्रदेश में पंजीकृत निर्माण श्रमिकों (भवन व अन्य निर्माण कार्यों में लगे) और उनके आश्रितों को दुर्घटना, बीमारी या दिव्यांगता की स्थिति में तत्काल आर्थिक सहायता देने हेतु चलाई जाती है।',
                  ),
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
                  const SizedBox(height: 8),
                  _bullet(context, 'श्रमिक को राज्य में पंजीकृत और अद्यतन (renewed) होना चाहिए।'),
                  _bullet(context, 'दुर्घटना या बीमारी के कारण श्रमिक दिव्यांग हो जाए या मृत्यु हो जाए, तो आश्रित/सहयोगी पात्र बनते हैं।'),
                  _bullet(context, 'आत्महत्या के मामलों में सहायता नहीं दी जाती।'),
                  _bullet(context, 'हत्या, सांप दंश, बिजली गिरना, प्रसव के समय मृत्यु या प्राकृतिक आपदा में हुई मृत्यु को सामान्य मृत्यु माना जाता है।'),
                  _bullet(context, 'आश्रित का आधार वेरिफिकेशन आवेदन पर आवश्यक है।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आश्रित कौन-कौन
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.family_restroom, 'कौन-कौन आश्रित हो सकते हैं?'),
                  const SizedBox(height: 8),
                  _bullet(context, 'पहले पति या पत्नी'),
                  _bullet(context, 'फिर वयस्क पुत्र/अविवाहित पुत्री'),
                  _bullet(context, 'फिर माता/पिता'),
                  _bullet(context, 'और अंत में अवयस्क बच्चे (यदि उपर्युक्त सभी नहीं हैं)'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // लाभ
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.account_balance_wallet_outlined, 'मृत्यु या दिव्यांगता पर मिलने वाला लाभ (Benefits)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'दुर्घटना से मृत्यु (पंजीकृत श्रमिक): ₹25,000 अंत्येष्टि + ₹5 लाख लाभ (लगभग ₹9,395 प्रति माह, 5 साल तक)'),
                  _bullet(context, 'सामान्य मृत्यु (पंजीकृत): ₹25,000 अंत्येष्टि + ₹2 लाख लाभ (लगभग ₹8,736 प्रति माह, 2 साल तक)'),
                  _bullet(context, 'दुर्घटना से मृत्यु (अ-पंजीकृत श्रमिक): ₹1 लाख एकमुश्त सहायता'),
                  _bullet(context, 'पूर्ण दिव्यांगता (100%): ₹4 लाख (लगभग ₹9,172 प्रति माह, 4 साल तक)'),
                  _bullet(context, 'अधिकतम 99% दिव्यांगता: ₹3 लाख (3 वर्ष तक)'),
                  _bullet(context, '25–49% दिव्यांगता: ₹2 लाख (2 वर्ष तक)'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आवश्यक दस्तावेज
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवश्यक दस्तावेज (Documents)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'श्रमिक का रेजिस्ट्रीकरण (पंजीकरण नंबर)'),
                  _bullet(context, 'ऑनलाइन जारी मृत्यु प्रमाणपत्र (मृत्यु के मामले में)'),
                  _bullet(context, 'FIR / पंचनामा / पोस्टमार्टम रिपोर्ट (दुर्घटना में मृत्यु के मामले में)'),
                  _bullet(context, 'आधार कार्ड और इससे लिंक बैंक पासबुक की कॉपी (आवेदक की)'),
                  _bullet(context, 'दिव्यांगता के मामलों में मुख्य चिकित्सा अधिकारी द्वारा प्रमाण पत्र या चिकित्सा सर्टिफिकेट'),
                  _bullet(context, 'स्वघोषणा पत्र कि कोई अन्य योजना का लाभ नहीं लिया हुआ है'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आवेदन कैसे करें
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.app_registration, 'कैसे करें आवेदन? (How to Apply)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'ऑनलाइन: upbocw.in पर जाकर पहले पंजीकरण, फिर योजना के लिए आवेदन, जरूरी जानकारी भरें और दस्तावेज अपलोड करके सबमिट करें।'),
                  _bullet(context, 'ऑफलाइन: निकटतम श्रम कार्यालय, तहसील दफ़्तर या विकासखंड कार्यालय से फॉर्म प्राप्त कर, भरकर आवश्यक दस्तावेजों के साथ वहाँ जमा करें।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // समय सीमा
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.schedule, 'निर्णय की समय सीमा (Timeline)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'अधिकारी आवेदन प्राप्ति के बाद 15 दिनों में मंजूरी या अस्वीकार का निर्णय देंगे; यदि आवेदन अस्वीकार होता है तो कारण बताना आवश्यक है।'),
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
                  _sectionHeader(context, Icons.lightbulb_outline, 'संक्षेप (In short)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'यदि आप उत्तर प्रदेश में पंजीकृत निर्माण श्रमिक हैं और दुर्घटना/बीमारी/दिव्यांगता/मृत्यु जैसी स्थिति हो, तो इस योजना के तहत परिजनों को ₹1 लाख से लेकर ₹5.25 लाख तक की आर्थिक सहायता मिल सकती है, जिसमें मासिक किश्तें और एकमुश्त अंत्येष्टि सहायता दोनों शामिल हैं। आवेदन ऑनलाइन या नज़दीकी जन सेवा/श्रम कार्यालय में कराया जा सकता है।'),
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
