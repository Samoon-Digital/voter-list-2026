import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/blogger_feed_page.dart';

class ResultFeedScreen extends StatelessWidget {
  const ResultFeedScreen({super.key});

  static const String blogId = '8198392631392785844';
  static const String apiKey = 'AIzaSyDvufoZKKOvEfIpGDnoLi_qRtwWMHSxoik';

  @override
  Widget build(BuildContext context) {
    return BloggerFeedPage(
      config: const BloggerFeedConfig(
        blogId: blogId,
        apiKey: apiKey,
        label: 'Result',
        title: 'रिज़ल्ट अपडेट',
      ),
    );
  }
}
