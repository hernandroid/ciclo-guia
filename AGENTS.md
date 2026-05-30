# Ciclo Guía

Android application for cycling infrastructure in Lima, Peru.

## Mission

Help cyclists discover, understand and navigate Lima's cycling infrastructure through a reliable and easy-to-use mobile application.

Current priority: ship a stable beta to Google Play.

Prioritize:

1. Reliability
2. Maintainability
3. User experience
4. Delivery speed

Avoid overengineering.

---

## Project Structure

The project currently uses a single Gradle module: `:app`.

Internal organization follows feature-first packages with Clean Architecture-style layering.

Main package groups:

* `core`
* `di`
* `feature/appstart`
* `feature/onboarding`
* `feature/map`
* `feature/settings`

Inside features, prefer:

* `data`
* `domain`
* `presentation`

---

## Architecture

The project follows:

* MVVM
* Pragmatic Clean Architecture
* Repository Pattern
* Dependency Injection with Hilt
* Coroutines

Layer rules:

* Domain contains business rules, repository contracts and use cases.
* Data contains repository implementations, data sources and framework-specific integrations.
* Presentation contains ViewModels, UI state, UI events and Compose UI.

Avoid leaking network, storage, Android framework or Firebase implementation details outside the data layer.

---

## Presentation Layer

Technology:

* Jetpack Compose
* Material 3
* Navigation Compose

Guidelines:

* Keep Composables focused on UI rendering.
* Avoid business rules inside Composables.
* Prefer stateless Composables whenever practical.
* Hoist state to ViewModels.
* Keep UI components reusable and preview-friendly when practical.
* Prefer readability over clever Compose abstractions.

UI-only logic is allowed in Compose when directly related to rendering, animations, gestures, permissions, MapLibre view lifecycle, AndroidView interop or map interaction glue.

---

## State Management

Preferred approach:

* StateFlow for screen state.
* SharedFlow for one-shot effects.
* Immutable UI models whenever possible.
* Single source of truth.

Compose mutable state may be used when justified by UI requirements or existing implementation constraints.

Do not expose mutable state publicly unless there is a clear reason.

---

## ViewModels

ViewModels should:

* Coordinate UI workflows.
* Expose state to the UI.
* Handle UI events.
* Call use cases or application workflows.
* Keep UI state updates predictable.

Avoid:

* Heavy parsing on the main thread.
* Business rules that belong in domain.
* Direct framework-specific data access.

---

## Domain Layer

Use cases are preferred for:

* Business rules.
* Application workflows.
* Multi-step operations.
* Operations reused by multiple ViewModels.

Simple repository passthroughs do not require a use case unless they improve readability, consistency or maintainability.

---

## Data Layer

Use:

* Repository pattern.
* Local and remote data sources.
* OkHttp through the existing HttpClient abstraction.
* DataStore Preferences for simple persisted metadata and preferences.

Repositories should hide implementation details from the rest of the app.

---

## Cycleway Data Pipeline

The cycleway dataset pipeline is:

ArcGIS source
-> GitHub Actions update workflow
-> Firebase Hosting public files
-> Android app HTTP sync
-> local file cache and DataStore metadata

The Android app should treat Firebase Hosting public files as the runtime remote source.

Current hosted files live under:

* `public/public-data/cycleways`

Current local cache convention:

* `filesDir/cycleways/latest.geojson`

Current local metadata convention:

* DataStore `cycleways_dataset`

There is an embedded asset:

* `app/src/main/assets/ciclovias.geojson`

This asset currently exists but may not be wired into the repository flow. Treat it as a potential offline fallback, not as the primary runtime source, unless explicitly requested.

Do not change dataset contracts unless explicitly requested.

---

## Mapping

MapLibre is the official mapping solution.

Do not replace MapLibre unless explicitly requested.

When implementing map-related features:

* Preserve current map behavior.
* Preserve cycleway rendering.
* Preserve feature selection behavior.
* Preserve camera interactions.
* Preserve location-centering behavior.
* Be careful with MapView lifecycle and AndroidView interop.

Map style configuration may include a default OpenFreeMap style and optional MapTiler configuration through Gradle properties.

---

## Dependency Injection

Use Hilt.

Prefer constructor injection.

Place Hilt modules in the existing `di` structure unless a feature-specific module is clearly better.

Avoid manual dependency creation when dependency injection is available.

---

## Coroutines

Use structured concurrency.

Avoid:

* GlobalScope
* Blocking calls on the main thread

Use appropriate dispatchers for IO operations.

Keep long-running work away from the main thread.

---

## WorkManager

WorkManager may be used for background synchronization.

If modifying workers:

* Verify scheduling.
* Verify Hilt Worker integration.
* Avoid duplicating sync logic already owned by repositories or use cases.

---

## Firebase

Be precise about Firebase usage.

Runtime cycleway data currently comes from Firebase Hosting public files over HTTP.

Do not assume Firestore, Storage, Auth, Analytics or Crashlytics are actively used in app logic unless verified in code.

Avoid adding Firebase-specific app logic without clear product value.

---

## Code Style

Follow:

* SOLID
* DRY
* KISS

Prefer:

* `val` over `var`
* Small focused functions
* Clear names
* Explicit code over overly generic abstractions
* Readability over cleverness

Avoid:

* Premature abstractions
* Unrelated refactors
* Unused imports
* Stale TODOs when touching nearby code

---

## Dependencies

Do not introduce new dependencies unless explicitly requested or clearly justified.

Prefer existing project solutions before adding new libraries.

Do not change Gradle versions without explicit approval.

---

## Testing

When modifying behavior:

* Add or update tests when reasonable.
* Prioritize critical flows, business rules, mappers, formatters, repositories and ViewModels.
* Avoid excessive test infrastructure.
* Be aware that existing tests may be placeholders or stale.

When relevant, run narrower tests in addition to build validation.

---

## Build Validation

For read-only tasks:

* Do not build unless requested or needed.

After code changes, run when possible:

```bash
./gradlew assembleDebug
```

When relevant, also run narrower tests.

Never claim a build or test passed unless it was actually executed.

---

## Review Output

When files were modified, explain:

* Modified files
* Architectural decisions when meaningful
* Risks, limitations and follow-up tasks

For trivial changes, keep the explanation concise.

For read-only analysis, clearly state that no files were modified.

---

## Git Guidelines

Prefer:

* Small focused changes
* Easy-to-review diffs
* Minimal scope modifications

Avoid:

* Large unrelated refactors
* Modifying files outside the requested scope
* Rewriting working features without justification

---

## Documentation

Update documentation when introducing significant architectural, behavioral or operational changes.

Keep explanations practical and concise.

---

## Forbidden Actions

Do not:

* Change package names without explicit approval.
* Change dependency versions without explicit approval.
* Introduce new architectural layers without clear value.
* Remove existing functionality without explicit approval.
* Perform large-scale refactors without explicit approval.
* Replace MapLibre without explicit approval.
* Change dataset contracts without explicit approval.

---

## Definition of Success

A successful contribution is:

* Easy to understand
* Easy to maintain
* Consistent with the existing architecture
* Safe to review
* Safe to release
* Focused on helping ship the next beta

---

## Communication

Unless explicitly requested otherwise:

- Respond in Spanish.
- Explain decisions, risks and architectural reasoning in Spanish.
- Keep source code in English.
- Keep package names, class names, function names and technical identifiers in English.
- Keep comments in English.
- Keep commit messages in English.
- Keep public-facing UI text in the language requested by the task.
