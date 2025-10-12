import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

class AadhaarServicesScreen extends StatelessWidget {
  const AadhaarServicesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('आधार कार्ड सेवाएँ')),
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
                        Icons.ondemand_video_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'वीडियो गाइड (हिंदी)',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'आधार कार्ड की सभी सेवाओं को इस्तेमाल करने के लिए हिंदी में वीडियो देखें।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'आधार वीडियो गाइड',
                              initialUrl:
                                  'https://www.youtube.com/results?search_query=aadhaar+services+hindi',
                              enableFindInPage: true,
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.play_circle_outline),
                      label: const Text('वीडियो देखें'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/7891064589',
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
                        Icons.verified_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'आधार कार्ड स्थिति चेक',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'नया आधार कार्ड या अपडेट कराया हुआ आधार की स्थिति यहाँ से देखें।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: ElevatedButton.icon(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const InAppWebViewPage(
                              title: 'आधार कार्ड स्थिति',
                              initialUrl:
                                  'https://myaadhaar.uidai.gov.in/CheckAadhaarStatus/en',
                              enableFindInPage: true,
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.search_rounded),
                      label: const Text('स्थिति चेक करें'),
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
                            Icons.picture_as_pdf_outlined,
                            color: theme.colorScheme.primary,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'आधार कार्ड PDF डाउनलोड',
                              style: theme.textTheme.titleMedium,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'यहाँ से आप अपने आधार कार्ड को आधार नंबर, एनरोलमेंट नंबर या वर्चुअल आईडी से PDF डाउनलोड कर सकते हैं।',
                      ),
                      const SizedBox(height: 12),
                      Center(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'आधार कार्ड डाउनलोड',
                                  initialUrl:
                                      'https://myaadhaar.uidai.gov.in/genricDownloadAadhaar/en',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.download_rounded),
                          label: const Text('डाउनलोड करें'),
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
                  child: const _RibbonBadge('OTP जरूरी'),
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
                        Icons.credit_card_outlined,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'पीवीसी आधार कार्ड ऑर्डर करें',
                          style: theme.textTheme.titleMedium,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'प्लास्टिक आधार कार्ड ऑर्डर करें और स्थिति भी चेक करें — बिना OTP के भी ऑर्डर हो जाता है।',
                  ),
                  const SizedBox(height: 12),
                  Center(
                    child: Wrap(
                      alignment: WrapAlignment.center,
                      spacing: 10,
                      runSpacing: 8,
                      children: [
                        ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'पीवीसी आधार कार्ड ऑर्डर',
                                  initialUrl:
                                      'https://myaadhaar.uidai.gov.in/genricPVC/en',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(
                            Icons.shopping_cart_checkout_outlined,
                          ),
                          label: const Text('ऑर्डर करें'),
                        ),
                        ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'पीवीसी ऑर्डर स्थिति',
                                  initialUrl:
                                      'https://myaadhaar.uidai.gov.in/checkStatus/en',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.fact_check_outlined),
                          label: const Text('स्थिति चेक करें'),
                        ),
                      ],
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
                            Icons.login_outlined,
                            color: theme.colorScheme.primary,
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'आधार कार्ड लॉगिन',
                              style: theme.textTheme.titleMedium,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'आधार लॉगिन करने पर आप आधार डाउनलोड, बायोमेट्रिक लॉक/अनलॉक, बैंक आधार सीडिंग स्थिति जैसी कई सुविधाओं को मैनेज कर सकते हैं।',
                      ),
                      const SizedBox(height: 12),
                      Center(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const InAppWebViewPage(
                                  title: 'आधार लॉगिन',
                                  initialUrl: 'https://myaadhaar.uidai.gov.in/',
                                  enableFindInPage: true,
                                ),
                              ),
                            );
                          },
                          icon: const Icon(Icons.key_outlined),
                          label: const Text('लॉगिन करें'),
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
                  child: const _RibbonBadge('OTP जरूरी'),
                ),
              ),
            ],
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
