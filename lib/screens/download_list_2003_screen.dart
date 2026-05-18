import 'package:flutter/material.dart';
import 'package:yojna_plus/screens/webview_screen.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:yojna_plus/utils/ad_manager.dart';
import 'package:yojna_plus/l10n/app_strings.dart';

import 'package:yojna_plus/widgets/info_popup_widget.dart';

class DownloadList2003Screen extends StatefulWidget {
  const DownloadList2003Screen({super.key});

  @override
  State<DownloadList2003Screen> createState() => _DownloadList2003ScreenState();
}

class _DownloadList2003ScreenState extends State<DownloadList2003Screen> {
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
          final theme = Theme.of(context);
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
              backgroundColor: theme.primaryColor,
              behavior: SnackBarBehavior.floating,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              duration: const Duration(seconds: 2),
            ),
          );
          Future.delayed(const Duration(seconds: 2), () {
            if (mounted) {
              AdManager.instance.showAd(context, '2003');
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
      appBar: AppBar(title: Text(AppStrings.tr(AppStrings.stateListTitle2003))),
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
                      color: theme.primaryColor,
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
                            AppStrings.tr(AppStrings.oldListTitle),
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
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'AP',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.arunachalPradesh),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showArunachalOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'KA',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.karnataka),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showKarnatakaOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'MH',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.maharashtra),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://ceoelection.maharashtra.gov.in/2002/2002rolldata.aspx',
                                  title: 'Maharashtra 2002 Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'MN',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.manipur),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showManipurOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'ML',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.meghalaya),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://www.ceomeghalaya.nic.in/erolls/electoral-rolls-2005.html',
                                  title: 'Meghalaya 2005 Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'NL',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.nagaland),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showNagalandOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'OD',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.odisha),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showOdishaOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'SK',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.sikkim),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showSikkimOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'TG',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.telangana),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showTelanganaOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'TR',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.tripura),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showTripuraOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'AN',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.andamanNicobar),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showAndamanOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'CH',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.chandigarh),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showChandigarhOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'DN',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Expanded(
                                child: Text(
                                  AppStrings.tr(AppStrings.dadraNagar),
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: theme.colorScheme.onSurface,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showDadraNagarOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'DL',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.delhi),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showDelhiOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'UK',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Row(
                            children: [
                              Text(
                                AppStrings.tr(AppStrings.uttarakhand),
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.onSurface,
                                ),
                              ),
                              const SizedBox(width: 8),
                              const _BlinkingNewBadge(),
                            ],
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://election.uk.gov.in/search2003uk',
                                  title: 'Uttarakhand Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'UP',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.uttarPradesh),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://ceouttarpradesh.nic.in/rollpdf/rollpdf.aspx',
                                  title: 'Uttar Pradesh Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'PB',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.punjab),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://elections.punjab.gov.in/Election/public/Eroll2003',
                                  title: 'Punjab Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'GA',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.goa),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url: 'https://ceogoa.in/SIR2002/SIR2002ERoll',
                                  title: 'Goa Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'WB',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.westBengal),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://ceowestbengal.wb.gov.in/roll_dist',
                                  title: 'West Bengal Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'MP',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.madhyaPradesh),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const WebViewScreen(
                                  url:
                                      'https://ceoelection.mp.gov.in/SE2003_SECTION.aspx',
                                  title: 'Madhya Pradesh Voter List',
                                ),
                              ),
                            );
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'LD',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.lakshadweep),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.download_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () async {
                            final url = Uri.parse(
                              'https://ceolakshadweep.gov.in/electoral_rolls_sir_2002_polling_station',
                            );
                            if (await canLaunchUrl(url)) {
                              await launchUrl(
                                url,
                                mode: LaunchMode.inAppBrowserView,
                              );
                            } else {
                              if (context.mounted) {
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('Could not launch URL'),
                                  ),
                                );
                              }
                            }
                          },
                        ),

                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'TN',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.tamilNadu),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showTamilNaduOptions(context);
                          },
                        ),
                        const Divider(),
                        ListTile(
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          leading: CircleAvatar(
                            backgroundColor: theme.colorScheme.secondary
                                .withOpacity(0.1),
                            child: Text(
                              'JH',
                              style: TextStyle(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          title: Text(
                            AppStrings.tr(AppStrings.jharkhand),
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.onSurface,
                            ),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.secondary.withOpacity(
                                0.1,
                              ),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.touch_app_rounded,
                              color: theme.colorScheme.secondary,
                              size: 20,
                            ),
                          ),
                          onTap: () {
                            _showJharkhandOptions(context);
                          },
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

  void _showTamilNaduOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.tamilNadu),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.download2002Pdf),
                icon: Icons.picture_as_pdf,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://erolls.tn.gov.in/Rollpdf/SIR_2002.aspx',
                        title: 'Tamil Nadu Voter List 2002',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.download2005Pdf),
                icon: Icons.picture_as_pdf,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://erolls.tn.gov.in/Rollpdf/SIR_2005.aspx',
                        title: 'Tamil Nadu Voter List 2005',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.otherOption),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://erolls.tn.gov.in/electoralsearch/',
                        title: 'Tamil Nadu Electoral Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showJharkhandOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.jharkhand),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.jhDownload2003),
                icon: Icons.picture_as_pdf,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://ceojh.jharkhand.gov.in/mrollpdf1/aceng.aspx',
                        title: 'Jharkhand Voter List 2003',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.jhSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'http://103.107.59.65/jhElectorSearch2003/',
                        title: 'Jharkhand Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.jhHowToSearch),
                icon: Icons.help_outline,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'http://103.107.59.65/jhElectorSearch2003/SearchNAmein2003EROLLbyNameandrelative_name.pdf',
                        title: 'How to Search Name',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showArunachalOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.arunachalPradesh),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.apPollingStationWise),
                icon: Icons.list_alt,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://ceoarunachal.nic.in/archive-electoral-roll',
                        title: 'Arunachal Pradesh Polling Station Wise',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.apAcWise),
                icon: Icons.list,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceoarunachal.nic.in/eroll2006ac',
                        title: 'Arunachal Pradesh AC Wise',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showKarnatakaOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.karnataka),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.kaDownload2002),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceo.karnataka.gov.in/voter_list/en',
                        title: 'Karnataka 2002 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.kaSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceo.karnataka.gov.in/search/en',
                        title: 'Karnataka 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showManipurOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.manipur),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.mnDownload2005),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceomanipur.nic.in/eroll2005',
                        title: 'Manipur 2005 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.mnSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceomanipur.nic.in/electorsearch',
                        title: 'Manipur 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showNagalandOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.nagaland),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.nlDownload2005),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceo.nagaland.gov.in/eroll_2005',
                        title: 'Nagaland 2005 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.nlSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://appceo.nagaland.gov.in/',
                        title: 'Nagaland 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showOdishaOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.odisha),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.odDownload2005),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceoodisha.nic.in/en/view_eroll_2002/',
                        title: 'Odisha 2005 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.odSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://ceoodisha.nic.in/en/search-your-name-2002/',
                        title: 'Odisha 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showSikkimOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.sikkim),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.skDownload2002),
                icon: Icons.download_rounded,
                onTap: () async {
                  Navigator.pop(context);
                  final url = Uri.parse(
                    'https://ceosikkim.nic.in/ElectoralRolls/PollingStationName?ERollID=35',
                  );
                  if (await canLaunchUrl(url)) {
                    await launchUrl(url, mode: LaunchMode.inAppWebView);
                  }
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.skSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceosikkim.nic.in/SearchFacility/Index',
                        title: 'Sikkim 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showTelanganaOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.telangana),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.tgDownload2002),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://ceotserms2.telangana.gov.in/sir2002/rolls.aspx',
                        title: 'Telangana 2002 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.tgSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://ceotserms2.telangana.gov.in/sir2002_search/sir_search.aspx',
                        title: 'Telangana 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showTripuraOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.tripura),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.trDownload2005),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  const url =
                      'https://erms.tripura.gov.in/sir2005/download-eroll';
                  launchUrl(
                    Uri.parse(url),
                    // Use externalApplication to ensure downloads are handled by the system browser
                    mode: LaunchMode.externalApplication,
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.trSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://erms.tripura.gov.in/sir2005/search-by-details',
                        title: 'Tripura 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showAndamanOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.andamanNicobar),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.anDownload2002),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://www.ceoandaman.nic.in/election/E-ROLL-2002/default.htm',
                        title: 'Andaman & Nicobar 2002 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.anSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceo.andamannicobar.gov.in/sir2002/',
                        title: 'Andaman & Nicobar 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showChandigarhOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.chandigarh),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.chDownload2002),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceochandigarh.gov.in/pages/intensive',
                        title: 'Chandigarh 2002 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.chSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://ceochandigarh.gov.in/pages/electoral_2022',
                        title: 'Chandigarh 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showDadraNagarOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.dadraNagar),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.dnDownload2002),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceodaman.nic.in/IntensiveSR2002.aspx',
                        title: 'Dadra & Nagar Haveli 2002 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.dnSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url: 'https://ceodaman.nic.in/ceoindex.html',
                        title: 'Dadra & Nagar Haveli 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  void _showDelhiOptions(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        elevation: 10,
        backgroundColor: Colors.white,
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                AppStrings.tr(AppStrings.delhi),
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.primaryColor,
                ),
              ),
              const SizedBox(height: 20),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.dlDownload2002),
                icon: Icons.download_rounded,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://www.ceodelhi.gov.in/ElectoralRoll_2002.aspx',
                        title: 'Delhi 2002 List Download',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.dlSearchName),
                icon: Icons.search,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://www.ceodelhi.gov.in/ElectorSearchByName_2002.aspx',
                        title: 'Delhi 2002 Name Search',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.dlPollingStation),
                icon: Icons.list_alt,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://www.ceodelhi.gov.in/ElectorSearchByPollingStation_2002.aspx',
                        title: 'Delhi 2002 Polling Station Wise',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              _buildOptionButton(
                context,
                title: AppStrings.tr(AppStrings.dlEpicSearch),
                icon: Icons.credit_card,
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const WebViewScreen(
                        url:
                            'https://www.ceodelhi.gov.in/ElectorSearch_2002.aspx',
                        title: 'Delhi 2002 EPIC Number Wise',
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 10),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildOptionButton(
    BuildContext context, {
    required String title,
    required IconData icon,
    required VoidCallback onTap,
  }) {
    final theme = Theme.of(context);
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: theme.primaryColor,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          elevation: 2,
        ),
        onPressed: onTap,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 24),
            const SizedBox(width: 12),
            Text(
              title,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }
}

class _BlinkingNewBadge extends StatefulWidget {
  const _BlinkingNewBadge();

  @override
  State<_BlinkingNewBadge> createState() => _BlinkingNewBadgeState();
}

class _BlinkingNewBadgeState extends State<_BlinkingNewBadge>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    )..repeat(reverse: true);
    _fadeAnimation = Tween<double>(begin: 0.4, end: 1.0).animate(_controller);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _fadeAnimation,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
        decoration: BoxDecoration(
          color: Colors.red,
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Text(
          'NEW',
          style: TextStyle(
            color: Colors.white,
            fontSize: 10,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
