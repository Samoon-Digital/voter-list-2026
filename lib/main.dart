import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:yojna_plus/services/update_service.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yojna_plus/screens/home_page.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:yojna_plus/screens/splash_screen.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:yojna_plus/utils/ad_manager.dart';
import 'package:yojna_plus/l10n/app_strings.dart';

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

  @override
  void initState() {
    super.initState();
    _checkUpdate();
  }

  Future<void> _checkUpdate() async {
    // Simulate update check or load
    await Future.delayed(Duration.zero);
    if (!mounted) return;
    setState(() {
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    // होम स्क्रीन पर जाएं
    return const UpdateGate(child: HomePage());
  }
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  if (defaultTargetPlatform == TargetPlatform.android) {
    await Firebase.initializeApp(options: _androidFirebaseOptions);
  } else {
    await Firebase.initializeApp();
  }
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

  unawaited(MobileAds.instance.initialize());
  AdManager.instance.preloadAll();
  await AppStrings.init();

  runApp(const MainApp());
}

// Deduplicated widgets moved to screens/.

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    // Voter List 2026 Theme
    const primaryColor = Color(0xFF1A237E); // Deep Indigo
    const secondaryColor = Color(0xFFFF6D00); // Vibrant Saffron
    const seed = primaryColor;

    final lightScheme =
        ColorScheme.fromSeed(
          seedColor: seed,
          brightness: Brightness.light,
        ).copyWith(
          primary: primaryColor,
          onPrimary: Colors.white,
          secondary: secondaryColor,
          onSecondary: Colors.white,
          tertiary: const Color(0xFF2E7D32), // Success Green
          surface: const Color(0xFFF5F5F5),
          error: const Color(0xFFD32F2F),
        );

    final darkScheme =
        ColorScheme.fromSeed(
          seedColor: seed,
          brightness: Brightness.dark,
        ).copyWith(
          primary: const Color(0xFF536DFE),
          secondary: const Color(0xFFFF9E80),
          tertiary: const Color(0xFF81C784),
          surface: const Color(0xFF121212),
          error: const Color(0xFFEF5350),
        );

    final lightTheme = ThemeData(
      useMaterial3: true,
      colorScheme: lightScheme,
      scaffoldBackgroundColor: lightScheme.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: primaryColor,
        foregroundColor: Colors.white,
        elevation: 2,
        centerTitle: true,
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 2,
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: secondaryColor,
          foregroundColor: Colors.white,
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          textStyle: const TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
    );

    final darkTheme = ThemeData(
      useMaterial3: true,
      colorScheme: darkScheme,
      scaffoldBackgroundColor: darkScheme.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: const Color(0xFF0D1245),
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
      ),
      cardTheme: CardThemeData(
        color: const Color(0xFF1E1E1E),
        elevation: 2,
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );

    return MaterialApp(
      title: 'Voterlist',
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
  bool _forceRequired = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initMessaging();
    _checkForUpdate();
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
    final force = await UpdateService.instance.checkForUpdate();
    if (mounted && force != _forceRequired) {
      setState(() => _forceRequired = force);
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
    if (state == AppLifecycleState.resumed) {
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
    return PopScope(
      canPop: false,
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
