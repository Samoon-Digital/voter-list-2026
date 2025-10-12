import 'package:flutter/foundation.dart';
import 'package:flutter_tts/flutter_tts.dart';

/// Controls Text-to-Speech playback and exposes progress for per-word highlighting.
class TtsReaderController extends ChangeNotifier {
  final FlutterTts _tts = FlutterTts();

  List<String> _segments = const [];
  List<int> _starts = const [];
  String _joined = '';

  bool _configured = false;
  bool _speaking = false;

  // Current highlight based on global offsets into the joined text
  int _globalStart = -1;
  int _globalEnd = -1;

  bool get isSpeaking => _speaking;

  /// Initialize with the ordered list of text segments you want to read and highlight.
  void init({required List<String> segments}) {
    _segments = segments;
    final starts = <int>[];
    final buffer = StringBuffer();
    var offset = 0;
    for (var i = 0; i < segments.length; i++) {
      starts.add(offset);
      buffer.write(segments[i]);
      offset += segments[i].length;
      if (i != segments.length - 1) {
        buffer.write('\n\n');
        offset += 2; // for the two newlines
      }
    }
    _starts = starts;
    _joined = buffer.toString();
  }

  Future<void> _ensureConfigured() async {
    if (_configured) return;
    // Hindi locale
    await _tts.setLanguage('hi-IN');
    // Balanced speech rate for Hindi
    await _tts.setSpeechRate(0.44);
    await _tts.setPitch(1.0);
    // Ensure progress/completion callbacks fire in sequence
    await _tts.awaitSpeakCompletion(true);

    _tts.setProgressHandler((text, start, end, word) {
      // start/end are character indices in `text`.
      _updateHighlight(start, end);
    });
    _tts.setCompletionHandler(() {
      _speaking = false;
      _globalStart = -1;
      _globalEnd = -1;
      notifyListeners();
    });
    _configured = true;
  }

  void _updateHighlight(int start, int end) {
    _globalStart = start;
    _globalEnd = end;
    notifyListeners();
  }

  /// Toggle speaking. If currently speaking, it stops.
  Future<void> toggleSpeak() async {
    if (_speaking) {
      await stop();
      return;
    }
    if (_joined.isEmpty && _segments.isNotEmpty) {
      init(segments: _segments);
    }
    await _ensureConfigured();
    // Restart from beginning every time for predictable highlighting
    await _tts.stop();
    _speaking = true;
    notifyListeners();
    await _tts.speak(_joined);
  }

  Future<void> stop() async {
    await _tts.stop();
    _speaking = false;
    _globalStart = -1;
    _globalEnd = -1;
    notifyListeners();
  }

  /// Returns highlight (start, end) relative to given [segmentIndex], or null if not in this segment.
  ({int start, int end})? highlightForSegment(int segmentIndex) {
    if (_globalStart < 0 || segmentIndex < 0 || segmentIndex >= _segments.length) return null;
    final segStart = _starts[segmentIndex];
    final segEnd = segStart + _segments[segmentIndex].length;
    // If current global highlight overlaps this segment
    if (_globalEnd <= segStart || _globalStart >= segEnd) return null;
    final localStart = (_globalStart - segStart).clamp(0, _segments[segmentIndex].length);
    final localEnd = (_globalEnd - segStart).clamp(0, _segments[segmentIndex].length);
    return (start: localStart, end: localEnd);
  }

  /// Replaces all segments at once and resets highlights.
  void setSegments(List<String> segments) {
    init(segments: segments);
    _globalStart = -1;
    _globalEnd = -1;
    notifyListeners();
  }

  @override
  void dispose() {
    _tts.stop();
    super.dispose();
  }
}
