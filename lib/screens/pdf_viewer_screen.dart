import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_pdfview/flutter_pdfview.dart';
import 'package:share_plus/share_plus.dart';

class PdfViewerScreen extends StatefulWidget {
  final String path;
  final String title;

  const PdfViewerScreen({super.key, required this.path, required this.title});

  @override
  State<PdfViewerScreen> createState() => _PdfViewerScreenState();
}

class _PdfViewerScreenState extends State<PdfViewerScreen> {
  int? pages = 0;
  int? currentPage = 0;
  bool isReady = false;
  String errorMessage = '';

  static const _channel = MethodChannel(
    'com.samoondigital.yojnaplus/downloads',
  );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(''), // Hidden title as requested
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: () => _sharePdf(),
            tooltip: 'Share PDF',
          ),
          IconButton(
            icon: const Icon(Icons.download_rounded),
            onPressed: () => _savePdfToDownloads(),
            tooltip: 'Download PDF',
          ),
        ],
      ),
      body: Stack(
        children: <Widget>[
          PDFView(
            filePath: widget.path,
            enableSwipe: true,
            swipeHorizontal: false,
            autoSpacing: true,
            pageFling: true,
            pageSnap: true,
            defaultPage: currentPage!,
            fitPolicy: FitPolicy.BOTH,
            preventLinkNavigation: false,
            onRender: (pages) {
              setState(() {
                pages = pages;
                isReady = true;
              });
            },
            onError: (error) {
              setState(() {
                errorMessage = error.toString();
              });
              print(error.toString());
            },
            onPageError: (page, error) {
              setState(() {
                errorMessage = '$page: ${error.toString()}';
              });
              print('$page: ${error.toString()}');
            },
            onViewCreated: (PDFViewController pdfViewController) {
              // controller = pdfViewController;
            },
            onPageChanged: (int? page, int? total) {
              setState(() {
                currentPage = page;
              });
            },
          ),
          errorMessage.isEmpty
              ? !isReady
                    ? const Center(child: CircularProgressIndicator())
                    : Container()
              : Center(child: Text(errorMessage)),
        ],
      ),
      floatingActionButton: FutureBuilder<int>(
        future: Future.value(currentPage),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            return Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              decoration: BoxDecoration(
                color: Colors.black54,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                "${(currentPage ?? 0) + 1} / $pages",
                style: const TextStyle(color: Colors.white),
              ),
            );
          }
          return Container();
        },
      ),
    );
  }

  Future<void> _savePdfToDownloads() async {
    try {
      final File sourceFile = File(widget.path);
      if (!sourceFile.existsSync()) {
        throw Exception("Source file not found");
      }

      String filename = widget.title;
      if (!filename.toLowerCase().endsWith('.pdf')) {
        filename += '.pdf';
      }
      // Sanitizing filename
      filename = filename.replaceAll(RegExp(r'[<>:"/\\|?*]'), '_');

      final bytes = await sourceFile.readAsBytes();
      final base64Bytes = base64Encode(bytes);

      try {
        await _channel.invokeMethod('saveToDownloads', {
          'fileName': filename,
          'mimeType': 'application/pdf',
          'bytesBase64': base64Bytes,
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Saved to Downloads successfully'),
              backgroundColor: Colors.green,
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
      } on PlatformException catch (e) {
        throw Exception("Native save failed: ${e.message}");
      }
    } catch (e) {
      debugPrint("Error saving PDF: $e");
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to save: $e'),
            backgroundColor: Colors.red,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    }
  }

  Future<void> _sharePdf() async {
    try {
      final File file = File(widget.path);
      if (await file.exists()) {
        final xFile = XFile(widget.path);
        await Share.shareXFiles([xFile], text: 'Check out this PDF');
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('File not found to share')),
          );
        }
      }
    } catch (e) {
      debugPrint('Error sharing: $e');
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Error sharing: $e')));
      }
    }
  }
}
