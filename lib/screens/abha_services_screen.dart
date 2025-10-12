import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/widgets/gradient_folder_icon.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';

const MethodChannel _downloadChannel = MethodChannel(
  'com.samoondigital.yojnaplus/downloads',
);
const String _abhaCreateUrl =
    'https://abha.abdm.gov.in/abha/v3/register/aadhaar';
const String _abhaDownloadUrl = 'https://abha.abdm.gov.in/abha/v3/login';
const String _abhaFindByMobileUrl = 'https://abha.abdm.gov.in/abha/v3/findabha';

class AbhaServicesScreen extends StatelessWidget {
  const AbhaServicesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('आभा कार्ड सेवाएँ'),
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
        padding: const EdgeInsets.all(12),
        children: [
          _ServiceTile(
            icon: Icons.badge_rounded,
            title: 'ABHA कार्ड बनाएँ',
            subtitle:
                'आधार/मोबाइल से नया ABHA (आयुष्मान भारत हेल्थ अकाउंट) रजिस्टर करें।',
            url: _abhaCreateUrl,
            buttonLabel: 'ABHA पंजीकरण शुरू करें',
          ),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/1761411130',
          ),
          const SizedBox(height: 12),
          _ServiceTile(
            icon: Icons.download_rounded,
            title: 'ABHA कार्ड डाउनलोड करें',
            subtitle:
                'निर्मित ABHA कार्ड PDF/स्मार्टकार्ड के रूप में डाउनलोड करें।',
            url: _abhaDownloadUrl,
            buttonLabel: 'ABHA कार्ड डाउनलोड',
            openExternally: true,
          ),
          _ServiceTile(
            icon: Icons.phone_iphone_rounded,
            title: 'मोबाइल नंबर से खोजें',
            subtitle:
                'अपना मोबाइल नंबर डालकर पता करें किन-किन के ABHA आपके नंबर से रजिस्टर्ड हैं (OTP की जरूरत नहीं)।',
            url: _abhaFindByMobileUrl,
            buttonLabel: 'मोबाइल से ABHA खोजें',
          ),
          const SizedBox(height: 16),
          Text(
            'नोट: सभी लिंक ABDM (आयुष्मान भारत डिजिटल मिशन) की आधिकारिक वेबसाइट पर ले जाते हैं।',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.outline,
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

class _ServiceTile extends StatelessWidget {
  const _ServiceTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.url,
    required this.buttonLabel,
    this.openExternally = false,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final String url;
  final String buttonLabel;
  final bool openExternally;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(icon, color: theme.colorScheme.primary),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(subtitle, style: theme.textTheme.bodyMedium),
            const SizedBox(height: 12),
            Center(
              child: ElevatedButton.icon(
                icon: const Icon(Icons.open_in_new_rounded),
                label: Text(buttonLabel),
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size(0, 48),
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 12,
                  ),
                  textStyle: const TextStyle(fontWeight: FontWeight.w700),
                ),
                onPressed: () async {
                  if (openExternally) {
                    final uri = Uri.parse(url);
                    final launched = await launchUrl(
                      uri,
                      mode: LaunchMode.externalApplication,
                    );
                    if (!launched) {
                      ScaffoldMessenger.maybeOf(context)?.showSnackBar(
                        const SnackBar(
                          content: Text(
                            'लिंक नहीं खुल सका, कृपया पुनः प्रयास करें।',
                          ),
                        ),
                      );
                    }
                    return;
                  }

                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) =>
                          InAppWebViewPage(title: title, initialUrl: url),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
