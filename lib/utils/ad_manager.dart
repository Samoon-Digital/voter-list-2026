import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

class AdManager {
  AdManager._();
  static final AdManager instance = AdManager._();

  // Ad Unit IDs (Production)
  final String _draftListAdUnitId = 'ca-app-pub-1638673809508848/8082121605';
  final String _list2003AdUnitId = 'ca-app-pub-1638673809508848/4946452687';
  final String _deletedListAdUnitId = 'ca-app-pub-1638673809508848/4946452687';

  final String _testInterstitialAdUnitId =
      'ca-app-pub-3940256099942544/1033173712';

  // Tag -> Ad Object
  final Map<String, InterstitialAd?> _ads = {};

  // Tag -> Loading State
  final Map<String, bool> _isLoading = {};

  String _getAdUnitIdForTag(String tag) {
    if (!kReleaseMode) return _testInterstitialAdUnitId;

    switch (tag) {
      case 'draft':
        return _draftListAdUnitId;
      case '2003':
        return _list2003AdUnitId;
      case 'deleted':
        return _deletedListAdUnitId;
      default:
        return _testInterstitialAdUnitId;
    }
  }

  void preloadAll() {
    loadAd('draft');
    loadAd('2003');
    loadAd('deleted');
  }

  void loadAd(String tag) {
    if (_isLoading[tag] == true || _ads[tag] != null) return;

    _isLoading[tag] = true;
    final adUnitId = _getAdUnitIdForTag(tag);

    InterstitialAd.load(
      adUnitId: adUnitId,
      request: const AdRequest(),
      adLoadCallback: InterstitialAdLoadCallback(
        onAdLoaded: (ad) {
          _ads[tag] = ad;
          _isLoading[tag] = false;
          debugPrint('Interstitial ad loaded for tag: $tag');

          ad.fullScreenContentCallback = FullScreenContentCallback(
            onAdDismissedFullScreenContent: (ad) {
              ad.dispose();
              _ads[tag] = null;
              loadAd(tag); // Reload immediately for next time
            },
            onAdFailedToShowFullScreenContent: (ad, error) {
              ad.dispose();
              _ads[tag] = null;
              loadAd(tag);
            },
          );
        },
        onAdFailedToLoad: (error) {
          debugPrint('Interstitial ad failed to load for tag $tag: $error');
          _isLoading[tag] = false;
          _ads[tag] = null;
        },
      ),
    );
  }

  void showAd(BuildContext context, String tag) {
    // In debug mode, we still want to show test ads
    final ad = _ads[tag];
    if (ad != null) {
      ad.show();
      _ads[tag] = null; // Clear immediately to prevent double show
    } else {
      debugPrint('Ad not ready for tag: $tag. Loading for next time.');
      loadAd(tag);
    }
  }
}
