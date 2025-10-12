import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/widgets/movie_ticket_native_ad.dart';
import 'package:yojna_plus/widgets/gradient_folder_icon.dart';

const MethodChannel _downloadChannel = MethodChannel('com.samoondigital.yojnaplus/downloads');

class RationCardStatusScreen extends StatelessWidget {
  const RationCardStatusScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('राशन कार्ड'),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 20),
            child: IconButton(
              tooltip: 'डाउनलोड',
              icon: const GradientFolderIcon(size: 24),
              onPressed: () => _openDownloadsFolder(context),
            ),
          ),
        ],
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
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.ondemand_video,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'हिन्दी वीडियो गाइड',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'राशन कार्ड से सुविधाओं को कैसे इस्तेमाल करना है उसके लिए हिन्दी वीडियो देखें।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text('वीडियो लिंक बाद में जोड़ा जाएगा।'),
                          ),
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
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
            decoration: const BoxDecoration(color: Color(0xFF3674B5)),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: const [
                Text(
                  'विज्ञापन',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontWeight: FontWeight.w800,
                    fontSize: 18,
                    color: Colors.white,
                  ),
                ),
                SizedBox(height: 8),
                NativeBannerAd(
                  adUnitId: 'ca-app-pub-1638673809508848/8560136662',
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.fact_check_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'राशन कार्ड स्टेटस',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'राशन कार्ड में कितने सदस्य मौजूद हैं, किन सदस्यों की KYC पेंडिंग है — बिना OTP के राशन कार्ड का स्टेटस चेक करें।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: _ResponsiveActionButton(
                      icon: Icons.search_rounded,
                      label: 'राशन कार्ड चेक करें',
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'राशन कार्ड स्टेटस',
                              initialUrl:
                                  'https://nfsa.gov.in/public/frmPublicGetMyRCDetails.aspx',
                              enableFindInPage: true,
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.list_alt_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'राशन कार्ड सूची',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'शहरी व ग्रामीण अपना नागरिक राशन कार्ड की सूची में अपना नाम देख सकते हैं — राशन कार्ड धारक का नाम, राशन कार्ड नंबर और पिता के नाम से खोजने की सुविधा है।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: _ResponsiveActionButton(
                      icon: Icons.people_alt_outlined,
                      label: 'सूची में नाम देखें',
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'राशन कार्ड सूची',
                              initialUrl: 'https://fcs.up.gov.in/',
                              nextUrl:
                                  'https://nfsa.up.gov.in/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1',
                              nextUrlDelay: Duration(milliseconds: 600),
                              nextUrlHeaders: {
                                'Referer': 'https://fcs.up.gov.in/',
                                'Origin': 'https://fcs.up.gov.in',
                              },
                              enableFindInPage: true,
                              forceMobileMode: true,
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Stack(
            clipBehavior: Clip.none,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.file_download_outlined,
                            color: theme.colorScheme.primary,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'राशन कार्ड स्लिप डाउनलोड',
                              style: theme.textTheme.titleMedium,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'यहाँ से आप अपने राशन कार्ड की स्लिप डाउनलोड कर सकते हैं, जिसमें आपका पूरा ब्योरा होगा — नाम, पता, परिवार के सदस्य, पिछला राशन कब उठा व कितना — उसकी डिटेल। ध्यान दें: इसके लिए आपके राशन कार्ड में लगे मोबाइल नंबर पर OTP जाएगी।',
                      ),
                      const SizedBox(height: 12),
                      Center(
                        child: _ResponsiveActionButton(
                          icon: Icons.download_rounded,
                          label: 'राशन कार्ड स्लिप डाउनलोड',
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'राशन कार्ड स्लिप डाउनलोड',
                                  initialUrl: 'https://fcs.up.gov.in/',
                                  nextUrl:
                                      'https://nfsa.up.gov.in/Food/TrackingRationCard/NFSARCSearch.aspx?AspxAutoDetectCookieSupport=1',
                                  nextUrlDelay: Duration(milliseconds: 600),
                                  nextUrlHeaders: {
                                    'Referer': 'https://fcs.up.gov.in/',
                                    'Origin': 'https://fcs.up.gov.in',
                                  },
                                  enableFindInPage: true,
                                  forceMobileMode: true,
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              Positioned(
                top: -2,
                right: 6,
                child: IgnorePointer(
                  ignoring: true,
                  child: _RibbonBadge('OTP जरूरी'),
                ),
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
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.assignment_turned_in_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'राशन कार्ड आवेदन/संशोधन स्थिति',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'यहाँ से आप अपने नये राशन कार्ड का स्टेटस देख सकते हैं और आपने राशन कार्ड में यूनिट जोड़ी है तो उसका भी स्टेटस यहाँ दिखेगा।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: _ResponsiveActionButton(
                      icon: Icons.track_changes_outlined,
                      label: 'स्थिति चेक करें',
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'आवेदन/संशोधन स्थिति',
                              initialUrl: 'https://fcs.up.gov.in/',
                              nextUrl:
                                  'https://nfsa.up.gov.in/Food/TrackingRationCard/TrackApplication.aspx?AspxAutoDetectCookieSupport=1',
                              nextUrlDelay: Duration(milliseconds: 600),
                              nextUrlHeaders: {
                                'Referer': 'https://fcs.up.gov.in/',
                                'Origin': 'https://fcs.up.gov.in',
                              },
                              enableFindInPage: true,
                              forceMobileMode: true,
                            ),
                          ),
                        );
                      },
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
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.report_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'शिकायत करें',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'यहाँ से आप राशन कार्ड से जुड़ी किसी भी समस्या के लिए ऑनलाइन शिकायत कर सकते हैं, जो सीधे खाद्य एवं रसद विभाग द्वारा मॉनिटर की जाती है। उदाहरण: राशन कार्ड न बनना, परिवार सदस्य न जोड़ना/नाम न कटना, कोटेदार से जुड़ी समस्या आदि।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: Wrap(
                      alignment: WrapAlignment.center,
                      spacing: 10,
                      runSpacing: 8,
                      children: [
                        _ResponsiveActionButton(
                          icon: Icons.report_gmailerrorred_outlined,
                          label: 'शिकायत करें',
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'शिकायत दर्ज करें',
                                  initialUrl:
                                      'https://cms.up.gov.in/jsk/User/Complain_New_Public.aspx',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                        ),
                        _ResponsiveActionButton(
                          icon: Icons.fact_check_outlined,
                          label: 'स्थिति चेक करें',
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'शिकायत स्थिति',
                                  initialUrl:
                                      'https://cms.up.gov.in/jsk/User/compstatus.aspx',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                        ),
                      ],
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

Future<void> _openDownloadsFolder(BuildContext context) async {
  try {
    await _downloadChannel.invokeMethod('openDownloadsUI');
  } catch (_) {
    ScaffoldMessenger.maybeOf(context)?.showSnackBar(
      const SnackBar(content: Text('डाउनलोड फ़ोल्डर नहीं खोल पाए।')),
    );
  }
}

class _ResponsiveActionButton extends StatelessWidget {
  const _ResponsiveActionButton({
    required this.icon,
    required this.label,
    required this.onPressed,
  });

  final IconData icon;
  final String label;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        textStyle: theme.textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.w700,
        ),
      ),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final isNarrow = constraints.maxWidth < 260;
          if (isNarrow) {
            return Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Icon(icon),
                const SizedBox(height: 6),
                Text(label, textAlign: TextAlign.center, softWrap: true),
              ],
            );
          }
          return Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon),
              const SizedBox(width: 8),
              Flexible(
                child: Text(
                  label,
                  textAlign: TextAlign.center,
                  overflow: TextOverflow.visible,
                  softWrap: true,
                ),
              ),
            ],
          );
        },
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
