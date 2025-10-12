import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

class MukhyamantriRahatKoshScreen extends StatelessWidget {
  const MukhyamantriRahatKoshScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('मुख्यमंत्री राहत कोष')),
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
                children: const [
                  _SectionHeader(icon: Icons.info_outline, title: 'क्या है योजना -'),
                  SizedBox(height: 8),
                  Text(
                    'मुख्यमंत्री राहत कोष एक आपातकालीन आर्थिक सहायता कोष है, जिसे उत्तर प्रदेश सरकार और मुख्यमंत्री के माध्यम से ज़रूरतमंद व्यक्ति या परिवारों को गंभीर बीमारी, दुर्घटना या प्राकृतिक आपदा जैसी कठिन परिस्थितियों में दिया जाता है।',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/4143391268',
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  _SectionHeader(icon: Icons.verified_user, title: 'पात्रता -'),
                  SizedBox(height: 8),
                  Text(
                    'कोई भी उत्तर प्रदेश का मूल निवासी व्यक्ति जिसे गंभीर बीमारी ,या दुर्घटना सहायता ,प्राकर्तिक आपदा , मैं आर्थिक सहायता ले सकता है व्यक्ति की आय 1 लाख रुपये सालाना से अधिक न हो आय प्रमाण पत्र के हिसाब से , व जिनके पास आयुष्मान कार्ड न हो ',
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
                children: const [
                  _SectionHeader(icon: Icons.assignment_turned_in_outlined, title: 'आवेदन हेतु प्रपत्र -'),
                  SizedBox(height: 8),
                  Text(
                    'आय , जाति , निवास , प्रमाण पत्र आनलाइन वाला , बैंक कापी , आधार कार्ड , बीमारी से जुड़ी रिपोर्ट , अस्पताल द्वारा लागत (Estimate) फ़ार्म , जो आपको अस्पताल द्वारा दिया जाता है',
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
                  const _SectionHeader(icon: Icons.account_balance, title: 'आवेदन प्रक्रिया - दुर्घटना , प्राकर्तिक आपदा ,'),
                  const SizedBox(height: 8),
                  const _BulletItem('अपने क्षेत्र के मोजूदा विधायक के कार्यालय में संपर्क करें।'),
                  const _BulletItem('प्रक्रिया पूर्ण होने के बाद आर्थिक सहायता प्रदान की जाती है।'),
                  const _BulletItem('उदाहरण: गाँव में आग लग जाना, बाढ़ आ जाना, इत्यादि'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const _SectionHeader(icon: Icons.local_hospital, title: 'आवेदन प्रक्रिया - बीमारी सहायता हेतु  ,'),
                  const SizedBox(height: 8),
                  const Text(
                    'सबसे पहले आपको  यूपी सरकार द्वारा इस योजना से जुड़े हुए अस्पताल मैं जाकर OPD कराना होगा जिसमें डॉक्टर आपकी बीमारी से जुड़ी सभी जांच करवाएगा ,ताकि बीमारी कन्फर्म हो सके ,',
                  ),
                  const SizedBox(height: 6),
                  _AttentionText('उत्तर प्रदेश में जुड़े अस्पतालों की लिस्ट देखें'),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'हिन्दी मैं देखें',
                                  initialUrl: 'https://cmrf.up.gov.in/empanelledHospitalsDetails',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.open_in_new),
                          label: const Text('हिन्दी मैं देखें'),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'अंग्रेजी मैं देखें',
                                  initialUrl: 'https://cmrf.up.gov.in/empanelledHospitalsDetails?language=en_US',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.open_in_new),
                          label: const Text('अंग्रेजी मैं देखें'),
                        ),
                      ),
                    ],
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
                children: const [
                  _SectionHeader(icon: Icons.format_list_numbered, title: 'आगे की प्रक्रिया -'),
                  SizedBox(height: 8),
                  _BulletItem('1) अब जब सब जांच कनफर्म हो जाएगी तब आपको अस्पताल मैं मोजूद मुख्यमंत्री राहत कोष वाले काउन्टर पर जाकर अपना लागत (estimate) फ़ार्म बनवाना जिसमे मुख्य रूप से आपके इलाज मैं लगने वाली लागत का लिखित फ़ार्म आपको अस्पताल द्वारा मरीज के नाम से  मिलेगा'),
                  _BulletItem('2) अब उस लागत फ़ार्म को अपने क्षेत्र के विधायक कार्यालय ,मैं जमा करना होता है , बाकी कागजों के साथ जो पहले बताए गए हैं'),
                  _BulletItem('3) कागज जमा होने के उपरांत , विधायक लिखित मैं एक आवेदन मुख्यमंत्री कोष मैं लिखते हैं , जिसके बाद पैसे सीधा मरीज के खाते मैं आते हैं इसमे 15 दिन तक लग सकते है'),
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
                  const _SectionHeader(icon: Icons.contact_support, title: 'संपर्क करें '),
                  const SizedBox(height: 8),
                  const Text(
                    'आप चाहे तो नीचे दिए हुआ वेबसाईट के लिंक से मुख्यमंत्री राहत कोष से संपर्क कर सकते हैं जिसमें आपको ईमेल और और मोबाईल नंबर और पूरा पता शामिल है , आप बात करके भी पूरी जानकारी ले सकते हैं ',
                  ),
                  const SizedBox(height: 10),
                  Align(
                    alignment: Alignment.center,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'संपर्क करें',
                              initialUrl: 'https://cmrf.up.gov.in/contact',
                              enableFindInPage: true,
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.open_in_new),
                      label: const Text('संपर्क करें'),
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

class _BulletItem extends StatelessWidget {
  const _BulletItem(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(Icons.check_circle_outline, size: 18, color: theme.colorScheme.primary),
          const SizedBox(width: 8),
          Expanded(child: Text(text)),
        ],
      ),
    );
  }
}

class _AttentionText extends StatefulWidget {
  const _AttentionText(this.text);

  final String text;

  @override
  State<_AttentionText> createState() => _AttentionTextState();
}

class _AttentionTextState extends State<_AttentionText> with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final CurvedAnimation _curved;
  late Animation<Color?> _color;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
    _curved = CurvedAnimation(parent: _controller, curve: Curves.easeInOut);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final theme = Theme.of(context);
    _color = ColorTween(
      begin: theme.colorScheme.primary,
      end: theme.colorScheme.secondary,
    ).animate(_curved);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return AnimatedBuilder(
      animation: _color,
      builder: (context, _) {
        return Text(
          widget.text,
          textAlign: TextAlign.center,
          style: theme.textTheme.bodyMedium?.copyWith(
            fontWeight: FontWeight.w700,
            color: _color.value ?? theme.colorScheme.primary,
          ),
        );
      },
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
        Expanded(child: Text(title, style: theme.textTheme.titleMedium)),
      ],
    );
  }
}
