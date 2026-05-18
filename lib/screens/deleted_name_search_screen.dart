import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:yojna_plus/utils/ad_manager.dart';
import 'package:yojna_plus/l10n/app_strings.dart';

import 'package:yojna_plus/widgets/info_popup_widget.dart';

class DeletedNameSearchScreen extends StatefulWidget {
  const DeletedNameSearchScreen({super.key});

  @override
  State<DeletedNameSearchScreen> createState() =>
      _DeletedNameSearchScreenState();
}

class _DeletedNameSearchScreenState extends State<DeletedNameSearchScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) =>
            InfoPopupWidget(onClose: () => Navigator.pop(context)),
      ).then((_) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: const Row(
                children: [
                  Icon(Icons.info_outline, color: Colors.white, size: 20),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'This app is ad supported. Ad loading...',
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
              backgroundColor: Colors.teal,
              behavior: SnackBarBehavior.floating,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              duration: const Duration(seconds: 2),
            ),
          );
          Future.delayed(const Duration(seconds: 2), () {
            if (mounted) {
              AdManager.instance.showAd(context, 'deleted');
            }
          });
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(AppStrings.tr(AppStrings.deletedSearchTitle))),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Card(
              elevation: 4,
              shadowColor: theme.colorScheme.primary.withOpacity(0.3),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                children: [
                  // Colored Header
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 12,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.teal,
                      borderRadius: const BorderRadius.only(
                        topLeft: Radius.circular(16),
                        topRight: Radius.circular(16),
                      ),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Text(
                            AppStrings.tr(AppStrings.searchDeletedHeader),
                            style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  // List Items
                  Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Column(
                      children: [
                        _buildSearchTile(
                          context,
                          theme,
                          'MP',
                          AppStrings.tr(AppStrings.madhyaPradesh),
                          'https://ceoelection.mp.gov.in/SearchDraftRoll.aspx',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'CG',
                          AppStrings.tr(AppStrings.chhattisgarh),
                          'https://election.cg.gov.in/ASDList/',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'BR',
                          AppStrings.tr(AppStrings.bihar),
                          'https://ceoelection.bihar.gov.in/electors_included_till_24_06_2025_not_in_draft_01_08_2025.html',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'RJ',
                          AppStrings.tr(AppStrings.rajasthan),
                          'https://election.rajasthan.gov.in/ASD_SIR_2026/ASD_List_EPIC.html',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'KL',
                          AppStrings.tr(AppStrings.kerala),
                          'https://order.ceo.kerala.gov.in/sir/search/index',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'LD',
                          AppStrings.tr(AppStrings.lakshadweep),
                          'https://ceolakshadweep.gov.in/sir_2026_asd',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'AN',
                          AppStrings.tr(AppStrings.andamanNicobar),
                          'https://ceo.andamannicobar.gov.in/sir2026/Search',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'JK',
                          AppStrings.tr(AppStrings.jammuKashmir),
                          'https://ceo.jk.gov.in/namesearch/',
                        ),
                        const Divider(),
                        _buildSearchTile(
                          context,
                          theme,
                          'TN',
                          AppStrings.tr(AppStrings.tamilNadu),
                          'https://erolls.tn.gov.in/asd/',
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSearchTile(
    BuildContext context,
    ThemeData theme,
    String code,
    String name,
    String urlString,
  ) {
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      leading: CircleAvatar(
        backgroundColor: Colors.teal.withOpacity(0.1),
        child: Text(
          code,
          style: const TextStyle(
            color: Colors.teal,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      title: Text(
        name,
        style: TextStyle(
          fontWeight: FontWeight.bold,
          color: theme.colorScheme.onSurface,
        ),
      ),
      trailing: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: Colors.teal.withOpacity(0.1),
          shape: BoxShape.circle,
        ),
        child: const Icon(Icons.search_rounded, color: Colors.teal, size: 20),
      ),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) =>
                WebViewScreen(url: urlString, title: '$name Search'),
          ),
        );
      },
    );
  }
}
