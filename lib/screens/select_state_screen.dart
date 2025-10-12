import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/uttar_pradesh_screen.dart';
import 'package:shared_preferences/shared_preferences.dart';

// Simple state selection screen (moved out of main.dart)
const kOtherIndianStates = <String>[
  'आंध्र प्रदेश',
  'अरुणाचल प्रदेश',
  'असम',
  'बिहार',
  'छत्तीसगढ़',
  'गोवा',
  'गुजरात',
  'हरियाणा',
  'हिमाचल प्रदेश',
  'झारखंड',
  'कर्नाटक',
  'केरल',
  'मध्य प्रदेश',
  'महाराष्ट्र',
  'मणिपुर',
  'मेघालय',
  'मिजोरम',
  'नगालैंड',
  'ओडिशा',
  'पंजाब',
  'राजस्थान',
  'सिक्किम',
  'तमिलनाडु',
  'तेलंगाना',
  'त्रिपुरा',
  'उत्तराखंड',
  'पश्चिम बंगाल',
];

class SelectStatePage extends StatelessWidget {
  const SelectStatePage({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('अपना राज्य चुनें'),
      ),
      body: ListView(
        padding: EdgeInsets.fromLTRB(
          12,
          12,
          12,
          50 + MediaQuery.of(context).padding.bottom,
        ),
        children: [
          // Live: Uttar Pradesh
          Card(
            child: ListTile(
              leading: Icon(Icons.flag, color: theme.colorScheme.primary),
              title: const Text(
                'उत्तर प्रदेश',
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
              subtitle: Text(
                'लाइव',
                style: TextStyle(color: theme.colorScheme.tertiary),
              ),
              trailing: const FittedBox(
                fit: BoxFit.scaleDown,
                child: LiveBadge(),
              ),
              onTap: () async {
                // Persist the selected state so selector shows only once
                final prefs = await SharedPreferences.getInstance();
                await prefs.setString('selected_state', 'UP');
                if (!context.mounted) return;
                // Replace selector with the state screen
                Navigator.of(context).pushReplacement(
                  MaterialPageRoute(
                    builder: (_) => const UttarPradeshPage(),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 16, 8, 8),
            child: Text(
              'अन्य राज्य — जल्द आ रहे हैं',
              style: theme.textTheme.titleMedium?.copyWith(
                color: theme.colorScheme.onSurface.withOpacity(0.7),
              ),
            ),
          ),
          ...kOtherIndianStates.map(
            (s) => Card(
              child: ListTile(
                leading: Icon(
                  Icons.location_city,
                  color: theme.colorScheme.outline,
                ),
                title: Text(s),
                subtitle: Text(
                  'जल्द आ रहा है',
                  style: TextStyle(color: theme.colorScheme.outline),
                ),
                enabled: false,
                onTap: null,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// Animated LIVE badge with pulsing dot (moved out of main.dart)
class LiveBadge extends StatefulWidget {
  const LiveBadge({super.key});

  @override
  State<LiveBadge> createState() => _LiveBadgeState();
}

class _LiveBadgeState extends State<LiveBadge>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: theme.colorScheme.errorContainer,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: 18,
            height: 18,
            child: AnimatedBuilder(
              animation: _controller,
              builder: (context, child) {
                final t = Curves.easeOut.transform(_controller.value);
                final rippleSize = 18 + 10 * t;
                final rippleOpacity = (1 - t).clamp(0.0, 1.0) * 0.35;
                return Stack(
                  alignment: Alignment.center,
                  children: [
                    Container(
                      width: rippleSize,
                      height: rippleSize,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color:
                            theme.colorScheme.error.withOpacity(rippleOpacity),
                      ),
                    ),
                    Container(
                      width: 8,
                      height: 8,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: theme.colorScheme.error,
                      ),
                    ),
                  ],
                );
              },
            ),
          ),
          const SizedBox(width: 6),
          Text(
            'लाइव',
            style: theme.textTheme.labelMedium?.copyWith(
              color: theme.colorScheme.onErrorContainer,
              fontWeight: FontWeight.w700,
              letterSpacing: 1.0,
            ),
          ),
        ],
      ),
    );
  }
}
