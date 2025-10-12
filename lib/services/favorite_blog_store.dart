import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FavoriteBlogEntry {
  FavoriteBlogEntry({
    required this.id,
    required this.title,
    required this.url,
    required this.label,
    required this.publishedIso,
    this.summary,
  });

  factory FavoriteBlogEntry.fromJson(Map<String, dynamic> json) {
    return FavoriteBlogEntry(
      id: json['id'] as String? ?? '',
      title: json['title'] as String? ?? '',
      url: json['url'] as String? ?? '',
      label: json['label'] as String? ?? '',
      publishedIso: json['publishedIso'] as String? ?? '',
      summary: json['summary'] as String?,
    );
  }

  final String id;
  final String title;
  final String url;
  final String label;
  final String publishedIso;
  final String? summary;

  DateTime? get publishedUtc =>
      publishedIso.isNotEmpty ? DateTime.tryParse(publishedIso) : null;

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': id,
      'title': title,
      'url': url,
      'label': label,
      'publishedIso': publishedIso,
      'summary': summary,
    };
  }
}

class FavoriteBlogStore {
  FavoriteBlogStore._();

  static final FavoriteBlogStore instance = FavoriteBlogStore._();

  static const String _prefsKey = 'blog_favorites_v1';

  final ValueNotifier<List<FavoriteBlogEntry>> favoritesNotifier =
      ValueNotifier<List<FavoriteBlogEntry>>(<FavoriteBlogEntry>[]);

  bool _loaded = false;

  Future<void> _ensureLoaded() async {
    if (_loaded) return;
    final prefs = await SharedPreferences.getInstance();
    final rawList = prefs.getStringList(_prefsKey) ?? <String>[];
    final entries = rawList
        .map((raw) => FavoriteBlogEntry.fromJson(
              jsonDecode(raw) as Map<String, dynamic>,
            ))
        .toList(growable: false);
    favoritesNotifier.value = List<FavoriteBlogEntry>.unmodifiable(entries);
    _loaded = true;
  }

  Future<List<FavoriteBlogEntry>> getFavorites() async {
    await _ensureLoaded();
    return List<FavoriteBlogEntry>.unmodifiable(favoritesNotifier.value);
  }

  Future<bool> isFavorite(String id) async {
    if (id.isEmpty) return false;
    await _ensureLoaded();
    return favoritesNotifier.value.any((entry) => entry.id == id);
  }

  Future<bool> toggleFavorite(FavoriteBlogEntry entry) async {
    await _ensureLoaded();
    final current = List<FavoriteBlogEntry>.from(favoritesNotifier.value);
    final existingIndex = current.indexWhere((item) => item.id == entry.id);
    final added = existingIndex == -1;
    if (added) {
      current.insert(0, entry);
    } else {
      current.removeAt(existingIndex);
    }
    favoritesNotifier.value = List<FavoriteBlogEntry>.unmodifiable(current);
    await _persist(current);
    return added;
  }

  Future<void> removeById(String id) async {
    if (id.isEmpty) return;
    await _ensureLoaded();
    final current = List<FavoriteBlogEntry>.from(favoritesNotifier.value);
    current.removeWhere((entry) => entry.id == id);
    favoritesNotifier.value = List<FavoriteBlogEntry>.unmodifiable(current);
    await _persist(current);
  }

  Future<void> _persist(List<FavoriteBlogEntry> entries) async {
    final prefs = await SharedPreferences.getInstance();
    final encoded = entries.map((entry) => jsonEncode(entry.toJson())).toList();
    await prefs.setStringList(_prefsKey, encoded);
  }
}
