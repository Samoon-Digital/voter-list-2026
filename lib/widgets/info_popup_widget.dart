import 'package:flutter/material.dart';
import 'package:yojna_plus/l10n/app_strings.dart';

class InfoPopupWidget extends StatelessWidget {
  final VoidCallback onClose;

  const InfoPopupWidget({super.key, required this.onClose});

  @override
  Widget build(BuildContext context) {
    // Informal colorful design
    return Dialog(
      backgroundColor: Colors.transparent,
      elevation: 0,
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [Color(0xFF6A11CB), Color(0xFF2575FC)], // Purple to Blue
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: Colors.white.withOpacity(0.5), width: 2),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.3),
              blurRadius: 10,
              offset: const Offset(0, 5),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(
              Icons.info_outline,
              size: 50,
              color: Colors.yellowAccent,
            ),
            const SizedBox(height: 16),
            Text(
              AppStrings.tr(AppStrings.infoPopupDesc),
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w600,
                height: 1.4,
              ),
            ),
            const SizedBox(height: 24),
            InkWell(
              onTap: onClose,
              borderRadius: BorderRadius.circular(30),
              child: Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.white, width: 2),
                ),
                child: const Icon(Icons.close, color: Colors.white, size: 24),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
