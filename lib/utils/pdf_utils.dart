import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/pdf_viewer_screen.dart';

void openPdfViewer(
  BuildContext context, {
  required String title,
  Uint8List? bytes,
  String? filePath,
}) {
  assert(
    bytes != null || filePath != null,
    'bytes या filePath में से कम से कम एक दें',
  );
  Navigator.of(context).push(
    MaterialPageRoute(
      builder: (_) =>
          SharedPdfViewerScreen(title: title, bytes: bytes, filePath: filePath),
    ),
  );
}
