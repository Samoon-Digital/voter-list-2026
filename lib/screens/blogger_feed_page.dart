import 'dart:async';
import 'dart:convert';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:share_plus/share_plus.dart';
import 'package:yojna_plus/services/favorite_blog_store.dart';
import 'package:yojna_plus/widgets/movie_ticket_native_ad.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:html/parser.dart' as html_parser;
import 'package:html/dom.dart' as dom;

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

class _BloggerPage {
  const _BloggerPage({required this.posts, this.nextPageToken});

  final List<_BloggerPost> posts;
  final String? nextPageToken;

  static const _BloggerPage empty = _BloggerPage(
    posts: <_BloggerPost>[],
    nextPageToken: null,
  );
}

class _BlogRichParagraph extends StatelessWidget {
  const _BlogRichParagraph({
    required this.html,
    required this.onTapLink,
    required this.registerRecognizer,
    required this.linkColor,
    this.baseStyle,
  });

  final String html;
  final void Function(String url) onTapLink;
  final void Function(TapGestureRecognizer recognizer) registerRecognizer;
  final Color linkColor;
  final TextStyle? baseStyle;

  @override
  Widget build(BuildContext context) {
    final defaultStyle = baseStyle ?? DefaultTextStyle.of(context).style;
    final doc = html_parser.parseFragment(html);
    final spans = _parseNodes(doc.nodes, defaultStyle);
    return RichText(
      text: TextSpan(children: spans, style: defaultStyle),
      textAlign: TextAlign.start,
    );
  }

  List<InlineSpan> _linkifyText(String text, TextStyle style) {
    final spans = <InlineSpan>[];
    final regex = RegExp(
      r'''((https?:\/\/)|(www\.))[\w\-.~:\/?#%\[\]@!$&'"()*+,;=]+''',
      caseSensitive: false,
    );
    var start = 0;
    for (final match in regex.allMatches(text)) {
      if (match.start > start) {
        spans.add(
          TextSpan(text: text.substring(start, match.start), style: style),
        );
      }
      var raw = match.group(0) ?? '';
      if (raw.isEmpty) {
        start = match.end;
        continue;
      }
      final trimmed = raw.replaceFirst(RegExp(r'[\)\]\.,;!?]+$'), '');
      final trailing = raw.substring(trimmed.length);
      final normalized =
          RegExp(r'^https?://', caseSensitive: false).hasMatch(trimmed)
          ? trimmed
          : 'https://$trimmed';
      final recognizer = TapGestureRecognizer()
        ..onTap = () => onTapLink(normalized);
      registerRecognizer(recognizer);
      spans.add(
        TextSpan(
          text: trimmed,
          style: style.merge(
            TextStyle(color: linkColor, decoration: TextDecoration.underline),
          ),
          recognizer: recognizer,
        ),
      );
      if (trailing.isNotEmpty) {
        spans.add(TextSpan(text: trailing, style: style));
      }
      start = match.end;
    }
    if (start < text.length) {
      spans.add(TextSpan(text: text.substring(start), style: style));
    }
    return spans;
  }

  List<InlineSpan> _parseNodes(List<dom.Node> nodes, TextStyle style) {
    final spans = <InlineSpan>[];
    for (final node in nodes) {
      if (node.nodeType == dom.Node.TEXT_NODE) {
        final text = node.text ?? '';
        if (text.isEmpty) {
          continue;
        }
        final linkified = _linkifyText(text, style);
        if (linkified.isEmpty) {
          spans.add(TextSpan(text: text, style: style));
        } else {
          spans.addAll(linkified);
        }
      } else if (node is dom.Element) {
        final element = node;
        final name = element.localName?.toLowerCase() ?? '';
        var childStyle = style;

        if (name == 'strong' || name == 'b') {
          childStyle = childStyle.merge(
            const TextStyle(fontWeight: FontWeight.w700),
          );
        } else if (name == 'em' || name == 'i') {
          childStyle = childStyle.merge(
            const TextStyle(fontStyle: FontStyle.italic),
          );
        } else if (name == 'u') {
          childStyle = childStyle.merge(
            const TextStyle(decoration: TextDecoration.underline),
          );
        }

        childStyle = _applyInlineColor(childStyle, element);

        if (name == 'a') {
          final href = element.attributes['href']?.trim() ?? '';
          final childSpans = _parseNodes(
            element.nodes,
            childStyle.merge(
              const TextStyle(decoration: TextDecoration.underline),
            ),
          );
          if (href.isNotEmpty) {
            final normalized = _normalizeUrl(href);
            final hasInline = _hasInlineColor(element);
            final recognizer = TapGestureRecognizer()
              ..onTap = () => onTapLink(normalized);
            registerRecognizer(recognizer);
            final linkStyle = childStyle.merge(
              TextStyle(
                decoration: TextDecoration.underline,
                color: hasInline ? (childStyle.color ?? linkColor) : linkColor,
              ),
            );
            if (childSpans.isEmpty) {
              final t = element.text.trim();
              final displayText = t.isNotEmpty ? t : normalized;
              spans.add(
                TextSpan(
                  text: displayText,
                  style: linkStyle,
                  recognizer: recognizer,
                ),
              );
            } else {
              // Force blue color on all descendants if no inline color on <a>
              final decorated = _decorateLinkChildren(
                childSpans,
                recognizer,
                forceColor: hasInline ? null : linkColor,
              );
              spans.add(
                TextSpan(
                  children: decorated,
                  style: linkStyle,
                  recognizer: recognizer,
                ),
              );
            }
          } else {
            spans.addAll(childSpans);
          }
        } else {
          spans.addAll(_parseNodes(element.nodes, childStyle));
        }
      }
    }
    return spans;
  }

  bool _hasInlineColor(dom.Element element) {
    final styleAttr = element.attributes['style'] ?? '';
    if (RegExp(r'color\s*:', caseSensitive: false).hasMatch(styleAttr)) {
      return true;
    }
    final colorAttr = element.attributes['color'];
    if (colorAttr != null && colorAttr.trim().isNotEmpty) return true;
    return false;
  }

  List<InlineSpan> _decorateLinkChildren(
    List<InlineSpan> children,
    TapGestureRecognizer recognizer, {
    Color? forceColor,
  }) {
    InlineSpan mapSpan(InlineSpan span) {
      if (span is TextSpan) {
        final TextStyle base = span.style ?? const TextStyle();
        TextStyle updated = base.merge(
          const TextStyle(decoration: TextDecoration.underline),
        );
        if (forceColor != null) {
          updated = updated.copyWith(color: forceColor);
        }
        final newChildren = span.children?.map(mapSpan).toList();
        return TextSpan(
          text: span.text,
          children: newChildren,
          style: updated,
          recognizer: span.recognizer ?? recognizer,
        );
      }
      return span;
    }

    return children.map(mapSpan).toList();
  }

  TextStyle _applyInlineColor(TextStyle style, dom.Element element) {
    final styleAttr = element.attributes['style'];
    final colorAttr = element.attributes['color'];
    final detected =
        _extractColor(styleAttr ?? '') ?? _extractNamedColor(colorAttr);
    if (detected != null) {
      style = style.merge(TextStyle(color: detected));
    }
    return style;
  }

  Color? _extractNamedColor(String? value) {
    if (value == null || value.trim().isEmpty) return null;
    final normalized = value.trim().toLowerCase();
    try {
      if (normalized.startsWith('#')) {
        return _colorFromHex(normalized.substring(1));
      }
      if (normalized.startsWith('rgb')) {
        return _extractColor('color:$normalized');
      }
      switch (normalized) {
        case 'red':
          return Colors.red;
        case 'blue':
          return Colors.blue;
        case 'green':
          return Colors.green;
        case 'black':
          return Colors.black;
        case 'white':
          return Colors.white;
        case 'yellow':
          return Colors.yellow;
        case 'orange':
          return Colors.orange;
        case 'purple':
          return Colors.purple;
        case 'pink':
          return Colors.pink;
        case 'brown':
          return Colors.brown;
        case 'gray':
        case 'grey':
          return Colors.grey;
      }
    } catch (_) {
      return null;
    }
    return null;
  }

  Color? _extractColor(String style) {
    final regexHex = RegExp(r'color\s*:\s*#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})');
    final matchHex = regexHex.firstMatch(style);
    if (matchHex != null) {
      return _colorFromHex(matchHex.group(1)!);
    }

    final regexRgb = RegExp(r'color\s*:\s*rgb\s*\(([^\)]+)\)');
    final matchRgb = regexRgb.firstMatch(style);
    if (matchRgb != null) {
      final parts = matchRgb
          .group(1)!
          .split(',')
          .map((e) => e.trim())
          .map(
            (e) => e.endsWith('%')
                ? (255 * double.parse(e.replaceAll('%', '')) / 100).round()
                : int.tryParse(e) ?? 0,
          )
          .toList();
      if (parts.length >= 3) {
        return Color.fromARGB(
          255,
          parts[0].clamp(0, 255),
          parts[1].clamp(0, 255),
          parts[2].clamp(0, 255),
        );
      }
    }
    return null;
  }

  Color _colorFromHex(String hex) {
    final clean = hex.length == 3
        ? hex.split('').map((c) => '$c$c').join()
        : hex;
    return Color(int.parse('0xFF$clean'));
  }

  String _normalizeUrl(String href) {
    if (href.isEmpty) return href;
    final trimmed = href.trim();
    if (RegExp(r'^https?://', caseSensitive: false).hasMatch(trimmed)) {
      return trimmed;
    }
    if (trimmed.startsWith('www.')) {
      return 'https://$trimmed';
    }
    return 'https://$trimmed';
  }
}

class BloggerFeedPage extends StatefulWidget {
  const BloggerFeedPage({super.key, required this.config, this.initialPostId});

  final BloggerFeedConfig config;
  final String? initialPostId;

  @override
  State<BloggerFeedPage> createState() => _BloggerFeedState();
}

class _BloggerFeedState extends State<BloggerFeedPage> {
  static const String _authority = 'www.googleapis.com';

  http.Client? _httpClient;
  final ScrollController _scrollController = ScrollController();
  List<_BloggerPost> _posts = [];
  String? _nextPageToken;
  String? _errorMessage;
  bool _loadingInitial = true;
  bool _loadingMore = false;
  String? _pendingInitialPostId;
  bool _initialNavigationPending = false;

  @override
  void initState() {
    super.initState();
    _httpClient = http.Client();
    _scrollController.addListener(_onScroll);
    _loadInitial();
    _pendingInitialPostId = widget.initialPostId;
    _initialNavigationPending = widget.initialPostId != null;
  }

  @override
  void dispose() {
    _httpClient?.close();
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    super.dispose();
  }

  Future<_BloggerPage> _fetchPostsPage({String? pageToken}) async {
    final client = _httpClient;
    if (client == null) return _BloggerPage.empty;

    final queryParameters = <String, String>{
      'key': widget.config.apiKey,
      'labels': widget.config.label,
      'orderBy': 'published',
      'status': 'live',
      'view': 'READER',
      'maxResults': '30',
      'fetchBodies': 'true',
    };
    if (pageToken != null && pageToken.isNotEmpty) {
      queryParameters['pageToken'] = pageToken;
    }
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
    final items = body['items'] as List<dynamic>? ?? const [];
    final posts = items
        .map((item) => _BloggerPost.fromJson(item as Map<String, dynamic>))
        .where((post) => post.title.isNotEmpty)
        .toList();
    final nextToken = body['nextPageToken'] as String?;
    return _BloggerPage(posts: posts, nextPageToken: nextToken);
  }

  Future<void> _loadInitial() async {
    setState(() {
      _loadingInitial = true;
      _errorMessage = null;
      _nextPageToken = null;
    });
    try {
      final page = await _fetchPostsPage();
      if (!mounted) return;
      setState(() {
        _posts = page.posts;
        _nextPageToken = page.nextPageToken;
        _loadingInitial = false;
      });
      _tryOpenPendingPost();
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loadingInitial = false;
        if (_posts.isEmpty) {
          _errorMessage = e.toString();
        }
      });
      if (_posts.isNotEmpty) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('लोड करने में दिक्कत: $e')));
      }
    }
  }

  Future<void> _loadMore() async {
    if (_loadingMore || _loadingInitial) return;
    final token = _nextPageToken;
    if (token == null || token.isEmpty) return;
    setState(() {
      _loadingMore = true;
    });
    try {
      final page = await _fetchPostsPage(pageToken: token);
      if (!mounted) return;
      setState(() {
        _posts = [..._posts, ...page.posts];
        _nextPageToken = page.nextPageToken;
        _loadingMore = false;
      });
      _tryOpenPendingPost();
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loadingMore = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('और पोस्ट लोड करने में दिक्कत: $e')),
      );
    }
  }

  void _onScroll() {
    if (!_scrollController.hasClients || _loadingMore || _loadingInitial) {
      return;
    }
    final position = _scrollController.position;
    if (position.maxScrollExtent - position.pixels <= 240) {
      _loadMore();
    }
  }

  void _tryOpenPendingPost() {
    if (!_initialNavigationPending || _pendingInitialPostId == null) return;
    final target = _posts.firstWhere(
      (post) => post.id == _pendingInitialPostId,
      orElse: () => _BloggerPost.empty,
    );
    if (target == _BloggerPost.empty) {
      return;
    }
    _initialNavigationPending = false;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      _openPost(target);
    });
  }

  void _openPost(_BloggerPost post) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) =>
            _BloggerPostDetailPage(post: post, label: widget.config.label),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.config.title)),
      body: RefreshIndicator(
        onRefresh: _loadInitial,
        child: _buildFeedContent(),
      ),
    );
  }

  Widget _buildFeedContent() {
    if (_loadingInitial && _posts.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.symmetric(vertical: 120),
        children: const [Center(child: CircularProgressIndicator())],
      );
    }

    if (_posts.isEmpty) {
      final hasError = _errorMessage != null && _errorMessage!.isNotEmpty;
      return _BloggerFeedMessage(
        icon: hasError ? Icons.wifi_off : Icons.article_outlined,
        title: hasError ? 'डेटा लोड करने में दिक्कत' : 'यहाँ अभी कुछ नहीं है',
        message: hasError
            ? _errorMessage!
            : 'जैसे ही नया अपडेट आएगा, वह यहाँ दिखाई देगा।',
      );
    }

    return ListView.separated(
      controller: _scrollController,
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      itemCount: _posts.length + (_loadingMore ? 1 : 0),
      separatorBuilder: (context, index) => const Divider(height: 1),
      itemBuilder: (context, index) {
        if (index >= _posts.length) {
          return const Padding(
            padding: EdgeInsets.symmetric(vertical: 16),
            child: Center(
              child: SizedBox(
                height: 22,
                width: 22,
                child: CircularProgressIndicator(strokeWidth: 2.4),
              ),
            ),
          );
        }
        final post = _posts[index];
        return _BloggerPostTile(post: post, onOpen: _openPost);
      },
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
        effectiveParagraphs.isNotEmpty
            ? effectiveParagraphs.join(' ')
            : cleaned,
        15,
      ),
      published:
          DateTime.tryParse(json['published'] as String? ?? '')?.toUtc() ??
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

  DateTime get publishedIst =>
      published.add(const Duration(hours: 5, minutes: 30));

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
  const _BloggerPostTile({required this.post, required this.onOpen});

  final _BloggerPost post;
  final void Function(_BloggerPost post) onOpen;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateColor = theme.colorScheme.primary;
    return ListTile(
      onTap: () => onOpen(post),
      contentPadding: const EdgeInsets.symmetric(horizontal: 4, vertical: 6),
      title: Text(
        post.title,
        style: theme.textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.w700,
          height: 1.2,
        ),
        maxLines: 2,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: Text(
        _formatIndianDate(post.publishedIst),
        style: theme.textTheme.bodySmall?.copyWith(
          fontWeight: FontWeight.w600,
          color: dateColor,
        ),
      ),
      dense: true,
      horizontalTitleGap: 8,
    );
  }
}

class _BloggerFeedMessage extends StatelessWidget {
  const _BloggerFeedMessage({
    required this.icon,
    required this.title,
    required this.message,
  });

  final IconData icon;
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 40),
      children: [
        Icon(icon, size: 64, color: theme.colorScheme.primary),
        const SizedBox(height: 16),
        Text(
          title,
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w700,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 12),
        Text(
          message,
          style: theme.textTheme.bodyMedium,
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
}

String _truncateWords(String text, int maxWords) {
  final words = text
      .split(RegExp(r'\s+'))
      .where((w) => w.trim().isNotEmpty)
      .toList();
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
  final List<TapGestureRecognizer> _linkRecognizers = [];

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
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  void dispose() {
    _resetLinkRecognizers();
    super.dispose();
  }

  void _resetLinkRecognizers() {
    for (final recognizer in _linkRecognizers) {
      recognizer.dispose();
    }
    _linkRecognizers.clear();
  }

  void _openLink(String rawUrl) {
    final trimmed = rawUrl.trim();
    if (trimmed.isEmpty) return;
    Uri? uri = Uri.tryParse(trimmed);
    if (uri == null) {
      uri = Uri.tryParse('https://$trimmed');
    } else if (!uri.hasScheme) {
      uri = Uri.tryParse('https://$trimmed');
    }
    if (uri == null) return;
    final hostTitle = uri.host.isNotEmpty ? uri.host : 'वेब पृष्ठ';
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) =>
            InAppWebViewPage(title: hostTitle, initialUrl: uri.toString()),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    _resetLinkRecognizers();
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

                Share.share(buffer.toString(), subject: post.title);
              },
            ),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 4, 20, 32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.fromLTRB(12, 9, 12, 10),
              decoration: const BoxDecoration(color: Color(0xFF3674B5)),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: const [
                  Text(
                    'विज्ञापन',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontWeight: FontWeight.w800,
                      fontSize: 19,
                      color: Colors.white,
                    ),
                  ),
                  NativeBannerAd(
                    adUnitId: 'ca-app-pub-1638673809508848/5037239330',
                  ),
                ],
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
              for (var i = 0; i < paragraphs.length; i++) ...[
                _BlogRichParagraph(
                  html: paragraphs[i],
                  baseStyle: theme.textTheme.bodyLarge?.copyWith(
                    fontWeight: FontWeight.w600,
                    height: 1.5,
                  ),
                  linkColor: Colors.blue,
                  onTapLink: _openLink,
                  registerRecognizer: _linkRecognizers.add,
                ),
                if (i != paragraphs.length - 1) const SizedBox(height: 18),
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

  final frag = html_parser.parseFragment(html);
  final paragraphs = <String>[];
  final sb = StringBuffer();

  bool isBlockName(String name) {
    return name == 'p' ||
        name == 'div' ||
        name == 'section' ||
        name == 'article' ||
        name == 'blockquote' ||
        name == 'li' ||
        name == 'ul' ||
        name == 'ol' ||
        name.startsWith('h');
  }

  void flush() {
    if (sb.isEmpty) return;
    final htmlStr = sb.toString().trim();
    if (htmlStr.isNotEmpty) paragraphs.add(htmlStr);
    sb.clear();
  }

  String nodeOuter(dom.Node node) {
    if (node is dom.Element) {
      return node.outerHtml;
    }
    return node.text ?? '';
  }

  void collectInnerHtml(dom.Element el, StringBuffer into) {
    for (final child in el.nodes) {
      into.write(nodeOuter(child));
    }
  }

  void walk(List<dom.Node> nodes) {
    for (final node in nodes) {
      if (node.nodeType == dom.Node.TEXT_NODE) {
        sb.write(node.text ?? '');
        continue;
      }
      if (node is dom.Element) {
        final name = node.localName?.toLowerCase() ?? '';
        if (name == 'br') {
          flush();
          continue;
        }
        if (isBlockName(name)) {
          // flush current inline buffer as its own paragraph
          flush();
          // push this block's inner HTML as a paragraph
          final inner = StringBuffer();
          collectInnerHtml(node, inner);
          final s = inner.toString().trim();
          if (s.isNotEmpty) paragraphs.add(s);
          continue;
        }
        // Inline element: append its full HTML
        sb.write(node.outerHtml);
      }
    }
  }

  walk(frag.nodes);
  flush();
  return paragraphs;
}
