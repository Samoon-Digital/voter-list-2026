import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/aadhaar_services_screen.dart';
import 'package:yojna_plus/screens/abha_services_screen.dart';
import 'package:yojna_plus/screens/gram_panchayat_forms_screen.dart';
import 'package:yojna_plus/screens/ration_card_status_screen.dart';
import 'package:yojna_plus/screens/voter_services_screen.dart';
import 'package:yojna_plus/widgets/feature_section.dart';
import 'package:yojna_plus/screens/schemes/awas_card_screen.dart';

class UsefulPage extends StatelessWidget {
  const UsefulPage({super.key});

  @override
  Widget build(BuildContext context) {
    return FeatureSection(
      title: 'उपयोगी लिंक',
      cards: [
        FeatureCardConfig(
          icon: Icons.credit_card,
          title: 'राशन कार्ड',
          description: 'राशन कार्ड की स्थिति, सूची व डाउनलोड के लिए त्वरित सहायता।',
          gradient: (theme) => [theme.colorScheme.primary, theme.colorScheme.secondary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const RationCardStatusScreen(),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.fingerprint,
          title: 'आधार सेवाएँ',
          description: 'UIDAI अपडेट, नामांकन स्थिति, डाउनलोड और अन्य महत्वपूर्ण सेवाएँ देखें।',
          gradient: (theme) => [theme.colorScheme.secondary, theme.colorScheme.tertiary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const AadhaarServicesScreen(),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.how_to_vote_rounded,
          title: 'वोटर कार्ड व पहचान पत्र',
          description: 'मतदाता सूची डाउनलोड, नाम खोज, संशोधन और चुनाव परिणाम जानकारी।',
          gradient: (theme) => [theme.colorScheme.tertiary, theme.colorScheme.primary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const VoterServicesScreen(),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.house_rounded,
          title: 'आवास योजना सूची - 2025',
          description: 'आवास सूची में अपना नाम देखें और स्थिति जाँचें।',
          gradient: (theme) => [theme.colorScheme.primary, theme.colorScheme.secondary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const AwasCardScreen(),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.health_and_safety_rounded,
          title: 'ABHA डिजिटल हेल्थ',
          description: 'ABHA ID बनाएं, डाउनलोड करें और अपना डिजिटल हेल्थ रिकॉर्ड प्रबंधित करें।',
          gradient: (theme) => [theme.colorScheme.primary, theme.colorScheme.tertiary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const AbhaServicesScreen(),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.assignment_rounded,
          title: 'ग्राम पंचायत फॉर्म्स',
          description: 'जाति, आय, निवास प्रमाण पत्र सहित ग्राम पंचायत के उपयोगी PDF फॉर्म्स।',
          gradient: (theme) => [theme.colorScheme.secondary, theme.colorScheme.primary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const GramPanchayatFormsScreen(),
            ),
          ),
        ),
      ],
    );
  }
}
