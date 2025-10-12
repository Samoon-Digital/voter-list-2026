import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

/// A reusable native banner ad that uses the registered platform factories.
class NativeBannerAd extends StatefulWidget {
  const NativeBannerAd({super.key, this.adUnitId});

  /// Override the default AdMob test unit if required.
  final String? adUnitId;

  static const String defaultAdUnitId = 'ca-app-pub-1638673809508848/1911979598';
  static const double _fixedHeight = 220;

  @override
  State<NativeBannerAd> createState() => _NativeBannerAdState();
}

class _NativeBannerAdState extends State<NativeBannerAd> with AutomaticKeepAliveClientMixin {
  NativeAd? _nativeAd;
  bool _isLoaded = false;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadAd();
  }

  @override
  void didUpdateWidget(covariant NativeBannerAd oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.adUnitId != widget.adUnitId) {
      _disposeAd();
      _loadAd();
    }
  }

  void _loadAd() {
    if (_isLoading) return;
    _isLoading = true;
    final ad = NativeAd(
      adUnitId: widget.adUnitId ?? NativeBannerAd.defaultAdUnitId,
      factoryId: 'nativeBanner',
      request: const AdRequest(),
      listener: NativeAdListener(
        onAdLoaded: (ad) {
          if (!mounted) {
            ad.dispose();
            return;
          }
          setState(() {
            _nativeAd = ad as NativeAd;
            _isLoaded = true;
            _isLoading = false;
          });
        },
        onAdFailedToLoad: (ad, error) {
          ad.dispose();
          if (mounted) {
            setState(() {
              _isLoaded = false;
              _isLoading = false;
            });
          } else {
            _isLoading = false;
          }
        },
      ),
    );
    ad.load();
  }

  void _disposeAd() {
    _nativeAd?.dispose();
    _nativeAd = null;
    _isLoaded = false;
  }

  @override
  void dispose() {
    _disposeAd();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    const double fixedHeight = NativeBannerAd._fixedHeight;
    if (!_isLoaded || _nativeAd == null) {
      return SizedBox(
        height: fixedHeight,
        width: double.infinity,
        child: Center(
          child: _isLoading
              ? const SizedBox(
                  height: 32,
                  width: 32,
                  child: CircularProgressIndicator(strokeWidth: 2.8),
                )
              : TextButton(
                  onPressed: _loadAd,
                  child: const Text('विज्ञापन लोड करें'),
                ),
        ),
      );
    }

    // Fixed-height container to prevent vertical expansion
    return SizedBox(
      height: fixedHeight,
      width: double.infinity,
      child: AdWidget(ad: _nativeAd!),
    );
  }

  @override
  bool get wantKeepAlive => true;
}
