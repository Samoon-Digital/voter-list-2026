import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/blogger_feed_page.dart';
import 'package:yojna_plus/screens/student/result_feed_screen.dart';
import 'package:yojna_plus/widgets/feature_section.dart';

class OthersPage extends StatelessWidget {
  const OthersPage({super.key});

  @override
  Widget build(BuildContext context) {
    return FeatureSection(
      title: 'स्टूडेंट कॉर्नर',
      cards: [
        FeatureCardConfig(
          icon: Icons.school_rounded,
          title: 'रिज़ल्ट',
          description: 'UP बोर्ड और सभी प्रकार के परिणाम एक ही जगह देखें।',
          gradient: (theme) => [theme.colorScheme.primary, theme.colorScheme.tertiary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => const ResultFeedScreen(),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.badge_rounded,
          title: 'एडमिट कार्ड',
          description: 'हर परीक्षा के एडमिट कार्ड यहाँ से तुरंत डाउनलोड करें।',
          gradient: (theme) => [theme.colorScheme.secondary, theme.colorScheme.primary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => BloggerFeedPage(
                config: BloggerFeedConfig(
                  blogId: ResultFeedScreen.blogId,
                  apiKey: ResultFeedScreen.apiKey,
                  label: 'Admitcard',
                  title: 'एडमिट कार्ड अपडेट',
                ),
              ),
            ),
          ),
        ),
        FeatureCardConfig(
          icon: Icons.work_outline_rounded,
          title: 'वैकेंसी व जॉब पोस्ट',
          description: 'नई सरकारी नौकरियाँ, आवेदन लिंक और दस्तावेज़ जानकारी पूरी तरह हिंदी में पाएँ।',
          gradient: (theme) => [theme.colorScheme.tertiary, theme.colorScheme.secondary],
          onTap: (context) => Navigator.of(context).push(
            MaterialPageRoute(
              builder: (_) => BloggerFeedPage(
                config: BloggerFeedConfig(
                  blogId: ResultFeedScreen.blogId,
                  apiKey: ResultFeedScreen.apiKey,
                  label: 'Jobpost',
                  title: 'नौकरी व वैकेंसी अपडेट',
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
