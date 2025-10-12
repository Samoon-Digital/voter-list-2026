import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:in_app_update/in_app_update.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yojna_plus/screens/select_state_screen.dart';
import 'package:yojna_plus/screens/uttar_pradesh_screen.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:yojna_plus/screens/onboarding_screen.dart';
import 'package:yojna_plus/screens/splash_screen.dart';

// Explicit Firebase options for Android to support initialization in background isolate
const FirebaseOptions _androidFirebaseOptions = FirebaseOptions(
  apiKey: 'AIzaSyCV5kWm_ZkboDPWagH4sWhmiuaBDsUX0a0',
  appId: '1:373614355764:android:1ef1d8eb45013c5b2bb161',
  messagingSenderId: '373614355764',
  projectId: 'yojna-plus',
  storageBucket: 'yojna-plus.firebasestorage.app',
);

@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // Ensure plugins are registered in background isolate before using them
  WidgetsFlutterBinding.ensureInitialized();
  if (defaultTargetPlatform == TargetPlatform.android) {
    await Firebase.initializeApp(options: _androidFirebaseOptions);
  } else {
    await Firebase.initializeApp();
  }
}

class RootRouter extends StatefulWidget {
  const RootRouter({super.key});

  @override
  State<RootRouter> createState() => _RootRouterState();
}

class _RootRouterState extends State<RootRouter> {
  bool _loading = true;
  bool _seen = false;
  String? _selectedState;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    final seen = prefs.getBool('onboarding_seen') ?? false;
    final selected = prefs.getString('selected_state');
    if (!mounted) return;
    setState(() {
      _seen = seen;
      _selectedState = selected;
      _loading = false;
    });
  }

  Future<void> _finishOnboarding() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('onboarding_seen', true);
    if (!mounted) return;
    setState(() {
      _seen = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (!_seen) {
      return OnboardingScreen(onFinished: _finishOnboarding);
    }
    if ((_selectedState ?? '').isNotEmpty) {
      // फिलहाल केवल उत्तर प्रदेश लाइव है
      return const UpdateGate(child: UttarPradeshPage());
    }
    return const UpdateGate(child: SelectStatePage());
  }
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  if (defaultTargetPlatform == TargetPlatform.android) {
    await Firebase.initializeApp(options: _androidFirebaseOptions);
  } else {
    await Firebase.initializeApp();
  }
  await MobileAds.instance.initialize();
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  runApp(const MainApp());
}

// Deduplicated widgets moved to screens/.

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    // Tailored color scheme for a Sarkari Yojna information app
    const seed = Color(0xFF0B57D0); // Trustworthy gov blue

    final lightScheme =
        ColorScheme.fromSeed(
          seedColor: seed,
          brightness: Brightness.light,
        ).copyWith(
          secondary: const Color(0xFFE65100), // Saffron accent for highlights
          tertiary: const Color(
            0xFF2E7D32,
          ), // Green accent for success/eligibility
          surface: const Color(0xFFF7F9FC),
          error: const Color(0xFFD32F2F),
        );

    final darkScheme =
        ColorScheme.fromSeed(
          seedColor: seed,
          brightness: Brightness.dark,
        ).copyWith(
          secondary: const Color(0xFFFFB74D),
          tertiary: const Color(0xFF81C784),
          surface: const Color(0xFF0F131A),
          error: const Color(0xFFEF5350),
        );

    final lightTheme = ThemeData(
      useMaterial3: true,
      colorScheme: lightScheme,
      scaffoldBackgroundColor: lightScheme.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: lightScheme.surface,
        foregroundColor: lightScheme.onSurface,
        elevation: 0,
        centerTitle: true,
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 1,
        margin: const EdgeInsets.all(12),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(14),
          side: BorderSide(color: lightScheme.outline),
        ),
      ),
      listTileTheme: ListTileThemeData(
        iconColor: lightScheme.primary,
        textColor: lightScheme.onSurface,
        selectedColor: lightScheme.primary,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: lightScheme.secondaryContainer,
        selectedColor: lightScheme.secondaryContainer.withOpacity(0.9),
        labelStyle: TextStyle(color: lightScheme.onSecondaryContainer),
        side: BorderSide(color: lightScheme.outline.withOpacity(0.5)),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: const Color(0xFF1565C0), // Link color
          textStyle: const TextStyle(
            decoration: TextDecoration.underline,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      dividerColor: lightScheme.outline.withOpacity(0.3),
    );

    final darkTheme = ThemeData(
      useMaterial3: true,
      colorScheme: darkScheme,
      scaffoldBackgroundColor: darkScheme.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: darkScheme.surface,
        foregroundColor: darkScheme.onSurface,
        elevation: 0,
        centerTitle: true,
      ),
      cardTheme: CardThemeData(
        color: darkScheme.surface,
        elevation: 1,
        margin: const EdgeInsets.all(12),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(14),
          side: BorderSide(color: darkScheme.outline),
        ),
      ),
      listTileTheme: ListTileThemeData(
        iconColor: darkScheme.primary,
        textColor: darkScheme.onSurface,
        selectedColor: darkScheme.primary,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: darkScheme.secondaryContainer,
        selectedColor: darkScheme.secondaryContainer.withOpacity(0.9),
        labelStyle: TextStyle(color: darkScheme.onSecondaryContainer),
        side: BorderSide(color: darkScheme.outline.withOpacity(0.5)),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: const Color(0xFF90CAF9), // Link color (dark)
          textStyle: const TextStyle(
            decoration: TextDecoration.underline,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      dividerColor: darkScheme.outline.withOpacity(0.4),
    );

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: ThemeMode.system,
      navigatorObservers: [
        FirebaseAnalyticsObserver(analytics: FirebaseAnalytics.instance),
      ],
      home: SplashScreen(next: const RootRouter()),
    );
  }
}
// Duplicate widgets removed. Using screens/select_state_screen.dart and screens/uttar_pradesh_screen.dart.

class UpdateGate extends StatefulWidget {
  const UpdateGate({super.key, required this.child});
  final Widget child;

  @override
  State<UpdateGate> createState() => _UpdateGateState();
}

class _UpdateGateState extends State<UpdateGate> with WidgetsBindingObserver {
  bool _checked = false;
  bool _forceRequired = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initMessaging();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkForUpdate();
    });
  }

  Future<void> _initMessaging() async {
    try {
      // Request permissions
      if (defaultTargetPlatform == TargetPlatform.iOS) {
        await FirebaseMessaging.instance.requestPermission(
          alert: true,
          badge: true,
          sound: true,
        );
      } else if (defaultTargetPlatform == TargetPlatform.android) {
        await Permission.notification.request();
      }

      // FCM token (send to server if needed)
      final token = await FirebaseMessaging.instance.getToken();
      if (kDebugMode) debugPrint('FCM token: $token');

      // Foreground messages
      FirebaseMessaging.onMessage.listen((message) {
        if (!mounted) return;
        final notif = message.notification;
        final title = notif?.title ?? 'नई सूचना';
        final body = notif?.body ?? '';
        final text = body.isNotEmpty ? '$title — $body' : title;
        ScaffoldMessenger.maybeOf(
          context,
        )?.showSnackBar(SnackBar(content: Text(text)));
      });

      // App opened from notification tap (background)
      FirebaseMessaging.onMessageOpenedApp.listen((message) {
        if (!mounted) return;
        final notif = message.notification;
        final title = notif?.title ?? 'नई सूचना';
        final body = notif?.body ?? '';
        final text = body.isNotEmpty ? '$title — $body' : title;
        ScaffoldMessenger.maybeOf(
          context,
        )?.showSnackBar(SnackBar(content: Text(text)));
      });

      // App opened from terminated state
      final initial = await FirebaseMessaging.instance.getInitialMessage();
      if (initial != null && mounted) {
        final notif = initial.notification;
        final title = notif?.title ?? 'नई सूचना';
        final body = notif?.body ?? '';
        final text = body.isNotEmpty ? '$title — $body' : title;
        ScaffoldMessenger.maybeOf(
          context,
        )?.showSnackBar(SnackBar(content: Text(text)));
      }

      // Analytics basic event
      await FirebaseAnalytics.instance.logAppOpen();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.maybeOf(context)?.showSnackBar(
        SnackBar(content: Text('नोटिफिकेशन सेटअप में समस्या: $e')),
      );
    }
  }

  Future<void> _checkForUpdate() async {
    if (_checked) return;
    _checked = true;
    // Android-only: Force immediate update if available
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return;
    try {
      final info = await InAppUpdate.checkForUpdate();
      if (info.updateAvailability == UpdateAvailability.updateAvailable) {
        if (info.immediateUpdateAllowed) {
          await InAppUpdate.performImmediateUpdate();
        } else {
          // Enforce forced update by blocking UI and asking user to update from Play Store
          if (mounted) setState(() => _forceRequired = true);
        }
      }
    } catch (e) {
      // Silent fail: Common on debug/emulator or non-Play builds. Avoid noisy snackbar at startup.
      if (kDebugMode) {
        debugPrint('Update check skipped: $e');
      }
    }
  }

  Future<void> _openStoreListing() async {
    try {
      final info = await PackageInfo.fromPlatform();
      final pkg = info.packageName;
      final marketUri = Uri.parse('market://details?id=$pkg');
      final webUri = Uri.parse(
        'https://play.google.com/store/apps/details?id=$pkg',
      );

      if (!await launchUrl(marketUri, mode: LaunchMode.externalApplication)) {
        await launchUrl(webUri, mode: LaunchMode.externalApplication);
      }
    } catch (_) {
      // Silently ignore. User stays on blocking screen.
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_forceRequired) {
      return _ForceUpdateView(onUpdatePressed: _openStoreListing);
    }
    return widget.child;
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed && _forceRequired) {
      // Re-check after returning from Play Store
      _checked = false;
      _checkForUpdate();
    }
  }
}

class _ForceUpdateView extends StatelessWidget {
  const _ForceUpdateView({required this.onUpdatePressed});
  final Future<void> Function() onUpdatePressed;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return WillPopScope(
      onWillPop: () async => false,
      child: Scaffold(
        body: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 420),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(20.0),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.system_update,
                          size: 56,
                          color: theme.colorScheme.primary,
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'नया अपडेट उपलब्ध है',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'कृपया आगे बढ़ने के लिए ऐप को अपडेट करें। यह अपडेट आवश्यक है।',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                        const SizedBox(height: 20),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            icon: const Icon(Icons.open_in_new),
                            label: const Text('गूगल प्ले पर अपडेट करें'),
                            onPressed: onUpdatePressed,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'अपडेट होने के बाद ऐप स्वतः खुल जाएगा।',
                          style: TextStyle(
                            fontSize: 12,
                            color: theme.colorScheme.outline,
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
    );
  }
}
