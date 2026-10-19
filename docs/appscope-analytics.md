# Appscope analytics

The existing main-process `YojnaPlusApp` initializes the Kotlin tracker. Firebase, ads, navigation and PDF workflows remain in place. No additional Android permissions are required.

## Build configuration

- Endpoint: `https://appscope-tracking.alimailsamun.workers.dev/api/track`
- App ID: `app_b8662a3d3b8c9dc5ab23d3f3`
- Set `appscope.tracking.key` in the ignored root `local.properties`, or supply `APPSCOPE_TRACKING_KEY` as a Gradle property or environment variable. The integration's tracking key is configured locally; do not put it in documentation, request logs or version control.
- A build without a key disables collection and logs a configuration warning. Preserve the setting when building on another computer or CI.
- The release worker class has an explicit R8 keep rule.

## Collection and delivery

- One random UUID identifies an app installation. No advertising/hardware ID, name, email, voter details, selected area, CAPTCHA or PDF content is sent.
- A visit is created on a debounced process foreground transition. Screen changes and activity recreation do not create visits. A real return from the background does.
- Remaining in the foreground across IST midnight creates the new day's session, matching the service's counting rules.
- Each complete event has an immutable event ID and UTC occurrence time. The event and installation ID are persisted atomically before upload in `no_backup/appscope.preferences_pb`. This prevents backup/device transfer from cloning the installation identity or pending events.
- Foreground delivery and WorkManager share a serialized queue drain. Offline delivery uses network-constrained persistent work and exponential backoff starting at 60 seconds. A 15-minute periodic recovery job covers the crash window between persisting an event and scheduling one-time work. Jobs do not create visits by themselves.
- Accepted responses (200/201) remove the event. Network failures, 408/429 and server errors keep it. A `Retry-After` deadline is persisted and checked before further uploads. Irrecoverable 4xx responses discard the event and log only the status.
- If an upload is accepted but the process dies before local acknowledgement, its retry keeps the same ID; the service deduplicates it.
- The queue holds at most 500 pending visits. Further visits are skipped while full. Events older than seven days expire, matching the backend's acceptance window. Offline events count on their original IST date when delivered.

## Limits and verification

No internet means no immediate remote dashboard update. Counts synchronize after connectivity returns, subject to Android scheduling. Android force-stop prevents background work until the app is opened again. Uninstalling/clearing app data removes unsent events and can create a new installation ID. Killing the process before its asynchronous local write completes can lose that visit.

Verification includes HTTP status/payload unit tests, isolated device tests for persistence, concurrency, bounded storage and retry identity, plus an emulator offline/reconnect test against this app's dashboard. Integration verification creates real test visits for the emulator installation in the configured app; these are not organic-user measurements.

Keep the app's privacy notice and Play Data safety declaration consistent with its actual analytics services and pseudonymous identifier handling.

Protocol reference: https://appscope-tracking.alimailsamun.workers.dev/android-guide.md
