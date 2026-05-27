# CS Bouira

Academic document viewer for the Computer Science department at Université de Bouira. Students browse years → semesters → modules → files (courses, exams, résumés, TDs/TPs, tests). Files are fetched live from `api.csbouira.xyz` (Google Drive proxy) and displayed in-app via WebView (PDFs/images). Supports bookmarks, offline downloads, and disk caching.

## UI Tech Stack

| Technology | Usage |
|---|---|
| **Kotlin** | Language |
| **Jetpack Compose** (BOM 2024.02) | Declarative UI — all screens are `@Composable` functions |
| **Material 3** | `Scaffold`, `TopAppBar`, `TabRow`, `Card`, `FilledTonalButton`, `OutlinedTextField`, `CircularProgressIndicator` |
| **Navigation Compose** (2.7.7) | `NavHost` with slide+fade animated transitions |
| **ViewModel + StateFlow** | Screen state management, reactive data binding via `collectAsState()` |
| **Room** (2.6.1, KSP) | Local persistence: bookmarks, download tracking, API response cache |
| **Retrofit 2 + Gson** | API calls (`getDriveYear`) returning `JsonElement`, deserialized to `DriveResponse` |
| **OkHttp** (custom client) | File downloads with progress callbacks |
| **WebView** | In-app file viewer for PDFs/images (Google Drive preview links, local file fallback) |
| **Coroutines** | Async operations (`viewModelScope.launch`, `Dispatchers.IO` for downloads) |
| **Compose Animation** | `fadeIn`, `fadeOut`, `slideInHorizontally`, `slideOutHorizontally` for screen transitions |
| **material-icons-extended** | File-type icons (PDF, Image, Code, Doc, Quiz, etc.) |
| **Gradle Version Catalog** | `libs.versions.toml` for dependency management |

## Architecture

Single-module app with `data/` (API, Room, models, repository) and `ui/` (screens by feature: home, modules, files, viewer, search, bookmarks). `ViewModelFactory` provides shared `CsbouiraRepository` with per-year cached API responses.
