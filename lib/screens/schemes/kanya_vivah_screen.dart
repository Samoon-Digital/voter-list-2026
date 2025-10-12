import 'package:flutter/material.dart';
 

class KanyaVivahScreen extends StatelessWidget {
  const KanyaVivahScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('कन्या विवाह सहायता योजना')),
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
                  _bullet(context, 'यह योजना केवल पंजीकृत निर्माण श्रमिकों के लिए है, और पंजीकरण के बाद कम से कम 365 दिन बोर्ड सदस्यता पूरी करनी होगी।'),
                  _bullet(context, 'शादी एक वर्ष के भीतर पूरी करनी होगी; सामूहिक विवाह की स्थिति में आवेदन कम-से-कम 15 दिन पहले करना आवश्यक।'),
                  _bullet(context, 'केवल दो बेटियों तक ही लाभ देय।'),
                  _bullet(context, 'बेटी की न्यूनतम आयु 18 वर्ष और वर की 21 वर्ष होना अनिवार्य (सरकारी नियमों के अनुसार)।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // दस्तावेज़
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'दस्तावेज़ (Documents)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'बेटी और वर का आयु प्रमाण (जन्म प्रमाण पत्र/स्कूल-लीविंग सर्टिफिकेट/परिवार रजिस्टर) — स्वप्रमाणित प्रति।'),
                  _bullet(context, 'विवाह प्रमाण — विवाह कार्ड आदि, जिसे ग्राम प्रधान/तहसीलदार/सभासद प्रमाणित करें।'),
                  _bullet(context, 'राशन कार्ड या परिवार रजिस्टर — स्वप्रमाणित प्रति।'),
                  _bullet(context, 'विवाह समारोह का फोटो — श्रमिक द्वारा प्रमाणित।'),
                  _bullet(context, 'पिछले 12 महीनों में कम से कम 90 दिन निर्माण कार्य करने का प्रमाण (नियोजन/स्वघोषणा प्रमाणपत्र)।'),
                  _bullet(context, 'घोषणा पत्र — किसी अन्य समान राज्य/केंद्र योजना का लाभ न लेने का।'),
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
                  _bullet(context, 'सामान्य विवाह (बेटी/महिला श्रमिक): ₹55,000।'),
                  _bullet(context, 'अंतर्जातीय विवाह: ₹61,000।'),
                  _bullet(context, 'सामूहिक विवाह (कम-से-कम 11 जोड़े): ₹65,000 प्रति बेटी।'),
                  _bullet(context, 'आयोजनकर्ता हेतु ₹7,000 प्रति जोड़ा।'),
                  _bullet(context, 'वर और वधू के कपड़ों हेतु प्रति व्यक्ति ₹5,000 — शादी से एक सप्ताह पूर्व सीधे खाते में; अनुपस्थिति की स्थिति में अग्रिम का समायोजन।'),
                  _bullet(context, 'महिला निर्माण श्रमिक की स्वयं की शादी: उपर्युक्त लाभ, बशर्ते माता/पिता ने पहले इसी मद में लाभ न लिया हो।'),
                  _bullet(context, 'पुनर्विवाह (विधिक तलाक/पति की मृत्यु): लाभ तभी, जब न्यायालय का निर्णय/मृत्यु प्रमाण उपलब्ध हो — लाभ सामूहिक विवाह के अनुरूप।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // अतिरिक्त जानकारी
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'अतिरिक्त जानकारी (Additional Updates)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'मुख्यमंत्री सामूहिक विवाह योजना में सहायता ₹51,000 से बढ़ाकर ₹1,00,000 — ₹60,000 नववधू के खाते में, ₹25,000 घरेलू उपहारों हेतु, ₹15,000 आयोजन खर्च।'),
                  _bullet(context, 'हालिया दिशा-निर्देश: आय, वर्ग और आधार-बायोमेट्रिक सत्यापन अनिवार्य।'),
                  _bullet(context, 'स्रोत: Indiatimes, Navbharat Times'),
                  _bullet(context, 'लागू क्षेत्र/उदाहरण: गाज़ियाबाद (Ghaziabad)'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

        
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
