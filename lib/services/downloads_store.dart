import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

class DownloadEntry {
  final String id; // unique id
  final String fileName;
  final String? mimeType;
  final String? path; // absolute file path if known
  final String? contentUri; // for DownloadManager items
  final bool viaDownloadManager;
  final int timestampMs;
  final int? dmId; // Android DownloadManager id

  DownloadEntry({
    required this.id,
    required this.fileName,
    required this.viaDownloadManager,
    required this.timestampMs,
    this.mimeType,
    this.path,
    this.contentUri,
    this.dmId,
  });

  Map<String, dynamic> toJson() => {
        'id': id,
        'fileName': fileName,
        'mimeType': mimeType,
        'path': path,
        'contentUri': contentUri,
        'viaDM': viaDownloadManager,
        'ts': timestampMs,
        'dmId': dmId,
      };

  static DownloadEntry fromJson(Map<String, dynamic> json) => DownloadEntry(
        id: json['id'] as String,
        fileName: json['fileName'] as String,
        mimeType: json['mimeType'] as String?,
        path: json['path'] as String?,
        contentUri: json['contentUri'] as String?,
        viaDownloadManager: (json['viaDM'] as bool?) ?? false,
        timestampMs: (json['ts'] as num?)?.toInt() ?? DateTime.now().millisecondsSinceEpoch,
        dmId: (json['dmId'] as num?)?.toInt(),
      );
}

class DownloadsStore {
  static const _kKey = 'downloads_v1';

  static Future<List<DownloadEntry>> getAll() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_kKey);
    if (raw == null || raw.isEmpty) return [];
    try {
      final list = (jsonDecode(raw) as List).cast<Map<String, dynamic>>();
      return list.map((e) => DownloadEntry.fromJson(e)).toList()
        ..sort((a, b) => b.timestampMs.compareTo(a.timestampMs));
    } catch (_) {
      return [];
    }
  }

  static Future<void> add(DownloadEntry entry) async {
    final items = await getAll();
    final updated = [entry, ...items];
    await _save(updated);
  }

  static Future<void> updateContentUri(String id, {String? contentUri, String? path, String? mimeType}) async {
    final items = await getAll();
    final updated = items.map((e) {
      if (e.id == id) {
        return DownloadEntry(
          id: e.id,
          fileName: e.fileName,
          viaDownloadManager: e.viaDownloadManager,
          timestampMs: e.timestampMs,
          mimeType: mimeType ?? e.mimeType,
          path: path ?? e.path,
          contentUri: contentUri ?? e.contentUri,
          dmId: e.dmId,
        );
      }
      return e;
    }).toList();
    await _save(updated);
  }

  static Future<void> remove(String id) async {
    final items = await getAll();
    final updated = items.where((e) => e.id != id).toList();
    await _save(updated);
  }

  static Future<void> clear() async {
    await _save([]);
  }

  static Future<void> _save(List<DownloadEntry> items) async {
    final prefs = await SharedPreferences.getInstance();
    final data = jsonEncode(items.map((e) => e.toJson()).toList());
    await prefs.setString(_kKey, data);
  }
}
