import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

class ShramikSupportForm extends StatefulWidget {
  const ShramikSupportForm({super.key});

  @override
  State<ShramikSupportForm> createState() => _ShramikSupportFormState();
}

class _ShramikSupportFormState extends State<ShramikSupportForm> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _mobileController = TextEditingController();

  bool _submitting = false;

  @override
  void dispose() {
    _nameController.dispose();
    _mobileController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    setState(() => _submitting = true);
    try {
      await FirebaseFirestore.instance.collection('support_requests').add({
        'name': _nameController.text.trim(),
        'mobile': _mobileController.text.trim(),
        'source': 'pm_kisan_support',
        'status': 'pending',
        'createdAt': FieldValue.serverTimestamp(),
      });
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('धन्यवाद! आपका विवरण जमा हो गया।')),
      );
      Navigator.of(context).maybePop();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('सबमिट करने में समस्या: $e')),
      );
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    const c1 = Color(0xFFE53935);
    const c2 = Color(0xFF4FC3F7);
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          Positioned.fill(
            child: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [c1, c2],
                ),
              ),
            ),
          ),
          const Positioned.fill(child: _FogPatches()),
          SafeArea(
            child: Align(
              alignment: Alignment.topRight,
              child: Padding(
                padding: const EdgeInsets.all(30),
                child: Material(
                  color: Colors.white24,
                  shape: const CircleBorder(),
                  child: IconButton(
                    icon: const Icon(Icons.close),
                    color: Colors.white,
                    onPressed: () => Navigator.of(context).maybePop(),
                    tooltip: 'बंद करें',
                  ),
                ),
              ),
            ),
          ),
          Positioned.fill(
            child: SafeArea(
              child: Center(
                child: SingleChildScrollView(
                  padding: EdgeInsets.fromLTRB(
                    20,
                    20,
                    20,
                    24 + MediaQuery.of(context).viewInsets.bottom,
                  ),
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 520),
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.black.withOpacity(0.22),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: Colors.white.withOpacity(0.18)),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: const [
                                Icon(Icons.verified_rounded, color: Colors.lightBlueAccent, size: 28),
                                SizedBox(width: 8),
                                Text(
                                  ' 150₹ मात्र',
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 22,
                                    fontWeight: FontWeight.w900,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 14),
                            const _PointLine('समाज कल्याण विभाग द्वारा योजनाओं का लाभ'),
                            const _PointLine('पढ़ाई, बीमारी, शादी से जुड़ी योजनाएँ'),
                            const _PointLine('हज़ारों रुपये का लाभ'),
                            const _PointLine('सिर्फ श्रमिक कार्ड होने पर इन सभी योजनाओं का लाभ'),
                            const _PointLine('सभी योजनाएँ व धनराशि उत्तर प्रदेश सरकार द्वारा वित्त पोषित'),
                            const SizedBox(height: 8),
                            const Center(
                              child: Text(
                                'हमसे संपर्क करें',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 16,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                            ),
                            const SizedBox(height: 14),
                            Form(
                              key: _formKey,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  TextFormField(
                                    controller: _nameController,
                                    keyboardType: TextInputType.name,
                                    textInputAction: TextInputAction.next,
                                    style:
                                        const TextStyle(color: Colors.white, fontWeight: FontWeight.w700),
                                    decoration: _inputDecoration(theme, hint: 'अपना नाम डाले'),
                                    validator: (v) {
                                      if (v == null || v.trim().isEmpty) {
                                        return 'कृपया अपना नाम लिखें';
                                      }
                                      return null;
                                    },
                                  ),
                                  const SizedBox(height: 12),
                                  TextFormField(
                                    controller: _mobileController,
                                    keyboardType: TextInputType.phone,
                                    inputFormatters: [
                                      FilteringTextInputFormatter.digitsOnly,
                                      LengthLimitingTextInputFormatter(10),
                                    ],
                                    style:
                                        const TextStyle(color: Colors.white, fontWeight: FontWeight.w700),
                                    decoration: _inputDecoration(theme, hint: 'अपना मोबाईल नंबर डाले'),
                                    validator: (v) {
                                      final s = v?.trim() ?? '';
                                      if (s.isEmpty) return 'कृपया मोबाइल नंबर लिखें';
                                      if (s.length != 10) return '10 अंकों का मोबाइल नंबर लिखें';
                                      return null;
                                    },
                                  ),
                                  const SizedBox(height: 16),
                                  ElevatedButton.icon(
                                    onPressed: _submitting ? null : _submit,
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: theme.colorScheme.primary,
                                      foregroundColor: theme.colorScheme.onPrimary,
                                      minimumSize: const Size.fromHeight(52),
                                      padding:
                                          const EdgeInsets.symmetric(vertical: 16, horizontal: 16),
                                      textStyle: const TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.w700,
                                      ),
                                      shape: RoundedRectangleBorder(
                                        borderRadius: BorderRadius.circular(12),
                                      ),
                                      elevation: 3,
                                    ),
                                    icon: const Icon(Icons.send_rounded),
                                    label: Text(_submitting ? 'जमा हो रहा है...' : 'फ़ार्म जमा करे'),
                                  ),
                                  const SizedBox(height: 8),
                                  const Text(
                                    'नोट : फार्म जमा करने के बाद हम आपको जल्द से जल्द काल करेंगे अभी आपको कोई भुगतान नहीं करना है',
                                    style: TextStyle(color: Colors.white, fontSize: 12.5),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  InputDecoration _inputDecoration(ThemeData theme, {required String hint}) {
    return InputDecoration(
      hintText: hint,
      hintStyle: const TextStyle(color: Colors.white70),
      filled: true,
      fillColor: Colors.white.withOpacity(0.10),
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: Colors.white.withOpacity(0.24)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: BorderSide(color: theme.colorScheme.secondary.withOpacity(0.9)),
      ),
      errorStyle: const TextStyle(color: Colors.white),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Colors.white),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Colors.white),
      ),
    );
  }
}

class _FogPatches extends StatelessWidget {
  const _FogPatches();

  @override
  Widget build(BuildContext context) {
    Color fog(double o) => Colors.white.withOpacity(o);
    Widget fogCircle({required double size, required Alignment alignment}) {
      return Align(
        alignment: alignment,
        child: Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: RadialGradient(
              colors: [fog(0.22), fog(0.10), fog(0.0)],
              stops: const [0.0, 0.55, 1.0],
            ),
          ),
        ),
      );
    }

    return IgnorePointer(
      child: Stack(
        children: [
          fogCircle(size: 320, alignment: const Alignment(-0.9, -0.8)),
          fogCircle(size: 380, alignment: const Alignment(0.8, -0.6)),
          fogCircle(size: 420, alignment: const Alignment(-0.6, 0.5)),
          fogCircle(size: 360, alignment: const Alignment(0.9, 0.8)),
        ],
      ),
    );
  }
}

class _PointLine extends StatelessWidget {
  const _PointLine(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.check_circle_rounded, size: 18, color: Colors.white),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 14,
                height: 1.35,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
