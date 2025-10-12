import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/widgets/gradient_folder_icon.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';
import 'package:url_launcher/url_launcher.dart';

const MethodChannel _downloadChannel = MethodChannel('com.samoondigital.yojnaplus/downloads');
const String _voterServicesVideoUrl = 'https://www.youtube.com/@ECI/videos';
const String _eciSearchUrl = 'https://electoralsearch.eci.gov.in/';
const String _nvspBaseUrl = 'https://voters.eci.gov.in/';
const String _eciResultsUrl = 'https://results.eci.gov.in/';
const String _bloLookupUrl = 'https://sec.up.nic.in/site/PRIBLOList.aspx';
const Map<String, String> _secMobileHeaders = {
  'Referer': 'https://sec.up.nic.in/',
  'Accept-Language': 'hi-IN,hi;q=0.9,en-US;q=0.8,en;q=0.7',
  'Upgrade-Insecure-Requests': '1',
};

class VoterServicesScreen extends StatelessWidget {
  const VoterServicesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('मतदाता सेवाएँ (EPIC)'),
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
          const _VideoTutorialCard(),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/5933973321',
          ),
          const SizedBox(height: 12),
          _ServiceTile(
            icon: Icons.download_rounded,
            title: 'मतदाता सूची डाउनलोड – ग्रामीण',
            subtitle: 'ग्राम पंचायत/ग्रामीण क्षेत्रों के लिए PDF मतदाता सूची डाउनलोड करें।',
            url: 'https://sec.up.nic.in/site/VoterList2021.aspx',
            buttonLabel: 'ग्रामीण सूची खोलें',
            overrideUserAgent: kMobileChromeUserAgent,
            forceMobileMode: true,
            initialHeaders: _secMobileHeaders,
            openExternally: true,
          ),
          _ServiceTile(
            icon: Icons.download_for_offline_rounded,
            title: 'मतदाता सूची डाउनलोड – शहरी',
            subtitle: 'नगर निकाय/शहरी क्षेत्रों के लिए PDF मतदाता सूची डाउनलोड करें।',
            url: 'https://sec.up.nic.in/site/VoterListULB.aspx',
            buttonLabel: 'शहरी सूची खोलें',
            overrideUserAgent: kMobileChromeUserAgent,
            forceMobileMode: true,
            initialHeaders: _secMobileHeaders,
            openExternally: true,
          ),
          _ServiceTile(
            icon: Icons.search_rounded,
            title: 'नाम खोजें – ग्रामीण',
            subtitle: 'EPIC/नाम से अपनी प्रविष्टि खोजें (ग्रामीण क्षेत्र)।',
            url: _eciSearchUrl,
            buttonLabel: 'ग्रामीण नाम खोजें',
          ),
          _ServiceTile(
            icon: Icons.manage_search_rounded,
            title: 'नाम खोजें – शहरी',
            subtitle: 'EPIC/नाम से अपनी प्रविष्टि खोजें (शहरी क्षेत्र)।',
            url: _eciSearchUrl,
            buttonLabel: 'शहरी नाम खोजें',
          ),
          _ServiceTile(
            icon: Icons.person_add_alt_1_rounded,
            title: 'अपना नाम जोड़ें',
            subtitle: 'नया पंजीकरण (Form 6) — NVSP/ECI',
            url: _nvspBaseUrl,
            buttonLabel: 'नाम जोड़ने की सेवा',
          ),
          _ServiceTile(
            icon: Icons.person_remove_alt_1_rounded,
            title: 'अपना नाम हटवाएँ/सुधारें',
            subtitle: 'हटाने/स्थानांतरण/सुधार के लिए आवेदन (Form 7/8)',
            url: _nvspBaseUrl,
            buttonLabel: 'नाम हटाएँ/सुधारें',
          ),
          _ServiceTile(
            icon: Icons.how_to_vote_rounded,
            title: 'चुनाव परिणाम',
            subtitle: 'हालिया और ऐतिहासिक चुनाव परिणाम देखें।',
            url: _eciResultsUrl,
            buttonLabel: 'चुनाव परिणाम देखें',
          ),
          _ServiceTile(
            icon: Icons.location_on_rounded,
            title: 'अपना BLO पता करें',
            subtitle: 'क्षेत्र विशेष के लिए ब्लॉक लेवल अधिकारी (BLO) की सूची देखें।',
            url: _bloLookupUrl,
            buttonLabel: 'BLO सूची देखें',
          ),
          const SizedBox(height: 16),
          Text(
            'नोट: उपर्युक्त लिंक भारत निर्वाचन आयोग (ECI) की आधिकारिक सेवाओं पर ले जाते हैं।',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.outline,
            ),
          ),
        ],
      ),
    );
  }

Future<void> _openDownloadsFolder(BuildContext context) async {
  final messenger = ScaffoldMessenger.maybeOf(context);
  try {
    await _downloadChannel.invokeMethod('openDownloadsUI');
  } catch (_) {
    messenger?.showSnackBar(
      const SnackBar(content: Text('डाउनलोड फ़ोल्डर नहीं खोल पाए।')),
    );
  }
}
}

class _VideoTutorialCard extends StatelessWidget {
  const _VideoTutorialCard();

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
                Icon(Icons.live_tv_rounded, color: theme.colorScheme.primary),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'मतदाता सेवाएँ उपयोग गाइड',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        'वीडियो ट्यूटोरियल में जानें कि मतदाता सेवाओं का उपयोग कैसे करें।',
                        style: theme.textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Center(
              child: ElevatedButton.icon(
                icon: const Icon(Icons.play_circle_fill_rounded),
                label: const Text('वीडियो देखें'),
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size(0, 48),
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  textStyle: const TextStyle(fontWeight: FontWeight.w700),
                ),
                onPressed: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const InAppWebViewPage(
                        title: 'मतदाता सेवाएँ वीडियो',
                        initialUrl: _voterServicesVideoUrl,
                      ),
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

class _ServiceTile extends StatelessWidget {
  const _ServiceTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.url,
    required this.buttonLabel,
    this.initialHeaders,
    this.overrideUserAgent,
    this.forceMobileMode = false,
    this.openExternally = false,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final String url;
  final String buttonLabel;
  final Map<String, String>? initialHeaders;
  final String? overrideUserAgent;
  final bool forceMobileMode;
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
            Text(
              subtitle,
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            Center(
              child: ElevatedButton.icon(
                icon: const Icon(Icons.open_in_new_rounded),
                label: Text(buttonLabel),
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size(0, 48),
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  textStyle: const TextStyle(fontWeight: FontWeight.w700),
                ),
                onPressed: () async {
                  final navigator = Navigator.of(context);
                  // If asked, try to open in external browser first.
                  if (openExternally) {
                    try {
                      final ok = await launchUrl(Uri.parse(url), mode: LaunchMode.externalApplication);
                      if (ok) return;
                    } catch (_) {
                      // fall back to in-app webview
                    }
                  }

                  final headers = initialHeaders ??
                      ((overrideUserAgent != null || forceMobileMode)
                          ? const {
                              'Referer': 'https://electoralsearch.eci.gov.in/',
                            }
                          : null);
                  await navigator.push(
                    MaterialPageRoute(
                      builder: (_) => InAppWebViewPage(
                        title: title,
                        initialUrl: url,
                        overrideUserAgent: overrideUserAgent,
                        initialHeaders: headers,
                        forceMobileMode: forceMobileMode,
                      ),
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
