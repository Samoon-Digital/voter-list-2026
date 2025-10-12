import 'package:flutter/material.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key, required this.onFinished});
  final VoidCallback onFinished;

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final _controller = PageController();
  int _index = 0;

  final _pages = const [
    _ObPage(
      title: 'वीडियो गाइड',
      body:
          'सभी योजनाओं के लिए ऐप में हिंदी वीडियो उपलब्ध हैं। रजिस्ट्रेशन कैसे करें, स्थिति कैसे देखें—हर चरण का स्पष्ट मार्गदर्शन पाएँ।',
      icon: Icons.ondemand_video,
    ),
    _ObPage(
      title: 'प्रदेश सरकार की सभी योजनाएँ',
      body:
          'उत्तर प्रदेश में चल रही आम नागरिक से जुड़ी योजनाओं को ध्यान से पढ़ें। प्रत्यक्ष लाभ देने वाली योजनाओं की जानकारी हमारे ऐप में अपनी भाषा हिंदी में उपलब्ध है।',
      icon: Icons.account_balance,
    ),
    _ObPage(
      title: 'रोज काम आने वाली सेवाएँ',
      body:
          'आधार कार्ड, राशन कार्ड, वोटर कार्ड, आयुष्मान (आभा) कार्ड जैसी सेवाओं का नया आवेदन, स्थिति जांचें और डाउनलोड सब एक ही जगह करें।',
      icon: Icons.fact_check,
    ),
    _ObPage(
      title: 'विद्यार्थियों के लिए',
      body:
          'एडमिट कार्ड डाउनलोड, परिणाम देखें और नवीनतम नौकरियों की जानकारी पाएँ। पढ़ाई से जुड़ी हर ज़रूरी सुविधा ऐप के अंदर मौजूद है।',
      icon: Icons.school,
    ),
    _ObPage(
      title: 'किसान भाइयों के लिए',
      body:
          'खतौनी, गन्ना कैलेंडर और किसानों से जुड़ी योजनाओं की सभी जानकारी एक ही स्थान पर पाएँ। कृषि से जुड़े हर अपडेट पर नज़र रखें।',
      icon: Icons.agriculture,
    ),
  ];

  void _next() {
    if (_index < _pages.length - 1) {
      _controller.nextPage(duration: const Duration(milliseconds: 250), curve: Curves.easeOut);
    } else {
      widget.onFinished();
    }
  }

  void _skip() {
    widget.onFinished();
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(
        title: const Text('परिचय'),
        actions: [
          TextButton(
            onPressed: _skip,
            child: const Text('स्किप'),
          )
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: PageView.builder(
                controller: _controller,
                itemCount: _pages.length,
                onPageChanged: (i) => setState(() => _index = i),
                itemBuilder: (_, i) => _pages[i],
              ),
            ),
            const SizedBox(height: 8),
            _Dots(count: _pages.length, index: _index),
            const SizedBox(height: 16),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12),
              child: Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: _skip,
                      child: const Text('बाद में'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FilledButton.icon(
                      style: FilledButton.styleFrom(backgroundColor: scheme.primary, foregroundColor: scheme.onPrimary),
                      onPressed: _next,
                      icon: Icon(_index == _pages.length - 1 ? Icons.check : Icons.arrow_forward),
                      label: Text(_index == _pages.length - 1 ? 'समाप्त' : 'आगे'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Dots extends StatelessWidget {
  const _Dots({required this.count, required this.index});
  final int count;
  final int index;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(count, (i) {
        final selected = i == index;
        return AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          margin: const EdgeInsets.symmetric(horizontal: 4),
          width: selected ? 24 : 8,
          height: 8,
          decoration: BoxDecoration(
            color: selected ? scheme.primary : scheme.outlineVariant.withValues(alpha: 0.6),
            borderRadius: BorderRadius.circular(999),
          ),
        );
      }),
    );
  }
}

class _ObPage extends StatelessWidget {
  const _ObPage({required this.title, required this.body, required this.icon});
  final String title;
  final String body;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 520),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircleAvatar(
                radius: 44,
                backgroundColor: scheme.primaryContainer,
                child: Icon(icon, size: 44, color: scheme.onPrimaryContainer),
              ),
              const SizedBox(height: 24),
              Text(
                title,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 10),
              Text(
                body,
                textAlign: TextAlign.center,
                style: TextStyle(color: scheme.onSurfaceVariant, height: 1.35),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
