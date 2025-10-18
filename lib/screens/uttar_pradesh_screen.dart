import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:yojna_plus/screens/pm_kisan_detail_screen.dart';
import 'package:yojna_plus/screens/shramik_card_detail_screen.dart';
import 'package:yojna_plus/screens/schemes/kanya_sumangala_screen.dart';
import 'package:yojna_plus/screens/schemes/awas_card_screen.dart';
import 'package:yojna_plus/screens/schemes/mukhyamantri_rahat_kosh_screen.dart';
import 'package:yojna_plus/screens/schemes/mukhyamantri_samuhik_vivah_screen.dart';
import 'package:yojna_plus/screens/select_state_screen.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:motion_tab_bar/motiontabbar.dart';
import 'package:yojna_plus/constants/app_links.dart';
import 'package:yojna_plus/screens/useful_page.dart';
import 'package:yojna_plus/screens/others_page.dart';
import 'package:yojna_plus/screens/notifications_page.dart';
import 'package:yojna_plus/widgets/gradient_folder_icon.dart';
import 'package:yojna_plus/screens/blogger_feed_page.dart';
import 'package:yojna_plus/screens/student/result_feed_screen.dart';
import 'package:yojna_plus/screens/favorite_blogs_screen.dart';

class UttarPradeshPage extends StatefulWidget {
  const UttarPradeshPage({super.key});

  @override
  State<UttarPradeshPage> createState() => _UttarPradeshPageState();
}

class _UttarPradeshPageState extends State<UttarPradeshPage>
    with SingleTickerProviderStateMixin {
  static const MethodChannel _downloadChannel = MethodChannel('com.samoondigital.yojnaplus/downloads');
  static const String _bloggerBlogId = ResultFeedScreen.blogId;
  static const String _bloggerApiKey = ResultFeedScreen.apiKey;
  static const String _bloggerLabel = 'General';
  static const BloggerFeedConfig _homeBlogConfig = BloggerFeedConfig(
    blogId: _bloggerBlogId,
    apiKey: _bloggerApiKey,
    label: _bloggerLabel,
    title: 'ताज़ा ब्लॉग अपडेट',
  );
  static const double _blogCardHeight = 148;
  static const double _blogCardWidth = 228;
  static const double _blogCardSpacing = 12;

  http.Client? _blogClient;
  Future<List<_HomeBlogPost>>? _blogPostsFuture;
  late final ScrollController _blogScrollController;
  Timer? _blogAutoScrollTimer;
  int _blogAutoIndex = 0;
  int _blogAutoItemCount = 0;

  final GlobalKey _tabBarKey = GlobalKey();

  static const List<String> _tabTitles = ['होम', 'उपयोगी', 'छात्र', 'किसान'];

  late final TabController _tabController;
  int _currentTabIndex = 0;


  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _tabTitles.length, vsync: this);
    _tabController.addListener(() {
      if (_tabController.indexIsChanging) return;
      final newIndex = _tabController.index;
      if (mounted && _currentTabIndex != newIndex) {
        setState(() {
          _currentTabIndex = newIndex;
        });
      }
    });
    _blogClient = http.Client();
    _blogPostsFuture = _fetchLatestBlogPosts();
    _blogScrollController = ScrollController();
  }

  Future<List<_HomeBlogPost>> _fetchLatestBlogPosts() async {
    final client = _blogClient;
    if (client == null) return const [];

    try {
      final queryParameters = <String, String>{
        'key': _bloggerApiKey,
        'labels': _bloggerLabel,
        'orderBy': 'published',
        'status': 'live',
        'view': 'READER',
        'maxResults': '15',
        'fields': 'items(id,title,url,published,content)',
      };

      final uri = Uri.https(
        'www.googleapis.com',
        '/blogger/v3/blogs/$_bloggerBlogId/posts',
        queryParameters,
      );

      final response = await client
          .get(uri, headers: const {'Accept': 'application/json'})
          .timeout(const Duration(seconds: 10));

      if (response.statusCode != 200) {
        return const [];
      }

      final decoded = jsonDecode(response.body) as Map<String, dynamic>;
      final items = decoded['items'] as List<dynamic>?;
      if (items == null) return const [];

      return items
          .map((item) => _HomeBlogPost.fromJson(item as Map<String, dynamic>))
          .where((post) => post.title.isNotEmpty)
          .take(6)
          .toList();
    } catch (_) {
      return const [];
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    _blogClient?.close();
    _stopBlogAutoScroll();
    _blogScrollController.dispose();
    super.dispose();
  }

  Future<void> _openDownloadsFolder() async {
    try {
      await _downloadChannel.invokeMethod('openDownloadsUI');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('डाउनलोड फ़ोल्डर नहीं खोल पाए।')), 
      );
    }
  }

  void _reloadBlogPosts() {
    if (!mounted) return;
    _blogClient ??= http.Client();
    setState(() {
      _blogPostsFuture = _fetchLatestBlogPosts();
      _blogAutoIndex = 0;
    });
  }

  void _openAllBlogs() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => const BloggerFeedPage(config: _homeBlogConfig),
      ),
    );
  }

  Future<void> _contactSupport() async {
    final uri = Uri.parse(kContactUrl);
    if (!await launchUrl(uri, mode: LaunchMode.externalApplication)) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('हमसे संपर्क नहीं कर पाए।')), 
      );
    }
  }

  void _openPrivacyPolicy() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => const InAppWebViewPage(
          title: 'गोपनीयता नीति',
          initialUrl: kPrivacyPolicyUrl,
        ),
      ),
    );
  }

  Future<void> _shareApp() async {
    try {
      await Share.share(
        'योजना प्लस ऐप इंस्टॉल करें: $kPlayStoreUrl',
        subject: 'योजना प्लस',
      );
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('ऐप साझा नहीं कर पाए।')), 
      );
    }
  }

  void _openFavoritesScreen() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => const FavoriteBlogsScreen(),
      ),
    );
  }

  void _stopBlogAutoScroll() {
    _blogAutoScrollTimer?.cancel();
    _blogAutoScrollTimer = null;
    _blogAutoItemCount = 0;
    _blogAutoIndex = 0;
  }

  void _startBlogAutoScroll(int itemCount) {
    if (itemCount < 2) {
      _stopBlogAutoScroll();
      return;
    }
    if (_blogAutoItemCount == itemCount && _blogAutoScrollTimer != null) {
      return;
    }
    _blogAutoItemCount = itemCount;
    _blogAutoIndex = _blogAutoIndex % itemCount;
    _blogAutoScrollTimer?.cancel();
    _blogAutoScrollTimer = Timer.periodic(const Duration(seconds: 3), (_) {
      if (!mounted || _blogAutoItemCount < 2) {
        _stopBlogAutoScroll();
        return;
      }
      final controller = _blogScrollController;
      _blogAutoIndex = (_blogAutoIndex + 1) % _blogAutoItemCount;
      final targetOffset = _blogAutoIndex * (_blogCardWidth + _blogCardSpacing);
      controller.animateTo(
        targetOffset,
        duration: const Duration(milliseconds: 420),
        curve: Curves.easeOutCubic,
      );
    });
  }

  Widget _buildBlogHighlightsSection(BuildContext context) {
    final theme = Theme.of(context);
    final future = _blogPostsFuture ??= _fetchLatestBlogPosts();
    return FutureBuilder<List<_HomeBlogPost>>(
      future: future,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return _buildBlogLoadingPlaceholder(theme);
        }

        if (snapshot.hasError) {
          _stopBlogAutoScroll();
          return _buildBlogLoadingPlaceholder(theme);
        }

        final posts = snapshot.data ?? const [];
        if (posts.isEmpty) {
          _stopBlogAutoScroll();
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text(
                    _homeBlogConfig.title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const Spacer(),
                  IconButton(
                    tooltip: 'रीलोड',
                    onPressed: _reloadBlogPosts,
                    icon: const Icon(Icons.refresh_rounded, size: 20),
                  ),
                  TextButton(
                    onPressed: _openAllBlogs,
                    child: const Text('सभी देखें'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 20),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceContainerHighest.withOpacity(0.32),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'अभी कोई लेख उपलब्ध नहीं है।',
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'जैसे ही नया ब्लॉग प्रकाशित होगा, वह यहाँ दिखाई देगा।',
                      style: theme.textTheme.bodyMedium,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
            ],
          );
        }

        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (!mounted) return;
          _startBlogAutoScroll(posts.length);
        });

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(
                  'ताज़ा अपडेट',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const Spacer(),
                TextButton(
                  onPressed: _openAllBlogs,
                  child: const Text('सभी देखें'),
                ),
              ],
            ),
            const SizedBox(height: 12),
            SizedBox(
              height: _blogCardHeight,
              child: ListView.separated(
                controller: _blogScrollController,
                padding: const EdgeInsets.symmetric(horizontal: 4),
                scrollDirection: Axis.horizontal,
                itemBuilder: (context, index) =>
                    _buildBlogCard(context, posts[index], index),
                separatorBuilder: (context, index) =>
                    const SizedBox(width: _blogCardSpacing),
                itemCount: posts.length,
              ),
            ),
            const SizedBox(height: 20),
          ],
        );
      },
    );
  }

  Widget _buildBlogLoadingPlaceholder(ThemeData theme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Text(
              'ताज़ा अपडेट',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
              ),
            ),
            const Spacer(),
            TextButton(
              onPressed: _openAllBlogs,
              child: const Text('सभी देखें'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Container(
          height: _blogCardHeight,
          decoration: BoxDecoration(
            color: theme.colorScheme.surfaceContainerHighest.withOpacity(0.4),
            borderRadius: BorderRadius.circular(20),
          ),
          alignment: Alignment.center,
          child: const SizedBox(
            height: 26,
            width: 26,
            child: CircularProgressIndicator(strokeWidth: 2.8),
          ),
        ),
        const SizedBox(height: 20),
      ],
    );
  }

  Widget _buildBlogCard(BuildContext context, _HomeBlogPost post, int index) {
    final theme = Theme.of(context);
    final palettes = <Color>[
      theme.colorScheme.primary,
      theme.colorScheme.secondary,
      theme.colorScheme.tertiary,
    ];
    final base = palettes[index % palettes.length];
    final accent = palettes[(index + 1) % palettes.length];

    return SizedBox(
      width: _blogCardWidth,
      child: GestureDetector(
        onTap: _openAllBlogs,
        child: Container(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [
                base.withOpacity(0.92),
                accent.withOpacity(0.82),
              ],
            ),
            borderRadius: BorderRadius.circular(22),
            boxShadow: [
              BoxShadow(
                color: base.withOpacity(0.24),
                blurRadius: 20,
                offset: const Offset(0, 12),
              ),
            ],
          ),
          child: Container(
            margin: const EdgeInsets.all(2.4),
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(20),
            ),
            padding: const EdgeInsets.fromLTRB(16, 18, 16, 18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  post.title,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const Spacer(),
                Row(
                  children: [
                    Icon(
                      Icons.event_note_outlined,
                      size: 18,
                      color: theme.colorScheme.primary,
                    ),
                    const SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        _formatHindiDateForHome(post.published),
                        style: theme.textTheme.labelMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                    const Icon(Icons.arrow_forward_rounded, size: 18),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final scheme = theme.colorScheme;
    return Scaffold(
      drawer: _buildDrawer(context),
      appBar: AppBar(
        automaticallyImplyLeading: false,
        leading: Builder(
          builder: (context) => IconButton(
            tooltip: 'मेनू',
            icon: const Icon(Icons.menu),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
        ),
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            _GradientText(
              text: _greetingForIndiaTime(),
              gradient: _gradientForIndiaTime(theme.colorScheme),
              style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
            ),
            const SizedBox(width: 15),
            _TimeGradientIcon(
              icon: _iconForIndiaTime(),
              gradient: _gradientForIndiaTime(theme.colorScheme),
              size: 28,
            ),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 20),
            child: IconButton(
              tooltip: 'डाउनलोड',
              icon: const GradientFolderIcon(size: 28),
              onPressed: _openDownloadsFolder,
            ),
          ),
        ],
      ),
      body: TabBarView(
        controller: _tabController,
        physics: const NeverScrollableScrollPhysics(),
        children: <Widget>[
          ListView(
            padding: EdgeInsets.fromLTRB(
              12,
              12,
              12,
              50 + MediaQuery.of(context).padding.bottom,
            ),
            children: [
              _buildBlogHighlightsSection(context),
              const SizedBox(height: 4),
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 4.0,
                  vertical: 6.0,
                ),
                child: Text(
                  'लोकप्रिय योजनाएँ',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
              ),
              const SizedBox(height: 16),
              FeaturedSchemeCard(
                icon: Icons.agriculture_rounded,
                title: 'पीएम-किसान सम्मान निधि',
                description:
                    '₹6,000 वार्षिक सहायता, आधार सीडिंग, स्थिति जाँच और सुधार के लिए सम्पूर्ण मार्गदर्शन।',
                chipLabel: 'केंद्रीय',
                chipBackground: theme.colorScheme.secondaryContainer,
                chipForeground: theme.colorScheme.onSecondaryContainer,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const PmKisanDetailScreen(),
                    ),
                  );
                },
                ribbonText: 'चर्चा में',
                ribbonIcon: Icons.trending_up,
              ),
              const SizedBox(height: 16),
              FeaturedSchemeCard(
                icon: Icons.construction_rounded,
                title: 'UP BOCW श्रमिक कार्ड',
                description:
                    'पंजीकरण, नवीनीकरण व 40+ योजनाएँ — श्रमिकों के लिए पूर्ण सहायता।',
                chipLabel: 'यूपी राज्य',
                chipBackground: theme.colorScheme.tertiaryContainer,
                chipForeground: theme.colorScheme.onTertiaryContainer,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const ShramikCardDetailScreen(),
                    ),
                  );
                },
                ribbonText: 'चर्चा में',
                ribbonIcon: Icons.trending_up,
              ),
              const SizedBox(height: 16),
              FeaturedSchemeCard(
                icon: Icons.child_friendly,
                title: 'मुख्यमंत्री कन्या सुमंगला योजना',
                description:
                    'लड़कियों के समग्र विकास हेतु चरणबद्ध आर्थिक सहायता — आवेदन/स्थिति देखने के लिए खोलें।',
                chipLabel: 'यूपी राज्य',
                chipBackground: theme.colorScheme.tertiaryContainer,
                chipForeground: theme.colorScheme.onTertiaryContainer,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const KanyaSumangalaScreen(),
                    ),
                  );
                },
              ),
              const SizedBox(height: 16),
              FeaturedSchemeCard(
                icon: Icons.house_rounded,
                title: 'प्रधानमंत्री आवास कार्ड',
                description:
                    'PMAY स्वीकृति, किश्त स्थिति और कार्ड डाउनलोड से जुड़ी सभी जानकारी।',
                chipLabel: 'केंद्रीय',
                chipBackground: theme.colorScheme.secondaryContainer,
                chipForeground: theme.colorScheme.onSecondaryContainer,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const AwasCardScreen(),
                    ),
                  );
                },
              ),
              const SizedBox(height: 16),
              FeaturedSchemeCard(
                icon: Icons.group,
                title: 'मुख्यमंत्री सामूहिक विवाह योजना',
                description:
                    'आवेदन पात्रता, दस्तावेज़, लाभ राशि व नवीनतम अपडेट देखें।',
                chipLabel: 'यूपी राज्य',
                chipBackground: theme.colorScheme.tertiaryContainer,
                chipForeground: theme.colorScheme.onTertiaryContainer,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const MukhyamantriSamuhikVivahScreen(),
                    ),
                  );
                },
              ),
              const SizedBox(height: 16),
              FeaturedSchemeCard(
                icon: Icons.volunteer_activism,
                title: 'मुख्यमंत्री राहत कोष',
                description: 'आपदा/चिकित्सा सहायता हेतु योजना की जानकारी देखें।',
                chipLabel: 'यूपी राज्य',
                chipBackground: theme.colorScheme.tertiaryContainer,
                chipForeground: theme.colorScheme.onTertiaryContainer,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const MukhyamantriRahatKoshScreen(),
                    ),
                  );
                },
              ),
              const SizedBox(height: 16),
            ],
          ),
          const UsefulPage(),
          const OthersPage(),
          const NotificationsPage(),
        ],
      ),
      bottomNavigationBar: Container(
        key: _tabBarKey,
        child: MotionTabBar(
          initialSelectedTab: _tabTitles[_currentTabIndex],
          labels: _tabTitles,
          icons: const [
            Icons.home_rounded,
            Icons.handyman_rounded,
            Icons.school_rounded,
            Icons.agriculture_rounded,
          ],
          tabSize: 50,
          tabBarHeight: 60,
          textStyle: theme.textTheme.labelMedium?.copyWith(
            color: scheme.onSurface,
            fontWeight: FontWeight.w600,
          ),
          tabIconColor: scheme.primary,
          tabIconSize: 26.0,
          tabIconSelectedSize: 26.0,
          tabSelectedColor: scheme.primary,
          tabIconSelectedColor: scheme.onPrimary,
          tabBarColor: scheme.surface,
          useSafeArea: true,
          onTabItemSelected: (int value) {
            _tabController.animateTo(value);
          },
        ),
      ),
    );
  }

  // --- Helpers for India time-based greeting and visuals ---
  DateTime _indiaNow() {
    // Convert device UTC to IST (UTC+05:30) without extra packages
    return DateTime.now().toUtc().add(const Duration(hours: 5, minutes: 30));
  }

  String _greetingForIndiaTime() {
    final h = _indiaNow().hour;
    if (h >= 5 && h < 12) return 'सुप्रभात';
    if (h >= 12 && h < 16) return 'शुभ दोपहर';
    if (h >= 16 && h < 20) return 'शुभ संध्या';
    return 'शुभ रात्रि';
  }

  IconData _iconForIndiaTime() {
    final h = _indiaNow().hour;
    if (h >= 5 && h < 12) return Icons.wb_sunny_rounded; // morning
    if (h >= 12 && h < 16) return Icons.wb_sunny; // afternoon
    if (h >= 16 && h < 20) return Icons.wb_twilight; // evening
    return Icons.nights_stay_rounded; // night
  }

  LinearGradient _gradientForIndiaTime(ColorScheme scheme) {
    final h = _indiaNow().hour;
    if (h >= 5 && h < 12) {
      // Morning: saffron -> warm yellow
      return LinearGradient(
        colors: [scheme.secondary, const Color(0xFFFFD54F)],
      );
    } else if (h >= 12 && h < 16) {
      // Afternoon: primary blue -> cyan
      return LinearGradient(colors: [scheme.primary, const Color(0xFF4DD0E1)]);
    } else if (h >= 16 && h < 20) {
      // Evening: deep orange -> purple
      return const LinearGradient(
        colors: [Color(0xFFF57C00), Color(0xFF8E24AA)],
      );
    } else {
      // Night: indigo -> blue grey
      return const LinearGradient(
        colors: [Color(0xFF3949AB), Color(0xFF546E7A)],
      );
    }
  }

  Widget _buildDrawer(BuildContext context) {
    final theme = Theme.of(context);
    final scheme = theme.colorScheme;

    final drawerGradient = LinearGradient(
      colors: [
        scheme.primary.withOpacity(0.24),
        scheme.secondary.withOpacity(0.18),
        scheme.surface,
      ],
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
    );

    return Drawer(
      child: Container(
        decoration: BoxDecoration(gradient: drawerGradient),
        child: SafeArea(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              DrawerHeader(
                margin: EdgeInsets.zero,
                padding: const EdgeInsets.fromLTRB(20, 8, 20, 12),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [
                      scheme.primary.withOpacity(0.85),
                      scheme.secondary.withOpacity(0.75),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Text(
                        'ऑनलाइन योजना',
                        style: theme.textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.w900,
                          color: scheme.onPrimary,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 6),
                      Text(
                        'सरकारी योजना ऐप',
                        style: theme.textTheme.labelLarge?.copyWith(
                          color: scheme.onPrimary.withOpacity(0.9),
                          fontWeight: FontWeight.w600,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              ),
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
                  children: [
                    _DrawerActionCard(
                      icon: Icons.cached_rounded,
                      title: 'राज्य बदलें',
                      subtitle: 'अन्य राज्य पेज पर जाएँ',
                      gradient: LinearGradient(
                        colors: [
                          scheme.primary.withOpacity(0.9),
                          scheme.tertiary.withOpacity(0.8),
                        ],
                      ),
                      onTap: () {
                        Navigator.of(context).pop();
                        Navigator.of(context).pushReplacement(
                          MaterialPageRoute(
                            builder: (_) => const SelectStatePage(),
                          ),
                        );
                      },
                    ),
                    const SizedBox(height: 12),
                    _DrawerActionCard(
                      icon: Icons.support_agent_rounded,
                      title: 'हमसे संपर्क करें',
                      subtitle: 'ईमेल के माध्यम से सहायता प्राप्त करें',
                      gradient: LinearGradient(
                        colors: [
                          scheme.secondary.withOpacity(0.9),
                          scheme.primary.withOpacity(0.85),
                        ],
                      ),
                      onTap: () {
                        Navigator.of(context).pop();
                        _contactSupport();
                      },
                    ),
                    const SizedBox(height: 12),
                    _DrawerActionCard(
                      icon: Icons.privacy_tip_rounded,
                      title: 'प्राइवेसी पॉलिसी',
                      subtitle: 'ऐप की गोपनीयता नीति पढ़ें',
                      gradient: LinearGradient(
                        colors: [
                          scheme.tertiary.withOpacity(0.9),
                          scheme.secondary.withOpacity(0.85),
                        ],
                      ),
                      onTap: () {
                        Navigator.of(context).pop();
                        _openPrivacyPolicy();
                      },
                    ),
                    const SizedBox(height: 12),
                    _DrawerActionCard(
                      icon: Icons.share_rounded,
                      title: 'ऐप साझा करें',
                      subtitle: 'दोस्तों को योजना प्लस भेजें',
                      gradient: LinearGradient(
                        colors: [
                          scheme.primaryContainer.withOpacity(0.9),
                          scheme.secondary.withOpacity(0.85),
                        ],
                      ),
                      onTap: () {
                        Navigator.of(context).pop();
                        _shareApp();
                      },
                    ),
                    const SizedBox(height: 14),
                    Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: _FavoritesDrawerButton(
                        onTap: () {
                          Navigator.of(context).pop();
                          _openFavoritesScreen();
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

const List<String> _homeHindiMonths = <String>[
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

String _stripHtmlForHome(String html) {
  final withoutTags = html.replaceAll(RegExp(r'<[^>]+>'), ' ');
  final withoutEntities = withoutTags.replaceAll(RegExp(r'&[^;]+;'), ' ');
  return withoutEntities.replaceAll(RegExp(r'\s+'), ' ').trim();
}

String _truncateWordsForHome(String text, int maxWords) {
  final words = text
      .split(RegExp(r'\s+'))
      .where((word) => word.trim().isNotEmpty)
      .toList();
  if (words.length <= maxWords) {
    return text.trim();
  }
  return '${words.take(maxWords).join(' ')}…';
}

String _formatHindiDateForHome(DateTime dateTime) {
  final local = dateTime.add(const Duration(hours: 5, minutes: 30));
  final day = local.day.toString().padLeft(2, '0');
  final month = _homeHindiMonths[local.month - 1];
  final year = local.year;
  return '$day $month $year';
}

class _HomeBlogPost {
  const _HomeBlogPost({
    required this.id,
    required this.title,
    required this.summary,
    required this.url,
    required this.published,
  });

  factory _HomeBlogPost.fromJson(Map<String, dynamic> json) {
    final content = (json['content'] as String?) ?? '';
    final stripped = _stripHtmlForHome(content);
    final summary = stripped.isNotEmpty
        ? _truncateWordsForHome(stripped, 18)
        : 'और पढ़ें…';
    final publishedRaw = json['published'] as String?;
    final published =
        DateTime.tryParse(publishedRaw ?? '')?.toUtc() ?? DateTime.now().toUtc();
    return _HomeBlogPost(
      id: (json['id'] as String?)?.trim() ?? '',
      title: (json['title'] as String?)?.trim() ?? '',
      summary: summary,
      url: (json['url'] as String?)?.trim() ?? '',
      published: published,
    );
  }

  final String id;
  final String title;
  final String summary;
  final String url;
  final DateTime published;
}

class _GradientText extends StatelessWidget {
  const _GradientText({required this.text, required this.gradient, this.style});
  final String text;
  final Gradient gradient;
  final TextStyle? style;

  @override
  Widget build(BuildContext context) {
    return ShaderMask(
      shaderCallback: (bounds) => gradient.createShader(
        Rect.fromLTWH(0, 0, bounds.width, bounds.height),
      ),
      blendMode: BlendMode.srcIn,
      child: Text(
        text,
        style: (style ?? const TextStyle()).copyWith(color: Colors.white),
      ),
    );
  }
}

class _TimeGradientIcon extends StatelessWidget {
  const _TimeGradientIcon({
    required this.icon,
    required this.gradient,
    this.size = 28,
  });
  final IconData icon;
  final Gradient gradient;
  final double size;

  @override
  Widget build(BuildContext context) {
    return ShaderMask(
      shaderCallback: (rect) => gradient.createShader(rect),
      blendMode: BlendMode.srcIn,
      child: Icon(icon, size: size, color: Colors.white),
    );
  }
}

class _DrawerActionCard extends StatelessWidget {
  const _DrawerActionCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.gradient,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final LinearGradient gradient;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textColor = Colors.white;
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Ink(
          decoration: BoxDecoration(
            gradient: gradient,
            borderRadius: BorderRadius.circular(18),
            boxShadow: [
              BoxShadow(
                color: gradient.colors.last.withOpacity(0.25),
                blurRadius: 14,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
          child: Row(
            children: [
              Container(
                height: 46,
                width: 46,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.18),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(icon, color: textColor, size: 26),
              ),
              const SizedBox(width: 18),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.titleMedium?.copyWith(
                        color: textColor,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      subtitle,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: textColor.withOpacity(0.9),
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.arrow_forward_ios_rounded, color: Colors.white, size: 18),
            ],
          ),
        ),
      ),
    );
  }
}

class _FavoritesDrawerButton extends StatefulWidget {
  const _FavoritesDrawerButton({required this.onTap});

  final VoidCallback onTap;

  @override
  State<_FavoritesDrawerButton> createState() => _FavoritesDrawerButtonState();
}

class _FavoritesDrawerButtonState extends State<_FavoritesDrawerButton>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 6),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final surface = theme.colorScheme.surface;
    final onSurface = theme.colorScheme.onSurface;
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        final t = _controller.value;
        final gradient = _animatedGradient(t);
        return Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: widget.onTap,
            borderRadius: BorderRadius.circular(22),
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [surface, surface.withOpacity(0.92)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(22),
                boxShadow: [
                  BoxShadow(
                    color: theme.colorScheme.primary.withOpacity(0.18),
                    blurRadius: 22,
                    offset: const Offset(0, 12),
                  ),
                ],
                border: Border.all(
                  color: theme.colorScheme.primary.withOpacity(0.12),
                  width: 1.4,
                ),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 18),
              child: Row(
                children: [
                  Container(
                    width: 54,
                    height: 54,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: gradient,
                      boxShadow: [
                        BoxShadow(
                          color: gradient.colors.last.withOpacity(0.6),
                          blurRadius: 18,
                          offset: const Offset(0, 10),
                        ),
                      ],
                    ),
                    child: const Icon(
                      Icons.favorite_rounded,
                      color: Colors.white,
                      size: 30,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          'पसंदीदा / लाइक्स',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          'सहेजी गई पोस्ट देखें',
                          style: theme.textTheme.bodyMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                            color: onSurface.withOpacity(0.8),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const Icon(
                    Icons.arrow_forward_ios_rounded,
                    size: 18,
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  LinearGradient _animatedGradient(double t) {
    final hue = (t * 360) % 360;
    final color1 = HSVColor.fromAHSV(1, hue, 0.9, 1).toColor();
    final color2 = HSVColor.fromAHSV(1, (hue + 50) % 360, 0.85, 1).toColor();
    return LinearGradient(
      colors: [color1, color2],
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
    );
  }
}

class _GradientIcon extends StatelessWidget {
  const _GradientIcon({required this.icon, required this.gradient});
  final IconData icon;
  final Gradient gradient;
  final double size = 24;

  @override
  Widget build(BuildContext context) {
    return ShaderMask(
      shaderCallback: (rect) => gradient.createShader(rect),
      blendMode: BlendMode.srcIn,
      child: Icon(icon, size: size, color: Colors.white),
    );
  }
}

// Reusable scheme card widget for simple list items
class SchemeCardBasic extends StatelessWidget {
  const SchemeCardBasic({
    super.key,
    required this.icon,
    required this.title,
    required this.brief,
    required this.badge,
    required this.onTap,
    this.stateBadge = false,
    this.ribbonText,
    this.ribbonIcon,
  });

  final IconData icon;
  final String title;
  final String brief;
  final String badge;
  final bool stateBadge; // true => use tertiary container styling
  final VoidCallback onTap;
  final String? ribbonText;
  final IconData? ribbonIcon;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bg = stateBadge
        ? theme.colorScheme.tertiaryContainer
        : theme.colorScheme.secondaryContainer;
    final fg = stateBadge
        ? theme.colorScheme.onTertiaryContainer
        : theme.colorScheme.onSecondaryContainer;

    return Stack(
      clipBehavior: Clip.none,
      children: [
        Card(
          child: InkWell(
            borderRadius: BorderRadius.circular(14),
            onTap: onTap,
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  LayoutBuilder(
                    builder: (context, c) {
                      final isNarrow = c.maxWidth < 320; // threshold for wrap
                      if (isNarrow) {
                        return Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                Icon(icon, color: theme.colorScheme.primary),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    title,
                                    style: theme.textTheme.titleMedium
                                        ?.copyWith(fontWeight: FontWeight.w700),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Align(
                              alignment: Alignment.centerLeft,
                              child: Chip(
                                label: Text(badge),
                                backgroundColor: bg,
                                labelStyle: TextStyle(color: fg),
                              ),
                            ),
                          ],
                        );
                      } else {
                        return Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            Icon(icon, color: theme.colorScheme.primary),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Text(
                                title,
                                style: theme.textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                            ),
                            Chip(
                              label: Text(badge),
                              backgroundColor: bg,
                              labelStyle: TextStyle(color: fg),
                            ),
                          ],
                        );
                      }
                    },
                  ),
                  const SizedBox(height: 8),
                  Text(brief, style: theme.textTheme.bodyMedium),
                ],
              ),
            ),
          ),
        ),
        if (ribbonText != null)
          Positioned(
            top: -2,
            right: 6,
            child: IgnorePointer(
              ignoring: true,
              child: _RibbonBadge(text: ribbonText!, icon: ribbonIcon),
            ),
          ),
      ],
    );
  }
}

class FeaturedSchemeCard extends StatelessWidget {
  const FeaturedSchemeCard({
    super.key,
    required this.icon,
    required this.title,
    required this.description,
    required this.chipLabel,
    required this.chipBackground,
    required this.chipForeground,
    required this.onTap,
    this.ribbonText,
    this.ribbonIcon,
  });

  final IconData icon;
  final String title;
  final String description;
  final String chipLabel;
  final Color chipBackground;
  final Color chipForeground;
  final VoidCallback onTap;
  final String? ribbonText;
  final IconData? ribbonIcon;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final gradient = LinearGradient(
      colors: [theme.colorScheme.primary, theme.colorScheme.secondary],
    );

    Widget iconBadge() {
      return Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: LinearGradient(
            colors: [theme.colorScheme.primary, theme.colorScheme.secondary],
          ),
          boxShadow: [
            BoxShadow(
              color: theme.colorScheme.primary.withOpacity(0.25),
              blurRadius: 18,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Icon(icon, color: theme.colorScheme.onPrimary, size: 22),
      );
    }

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 4),
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          Material(
            color: Colors.transparent,
            child: InkWell(
              borderRadius: BorderRadius.circular(20),
              onTap: onTap,
              child: Container(
                decoration: BoxDecoration(
                  gradient: gradient,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: theme.colorScheme.primary.withOpacity(0.20),
                      blurRadius: 18,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: Container(
                  margin: const EdgeInsets.all(1.6),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.surface,
                    borderRadius: BorderRadius.circular(18),
                  ),
                  padding:
                      const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                  child: LayoutBuilder(
                    builder: (context, constraints) {
                      final isNarrow = constraints.maxWidth < 340;
                      final headerChildren = <Widget>[
                        iconBadge(),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                title,
                                style: theme.textTheme.titleLarge?.copyWith(
                                  fontWeight: FontWeight.w900,
                                  height: 1.1,
                                ),
                              ),
                              const SizedBox(height: 6),
                              Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 9,
                                  vertical: 4,
                                ),
                                decoration: BoxDecoration(
                                  color: chipBackground,
                                  borderRadius: BorderRadius.circular(20),
                                ),
                                child: Text(
                                  chipLabel,
                                  style: theme.textTheme.labelMedium?.copyWith(
                                    color: chipForeground,
                                    fontWeight: FontWeight.w700,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ];

                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (isNarrow)
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  crossAxisAlignment: CrossAxisAlignment.center,
                                  children: headerChildren,
                                ),
                                const SizedBox(height: 12),
                              ],
                            )
                          else
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: headerChildren,
                            ),
                          if (!isNarrow) const SizedBox(height: 16),
                          Text(
                            description,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              fontWeight: FontWeight.w600,
                              height: 1.3,
                            ),
                          ),
                          const SizedBox(height: 14),
                          FilledButton.icon(
                            onPressed: onTap,
                            icon: const Icon(Icons.arrow_forward_rounded),
                            label: const Text('विस्तार से देखें'),
                            style: FilledButton.styleFrom(
                              minimumSize: const Size.fromHeight(44),
                              textStyle:
                                  const TextStyle(fontWeight: FontWeight.w800),
                            ),
                          ),
                        ],
                      );
                    },
                  ),
                ),
              ),
            ),
          ),
          if (ribbonText != null)
            Positioned(
              top: -14,
              right: 14,
              child: IgnorePointer(
                ignoring: true,
                child: _RibbonBadge(text: ribbonText!, icon: ribbonIcon),
              ),
            ),
        ],
      ),
    );
  }
}

class _RibbonBadge extends StatefulWidget {
  const _RibbonBadge({required this.text, this.icon});
  final String text;
  final IconData? icon;

  @override
  State<_RibbonBadge> createState() => _RibbonBadgeState();
}

class _RibbonBadgeState extends State<_RibbonBadge>
    with SingleTickerProviderStateMixin {
  AnimationController? _controller;
  Animation<double>? _opacity;

  @override
  void initState() {
    super.initState();
    if (widget.icon != null) {
      _initAnimation();
    }
  }

  @override
  void didUpdateWidget(covariant _RibbonBadge oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.icon != null && _controller == null) {
      _initAnimation();
    } else if (widget.icon == null && _controller != null) {
      _disposeAnimation();
    }
  }

  @override
  void dispose() {
    _disposeAnimation();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    Widget? trailing;
    if (widget.icon != null) {
      final iconWidget = Icon(
        widget.icon,
        size: 14,
        color: theme.colorScheme.onSecondary,
      );
      if (_controller != null && _opacity != null) {
        trailing = AnimatedBuilder(
          animation: _controller!,
          child: iconWidget,
          builder: (context, child) {
            final opacity = _opacity!.value.clamp(0.0, 1.0);
            return Opacity(opacity: opacity, child: child);
          },
        );
      } else {
        trailing = iconWidget;
      }
    }
    return Material(
      color: Colors.transparent,
      elevation: 2,
      borderRadius: BorderRadius.circular(6),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: theme.colorScheme.secondary,
          borderRadius: BorderRadius.circular(6),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              widget.text,
              style: theme.textTheme.labelSmall?.copyWith(
                color: theme.colorScheme.onSecondary,
                fontWeight: FontWeight.w700,
              ),
            ),
            if (trailing != null) ...[
              const SizedBox(width: 4),
              trailing,
            ],
          ],
        ),
      ),
    );
  }

  void _initAnimation() {
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 520),
    )..repeat();

    final curved = CurvedAnimation(
      parent: _controller!,
      curve: Curves.linear,
    );

    _opacity = TweenSequence<double>([
      TweenSequenceItem(
        tween: Tween(begin: 0.15, end: 1.0)
            .chain(CurveTween(curve: Curves.easeOutQuad)),
        weight: 50,
      ),
      TweenSequenceItem(
        tween: Tween(begin: 1.0, end: 0.15)
            .chain(CurveTween(curve: Curves.easeInQuad)),
        weight: 50,
      ),
    ]).animate(curved);
  }

  void _disposeAnimation() {
    _controller?.dispose();
    _controller = null;
    _opacity = null;
  }
}
