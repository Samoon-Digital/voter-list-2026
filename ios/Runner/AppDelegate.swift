import Flutter
import UIKit
import GoogleMobileAds

@main
@objc class AppDelegate: FlutterAppDelegate {
  private let nativeBannerFactoryId = "nativeBanner"

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)

    if let registrar = self.registrar(forPlugin: "GoogleMobileAdsPlugin") {
      let factory = NativeBannerAdFactory()
      GoogleMobileAdsPlugin.registerNativeAdFactory(
        registrar,
        factoryId: nativeBannerFactoryId,
        nativeAdFactory: factory
      )
    }

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  override func applicationWillTerminate(_ application: UIApplication) {
    if let registrar = self.registrar(forPlugin: "GoogleMobileAdsPlugin") {
      GoogleMobileAdsPlugin.unregisterNativeAdFactory(registrar, factoryId: nativeBannerFactoryId)
    }
    super.applicationWillTerminate(application)
  }
}

private class NativeBannerAdFactory: NSObject, FLTNativeAdFactory {
  func createNativeAd(
    _ nativeAd: GADNativeAd,
    customOptions: [AnyHashable: Any]? = nil
  ) -> GADNativeAdView {
    let nib = UINib(nibName: "NativeBanner", bundle: nil)
    guard
      let nativeAdView = nib.instantiate(withOwner: nil, options: nil).first
        as? GADNativeAdView
    else {
      return GADNativeAdView()
    }

    nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent
    nativeAdView.mediaView?.contentMode = .scaleAspectFill

    nativeAdView.headlineView = nativeAdView.headlineView ?? UIView()
    (nativeAdView.headlineView as? UILabel)?.text = nativeAd.headline

    if let body = nativeAd.body, let bodyLabel = nativeAdView.bodyView as? UILabel {
      bodyLabel.text = body
      bodyLabel.isHidden = false
    } else {
      nativeAdView.bodyView?.isHidden = true
    }

    if let advertiser = nativeAd.advertiser, let advertiserLabel = nativeAdView.advertiserView as? UILabel {
      advertiserLabel.text = advertiser
      advertiserLabel.isHidden = false
    } else {
      nativeAdView.advertiserView?.isHidden = true
    }

    if let icon = nativeAd.icon, let iconView = nativeAdView.iconView as? UIImageView {
      iconView.image = icon.image
      iconView.isHidden = false
    } else {
      nativeAdView.iconView?.isHidden = true
    }

    if let callToAction = nativeAd.callToAction, let ctaButton = nativeAdView.callToActionView as? UIButton {
      ctaButton.setTitle(callToAction, for: .normal)
      ctaButton.isHidden = false
      ctaButton.isUserInteractionEnabled = false
    } else {
      nativeAdView.callToActionView?.isHidden = true
    }

    nativeAdView.nativeAd = nativeAd
    return nativeAdView
  }
}
