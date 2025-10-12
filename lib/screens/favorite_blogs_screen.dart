import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/blogger_feed_page.dart';
import 'package:yojna_plus/screens/student/result_feed_screen.dart';
import 'package:yojna_plus/services/favorite_blog_store.dart';

class FavoriteBlogsScreen extends StatefulWidget {
  const FavoriteBlogsScreen({super.key});

  @override
  State<FavoriteBlogsScreen> createState() => _FavoriteBlogsScreenState();
}

class _InfoChip extends StatelessWidget {
  const _InfoChip({
    required this.icon,
    required this.label,
    required this.color,
  });

  final IconData icon;
  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: theme.colorScheme.onSurfaceVariant),
          const SizedBox(width: 6),
          Text(
            label,
            style: theme.textTheme.labelMedium?.copyWith(
              fontWeight: FontWeight.w700,
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}

class _FavoriteBlogsScreenState extends State<FavoriteBlogsScreen> {
  late Future<List<FavoriteBlogEntry>> _favoritesFuture;

  @override
  void initState() {
    super.initState();
    _favoritesFuture = FavoriteBlogStore.instance.getFavorites();
    FavoriteBlogStore.instance.favoritesNotifier.addListener(_handleChanged);
  }

  @override
  void dispose() {
    FavoriteBlogStore.instance.favoritesNotifier.removeListener(_handleChanged);
    super.dispose();
  }

  void _handleChanged() {
    setState(() {
      _favoritesFuture = FavoriteBlogStore.instance.getFavorites();
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: AppBar(
        title: const Text('पसंदीदा ब्लॉग'),
      ),
      body: FutureBuilder<List<FavoriteBlogEntry>>(
        future: _favoritesFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          final favorites = snapshot.data ?? const <FavoriteBlogEntry>[];
          if (favorites.isEmpty) {
            return _EmptyFavoritesState(onExplore: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const BloggerFeedPage(
                    config: BloggerFeedConfig(
                      blogId: ResultFeedScreen.blogId,
                      apiKey: ResultFeedScreen.apiKey,
                      label: 'General',
                      title: 'ब्लॉग अपडेट',
                    ),
                  ),
                ),
              );
            });
          }

          return ListView.separated(
            itemCount: favorites.length,
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
            itemBuilder: (context, index) {
              final entry = favorites[index];
              return _FavoriteTile(entry: entry);
            },
            separatorBuilder: (context, index) => const SizedBox(height: 12),
          );
        },
      ),
    );
  }
}

class _FavoriteTile extends StatelessWidget {
  const _FavoriteTile({required this.entry});

  final FavoriteBlogEntry entry;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final published = entry.publishedUtc;
    final summary = entry.summary?.trim() ?? '';
    final dateChipText = published != null ? _formatSubtitle(published) : null;
    final label = entry.label.isNotEmpty ? entry.label : 'General';
    final frameGradient = LinearGradient(
      colors: [
        theme.colorScheme.primary.withOpacity(0.42),
        theme.colorScheme.secondary.withOpacity(0.34),
      ],
    );

    return Container(
      decoration: BoxDecoration(
        gradient: frameGradient,
        borderRadius: BorderRadius.circular(22),
        boxShadow: [
          BoxShadow(
            color: theme.colorScheme.primary.withOpacity(0.18),
            blurRadius: 18,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: Material(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(20),
        child: InkWell(
          borderRadius: BorderRadius.circular(20),
          onTap: () {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (_) => BloggerFeedPage(
                  config: BloggerFeedConfig(
                    blogId: ResultFeedScreen.blogId,
                    apiKey: ResultFeedScreen.apiKey,
                    label: label,
                    title: 'ब्लॉग अपडेट',
                  ),
                  initialPostId: entry.id,
                ),
              ),
            );
          },
          child: Padding(
            padding: const EdgeInsets.fromLTRB(18, 18, 12, 18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      padding: const EdgeInsets.all(16),
                      decoration: const BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: LinearGradient(
                          colors: [Color(0xFFFFB300), Color(0xFFFF6F00)],
                        ),
                      ),
                      child: const Icon(
                        Icons.favorite_rounded,
                        color: Colors.white,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            entry.title,
                            style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.w800,
                              height: 1.2,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                          if (summary.isNotEmpty) ...[
                            const SizedBox(height: 8),
                            Text(
                              summary,
                              style: theme.textTheme.bodyMedium?.copyWith(
                                fontWeight: FontWeight.w600,
                                color: theme.colorScheme.onSurface.withOpacity(0.78),
                                height: 1.3,
                              ),
                              maxLines: 3,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ],
                      ),
                    ),
                    IconButton(
                      tooltip: 'हटाएँ',
                      icon: const Icon(Icons.delete_outline_rounded),
                      onPressed: () {
                        FavoriteBlogStore.instance.removeById(entry.id);
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                Wrap(
                  spacing: 10,
                  runSpacing: 8,
                  children: [
                    if (dateChipText != null)
                      _InfoChip(
                        icon: Icons.schedule_rounded,
                        label: dateChipText,
                        color: theme.colorScheme.primary.withOpacity(0.12),
                      ),
                    _InfoChip(
                      icon: Icons.label_rounded,
                      label: label,
                      color: theme.colorScheme.secondary.withOpacity(0.12),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  String _formatSubtitle(DateTime publishedUtc) {
    final ist = publishedUtc.add(const Duration(hours: 5, minutes: 30));
    final day = ist.day.toString().padLeft(2, '0');
    final month = ist.month.toString().padLeft(2, '0');
    final year = ist.year;
    final hour = ist.hour % 12 == 0 ? 12 : ist.hour % 12;
    final minute = ist.minute.toString().padLeft(2, '0');
    final period = ist.hour >= 12 ? 'PM' : 'AM';
    return '$day-$month-$year • $hour:$minute $period';
  }
}

class _EmptyFavoritesState extends StatelessWidget {
  const _EmptyFavoritesState({required this.onExplore});

  final VoidCallback onExplore;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.star_border_rounded, size: 72),
            const SizedBox(height: 18),
            Text(
              'अभी कोई पसंदीदा लेख नहीं।',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            Text(
              'किसी भी ब्लॉग पोस्ट पर स्टार टैप करके यहाँ जोड़ें।',
              style: theme.textTheme.bodyMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: onExplore,
              icon: const Icon(Icons.explore_rounded),
              label: const Text('ब्लॉग्स देखें'),
            ),
          ],
        ),
      ),
    );
  }
}
