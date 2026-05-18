import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:yojna_plus/main.dart';
import 'package:yojna_plus/l10n/app_strings.dart';

class LanguageSelectionScreen extends StatefulWidget {
  const LanguageSelectionScreen({super.key});

  @override
  State<LanguageSelectionScreen> createState() =>
      _LanguageSelectionScreenState();
}

class _LanguageSelectionScreenState extends State<LanguageSelectionScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1000),
    );
    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOut));
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.2),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOut));

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _setLanguage(String languageCode) async {
    await AppStrings.setLanguage(languageCode);

    if (mounted) {
      if (Navigator.canPop(context)) {
        Navigator.pop(context, true); // Return true to indicate change
      } else {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (context) => const RootRouter()),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: FadeTransition(
          opacity: _fadeAnimation,
          child: SlideTransition(
            position: _slideAnimation,
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Icon or Logo
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.deepPurple.withOpacity(0.1),
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.translate_rounded,
                      size: 48,
                      color: Colors.deepPurple,
                    ),
                  ),
                  const SizedBox(height: 32),

                  // Title
                  Text(
                    'Choose Your Language',
                    style: GoogleFonts.poppins(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Colors.black87,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'अपनी भाषा चुनें',
                    style: GoogleFonts.poppins(
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                      color: Colors.grey[600],
                    ),
                  ),
                  const SizedBox(height: 48),

                  // English Card
                  _buildLanguageCard(
                    title: 'English',
                    subtitle: 'English',
                    color: Colors.blue.shade50,
                    borderColor: Colors.blue.shade200,
                    textColor: Colors.blue.shade900,
                    onTap: () => _setLanguage('en'),
                  ),
                  const SizedBox(height: 20),

                  // Hindi Card
                  _buildLanguageCard(
                    title: 'हिंदी',
                    subtitle: 'Hindi',
                    color: Colors.orange.shade50,
                    borderColor: Colors.orange.shade200,
                    textColor: Colors.deepOrange.shade900,
                    fontStyle: GoogleFonts.poppins(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                    onTap: () => _setLanguage('hi'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLanguageCard({
    required String title,
    required String subtitle,
    required Color color,
    required Color borderColor,
    required Color textColor,
    required VoidCallback onTap,
    TextStyle? fontStyle,
  }) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 24),
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: borderColor, width: 2),
            boxShadow: [
              BoxShadow(
                color: color.withOpacity(0.5),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style:
                        fontStyle ??
                        GoogleFonts.poppins(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: textColor,
                        ),
                  ),
                  Text(
                    subtitle,
                    style: GoogleFonts.poppins(
                      fontSize: 14,
                      color: textColor.withOpacity(0.7),
                    ),
                  ),
                ],
              ),
              Icon(Icons.arrow_forward_ios_rounded, color: textColor, size: 20),
            ],
          ),
        ),
      ),
    );
  }
}
