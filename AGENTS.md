# Agent Instructions

This is a live production Android app. Treat every change as production-sensitive.

## Core Rules

- Follow native Android best practices using Kotlin and Jetpack Compose.
- Keep the architecture clean: MVVM, Repository pattern, and clear separation between UI and business logic.
- Reuse existing ViewModels, repositories, utilities, navigation, Compose components, themes, and helper APIs before creating anything new.
- Do not rewrite, duplicate, or refactor existing working logic unless the requested change requires it.
- Only implement the requested change. Do not touch unrelated screens, flows, files, ads, analytics, permissions, navigation, or PDF workflows.
- Preserve the existing UI, theme, layout, design language, animations, navigation flow, and user experience unless explicitly asked to change them.

## Production Features To Protect

- Ads: app-open, banner, native, and interstitial flows.
- PDF download, auto-save, local file handling, viewer opening, and downloads list.
- In-app WebView and external website flows.
- Permissions, analytics, notifications, navigation, and app startup.

## Implementation Standards

- Keep Compose state stable and avoid unnecessary recomposition.
- Keep code modular, small, readable, and scalable.
- Prefer existing reusable Compose components over new UI code.
- Optimize for memory usage, startup time, and runtime performance.
- Avoid unused imports, dead code, broad refactors, and unrelated formatting churn.

## Verification

Before finishing any task:

- Build or run the narrowest useful verification.
- Confirm existing behavior remains unchanged.
- Mention any tests/builds run and any known limitations.
