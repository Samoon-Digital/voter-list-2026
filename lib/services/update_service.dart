import 'package:flutter/foundation.dart';
import 'package:in_app_update/in_app_update.dart';

class UpdateService {
  UpdateService._();
  static final UpdateService instance = UpdateService._();

  bool _updateInProgress = false;

  /// Checks for available updates and triggers immediate update if possible.
  /// Returns true if a force update screen should be shown (update available but immediate update failed/not allowed).
  Future<bool> checkForUpdate() async {
    if (_updateInProgress) return false;

    // In-app updates only work on Android
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return false;

    try {
      final info = await InAppUpdate.checkForUpdate();

      if (info.updateAvailability == UpdateAvailability.updateAvailable) {
        if (info.immediateUpdateAllowed) {
          _updateInProgress = true;
          await InAppUpdate.performImmediateUpdate();
          _updateInProgress = false;
          return false;
        } else {
          // If immediate update is not allowed, we might need a force update view
          return true;
        }
      }
    } catch (e) {
      debugPrint('In-app update check failed: $e');
    }

    return false;
  }
}
