# UsmanNotepad V3.00 Premium UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship UsmanNotepad V3.00 with a premium Jetpack Compose primary UI while preserving the proven V2 Java/SQLite feature engine and user data.

**Architecture:** Add Kotlin + Compose alongside the existing Java code, make new Compose activities the primary launcher/editor/search/navigation surfaces, and bridge to the existing `NoteRepository`, security, reminders, backup, graph, history, drawing, tags, templates, and widget flows. Do not rewrite the database or V1/V2 migration layer.

**Tech Stack:** Java 17, Kotlin 2.1.20, Compose compiler plugin 2.1.20, Jetpack Compose BOM 2025.12.00, Material 3 foundations, Activity Compose 1.10.1, AGP 8.9.2, Gradle 8.11.1, compileSdk/targetSdk 35, existing SQLite repository and Android platform APIs.

**Spec:** `docs/superpowers/specs/2026-09-03-usmannotepad-v3-premium-ui-design.md`

## Global Constraints
- applicationId `com.usman.notepad`.
- versionName `3.0.0`; versionCode `3`.
- minSdk 23; targetSdk 35; compileSdk 35.
- AGP 8.9.2; Gradle 8.11.1; JDK 17.
- Preserve existing SQLite data, encryption, migration, reminders, backup, attachments and widget behavior.
- Do not adopt Compose 1.12 / SDK 37 / AGP 9 in this release.
- No OpenAI/external AI API/cloud account/analytics requirement.
- Every visible V3 action must work or route to an existing working V2 flow.
- Locked-note previews must never expose encrypted body content.
- Final workflow artifact name must remain exactly `UsmanNotepad-APK`.

---

## File Map

### Build/config
- Modify `build.gradle.kts` — add Kotlin Android and Compose compiler plugins version 2.1.20.
- Modify `app/build.gradle.kts` — apply Kotlin/Compose plugins, enable Compose, add stable Compose dependencies, set version 3.
- Modify `app/src/main/AndroidManifest.xml` — make `V3MainActivity` launcher and register `V3EditorActivity`; retain legacy activities/receivers.
- Modify `.github/workflows/main.yml` — collect `UsmanNotepad-V3.00.apk`.

### V3 theme/state
- Create `app/src/main/java/com/usman/notepad/v3/theme/UsmanTheme.kt` — light/dark/OLED palettes, typography, shapes.
- Create `app/src/main/java/com/usman/notepad/v3/theme/UsmanTokens.kt` — spacing, motion, accent presets, writing density.
- Create `app/src/main/java/com/usman/notepad/v3/state/AppearancePreferences.kt` — SharedPreferences-backed theme/accent/density/text-size state.
- Create `app/src/main/java/com/usman/notepad/v3/state/UiModels.kt` — route/filter/preview models and privacy-safe mapping helpers.

### Components
- Create `app/src/main/java/com/usman/notepad/v3/components/UsmanComponents.kt` — editorial rows, pinned cards, search surface, chips, section headers, bottom navigation, floating create control, empty state.

### Screens/activities
- Create `app/src/main/java/com/usman/notepad/v3/V3MainActivity.kt` — Compose launcher and three-tab shell.
- Create `app/src/main/java/com/usman/notepad/v3/V3EditorActivity.kt` — Compose editor preserving repository save/history/link logic.
- Create `app/src/main/java/com/usman/notepad/v3/screens/NotesScreen.kt` — premium home.
- Create `app/src/main/java/com/usman/notepad/v3/screens/SearchScreen.kt` — dedicated live search.
- Create `app/src/main/java/com/usman/notepad/v3/screens/FoldersScreen.kt` — folder navigation.
- Create `app/src/main/java/com/usman/notepad/v3/screens/MoreScreen.kt` — grouped menu.
- Create `app/src/main/java/com/usman/notepad/v3/screens/AppearanceScreen.kt` — live preview and theme controls.
- Create `app/src/main/java/com/usman/notepad/v3/screens/TrashScreen.kt` — calm trash presentation.
- Create `app/src/main/java/com/usman/notepad/v3/screens/CreateSheet.kt` — working creation actions only.
- Create `app/src/main/java/com/usman/notepad/v3/screens/EditorSheets.kt` — editor action grouping and legacy bridges.

### Tests
- Create `app/src/test/java/com/usman/notepad/v3/UiModelsTest.kt`.
- Create `app/src/test/java/com/usman/notepad/v3/AppearancePreferencesLogicTest.kt`.

---

### Task 1: Add the compatible Compose toolchain and V3 theme state

**Files:** build files, manifest, `UsmanTokens.kt`, `UsmanTheme.kt`, `AppearancePreferences.kt`, `UiModels.kt`, tests.

**Interfaces:**
- `enum class ThemeMode { SYSTEM, LIGHT, DARK, OLED }`
- `enum class WritingDensity { CALM, MODERN, COMPACT }`
- `data class AppearanceState(val themeMode: ThemeMode, val accentIndex: Int, val density: WritingDensity, val textScale: Float)`
- `AppearancePreferences.load(context): AppearanceState`
- `AppearancePreferences.save(context, state)`
- `privacySafePreview(note: Note, hideLockedTitle: Boolean): NotePreviewModel`

- [ ] **Step 1: Add failing unit tests for privacy-safe preview and appearance normalization.**

```kotlin
@Test fun lockedNoteNeverExposesBody() {
    val n = Note().apply { title = "Secret"; body = "private body"; locked = true }
    val p = privacySafePreview(n, false)
    assertEquals("Protected note", p.preview)
    assertFalse(p.preview.contains("private body"))
}

@Test fun accentIndexIsClamped() {
    assertEquals(0, normalizeAccentIndex(-5))
    assertEquals(5, normalizeAccentIndex(999))
}
```

- [ ] **Step 2: Run:** `gradle --no-daemon testDebugUnitTest --tests 'com.usman.notepad.v3.*'`.
Expected: fail because V3 helpers do not exist.

- [ ] **Step 3: Update Gradle configuration.**
Root plugins must include:

```kotlin
plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
}
```

App plugins/dependencies must include:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures { compose = true }
    defaultConfig { versionCode = 3; versionName = "3.0.0" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.20")
}
```

- [ ] **Step 4: Implement tokens/theme/preferences/helpers.**
Use warm paper light palette, graphite dark, true-black OLED, six restrained accent presets, and density-based spacing/text scale.

- [ ] **Step 5: Run tests and compile Kotlin:** `gradle --no-daemon testDebugUnitTest :app:compileDebugKotlin --stacktrace`.
Expected: success.

- [ ] **Step 6: Commit:** `feat: add V3 Compose design system and appearance state`.

---

### Task 2: Build the premium Home shell and working create flow

**Files:** `V3MainActivity.kt`, `NotesScreen.kt`, `CreateSheet.kt`, `UsmanComponents.kt`, manifest.

**Interfaces:**
- `enum class MainTab { NOTES, FOLDERS, MORE }`
- `enum class HomeFilter { ALL, PINNED, FAVORITES, RECENT }`
- `@Composable fun NotesScreen(repo: NoteRepository, appearance: AppearanceState, onOpenNote: (Long) -> Unit, onSearch: () -> Unit, onCreate: () -> Unit)`
- `@Composable fun CreateSheet(onText: () -> Unit, onChecklist: () -> Unit, onScratch: () -> Unit, onPhoto: () -> Unit, onVoice: () -> Unit, onDismiss: () -> Unit)`

- [ ] **Step 1: Add tests for greeting/filter mapping.**

```kotlin
@Test fun eveningGreeting() = assertEquals("Good evening", greetingForHour(20))
@Test fun recentFilterMapsToRepositoryAll() = assertEquals("all", repositoryFilter(HomeFilter.RECENT))
```

- [ ] **Step 2: Run targeted tests and confirm failure.**

- [ ] **Step 3: Implement reusable premium components:** editorial note row, pinned card, tonal search bar, compact filter chip, bottom nav, floating `＋ New note` pill, empty state.

- [ ] **Step 4: Implement `V3MainActivity` with `setContent { UsmanNotepadTheme { ... } }`, three tabs, appearance loading, and repository instance.**

- [ ] **Step 5: Implement Home behavior.**
Use repository-backed lists; do not expose locked body; pinned section only when data exists; recent list below; natural greeting and count; editorial list with whitespace rather than boxed-card soup.

- [ ] **Step 6: Implement create actions:**
Text → `V3EditorActivity` new note.
Checklist → `V3EditorActivity` with extra `initial_mode=checklist`.
Scratch → existing `repo.getOrCreateScratch()` then V3 editor.
Photo → create/open note then route to legacy `EditorActivity` attachment flow only if necessary.
Voice → create/open note then route to legacy `EditorActivity` voice flow only if V3 direct voice action is not yet available.

- [ ] **Step 7: Make `V3MainActivity` the launcher in manifest; keep old `MainActivity` registered but not launcher.**

- [ ] **Step 8: Run:** `gradle --no-daemon testDebugUnitTest :app:assembleDebug --stacktrace`.
Expected: success.

- [ ] **Step 9: Commit:** `feat: add premium V3 home and navigation shell`.

---

### Task 3: Build the V3 hero editor with real autosave and legacy tool bridges

**Files:** `V3EditorActivity.kt`, `EditorSheets.kt`, `UiModels.kt`.

**Interfaces:**
- Activity extras: `note_id: Long`, `initial_mode: String?`.
- Autosave debounce: 700ms, matching current V2 behavior.
- `EditorSaveState = Idle | Saving | Saved | Error`.

- [ ] **Step 1: Add pure tests for editor title fallback and save-label mapping.**

```kotlin
@Test fun blankTitleFallsBackToUntitled() = assertEquals("Untitled", editorDisplayTitle("   "))
@Test fun savingLabelIsQuiet() = assertEquals("Saving…", saveLabel(EditorSaveState.Saving))
```

- [ ] **Step 2: Confirm targeted test failure.**

- [ ] **Step 3: Implement Compose editor surface:** minimal back/overflow row, large borderless title, borderless body, natural RTL text direction, subtle save state, IME-safe bottom toolbar.

- [ ] **Step 4: Preserve data semantics:**
- load note via existing `NoteRepository`
- enforce time-capsule gate before rendering
- decrypt locked note through current `AppLock`/`CryptoManager` path or bridge to legacy unlock activity when platform credential handling cannot be safely embedded
- save body/title/mode through `repo.save(note, snapshot)`
- rebuild wiki link rows through current parser/repository behavior
- save on pause/back and after debounce

- [ ] **Step 5: Implement editor sheets with working routes only.**
Primary quick toolbar: `Aa`, checklist, bullet, insert.
Overflow groups existing actions: History, Tags, Folder, Reminder, Time Capsule, Expiry, Lock, Theme, Focus, Voice, Image, Camera, Attachments, Export, Drawing, Backlinks, Template, Delete.
For complex existing Android-platform flows, launch the proven legacy `EditorActivity` or dedicated existing activity with the same note ID rather than duplicating risky logic.

- [ ] **Step 6: Implement focus mode and scratch styling.**
Scratch uses warmer surface and immediate body focus. Focus mode reduces chrome prominence, not navigation safety.

- [ ] **Step 7: Run unit tests and assemble debug.**

- [ ] **Step 8: Commit:** `feat: add distraction-free V3 Compose editor`.

---

### Task 4: Add Search, Folders, More, Appearance, and Trash Compose screens

**Files:** `SearchScreen.kt`, `FoldersScreen.kt`, `MoreScreen.kt`, `AppearanceScreen.kt`, `TrashScreen.kt`, `V3MainActivity.kt`.

**Interfaces:**
- Search receives `NoteRepository` and emits `onOpenNote(Long)`.
- Folders reads `repo.folderRows()` and routes folder selection to filtered note list or existing `FoldersActivity` when nested mutation is required.
- More routes to existing working activities for secondary V2 features.
- Appearance writes `AppearancePreferences` and updates Compose state immediately.

- [ ] **Step 1: Add tests for search match emphasis helper and theme labels.**

```kotlin
@Test fun oledLabelIsStable() = assertEquals("OLED", ThemeMode.OLED.displayName())
@Test fun noMatchReturnsWholeText() = assertEquals(listOf("hello"), splitHighlight("hello", "xyz"))
```

- [ ] **Step 2: Confirm failure.**

- [ ] **Step 3: Implement dedicated Search UI with immediate focus, live results, restrained accent match highlight and privacy-safe locked rows.**

- [ ] **Step 4: Implement Folders tab with editorial header, nested indentation, note counts when available, and create/manage route to legacy `FoldersActivity`.**

- [ ] **Step 5: Implement More grouped sections:** Favorites, Tags, Templates, Archive, Trash; Appearance; Privacy & Lock; Backup & Export; About. Route each row to existing Java activity or Compose sub-screen.

- [ ] **Step 6: Implement Appearance live mini-preview, System/Light/Dark/OLED theme choice, six accent swatches, Calm/Modern/Compact density, text scale control. Save immediately and apply without restart.**

- [ ] **Step 7: Implement Trash screen using repository deleted-note APIs where available; if V2 TrashActivity owns restore/delete-forever mutations, render Compose list for browse and route destructive operations to the proven legacy screen.**

- [ ] **Step 8: Run tests and assemble debug.**

- [ ] **Step 9: Commit:** `feat: add V3 search folders appearance and more screens`.

---

### Task 5: Visual polish, accessibility, RTL, and behavior audit

**Files:** all V3 Compose files; no data-schema changes.

- [ ] **Step 1: Structure pass.**
Check Notes/Folders/More bottom-nav consistency, no redundant headers/cards, 4–7 meaningful note rows visible on common phone heights, pinned section conditional.

- [ ] **Step 2: Visual polish pass.**
Ensure spacing uses tokens, title/body typography follows theme, surfaces remain warm/graphite, no default purple, shadows only on floating elements, icon/text touch targets ≥44dp.

- [ ] **Step 3: Delight pass.**
Add 120–260ms alpha/translation/selection animations, polished pressed states, empty-state microcopy, compact snackbars where actions support undo.

- [ ] **Step 4: RTL audit.**
Editor body/title use natural direction and start alignment; test source with mixed Urdu/English sample string `"آج meeting notes complete کرنا ہے"` without forced LTR transformation.

- [ ] **Step 5: Dead-control audit.**
Search V3 source for click handlers and verify every visible button has a route/action. Remove any placeholder control.

- [ ] **Step 6: Run:** `gradle --no-daemon testDebugUnitTest :app:assembleDebug --stacktrace`.

- [ ] **Step 7: Commit:** `style: refine V3 editorial UI and accessibility`.

---

### Task 6: CI release build and artifact verification

**Files:** `.github/workflows/main.yml` and release metadata if needed.

- [ ] **Step 1: Update workflow collection name.**

```bash
APK="$(find app/build/outputs/apk -type f -name '*debug*.apk' | head -n 1)"
test -n "$APK"
cp "$APK" UsmanNotepad-V3.00.apk
```

Artifact config remains:

```yaml
name: UsmanNotepad-APK
path: UsmanNotepad-V3.00.apk
if-no-files-found: error
```

- [ ] **Step 2: Ensure workflow runs `testDebugUnitTest` before `:app:assembleDebug`.**

- [ ] **Step 3: Push on isolated `v3-development` branch and inspect Actions job steps. If compile/test fails, fetch job logs, fix the exact error, push again, and repeat until all steps conclude `success`.**

- [ ] **Step 4: Download the successful `UsmanNotepad-APK` artifact.**

- [ ] **Step 5: Verify ZIP integrity and APK contents.**
Required checks:
- artifact ZIP integrity passes
- archive contains `UsmanNotepad-V3.00.apk`
- `file` identifies Android APK
- APK contains `AndroidManifest.xml`
- APK contains `classes.dex`
- APK has APK Signing Block
- record SHA-256

- [ ] **Step 6: Promote only the verified V3 branch head to `main` using a fast-forward ref update.**

- [ ] **Step 7: Verify the resulting main-branch workflow run again if promotion triggers CI; use that latest successful artifact as the final user download.**

- [ ] **Step 8: Final report contains only essential release facts and the direct sandbox APK/ZIP download link.**

## Plan Self-Review
- Spec coverage: theme, Home, Editor, Search, Folders, More, Appearance, Trash, RTL, accessibility, existing-feature preservation, CI and artifact verification are mapped to Tasks 1–6.
- Data safety: no Room/schema rewrite appears in any task.
- Privacy: locked-body preview protection is explicitly tested in Task 1 and reused by list/search surfaces.
- Dependency consistency: Kotlin and Compose compiler both 2.1.20; Compose BOM fixed to 2025.12.00; AGP/SDK remain 8.9.2/35.
- No placeholder implementation steps are used; legacy bridges are explicit choices for already-working secondary flows.
