import 'package:flutter/material.dart';
import 'package:yojna_plus/tts/tts_reader.dart';

class HighlightableText extends StatelessWidget {
  const HighlightableText({
    super.key,
    required this.controller,
    required this.segmentIndex,
    required this.text,
    this.style,
    this.textAlign,
  });

  final TtsReaderController controller;
  final int segmentIndex;
  final String text;
  final TextStyle? style;
  final TextAlign? textAlign;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final range = controller.highlightForSegment(segmentIndex);
        if (range == null) {
          return Text(text, style: style, textAlign: textAlign);
        }
        final start = range.start;
        final end = range.end.clamp(start, text.length);
        final pre = start > 0 ? text.substring(0, start) : '';
        final mid = text.substring(start, end);
        final post = end < text.length ? text.substring(end) : '';
        final highlightBg = theme.colorScheme.secondaryContainer.withValues(alpha: 0.8);
        final highlightFg = theme.colorScheme.onSecondaryContainer;
        return Text.rich(
          TextSpan(children: [
            if (pre.isNotEmpty) TextSpan(text: pre, style: style),
            TextSpan(
              text: mid,
              style: (style ?? const TextStyle()).copyWith(
                backgroundColor: highlightBg,
                color: highlightFg,
                fontWeight: FontWeight.w700,
              ),
            ),
            if (post.isNotEmpty) TextSpan(text: post, style: style),
          ]),
          textAlign: textAlign,
        );
      },
    );
  }
}
