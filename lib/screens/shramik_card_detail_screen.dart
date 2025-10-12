import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/screens/shramik_support_form.dart';
import 'package:yojna_plus/screens/schemes/matru_shishu_balika_screen.dart';
import 'package:yojna_plus/screens/schemes/atal_awasiya_screen.dart';
import 'package:yojna_plus/screens/schemes/awasiya_school_screen.dart';
import 'package:yojna_plus/screens/schemes/kaushal_vikas_screen.dart';
import 'package:yojna_plus/screens/schemes/kanya_vivah_screen.dart';
import 'package:yojna_plus/screens/schemes/shauchalay_sahayta_screen.dart';
import 'package:yojna_plus/screens/schemes/aapda_rahat_screen.dart';
import 'package:yojna_plus/screens/schemes/mg_pension_screen.dart';
import 'package:yojna_plus/screens/schemes/gambhir_bimari_screen.dart';
import 'package:yojna_plus/screens/schemes/mrityu_divyang_screen.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

class ShramikCardDetailScreen extends StatelessWidget {
  const ShramikCardDetailScreen({super.key});

  static const List<String> _schemes = [
    'मातृत्व, शिशु एवं बालिका मदद योजना',
    'अटल आवासीय विद्यालय योजना',
    'आवासीय विद्यालय योजना',
    'कौशल विकास, तकनीकी उन्नयन एवं प्रमाणन योजना',
    'कन्या विवाह सहायता योजना',
    'शौचालय सहायता योजना',
    'आपदा राहत सहायता योजना',
    'महात्मा गाँधी पेन्शन योजना',
    'गम्भीर बीमारी सहायता योजना',
    'निर्माण कामगार मृत्यु व दिव्यांगता सहायता योजना',
  ];

  @override
  Widget build(BuildContext context) {
    Future<void> contactSupport() async {
      await showGeneralDialog(
        context: context,
        barrierLabel: 'ShramikSupportForm',
        barrierDismissible: true,
        barrierColor: Colors.black.withOpacity(0.25),
        transitionDuration: const Duration(milliseconds: 220),
        pageBuilder: (_, __, ___) => const ShramikSupportForm(),
        transitionBuilder: (context, anim, secondaryAnim, child) {
          final curved = Curves.easeOutCubic.transform(anim.value);
          return Opacity(
            opacity: anim.value,
            child: Transform.translate(
              offset: Offset(0, (1 - curved) * 24),
              child: child,
            ),
          );
        },
      );
    }

    return Scaffold(
      appBar: AppBar(title: const Text('UP BOCW श्रमिक कार्ड')),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          _ShramikSupportCard(onContact: contactSupport),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.info_outline, 'परिचय'),
                  const SizedBox(height: 8),
                  Text(
                    'उत्तर प्रदेश सरकार द्वारा जो रोज मजदूरी करते हैं उन सब के लिए ये योजना है जिसमें 40 तरह का काम शामिल है और हजारों रुपये का काम अनुदान श्रमिक कार्ड के माध्यम से दिए जाते हैं',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 12),
                  Align(
                    alignment: Alignment.center,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            fullscreenDialog: true,
                            builder: (_) => const WorkListFullScreen(),
                          ),
                        );
                      },
                      icon: const Icon(Icons.list_alt_outlined),
                      label: const Text('काम की सूची देखें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/5944198721',
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.verified_user, 'श्रमिक कार्ड हेतु पात्रता'),
                  const SizedBox(height: 8),
                  const Text(
                    'व्यक्ति मजदूर होना चाहिए जैसे खेती किसानी मजदूर इत्यादि और व्यक्ति या महिला की उम्र 18 साल से 60 साल के बीच की होनी चाहिए',
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
                  _sectionHeader(context, Icons.description_outlined, 'आवेदन'),
                  const SizedBox(height: 8),
                  const Text(
                    'नया आवेदन सिर्फ और सिर्फ CSC जन सेवा केंद्र के माध्यम से कराया जा सकता है अपने नजदीकी सेंटर पर जाए और कार्ड को हर साल रेनिवल करना होगा इसको ध्यान रखें',
                  ),
                  const SizedBox(height: 12),
                  Align(
                    alignment: Alignment.center,
                    child: ElevatedButton(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'CSC लोकेटर',
                              initialUrl: 'https://locator.csccloud.in/',
                            ),
                          ),
                        );
                      },
                      child: const Text('जन सेवा केंद्र ढूँढे'),
                    ),
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
                  _sectionHeader(context, Icons.list_alt_outlined, 'योजनाएँ'),
                  const SizedBox(height: 8),
                  ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: _schemes.length,
                    separatorBuilder: (_, __) => const Divider(height: 1),
                    itemBuilder: (context, index) {
                      final name = _schemes[index];
                      return ListTile(
                        title: Text(name),
                        trailing: OutlinedButton.icon(
                          onPressed: () => _showSchemeDialog(context, name),
                          icon: const Icon(Icons.info_outline),
                          label: const Text('जानकारी'),
                        ),
                        onTap: () => _showSchemeDialog(context, name),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 0, vertical: 2),
                        visualDensity: const VisualDensity(vertical: -1),
                      );
                    },
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
                  _sectionHeader(context, Icons.manage_search, 'आवेदन स्थिति जाँचें'),
                  const SizedBox(height: 8),
                  const Text('आपने जिन योजनाओं के लिए आवेदन किया है, उनकी स्थिति यहाँ देख सकते हैं।'),
                  const SizedBox(height: 12),
                  Align(
                    alignment: Alignment.center,
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'UP BOCW - आवेदन स्थिति',
                              initialUrl:
                                  'https://upbocw.in/Dynamic/PublicUser/SearchLabourProfile.aspx?Tab=4',
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.manage_search),
                      label: const Text('आवेदन स्थिति देखें'),
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

class _ShramikSupportCard extends StatelessWidget {
  const _ShramikSupportCard({required this.onContact});

  final Future<void> Function() onContact;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primary = theme.colorScheme.primary;
    const borderColor = Color(0xFFFF7043);
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: const LinearGradient(
          colors: [Color(0xFFFFA36C), Color(0xFFFF7043)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Container(
        margin: const EdgeInsets.all(1.5),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(18.5),
          color: theme.colorScheme.surface,
          boxShadow: [
            BoxShadow(
              color: primary.withOpacity(0.08),
              blurRadius: 18,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 22, 20, 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Align(
                alignment: Alignment.topLeft,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                  decoration: BoxDecoration(
                    color: borderColor,
                    borderRadius: BorderRadius.circular(30),
                  ),
                  child: Text(
                    'सुझावित',
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.w700,
                      letterSpacing: 0.4,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 18),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: primary.withOpacity(0.12),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(Icons.auto_awesome, color: primary),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'श्रमिक कार्ड बनवाएं',
                          style: theme.textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.w700,
                            height: 1.2,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'हज़ारों रुपयों का लाभ 9+ योजनाएँ इस कार्ड के द्वारा उत्तर प्रदेश सरकार द्वारा दी जाती हैं',
                          style: theme.textTheme.bodyMedium?.copyWith(height: 1.4),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              Align(
                alignment: Alignment.center,
                child: ElevatedButton.icon(
                  onPressed: () async {
                    await onContact();
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: primary,
                    foregroundColor: theme.colorScheme.onPrimary,
                    elevation: 0,
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
                  ),
                  icon: const Icon(Icons.headset_mic_outlined),
                  label: const Text('हमसे संपर्क करें'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

Widget _sectionHeader(BuildContext context, IconData icon, String title) {
  final theme = Theme.of(context);
  return Row(
    crossAxisAlignment: CrossAxisAlignment.center,
    children: [
      Icon(icon, color: theme.colorScheme.primary),
      const SizedBox(width: 8),
      Expanded(
        child: Text(
          title,
          style: theme.textTheme.titleMedium,
        ),
      ),
    ],
  );
}

  void _showSchemeDialog(BuildContext context, String schemeName) {
    // Route each scheme to its dedicated screen; fallback to placeholder dialog.
    Widget? screen;
    switch (schemeName) {
      case 'मातृत्व, शिशु एवं बालिका मदद योजना':
        screen = const MatruShishuBalikaScreen();
        break;
      case 'अटल आवासीय विद्यालय योजना':
        screen = const AtalAwasiyaScreen();
        break;
      case 'आवासीय विद्यालय योजना':
        screen = const AwasiyaSchoolScreen();
        break;
      case 'कौशल विकास, तकनीकी उन्नयन एवं प्रमाणन योजना':
        screen = const KaushalVikasScreen();
        break;
      case 'कन्या विवाह सहायता योजना':
        screen = const KanyaVivahScreen();
        break;
      case 'शौचालय सहायता योजना':
        screen = const ShauchalaySahaytaScreen();
        break;
      case 'आपदा राहत सहायता योजना':
        screen = const AapdaRahatScreen();
        break;
      case 'महात्मा गाँधी पेन्शन योजना':
        screen = const MGPensionScreen();
        break;
      case 'गम्भीर बीमारी सहायता योजना':
        screen = const GambhirBimariScreen();
        break;
      case 'निर्माण कामगार मृत्यु व दिव्यांगता सहायता योजना':
        screen = const MrityuDivyangScreen();
        break;
    }
    if (screen != null) {
      Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => screen!),
      );
      return;
    }
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(schemeName),
          content: const Text('इस योजना का विस्तृत विवरण जल्द जोड़ा जाएगा।'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('बंद करें'),
            ),
          ],
        );
      },
    );
  }


class WorkListFullScreen extends StatelessWidget {
  const WorkListFullScreen({super.key});

  static const String _heading = 'निर्माण कार्य जिनमें कार्यरत श्रमिक पंजीयन हेतु पात्र हैं -';

  static const List<String> _items = [
    'बेल्डिंग का कार्य',
    'बढ़ई का कार्य',
    'कुआँ खोदना',
    'रोलर चलाना',
    'छप्पर डालने का कार्य',
    'राजमिस्त्री का कार्य',
    'प्लम्बरिंग',
    'लोहार',
    'मोजैक पाॅलिश',
    'सड़क बनाना',
    'मिक्सर चलाने का कार्य',
    'पुताई',
    'इलेक्ट्रिक वर्क',
    'हथौड़ा चलाने का कार्य',
    'सुरंग निर्माण',
    'टाइल्स लगाने का कार्य',
    'कुएं से तलछट हटाने का कार्य',
    'चट्टान तोड़ने का कार्य या खनिकर्म',
    'वर्क-सड़क निर्माण से सम्बन्धित स्प्रे वर्क या मिक्सिंग',
    'मार्बल एवं स्टोन वर्क',
    'चौकीदारी - निर्माण सथल पर सुरक्षा प्रदान करने के लिये',
    'सभी प्रकार के पत्थर, तोड़ने व पीसने का कार्य',
    'निर्माण स्थल पर लिपिकीय व लेखा कार्य करने वाले कर्मकार',
    'सीमेन्ट, कंकरीट, ईंट ढोने का कार्य करने वाले',
    'बांध, पुल, सड़क निर्माण या भवन निर्माण से सम्बन्धित कोई संक्रिया',
    'बाढ़ प्रबन्धन',
    'ठंडा व गरम मशीनरी की स्थापना व मरम्मत',
    'अग्निशमन प्रणाली की स्थापना व मरम्मत',
    'बडे यांत्रिक कार्य - मशीनरी, पुल का निर्माण का कार्य',
    'मकानों/भवनों की आन्तरिक सज्जा का कार्य',
    'खिड़की, ग्रिल, दरवाजे आदि की गढ़ाई व स्थापना का कार्य',
    'माड्यूलर किचन की स्थापना',
    'सामुदायिक पार्क या फुटपाथ निर्माण',
    'ईंट भट्ठों पर ईट निर्माण कार्य',
    'मिट्टी, बालू, मौरंग खनन कार्य',
    'सुरक्षा द्वार व अन्य उपकरणों की स्थापना का कार्य',
    'लिफ्ट व स्वचालित सीढी की स्थापना का कार्य',
    'सीमेन्ट, ईंट आदि ढोने का कार्य',
    'मिट्टी का काम',
    'चूना बनाना',
  ];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: true,
        title: const Text('काम की सूची'),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                _heading,
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 12),
              Expanded(
                child: ListView.separated(
                  itemCount: _items.length,
                  separatorBuilder: (_, __) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final item = _items[index];
                    return ListTile(
                      leading: Icon(Icons.check_circle_outline, color: theme.colorScheme.primary),
                      title: Text(item),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 0, vertical: 2),
                      visualDensity: const VisualDensity(vertical: -1),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
