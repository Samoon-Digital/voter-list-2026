import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:share_plus/share_plus.dart';

import '../services/downloads_store.dart';

class DownloadsScreen extends StatefulWidget {
  const DownloadsScreen({super.key});

  @override
  State<DownloadsScreen> createState() => _DownloadsScreenState();
}

class _DownloadsScreenState extends State<DownloadsScreen> {
  late Future<List<DownloadEntry>> _future;
  static const MethodChannel _downloadChannel = MethodChannel('com.samoondigital.yojnaplus/downloads');

  @override
  void initState() {
    super.initState();
    _future = DownloadsStore.getAll();
    // Try resolving missing content URIs shortly after opening
    WidgetsBinding.instance.addPostFrameCallback((_) => _resolveMissingUris());
  }

  Future<void> _resolveMissingUris() async {
    final items = await DownloadsStore.getAll();
    for (final e in items) {
      if (e.viaDownloadManager && e.contentUri == null && e.dmId != null) {
        try {
          final uri = await _downloadChannel.invokeMethod<String>('getDownloadedFileUri', {'id': e.dmId});
          if (uri != null && uri.isNotEmpty) {
            await DownloadsStore.updateContentUri(e.id, contentUri: uri);
          }
        } catch (_) {}
      }
    }
    if (mounted) {
      setState(() {
        _future = DownloadsStore.getAll();
      });
    }
  }

  Future<void> _refresh() async {
    setState(() {
      _future = DownloadsStore.getAll();
    });
    await _resolveMissingUris();
  }

  Future<void> _openFolder() async {
    try {
      await _downloadChannel.invokeMethod('openDownloadsUI');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('डाउनलोड फ़ोल्डर नहीं खुल सका।')), 
      );
    }
  }

  String _formatIndianTs(int tsMs) {
    final dt = DateTime.fromMillisecondsSinceEpoch(tsMs);
    final d = dt.day.toString().padLeft(2, '0');
    final m = dt.month.toString().padLeft(2, '0');
    final y = dt.year.toString();
    final h12 = ((dt.hour + 11) % 12) + 1;
    final mm = dt.minute.toString().padLeft(2, '0');
    final ap = dt.hour >= 12 ? 'PM' : 'AM';
    return '$d/$m/$y, $h12:$mm $ap';
  }

  Future<void> _shareItem(DownloadEntry e) async {
    if (e.path != null && e.path!.isNotEmpty) {
      await Share.shareXFiles([XFile(e.path!, mimeType: e.mimeType, name: e.fileName)]);
    } else if (e.contentUri != null && e.contentUri!.isNotEmpty) {
      await Share.share('फ़ाइल: ${e.fileName}\nURI: ${e.contentUri}');
    } else {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('शेयर करने के लिए फ़ाइल उपलब्ध नहीं।')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('डाउनलोड की गई फ़ाइलें'),
        actions: [
          IconButton(
            tooltip: 'फ़ोल्डर खोलें',
            icon: const Icon(Icons.folder_open),
            onPressed: _openFolder,
          ),
          IconButton(
            tooltip: 'रिफ्रेश',
            icon: const Icon(Icons.refresh),
            onPressed: _refresh,
          ),
        ],
      ),
      body: FutureBuilder<List<DownloadEntry>>(
        future: _future,
        builder: (context, snap) {
          final items = snap.data ?? const <DownloadEntry>[];
          if (snap.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (items.isEmpty) {
            return const Center(child: Text('अभी तक कोई फाइल नहीं।'));
          }
          return ListView.separated(
            itemCount: items.length,
            separatorBuilder: (_, __) => const Divider(height: 0),
            itemBuilder: (context, i) {
              final e = items[i];
              final loc = e.path?.isNotEmpty == true
                  ? e.path!
                  : (e.contentUri?.isNotEmpty == true ? e.contentUri! : 'स्थान उपलब्ध नहीं');
              return ListTile(
                leading: const Icon(Icons.insert_drive_file_outlined),
                title: Text(e.fileName, maxLines: 1, overflow: TextOverflow.ellipsis),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(loc, maxLines: 1, overflow: TextOverflow.ellipsis),
                    const SizedBox(height: 2),
                    Text(_formatIndianTs(e.timestampMs), style: Theme.of(context).textTheme.bodySmall),
                  ],
                ),
                trailing: Wrap(spacing: 6, children: [
                  IconButton(
                    tooltip: 'फ़ोल्डर',
                    icon: const Icon(Icons.folder_open),
                    onPressed: _openFolder,
                  ),
                  IconButton(
                    tooltip: 'शेयर',
                    icon: const Icon(Icons.share),
                    onPressed: () => _shareItem(e),
                  ),
                ]),
              );
            },
          );
        },
      ),
    );
  }
}
