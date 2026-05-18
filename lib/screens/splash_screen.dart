import 'package:flutter/material.dart';
import 'dart:async';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:yojna_plus/screens/language_selection_screen.dart';
import 'package:yojna_plus/utils/ad_manager.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key, required this.next});
  final Widget next;

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    // Preload ads immediately for valid background loading
    AdManager.instance.preloadAll();
    _checkLanguageAndNavigate();
  }

  Future<void> _checkLanguageAndNavigate() async {
    // Wait for the splash animation/delay
    await Future.delayed(const Duration(seconds: 3));
    if (!mounted) return;

    final prefs = await SharedPreferences.getInstance();
    final String? languageCode = prefs.getString('language_code');

    if (mounted) {
      if (languageCode == null) {
        // First time launch -> Language Selection
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const LanguageSelectionScreen()),
        );
      } else {
        // Language already selected -> Go to Home (widget.next)
        Navigator.of(
          context,
        ).pushReplacement(MaterialPageRoute(builder: (_) => widget.next));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(24),
                child: Image.asset(
                  'assets/launcher/voter2026.png',
                  width: 120,
                  height: 120,
                  fit: BoxFit.cover,
                ),
              ),
              const SizedBox(height: 20),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 8,
                ),
                decoration: BoxDecoration(
                  color: theme.colorScheme.primary.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: theme.colorScheme.primary.withOpacity(0.2),
                    width: 1.5,
                  ),
                ),
                child: Text(
                  'By Samoon Digital',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: theme.colorScheme.primary,
                    letterSpacing: 0.5,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
