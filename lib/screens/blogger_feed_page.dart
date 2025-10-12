import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:share_plus/share_plus.dart';
import 'package:yojna_plus/services/favorite_blog_store.dart';

const List<String> _hindiMonths = <String>[
  'जनवरी',
  'फ़रवरी',
  'मार्च',
  'अप्रैल',
  'मई',
  'जून',
  'जुलाई',
  'अगस्त',
  'सितंबर',
  'अक्टूबर',
  'नवंबर',
  'दिसंबर',
];

class BloggerFeedConfig {
  const BloggerFeedConfig({
    required this.blogId,
    required this.apiKey,
    required this.label,
    required this.title,
  });

  final String blogId;
  final String apiKey;
  final String label;
  final String title;
}

class BloggerFeedPage extends StatefulWidget {
  const BloggerFeedPage({
    super.key,
    required this.config,
    this.initialPostId,
  });

  final BloggerFeedConfig config;
  final String? initialPostId;

  @override
  State<BloggerFeedPage> createState() => _BloggerFeedState();
}

class _BloggerFeedState extends State<BloggerFeedPage> {
  static const String _authority = 'www.googleapis.com';

  http.Client? _httpClient;
  late Future<List<_BloggerPost>> _postsFuture;
  String? _pendingInitialPostId;
  bool _initialNavigationPending = false;

  @override
  void initState() {
    super.initState();
    _httpClient = http.Client();
    _postsFuture = _fetchPosts();
    _pendingInitialPostId = widget.initialPostId;
    _initialNavigationPending = widget.initialPostId != null;
  }

  @override
  void dispose() {
    _httpClient?.close();
    super.dispose();
  }

  Future<List<_BloggerPost>> _fetchPosts() async {
    final client = _httpClient;
    if (client == null) return const [];

    final queryParameters = <String, String>{
      'key': widget.config.apiKey,
      'labels': widget.config.label,
      'orderBy': 'published',
      'status': 'live',
      'view': 'READER',
      'maxResults': '30',
      'fetchBodies': 'true',
    };
    final uri = Uri.https(
      _authority,
      '/blogger/v3/blogs/${widget.config.blogId}/posts',
      queryParameters,
    );

    final response = await client
        .get(uri, headers: const {'Accept': 'application/json'})
        .timeout(const Duration(seconds: 12));

    if (response.statusCode != 200) {
      throw Exception('सर्वर त्रुटि: ${response.statusCode}');
    }

    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final items = body['items'] as List<dynamic>?;
    if (items == null) return const [];

    return items
        .map((item) => _BloggerPost.fromJson(item as Map<String, dynamic>))
        .where((post) => post.title.isNotEmpty)
        .toList();
  }

  Future<void> _handleRefresh() async {
    final posts = await _fetchPosts();
    if (!mounted) return;
    setState(() {
      _postsFuture = Future.value(posts);
      if (_pendingInitialPostId != null) {
        _initialNavigationPending = true;
      }
    });
  }

  void _openPost(_BloggerPost post) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => _BloggerPostDetailPage(
          post: post,
          label: widget.config.label,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.config.title),
      ),
      body: FutureBuilder<List<_BloggerPost>>(
        future: _postsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            final message = snapshot.error?.toString() ?? 'अज्ञात त्रुटि';
            return _BloggerFeedMessage(
              icon: Icons.wifi_off,
              title: 'डेटा लोड करने में दिक्कत',
              message: message,
              onRefresh: _handleRefresh,
            );
          }

          final posts = snapshot.data ?? const [];
          if (posts.isEmpty) {
            return _BloggerFeedMessage(
              icon: Icons.article_outlined,
              title: 'यहाँ अभी कुछ नहीं है',
              message: 'जैसे ही नया अपडेट आएगा, वह यहाँ दिखाई देगा।',
              onRefresh: _handleRefresh,
            );
          }

          if (_initialNavigationPending && _pendingInitialPostId != null) {
            final target = posts.firstWhere(
              (post) => post.id == _pendingInitialPostId,
              orElse: () => _BloggerPost.empty,
            );
            if (target != _BloggerPost.empty) {
              _initialNavigationPending = false;
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (!mounted) return;
                _openPost(target);
              });
            }
          }

          return RefreshIndicator(
            onRefresh: _handleRefresh,
            child: ListView.separated(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.only(top: 16, bottom: 32),
              itemCount: posts.length,
              itemBuilder: (context, index) {
                final post = posts[index];
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: _BloggerPostTile(
                    post: post,
                    onOpen: _openPost,
                  ),
                );
              },
              separatorBuilder: (context, index) => Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                child: Container(
                  height: 1,
                  color: Colors.blueGrey.shade100,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

class _BloggerPost {
  _BloggerPost({
    required this.id,
    required this.title,
    required this.summary,
    required this.published,
    required this.url,
    required this.bodyParagraphs,
  });

  factory _BloggerPost.fromJson(Map<String, dynamic> json) {
    final rawContent = (json['content'] as String?) ?? '';
    final paragraphs = _htmlToParagraphs(rawContent);
    final cleaned = _stripHtmlTags(rawContent);
    final effectiveParagraphs = paragraphs.isNotEmpty
        ? paragraphs
        : (cleaned.isNotEmpty ? <String>[cleaned] : const <String>[]);
    return _BloggerPost(
      id: json['id'] as String? ?? '',
      title: (json['title'] as String?)?.trim() ?? '',
      summary: _truncateWords(
        effectiveParagraphs.isNotEmpty ? effectiveParagraphs.join(' ') : cleaned,
        15,
      ),
      published: DateTime.tryParse(json['published'] as String? ?? '')?.toUtc() ??
          DateTime.now().toUtc(),
      url: json['url'] as String? ?? (json['link'] as String? ?? ''),
      bodyParagraphs: effectiveParagraphs,
    );
  }

  final String id;
  final String title;
  final String summary;
  final DateTime published;
  final String url;
  final List<String> bodyParagraphs;

  DateTime get publishedIst => published.add(const Duration(hours: 5, minutes: 30));

  static final _BloggerPost empty = _BloggerPost(
    id: '',
    title: '',
    summary: '',
    published: DateTime.fromMillisecondsSinceEpoch(0),
    url: '',
    bodyParagraphs: const <String>[],
  );
}

class _BloggerPostTile extends StatelessWidget {
  const _BloggerPostTile({
    required this.post,
    required this.onOpen,
  });

  final _BloggerPost post;
  final void Function(_BloggerPost post) onOpen;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final background = theme.colorScheme.surface;
    final borderColor = theme.colorScheme.primary.withOpacity(0.18);
    final textColor = theme.colorScheme.onSurface;
    final dateColor = theme.colorScheme.primary;
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(14),
        onTap: () {
          onOpen(post);
        },
        child: Ink(
          decoration: BoxDecoration(
            color: background,
            borderRadius: BorderRadius.circular(14),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(theme.brightness == Brightness.dark ? 0.12 : 0.08),
                blurRadius: 16,
                offset: const Offset(0, 8),
              ),
            ],
            border: Border.all(color: borderColor, width: 1.1),
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  post.title,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                    fontSize: 18,
                    color: textColor,
                    height: 1.28,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  _formatIndianDate(post.publishedIst),
                  style: theme.textTheme.labelLarge?.copyWith(
                    fontWeight: FontWeight.w600,
                    color: dateColor,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _BloggerFeedMessage extends StatelessWidget {
  const _BloggerFeedMessage({
    required this.icon,
    required this.title,
    required this.message,
    required this.onRefresh,
  });

  final IconData icon;
  final String title;
  final String message;
  final Future<void> Function() onRefresh;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 40),
        children: [
          Icon(icon, size: 64, color: theme.colorScheme.primary),
          const SizedBox(height: 16),
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),
          Text(
            message,
            style: theme.textTheme.bodyMedium,
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}

String _truncateWords(String text, int maxWords) {
  final words = text.split(RegExp(r'\s+')).where((w) => w.trim().isNotEmpty).toList();
  if (words.length <= maxWords) {
    return text.trim();
  }
  final clipped = words.take(maxWords).join(' ');
  return '$clipped…';
}

String _stripHtmlTags(String html) {
  final withoutTags = html.replaceAll(RegExp(r'<[^>]+>'), ' ');
  final withoutEntities = withoutTags.replaceAll(RegExp(r'&[^;]+;'), ' ');
  return withoutEntities.replaceAll(RegExp(r'\s+'), ' ').trim();
}

String _formatIndianDate(DateTime dateTime) {
  final day = dateTime.day.toString().padLeft(2, '0');
  final month = _hindiMonths[dateTime.month - 1];
  final year = dateTime.year;
  return '$day $month $year';
}

String _formatIndianTimestamp(DateTime dateTime) {
  final day = dateTime.day.toString().padLeft(2, '0');
  final month = _hindiMonths[dateTime.month - 1];
  final year = dateTime.year;

  final hour = dateTime.hour % 12 == 0 ? 12 : dateTime.hour % 12;
  final minute = dateTime.minute.toString().padLeft(2, '0');
  final period = dateTime.hour >= 12 ? 'PM' : 'AM';

  return '$day $month $year, ${hour.toString().padLeft(2, '0')}:$minute $period';
}

class _BloggerPostDetailPage extends StatefulWidget {
  const _BloggerPostDetailPage({required this.post, required this.label});

  final _BloggerPost post;
  final String label;

  @override
  State<_BloggerPostDetailPage> createState() => _BloggerPostDetailPageState();
}

class _BloggerPostDetailPageState extends State<_BloggerPostDetailPage> {
  bool _isFavorite = false;
  bool _loadingFavorite = true;

  @override
  void initState() {
    super.initState();
    _loadFavoriteState();
  }

  Future<void> _loadFavoriteState() async {
    final isFav = await FavoriteBlogStore.instance.isFavorite(widget.post.id);
    if (!mounted) return;
    setState(() {
      _isFavorite = isFav;
      _loadingFavorite = false;
    });
  }

  Future<void> _toggleFavorite() async {
    if (_loadingFavorite) return;
    setState(() {
      _loadingFavorite = true;
    });
    final entry = FavoriteBlogEntry(
      id: widget.post.id,
      title: widget.post.title,
      url: widget.post.url,
      label: widget.label,
      publishedIso: widget.post.published.toIso8601String(),
      summary: widget.post.summary.isNotEmpty ? widget.post.summary : null,
    );
    final added = await FavoriteBlogStore.instance.toggleFavorite(entry);
    if (!mounted) return;
    setState(() {
      _isFavorite = added;
      _loadingFavorite = false;
    });
    final message = added ? 'पसंदीदा में जोड़ा गया' : 'पसंदीदा से हटाया गया';
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final paragraphs = widget.post.bodyParagraphs;
    return Scaffold(
      appBar: AppBar(
        title: null,
        actions: [
          IconButton(
            tooltip: _isFavorite ? 'पसंदीदा से हटाएँ' : 'पसंदीदा में जोड़ें',
            iconSize: 28,
            onPressed: _loadingFavorite ? null : _toggleFavorite,
            icon: AnimatedSwitcher(
              duration: const Duration(milliseconds: 200),
              child: _loadingFavorite
                  ? const SizedBox(
                      key: ValueKey('fav-loading'),
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(strokeWidth: 2.4),
                    )
                  : Icon(
                      _isFavorite
                          ? Icons.star_rounded
                          : Icons.star_border_rounded,
                      key: ValueKey<bool>(_isFavorite),
                      size: 28,
                    ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(right: 20),
            child: IconButton(
              tooltip: 'शेयर करें',
              icon: const Icon(Icons.share_rounded),
              onPressed: () {
                final post = widget.post;
                final buffer = StringBuffer(post.title);
                if (post.url.isNotEmpty) {
                  buffer
                    ..writeln()
                    ..write(post.url);
                } else if (post.bodyParagraphs.isNotEmpty) {
                  buffer
                    ..writeln()
                    ..write(post.bodyParagraphs.take(2).join('\n'));
                }

                buffer
                  ..writeln()
                  ..writeln()
                  ..writeln('देखने के लिए Online Yojna ऐप को डाउनलोड करें:')
                  ..write(
                    'https://play.google.com/store/apps/details?id=com.samoondigital.yojnaplus&pcampaignid=web_share',
                  );

                Share.share(
                  buffer.toString(),
                  subject: post.title,
                );
              },
            ),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 24, 20, 32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.post.title,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.w800,
                height: 1.2,
              ),
            ),
            const SizedBox(height: 12),
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Icon(
                  Icons.schedule_rounded,
                  size: 18,
                  color: theme.colorScheme.primary,
                ),
                const SizedBox(width: 8),
                Text(
                  _formatIndianTimestamp(widget.post.publishedIst),
                  style: theme.textTheme.labelLarge?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: theme.colorScheme.primary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            if (paragraphs.isEmpty)
              Text(
                'इस पोस्ट की सामग्री उपलब्ध नहीं है।',
                style: theme.textTheme.bodyLarge?.copyWith(
                  fontWeight: FontWeight.w600,
                  height: 1.5,
                ),
              )
            else
              ...[
                for (var i = 0; i < paragraphs.length; i++) ...[
                  Text(
                    paragraphs[i],
                    style: theme.textTheme.bodyLarge?.copyWith(
                      fontWeight: FontWeight.w600,
                      height: 1.5,
                    ),
                  ),
                  if (i != paragraphs.length - 1) const SizedBox(height: 18),
                ],
              ],
          ],
        ),
      ),
    );
  }
}

List<String> _htmlToParagraphs(String html) {
  if (html.trim().isEmpty) {
    return const <String>[];
  }

  var working = html
      .replaceAll(RegExp(r'<br\s*/?>', caseSensitive: false), '\n')
      .replaceAll(RegExp(r'</p>', caseSensitive: false), '\n')
      .replaceAll(RegExp(r'</div>', caseSensitive: false), '\n')
      .replaceAll(RegExp(r'</h[1-6]>', caseSensitive: false), '\n')
      .replaceAll(RegExp(r'</li>', caseSensitive: false), '\n')
      .replaceAll(RegExp(r'<li[^>]*>', caseSensitive: false), '• ');

  working = working.replaceAll(RegExp(r'<[^>]+>'), '');
  working = working.replaceAll(RegExp(r'&nbsp;', caseSensitive: false), ' ');
  working = working.replaceAll(RegExp(r'&amp;', caseSensitive: false), '&');
  working = working.replaceAll(RegExp(r'&quot;', caseSensitive: false), '"');
  working = working.replaceAll(RegExp(r'&#39;'), "'");
  working = working.replaceAll(RegExp(r'&rsquo;', caseSensitive: false), "'");
  working = working.replaceAll(RegExp(r'&lsquo;', caseSensitive: false), "'");
  working = working.replaceAll(RegExp(r'&ldquo;', caseSensitive: false), '"');
  working = working.replaceAll(RegExp(r'&rdquo;', caseSensitive: false), '"');

  return working
      .split(RegExp(r'\n+'))
      .map((line) => line.replaceAll(RegExp(r'[ \t]+'), ' ').trim())
      .where((line) => line.isNotEmpty)
      .toList();
}
