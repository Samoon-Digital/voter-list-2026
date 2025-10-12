import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:pdfx/pdfx.dart';
import 'package:share_plus/share_plus.dart';

class SharedPdfViewerScreen extends StatefulWidget {
  const SharedPdfViewerScreen({
    super.key,
    required this.title,
    this.bytes,
    this.filePath,
  }) : assert(bytes != null || filePath != null, 'PDF bytes या filePath में से एक प्रदान करें');

  final String title;
  final Uint8List? bytes;
  final String? filePath;

  @override
  State<SharedPdfViewerScreen> createState() => _SharedPdfViewerScreenState();
}

class _SharedPdfViewerScreenState extends State<SharedPdfViewerScreen> {
  PdfControllerPinch? _controller;
  bool _initializing = true;
  String? _error;
  String? _password;
  bool _askedPasswordOnce = false;

  @override
  void initState() {
    super.initState();
    _reopenDocument();
  }

  Future<void> _waitForDocument(Future<PdfDocument> future) async {
    setState(() {
      _initializing = true;
      _error = null;
    });
    try {
      await future;
      if (!mounted) return;
      setState(() => _initializing = false);
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString();
        _initializing = false;
      });
      // If password likely or any first-time failure, prompt once
      if (!_askedPasswordOnce || _looksLikePasswordError(_error)) {
        _askedPasswordOnce = true;
        _promptPassword();
      }
    }
  }

  Future<PdfDocument> _openDocumentFuture() {
    if (widget.bytes != null) {
      return PdfDocument.openData(widget.bytes!, password: _password);
    }
    return PdfDocument.openFile(widget.filePath!, password: _password);
  }

  bool _looksLikePasswordError(String? err) {
    if (err == null) return false;
    final e = err.toLowerCase();
    return e.contains('password') || e.contains('encrypt');
  }

  Future<void> _promptPassword() async {
    final ctrl = TextEditingController();
    final pass = await showDialog<String>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('PDF पासवर्ड दर्ज करें'),
          content: TextField(
            controller: ctrl,
            obscureText: true,
            decoration: const InputDecoration(hintText: 'पासवर्ड'),
            onSubmitted: (_) => Navigator.of(context).pop(ctrl.text.trim()),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('रद्द करें'),
            ),
            FilledButton(
              onPressed: () => Navigator.of(context).pop(ctrl.text.trim()),
              child: const Text('ओके'),
            ),
          ],
        );
      },
    );
    if (pass == null || pass.isEmpty) return;
    _password = pass;
    _reopenDocument();
  }

  void _reopenDocument() {
    final future = _openDocumentFuture();
    final old = _controller;
    final ctrl = PdfControllerPinch(document: future);
    _controller = ctrl;
    setState(() {
      _initializing = true;
      _error = null;
    });
    _waitForDocument(future);
    if (old != null) {
      // Dispose old after frame
      WidgetsBinding.instance.addPostFrameCallback((_) => old.dispose());
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  Future<void> _sharePdf() async {
    try {
      if (widget.filePath != null) {
        await Share.shareXFiles(
          [XFile(widget.filePath!, mimeType: 'application/pdf')],
          subject: widget.title,
        );
      } else if (widget.bytes != null) {
        await Share.shareXFiles(
          [XFile.fromData(widget.bytes!, mimeType: 'application/pdf', name: '${widget.title}.pdf')],
          subject: widget.title,
        );
      }
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('PDF साझा करने में समस्या: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title, overflow: TextOverflow.ellipsis),
        actions: [
          IconButton(
            icon: const Icon(Icons.share_rounded),
            tooltip: 'शेयर करें',
            onPressed: _sharePdf,
          ),
          IconButton(
            icon: const Icon(Icons.lock_open_rounded),
            tooltip: 'पासवर्ड दर्ज करें',
            onPressed: _promptPassword,
          ),
        ],
      ),
      body: _buildBody(theme),
    );
  }

  Widget _buildBody(ThemeData theme) {
    if (_initializing) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_error != null) {
      return Center(child: Text('PDF खोलने में समस्या: $_error'));
    }
    return PdfViewPinch(
      controller: _controller!,
      onDocumentError: (error) {
        if (!mounted) return;
        setState(() => _error = error.toString());
      },
      backgroundDecoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.1),
      ),
    );
  }
}
