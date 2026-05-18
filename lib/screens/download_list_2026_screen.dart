import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yojna_plus/utils/ad_manager.dart';
import 'package:yojna_plus/l10n/app_strings.dart';

import 'package:yojna_plus/widgets/info_popup_widget.dart';

class DownloadList2026Screen extends StatefulWidget {
  const DownloadList2026Screen({super.key});

  @override
  State<DownloadList2026Screen> createState() => _DownloadList2026ScreenState();
}

class _DownloadList2026ScreenState extends State<DownloadList2026Screen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) =>
            InfoPopupWidget(onClose: () => Navigator.pop(context)),
      ).then((_) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: const Row(
                children: [
                  Icon(Icons.info_outline, color: Colors.white, size: 20),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'This app is ad supported. Ad loading...',
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
              backgroundColor: Colors.deepPurple,
              behavior: SnackBarBehavior.floating,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              duration: const Duration(seconds: 2),
            ),
          );
          Future.delayed(const Duration(seconds: 2), () {
            if (mounted) {
              AdManager.instance.showAd(context, 'draft');
            }
          });
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(AppStrings.tr(AppStrings.stateListTitle2026))),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                Card(
                  elevation: 4,
                  shadowColor: theme.colorScheme.primary.withOpacity(0.3),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Column(
                    children: [
                      // Colored Header
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 12,
                        ),
                        decoration: const BoxDecoration(
                          gradient: LinearGradient(
                            colors: [Colors.deepPurple, Colors.indigo],
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                          ),
                          borderRadius: BorderRadius.only(
                            topLeft: Radius.circular(16),
                            topRight: Radius.circular(16),
                          ),
                        ),
                        width: double.infinity,
                        child: Text(
                          AppStrings.tr(AppStrings.draftListTitle),
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            fontSize: 18,
                          ),
                        ),
                      ),
                      // Info Text
                      Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Text(
                          AppStrings.tr(AppStrings.draftListDesc),
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                            height: 1.5,
                          ),
                        ),
                      ),
                      const Divider(height: 1),
                      // List Items
                      Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Column(
                          children: [
                            _buildStateItem(
                              AppStrings.tr(AppStrings.chhattisgarh),
                              Colors.orange,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.kerala),
                              Colors.green,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.madhyaPradesh),
                              Colors.blue,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.rajasthan),
                              Colors.brown,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.andamanNicobar),
                              Colors.teal,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.chandigarh),
                              Colors.purple,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.dadraNagar),
                              Colors.deepOrange,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.delhi),
                              Colors.red,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.jammuKashmir),
                              Colors.indigo,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.ladakh),
                              Colors.cyan,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.lakshadweep),
                              Colors.pink,
                            ),
                            _buildStateItem(
                              AppStrings.tr(AppStrings.puducherry),
                              Colors.amber,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 24),
                // Static Download Button
                Container(
                  decoration: BoxDecoration(
                    boxShadow: [
                      BoxShadow(
                        color: Colors.deepPurple.withOpacity(0.4),
                        blurRadius: 15,
                        spreadRadius: 2,
                        offset: const Offset(0, 5),
                      ),
                    ],
                  ),
                  child: ElevatedButton.icon(
                    onPressed: () async {
                      final url = Uri.parse(
                        'https://voters.eci.gov.in/download-eroll?',
                      );
                      if (await canLaunchUrl(url)) {
                        await launchUrl(url, mode: LaunchMode.inAppBrowserView);
                      } else {
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Could not launch URL'),
                            ),
                          );
                        }
                      }
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.deepPurple,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(
                        horizontal: 32,
                        vertical: 16,
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30),
                      ),
                      textStyle: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.0,
                      ),
                    ),
                    icon: const Icon(Icons.download_for_offline, size: 28),
                    label: Text(
                      AppStrings.tr(AppStrings.downloadNow).toUpperCase(),
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                // Helper text
                Text(
                  'Tap the button above to visit the official ECI portal.',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: Colors.grey,
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildStateItem(String name, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 8.0),
      child: Row(
        children: [
          Icon(Icons.check_circle, color: color, size: 20),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              name,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: color, // Multi-colored text
              ),
            ),
          ),
          const _BlinkingNewBadge(),
        ],
      ),
    );
  }
}

class _BlinkingNewBadge extends StatefulWidget {
  const _BlinkingNewBadge();

  @override
  State<_BlinkingNewBadge> createState() => _BlinkingNewBadgeState();
}

class _BlinkingNewBadgeState extends State<_BlinkingNewBadge>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    )..repeat(reverse: true);
    _fadeAnimation = Tween<double>(begin: 0.4, end: 1.0).animate(_controller);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _fadeAnimation,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
        decoration: BoxDecoration(
          color: Colors.red,
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Text(
          'NEW',
          style: TextStyle(
            color: Colors.white,
            fontSize: 10,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
