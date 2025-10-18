import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:file_saver/file_saver.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:url_launcher/url_launcher.dart';
import '../services/downloads_store.dart';

const MethodChannel _downloadChannel = MethodChannel(
  'com.samoondigital.yojnaplus/downloads',
);
const String kMobileChromeUserAgent =
    'Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36';
const String kDesktopChromeUserAgent =
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.82 Safari/537.36';
const String kIPhoneSafariUserAgent =
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.4 Mobile/15E148 Safari/604.1';
final List<ContentBlocker> kDefaultContentBlockers = <ContentBlocker>[
  ContentBlocker(
    trigger: ContentBlockerTrigger(
      urlFilter: '.*(doubleclick\\.net).*',
      unlessDomain: ['upbhulekh.gov.in'],
    ),
    action: ContentBlockerAction(type: ContentBlockerActionType.BLOCK),
  ),
  ContentBlocker(
    trigger: ContentBlockerTrigger(
      urlFilter: '.*(googletagmanager\\.com).*',
      unlessDomain: ['upbhulekh.gov.in'],
    ),
    action: ContentBlockerAction(type: ContentBlockerActionType.BLOCK),
  ),
  ContentBlocker(
    trigger: ContentBlockerTrigger(
      urlFilter: '.*(google-analytics\\.com).*',
      unlessDomain: ['upbhulekh.gov.in'],
    ),
    action: ContentBlockerAction(type: ContentBlockerActionType.BLOCK),
  ),
  ContentBlocker(
    trigger: ContentBlockerTrigger(
      urlFilter: '.*(googlesyndication\\.com).*',
      unlessDomain: ['upbhulekh.gov.in'],
    ),
    action: ContentBlockerAction(type: ContentBlockerActionType.BLOCK),
  ),
];

class InAppWebViewPage extends StatefulWidget {
  const InAppWebViewPage({
    super.key,
    required this.title,
    required this.initialUrl,
    this.enableFindInPage = false,
    this.nextUrl,
    this.overrideUserAgent,
    this.initialHeaders,
    this.nextUrlHeaders,
    this.nextUrlViaJavascript = false,
    this.nextUrlDelay = const Duration(milliseconds: 700),
    this.forceMobileMode = false,
    this.popupWindowId,
    this.downloadReferer,
    this.automationProfile,
    this.automationDelay = const Duration(milliseconds: 700),
    this.disableViewportFix = false,
    this.disableContentBlockers = false,
    this.initialLoadDelay = const Duration(milliseconds: 600),
  });

  final String title;
  final String initialUrl;
  final bool enableFindInPage;
  // Optional: when provided, this URL will be opened automatically
  // after the first page finishes loading. Useful for sites that
  // require a home page or gateway to be visited first to set cookies/session.
  final String? nextUrl;
  final String? overrideUserAgent;
  final Map<String, String>? initialHeaders;
  final Map<String, String>? nextUrlHeaders;
  final bool nextUrlViaJavascript;
  final Duration nextUrlDelay;
  final bool forceMobileMode;
  final int? popupWindowId;
  // When opening in a popup, use this as referer for downloads (helps SEC up.nic.in)
  final String? downloadReferer;
  // Optional automation profile (e.g., 'npci_base') to run page-specific JS flows.
  final String? automationProfile;
  final Duration automationDelay;
  final bool disableViewportFix;
  final bool disableContentBlockers;
  // Delay before the very first load to let WebView initialize and cookies attach reliably
  final Duration initialLoadDelay;

  @override
  State<InAppWebViewPage> createState() => _InAppWebViewPageState();
}

class SimpleWebError {
  final int code;
  final String description;
  final String? url;
  const SimpleWebError({
    required this.code,
    required this.description,
    this.url,
  });
}

class _InAppWebViewPageState extends State<InAppWebViewPage> {
  InAppWebViewController? _controller;
  int _progress = 0;
  SimpleWebError? _lastError;
  // All navigation stays within the in-app WebView.
  // Find-in-page state
  final TextEditingController _findCtrl = TextEditingController();
  bool _isSearching = false;
  int _loadCount = 0; // counts completed loads
  bool _didOpenNext = false; // ensure nextUrl is opened only once
  String? _currentUrl;
  final Map<String, int> _rationRetryCount = {};
  bool _uaFallbackTried = false;
  // Automation state
  bool _npciStage1Done = false; // clicked Customer and then BASE link
  bool _npciStage2Done = false; // redirected from rid page to homepage
  bool _vaadVarasatApplyTriggered = false;
  bool _vaadVarasatStatusTriggered = false;
  bool _vaadChainStep1Done = false;
  bool _vaadChainStep2Done = false;
  bool _vaadChainVisitedHome = false;

  @override
  void initState() {
    super.initState();
    _ensureLocationPermission();
  }

  Future<void> _applyBhulekhTweaks(InAppWebViewController controller) async {
    try {
      final url = await controller.getUrl();
      final host = url?.host.toLowerCase() ?? '';
      if (!host.contains('upbhulekh.gov.in')) {
        return;
      }
      await controller.evaluateJavascript(
        source: r'''
        (function(){
          try {
            var body = document.body;
            if (body) {
              body.style.removeProperty('height');
              body.style.removeProperty('overflow');
              body.style.visibility = 'visible';
              body.style.opacity = '1';
            }
            var overlayContainer = document.querySelector('.cdk-overlay-container');
            if (overlayContainer) {
              overlayContainer.style.overflow = 'visible';
            }
            var overlays = document.querySelectorAll('.cdk-overlay-pane, .mat-select-panel, .mat-autocomplete-panel');
            overlays.forEach(function(el){
              try {
                el.style.maxHeight = 'calc(100vh - 80px)';
                el.style.overflowY = 'auto';
                el.style.overflowX = 'hidden';
              } catch (_) {}
            });
            setTimeout(function(){
              try {
                var overlaysLate = document.querySelectorAll('.cdk-overlay-pane, .mat-select-panel, .mat-autocomplete-panel');
                overlaysLate.forEach(function(el){
                  el.style.maxHeight = 'calc(100vh - 80px)';
                  el.style.overflowY = 'auto';
                  el.style.overflowX = 'hidden';
                });
              } catch (_) {}
            }, 400);
          } catch (_) {}
        })();
        ''' ,
      );
    } catch (_) {}
  }

  bool _isNpciHost(String host) {
    final h = host.toLowerCase();
    return h.endsWith('npci.org.in') || h.endsWith('npci.org');
  }

  bool _isBaseNpciHost(String host) {
    final h = host.toLowerCase();
    return h.endsWith('base.npci.org.in') || h.endsWith('base.npci.org');
  }

  Future<void> _runAutomationIfAny(
    InAppWebViewController controller,
    WebUri? url,
  ) async {
    if (widget.automationProfile == null || widget.automationProfile!.isEmpty) {
      return;
    }
    final profile = widget.automationProfile!.toLowerCase();
    final current = (url?.toString().isNotEmpty == true)
        ? url!.toString()
        : (_currentUrl ?? '');
    if (current.isEmpty) return;
    final host = Uri.tryParse(current)?.host.toLowerCase() ?? '';

    if (profile == 'npci_base') {
      final delayMs = widget.automationDelay.inMilliseconds.clamp(0, 5000);

      // Stage 1: On npci.org homepage, click Customer tab then BASE link with small delays.
      if (!_npciStage1Done && _isNpciHost(host) && !host.startsWith('base.')) {
        try {
          final result = await controller.callAsyncJavaScript(
            functionBody: r'''
              const delay = arguments['delay'];
              const wait = (ms) => new Promise(res => setTimeout(res, ms));
              const norm = (s) => (s || '').toString().replace(/\s+/g, ' ').trim().toLowerCase();
              const clickEl = (el) => {
                if (!el) return false;
                try { el.click(); return true; } catch (_) {}
                try { el.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true })); return true; } catch (_) {}
                try { el.dispatchEvent(new Event('click', { bubbles: true, cancelable: true })); return true; } catch (_) {}
                return false;
              };
              const findToggle = () => {
                const selectors = [
                  'button.navbar-toggler',
                  'button.menu-toggle',
                  'button[data-bs-toggle="collapse"]',
                  'button[aria-label*="menu" i]',
                  'button[aria-label*="toggle" i]',
                  'button[aria-controls*="menu" i]',
                  '.navbar-toggle',
                  '.hamburger',
                  '.menu-toggle'
                ];
                for (const sel of selectors) {
                  const el = document.querySelector(sel);
                  if (el && getComputedStyle(el).display !== 'none') return el;
                }
                const fallback = Array.from(document.querySelectorAll('button,div,a,span')).find((el) => {
                  const text = norm(el.textContent || '');
                  if (!text) return false;
                  return text.includes('menu') || text.includes('मेनू');
                });
                return fallback || null;
              };
              const findByText = (selector, texts, extraCheck) => {
                const els = Array.from(document.querySelectorAll(selector));
                const keys = (texts || []).map((t) => norm(t));
                for (const el of els) {
                  if (typeof extraCheck === 'function' && !extraCheck(el)) continue;
                  const txt = norm(el.textContent || '');
                  if (!txt) continue;
                  if (keys.some((k) => txt.includes(k))) return el;
                }
                return null;
              };
              async function run() {
                try {
                  await wait(delay || 600);
                  const toggle = findToggle();
                  if (toggle) {
                    clickEl(toggle);
                    await wait(500);
                  }
                  const customer = findByText('button, a, div, span, li', ['customer', 'customers', 'ग्राहक'], null);
                  if (customer) {
                    const expanded = (customer.getAttribute && customer.getAttribute('aria-expanded')) || '';
                    if (expanded && expanded.toLowerCase() === 'false') {
                      clickEl(customer);
                      await wait(450);
                    } else {
                      clickEl(customer);
                      await wait(450);
                    }
                  }
                  const baseTarget = findByText(
                    'a, button, div, span',
                    ['bharat aadhaar seeding enabler (base)', 'bharat aadhaar seeding enabler', 'aadhaar seeding enabler', 'aadhaar seeding', 'base'],
                    (el) => {
                      try {
                        const href = (el.getAttribute && el.getAttribute('href')) || '';
                        if (href && href.toLowerCase().includes('base.npci.org')) return true;
                      } catch (_) {}
                      return true;
                    }
                  );
                  if (baseTarget) {
                    try {
                      if (baseTarget.tagName === 'A') {
                        baseTarget.setAttribute('target', '_self');
                      }
                    } catch (_) {}
                    if (!clickEl(baseTarget)) {
                      return 'base_click_failed';
                    }
                    await wait(200);
                    try {
                      const href = (baseTarget.getAttribute && baseTarget.getAttribute('href')) || '';
                      if (href && href.trim() !== '' && !href.startsWith('#')) {
                        window.location.href = href;
                      }
                    } catch (_) {}
                    return 'clicked_base';
                  }
                  const fallbackLink = Array.from(document.querySelectorAll('a')).find((a) => {
                    const href = (a.href || '').toLowerCase();
                    return href.includes('base.npci.org') || href.includes('aadhaarseedingrequest');
                  });
                  if (fallbackLink && fallbackLink.href) {
                    window.location.href = fallbackLink.href;
                    return 'navigated_fallback';
                  }
                  return 'not_found';
                } catch (e) {
                  return 'error';
                }
              }
              return run();
            ''',
            arguments: {'delay': delayMs},
          );
          final value = result?.value;
          if (value is String) {
            if (value.startsWith('clicked') || value.startsWith('navigated')) {
              _npciStage1Done = true;
            }
          } else if (value != null) {
            _npciStage1Done = true;
          }
        } catch (_) {}
      }

      // Stage 2: On base.npci.org rid page, nudge to homepage after small delay so session is set.
      // Example: https://base.npci.org.in/aadhaarSeedingRequest?rid=....
      if (!_npciStage2Done && _isBaseNpciHost(host)) {
        final u = current.toLowerCase();
        if (u.contains('/aadhaarseedingrequest') && u.contains('rid=')) {
          try {
            await controller.callAsyncJavaScript(
              functionBody: r'''
                const delay = arguments['delay'];
                setTimeout(function(){
                  try{
                    var target = 'https://base.npci.org.in/base/homepage';
                    if (location.hostname.endsWith('base.npci.org')) target = 'https://base.npci.org/base/homepage';
                    window.location.href = target;
                  }catch(_){ }
                }, delay || 700);
              ''',
              arguments: {'delay': delayMs},
            );
            _npciStage2Done = true;
          } catch (_) {}
        }
      }
    }

    if (profile == 'fcs_to_nfsa_citizen') {
      final d = widget.automationDelay.inMilliseconds.clamp(0, 5000);
      if (host.contains('fcs.up.gov.in')) {
        try {
          await controller.callAsyncJavaScript(
            functionBody: r'''
              const d=arguments['delay'];
              setTimeout(function(){ try{ window.location.href='https://nfsa.up.gov.in/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1'; }catch(_){ } }, d||800);
            ''',
            arguments: {'delay': d},
          );
        } catch (_) {}
      } else if (host.contains('nfsa.up.gov.in')) {
        try {
          await controller.callAsyncJavaScript(
            functionBody: r'''
              const toCitizen = 'https://nfsa.up.gov.in/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1';
              const u=location.pathname.toLowerCase();
              if (!u.includes('/food/citizen/default.aspx')) { try{ window.location.href=toCitizen; }catch(_){ } }
            ''',
          );
        } catch (_) {}
      }
    }

    if (profile == 'fcs_to_nfsa_search') {
      final d = widget.automationDelay.inMilliseconds.clamp(0, 5000);
      if (host.contains('fcs.up.gov.in')) {
        try {
          await controller.callAsyncJavaScript(
            functionBody: r'''
              const d=arguments['delay'];
              setTimeout(function(){ try{ window.location.href='https://nfsa.up.gov.in/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1'; }catch(_){ } }, d||800);
            ''',
            arguments: {'delay': d},
          );
        } catch (_) {}
      } else if (host.contains('nfsa.up.gov.in')) {
        try {
          await controller.callAsyncJavaScript(
            functionBody: r'''
              const d=arguments['delay'];
              const toCitizen = 'https://nfsa.up.gov.in/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1';
              const toSearch = 'https://nfsa.up.gov.in/Food/TrackingRationCard/NFSARCSearch.aspx?AspxAutoDetectCookieSupport=1';
              const u=location.pathname.toLowerCase();
              if (!u.includes('/food/citizen/default.aspx') && !u.includes('/food/trackingrationcard/nfsarcsearch.aspx')) {
                setTimeout(function(){ try{ window.location.href=toCitizen; }catch(_){ } }, d||700);
              } else if (u.includes('/food/citizen/default.aspx')) {
                setTimeout(function(){ try{ window.location.href=toSearch; }catch(_){ } }, (d||700)+400);
              }
            ''',
            arguments: {'delay': d},
          );
        } catch (_) {}
      }
    }

    if (profile == 'nfsa_up_nfsarcsearch') {
      final d2 = widget.automationDelay.inMilliseconds.clamp(0, 5000);
      if (host.contains('nfsa.up.gov.in')) {
        try {
          await controller.callAsyncJavaScript(
            functionBody: r'''
              const d=arguments['delay'];
              const u=location.pathname.toLowerCase();
              const toCitizen = '/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1';
              const toSearch = '/Food/TrackingRationCard/NFSARCSearch.aspx?AspxAutoDetectCookieSupport=1';
              const jump = (p)=>{ try{ window.location.href=p; }catch(_){ } };
              if (!u.includes('/food/citizen/default.aspx') && !u.includes('/food/trackingrationcard/nfsarcsearch.aspx')) {
                setTimeout(function(){ jump(toCitizen); }, d||700);
              } else if (u.includes('/food/citizen/default.aspx')) {
                setTimeout(function(){ jump(toSearch); }, (d||700)+400);
              }
            ''',
            arguments: {'delay': d2},
          );
        } catch (_) {}
      }
    }

    if (profile == 'nfsa_up_citizen') {
      final d3 = widget.automationDelay.inMilliseconds.clamp(0, 5000);
      if (host.contains('nfsa.up.gov.in')) {
        try {
          await controller.callAsyncJavaScript(
            functionBody: r'''
              const d=arguments['delay'];
              const u=location.pathname.toLowerCase();
              const toCitizen = '/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1';
              if (!u.includes('/food/citizen/default.aspx')) {
                setTimeout(function(){ try{ window.location.href=toCitizen; }catch(_){ } }, d||700);
              }
            ''',
            arguments: {'delay': d3},
          );
        } catch (_) {}
      }
    }

    if (profile == 'vaad_varasat') {
      final Uri? initUri = Uri.tryParse(widget.initialUrl);
      final Uri? curUri = Uri.tryParse(current);
      final flow = (curUri?.queryParameters['flow'] ??
              initUri?.queryParameters['flow'] ??
              '')
          .toLowerCase();
      final bool isStatus = flow == 'status';
      final bool alreadyTriggered = isStatus
          ? _vaadVarasatStatusTriggered
          : _vaadVarasatApplyTriggered;
      if (!alreadyTriggered && host.contains('vaad.up.nic.in')) {
        final lowerCurrent = current.toLowerCase();
        if (lowerCurrent.contains('index2.html') ||
            lowerCurrent.endsWith('vaad.up.nic.in') ||
            lowerCurrent.endsWith('vaad.up.nic.in/')) {
          final target = isStatus
              ? 'search_p11_application.aspx'
              : 'khapu/pakha11user.aspx';
          try {
            final result = await controller.callAsyncJavaScript(
              functionBody: r'''
                const target = (arguments['target'] || '').toLowerCase();
                return new Promise((resolve) => {
                  let attempts = 0;
                  const maxAttempts = 12;
                  const attempt = () => {
                    attempts++;
                    const links = Array.from(document.querySelectorAll('a'));
                    for (const link of links) {
                      const href = (link.getAttribute('href') || '').toLowerCase();
                      if (href && href.includes(target)) {
                        try { link.target = '_self'; } catch (_) {}
                        try {
                          link.click();
                          resolve('clicked');
                          return;
                        } catch (_) {}
                        try {
                          if (link.href) {
                            window.location.href = link.href;
                            resolve('navigated');
                            return;
                          }
                        } catch (_) {}
                      }
                    }
                    if (attempts >= maxAttempts) {
                      resolve('not_found');
                    } else {
                      setTimeout(attempt, 250);
                    }
                  };
                  attempt();
                });
              ''',
              arguments: {'target': target},
            );
            final value = result?.value;
            if (value == 'clicked' || value == 'navigated') {
              if (isStatus) {
                _vaadVarasatStatusTriggered = true;
              } else {
                _vaadVarasatApplyTriggered = true;
              }
            }
          } catch (_) {}
        }
      }
    }

    if (profile == 'vaad_varasat_chain') {
      if (host.contains('vaad.up.nic.in')) {
        final uri = Uri.tryParse(current);
        final path = (uri?.path ?? '').toLowerCase();
        final lowerCurrent = current.toLowerCase();

        // Step 0: If we started directly on index2, go to homepage once to set cookies, then continue chain
        if (!_vaadChainVisitedHome && lowerCurrent.contains('index2.html') && !_vaadChainStep1Done) {
          try {
            await Future.delayed(widget.nextUrlDelay);
            await controller.loadUrl(
              urlRequest: URLRequest(
                url: WebUri('https://vaad.up.nic.in/'),
                headers: { 'Referer': 'https://vaad.up.nic.in/index2.html' },
              ),
            );
            _vaadChainVisitedHome = true;
            return;
          } catch (_) {}
        }

        // Step 1: On homepage -> go to index2.html via in-page click, fallback to loadUrl with Referer
        if (!_vaadChainStep1Done && (path.isEmpty || path == '/' || path == '/index.html' || path == '/default.aspx')) {
          try {
            final result = await controller.callAsyncJavaScript(
              functionBody: r'''
                return new Promise((resolve)=>{
                  try{
                    const tryClick = () => {
                      const links = Array.from(document.querySelectorAll('a'));
                      for (const a of links) {
                        const href = (a.getAttribute('href') || '').toLowerCase();
                        if (href.includes('index2.html')) {
                          try { a.target = '_self'; } catch(_){}
                          try { a.click(); resolve('clicked'); return; } catch(_){ }
                          try { if (a.href) { window.location.href = a.href; resolve('navigated'); return; } } catch(_){}
                        }
                      }
                      resolve('not_found');
                    };
                    setTimeout(tryClick, 300);
                  }catch(_){ resolve('error'); }
                });
              ''',
            );
            final value = result?.value;
            if (value == 'clicked' || value == 'navigated') {
              _vaadChainStep1Done = true;
            } else if (value == 'not_found') {
              try {
                await Future.delayed(widget.nextUrlDelay);
                await controller.loadUrl(
                  urlRequest: URLRequest(
                    url: WebUri('https://vaad.up.nic.in/index2.html'),
                    headers: { 'Referer': 'https://vaad.up.nic.in/' },
                  ),
                );
                _vaadChainStep1Done = true;
              } catch (_) {}
            }
          } catch (_) {}
        }

        // Step 2: On index2.html -> go to final KhaPu page via in-page click, fallback to loadUrl with Referer
        if (!_vaadChainStep2Done && lowerCurrent.contains('index2.html')) {
          try {
            final result = await controller.callAsyncJavaScript(
              functionBody: r'''
                return new Promise((resolve)=>{
                  try{
                    let attempts = 0;
                    const MAX = 12;
                    const tick = () => {
                      attempts++;
                      const links = Array.from(document.querySelectorAll('a'));
                      for (const a of links) {
                        const href = (a.getAttribute('href') || '').toLowerCase();
                        if (href.includes('khapu/pakha11user.aspx')) {
                          try { a.target = '_self'; } catch(_){}
                          try { a.click(); resolve('clicked'); return; } catch(_){ }
                          try { if (a.href) { window.location.href = a.href; resolve('navigated'); return; } } catch(_){}
                        }
                      }
                      if (attempts >= MAX) { resolve('not_found'); }
                      else { setTimeout(tick, 250); }
                    };
                    setTimeout(tick, 350);
                  }catch(_){ resolve('error'); }
                });
              ''',
            );
            final value = result?.value;
            if (value == 'clicked' || value == 'navigated') {
              _vaadChainStep2Done = true;
            } else if (value == 'not_found') {
              try {
                await Future.delayed(widget.nextUrlDelay);
                await controller.loadUrl(
                  urlRequest: URLRequest(
                    url: WebUri('https://vaad.up.nic.in/KhaPu/pakha11User.aspx'),
                    headers: { 'Referer': 'https://vaad.up.nic.in/index2.html' },
                  ),
                );
                _vaadChainStep2Done = true;
              } catch (_) {}
            }
          } catch (_) {}
        }
      }
    }
  }

  Future<String?> _getAbhaAuthHeader() async {
    final c = _controller;
    if (c == null) return null;
    try {
      final res = await c.callAsyncJavaScript(
        functionBody: r'''
          const vals = [];
          try { for (let i=0;i<localStorage.length;i++){ const k=localStorage.key(i); vals.push(localStorage.getItem(k)||''); } } catch(e){}
          try { for (let i=0;i<sessionStorage.length;i++){ const k=sessionStorage.key(i); vals.push(sessionStorage.getItem(k)||''); } } catch(e){}
          let tok = vals.find(v => /^Bearer\s+[^\s"']+$/.test(v)) || '';
          if (!tok) {
            const jwt = vals.find(v => /^[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+$/.test(v) || (typeof v==='string' && v.startsWith('eyJ')));
            if (jwt) tok = 'Bearer ' + jwt;
          }
          return tok || '';
        ''',
      );
      final value = res?.value;
      if (value is String && value.isNotEmpty) return value;
    } catch (_) {}
    return null;
  }

  Future<String?> _suggestFileNameForBlob(String blobUrl) async {
    final controller = _controller;
    if (controller == null) return null;
    try {
      final res = await controller.callAsyncJavaScript(
        functionBody: '''
          const target = arguments['url'];
          function clean(n){ return (n||'').trim(); }
          // 1) Exact match <a download href=blob:...>
          const anchors = Array.from(document.querySelectorAll('a[download]'));
          for (const a of anchors){
            try{
              if (a.href === target){
                const n = clean(a.getAttribute('download'));
                if (n) return n;
              }
            }catch(e){}
          }
          // 2) Any download anchor pointing to any blob
          for (const a of anchors){
            try{
              if ((a.href||'').startsWith('blob:')){
                const n = clean(a.getAttribute('download'));
                if (n) return n;
              }
            }catch(e){}
          }
          return '';
        ''',
        arguments: {'url': blobUrl},
      );
      final value = res?.value;
      if (value is String && value.isNotEmpty) return value;
    } catch (_) {}
    return null;
  }

  @override
  void dispose() {
    _findCtrl.dispose();
    super.dispose();
  }

  Future<void> _ensureLocationPermission() async {
    try {
      final status = await Permission.location.status;
      if (!status.isGranted) {
        await Permission.location.request();
      }
    } catch (_) {
      // ignore errors; webview will still try to request via its own prompt
    }
  }

  Future<bool> _handleWillPop() async {
    // If on ABHA-related page, logout then close screen instead of navigating back inside WebView
    final url = _currentUrl;
    final isAbha = url != null && _isAbhaHost(Uri.tryParse(url)?.host ?? '');
    if (isAbha) {
      await _logoutAbhaIfNeeded();
      if (mounted) Navigator.of(context).pop();
      return false;
    }
    if (_controller != null && await _controller!.canGoBack()) {
      await _controller!.goBack();
      return false;
    }
    return true;
  }

  @override
  Widget build(BuildContext context) {
    bool uaLooksMobile(String ua) {
      final u = ua.toLowerCase();
      return u.contains('mobile') ||
          u.contains('iphone') ||
          u.contains('android');
    }

    return PopScope(
      canPop: false,
      onPopInvoked: (didPop) {
        if (didPop) return;
        _handleWillPop().then((allow) {
          if (!mounted) return;
          if (allow) {
            Navigator.of(context).pop();
          }
        });
      },
      child: Scaffold(
        appBar: AppBar(
          automaticallyImplyLeading: false,
          leading: IconButton(
            tooltip: 'बंद करें',
            icon: const Icon(Icons.close),
            onPressed: () async {
              final navigator = Navigator.of(context);
              final url = _currentUrl;
              final isAbha =
                  url != null && _isAbhaHost(Uri.tryParse(url)?.host ?? '');
              if (isAbha) {
                await _logoutAbhaIfNeeded();
              }
              navigator.pop();
            },
          ),
          title: _isSearching
              ? TextField(
                  controller: _findCtrl,
                  autofocus: true,
                  textInputAction: TextInputAction.search,
                  onSubmitted: (_) => _doFindFirst(),
                  decoration: const InputDecoration(
                    hintText: 'पेज में खोजें…',
                    border: InputBorder.none,
                  ),
                  style: Theme.of(
                    context,
                  ).textTheme.titleMedium?.copyWith(fontSize: 15),
                )
              : Text(
                  widget.title,
                  style: Theme.of(
                    context,
                  ).textTheme.titleMedium?.copyWith(fontSize: 15),
                ),
          bottom: PreferredSize(
            preferredSize: const Size.fromHeight(3),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              height: _progress < 100 ? 3 : 0,
              alignment: Alignment.centerLeft,
              child: LinearProgressIndicator(
                value: _progress == 0 ? null : _progress / 100,
                minHeight: 3,
              ),
            ),
          ),
          actions: [
            IconButton(
              tooltip: 'पेज को PDF में सहेजें',
              icon: const Icon(Icons.picture_as_pdf_outlined),
              onPressed: _saveCurrentPageAsPdf,
            ),
            if (!_isSearching)
              IconButton(
                tooltip: 'पेज में खोजें',
                icon: const Icon(Icons.find_in_page_outlined),
                onPressed: () => setState(() => _isSearching = true),
              ),
            if (_isSearching) ...[
              IconButton(
                tooltip: 'पिछला',
                icon: const Icon(Icons.keyboard_arrow_up),
                onPressed: () => _doFindNext(false),
              ),
              IconButton(
                tooltip: 'अगला',
                icon: const Icon(Icons.keyboard_arrow_down),
                onPressed: () => _doFindNext(true),
              ),
              IconButton(
                tooltip: 'खोज बंद करें',
                icon: const Icon(Icons.close),
                onPressed: _closeFind,
              ),
            ],
            IconButton(
              tooltip: 'रीफ़्रेश',
              icon: const Icon(Icons.refresh),
              onPressed: () => _controller?.reload(),
            ),
          ],
        ),
        body: SafeArea(
          child: _lastError == null
              ? InAppWebView(
                  windowId: widget.popupWindowId,
                  initialUrlRequest: widget.popupWindowId == null
                      ? URLRequest(
                          url: WebUri('about:blank'),
                        )
                      : null,
                  initialSettings: InAppWebViewSettings(
                    javaScriptEnabled: true,
                    supportZoom: true,
                    mediaPlaybackRequiresUserGesture: false,
                    transparentBackground: false,
                    useShouldOverrideUrlLoading: true,
                    useOnDownloadStart: true,
                    javaScriptCanOpenWindowsAutomatically: true,
                    supportMultipleWindows: true,
                    domStorageEnabled: true,
                    databaseEnabled: true,
                    cacheEnabled: true,
                    clearCache: false,
                    loadWithOverviewMode: true,
                    cacheMode: CacheMode.LOAD_CACHE_ELSE_NETWORK,
                    loadsImagesAutomatically: true,
                    useWideViewPort: true,
                    builtInZoomControls: true,
                    displayZoomControls: false,
                    mixedContentMode:
                        MixedContentMode.MIXED_CONTENT_ALWAYS_ALLOW,
                    thirdPartyCookiesEnabled: true,
                    geolocationEnabled: true,
                    useHybridComposition: true,
                    contentBlockers: widget.disableContentBlockers
                        ? <ContentBlocker>[]
                        : kDefaultContentBlockers,
                    userAgent:
                        widget.overrideUserAgent ?? kMobileChromeUserAgent,
                    preferredContentMode: widget.forceMobileMode
                        ? UserPreferredContentMode.MOBILE
                        : (widget.overrideUserAgent != null
                              ? (uaLooksMobile(widget.overrideUserAgent!)
                                    ? UserPreferredContentMode.MOBILE
                                    : UserPreferredContentMode.DESKTOP)
                              : UserPreferredContentMode.MOBILE),
                  ),
                  onWebViewCreated: (controller) {
                    _controller = controller;
                    // Delayed initial load for stability
                    if (widget.popupWindowId == null) {
                      Future.delayed(widget.initialLoadDelay, () async {
                        if (!mounted) return;
                        try {
                          await controller.loadUrl(
                            urlRequest: URLRequest(
                              url: WebUri(widget.initialUrl),
                              headers: widget.initialHeaders,
                            ),
                          );
                        } catch (_) {}
                      });
                    }
                  },
                  onConsoleMessage: (controller, consoleMessage) {
                    if (kDebugMode) {
                      debugPrint(
                        '[WEBVIEW] ${consoleMessage.messageLevel}: ${consoleMessage.message}',
                      );
                    }
                  },
                  onLoadStart: (controller, url) {
                    setState(() {
                      _lastError = null;
                      _currentUrl = url?.toString();
                    });
                  },
                  onLoadStop: (controller, url) async {
                    setState(() {
                      _progress = 100;
                      _lastError = null;
                      _currentUrl = url?.toString();
                    });
                    _loadCount++;
                    // Always ensure a reasonable mobile viewport for better rendering on small screens.
                    if (!widget.disableViewportFix) {
                      await controller.evaluateJavascript(
                        source: r'''
                        (function() {
                          try {
                            var existing = document.querySelector('meta[name="viewport"]');
                            if (!existing) {
                              existing = document.createElement('meta');
                              existing.name = 'viewport';
                              existing.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                              document.head.appendChild(existing);
                            }
                            try {
                              var b = document.body;
                              if (b) {
                                b.style.visibility = 'visible';
                                b.style.opacity = '1';
                                if (getComputedStyle(b).display === 'none') {
                                  b.style.display = 'block';
                                }
                                b.style.removeProperty('height');
                                b.style.removeProperty('overflow');
                              }
                            } catch (_) {}
                          } catch (_) {}
                        })();
                      ''',
                      );
                    }
                    await _applyBhulekhTweaks(controller);
                    // Capture last form POST (ASP.NET WebForms) so we can reproduce downloads requiring POST/VIEWSTATE
                    try {
                      await controller.evaluateJavascript(
                        source: r'''
                        (function(){
                          try{
                            window.__yojnaLastPost = null;
                            document.addEventListener('submit', function(e){
                              try{
                                var f = e.target;
                                if(!f || f.tagName !== 'FORM') return;
                                var action = f.getAttribute('action') || location.href;
                                try { action = new URL(action, location.href).toString(); } catch(_){ }
                                var fd;
                                try { fd = new FormData(f, e.submitter); } catch(_){ fd = new FormData(f); }
                                var pairs = [];
                                fd.forEach(function(v,k){
                                  if (typeof v === 'string') {
                                    pairs.push(encodeURIComponent(k)+'='+encodeURIComponent(v));
                                  } else {
                                    pairs.push(encodeURIComponent(k)+'=');
                                  }
                                });
                                window.__yojnaLastPost = { action: action, body: pairs.join('&') };
                              } catch(_){ }
                            }, true);
                            // Also patch programmatic submits (form.submit()) used by __doPostBack
                            try{
                              var _nativeSubmit = HTMLFormElement.prototype.submit;
                              HTMLFormElement.prototype.submit = function(){
                                try{
                                  var f = this;
                                  var action = f.getAttribute('action') || location.href;
                                  try { action = new URL(action, location.href).toString(); } catch(_){ }
                                  var fd = new FormData(f);
                                  var pairs = [];
                                  fd.forEach(function(v,k){
                                    if (typeof v === 'string') {
                                      pairs.push(encodeURIComponent(k)+'='+encodeURIComponent(v));
                                    } else {
                                      pairs.push(encodeURIComponent(k)+'=');
                                    }
                                  });
                                  window.__yojnaLastPost = { action: action, body: pairs.join('&') };
                                }catch(_){ }
                                return _nativeSubmit.apply(this, arguments);
                              };
                            }catch(_){ }
                          }catch(_){ }
                        })();
                      ''',
                      );
                    } catch (_) {}
                    // If page looks blank, try fallbacks: iPhone Safari UA, then SOFTWARE layer.
                    try {
                      final status = await controller.evaluateJavascript(
                        source: r'''
                        (function(){
                          try {
                            if (!document.body) return 'EMPTY';
                            var text = (document.body.innerText||'').trim();
                            var dim = document.body.getBoundingClientRect();
                            if (text.length === 0 || dim.width === 0 || dim.height === 0) return 'EMPTY';
                            return 'OK';
                          } catch(_) { return 'ERR'; }
                        })();
                      ''',
                      );
                      if (status == 'EMPTY') {
                        if (!_uaFallbackTried) {
                          _uaFallbackTried = true;
                          await controller.setSettings(
                            settings: InAppWebViewSettings(
                              userAgent: kIPhoneSafariUserAgent,
                              preferredContentMode:
                                  UserPreferredContentMode.MOBILE,
                            ),
                          );
                          await controller.reload();
                          return;
                        }
                      }
                    } catch (_) {}
                    if (!_didOpenNext && widget.nextUrl != null) {
                      final currentStr = url?.toString() ?? _currentUrl ?? '';
                      final curHost =
                          Uri.tryParse(currentStr)?.host.toLowerCase() ?? '';
                      final initHost =
                          Uri.tryParse(widget.initialUrl)?.host.toLowerCase() ??
                          '';
                      // Only trigger the nextUrl when we are on the initial host (e.g., fcs.up.gov.in)
                      if (initHost.isEmpty || curHost == initHost) {
                        _didOpenNext = true;
                        if (widget.nextUrlViaJavascript) {
                          final escapedUrl = widget.nextUrl!.replaceAll(
                            "'",
                            r"\'",
                          );
                          final delayMs = widget.nextUrlDelay.inMilliseconds
                              .clamp(0, 10000);
                          await controller.evaluateJavascript(
                            source:
                                '''
                          setTimeout(function(){
                            try{
                              var a=document.createElement('a');
                              a.href='$escapedUrl';
                              a.target='_self';
                              (document.body||document.documentElement).appendChild(a);
                              a.click();
                            }catch(_){ }
                            try{ window.location.assign('$escapedUrl'); }catch(_){ }
                            try{ location.href='$escapedUrl'; }catch(_){ }
                          }, $delayMs);
                        ''',
                          );
                        } else {
                          final headers = <String, String>{
                            'Referer': widget.initialUrl,
                            if (widget.nextUrlHeaders != null)
                              ...widget.nextUrlHeaders!,
                          };
                          await Future.delayed(widget.nextUrlDelay);
                          await controller.loadUrl(
                            urlRequest: URLRequest(
                              url: WebUri(widget.nextUrl!),
                              headers: headers,
                            ),
                          );
                        }
                      }
                    }
                    // Run optional automation after every load stop
                    await _runAutomationIfAny(controller, url);
                  },
                  onProgressChanged: (controller, progress) {
                    setState(() => _progress = progress);
                  },
                  shouldOverrideUrlLoading: (controller, navigationAction) async {
                    final uri = navigationAction.request.url;
                    if (uri == null) return NavigationActionPolicy.CANCEL;
                    final scheme = uri.scheme.toLowerCase();
                    // NFSA root guard: if portal bounces to root, force the intended page
                    try {
                      final host = uri.host.toLowerCase();
                      final path = uri.path.toLowerCase();
                      final profile = (widget.automationProfile ?? '')
                          .toLowerCase();
                      if (host.contains('nfsa.up.gov.in') &&
                          (path == '/' ||
                              path == '/food/' ||
                              path == '/food')) {
                        if (profile == 'nfsa_up_citizen' ||
                            profile == 'fcs_to_nfsa_citizen') {
                          await Future.delayed(widget.nextUrlDelay);
                          await controller.loadUrl(
                            urlRequest: URLRequest(
                              url: WebUri(
                                'https://nfsa.up.gov.in/Food/citizen/Default.aspx?AspxAutoDetectCookieSupport=1',
                              ),
                              headers: {
                                'Referer': _currentUrl ?? widget.initialUrl,
                              },
                            ),
                          );
                          return NavigationActionPolicy.CANCEL;
                        } else if (profile == 'nfsa_up_nfsarcsearch' ||
                            profile == 'fcs_to_nfsa_search') {
                          await Future.delayed(widget.nextUrlDelay);
                          await controller.loadUrl(
                            urlRequest: URLRequest(
                              url: WebUri(
                                'https://nfsa.up.gov.in/Food/TrackingRationCard/NFSARCSearch.aspx?AspxAutoDetectCookieSupport=1',
                              ),
                              headers: {
                                'Referer': _currentUrl ?? widget.initialUrl,
                              },
                            ),
                          );
                          return NavigationActionPolicy.CANCEL;
                        }
                      }
                    } catch (_) {}
                    if (scheme == 'http' ||
                        scheme == 'https' ||
                        scheme == 'about' ||
                        scheme == 'blob' ||
                        scheme == 'data' ||
                        scheme == 'javascript') {
                      if (scheme == 'http' || scheme == 'https') {
                        final host = uri.host.toLowerCase();
                        const secDownloadPaths = [
                          '/site/download',
                          '/site/downloadfile',
                          '/site/downloadhandler',
                          '/site/voterlistdownload',
                        ];
                        if (host.endsWith('sec.up.nic.in')) {
                          final pathLower = uri.path.toLowerCase();
                          if (pathLower.endsWith('.pdf') ||
                              pathLower.contains('/download') ||
                              secDownloadPaths.any(pathLower.startsWith)) {
                            final opened = await _launchExternalUrl(uri);
                            if (opened) {
                              _showSnack(
                                'फाइल सिस्टम ब्राउज़र में खोली गई है।',
                              );
                              return NavigationActionPolicy.CANCEL;
                            }
                          }
                        }
                      }
                      return NavigationActionPolicy.ALLOW;
                    }
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('यह लिंक ऐप में समर्थित नहीं है'),
                      ),
                    );
                    return NavigationActionPolicy.CANCEL;
                  },
                  onCreateWindow: (controller, createWindowRequest) async {
                    // Open popups in another in-app WebView that is linked via windowId
                    await Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => InAppWebViewPage(
                          title: widget.title,
                          initialUrl: 'about:blank',
                          overrideUserAgent: widget.overrideUserAgent,
                          initialHeaders: widget.initialHeaders,
                          nextUrl: null,
                          nextUrlViaJavascript: false,
                          forceMobileMode: widget.forceMobileMode,
                          popupWindowId: createWindowRequest.windowId,
                          downloadReferer: _currentUrl,
                          automationProfile: widget.automationProfile,
                          automationDelay: widget.automationDelay,
                        ),
                      ),
                    );
                    return true;
                  },
                  onReceivedServerTrustAuthRequest:
                      (controller, challenge) async {
                        // Proceed for all hosts to keep pages loading in-app.
                        return ServerTrustAuthResponse(
                          action: ServerTrustAuthResponseAction.PROCEED,
                        );
                      },
                  onGeolocationPermissionsShowPrompt:
                      (controller, origin) async {
                        // Allow site geolocation requests inside WebView.
                        return GeolocationPermissionShowPromptResponse(
                          origin: origin,
                          allow: true,
                          retain: true,
                        );
                      },
                  onRenderProcessGone: (controller, detail) async {
                    // Auto-reload if the WebView renderer crashes.
                    await controller.reload();
                  },
                  onReceivedError: (controller, request, error) {
                    final bool isMainFrame = request.isForMainFrame ?? true;
                    if (!isMainFrame) {
                      return;
                    }
                    final scheme = request.url.scheme.toLowerCase();
                    if (scheme == 'about' || scheme == 'data' || scheme == 'javascript' || scheme.isEmpty) {
                      return;
                    }
                    final host = request.url.host.toLowerCase();
                    final int code = (() {
                      try {
                        final dynamic e = error;
                        final c = e.errorCode;
                        if (c is int) return c;
                      } catch (_) {}
                      return -1;
                    })();
                    final bool isTransient =
                        code == -6 /* ERROR_HOST_LOOKUP */ ||
                        code == -7 /* ERROR_CONNECT */ ||
                        code == -8 /* ERROR_TIMEOUT */ ||
                        code == -2 /* ERROR_UNKNOWN */;
                    final bool isGovNic =
                        host == 'gov.in' ||
                        host == 'nic.in' ||
                        host.endsWith('.gov.in') ||
                        host.endsWith('.nic.in') ||
                        host.contains('rhreporting.nic.in');

                    if (isGovNic && isTransient) {
                      _rationRetryCount[host] =
                          (_rationRetryCount[host] ?? 0) + 1;
                      if (_rationRetryCount[host]! <= 2) {
                        controller.reload();
                        return;
                      }
                      // Allow user to continue without closing WebView
                      return;
                    }

                    // Retry logic for ration card related hosts on transient errors
                    final isRationHost = _isRationHost(host);
                    if (isRationHost && isTransient) {
                      _rationRetryCount[host] =
                          (_rationRetryCount[host] ?? 0) + 1;
                      if (_rationRetryCount[host]! <= 2) {
                        controller.reload();
                        return;
                      }
                    }

                    setState(
                      () => _lastError = SimpleWebError(
                        code: code,
                        description: error.description,
                        url: request.url.toString(),
                      ),
                    );
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('लोड करने में समस्या: $code')),
                    );
                  },
                  onReceivedHttpError: (controller, request, response) async {
                    final bool isMain = request.isForMainFrame ?? true;
                    if (!isMain) {
                      return;
                    }
                    final statusCode = response.statusCode ?? -1;
                    final host = request.url.host.toLowerCase();
                    final bool isGovNic =
                        host == 'gov.in' ||
                        host == 'nic.in' ||
                        host.endsWith('.gov.in') ||
                        host.endsWith('.nic.in') ||
                        host.contains('rhreporting.nic.in');
                    if (isGovNic &&
                        (statusCode == 404 ||
                            statusCode == 500 ||
                            statusCode == 503)) {
                      // For rhreporting.nic.in specifically, auto recover by visiting Home then retry nextUrl
                      if (host.contains('rhreporting.nic.in')) {
                        try {
                          _didOpenNext =
                              false; // allow nextUrl to trigger again after Home
                          await controller.loadUrl(
                            urlRequest: URLRequest(
                              url: WebUri(
                                'https://rhreporting.nic.in/netiay/Home.aspx',
                              ),
                              headers: {
                                'Referer': _currentUrl ?? widget.initialUrl,
                              },
                            ),
                          );
                        } catch (_) {}
                      }
                      // Suppress error overlay; many NIC/GOV sites briefly return errors before redirecting
                      return;
                    }
                    setState(
                      () => _lastError = SimpleWebError(
                        code: statusCode,
                        description:
                            response.reasonPhrase ?? 'HTTP $statusCode',
                        url: request.url.toString(),
                      ),
                    );
                  },
                  onDownloadStartRequest: (controller, request) async {
                    await _handleDownloadRequest(request);
                  },
                  shouldInterceptRequest: (controller, req) async {
                    try {
                      if (!Platform.isAndroid) return null;
                      final url = req.url;
                      final isMain = req.isForMainFrame ?? true;
                      if (!isMain) return null;
                      final host = url.host.toLowerCase();
                      // IMPORTANT: Do not intercept government/NIC domains; these portals
                      // rely on native WebView navigation, cookies, headers and ASP.NET postbacks.
                      if (host == 'gov.in' ||
                          host == 'nic.in' ||
                          host.endsWith('.gov.in') ||
                          host.endsWith('.nic.in') ||
                          host.endsWith('up.nic.in') ||
                          host.endsWith('up.gov.in') ||
                          host.contains('rhreporting.nic.in')) {
                        return null;
                      }

                      final Uri uri = Uri.parse(url.toString());

                      final client = HttpClient();
                      client.badCertificateCallback = (c, h, p) => true;
                      final httpReq = await client.openUrl(
                        (req.method ?? 'GET').toUpperCase(),
                        uri,
                      );
                      httpReq.followRedirects = true;

                      // Copy request headers except X-Requested-With
                      final headers = Map<String, String>.from(
                        req.headers ?? {},
                      );
                      headers.removeWhere(
                        (k, v) => k.toLowerCase() == 'x-requested-with',
                      );
                      headers['Accept-Language'] =
                          'hi-IN,hi;q=0.9,en-US;q=0.8,en;q=0.7';
                      headers['Upgrade-Insecure-Requests'] = '1';
                      headers['User-Agent'] =
                          widget.overrideUserAgent ?? kMobileChromeUserAgent;
                      final String ref =
                          (_currentUrl != null && _currentUrl!.isNotEmpty)
                          ? _currentUrl!
                          : widget.initialUrl;
                      if (ref.isNotEmpty) headers['Referer'] = ref;

                      // Attach cookies
                      try {
                        final cookies = await CookieManager.instance()
                            .getCookies(url: url);
                        if (cookies.isNotEmpty) {
                          headers[HttpHeaders.cookieHeader] = cookies
                              .map((c) => '${c.name}=${c.value}')
                              .join('; ');
                        }
                      } catch (_) {}

                      headers.forEach((k, v) {
                        if (k.isEmpty || v.isEmpty) return;
                        try {
                          httpReq.headers.set(k, v);
                        } catch (_) {}
                      });

                      final httpRes = await httpReq.close();
                      final bytes = await consolidateHttpClientResponseBytes(
                        httpRes,
                      );
                      final contentType =
                          httpRes.headers.contentType?.mimeType ?? 'text/html';
                      final encoding =
                          httpRes.headers.contentType?.charset ?? 'utf-8';
                      final reason = httpRes.reasonPhrase ?? '';
                      final respHeaders = <String, String>{};
                      for (final h in [
                        'cache-control',
                        'pragma',
                        'expires',
                        'content-disposition',
                      ]) {
                        final v = httpRes.headers.value(h);
                        if (v != null) respHeaders[h] = v;
                      }
                      client.close(force: true);
                      return WebResourceResponse(
                        data: Uint8List.fromList(bytes),
                        contentType: contentType,
                        contentEncoding: encoding,
                        statusCode: httpRes.statusCode,
                        reasonPhrase: reason,
                        headers: respHeaders,
                      );
                    } catch (_) {
                      return null;
                    }
                  },
                )
              : _ErrorFallback(
                  error: _lastError!,
                  onRetry: () {
                    setState(() => _lastError = null);
                    _controller?.reload();
                  },
                ),
        ),
      ),
    );
  }

  // Simple cross-platform find-in-page using window.find()
  Future<void> _doFindFirst() async {
    final q = _findCtrl.text.trim();
    if (q.isEmpty) return;
    await _controller?.evaluateJavascript(
      source:
          "try{window.find(${jsonEncode(q)}, false, false, true, false, false, false);}catch(e){}",
    );
  }

  Future<void> _doFindNext(bool forward) async {
    final q = _findCtrl.text.trim();
    if (q.isEmpty) return;
    final backward = (!forward).toString();
    await _controller?.evaluateJavascript(
      source:
          "try{window.find(${jsonEncode(q)}, false, $backward, true, false, false, false);}catch(e){}",
    );
  }

  Future<void> _closeFind() async {
    setState(() => _isSearching = false);
    _findCtrl.clear();
    await _controller?.evaluateJavascript(
      source: "try{window.getSelection().removeAllRanges();}catch(e){}",
    );
  }

  Future<void> _saveCurrentPageAsPdf() async {
    final controller = _controller;
    if (controller == null) {
      _showSnack('वेब पेज अभी लोड नहीं हुआ है।');
      return;
    }
    try {
      await controller.printCurrentPage();
    } catch (e) {
      _showSnack('प्रिंट विकल्प खोलने में समस्या: $e');
    }
  }

  Future<bool> _launchExternalForDownload(DownloadStartRequest request) async {
    final raw = request.url.toString();
    if (raw.isEmpty) return false;
    final uri = Uri.tryParse(raw);
    if (uri == null) return false;
    return _launchExternalUrl(uri);
  }

  Future<bool> _launchExternalUrl(Uri uri) async {
    final scheme = uri.scheme.toLowerCase();
    if (scheme != 'http' && scheme != 'https') return false;
    try {
      return await launchUrl(uri, mode: LaunchMode.externalApplication);
    } catch (_) {
      return false;
    }
  }

  Future<void> _handleDownloadRequest(DownloadStartRequest request) async {
    if (!mounted) return;

    final messenger = ScaffoldMessenger.maybeOf(context);
    messenger?.hideCurrentSnackBar();

    // ABHA sites often require session-bound requests (tokens/POST). Use DM for others; ABHA uses in-page fetch.
    final reqHost = request.url.host.toLowerCase();
    final curHost = (Uri.tryParse(_currentUrl ?? '')?.host ?? '').toLowerCase();
    final isAbha = _isAbhaHost(reqHost) || _isAbhaHost(curHost);
    final isUpNic =
        reqHost.endsWith('up.nic.in') || curHost.endsWith('up.nic.in');

    // For SEC up.nic.in, first try JS GET (same URL) with credentials, then try reproducing last form POST.
    if (!isAbha && isUpNic) {
      final openedExternally = await _launchExternalForDownload(request);
      if (openedExternally) {
        messenger?.hideCurrentSnackBar();
        if (!mounted) return;
        _showSnack('फाइल सिस्टम ब्राउज़र में खोली गई है।');
        return;
      }
      messenger?.showSnackBar(
        const SnackBar(
          content: Row(
            children: [
              SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2.2),
              ),
              SizedBox(width: 14),
              Expanded(
                child: Text('फाइल डाउनलोड हो रही है… कृपया प्रतीक्षा करें।'),
              ),
            ],
          ),
          duration: Duration(seconds: 15),
          behavior: SnackBarBehavior.floating,
        ),
      );
      // 1) Try JS GET fetch for the download URL (preserves cookies/session fully).
      try {
        final fetched = await _tryFetchViaJsGetUrl(request);
        if (fetched != null && fetched.bytes.isNotEmpty) {
          messenger?.hideCurrentSnackBar();
          final bytes = fetched.bytes;
          final mime = fetched.mimeType ?? 'application/pdf';
          final looksPdf =
              bytes.length > 4 &&
              bytes[0] == 0x25 &&
              bytes[1] == 0x50 &&
              bytes[2] == 0x44 &&
              bytes[3] == 0x46;
          if (looksPdf || mime.toLowerCase().contains('pdf')) {
            String fileName = _resolveFileName(request);
            if ((fileName.isEmpty || p.extension(fileName).isEmpty) &&
                fetched.contentDisposition != null) {
              final fromResp = _filenameFromContentDisposition(
                fetched.contentDisposition,
              );
              if (fromResp != null && fromResp.trim().isNotEmpty) {
                fileName = fromResp.trim();
              }
            }
            if (fileName.isEmpty) fileName = 'voterlist.pdf';
            final savedPath = await _saveDownloadedBytes(bytes, fileName, mime);
            try {
              final entryId = DateTime.now().microsecondsSinceEpoch.toString();
              String? path;
              String? contentUri;
              if (savedPath != null &&
                  savedPath.isNotEmpty &&
                  savedPath != 'downloads') {
                if (savedPath.startsWith('content://')) {
                  contentUri = savedPath;
                } else if (savedPath.startsWith('file://')) {
                  path = Uri.parse(savedPath).toFilePath();
                } else {
                  path = savedPath;
                }
              }
              await DownloadsStore.add(
                DownloadEntry(
                  id: entryId,
                  fileName: fileName,
                  viaDownloadManager: false,
                  timestampMs: DateTime.now().millisecondsSinceEpoch,
                  mimeType: mime,
                  path: path,
                  contentUri: contentUri,
                ),
              );
            } catch (_) {}
            if (!mounted) return;
            if (savedPath == null ||
                savedPath.isEmpty ||
                savedPath == 'downloads') {
              _showSnack('फाइल “Downloads” में सहेजी गई।');
            } else {
              _showSnack('फाइल सहेजी गई: $savedPath');
            }
            return;
          }
        }
      } catch (_) {}
      // 2) Try reproducing captured POST
      try {
        final captured = await _tryFetchViaCapturedPost(request);
        messenger?.hideCurrentSnackBar();
        if (captured != null && captured.bytes.isNotEmpty) {
          final bytes = captured.bytes;
          final mime = captured.mimeType ?? 'application/pdf';
          final looksPdf =
              bytes.length > 4 &&
              bytes[0] == 0x25 &&
              bytes[1] == 0x50 &&
              bytes[2] == 0x44 &&
              bytes[3] == 0x46;
          if (looksPdf || (mime.toLowerCase().contains('pdf'))) {
            String fileName = _resolveFileName(request);
            if ((fileName.isEmpty || p.extension(fileName).isEmpty) &&
                captured.contentDisposition != null) {
              final fromResp = _filenameFromContentDisposition(
                captured.contentDisposition,
              );
              if (fromResp != null && fromResp.trim().isNotEmpty) {
                fileName = fromResp.trim();
              }
            }
            if (fileName.isEmpty) fileName = 'voterlist.pdf';
            final savedPath = await _saveDownloadedBytes(bytes, fileName, mime);
            try {
              final entryId = DateTime.now().microsecondsSinceEpoch.toString();
              String? path;
              String? contentUri;
              if (savedPath != null &&
                  savedPath.isNotEmpty &&
                  savedPath != 'downloads') {
                if (savedPath.startsWith('content://')) {
                  contentUri = savedPath;
                } else if (savedPath.startsWith('file://')) {
                  path = Uri.parse(savedPath).toFilePath();
                } else {
                  path = savedPath;
                }
              }
              await DownloadsStore.add(
                DownloadEntry(
                  id: entryId,
                  fileName: fileName,
                  viaDownloadManager: false,
                  timestampMs: DateTime.now().millisecondsSinceEpoch,
                  mimeType: mime,
                  path: path,
                  contentUri: contentUri,
                ),
              );
            } catch (_) {}

            if (!mounted) return;
            if (savedPath == null || savedPath.isEmpty) {
              _showSnack('फाइल “Downloads” में सहेजी गई।');
            } else if (savedPath == 'downloads') {
              _showSnack('फाइल “Downloads” में सहेजी गई।');
            } else {
              _showSnack('फाइल सहेजी गई: $savedPath');
            }
            return; // done
          }
        }
      } catch (_) {
        messenger?.hideCurrentSnackBar();
      }
      // Captured POST failed or not PDF; try DownloadManager next
      if (await _tryAndroidDownload(request)) {
        return;
      }
      // Fall through to generic fetch with waiting snackbar
    }

    if (!isAbha && await _tryAndroidDownload(request)) {
      return;
    }

    messenger?.showSnackBar(
      SnackBar(
        content: Row(
          children: const [
            SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(strokeWidth: 2.2),
            ),
            SizedBox(width: 14),
            Expanded(
              child: Text('फाइल डाउनलोड हो रही है… कृपया प्रतीक्षा करें।'),
            ),
          ],
        ),
        duration: const Duration(seconds: 6),
        behavior: SnackBarBehavior.floating,
      ),
    );

    try {
      final result = await _fetchDownloadBytes(request);
      if (!mounted) return;
      messenger?.hideCurrentSnackBar();
      if (result == null) {
        _showSnack('फाइल डाउनलोड नहीं हो पाई।');
        return;
      }
      final bytes = result.bytes;
      if (bytes.isEmpty) {
        _showSnack('फाइल डाउनलोड नहीं हो पाई।');
        return;
      }

      final networkMime = result.mimeType;
      final effectiveMime = (networkMime != null && networkMime.isNotEmpty)
          ? networkMime
          : (request.mimeType ?? '');
      final urlScheme = request.url.scheme.toLowerCase();
      String fileName = _resolveFileName(request);
      if ((fileName.isEmpty || p.extension(fileName).isEmpty) &&
          result.contentDisposition != null) {
        final fromResp = _filenameFromContentDisposition(
          result.contentDisposition,
        );
        if (fromResp != null && fromResp.trim().isNotEmpty) {
          fileName = fromResp.trim();
        }
      }
      if ((fileName.isEmpty || p.extension(fileName).isEmpty) &&
          (urlScheme == 'blob')) {
        final suggested = await _suggestFileNameForBlob(request.url.toString());
        if (suggested != null && suggested.trim().isNotEmpty) {
          fileName = suggested.trim();
        }
      }
      final ext = _resolveExtension(fileName, effectiveMime);
      // Validate PDF bytes to avoid saving HTML error pages as tiny PDFs
      final lowerMime = (effectiveMime).toLowerCase();
      final bool expectPdf =
          lowerMime.contains('pdf') ||
          p.extension(fileName).toLowerCase() == '.pdf' ||
          ext.toLowerCase() == 'pdf';
      final bool looksPdf =
          bytes.length > 4 &&
          bytes[0] == 0x25 &&
          bytes[1] == 0x50 &&
          bytes[2] == 0x44 &&
          bytes[3] == 0x46; // %PDF
      if (expectPdf && !looksPdf) {
        // Fallback: for SEC up.nic.in, try Android DownloadManager with headers/cookies
        final hostNow = (Uri.tryParse(_currentUrl ?? '')?.host ?? '')
            .toLowerCase();
        final isUpNicNow =
            hostNow.endsWith('up.nic.in') ||
            request.url.host.toLowerCase().endsWith('up.nic.in');
        if (isUpNicNow) {
          final dmOk = await _tryAndroidDownload(request);
          if (dmOk) {
            return;
          }
        }
      }
      String resolvedName = fileName.isNotEmpty
          ? fileName
          : 'downloaded-file.$ext';
      if (p.extension(resolvedName).isEmpty && ext.isNotEmpty) {
        resolvedName = '$resolvedName.$ext';
      }
      resolvedName = _overrideFileNameByContext(resolvedName, defaultExt: ext);
      final savedPath = await _saveDownloadedBytes(
        bytes,
        resolvedName,
        effectiveMime,
      );
      // Log into local store for Downloads screen
      try {
        final entryId = DateTime.now().microsecondsSinceEpoch.toString();
        String? path;
        String? contentUri;
        if (savedPath != null &&
            savedPath.isNotEmpty &&
            savedPath != 'downloads') {
          if (savedPath.startsWith('content://')) {
            contentUri = savedPath;
          } else if (savedPath.startsWith('file://')) {
            path = Uri.parse(savedPath).toFilePath();
          } else {
            path = savedPath;
          }
        }
        await DownloadsStore.add(
          DownloadEntry(
            id: entryId,
            fileName: resolvedName,
            viaDownloadManager: false,
            timestampMs: DateTime.now().millisecondsSinceEpoch,
            mimeType: effectiveMime,
            path: path,
            contentUri: contentUri,
          ),
        );
      } catch (_) {}

      if (!mounted) return;
      if (savedPath == null || savedPath.isEmpty) {
        _showSnack('फाइल “Downloads” में सहेजी गई।');
      } else if (savedPath == 'downloads') {
        _showSnack('फाइल “Downloads” में सहेजी गई।');
      } else {
        _showSnack('फाइल सहेजी गई: $savedPath');
      }
    } catch (e) {
      if (!mounted) return;
      messenger?.hideCurrentSnackBar();
      _showSnack('डाउनलोड विफल: $e');
    }
  }

  Future<bool> _tryAndroidDownload(DownloadStartRequest request) async {
    if (!Platform.isAndroid ||
        defaultTargetPlatform != TargetPlatform.android) {
      return false;
    }

    final scheme = request.url.scheme.toLowerCase();
    if (scheme == 'data' || scheme == 'blob') {
      return false;
    }

    final urlString = request.url.toString();
    if (urlString.isEmpty) {
      _showSnack('अमान्य डाउनलोड लिंक।');
      return true;
    }

    String rawName = _resolveFileName(request);
    final ext = _resolveExtension(rawName, request.mimeType);
    if (rawName.isEmpty) rawName = 'downloaded-file.$ext';
    if (p.extension(rawName).isEmpty) rawName = '$rawName.$ext';
    String targetFileName = _sanitizeFileName(rawName);
    targetFileName = _overrideFileNameByContext(
      targetFileName,
      defaultExt: ext,
    );
    final String effectiveMime = request.mimeType ?? _mimeFromExtension(ext);

    final lowerEffMime = (effectiveMime ?? '').toLowerCase();
    final bool expectPdfHeader =
        lowerEffMime.contains('pdf') ||
        p.extension(targetFileName).toLowerCase() == '.pdf' ||
        ext.toLowerCase() == 'pdf';
    final headers = <String, String>{
      HttpHeaders.acceptHeader: expectPdfHeader
          ? 'application/pdf,application/octet-stream,*/*;q=0.9'
          : 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
      'Cache-Control': 'no-cache',
      'Pragma': 'no-cache',
      'Sec-Fetch-Site': 'same-origin',
      'Sec-Fetch-Mode': 'navigate',
      'Sec-Fetch-Dest': 'document',
    };

    final ua = request.userAgent;
    if (ua != null && ua.isNotEmpty) {
      headers[HttpHeaders.userAgentHeader] = ua;
    }

    String referer = _currentUrl?.isNotEmpty == true
        ? _currentUrl!
        : widget.initialUrl;
    // Prefer parent referer when available (helps popup-initiated SEC downloads)
    if ((widget.downloadReferer ?? '').isNotEmpty) {
      referer = widget.downloadReferer!;
    }
    if (referer.isNotEmpty) {
      headers['Referer'] = referer;
    }
    // Add Origin derived from referer or request URL
    try {
      final o = Uri.tryParse(referer) ?? request.url;
      final origin = '${o.scheme}://${o.host}';
      if (origin.isNotEmpty) headers['Origin'] = origin;
    } catch (_) {}

    // Language and compatibility hints (helpful for Indian e-gov portals)
    headers['Accept-Language'] = 'hi-IN,hi;q=0.9,en-US;q=0.8,en;q=0.7';
    headers['Upgrade-Insecure-Requests'] = '1';

    try {
      final cookies = await CookieManager.instance().getCookies(
        url: request.url,
      );
      if (cookies.isNotEmpty) {
        headers[HttpHeaders.cookieHeader] = cookies
            .map((c) => '${c.name}=${c.value}')
            .join('; ');
      }
    } catch (_) {
      // ignore cookie failures
    }

    headers.removeWhere((key, value) => key.isEmpty || value.isEmpty);

    try {
      final dmId = await _downloadChannel
          .invokeMethod<dynamic>('enqueueDownload', {
            'url': urlString,
            'fileName': targetFileName,
            'mimeType': effectiveMime,
            'description': widget.title,
            'headers': headers,
            'contentDisposition': request.contentDisposition,
          });
      // Log into local store for Downloads screen
      try {
        final entryId = DateTime.now().microsecondsSinceEpoch.toString();
        await DownloadsStore.add(
          DownloadEntry(
            id: entryId,
            fileName: targetFileName,
            viaDownloadManager: true,
            timestampMs: DateTime.now().millisecondsSinceEpoch,
            mimeType: effectiveMime,
            dmId: (dmId as num?)?.toInt(),
          ),
        );
      } catch (_) {}
      _showSnack('डाउनलोड शुरू हो गया है। नोटिफिकेशन देखें।');
      return true;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        debugPrint('DownloadManager enqueue failed: $e');
      }
      return false;
    } catch (e) {
      if (kDebugMode) {
        debugPrint('DownloadManager unexpected error: $e');
      }
      return false;
    }
  }

  Future<({Uint8List bytes, String? mimeType, String? contentDisposition})?>
  _fetchDownloadBytes(DownloadStartRequest request) async {
    final webUri = request.url;
    final scheme = webUri.scheme.toLowerCase();
    if (scheme == 'data') {
      final decoded = _decodeDataUrl(webUri.toString());
      if (decoded == null) {
        _showSnack('फाइल डाउनलोड नहीं हो पाई।');
      }
      if (decoded == null) return null;
      return (
        bytes: decoded.bytes,
        mimeType: decoded.mimeType,
        contentDisposition: null,
      );
    }

    if (scheme == 'blob') {
      final dataUrl = await _resolveBlobUrl(webUri.toString());
      if (dataUrl == null) {
        _showSnack('फाइल डाउनलोड नहीं हो पाई।');
        return null;
      }
      final decoded = _decodeDataUrl(dataUrl);
      if (decoded == null) {
        _showSnack('फाइल डाउनलोड नहीं हो पाई।');
        return null;
      }
      return (
        bytes: decoded.bytes,
        mimeType: decoded.mimeType,
        contentDisposition: null,
      );
    }

    // ABHA/SEC (up.nic.in) endpoints can require session-bound requests (often POST with VIEWSTATE).
    // Try fetching via page context first so cookies/tokens and method apply automatically.
    try {
      final host = webUri.host.toLowerCase();
      if (_isAbhaHost(host) || host.endsWith('up.nic.in')) {
        final res = await _controller
            ?.callAsyncJavaScript(
              functionBody: '''
            const url = arguments['url'];
            return fetch(url, {
              credentials: 'include',
              redirect: 'follow',
              cache: 'no-store',
              headers: { 'Accept': 'application/pdf,*/*' }
            })
              .then(r => r.blob())
              .then(blob => new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onloadend = () => resolve(reader.result);
                reader.onerror = () => reject('READ_ERROR');
                reader.readAsDataURL(blob);
              }));
          ''',
              arguments: {'url': webUri.toString()},
            )
            .timeout(const Duration(seconds: 15), onTimeout: () => null);
        final value = res?.value;
        if (value is String && value.startsWith('data:')) {
          final decoded = _decodeDataUrl(value);
          if (decoded != null) {
            return (
              bytes: decoded.bytes,
              mimeType: decoded.mimeType,
              contentDisposition: null,
            );
          }
        }
      }
    } catch (_) {}

    final uri = Uri.tryParse(webUri.toString());
    if (uri == null) {
      _showSnack('अमान्य डाउनलोड लिंक।');
      return null;
    }

    final guessedName = _resolveFileName(request);
    final resolvedExt = _resolveExtension(guessedName, request.mimeType);
    final bool expectPdfHeader =
        (request.mimeType?.toLowerCase().contains('pdf') ?? false) ||
        resolvedExt.toLowerCase() == 'pdf';

    final client = HttpClient();
    client.badCertificateCallback = (cert, host, port) => true;

    final httpRequest = await client.getUrl(uri);
    httpRequest.followRedirects = true;

    httpRequest.headers.set(
      HttpHeaders.acceptHeader,
      expectPdfHeader
          ? 'application/pdf,application/octet-stream,*/*;q=0.9'
          : 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
    );
    httpRequest.headers.set(
      'Accept-Language',
      'hi-IN,hi;q=0.9,en-US;q=0.8,en;q=0.7',
    );
    httpRequest.headers.set('Upgrade-Insecure-Requests', '1');
    httpRequest.headers.set('Cache-Control', 'no-cache');
    httpRequest.headers.set('Pragma', 'no-cache');
    httpRequest.headers.set('Sec-Fetch-Site', 'same-origin');
    httpRequest.headers.set('Sec-Fetch-Mode', 'navigate');
    httpRequest.headers.set('Sec-Fetch-Dest', 'document');
    // Prefer parent referer for up.nic.in and add Origin
    String httpReferer = _currentUrl?.isNotEmpty == true
        ? _currentUrl!
        : widget.initialUrl;
    if ((widget.downloadReferer ?? '').isNotEmpty) {
      httpReferer = widget.downloadReferer!;
    }
    if (httpReferer.isNotEmpty) {
      httpRequest.headers.set('Referer', httpReferer);
      try {
        final o = Uri.tryParse(httpReferer);
        if (o != null) {
          httpRequest.headers.set('Origin', '${o.scheme}://${o.host}');
        }
      } catch (_) {}
    }
    final userAgent = request.userAgent;
    if (userAgent != null && userAgent.isNotEmpty) {
      httpRequest.headers.set(HttpHeaders.userAgentHeader, userAgent);
    } else {
      httpRequest.headers.set(
        HttpHeaders.userAgentHeader,
        'Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36',
      );
    }
    if (_currentUrl != null && _currentUrl!.isNotEmpty) {
      httpRequest.headers.set('Referer', _currentUrl!);
    }

    try {
      final cookies = await CookieManager.instance().getCookies(
        url: request.url,
      );
      if (cookies.isNotEmpty) {
        final cookieHeader = cookies
            .map((c) => '${c.name}=${c.value}')
            .join('; ');
        httpRequest.headers.set(HttpHeaders.cookieHeader, cookieHeader);
      }
    } catch (_) {
      // Ignore cookie errors.
    }

    // For ABHA host, also try to attach Authorization header from storage tokens
    try {
      if (_isAbhaHost(uri.host)) {
        final auth = await _getAbhaAuthHeader();
        if (auth != null && auth.isNotEmpty) {
          httpRequest.headers.set(HttpHeaders.authorizationHeader, auth);
        }
      }
    } catch (_) {}

    final response = await httpRequest.close();
    if (response.statusCode >= 400) {
      client.close(force: true);
      throw HttpException('HTTP ${response.statusCode}', uri: uri);
    }

    final bytes = await consolidateHttpClientResponseBytes(response);
    final contentType = response.headers.contentType?.mimeType;
    final contentDisposition = response.headers.value('content-disposition');
    client.close(force: true);
    return (
      bytes: bytes,
      mimeType: contentType,
      contentDisposition: contentDisposition,
    );
  }

  // Try to fetch arbitrary URL via in-page JS GET to preserve exact session (cookies, referer, CSP context)
  Future<({Uint8List bytes, String? mimeType, String? contentDisposition})?>
  _tryFetchViaJsGetUrl(DownloadStartRequest request) async {
    try {
      final res = await _controller
          ?.callAsyncJavaScript(
            functionBody: '''
          const url = arguments['url'];
          return fetch(url, {
            credentials: 'include',
            redirect: 'follow',
            cache: 'no-store',
            headers: { 'Accept': 'application/pdf,*/*' }
          }).then(async (r) => {
            const disp = (r.headers && r.headers.get) ? (r.headers.get('content-disposition')||'') : '';
            const ct = (r.headers && r.headers.get) ? (r.headers.get('content-type')||'') : '';
            const blob = await r.blob();
            return await new Promise((resolve, reject) => {
              const reader = new FileReader();
              reader.onloadend = () => resolve({ dataUrl: reader.result, contentDisposition: disp, contentType: ct });
              reader.onerror = () => reject('READ_ERROR');
              reader.readAsDataURL(blob);
            });
          });
        ''',
            arguments: {'url': request.url.toString()},
          )
          .timeout(const Duration(seconds: 15), onTimeout: () => null);

      final value = res?.value;
      if (value is Map) {
        final dataUrl = value['dataUrl']?.toString();
        if (dataUrl is String && dataUrl.startsWith('data:')) {
          final decoded = _decodeDataUrl(dataUrl);
          if (decoded != null) {
            final mime = (value['contentType']?.toString() ?? decoded.mimeType);
            final cd = value['contentDisposition']?.toString();
            return (
              bytes: decoded.bytes,
              mimeType: mime,
              contentDisposition: cd,
            );
          }
        }
      }
      return null;
    } catch (_) {
      return null;
    }
  }

  // Try to reproduce the last submitted FORM (captured via JS) and POST it to fetch the PDF.
  Future<({Uint8List bytes, String? mimeType, String? contentDisposition})?>
  _tryFetchViaCapturedPost(DownloadStartRequest request) async {
    try {
      final js = await _controller?.callAsyncJavaScript(
        functionBody:
            r'''try{return window.__yojnaLastPost||null;}catch(e){return null;}''',
      );
      final value = js?.value;
      if (value is! Map) return null;
      final action = value['action']?.toString() ?? '';
      final body = value['body']?.toString() ?? '';
      if (action.isEmpty || body.isEmpty) return null;

      final uri = Uri.tryParse(action);
      if (uri == null) return null;

      // 1) Try in-page JS fetch with POST to preserve exact session context.
      try {
        final res = await _controller
            ?.callAsyncJavaScript(
              functionBody: '''
            const u = arguments['url'];
            const b = arguments['body'];
            return fetch(u, {
              method: 'POST',
              credentials: 'include',
              redirect: 'follow',
              cache: 'no-store',
              headers: { 'Accept': 'application/pdf,*/*', 'Content-Type': 'application/x-www-form-urlencoded' },
              body: b
            })
            .then(r => r.blob())
            .then(blob => new Promise((resolve, reject) => {
              const reader = new FileReader();
              reader.onloadend = () => resolve(reader.result);
              reader.onerror = () => reject('READ_ERROR');
              reader.readAsDataURL(blob);
            }));
          ''',
              arguments: {'url': action, 'body': body},
            )
            .timeout(const Duration(seconds: 20), onTimeout: () => null);
        final jsVal = res?.value;
        if (jsVal is String && jsVal.startsWith('data:')) {
          final decoded = _decodeDataUrl(jsVal);
          if (decoded != null) {
            return (
              bytes: decoded.bytes,
              mimeType: decoded.mimeType,
              contentDisposition: null,
            );
          }
        }
      } catch (_) {}

      // 2) Fallback to native Dart HttpClient POST with cookies/headers.
      final client = HttpClient();
      client.badCertificateCallback = (c, h, p) => true;
      final httpReq = await client.openUrl('POST', uri);
      httpReq.followRedirects = true;
      final requestedName = _resolveFileName(request);
      final requestedExt = _resolveExtension(requestedName, request.mimeType);
      final bool expectPdfHeader =
          (request.mimeType?.toLowerCase().contains('pdf') ?? false) ||
          requestedExt.toLowerCase() == 'pdf';
      httpReq.headers.set(
        HttpHeaders.acceptHeader,
        expectPdfHeader
            ? 'application/pdf,application/octet-stream,*/*;q=0.9'
            : 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
      );
      httpReq.headers.set(
        HttpHeaders.contentTypeHeader,
        'application/x-www-form-urlencoded',
      );
      httpReq.headers.set(
        'Accept-Language',
        'hi-IN,hi;q=0.9,en-US;q=0.8,en;q=0.7',
      );
      httpReq.headers.set('Upgrade-Insecure-Requests', '1');
      httpReq.headers.set('Cache-Control', 'no-cache');
      httpReq.headers.set('Pragma', 'no-cache');
      httpReq.headers.set('Sec-Fetch-Site', 'same-origin');
      httpReq.headers.set('Sec-Fetch-Mode', 'navigate');
      httpReq.headers.set('Sec-Fetch-Dest', 'document');
      httpReq.headers.set('Origin', '${uri.scheme}://${uri.host}');
      final userAgent =
          await _controller?.getSettings().then((s) => s?.userAgent) ?? '';
      if (userAgent.isNotEmpty) {
        httpReq.headers.set(HttpHeaders.userAgentHeader, userAgent);
      }
      final referer = _currentUrl?.isNotEmpty == true
          ? _currentUrl!
          : widget.initialUrl;
      if (referer.isNotEmpty) {
        httpReq.headers.set('Referer', referer);
      }
      try {
        final cookies = await CookieManager.instance().getCookies(
          url: WebUri(uri.toString()),
        );
        if (cookies.isNotEmpty) {
          httpReq.headers.set(
            HttpHeaders.cookieHeader,
            cookies.map((c) => '${c.name}=${c.value}').join('; '),
          );
        }
      } catch (_) {}

      httpReq.add(utf8.encode(body));
      final res = await httpReq.close();
      if (res.statusCode >= 400) {
        client.close(force: true);
        return null;
      }
      final bytes = await consolidateHttpClientResponseBytes(res);
      final ct = res.headers.contentType?.mimeType;
      final cd = res.headers.value('content-disposition');
      client.close(force: true);
      return (bytes: bytes, mimeType: ct, contentDisposition: cd);
    } catch (_) {
      return null;
    }
  }

  ({Uint8List bytes, String? mimeType})? _decodeDataUrl(String dataUrl) {
    try {
      final data = UriData.parse(dataUrl);
      final bytes = Uint8List.fromList(data.contentAsBytes());
      final mimeType = data.mimeType.isEmpty ? null : data.mimeType;
      return (bytes: bytes, mimeType: mimeType);
    } catch (_) {
      return null;
    }
  }

  Future<String?> _resolveBlobUrl(String blobUrl) async {
    final controller = _controller;
    if (controller == null) {
      return null;
    }

    try {
      final result = await controller.callAsyncJavaScript(
        functionBody: '''
          const url = arguments['url'];
          return fetch(url)
            .then(response => response.blob())
            .then(blob => new Promise((resolve, reject) => {
              const reader = new FileReader();
              reader.onloadend = () => resolve(reader.result);
              reader.onerror = () => reject('BLOB_READ_ERROR');
              reader.readAsDataURL(blob);
            }));
        ''',
        arguments: {'url': blobUrl},
      );

      final value = result?.value;
      if (value is String && value.startsWith('data:')) {
        return value;
      }
    } catch (e) {
      if (kDebugMode) {
        debugPrint('Failed to resolve blob url: $e');
      }
    }
    return null;
  }

  String _resolveFileName(DownloadStartRequest request) {
    final fromDisposition = _filenameFromContentDisposition(
      request.contentDisposition,
    );
    if (fromDisposition != null && fromDisposition.trim().isNotEmpty) {
      return fromDisposition.trim();
    }

    try {
      final uri = request.url;
      final scheme = uri.scheme.toLowerCase();
      if (scheme == 'blob' || scheme == 'data') {
        return '';
      }
      final parsed = Uri.tryParse(uri.toString());
      final lastSegment = parsed?.pathSegments.isNotEmpty == true
          ? parsed!.pathSegments.last
          : null;
      if (lastSegment != null && lastSegment.trim().isNotEmpty) {
        return Uri.decodeComponent(lastSegment);
      }
    } catch (_) {}

    return '';
  }

  String? _filenameFromContentDisposition(String? contentDisposition) {
    if (contentDisposition == null || contentDisposition.isEmpty) {
      return null;
    }

    final utf8Match = RegExp(
      r'filename\*\s*=\s*([^;]+)',
    ).firstMatch(contentDisposition);
    if (utf8Match != null) {
      final value = utf8Match.group(1) ?? '';
      final split = value.split("''");
      if (split.length == 2) {
        final encoded = split[1].trim().replaceAll('"', '');
        return Uri.decodeComponent(encoded);
      }
      return value.replaceAll('"', '').trim();
    }

    final match = RegExp(
      r'filename\s*=\s*"?([^";]+)',
    ).firstMatch(contentDisposition);
    if (match != null) {
      return match.group(1)?.trim();
    }

    return null;
  }

  String _resolveExtension(String fileName, String? mimeType) {
    final existing = p.extension(fileName).replaceFirst('.', '');
    if (existing.isNotEmpty) {
      return existing;
    }
    final fromMime = _extensionFromMime(mimeType);
    if (fromMime != null) {
      return fromMime;
    }
    return 'bin';
  }

  String? _extensionFromMime(String? mimeType) {
    if (mimeType == null || mimeType.isEmpty) return null;
    final lower = mimeType.toLowerCase();
    switch (lower) {
      case 'application/pdf':
        return 'pdf';
      case 'application/zip':
        return 'zip';
      case 'application/msword':
        return 'doc';
      case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
        return 'docx';
      case 'application/vnd.ms-excel':
        return 'xls';
      case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
        return 'xlsx';
      case 'image/jpeg':
        return 'jpg';
      case 'image/png':
        return 'png';
      case 'image/gif':
        return 'gif';
      case 'text/plain':
        return 'txt';
      case 'application/json':
        return 'json';
      default:
        return null;
    }
  }

  Future<String?> _saveDownloadedBytes(
    Uint8List bytes,
    String fileName,
    String? mimeType,
  ) async {
    final ext = _resolveExtension(fileName, mimeType);
    String name = fileName.trim();
    if (name.isEmpty) name = 'downloaded-file.$ext';
    if (p.extension(name).isEmpty) name = '$name.$ext';
    final finalName = _sanitizeFileName(name);
    final resolvedMime = (mimeType != null && mimeType.isNotEmpty)
        ? mimeType
        : _mimeFromExtension(ext);

    // Prefer native SAF (MediaStore) on Android to ensure files appear in Downloads app
    if (Platform.isAndroid) {
      try {
        final saved = await _downloadChannel
            .invokeMethod<String>('saveToDownloads', {
              'fileName': finalName,
              'mimeType': resolvedMime,
              'bytesBase64': base64Encode(bytes),
            });
        if (saved != null && saved.isNotEmpty) {
          return saved; // may be content:// or absolute path
        }
      } catch (_) {
        // fall through to FileSaver
      }
    }

    try {
      final baseName = p.basenameWithoutExtension(finalName);
      final extOnly = p.extension(finalName).replaceFirst('.', '');
      final isPdf =
          resolvedMime.toLowerCase() == 'application/pdf' ||
          extOnly.toLowerCase() == 'pdf';
      final result = await FileSaver.instance.saveFile(
        name: baseName,
        bytes: bytes,
        fileExtension: extOnly.isEmpty ? 'pdf' : extOnly,
        mimeType: isPdf ? MimeType.pdf : MimeType.other,
        customMimeType: isPdf ? null : resolvedMime,
      );
      if (result.isNotEmpty) return result;
      return 'downloads';
    } catch (_) {
      // Fall back to app specific directory.
    }

    final baseDir =
        await getExternalStorageDirectory() ??
        await getApplicationDocumentsDirectory();
    final folder = Directory(p.join(baseDir.path, 'Online Yojna', 'Downloads'));
    if (!await folder.exists()) {
      await folder.create(recursive: true);
    }
    final path = p.join(folder.path, finalName);
    final file = File(path);
    await file.writeAsBytes(bytes, flush: true);
    return path;
  }

  String _mimeFromExtension(String ext) {
    switch (ext.toLowerCase()) {
      case 'pdf':
        return 'application/pdf';
      case 'zip':
        return 'application/zip';
      case 'jpg':
      case 'jpeg':
        return 'image/jpeg';
      case 'png':
        return 'image/png';
      case 'gif':
        return 'image/gif';
      case 'txt':
        return 'text/plain';
      case 'json':
        return 'application/json';
      case 'doc':
        return 'application/msword';
      case 'docx':
        return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
      case 'xls':
        return 'application/vnd.ms-excel';
      case 'xlsx':
        return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      default:
        return 'application/octet-stream';
    }
  }

  // Removed _resolveDownloadDirectory: SAF/app-specific storage is used instead.

  String _sanitizeFileName(String source) {
    // Keep spaces and unicode (incl. Hindi). Remove only invalid path chars and control chars.
    var s = source.replaceAll(RegExp(r'[\\/:*?"<>|\x00-\x1F]'), '').trim();
    if (s.isEmpty) s = 'yojna-file';
    return s;
  }

  // --- Context helpers ---
  bool _isAbhaHost(String host) {
    final h = host.toLowerCase();
    return h.contains('abha') ||
        h.contains('ndhm') ||
        h.contains('healthid') ||
        h.contains('abdm');
  }

  bool _isRationHost(String host) {
    final h = host.toLowerCase();
    return h.contains('ration') ||
        h.contains('epds') ||
        h.contains('nfsa') ||
        h.contains('food') ||
        h.contains('fcs');
  }

  bool _isAadhaarContext(String? url) {
    if (url == null) return false;
    final u = url.toLowerCase();
    return u.contains('uidai') ||
        u.contains('aadhaar') ||
        u.contains('aadhar') ||
        u.contains('resident.uidai.gov');
  }

  String _overrideFileNameByContext(String name, {String? defaultExt}) {
    final cur = _currentUrl ?? '';
    final ext = p.extension(name).isNotEmpty
        ? p.extension(name)
        : (defaultExt != null && defaultExt.isNotEmpty
              ? '.${defaultExt.replaceAll('.', '')}'
              : '');
    // Per user request:
    // Aadhaar page => "Aadhaar Card"
    // Ration card page => "ABHA Card"
    if (_isAadhaarContext(cur)) {
      return _sanitizeFileName('Aadhaar Card$ext');
    }
    final host = Uri.tryParse(cur)?.host ?? '';
    if (_isRationHost(host)) {
      return _sanitizeFileName('ABHA Card$ext');
    }
    return name;
  }

  Future<void> _logoutAbhaIfNeeded() async {
    final url = _currentUrl;
    if (url == null || url.isEmpty) return;
    final host = Uri.tryParse(url)?.host ?? '';
    if (!_isAbhaHost(host)) return;
    try {
      // Clear web storage
      final c = _controller;
      if (c != null) {
        await c.callAsyncJavaScript(
          functionBody:
              'try{localStorage.clear();sessionStorage.clear();}catch(e){}',
        );
      }
      // Clear cookies (may logout across domains, but ensures session cleared)
      await CookieManager.instance().deleteAllCookies();
    } catch (_) {}
  }

  // Removed _ensureStoragePermission: no direct external storage; using SAF.

  void _showSnack(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }
}

class _ErrorFallback extends StatelessWidget {
  const _ErrorFallback({required this.error, required this.onRetry});

  final SimpleWebError error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final isCertIssue = error.code == -202 || error.description.contains('SSL');
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const Icon(Icons.lock, size: 44),
            const SizedBox(height: 8),
            Text(
              isCertIssue
                  ? 'सुरक्षा प्रमाणपत्र की समस्या'
                  : 'पेज लोड नहीं हो पाया',
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 6),
            Text(
              'कोड: ${error.code} — ${error.description}',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            Wrap(
              alignment: WrapAlignment.center,
              spacing: 8,
              children: [
                FilledButton.icon(
                  onPressed: onRetry,
                  icon: const Icon(Icons.refresh),
                  label: const Text('पुनः प्रयास'),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              'यदि समस्या बनी रहे, Android System WebView/Chrome अपडेट करें और डिवाइस की तारीख/समय जाँचें।',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: Theme.of(context).colorScheme.outline,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
