import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

class MukhyamantriSamuhikVivahScreen extends StatelessWidget {
  const MukhyamantriSamuhikVivahScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('मुख्यमंत्री सामूहिक विवाह योजना')),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          // सुझाव कार्ड सूची के अंत में ले जाया गया
          // योजना का उद्देश्य/परिचय
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  _SectionHeader(icon: Icons.info_outline, title: 'योजना का उद्देश्य/परिचय'),
                  SizedBox(height: 8),
                  _Paragraph(
                    'उत्तर प्रदेश सरकार ने राज्य के आर्थिक रूप से कमजोर परिवार की बेटियों के विवाह तथा विधवा/तलाकशुदा महिला के पुनर्विवाह में सहायता करने के लिए उत्तर प्रदेश मुख्यमंत्री सामूहिक विवाह योजना की शुरुआत की है। इस योजना के तहत प्रत्येक लाभार्थी युगल के विवाह पर प्रदेश सरकार कुल रु० 51,000/- धनराशि व्यय की जाती है।',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/2816855392',
          ),
          const SizedBox(height: 12),
          // पात्रता
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  _SectionHeader(icon: Icons.verified_user, title: 'पात्रता'),
                  SizedBox(height: 8),
                  _Bullet('उम्र — आवेदक कन्या/महिला की आयु 18 वर्ष या इससे अधिक और वर की आयु 21 वर्ष या इससे अधिक होनी चाहिए।'),
                  _Bullet('वर्ग — अनुसूचित जाति/अनुसूचित जनजाति/अन्य पिछड़ा वर्ग/सामान्य/अल्पसंख्यक।'),
                  _Bullet('आय — परिवार की वार्षिक आय रु० 2 लाख तक होनी चाहिए।'),
                  _Bullet('मूल निवास — आवेदिका उत्तर प्रदेश की मूल निवासी हो।'),
                  SizedBox(height: 6),
                  _SubHeader('विशेष मानदण्ड'),
                  SizedBox(height: 6),
                  _Bullet('इस योजना का लाभ उन्हीं कन्याओं को दिया जाएगा, जिनका विवाह तय हो गया है व पूर्व में विवाह नहीं किया है।', indent: 16),
                  _Bullet('वे महिलाएं जिनका कानूनी रूप से तलाक हो चुका है, लाभ लेने की पात्र हैं।', indent: 16),
                  _Bullet('विधवा महिलाएं भी इस योजना का लाभ लेने की पात्र हैं।', indent: 16),
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
                children: const [
                  _SectionHeader(icon: Icons.account_balance_wallet_outlined, title: 'लाभ'),
                  SizedBox(height: 8),
                  _Paragraph('इस योजना के अन्तर्गत उत्तर प्रदेश सरकार द्वारा प्रति युगल रु० 51,000.00 की आर्थिक सहायता प्रदान की जाती है —'),
                  SizedBox(height: 6),
                  _Bullet('रु० 35,000.00 कन्या के बैंक खाते में अन्तरित किए जाते हैं।', indent: 16),
                  _Bullet('रु० 10,000.00 के वैवाहिक उपहार वर-वधू को विवाह के समय उपलब्ध कराए जाते हैं।', indent: 16),
                  _Bullet('रु० 6,000.00 विवाह के समारोहपूर्वक आयोजन (बिजली, पानी, पण्डाल, भोजन आदि) पर व्यय।', indent: 16),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आवश्यकताएँ
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  _SectionHeader(icon: Icons.assignment_turned_in_outlined, title: 'आवश्यकताएँ (Documents)'),
                  SizedBox(height: 8),
                  _Bullet('आधार कार्ड (वर व वधू)।'),
                  _Bullet('कन्या के परिवार का आय प्रमाण पत्र।'),
                  _Bullet('निवास प्रमाण पत्र।'),
                  _Bullet('जाति प्रमाण पत्र (अनुसूचित जाति/अनुसूचित जनजाति/अन्य पिछड़ा वर्ग की दशा में)।'),
                  _Bullet('वर-वधू की फोटो।'),
                  _Bullet('मोबाइल नंबर।'),
                  _Bullet('बैंक खाता विवरण।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          // जरूरी सुझाव कार्ड (नीचे)
          Stack(
            clipBehavior: Clip.none,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'आप नजदीकी जन सेवा केंद्र से आवेदन कर सकते हैं यह सुरक्षित तरीका है। यदि आप चाहें तो स्वयं भी नीचे दिए हुए लिंक से आवेदन कर सकते हैं।',
                      ),
                      const SizedBox(height: 10),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'आवेदन लिंक',
                                  initialUrl: 'https://cmsvy.upsdc.gov.in/applicationforms.php',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.open_in_new),
                          label: const Text('आवेदन लिंक खोलें'),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const Positioned(
                top: -2,
                right: 6,
                child: _RibbonBadge('जरूरी सुझाव'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionHeader(icon: Icons.question_answer_outlined, title: 'सवाल-जवाब (FAQ)'),
                  const SizedBox(height: 8),
                  const _SubHeader('आवेदन पत्र फाइनल सबमिट करने के बाद कहां जाता है?'),
                  const SizedBox(height: 4),
                  const _Paragraph('आवेदन पत्र जिला समाज कल्याण अधिकारी के लॉग इन पर जाता है।'),
                  const SizedBox(height: 8),
                  const _SubHeader('आवेदन पत्र का प्रिंट निकालने के बाद कहां जमा करना होता है?'),
                  const SizedBox(height: 4),
                  const _Paragraph('आवेदन पत्र का प्रिंट निकालने के बाद जिला समाज कल्याण कार्यालय में जमा करना होता है।'),
                  const SizedBox(height: 8),
                  const _SubHeader('लाभ प्राप्त करने के लिए किससे सम्पर्क करना होगा?'),
                  const SizedBox(height: 4),
                  const _Bullet('ग्रामीण क्षेत्र के आवेदकों को खंड विकास अधिकारी कार्यालय से सम्पर्क करना होगा'),
                  const _Bullet('नगर क्षेत्र के आवेदकों को आवेदन संबंधित नगर निकायों से सम्पर्क करना होगा'),
                  const _Bullet('समाज कल्याण विभाग हेल्पलाइन नंबर-14568 डायल कर अपनी समस्या दूर कर सकते हैं।'),
                  const SizedBox(height: 8),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(Icons.info_outline, color: Theme.of(context).colorScheme.primary),
                      const SizedBox(width: 8),
                      const Expanded(
                        child: Text(
                          'नोट: हेल्पलाइन नम्बर 9:30 AM से 06:00 PM (सोमवार से शनिवार, सार्वजनिक अवकाश को छोड़कर) कार्यालय समय में ही काम करेगा।',
                        ),
                      ),
                    ],
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

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.icon, required this.title});
  final IconData icon;
  final String title;
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Icon(icon, color: theme.colorScheme.primary),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
          ),
        ),
      ],
    );
  }
}

class _Paragraph extends StatelessWidget {
  const _Paragraph(this.text);
  final String text;
  @override
  Widget build(BuildContext context) {
    return Text(text);
  }
}

class _SubHeader extends StatelessWidget {
  const _SubHeader(this.text);
  final String text;
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Text(
      text,
      style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
    );
  }
}

class _Bullet extends StatelessWidget {
  const _Bullet(this.text, {this.indent = 0});
  final String text;
  final double indent;
  @override
  Widget build(BuildContext context) {
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

class _RibbonBadge extends StatelessWidget {
  const _RibbonBadge(this.text);
  final String text;
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: Colors.transparent,
      elevation: 2,
      borderRadius: BorderRadius.circular(6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: theme.colorScheme.secondary,
          borderRadius: BorderRadius.circular(6),
        ),
        child: Text(
          text,
          style: theme.textTheme.labelSmall?.copyWith(
            color: theme.colorScheme.onSecondary,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }
}
