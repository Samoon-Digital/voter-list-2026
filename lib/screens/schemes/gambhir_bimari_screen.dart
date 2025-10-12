import 'package:flutter/material.dart';

class GambhirBimariScreen extends StatelessWidget {
  const GambhirBimariScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('गम्भीर बीमारी सहायता योजना')),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          // पात्रता
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.verified_user, 'पात्रता (Eligibility)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'अद्यतन पंजीकृत श्रमिक।'),
                  _bullet(context, 'ऐसे श्रमिक जो प्रधानमंत्री जन-आरोग्य योजना एवं मुख्यमन्त्री जन-आरोग्य योजना में हितलाभ हेतु पात्र नहीं हैं।'),
                  _bullet(context, 'योजना में पंजीकृत श्रमिक तथा उसकी पति/ पत्नी, अविवाहित पुत्रियाॅं एवं 21 वर्ष से कम आयु के पुत्र सम्मिलित हैं।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // आवश्यक अभिलेख
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.assignment_turned_in_outlined, 'आवश्यक अभिलेख (Documents)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'अद्यतन पंजीकृत होने का साक्ष्य'),
                  _bullet(context, 'बीमारी से सम्बन्धित अभिलेख'),
                  _bullet(context, 'नियत प्रारूप पर चिकित्सक का प्रमाण पत्र'),
                  _bullet(context, 'दवाईयों के क्रय पर मूल बिल'),
                  _bullet(context, 'अविवाहित पुत्री या 21 वर्ष से कम आयु का पुत्र होने पर आश्रित होने का प्रमाण-पत्र।'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          // देय हितलाभ
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _sectionHeader(context, Icons.health_and_safety_outlined, 'देय हितलाभ (Benefits)'),
                  const SizedBox(height: 8),
                  _bullet(context, 'सरकारी/ स्वायत्तशासी चिकित्सालयों अथवा SACHIS के इम्पैनल्ड चिकित्सालयों में इलाज कराने पर आयुष्मान भरत योजना में देय हितलाभ के समतुल्य राशि पूर्ण प्रतिपूर्ति।'),
                  _bullet(context, 'चिकित्सा/शल्यक्रिया में चिकित्सालय द्वारा इलाज का इस्टीमेट दिये जाने पर चिकित्सालय को अग्रिम राशि का भी भुगतान किया जा सकता है।'),
                  _bullet(context, 'कोई अधिकतम राशि नियत नहीं।'),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

Widget _sectionHeader(BuildContext context, IconData icon, String title) {
  final theme = Theme.of(context);
  return Row(
    crossAxisAlignment: CrossAxisAlignment.center,
    children: [
      Icon(icon, color: theme.colorScheme.primary),
      const SizedBox(width: 8),
      Expanded(child: Text(title, style: theme.textTheme.titleMedium)),
    ],
  );
}

Widget _bullet(BuildContext context, String text, {double indent = 0}) {
  final theme = Theme.of(context);
  return Padding(
    padding: EdgeInsets.only(bottom: 6, left: indent),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('•', style: theme.textTheme.bodyMedium),
        const SizedBox(width: 8),
        Expanded(child: Text(text)),
      ],
    ),
  );
}
}
