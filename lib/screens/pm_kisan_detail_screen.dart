import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:yojna_plus/widgets/movie_ticket_native_ad.dart';

class PmKisanDetailScreen extends StatelessWidget {
  const PmKisanDetailScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('पीएम - किसान'),
      ),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          Stack(
            clipBehavior: Clip.none,
            children: [
              // Static gradient border with strong shadow (no animation)
              Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  gradient: LinearGradient(
                    colors: [
                      theme.colorScheme.primary,
                      theme.colorScheme.secondary,
                    ],
                  ),
                ),
                child: Container(
                  margin: const EdgeInsets.all(2),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.surface,
                    borderRadius: BorderRadius.circular(14),
                    boxShadow: [
                      BoxShadow(
                        color: theme.colorScheme.primary.withOpacity(0.18),
                        blurRadius: 14,
                        spreadRadius: 0.5,
                        offset: const Offset(0, 6),
                      ),
                    ],
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Text('✨', style: TextStyle(fontSize: 18)),
                            const SizedBox(width: 6),
                            Flexible(
                              child: Text(
                                'पीएम किसान से जुड़ी समस्याओं का समाधान पाएँ — एकदम सटीक',
                                style: theme.textTheme.titleLarge?.copyWith(
                                  fontWeight: FontWeight.w900,
                                  height: 1.2,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        Center(
                          child: ElevatedButton.icon(
                            onPressed: () {
                              _showSupportOverlay(context);
                            },
                            style: ElevatedButton.styleFrom(
                              backgroundColor: theme.colorScheme.primary,
                              foregroundColor: theme.colorScheme.onPrimary,
                              padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 14),
                              shape: const StadiumBorder(),
                              elevation: 2,
                            ),
                            icon: const Icon(Icons.support_agent_rounded),
                            label: const Text('हमसे सहायता ले'),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              Positioned(
                top: -10,
                left: 10,
                child: _RibbonBadge('सुझावित'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.fromLTRB(12, 9, 12, 0),
            decoration: const BoxDecoration(
              color: Color(0xFF3674B5),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: const [
                Text(
                  'विज्ञापन',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontWeight: FontWeight.w800,
                    fontSize: 19,
                    color: Colors.white,
                  ),
                ),
                SizedBox(height: 0),
                NativeBannerAd(),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: ImportantNoticeCard(
                text:
                    'जिनका पहले से आवेदन हैं और कुछ किश्तें आन के बाद पैसा मिलना बंद हो गया उसको हम स्टेप बी स्टेप ठीक करेंगे घर बैठे अपने मोबाईल से ध्यान दें इस स्टेप्स को पूरा करने के लिए आपके आधार कार्ड मैं मोबाईल नंबर जुड़ा होना चाहिए',
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14),
              side: BorderSide(color: theme.colorScheme.outline),
            ),
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  ImportantNoticeCard(
                    title: 'वीडियो देखना जरूरी है',
                    text:
                        'पहले आप इस वीडियो  को देख  लो इस मैं स्टेप बाय स्टेप  एप को इस्तेमाल करना और पीएम की समस्या का समाधान कैसे करना  है हिन्दी मैं पूरा बताया गया है उसके बाद आगे बढ़े',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('वीडियो लिंक जल्द जोड़े जाएंगे')),
                        );
                      },
                      icon: const Icon(Icons.play_circle_fill_outlined),
                      label: const Text('वीडियो देखें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          StepActionCard(
            stepLabel: 'पहला कदम',
            title: 'अपना रजिस्ट्रेशन नंबर जानें',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const InAppWebViewPage(
                    title: 'अपना रजिस्ट्रेशन नंबर जानें',
                    initialUrl: 'https://pmkisan.gov.in/KnowYour_Registration.aspx',
                  ),
                ),
              );
            },
          ),
          StepActionCard(
            stepLabel: 'दूसरा कदम',
            title: 'PM-KISAN की स्थिति जाँचें',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const InAppWebViewPage(
                    title: 'PM-KISAN की स्थिति जाँचें',
                    initialUrl: 'https://pmkisan.gov.in/BeneficiaryStatus_New.aspx',
                  ),
                ),
              );
            },
          ),
          StepActionCard(
            stepLabel: 'तीसरा कदम',
            title: 'अपना आधार सीडिंग चेक करें',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const InAppWebViewPage(
                    title: 'अपना आधार सीडिंग चेक करें',
                    initialUrl: 'https://www.npci.org.in/',
                    automationProfile: 'npci_base',
                    automationDelay: Duration(milliseconds: 900),
                  ),
                ),
              );
            },
          ),
          StepActionCard(
            stepLabel: 'चौथा कदम',
            title: 'अपना मोबाइल नंबर बदलें',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const InAppWebViewPage(
                    title: 'अपना मोबाइल नंबर बदलें',
                    initialUrl: 'https://pmkisan.gov.in/MobileUpdation_Pub.aspx',
                  ),
                ),
              );
            },
          ),
          StepActionCard(
            stepLabel: 'पाँचवाँ कदम',
            title: 'पीएम किसान KYC करें',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const InAppWebViewPage(
                    title: 'पीएम किसान KYC',
                    initialUrl: 'https://pmkisan.gov.in/aadharekyc.aspx',
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.app_registration, 'नया आवेदन'),
                  const SizedBox(height: 8),
                  const Text('- पीएम किसान नया आवेदन के लिए जरूरी कागज -'),
                  const SizedBox(height: 4),
                  const Text('खतौनी ,आधार कार्ड , बैंक कापी  लेकर अपने नजदीकी जनसेवा केंद्र पर जाकर आवेदन करवाएं'),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'जनसेवा केंद्र ढूँढें',
                                  initialUrl: 'https://locator.csccloud.in/',
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.location_on_outlined),
                          label: const Text('जनसेवा केंद्र ढूँढें'),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: ElevatedButton(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'आवेदन की स्थिति',
                                  initialUrl: 'https://pmkisan.gov.in/FarmerStatus.aspx',
                                ),
                              ),
                            );
                          },
                          style: ElevatedButton.styleFrom(
                            alignment: Alignment.center,
                          ),
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Row(
                                mainAxisSize: MainAxisSize.min,
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: const [
                                  Icon(Icons.fact_check_outlined),
                                  SizedBox(width: 6),
                                  Text('आवेदन की '),
                                ],
                              ),
                              const SizedBox(height: 2),
                              const Text(
                                'स्थिति',
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
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

// Full-screen support overlay dialog
void _showSupportOverlay(BuildContext context) {
  showGeneralDialog(
    context: context,
    barrierLabel: 'SupportOverlay',
    barrierDismissible: true,
    barrierColor: Colors.black.withOpacity(0.25),
    transitionDuration: const Duration(milliseconds: 220),
    pageBuilder: (_, __, ___) => const _FullScreenSupportDialog(),
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

 // Dialog widgets moved to top-level below

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
}

class _FullScreenSupportDialog extends StatefulWidget {
  const _FullScreenSupportDialog();

  @override
  State<_FullScreenSupportDialog> createState() => _FullScreenSupportDialogState();
}

class _FullScreenSupportDialogState extends State<_FullScreenSupportDialog> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _mobileController = TextEditingController();

  bool _submitting = false;
  @override
  void dispose() {
    _nameController.dispose();
    _mobileController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    setState(() => _submitting = true);
    try {
      await FirebaseFirestore.instance.collection('support_requests').add({
        'name': _nameController.text.trim(),
        'mobile': _mobileController.text.trim(),
        'source': 'pm_kisan_support',
        'status': 'pending',
        'createdAt': FieldValue.serverTimestamp(),
      });
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('धन्यवाद! आपका विवरण जमा हो गया।')),
      );
      Navigator.of(context).maybePop();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('सबमिट करने में समस्या: $e')),
      );
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }
  @override
  Widget build(BuildContext context) {
    const c1 = Color(0xFFE53935); // red
    const c2 = Color(0xFF4FC3F7); // sky blue
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          // Gradient background
          Positioned.fill(
            child: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [c1, c2],
                ),
              ),
            ),
          ),
          // Fog-like soft white patches
          const Positioned.fill(child: _FogPatches()),

          // Close button
          SafeArea(
            child: Align(
              alignment: Alignment.topRight,
              child: Padding(
                padding: const EdgeInsets.all(30),
                child: Material(
                  color: Colors.white24,
                  shape: const CircleBorder(),
                  child: IconButton(
                    icon: const Icon(Icons.close),
                    color: Colors.white,
                    onPressed: () => Navigator.of(context).maybePop(),
                    tooltip: 'बंद करें',
                  ),
                ),
              ),
            ),
          ),

          // Main content
          Positioned.fill(
            child: SafeArea(
              child: Center(
                child: SingleChildScrollView(
                  padding: EdgeInsets.fromLTRB(
                    20,
                    20,
                    20,
                    24 + MediaQuery.of(context).viewInsets.bottom,
                  ),
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 520),
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.black.withOpacity(0.22),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: Colors.white.withOpacity(0.18)),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // 99 rupees + blue tick header
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: const [
                                Icon(Icons.verified_rounded, color: Colors.lightBlueAccent, size: 28),
                                SizedBox(width: 8),
                                Text(
                                  ' 99₹ मात्र',
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 22,
                                    fontWeight: FontWeight.w900,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 14),

                            // Bullet points / details
                            _PointLine('फोन काल के माध्यम से बातचीत करके आप की इन समस्यों का समाधान बताया जाएगा'),
                            _PointLine('आधार सीडिंग सुविधा'),
                            _PointLine('पीएम किसान गड़बड़ी का सुधार'),
                            _PointLine('रुकी हुई किश्तें'),
                            _PointLine('बिल्कुल सटीक व सही जानकारी बस आप एक काल दूर हैं'),
                            const SizedBox(height: 8),
                            const Center(
                              child: Text(
                                'हमसे संपर्क करें',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                            ),

                            const SizedBox(height: 14),

                            // Form
                            Form(
                              key: _formKey,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  TextFormField(
                                    controller: _nameController,
                                    keyboardType: TextInputType.name,
                                    textInputAction: TextInputAction.next,
                                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700),
                                    decoration: _inputDecoration(theme, hint: 'अपना नाम डाले'),
                                    validator: (v) {
                                      if (v == null || v.trim().isEmpty) {
                                        return 'कृपया अपना नाम लिखें';
                                      }
                                      return null;
                                    },
                                  ),
                                  const SizedBox(height: 12),
                                  TextFormField(
                                    controller: _mobileController,
                                    keyboardType: TextInputType.phone,
                                    inputFormatters: [
                                      FilteringTextInputFormatter.digitsOnly,
                                      LengthLimitingTextInputFormatter(10),
                                    ],
                                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700),
                                    decoration: _inputDecoration(theme, hint: 'अपना मोबाईल नंबर डाले'),
                                    validator: (v) {
                                      final s = v?.trim() ?? '';
                                      if (s.isEmpty) return 'कृपया मोबाइल नंबर लिखें';
                                      if (s.length != 10) return '10 अंकों का मोबाइल नंबर लिखें';
                                      return null;
                                    },
                                  ),
                                  const SizedBox(height: 16),
                                  ElevatedButton.icon(
                                    onPressed: _submitting ? null : _submit,
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: theme.colorScheme.primary,
                                      foregroundColor: theme.colorScheme.onPrimary,
                                      minimumSize: const Size.fromHeight(52),
                                      padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 16),
                                      textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                                      elevation: 3,
                                    ),
                                    icon: const Icon(Icons.send_rounded),
                                    label: Text(_submitting ? 'जमा हो रहा है...' : 'फ़ार्म जमा करे'),
                                  ),
                                  const SizedBox(height: 8),
                                  const Text(
                                    'नोट : फार्म जमा करने के बाद हम आपको जल्द से जल्द काल करेंगे अभी आपको कोई भुगतान नहीं करना है',
                                    style: TextStyle(color: Colors.white, fontSize: 12.5),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  InputDecoration _inputDecoration(ThemeData theme, {required String hint}) {
    return InputDecoration(
      hintText: hint,
      hintStyle: const TextStyle(color: Colors.white70),
      filled: true,
      fillColor: Colors.white.withOpacity(0.10),
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: Colors.white.withOpacity(0.24)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: theme.colorScheme.secondary.withOpacity(0.9)),
      ),
      errorStyle: const TextStyle(color: Colors.white),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Colors.white),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Colors.white),
      ),
    );
  }
}

class _FogPatches extends StatelessWidget {
  const _FogPatches();

  @override
  Widget build(BuildContext context) {
    Color fog(double o) => Colors.white.withOpacity(o);
    Widget fogCircle({required double size, required Alignment alignment}) {
      return Align(
        alignment: alignment,
        child: Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: RadialGradient(
              colors: [fog(0.22), fog(0.10), fog(0.0)],
              stops: const [0.0, 0.55, 1.0],
            ),
          ),
        ),
      );
    }

    return IgnorePointer(
      child: Stack(
        children: [
          fogCircle(size: 320, alignment: const Alignment(-0.9, -0.8)),
          fogCircle(size: 380, alignment: const Alignment(0.8, -0.6)),
          fogCircle(size: 420, alignment: const Alignment(-0.6, 0.5)),
          fogCircle(size: 360, alignment: const Alignment(0.9, 0.8)),
        ],
      ),
    );
  }
}

class _PointLine extends StatelessWidget {
  const _PointLine(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.check_circle_rounded, size: 18, color: Colors.white),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 14,
                height: 1.35,
              ),
            ),
          ),
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

// Animated important notice card to draw user's attention
class ImportantNoticeCard extends StatefulWidget {
  const ImportantNoticeCard({super.key, required this.text, this.title = 'महत्वपूर्ण सूचना'});
  final String text;
  final String title;

  @override
  State<ImportantNoticeCard> createState() => _ImportantNoticeCardState();
}

class _ImportantNoticeCardState extends State<ImportantNoticeCard>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _fade;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
    _fade = CurvedAnimation(parent: _controller, curve: Curves.easeInOut);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(
              width: 18,
              height: 18,
              child: AnimatedBuilder(
                animation: _controller,
                builder: (context, child) {
                  final t = Curves.easeOut.transform(_controller.value);
                  final rippleSize = 18 + 8 * t;
                  final rippleOpacity = (1 - t).clamp(0.0, 1.0) * 0.35;
                  return Stack(
                    alignment: Alignment.center,
                    children: [
                      Container(
                        width: rippleSize,
                        height: rippleSize,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: theme.colorScheme.error
                              .withOpacity(rippleOpacity),
                        ),
                      ),
                      Container(
                        width: 8,
                        height: 8,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: theme.colorScheme.error,
                        ),
                      ),
                    ],
                  );
                },
              ),
            ),
            const SizedBox(width: 8),
            Text(
              widget.title,
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w800,
                color: theme.colorScheme.error,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        FadeTransition(
          opacity: Tween<double>(begin: 0.75, end: 1.0).animate(_fade),
          child: Text(widget.text),
        ),
      ],
    );
  }
}

/// Reusable step card with left step label and right-side action button.
class StepActionCard extends StatelessWidget {
  const StepActionCard({
    super.key,
    required this.stepLabel,
    required this.title,
    required this.onPressed,
  });

  final String stepLabel;
  final String title;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Stack(
      clipBehavior: Clip.none,
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // शीर्ष पर रिबन के लिए अतिरिक्त जगह
                      const SizedBox(height: 22),
                      Text(
                        title,
                        style: theme.textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: onPressed,
                  child: const Text('यहाँ से'),
                ),
              ],
            ),
          ),
        ),
        Positioned(
          top: 6,
          left: 6,
          child: _RibbonBadge(stepLabel),
        ),
      ],
    );
  }
}

/// Animated glow wrapper to draw attention around a card's edges.
class EdgeGlowWrapper extends StatefulWidget {
  const EdgeGlowWrapper({super.key, required this.child, this.borderRadius = 14});
  final Widget child;
  final double borderRadius;

  @override
  State<EdgeGlowWrapper> createState() => _EdgeGlowWrapperState();
}

class _EdgeGlowWrapperState extends State<EdgeGlowWrapper>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ac;

  @override
  void initState() {
    super.initState();
    _ac = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _ac.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return AnimatedBuilder(
      animation: _ac,
      builder: (context, child) {
        final t = Curves.easeInOut.transform(_ac.value); // 0..1
        final glowOpacity = 0.16 + 0.16 * t; // soft pulsing glow
        final borderOpacity = 0.22 + 0.22 * t;
        final blur = 16 + 6 * t;
        final spread = 1.0 + 1.2 * t;
        final c = theme.colorScheme.primary;
        return Stack(
          children: [
            // Behind: soft glow
            Positioned.fill(
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(widget.borderRadius),
                  boxShadow: [
                    BoxShadow(
                      color: c.withOpacity(glowOpacity),
                      blurRadius: blur,
                      spreadRadius: spread,
                    ),
                  ],
                ),
              ),
            ),
            // Content
            child!,
            // Above: subtle animated border highlight
            Positioned.fill(
              child: IgnorePointer(
                child: Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(widget.borderRadius),
                    border: Border.all(
                      color: c.withOpacity(borderOpacity),
                      width: 1.2 + 0.8 * t,
                    ),
                  ),
                ),
              ),
            ),
          ],
        );
      },
      child: widget.child,
    );
  }
}
