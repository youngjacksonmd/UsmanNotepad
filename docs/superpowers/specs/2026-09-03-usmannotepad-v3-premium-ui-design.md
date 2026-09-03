# UsmanNotepad V3.00 Premium UI Design

## Goal
Upgrade UsmanNotepad V2.00 into V3.00 with a premium Android interface based on the approved **Soft Editorial Minimalism** direction while preserving existing notes, database schema, encryption behavior, reminders, backup/restore, widgets, attachments, folders, tags, history, graph, scratch/daily notes, share capture, and V1→V2 migration compatibility.

V3 is primarily a visual/interaction architecture upgrade, not a data reset.

## Product Promise
UsmanNotepad should feel like a premium digital notebook: calm, editorial, warm, distraction-free, and unmistakably more polished than a generic Android CRUD app.

The interface must prioritize:
1. content
2. typography
3. spacing
4. hierarchy
5. interaction
6. color
7. decoration

## Version and Compatibility
- applicationId: `com.usman.notepad`
- versionName: `3.0.0`
- versionCode: `3`
- minSdk: 23
- targetSdk: 35
- compileSdk: 35
- AGP: 8.9.2
- Gradle: 8.11.1
- JDK: 17
- Keep the currently proven Android 35 build pipeline.

### Compose compatibility choice
Do not adopt Compose 1.12 because it requires compileSdk 37 and AGP 9.x. Use a stable Compose 1.10.x-compatible stack with Kotlin/Compose compiler versions that build reliably on AGP 8.9.2 / Gradle 8.11.1.

## Architectural Strategy
Use a **hybrid Compose migration**:

- Keep existing Java repositories, SQLite helpers, security classes, reminder services, backup codec/manager, receivers, widget implementation, attachment storage, and migration logic.
- Add Kotlin + Jetpack Compose for primary user-facing screens.
- Existing Java Activities may remain temporarily for secondary flows when replacing them would add risk without visible V3 value.
- Primary V3 entry points must be Compose-driven.
- No Room rewrite in V3; data remains in the current SQLite schema to avoid migration risk.
- No fake buttons: every visible action must call a working V2 capability or be omitted.

## Package Direction
Create Compose UI under:
- `app/src/main/java/com/usman/notepad/v3/theme/`
- `app/src/main/java/com/usman/notepad/v3/components/`
- `app/src/main/java/com/usman/notepad/v3/screens/`
- `app/src/main/java/com/usman/notepad/v3/navigation/`
- `app/src/main/java/com/usman/notepad/v3/state/`

The repository/data layer remains where it is.

## Design System
Define centralized tokens rather than magic numbers.

### Color
Light mode:
- warm ivory/paper background
- gentle cream surfaces
- deep charcoal text
- muted warm-gray secondary text
- restrained indigo-violet accent

Dark mode:
- graphite/midnight background
- elevated charcoal surfaces
- soft off-white text
- muted gray secondary text
- restrained indigo-violet accent

OLED mode:
- near-black/black canvas
- preserved hierarchy through tonal surfaces, separators, and typography

### Typography
Use Android-safe sans-serif typography with a strong editorial hierarchy. Avoid shipping external font binaries.

Default hierarchy:
- home/editor headline: ~30–34sp
- section title: ~22–26sp
- note title: ~18–19sp
- body: ~16–18sp
- metadata: ~12–14sp

Use comfortable line height and paragraph spacing. RTL/Urdu text must render naturally.

### Spacing
Central 4dp/8dp system:
- 4 micro
- 8 compact
- 12 related controls
- 16 standard
- 20 comfortable
- 24 section
- 32 major separation
- 40–48 editorial whitespace

### Shape
Use multiple radii by hierarchy; do not round everything identically.

### Elevation
Prefer tonal separation and spacing. Use visible elevation only for floating create controls, sheets, and transient editor toolbars.

### Motion
- 120–180ms micro interactions
- 180–260ms small transitions
- 250–350ms larger sheets
- prefer alpha/translation/scale over expensive effects
- respect reduced-motion settings where possible

## Navigation
Bottom navigation has exactly three destinations:
1. Notes
2. Folders
3. More

The editor is a destination outside the persistent bottom navigation.

Avoid a crowded navigation bar.

## Home / Notes Screen
Home must be the strongest first impression.

### Header
Editorial composition on the background canvas, not inside a card:
- natural contextual greeting such as Good morning / afternoon / evening
- primary heading such as `Your Notes`
- subtle note/pin count metadata

### Search
Use an integrated floating tonal search surface with no harsh border.

### Filters
Only compact primary filters on home:
- All
- Pinned
- Favorites
- Recent

Advanced filters remain secondary.

### Pinned
If pinned notes exist, show a horizontally scrollable premium pinned section with larger previews and restrained tinted surfaces.

### Recent / Main list
Default presentation is an editorial list, not Google Keep-style colored masonry.
Rows show:
- title
- short content preview or checklist preview
- up to two tags when available
- useful metadata such as timestamp/folder/pin/favorite/reminder
- locked notes never reveal body content

### Empty state
Minimal icon/mark + typography, no giant stock illustration.

### Create control
Signature floating pill: `＋ New note`.
Tap opens a polished create sheet with only working actions:
- Text
- Checklist
- Scratch Pad
- Photo if current attachment flow is stable
- Voice if current speech intent is available

## Editor
The editor is the hero experience.

### Layout
- minimal top row with back and a compact overflow/pin control
- generous vertical spacing
- large borderless title
- borderless body area
- no permanent giant toolbar
- subtle autosave state: `Saving…` / `✓ Saved`

### Behavior
Reuse current save/revision/link logic.
Preserve:
- autosave
- history
- tags
- folder assignment
- reminder
- time capsule
- expiry
- lock/unlock
- themes
- focus mode
- voice
- image/camera attachment
- drawing
- backlinks
- export
- templates
- slash commands
- quick summary/action items/related notes

Do not expose all actions simultaneously. Group them into polished sheets/menus.

### Editor toolbar
Compact floating toolbar above IME when appropriate:
- `Aa`
- checklist
- bullet/list
- insert

Only actions that actually map to existing command/body operations may be enabled.

### Focus mode
Visually quiet chrome without trapping the user.

### Scratch Pad
Distinct slightly warmer surface, immediate cursor/keyboard, minimal actions, continuous save.

## Search Screen
Dedicated premium search experience:
- focused search field immediately
- live results
- subtle match emphasis using accent tint, never bright yellow
- filters kept secondary
- result rows reuse editorial note treatment

Search must include title/body and current repository-supported metadata where available.

## Folders
Compose folder screen:
- editorial header
- supporting text
- elegant rows with folder name and note count
- subtle desaturated folder color identity if practical
- nested folders represented clearly without giant cards

Folder detail:
- folder title
- note count
- search within folder if repository support is stable
- same editorial note list

## More Screen
Replace generic settings clone with grouped sections:

ORGANIZE
- Favorites
- Tags
- Templates
- Archive
- Trash

PERSONALIZE
- Appearance
- Editor preferences if implemented

PRIVACY
- Privacy & Lock

DATA
- Backup & Export

ABOUT
- About UsmanNotepad

Rows use small icon, title, optional value, and chevron only when navigating.

## Appearance Screen
Must feel premium and screenshot-worthy.

Include:
- live miniature note preview
- Theme: System / Light / Dark / OLED
- Accent selection using restrained preset swatches
- Writing Style: Calm / Modern / Compact
- Text size: compact options or slider

Persist UI-only preferences locally using SharedPreferences or DataStore-compatible storage without touching note content.

### Density presets
Calm:
- more whitespace
- slightly larger typography

Modern:
- balanced default

Compact:
- smaller spacing / more rows visible

## Trash
Calm screen:
- explanation that deleted notes remain for the current V2 retention policy
- note title
- deletion metadata if available
- Restore
- Delete Forever with explicit confirmation

## Lock UX
Preserve current security mechanics.
Improve presentation:
- warm minimal lock surface
- `Welcome back`
- `Unlock to continue`
- device credential / biometric action where current V2 flow supports it
- locked-note rows never expose body
- hide-title preference remains supported

## RTL / Urdu
All note content must support English, Urdu, Roman Urdu, Arabic, and mixed-direction text.
- use natural text direction for editor content
- avoid forced LTR body alignment
- UI chrome may remain language-neutral English unless localization is added later

## Accessibility
- target 44dp+ touch areas
- semantic labels for icon-only actions
- support font scaling without layout breakage
- do not rely on color alone
- maintain readable contrast in all themes

## Performance
- no global blur/glass effects
- LazyColumn/LazyRow for lists
- stable keys for notes/folders
- avoid unnecessary recomposition
- local DB interactions should not show spinners unless genuinely slow

## Existing Functionality Preservation
V3 must not regress:
- create/edit/save/delete/restore
- V1/V2 note persistence
- SQLite data
- search
- folders/tags
- pin/favorite/archive
- recycle bin
- history
- templates
- scratch/daily/quick copy
- lock/encryption
- reminders/time capsule/expiry
- wiki links/backlinks/graph
- local summary/action/related tools
- share inbox
- voice
- image/camera/drawing
- widget
- import/export/backup

## Visual Review Gates
Three refinement passes are required before release:

### Pass 1 — Structure
Review hierarchy, navigation, content placement, density.

### Pass 2 — Visual polish
Review spacing, typography, colors, shapes, icons, light/dark/OLED contrast.

### Pass 3 — Delight
Review pressed states, transitions, empty states, snackbar/dialog microcopy, small alignment inconsistencies.

Because emulator screenshots may not be available in the connected GitHub environment, verification will combine Compose preview-safe structure, screenshot-oriented code inspection, build tests, and instrumentation/unit checks where feasible. No claim of pixel-perfect visual quality will be made without an actual rendered-device screenshot.

## Testing
Unit tests:
- theme preference mapping
- greeting/time label logic
- note preview privacy logic
- filter mapping
- formatting helpers
- appearance preset persistence logic where testable

Build verification:
- `testDebugUnitTest`
- `:app:assembleDebug`
- Android resource/manifest compile
- artifact upload

Manual/source-level acceptance:
- all visible buttons map to real flows
- no V2 data schema reset
- no INTERNET requirement added solely for UI
- locked notes do not expose content in Compose lists
- RTL-safe editor settings used

## CI / Release
GitHub Actions must:
1. checkout
2. JDK 17
3. SDK 35 / Build Tools 35.0.0
4. Gradle 8.11.1
5. run unit tests
6. assemble debug APK
7. collect as `UsmanNotepad-V3.00.apk`
8. upload artifact named exactly `UsmanNotepad-APK`

Final artifact must be downloaded and inspected as a valid APK with signing block, AndroidManifest.xml and classes.dex.

## Non-Goals for V3
- Room migration
- cloud account/sync
- self-hosted server
- OpenAI/external AI API
- bundled LLM
- OCR overhaul
- call monitoring
- persistent overlay
- unnecessary feature expansion that compromises polish

## Acceptance
V3 is accepted when:
- installs as version 3.0.0 / code 3 under the same app ID
- existing V2 data remains accessible
- primary screens are Compose-based and follow Soft Editorial Minimalism
- light/dark/OLED and accent/density preferences work
- Home, Editor, Search, Folders, More, Appearance, Trash are visually coherent
- all exposed actions work
- unit tests pass
- debug APK builds successfully in GitHub Actions
- final `UsmanNotepad-APK` artifact contains `UsmanNotepad-V3.00.apk`
