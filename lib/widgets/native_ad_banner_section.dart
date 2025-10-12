import 'package:flutter/material.dart';
import 'package:yojna_plus/widgets/movie_ticket_native_ad.dart';

/// Branded native ad banner with flat corners and headline, matching
/// `PmKisanDetailScreen` styling.
class NativeAdBannerSection extends StatelessWidget {
  const NativeAdBannerSection({super.key, required this.adUnitId});

  final String adUnitId;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
      color: const Color(0xFF3674B5),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text(
            'विज्ञापन',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontWeight: FontWeight.w800,
              fontSize: 19,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 8),
          NativeBannerAd(adUnitId: adUnitId),
        ],
      ),
    );
  }
}
