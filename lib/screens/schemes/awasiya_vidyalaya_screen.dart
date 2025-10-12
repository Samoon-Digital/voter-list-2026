import 'package:flutter/material.dart';

class AwasiyaVidyalayaScreen extends StatelessWidget {
  const AwasiyaVidyalayaScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('आवासीय विद्यालय योजना')),
      body: const Padding(
        padding: EdgeInsets.all(12),
        child: Text('इस योजना का विस्तृत विवरण जल्द जोड़ा जाएगा।'),
      ),
    );
  }
}
