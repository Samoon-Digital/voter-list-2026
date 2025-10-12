import 'package:circle_splash/circle_splash.dart';
import 'package:flutter/material.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key, required this.next});
  final Widget next;

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  // Legacy animations removed; CircleSplash handles animation now.

  @override
  void initState() {
    super.initState();
    // No manual controller needed.
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bg = theme.colorScheme.surface;

    final logo = ClipRRect(
      borderRadius: BorderRadius.circular(28), // Corner radius for icon
      child: Image.asset(
        'assets/launcher/yojnalogo.jpg',
        width: 140,
        height: 140,
        fit: BoxFit.cover,
      ),
    );

    // CircleSplash: center expansion using launcher icon
    return CircleSplashScreen(
      config: CircleSplashConfig(
        animationType: CircleSplashAnimationType.center,
        backgroundColor: bg,
        circleColor: theme.colorScheme.primary,
        animationDuration: const Duration(milliseconds: 2500),
        fadeDuration: const Duration(milliseconds: 300),
      ),
      onAnimationComplete: () {
        Navigator.of(
          context,
        ).pushReplacement(MaterialPageRoute(builder: (_) => widget.next));
      },
      child: logo,
    );
  }
}
