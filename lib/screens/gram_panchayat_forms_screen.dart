import 'dart:io';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:file_saver/file_saver.dart';
import 'package:yojna_plus/utils/pdf_utils.dart';
import 'package:yojna_plus/widgets/native_ad_banner_section.dart';
import 'package:flutter/services.dart';
import 'package:yojna_plus/services/downloads_store.dart';
import 'package:yojna_plus/widgets/gradient_folder_icon.dart';

const MethodChannel _downloadChannel = MethodChannel('com.samoondigital.yojnaplus/downloads');

const String _pdfRawUrl =
    'https://raw.githubusercontent.com/Samoon-Digital/Gram-Panchayat-Documents-/main/parmaan%20patr.pdf';
// Removed legacy external storage permission key; using SAF or app-specific storage.

class GramPanchayatFormsScreen extends StatefulWidget {
  const GramPanchayatFormsScreen({super.key});

  @override
  State<GramPanchayatFormsScreen> createState() => _GramPanchayatFormsScreenState();
}

Future<void> _openDownloadsFolder(BuildContext context) async {
  try {
    await _downloadChannel.invokeMethod('openDownloadsUI');
  } catch (_) {
    ScaffoldMessenger.maybeOf(context)?.showSnackBar(
      const SnackBar(content: Text('डाउनलोड फ़ोल्डर नहीं खोल पाए।')),
    );
  }
}

class _GramPanchayatFormsScreenState extends State<GramPanchayatFormsScreen> {
  Uint8List? _pdfBytes;
  bool _loading = false;
  String? _error;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  Future<void> _loadPdf() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final response = await http.get(Uri.parse(_pdfRawUrl));
      if (response.statusCode != 200) {
        throw HttpException('HTTP ${response.statusCode}');
      }
      final bytes = response.bodyBytes;
      if (!mounted) return;
      setState(() {
        _pdfBytes = bytes;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }
  Future<void> _ensurePdfLoaded() async {
    if (_pdfBytes != null || _loading) return;
    await _loadPdf();
  }

  Future<void> _downloadPdf() async {
    try {
      _showDownloadSnackbar();
      final response = await http.get(Uri.parse(_pdfRawUrl));
      if (response.statusCode != 200) {
        throw HttpException('HTTP ${response.statusCode}');
      }
      final bytes = response.bodyBytes;

      // Preserve original filename from URL
      String fileName = 'parmaan patr.pdf';
      try {
        final seg = Uri.parse(_pdfRawUrl).pathSegments;
        if (seg.isNotEmpty) {
          final last = Uri.decodeComponent(seg.last);
          if (last.toLowerCase().endsWith('.pdf')) fileName = last;
        }
      } catch (_) {}

      // Primary: save via SAF/MediaStore and log entry
      try {
        final saved = await _downloadChannel.invokeMethod<String>('saveToDownloads', {
          'fileName': fileName,
          'mimeType': 'application/pdf',
          'bytesBase64': base64Encode(bytes),
        });
        String? path;
        String? contentUri;
        if (saved != null && saved.isNotEmpty) {
          if (saved.startsWith('content://')) {
            contentUri = saved;
          } else {
            path = saved;
          }
        }
        final entryId = DateTime.now().microsecondsSinceEpoch.toString();
        await DownloadsStore.add(DownloadEntry(
          id: entryId,
          fileName: fileName,
          viaDownloadManager: false,
          timestampMs: DateTime.now().millisecondsSinceEpoch,
          mimeType: 'application/pdf',
          path: path,
          contentUri: contentUri,
        ));
        if (!mounted) return;
        _pdfBytes = bytes;
        ScaffoldMessenger.of(context).hideCurrentSnackBar();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(saved == null || saved.isEmpty || saved == 'downloads' ? 'फाइल “Downloads” में सहेजी गई' : 'फाइल सहेजी गई: $saved')),
        );
        return;
      } catch (_) {
        // Fall back to FileSaver/app storage below
      }

      // Fallback 1: FileSaver
      try {
        final result = await FileSaver.instance.saveFile(
          name: p.basenameWithoutExtension(fileName),
          bytes: bytes,
          fileExtension: 'pdf',
          mimeType: MimeType.other,
          customMimeType: 'application/pdf',
        );
        String? path;
        String? contentUri;
        if (result.isNotEmpty) {
          if (result.startsWith('content://')) {
            contentUri = result;
          } else {
            path = result;
          }
        }
        final entryId = DateTime.now().microsecondsSinceEpoch.toString();
        await DownloadsStore.add(DownloadEntry(
          id: entryId,
          fileName: fileName,
          viaDownloadManager: false,
          timestampMs: DateTime.now().millisecondsSinceEpoch,
          mimeType: 'application/pdf',
          path: path,
          contentUri: contentUri,
        ));
        if (!mounted) return;
        _pdfBytes = bytes;
        ScaffoldMessenger.of(context).hideCurrentSnackBar();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result.isEmpty ? 'फाइल “Downloads” में सहेजी गई' : 'फाइल सहेजी गई: $result')),
        );
        return;
      } catch (_) {}

      // Fallback 2: App-specific storage
      final baseDir = await getExternalStorageDirectory() ?? await getApplicationDocumentsDirectory();
      final folder = Directory(p.join(baseDir.path, 'Online Yojna'));
      if (!await folder.exists()) {
        await folder.create(recursive: true);
      }
      final file = File(p.join(folder.path, fileName));
      await file.writeAsBytes(bytes, flush: true);
      final entryId = DateTime.now().microsecondsSinceEpoch.toString();
      await DownloadsStore.add(DownloadEntry(
        id: entryId,
        fileName: fileName,
        viaDownloadManager: false,
        timestampMs: DateTime.now().millisecondsSinceEpoch,
        mimeType: 'application/pdf',
        path: file.path,
      ));

      if (!mounted) return;
      _pdfBytes = bytes;
      ScaffoldMessenger.of(context).hideCurrentSnackBar();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('फाइल सहेजी गई: ${file.path}')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).hideCurrentSnackBar();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('डाउनलोड विफल: $e')),
      );
    }
  }

  void _showDownloadSnackbar() {
    final theme = Theme.of(context);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const SizedBox(
              width: 22,
              height: 22,
              child: CircularProgressIndicator(strokeWidth: 2.4),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    'PDF डाउनलोड हो रहा है...',
                    style: TextStyle(fontWeight: FontWeight.w600),
                  ),
                  SizedBox(height: 2),
                  Text(
                    'कृपया थोड़ा इंतजार करें।',
                    style: TextStyle(fontSize: 12),
                  ),
                ],
              ),
            ),
          ],
        ),
        backgroundColor: theme.colorScheme.surface,
        behavior: SnackBarBehavior.floating,
        elevation: 6,
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        duration: const Duration(seconds: 4),
      ),
    );
  }

  void _openFullScreen() {
    final bytes = _pdfBytes;
    if (bytes == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('कृपया पहले "फॉर्म देखें" या डाउनलोड करें।')),
      );
      return;
    }
    openPdfViewer(
      context,
      title: 'जाति, आय, निवास प्रमाण पत्र',
      bytes: bytes,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('ग्राम पंचायत PDF फॉर्म'),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 20),
            child: IconButton(
              tooltip: 'डाउनलोड',
              icon: const GradientFolderIcon(size: 24),
              onPressed: () => _openDownloadsFolder(context),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Icon(Icons.assignment_ind_rounded, color: theme.colorScheme.primary),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'जाति, आय, निवास प्रमाण पत्र',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'ग्राम प्रधान द्वारा प्रमाणित होने वाला प्रमाण पत्र।',
                    style: theme.textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 16),
                  Center(
                    child: Wrap(
                      alignment: WrapAlignment.center,
                      spacing: 12,
                      runSpacing: 8,
                      children: [
                        ElevatedButton.icon(
                          icon: const Icon(Icons.visibility_rounded),
                          label: _loading
                              ? const SizedBox(
                                  width: 18,
                                  height: 18,
                                  child: CircularProgressIndicator(strokeWidth: 2.2),
                                )
                              : const Text('फॉर्म देखें'),
                          style: ElevatedButton.styleFrom(
                            minimumSize: const Size(160, 48),
                            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                            textStyle: const TextStyle(fontWeight: FontWeight.w700),
                          ),
                          onPressed: _loading
                              ? null
                              : () async {
                                  await _ensurePdfLoaded();
                                  if (!mounted) return;
                                  if (_error != null) {
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      SnackBar(content: Text('PDF लोड करने में समस्या: $_error')),
                                    );
                                    return;
                                  }
                                  _openFullScreen();
                                },
                        ),
                        ElevatedButton.icon(
                          icon: const Icon(Icons.download_rounded),
                          label: const Text('फॉर्म डाउनलोड करें'),
                          style: ElevatedButton.styleFrom(
                            minimumSize: const Size(160, 48),
                            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                            textStyle: const TextStyle(fontWeight: FontWeight.w700),
                          ),
                          onPressed: _downloadPdf,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                  if (_error != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        'PDF लोड करने में समस्या: $_error',
                        style: theme.textTheme.bodySmall
                            ?.copyWith(color: theme.colorScheme.error, fontWeight: FontWeight.w600),
                        textAlign: TextAlign.center,
                      ),
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          const NativeAdBannerSection(
            adUnitId: 'ca-app-pub-1638673809508848/2638737906',
          ),
        ],
      ),
    );
  }
}
