# UsmanNotepad V3.00 Premium UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver UsmanNotepad V3.00 as an installable premium Compose-based Android APK while preserving V2 data and working feature logic.

**Architecture:** Keep the Java SQLite/security/reminder/backup engine and add a Kotlin Jetpack Compose presentation layer around it. A small adapter exposes V2 repository operations to Compose screens; appearance preferences are additive and do not modify note storage.

**Tech Stack:** Java 17, Kotlin 2.2.10, AGP 8.9.2, Gradle 8.11.1, compileSdk/targetSdk 35, Jetpack Compose BOM 2025.05.01, Material 3 foundations, Android Views interop only where a preserved V2 flow remains Java-based.

**Spec:** `docs/superpowers/specs/2026-09-03-usmannotepad-v3-design.md`

## Global Constraints
- `applicationId = com.usman.notepad`.
- `versionCode = 3`; `versionName = 3.0.0`.
- JDK 17, AGP 8.9.2, Gradle 8.11.1, compileSdk 35, targetSdk 35, minSdk 23, Build Tools 35.0.0.
- Kotlin 2.2.10 plus matching Compose compiler Gradle plugin.
- Compose BOM 2025.05.01.
- Existing SQLite database, note IDs, V1 migration path, encrypted content, backup format, reminders and attachments remain compatible.
- No external AI API, analytics SDK, cloud account, or mandatory INTERNET permission.
- Every visible action must invoke a working feature.
- CI artifact name remains exactly `UsmanNotepad-APK` and contains `UsmanNotepad-V3.00.apk`.

---

## File Structure

### Build
- Modify `build.gradle.kts` — add Kotlin Android and Compose compiler plugins.
- Modify `app/build.gradle.kts` — V3 version metadata, Compose build feature and dependencies.
- Modify `.github/workflows/main.yml` — collect APK as `UsmanNotepad-V3.00.apk`, run tests before build, enable V3 branch.

### Compose shell
- Create `app/src/main/java/com/usman/notepad/v3/V3MainActivity.kt` — Compose entry point and top-level navigation.
- Create `app/src/main/java/com/usman/notepad/v3/V3Routes.kt` — route constants and route argument helpers.
- Create `app/src/main/java/com/usman/notepad/v3/data/V3RepositoryAdapter.kt` — Java repository bridge and view models for note/folder/tag rows.
- Create `app/src/main/java/com/usman/notepad/v3/data/AppearanceStore.kt` — theme/accent/density/text-size preferences.

### Design system
- Create `app/src/main/java/com/usman/notepad/v3/theme/UsmanTheme.kt`.
- Create `app/src/main/java/com/usman/notepad/v3/theme/UsmanTokens.kt`.
- Create `app/src/main/java/com/usman/notepad/v3/components/EditorialComponents.kt`.

### Screens
- Create `v3/screens/HomeScreen.kt`.
- Create `v3/screens/SearchScreen.kt`.
- Create `v3/screens/EditorScreen.kt`.
- Create `v3/screens/FoldersScreen.kt`.
- Create `v3/screens/MoreScreen.kt`.
- Create `v3/screens/AppearanceScreen.kt`.
- Create `v3/screens/TrashScreen.kt`.
- Create `v3/screens/ScratchScreen.kt`.

### Tests
- Create `app/src/test/java/com/usman/notepad/v3/PreviewFormatterTest.kt`.
- Create `app/src/test/java/com/usman/notepad/v3/AppearanceStoreLogicTest.kt`.
- Preserve existing Java tests.

---

### Task 1: Enable Kotlin + stable Compose without changing V2 data

**Files:** `build.gradle.kts`, `app/build.gradle.kts`.

**Produces:** a mixed Java/Kotlin Android project with Compose enabled and V3 metadata.

- [ ] Add plugins `org.jetbrains.kotlin.android` version `2.2.10` and `org.jetbrains.kotlin.plugin.compose` version `2.2.10` with `apply false` at root.
- [ ] Apply both plugins in `app/build.gradle.kts`.
- [ ] Set `versionCode = 3` and `versionName = "3.0.0"`; keep SDK values unchanged.
- [ ] Enable `buildFeatures { compose = true }`.
- [ ] Add Compose BOM `2025.05.01`, Material3, foundation, UI, tooling-preview, activity-compose, lifecycle-runtime-compose, icons-core and test dependencies.
- [ ] Run `gradle --no-daemon testDebugUnitTest :app:assembleDebug --stacktrace` in CI; expected result is compile success before screen migration.
- [ ] Commit: `build: enable stable Compose stack for V3`.

### Task 2: Build the V3 design-token system

**Files:** `UsmanTokens.kt`, `UsmanTheme.kt`, `AppearanceStore.kt`.

**Interfaces:**
- `enum class V3ThemeMode { SYSTEM, LIGHT, DARK, OLED }`
- `enum class V3Accent { INDIGO, VIOLET, SAGE, ROSE, SKY, GRAPHITE }`
- `enum class V3Density { CALM, MODERN, COMPACT }`
- `data class AppearancePrefs(val themeMode: V3ThemeMode, val accent: V3Accent, val density: V3Density, val textScale: Float)`
- `AppearanceStore.load(context): AppearancePrefs`
- `AppearanceStore.save(context, prefs)`

- [ ] Write pure unit tests for preference enum serialization, default values and text-scale clamping to `0.85f..1.25f`.
- [ ] Implement warm light, graphite dark, OLED color schemes and restrained accent mapping.
- [ ] Implement centralized spacing, shapes, typography sizes and motion-duration constants.
- [ ] Implement `UsmanNotepadTheme(prefs, content)` with system dark detection when mode is SYSTEM.
- [ ] Run unit tests and assembleDebug.
- [ ] Commit: `feat: add V3 premium design system`.

### Task 3: Add repository bridge and safe preview formatting

**Files:** `V3RepositoryAdapter.kt`, `PreviewFormatterTest.kt`.

**Interfaces:**
- `data class V3NoteRow(id, title, preview, pinned, favorite, locked, quickCopy, mode, updatedAt, folderLabel, tags, checklistDone, checklistTotal)`
- `V3RepositoryAdapter.listNotes(query: String, filter: String): List<V3NoteRow>`
- `openNoteIntent(context, id): Intent`
- `createNote(context, mode: String): Long`
- `togglePin(id)`, `toggleFavorite(id)`, `archive(id)`, `trash(id)`, `restore(id)`.

- [ ] Write tests for locked-note masking, blank-title fallback, preview whitespace normalization, max preview length, checklist `☐/☑/✓` counting.
- [ ] Implement adapter by calling the existing Java `NoteRepository` and `Note` model; do not create a second database.
- [ ] Ensure locked notes never expose body text in `V3NoteRow.preview`.
- [ ] Add folder/tag labels only when repository data is available; absence falls back to empty metadata.
- [ ] Run unit tests and build.
- [ ] Commit: `feat: bridge V2 data into V3 Compose UI`.

### Task 4: Create top-level V3 shell, bottom navigation and common components

**Files:** `V3MainActivity.kt`, `V3Routes.kt`, `EditorialComponents.kt`, `AndroidManifest.xml`.

**Interfaces:**
- top destinations: Notes, Folders, More.
- route helpers for Search, Editor(noteId/newMode), Appearance, Trash and Scratch.

- [ ] Implement edge-to-edge Compose Activity with `UsmanNotepadTheme`.
- [ ] Build reusable `EditorialNoteRow`, `PinnedNoteCard`, `FloatingSearchBar`, `FilterPill`, `FloatingComposeButton`, `SectionHeader`, `SettingsRow`, `EmptyState`, and premium modal-sheet wrappers.
- [ ] Implement three-destination bottom navigation; hide it on editor/search/full-screen utility routes.
- [ ] Update launcher manifest entry to `V3MainActivity` while keeping existing Java Activities registered for preserved tools/share/widget/reminder flows.
- [ ] Ensure widget/share intents that formerly opened MainActivity route to the new launcher or preserved activity without breaking manifest resolution.
- [ ] Run manifest merge/build.
- [ ] Commit: `feat: add V3 navigation shell and editorial components`.

### Task 5: Rebuild Home and Search as premium Compose screens

**Files:** `HomeScreen.kt`, `SearchScreen.kt`.

**Home requirements:** contextual greeting, editorial heading, note/pinned counts, tonal search, four compact filters, optional pinned horizontal list, recent editorial list, signature New Note control, create bottom sheet.

**Create sheet actions:**
- Text -> create/open blank note
- Checklist -> create with checklist mode
- Scratch Pad -> Scratch route
- Photo -> create note then invoke existing image flow through EditorActivity
- Voice -> create note then invoke existing voice flow through EditorActivity

- [ ] Implement note list with stable keys and locked-content masking.
- [ ] Implement scroll-aware compose button expansion/collapse using simple alpha/size/label transitions under 260ms.
- [ ] Implement filter chips `all`, `pinned`, `favorites`, `recent`; map recent to local updated-time ordering without creating fake data.
- [ ] Implement full-screen live Search with immediate focus, matching excerpt and empty state.
- [ ] Tapping a note opens the V3 Editor route for standard editing; long press opens working pin/favorite/archive/trash actions.
- [ ] Run build and unit tests.
- [ ] Commit: `feat: redesign V3 home and search`.

### Task 6: Build the hero Compose editor while preserving V2 tools

**Files:** `EditorScreen.kt`, `V3RepositoryAdapter.kt` additions.

**Interfaces:**
- `loadEditableNote(id): EditableV3Note`
- `saveEditableNote(note, snapshot: Boolean)` delegates to existing repository.
- utility bridge `openLegacyEditorTool(context, noteId)` opens the existing `EditorActivity` for advanced tools not reimplemented in Compose.

- [ ] Implement borderless title/body fields with content-driven text direction, large title typography and comfortable body line-height.
- [ ] Implement 700ms debounced autosave plus immediate save on navigation/background through lifecycle observer.
- [ ] Display unobtrusive `Saving…` then `✓ Saved` state.
- [ ] Implement minimal top chrome: Back, pin toggle, More.
- [ ] Implement floating keyboard toolbar for structure actions: heading marker, checklist, bullet, insert sheet.
- [ ] Insert sheet actions that can be implemented locally modify body text; image/reminder/drawing/backlinks/history/security/export invoke the existing Java advanced tool flow so no dead control exists.
- [ ] Implement focus mode by fading secondary chrome without hiding required back navigation.
- [ ] Ensure encrypted locked notes are not directly decrypted by Compose unless existing security flow has authenticated; route locked note opening through `EditorActivity` authentication when necessary.
- [ ] Run tests/build.
- [ ] Commit: `feat: add distraction-free V3 Compose editor`.

### Task 7: Build Folders, More, Appearance, Trash and Scratch

**Files:** `FoldersScreen.kt`, `MoreScreen.kt`, `AppearanceScreen.kt`, `TrashScreen.kt`, `ScratchScreen.kt`.

- [ ] Folders: render nested folder rows with counts/indentation and open folder-filtered notes; folder management actions invoke existing repository APIs or existing FoldersActivity where richer editing is needed.
- [ ] More: grouped ORGANIZE/PERSONALIZE/PRIVACY/DATA/ABOUT rows; every row routes to a working Compose screen or existing Java Activity.
- [ ] Appearance: live miniature note preview, theme mode, accent, density and text-size controls that persist immediately.
- [ ] Trash: list soft-deleted notes, Restore and Delete Forever confirmation through existing repository.
- [ ] Scratch: warm surface, body-first immediate focus, autosave, Discard, Convert to Note.
- [ ] Verify all theme variants and empty states compile without hardcoded white/black assumptions.
- [ ] Commit: `feat: add V3 folders appearance more trash and scratch`.

### Task 8: V2 feature reachability and compatibility pass

**Files:** launcher/navigation/manifest plus existing Java Activities only where routing corrections are required.

- [ ] Verify existing Settings/Backup flow remains reachable from More -> Backup & Export.
- [ ] Verify Privacy & Lock reaches existing lock configuration and does not bypass encryption.
- [ ] Verify Tags, Templates, Archive, Graph, History, reminders, drawing, share capture and widget paths remain reachable.
- [ ] Verify installation keeps package name and database identifiers unchanged.
- [ ] Verify share target and reminder notification intents open a valid activity after launcher change.
- [ ] Run full unit tests and assembleDebug.
- [ ] Commit: `fix: preserve V2 feature routes in V3 shell`.

### Task 9: Visual refinement passes

**Files:** Compose theme/components/screens only.

- [ ] Pass 1 Structure: reduce duplicate headers, keep 4–7 useful notes visible on common phone height, verify navigation hierarchy and create action prominence.
- [ ] Pass 2 Polish: normalize spacing to token values, typography hierarchy, muted separators, corner-radius hierarchy, metadata density, light/dark/OLED surface contrast.
- [ ] Pass 3 Delight: pressed states, subtle haptics on create/pin/favorite, filter selection animation, empty-state microcopy, save-state transitions.
- [ ] Audit for visual red flags: no default purple, no rainbow cards, no excessive cards, no icon soup, no giant toolbar, no fake buttons.
- [ ] Run tests/build after refinements.
- [ ] Commit: `style: refine UsmanNotepad V3 premium experience`.

### Task 10: CI and final APK verification

**Files:** `.github/workflows/main.yml`.

- [ ] Workflow runs on `v3-development` and `main` pushes.
- [ ] Install SDK 35/build-tools 35.0.0 and use Gradle 8.11.1/JDK 17.
- [ ] Run `gradle --no-daemon testDebugUnitTest --stacktrace` before `gradle --no-daemon :app:assembleDebug --stacktrace`.
- [ ] Copy debug APK to `UsmanNotepad-V3.00.apk`.
- [ ] Upload artifact name exactly `UsmanNotepad-APK`.
- [ ] Fetch final job steps and require every build/test/upload step conclusion `success`.
- [ ] Download artifact ZIP, run archive integrity test, confirm exactly one `.apk`, verify APK contains `AndroidManifest.xml`, `classes.dex`, Gradle app metadata and APK signing block.
- [ ] Move verified V3 branch commit to `main` using a fast-forward update only.
- [ ] Allow final main CI to finish successfully and download the final main-branch artifact.

## Acceptance Checklist
- Existing V2 notes survive V3 install.
- V3 launcher is Compose-based.
- Home, Search, Editor, Folders, More, Appearance, Trash and Scratch use Soft Editorial Minimalism.
- Light, Dark and OLED are deliberately styled.
- Bottom navigation has exactly Notes / Folders / More.
- Locked note previews never reveal content.
- Existing V2 feature flows remain reachable.
- `versionName 3.0.0`, `versionCode 3`.
- All unit tests pass.
- `assembleDebug` passes.
- final artifact is `UsmanNotepad-APK` containing `UsmanNotepad-V3.00.apk`.
