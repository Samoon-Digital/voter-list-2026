import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:permission_handler/permission_handler.dart';

import 'dart:io';
import 'dart:convert';
import 'package:path_provider/path_provider.dart';
import 'package:yojna_plus/screens/pdf_viewer_screen.dart';

class WebViewScreen extends StatefulWidget {
  final String url;
  final String title;

  const WebViewScreen({super.key, required this.url, required this.title});

  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen> {
  InAppWebViewController? webViewController;
  double progress = 0;
  bool _canGoBack = false;
  static const _channel = MethodChannel(
    'com.samoondigital.yojnaplus/downloads',
  );
  bool _isLoadingPdf = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        leading: _canGoBack
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () {
                  webViewController?.goBack();
                },
              )
            : const BackButton(),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              webViewController?.reload();
            },
          ),
        ],
      ),
      body: Stack(
        children: [
          InAppWebView(
            initialUrlRequest: URLRequest(url: WebUri(widget.url)),
            initialSettings: InAppWebViewSettings(
              useShouldOverrideUrlLoading: true,
              mediaPlaybackRequiresUserGesture: false,
              useHybridComposition: true,
              allowsInlineMediaPlayback: true,
              javaScriptEnabled: true,
              useOnDownloadStart: true,
              allowsBackForwardNavigationGestures: true,
              supportMultipleWindows: widget.url.contains('telangana'),
              javaScriptCanOpenWindowsAutomatically: widget.url.contains(
                'telangana',
              ),
            ),
            onReceivedServerTrustAuthRequest: (controller, challenge) async {
              if (challenge.protectionSpace.host.contains('meghalaya') ||
                  challenge.protectionSpace.host.contains('nic.in')) {
                return ServerTrustAuthResponse(
                  action: ServerTrustAuthResponseAction.PROCEED,
                );
              }
              return ServerTrustAuthResponse(
                action: ServerTrustAuthResponseAction.CANCEL,
              );
            },
            onCreateWindow: (controller, createWindowRequest) async {
              showDialog(
                context: context,
                builder: (context) {
                  return Dialog(
                    insetPadding: const EdgeInsets.all(16),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: SizedBox(
                      width: double.infinity,
                      height: 500,
                      child: Column(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 12,
                            ),
                            decoration: BoxDecoration(
                              color: Theme.of(context).primaryColor,
                              borderRadius: const BorderRadius.only(
                                topLeft: Radius.circular(16),
                                topRight: Radius.circular(16),
                              ),
                            ),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                const Text(
                                  "Verification",
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                IconButton(
                                  icon: const Icon(
                                    Icons.close,
                                    color: Colors.white,
                                  ),
                                  onPressed: () {
                                    Navigator.pop(context);
                                  },
                                ),
                              ],
                            ),
                          ),
                          Expanded(
                            child: InAppWebView(
                              windowId: createWindowRequest.windowId,
                              initialSettings: InAppWebViewSettings(
                                javaScriptEnabled: true,
                                useOnDownloadStart: true,
                              ),
                              onWebViewCreated: (controller) {
                                // Re-register handlers for the popup window
                                controller.addJavaScriptHandler(
                                  handlerName: 'blobDownload',
                                  callback: (args) async {
                                    if (args.isNotEmpty) {
                                      final String base64Data = args[0];
                                      final String fileName = args.length > 1
                                          ? args[1]
                                          : 'document.pdf';
                                      await _handleBase64Download(
                                        base64Data,
                                        fileName,
                                      );
                                      if (mounted) Navigator.pop(context);
                                    }
                                  },
                                );
                                controller.addJavaScriptHandler(
                                  handlerName: 'downloadPdfUrl',
                                  callback: (args) async {
                                    if (args.isNotEmpty) {
                                      final url = args[0] as String;
                                      debugPrint(
                                        "DEBUG: Received PDF URL from JS: $url",
                                      );
                                      _downloadAndOpenPdf(url);
                                      if (mounted) Navigator.pop(context);
                                    }
                                  },
                                );
                              },
                              onLoadStop: (controller, url) async {
                                debugPrint("Popup onLoadStop: $url");
                                // Inject generic script or specific scripts if needed in popup
                                if (url != null &&
                                    url.host.contains('ceodelhi.gov.in')) {
                                  await _injectDelhiScript(controller);
                                } else if (url != null &&
                                    url.host.contains('telangana.gov.in')) {
                                  await _injectTelanganaScript(controller);
                                }
                              },
                              onConsoleMessage: (controller, consoleMessage) {
                                debugPrint(
                                  "POPUP_JS: ${consoleMessage.message}",
                                );
                              },
                              onDownloadStartRequest:
                                  (controller, downloadRequest) async {
                                    final url = downloadRequest.url.toString();
                                    debugPrint("Popup Download Request: $url");

                                    if (url.startsWith('blob:')) {
                                      await _handleBlobUrl(
                                        controller,
                                        url,
                                        downloadRequest.suggestedFilename,
                                      );
                                      if (mounted) Navigator.pop(context);
                                      return;
                                    }

                                    final isPdf =
                                        (downloadRequest.mimeType
                                                ?.toLowerCase() ==
                                            'application/pdf') ||
                                        url.toLowerCase().endsWith('.pdf');

                                    if (isPdf) {
                                      await _downloadAndOpenPdf(
                                        url,
                                        userAgent: downloadRequest.userAgent,
                                      );
                                    } else {
                                      await _downloadFile(
                                        url,
                                        downloadRequest.suggestedFilename,
                                        downloadRequest.mimeType,
                                        downloadRequest.userAgent,
                                        downloadRequest.contentDisposition,
                                      );
                                    }
                                    if (mounted) Navigator.pop(context);
                                  },
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              );
              return true;
            },
            onWebViewCreated: (controller) {
              webViewController = controller;
              // Register handler for Base64 download
              controller.addJavaScriptHandler(
                handlerName: 'blobDownload',
                callback: (args) async {
                  if (args.isNotEmpty) {
                    final String base64Data = args[0];
                    final String fileName = args.length > 1
                        ? args[1]
                        : 'document.pdf';
                    await _handleBase64Download(base64Data, fileName);
                  }
                },
              );
              controller.addJavaScriptHandler(
                handlerName: 'downloadPdfUrl',
                callback: (args) async {
                  if (args.isNotEmpty) {
                    final url = args[0] as String;
                    debugPrint("DEBUG: Received PDF URL from JS: $url");
                    _downloadAndOpenPdf(url);
                  }
                },
              );
            },
            onProgressChanged: (controller, p) {
              setState(() {
                progress = p / 100;
              });
            },
            onLoadStop: (controller, url) async {
              final canBack = await controller.canGoBack();
              setState(() {
                _canGoBack = canBack;
              });

              // Inject script for Jharkhand PDF page
              if (url != null &&
                  url.host.contains('ceojh.jharkhand.gov.in') &&
                  url.path.contains('aceng.aspx')) {
                await _injectJharkhandScript(controller);
              } else if (url != null &&
                  url.host.contains('elections.punjab.gov.in')) {
                await _injectPunjabScript(controller);
              } else if (url != null &&
                  url.host.contains('ceo.nagaland.gov.in')) {
                await _injectNagalandScript(controller);
              } else if (url != null && url.host.contains('ceosikkim.nic.in')) {
                debugPrint("DEBUG: Sikkim URL Detected: $url");
                await _injectSikkimScript(controller);
              } else if (url != null && url.host.contains('ceodelhi.gov.in')) {
                await _injectDelhiScript(controller);
              } else if (url != null &&
                  url.host.contains('erms.tripura.gov.in')) {
                await _injectTripuraScript(controller);
              }
            },
            onConsoleMessage: (controller, consoleMessage) {
              debugPrint("JS_LOG: ${consoleMessage.message}");
            },
            onDownloadStartRequest: (controller, downloadRequest) async {
              final url = downloadRequest.url.toString();

              if (url.startsWith('blob:')) {
                await _handleBlobUrl(
                  controller,
                  url,
                  downloadRequest.suggestedFilename,
                );
                return;
              }

              final isPdf =
                  (downloadRequest.mimeType?.toLowerCase() ==
                      'application/pdf') ||
                  url.toLowerCase().endsWith('.pdf');

              if (isPdf) {
                await _downloadAndOpenPdf(
                  url,
                  userAgent: downloadRequest.userAgent,
                );
              } else {
                await _downloadFile(
                  url,
                  downloadRequest.suggestedFilename,
                  downloadRequest.mimeType,
                  downloadRequest.userAgent,
                  downloadRequest.contentDisposition,
                );
              }
            },
            shouldOverrideUrlLoading: (controller, navigationAction) async {
              final uri = navigationAction.request.url!;
              final urlString = uri.toString().toLowerCase();

              // Intercept PDF links
              if (urlString.endsWith('.pdf')) {
                _downloadAndOpenPdf(uri.toString());
                return NavigationActionPolicy.CANCEL;
              }

              return NavigationActionPolicy.ALLOW;
            },
          ),
          if (progress < 1.0) LinearProgressIndicator(value: progress),
          if (_isLoadingPdf)
            Container(
              color: Colors.black54,
              child: const Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    CircularProgressIndicator(),
                    SizedBox(height: 16),
                    Text(
                      'Pdf Opening site is slow please wait until full pdf load',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }

  Future<void> _downloadAndOpenPdf(String url, {String? userAgent}) async {
    setState(() {
      _isLoadingPdf = true;
    });

    try {
      // Get Cookies and other headers
      final cookieManager = CookieManager.instance();
      final cookies = await cookieManager.getCookies(url: WebUri(url));
      final cookieHeader = cookies
          .map((c) => '${c.name}=${c.value}')
          .join('; ');

      final client = HttpClient()
        ..badCertificateCallback = ((cert, host, port) => true);

      final request = await client.getUrl(Uri.parse(url));

      if (cookieHeader.isNotEmpty) {
        request.headers.add('Cookie', cookieHeader);
      }
      if (userAgent != null) {
        request.headers.add('User-Agent', userAgent);
      }
      // Add Referer as it is often needed
      request.headers.add('Referer', url);

      final response = await request.close();

      if (response.statusCode == 200) {
        // Validate Content-Type
        final contentType =
            response.headers.value('content-type')?.toLowerCase() ?? '';
        if (contentType.contains('text/html')) {
          throw Exception(
            "Server returned HTML instead of PDF. Session might be expired or invalid.",
          );
        }

        final dir = await getApplicationDocumentsDirectory();
        // Try to guess extension if missing
        var filename = url.split('/').last;
        // Clean filename of query params
        if (filename.contains('?')) {
          filename = filename.split('?').first;
        }
        if (!filename.toLowerCase().endsWith('.pdf')) {
          filename = '$filename.pdf';
        }

        final file = File('${dir.path}/$filename');

        // Read response bytes
        final bytes = await response.fold<List<int>>(
          <int>[],
          (List<int> previous, List<int> element) => previous..addAll(element),
        );

        await file.writeAsBytes(bytes);

        if (mounted) {
          setState(() {
            _isLoadingPdf = false;
          });
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) =>
                  PdfViewerScreen(path: file.path, title: filename),
            ),
          );
        }
      } else {
        throw Exception('Failed to download PDF: ${response.statusCode}');
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoadingPdf = false;
        });
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Error opening PDF: $e')));
      }
    }
  }

  Future<void> _downloadFile(
    String url,
    String? filename,
    String? mimeType,
    String? userAgent,
    String? contentDisposition,
  ) async {
    try {
      // Check if native side says we need permission (Android < 10)
      final bool needsPermission =
          await _channel.invokeMethod('needsStoragePermission') ?? true;

      if (needsPermission) {
        var status = await Permission.storage.status;
        if (!status.isGranted) {
          status = await Permission.storage.request();
          if (!status.isGranted) {
            if (mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Storage permission denied')),
              );
            }
            return;
          }
        }
      }

      final fName =
          filename ?? 'download_${DateTime.now().millisecondsSinceEpoch}';

      // Fetch cookies for the native download manager
      final cookieManager = CookieManager.instance();
      final cookies = await cookieManager.getCookies(url: WebUri(url));
      final cookieHeader = cookies
          .map((c) => '${c.name}=${c.value}')
          .join('; ');

      final headers = <String, String>{};
      if (userAgent != null) headers['User-Agent'] = userAgent;
      if (cookieHeader.isNotEmpty) headers['Cookie'] = cookieHeader;
      headers['Referer'] = url;

      await _channel.invokeMethod('enqueueDownload', {
        'url': url,
        'fileName': fName,
        'mimeType': mimeType,
        'description': 'Downloading file...',
        'contentDisposition': contentDisposition,
        'headers': headers,
      });

      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Downloading $fName...')));
      }
    } catch (e) {
      debugPrint('Download error: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to start download')),
        );
      }
    }
  }

  Future<void> _handleBlobUrl(
    InAppWebViewController controller,
    String blobUrl,
    String? suggestedFilename,
  ) async {
    final String fileName = suggestedFilename ?? 'document.pdf';
    final script =
        '''
      (async function() {
        try {
          const response = await fetch('$blobUrl');
          const blob = await response.blob();
          const reader = new FileReader();
          reader.onloadend = function() {
            const base64data = reader.result;
            window.flutter_inappwebview.callHandler('blobDownload', base64data, '$fileName');
          };
          reader.readAsDataURL(blob);
        } catch (error) {
          console.error('Blob fetch error:', error);
        }
      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectJharkhandScript(InAppWebViewController controller) async {
    // This script hijacks the form submission
    final script = '''
      (function() {
        var form = document.forms[0];
        if (!form) return;

        // Ensure we capture the specific submit button click
        var submitButtons = form.querySelectorAll('input[type=submit]');
        for (var i = 0; i < submitButtons.length; i++) {
            submitButtons[i].onclick = function(e) {
                // Store the clicked button's name and value
                form.__clickedButtonName = this.name;
                form.__clickedButtonValue = this.value;
            };
        }

        // Override the submit behavior
        form.onsubmit = function(e) {
            e.preventDefault();
            
            var formData = new FormData(form);
            
            // Append the clicked button if available (crucial for ASP.NET)
            if (form.__clickedButtonName) {
                formData.append(form.__clickedButtonName, form.__clickedButtonValue);
            } else {
                 // Fallback: If "Ok" button exists but wasn't explicitly clicked (e.g. Enter key), try to find it
                 var okBtn = document.getElementById('Button3');
                 if(okBtn) {
                     formData.append(okBtn.name, okBtn.value);
                 }
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', form.action, true);
            xhr.responseType = 'blob';
            
            // Mimic standard browser headers
            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0'); // Prevent caching

            xhr.onload = function () {
                if (this.status === 200) {
                    var blob = this.response;
                    
                    // Verify if it is actually a PDF by checking the header (Magic Bytes)
                    // We cannot rely solely on Content-Type as specific servers might send text/html for pdfs
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        if (header === "%PDF") {
                             // It IS a PDF, proceed to download
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Jharkhand_Voter_List_2003.pdf');
                            }
                        } else {
                            // Not a PDF (likely HTML error page), display it
                            var reader = new FileReader();
                            reader.onload = function() {
                               document.open();
                               document.write(reader.result);
                               document.close();
                            };
                            reader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     // Error handling
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.send(formData);
        };
      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectPunjabScript(InAppWebViewController controller) async {
    final script = '''
      (function() {
        var form = document.querySelector('form');
        if (!form) return;

        // Capture submit button clicks to send the correct value
        var submitButtons = form.querySelectorAll('input[type=submit]');
        for (var i = 0; i < submitButtons.length; i++) {
            submitButtons[i].onclick = function(e) {
                form.__clickedButtonName = this.name;
                form.__clickedButtonValue = this.value;
                form.__clickedButtonId = this.id;
            };
        }

        form.onsubmit = function(e) {
            e.preventDefault();
            
            var formData = new FormData(form);
            
            // Append the clicked button. For Punjab, it's usually "btnSubmit" with different values.
            if (form.__clickedButtonName) {
                formData.append(form.__clickedButtonName, form.__clickedButtonValue);
            } else {
                 // Fallback: Default to "Download Elector List" (btnPart)
                 var btnPart = document.getElementById('btnPart');
                 if(btnPart) {
                     formData.append(btnPart.name, btnPart.value);
                 }
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', form.action, true);
            xhr.responseType = 'blob';
            
            // Standard headers
            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0');

            xhr.onload = function () {
                if (this.status === 200) {
                    var blob = this.response;
                    
                    // Magic Byte Check (%PDF)
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        if (header === "%PDF") {
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Punjab_Voter_List_2003.pdf');
                            }
                        } else {
                            // Likely HTML error
                            var reader = new FileReader();
                            reader.onload = function() {
                               document.open();
                               document.write(reader.result);
                               document.close();
                            };
                            reader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.send(formData);
        };
      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectNagalandScript(InAppWebViewController controller) async {
    final script = '''
      (function() {
        console.log("Nagaland Script: Injected V4 - Robust");
        var form = document.forms[0];
        if (!form) {
            console.log("Nagaland Script: No form found");
            return;
        }

        // Capture generic clicks and INTERCEPT THEM
        document.addEventListener('click', function(e) {
            var target = e.target;
            // Handle clicks on images inside buttons or labels
            while (target && target !== document) {
                if (target.type == 'submit' || target.type == 'image' || target.tagName == 'BUTTON' || (target.tagName === 'INPUT' && target.type === 'submit')) {
                     console.log("Nagaland Script: Click intercepted on " + target.tagName);
                     
                     // 1. Extract ASP.NET params if present
                     var onclick = target.getAttribute('onclick');
                     var eventTarget = target.name || ''; 
                     var eventArgument = '';
                     
                     if (onclick && onclick.includes('__doPostBack')) {
                         var matches = onclick.match(/__doPostBack\\(['"](.*?)['"],\\s*['"](.*?)['"]\\)/);
                         if (matches) {
                             eventTarget = matches[1];
                             eventArgument = matches[2];
                             console.log("Nagaland Script: Extracted __doPostBack args: " + eventTarget);
                         }
                     }
                     
                     // 2. Set global vars for interceptSubmit to use
                     window.__EVENTTARGET = eventTarget;
                     window.__EVENTARGUMENT = eventArgument;
                     
                     form.__clickedButtonName = target.name;
                     form.__clickedButtonValue = target.value;
                     
                     // 3. Stop everything else to prevent the page's broken JS from crashing
                     e.preventDefault();
                     e.stopImmediatePropagation();
                     
                     // 4. Submit
                     interceptSubmit(form);
                     return;
                }
                target = target.parentNode;
            }
        }, true);

        function interceptSubmit(formElement) {
            console.log("Nagaland Script: Intercepting submit");
            var formData = new FormData(formElement);
            
            if (formElement.__clickedButtonName) {
                formData.append(formElement.__clickedButtonName, formElement.__clickedButtonValue);
            }

            // ASP.NET Event validation
            if (window.__EVENTTARGET) {
                 formData.append('__EVENTTARGET', window.__EVENTTARGET);
                 formData.append('__EVENTARGUMENT', window.__EVENTARGUMENT || '');
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', formElement.action || window.location.href, true);
            xhr.responseType = 'blob'; // Important for PDF
            
            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0'); 

            xhr.onload = function () {
                if (this.status === 200) {
                    var blob = this.response;
                    
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        console.log("Nagaland Script: Header " + header);
                        if (header === "%PDF") {
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Nagaland_Voter_List.pdf');
                            }
                        } else {
                            // It's HTML (likely error or next page), render it
                            var reader = new FileReader();
                            reader.onload = function() {
                               document.open();
                               document.write(reader.result);
                               document.close();
                            };
                            reader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     console.log("Nagaland Script: Download failed " + this.status);
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.onerror = function() {
                console.log("Nagaland Script: Network Error");
                alert('Network Error during download');
            };
            xhr.send(formData);
        }

        // Keeping other hooks just in case
        window.addEventListener('submit', function(e) {
            console.log("Nagaland Script: Submit Captured (Backup)");
            e.preventDefault();
            e.stopPropagation();
            interceptSubmit(e.target);
        }, true);
        
        // Hook programmatic submit
        HTMLFormElement.prototype.submit = function() {
             console.log("Nagaland Script: Programmatic submit");
             interceptSubmit(this);
        };

        // Hook __doPostBack
        if (typeof __doPostBack === 'function') {
             __doPostBack = function(eventTarget, eventArgument) {
                 console.log("Nagaland Script: __doPostBack hook");
                 window.__EVENTTARGET = eventTarget;
                 window.__EVENTARGUMENT = eventArgument;
                 form.__clickedButtonName = null;
                 interceptSubmit(form); 
             };
        }
      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectTripuraScript(InAppWebViewController controller) async {
    final script = '''
      (function() {
        console.log("Tripura Script: Injected V4 - ContentType Fix");
        var form = document.forms[0];
        if (!form) {
            console.log("Tripura Script: No form found");
            return;
        }

        // Helper: Set or create hidden input
        function setHiddenInput(name, value) {
            var input = form.querySelector('input[name="' + name + '"]');
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = name;
                form.appendChild(input);
            }
            input.value = value;
        }

        document.addEventListener('click', function(e) {
            var target = e.target;
            while (target && target !== document) {
                if (target.type == 'submit' || target.type == 'image' || target.tagName == 'BUTTON' || (target.tagName === 'INPUT' && target.type === 'submit')) {
                     console.log("Tripura Script: Click intercepted on " + target.tagName);
                     
                     var onclick = target.getAttribute('onclick');
                     var eventTarget = target.name || ''; 
                     var eventArgument = '';
                     
                     if (onclick && onclick.includes('__doPostBack')) {
                         var matches = onclick.match(/__doPostBack\\(['"](.*?)['"],\\s*['"](.*?)['"]\\)/);
                         if (matches) {
                             eventTarget = matches[1];
                             eventArgument = matches[2];
                         }
                     }
                     
                     window.__EVENTTARGET = eventTarget;
                     window.__EVENTARGUMENT = eventArgument;
                     
                     form.__clickedButtonName = target.name;
                     form.__clickedButtonValue = target.value;
                     
                     e.preventDefault();
                     e.stopImmediatePropagation();
                     
                     interceptSubmit(form);
                     return;
                }
                target = target.parentNode;
            }
        }, true);

        function interceptSubmit(formElement) {
            console.log("Tripura Script: Intercepting submit");
            
            // 1. Prepare DOM state
            if (window.__EVENTTARGET) setHiddenInput('__EVENTTARGET', window.__EVENTTARGET);
            if (window.__EVENTARGUMENT) setHiddenInput('__EVENTARGUMENT', window.__EVENTARGUMENT);
            
            var formData = new FormData(formElement);
            if (formElement.__clickedButtonName) {
                formData.append(formElement.__clickedButtonName, formElement.__clickedButtonValue);
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', formElement.action || window.location.href, true);
            xhr.responseType = 'blob'; 
            
            // CRITICAL FIX: ASP.NET usually expects application/x-www-form-urlencoded
            // FormData sends multipart/form-data. We must respect the form's enctype or default.
            var isMultipart = formElement.enctype && formElement.enctype.toLowerCase() === 'multipart/form-data';
            
            if (!isMultipart) {
                 console.log("Tripura Script: Sending as application/x-www-form-urlencoded");
                 xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                 
                 // Convert FormData to URLSearchParams
                 var params = new URLSearchParams();
                 for (var pair of formData.entries()) {
                     params.append(pair[0], pair[1]);
                 }
                 xhr.send(params);
            } else {
                 console.log("Tripura Script: Sending as multipart/form-data");
                 xhr.send(formData);
            }

            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0'); 

            xhr.onload = function () {
                if (this.status === 200) {
                    var blob = this.response;
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        console.log("Tripura Script: Header " + header);
                        if (header === "%PDF") {
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Tripura_Voter_List_2005.pdf');
                            }
                        } else {
                            var reader = new FileReader();
                            reader.onload = function() {
                               document.open();
                               document.write(reader.result);
                               document.close();
                            };
                            reader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.onerror = function() {
                alert('Network Error during download');
            };
        }

        window.addEventListener('submit', function(e) {
            e.preventDefault();
            e.stopPropagation();
            interceptSubmit(e.target);
        }, true);
        
        HTMLFormElement.prototype.submit = function() {
             interceptSubmit(this);
        };

        if (typeof __doPostBack === 'function') {
             __doPostBack = function(eventTarget, eventArgument) {
                 window.__EVENTTARGET = eventTarget;
                 window.__EVENTARGUMENT = eventArgument;
                 form.__clickedButtonName = null;
                 interceptSubmit(form);
             };
        }
      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectSikkimScript(InAppWebViewController controller) async {
    final script = '''
      (function() {
        console.log("Sikkim Script: Injected V2");
        var form = document.querySelector('form');
        if (!form) {
            console.log("Sikkim Script: No form found");
            return;
        }

        // Use Capture Phase to ensure we get the event first
        window.addEventListener('click', function(e) {
             var target = e.target;
             if(target.tagName === 'INPUT' && (target.type === 'submit' || target.type === 'image')) {
                 console.log("Sikkim Script: Click captured on " + target.name);
                 form.__clickedButtonName = target.name;
                 form.__clickedButtonValue = target.value;
             }
        }, true);

        function interceptSubmit(formElement) {
            console.log("Sikkim Script: Intercepting submit");
             var formData = new FormData(formElement);
            
            if (formElement.__clickedButtonName) {
                console.log("Sikkim Script: Appending clicked button: " + formElement.__clickedButtonName);
                formData.append(formElement.__clickedButtonName, formElement.__clickedButtonValue);
            }

            // ASP.NET PostBack check
            if (!formElement.__clickedButtonName && window.__EVENTTARGET) {
                 console.log("Sikkim Script: Appending event target: " + window.__EVENTTARGET);
                 formData.append('__EVENTTARGET', window.__EVENTTARGET);
                 formData.append('__EVENTARGUMENT', window.__EVENTARGUMENT || '');
            }

            // Log all FormData keys for debugging
            for (var pair of formData.entries()) {
                console.log("Sikkim Form Data: " + pair[0] + ', ' + pair[1]); 
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', formElement.action || window.location.href, true);
            xhr.responseType = 'blob'; // Important
            
            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0');

            xhr.onload = function () {
                console.log("Sikkim Script: Response received, status: " + this.status);
                if (this.status === 200) {
                    var blob = this.response;
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        console.log("Sikkim Script: Header bytes: " + header);

                        if (header === "%PDF") {
                            console.log("Sikkim Script: PDF detected. Sending to Flutter.");
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Sikkim_Voter_List_2002.pdf');
                            }
                        } else {
                             // Check if it's JSON
                             var textReader = new FileReader();
                             textReader.onload = function() {
                                 var text = textReader.result;
                                 try {
                                     var json = JSON.parse(text);
                                     if (json.success && json.url) {
                                         console.log("Sikkim Script: JSON OK, URL: " + json.url);
                                         // Extract PDFUrl param
                                         var urlObj = new URL(json.url, window.location.href);
                                         var pdfUrl = urlObj.searchParams.get("PDFUrl");
                                         if (pdfUrl) {
                                              console.log("Sikkim Script: Found PDF Url: " + pdfUrl);
                                              window.flutter_inappwebview.callHandler('downloadPdfUrl', pdfUrl);
                                              return;
                                         }
                                     }
                                 } catch(e) {
                                     console.log("Sikkim Script: Not JSON");
                                 }

                                 console.log("Sikkim Script: Not a PDF or Valid JSON. Rendering HTML.");
                                 document.open();
                                 document.write(text);
                                 document.close();
                             };
                             textReader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.onerror = function() {
                console.log("Sikkim Script: XHR Error");
            };
            xhr.send(formData);
        }

        // 1. Hook normal submit event with CAPTURE phase
        window.addEventListener('submit', function(e) {
            console.log("Sikkim Script: submit event captured");
            e.preventDefault();
            e.stopPropagation(); // Stop others from interfering
            interceptSubmit(e.target);
        }, true);

        // 2. Hook programmatic submit
        var originalSubmit = HTMLFormElement.prototype.submit;
        HTMLFormElement.prototype.submit = function() {
             console.log("Sikkim Script: form.submit() called");
             interceptSubmit(this);
        };

        // 3. Hook __doPostBack specifically if it exists
        if (typeof __doPostBack === 'function') {
             console.log("Sikkim Script: Hooking __doPostBack");
             var oldPostBack = __doPostBack;
             __doPostBack = function(eventTarget, eventArgument) {
                 console.log("Sikkim Script: __doPostBack called with " + eventTarget);
                 // We need to bypass the onsubmit check if we use submit() programmatically
                 // But we want to trigger *our* interceptor.
                 window.__EVENTTARGET = eventTarget;
                 window.__EVENTARGUMENT = eventArgument;
                 
                 // Manually call our interceptor instead of form.submit() to avoid loops or misses?
                 // But form.submit() should hit our hook.
                 form.__clickedButtonName = null;
                 interceptSubmit(form); 
             };
        }
      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectDelhiScript(InAppWebViewController controller) async {
    final script = '''
      (function() {
        console.log("Delhi Script: Injected V2");
        var form = document.forms[0];
        if (!form) return;

        // Capture submit button clicks
        var submitButtons = form.querySelectorAll('input[type=submit]');
        for (var i = 0; i < submitButtons.length; i++) {
            submitButtons[i].onclick = function(e) {
                form.__clickedButtonName = this.name;
                form.__clickedButtonValue = this.value;
            };
        }

        function interceptSubmit(formElement) {
            console.log("Delhi Script: Intercepting submit");
            var formData = new FormData(formElement);
            
            if (formElement.__clickedButtonName) {
                formData.append(formElement.__clickedButtonName, formElement.__clickedButtonValue);
            }

            // ASP.NET Event validation
            if (window.__EVENTTARGET) {
                 formData.append('__EVENTTARGET', window.__EVENTTARGET);
                 formData.append('__EVENTARGUMENT', window.__EVENTARGUMENT || '');
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', formElement.action || window.location.href, true);
            xhr.responseType = 'blob';
            
            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0'); 

            xhr.onload = function () {
                if (this.status === 200) {
                    var blob = this.response;
                    
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        if (header === "%PDF") {
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Delhi_Voter_List_2002.pdf');
                            }
                        } else {
                            var reader = new FileReader();
                            reader.onload = function() {
                               document.open();
                               document.write(reader.result);
                               document.close();
                            };
                            reader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.onerror = function() {
                alert('Network Error during download');
            };
            xhr.send(formData);
        }

        // Hook submit event with capture
        window.addEventListener('submit', function(e) {
            console.log("Delhi Script: Submit Captured");
            e.preventDefault();
            e.stopPropagation();
            interceptSubmit(e.target);
        }, true);

        // Hook programmatic submit
        var originalSubmit = HTMLFormElement.prototype.submit;
        HTMLFormElement.prototype.submit = function() {
             console.log("Delhi Script: Programmatic submit");
             interceptSubmit(this);
        };

        // Hook __doPostBack
        if (typeof __doPostBack === 'function') {
             var oldPostBack = __doPostBack;
             __doPostBack = function(eventTarget, eventArgument) {
                 console.log("Delhi Script: __doPostBack");
                 window.__EVENTTARGET = eventTarget;
                 window.__EVENTARGUMENT = eventArgument;
                 form.__clickedButtonName = null;
                 interceptSubmit(form); 
             };
        }

      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _injectTelanganaScript(InAppWebViewController controller) async {
    final script = '''
      (function() {
        console.log("Telangana Script: Injected V4 - Aggressive Intercept");
        var form = document.forms[0];
        if (!form) {
            console.log("Telangana Script: No form found");
            return;
        }

        // Capture generic clicks and INTERCEPT THEM
        document.addEventListener('click', function(e) {
            var target = e.target;
            // Handle clicks on images inside buttons or labels
            while (target && target !== document) {
                if (target.type == 'submit' || target.type == 'image' || target.tagName == 'BUTTON' || (target.tagName === 'INPUT' && target.type === 'submit')) {
                     console.log("Telangana Script: Click intercepted on " + target.tagName);
                     
                     // 1. Extract ASP.NET params if present
                     var onclick = target.getAttribute('onclick');
                     var eventTarget = target.name || ''; 
                     var eventArgument = '';
                     
                     if (onclick && onclick.includes('__doPostBack')) {
                         var matches = onclick.match(/__doPostBack\\(['"](.*?)['"],\\s*['"](.*?)['"]\\)/);
                         if (matches) {
                             eventTarget = matches[1];
                             eventArgument = matches[2];
                             console.log("Telangana Script: Extracted __doPostBack args: " + eventTarget);
                         }
                     }
                     
                     // 2. Set global vars for interceptSubmit to use
                     window.__EVENTTARGET = eventTarget;
                     window.__EVENTARGUMENT = eventArgument;
                     
                     form.__clickedButtonName = target.name;
                     form.__clickedButtonValue = target.value;
                     
                     // 3. Stop everything else to prevent the page's broken JS from crashing
                     e.preventDefault();
                     e.stopImmediatePropagation();
                     
                     // 4. Submit
                     interceptSubmit(form);
                     return;
                }
                target = target.parentNode;
            }
        }, true);

        function interceptSubmit(formElement) {
            console.log("Telangana Script: Intercepting submit");
            var formData = new FormData(formElement);
            
            if (formElement.__clickedButtonName) {
                formData.append(formElement.__clickedButtonName, formElement.__clickedButtonValue);
            }

            // ASP.NET Event validation
            if (window.__EVENTTARGET) {
                 formData.append('__EVENTTARGET', window.__EVENTTARGET);
                 formData.append('__EVENTARGUMENT', window.__EVENTARGUMENT || '');
            }

            var xhr = new XMLHttpRequest();
            xhr.open('POST', formElement.action || window.location.href, true);
            xhr.responseType = 'blob'; // Important for PDF
            
            xhr.setRequestHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7');
            xhr.setRequestHeader('Upgrade-Insecure-Requests', '1');
            xhr.setRequestHeader('Cache-Control', 'max-age=0'); 

            xhr.onload = function () {
                if (this.status === 200) {
                    var blob = this.response;
                    
                    var headerReader = new FileReader();
                    headerReader.onloadend = function() {
                        var arr = (new Uint8Array(headerReader.result)).subarray(0, 4);
                        var header = "";
                        for(var i = 0; i < arr.length; i++) {
                            header += String.fromCharCode(arr[i]);
                        }
                        
                        console.log("Telangana Script: Header " + header);
                        if (header === "%PDF") {
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                window.flutter_inappwebview.callHandler('blobDownload', base64data, 'Telangana_Voter_List.pdf');
                            }
                        } else {
                            // It's HTML (likely error or next page), render it
                            var reader = new FileReader();
                            reader.onload = function() {
                               document.open();
                               document.write(reader.result);
                               document.close();
                            };
                            reader.readAsText(blob);
                        }
                    };
                    headerReader.readAsArrayBuffer(blob);
                } else {
                     console.log("Telangana Script: Download failed " + this.status);
                     alert('Download failed with status: ' + this.status);
                }
            };
            xhr.onerror = function() {
                console.log("Telangana Script: Network Error");
                alert('Network Error during download');
            };
            xhr.send(formData);
        }

        // Keeping other hooks just in case, but the click interceptor should handle 99% of cases
        window.addEventListener('submit', function(e) {
            console.log("Telangana Script: Submit Captured (Backup)");
            e.preventDefault();
            e.stopPropagation();
            interceptSubmit(e.target);
        }, true);
        
        // Hook programmatic submit
        HTMLFormElement.prototype.submit = function() {
             console.log("Telangana Script: Programmatic submit");
             interceptSubmit(this);
        };

        // Hook __doPostBack
        if (typeof __doPostBack === 'function') {
             __doPostBack = function(eventTarget, eventArgument) {
                 console.log("Telangana Script: __doPostBack hook");
                 window.__EVENTTARGET = eventTarget;
                 window.__EVENTARGUMENT = eventArgument;
                 form.__clickedButtonName = null;
                 interceptSubmit(form); 
             };
        }

      })();
    ''';
    await controller.evaluateJavascript(source: script);
  }

  Future<void> _handleBase64Download(
    String base64DataString,
    String fileName,
  ) async {
    setState(() {
      _isLoadingPdf = true;
    });

    try {
      // Strip the Data URI prefix if present
      String base64Content = base64DataString;
      if (base64Content.contains(',')) {
        base64Content = base64Content.split(',').last;
      }

      final bytes = base64Decode(base64Content.replaceAll(RegExp(r'\s+'), ''));
      final dir = await getApplicationDocumentsDirectory();
      final file = File('${dir.path}/$fileName');

      await file.writeAsBytes(bytes);

      if (mounted) {
        setState(() {
          _isLoadingPdf = false;
        });
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => PdfViewerScreen(
              path: file.path,
              title: fileName.replaceAll('.pdf', ''),
            ),
          ),
        );
      }
    } catch (e) {
      debugPrint('Base64 decode error: $e');
      if (mounted) {
        setState(() {
          _isLoadingPdf = false;
        });
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Failed to open PDF: $e')));
      }
    }
  }
}
