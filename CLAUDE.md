# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug
```

## Architecture Overview

**XiaobaoTV** is an Android video streaming app built with Jetpack Compose and Kotlin, following **Clean Architecture with MVVM**:

### Layers

- **`data/`** — Network, parsing, local DB, caches
  - `data/remote/` — Retrofit API (`XiaobaoApi`)
  - `data/parser/` — Jsoup-based HTML parsers for scraping vod detail, play pages, search, and category pages
  - `data/repository/` — Repository implementations
  - `data/cache/` — In-memory LRU caches (`DetailPageCache`, `PlaybackUrlCache`)
  - `data/local/` — Room database, DAOs, entities
  - `data/model/` — Network DTOs (Moshi-annotated)
- **`domain/`** — Business logic (no Android dependencies)
  - `domain/model/` — Domain models (`VodContent`, `VideoSource`, `Episode`, etc.)
  - `domain/repository/` — Repository interfaces
  - `domain/usecase/` — Thin use cases delegating to repositories
- **`ui/`** — Compose screens, ViewModels, navigation, theme, shared components
  - One package per feature (`home`, `detail`, `player`, `category`, `search`, `auth`, `profile`)
  - Each feature package has its Screen composable and ViewModel
- **`di/`** — Hilt DI modules (`NetworkModule`, `DatabaseModule`, `RepositoryModule`, `FirebaseModule`, `VideoCacheModule`)

### Navigation

Bottom nav with 4 tabs: Home, Categories, Search, Profile. Uses Jetpack Navigation Compose with routes defined in `Routes` object. Full-screen video player hides the bottom nav via `FullScreenState` + `CompositionLocal`.

### Data Flow

1. **Content (vod list/detail)**: API → Jsoup HTML parse → cache (Room + in-memory LRU) → domain model → ViewModel → Compose
2. **Video playback**: Detail page HTML → parse video sources → play page fetch → extract m3u8 URL → ExoPlayer
3. **Search**: Custom HTML scraping of search results page (API `wd` param is ignored by server)
4. **Category**: Paging 3 with `CategoryPagingSource`, fallback to HTML show page when API returns empty

### Caching Strategy

- **Vod detail**: 3-tier (in-memory LRU → Room → network), 1-hour Room TTL
- **Detail HTML**: Shared in-memory cache (`DetailPageCache`) deduplicates concurrent fetches between `ContentRepositoryImpl` and `VideoRepositoryImpl`
- **Playback URLs**: In-memory LRU cache, pre-fetched by `DetailViewModel` for instant playback
- **HTTP**: 10MB OkHttp cache, forced 5-min cache for HTML responses
- **ExoPlayer**: 200MB disk cache
- **Images**: Coil with 20% memory cache + 128MB disk cache

### Parser Overview

- `VodDetailParser` — Parses `/movie/detail/{id}.html` → `VodContent`
- `PlayPageParser` — Parses video source tabs + episode lists; extracts m3u8 URL from JS variables
- `SearchPageParser` — Parses `/search/wd/{query}.html` → list of `VodContent`
- `ShowPageParser` — Parses `/movie/show/{typeId}.html` → list of `VodContent`

### Key Patterns

- ViewModels use `MutableStateFlow` + `StateFlow` for UI state
- Repositories return `Result<T>` for error handling
- Use cases are thin wrappers that inject repositories
- All HTML fetching uses OkHttp directly (not Retrofit); only the vod list API uses Retrofit
- `Dispatchers.IO` for network/disk work via `withContext`
- Retry with exponential backoff for network calls (2 attempts, 1s delay)
