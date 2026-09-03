# UsmanNotepad V3.00 Premium UI Design

## Goal
Upgrade UsmanNotepad V2 into V3.00 with a premium, distinctive Android interface built around the approved **Soft Editorial Minimalism** direction while preserving the existing local-first data model, note content, encryption, reminders, backup/import/export, attachments, folders, tags, history, graph, quick capture, and migration behavior.

## Product Rule
V3 is a visual-system and interaction upgrade, not a data rewrite. Existing user notes and V2 functionality remain the source of truth. If a visual control cannot be wired to a working feature, it is not shown.

## Version and Build Baseline
- applicationId: `com.usman.notepad`
- versionCode: `3`
- versionName: `3.0.0`
- minSdk: `23`
- targetSdk: `35`
- compileSdk: `35`
- Build Tools: `35.0.0`
- JDK: `17`
- AGP: `8.9.2`
- Gradle: `8.11.1`
- Kotlin: `2.2.10`
- Compose compiler plugin: Kotlin-matched `2.2.10`
- Compose BOM: `2025.05.01` (Compose 1.8.x family)

The project deliberately does not jump to Compose 1.12/API 37 because the existing CI pipeline is proven on SDK 35 and the V3 objective is a reliable installable APK. Newer UI libraries that require AGP 9/API 37 are deferred until the Android SDK pipeline is intentionally upgraded.

## Architecture
V3 uses a hybrid architecture:

1. Keep the existing Java SQLite repository, models, security, reminder, backup, widget, and utility code where it is already correct.
2. Add a Kotlin/Jetpack Compose presentation layer for the primary screens.
3. Expose the existing Java repository directly to Kotlin through a thin `V3RepositoryAdapter` rather than replacing SQLite with Room.
4. Store appearance preferences using lightweight SharedPreferences/DataStore-compatible settings without altering note storage.
5. Keep legacy Java Activities available only for secondary flows that are expensive or risky to rewrite immediately, but the visible V3 navigation routes the user through Compose-first screens.

## Design Identity — Soft Editorial Minimalism
The visual system is recognizable through four recurring elements:

1. Editorial typography with strong hierarchy.
2. Warm paper-like light surfaces and quiet graphite dark surfaces.
3. Restrained indigo-violet interactive accent.
4. Signature floating `+ New note` control.

Beauty comes from proportion, spacing, typography, rhythm, and subtle tonal separation. Avoid card soup, icon soup, default Material purple, large gradients, bright rainbow notes, heavy elevation, and tutorial-project composition.

## Theme System
Create a centralized Compose theme package with:

- `UsmanNotepadTheme`
- `UsmanColors`
- `UsmanTypography`
- `UsmanShapes`
- `UsmanSpacing`
- `UsmanMotion`

### Light
- warm ivory canvas
- cream/soft paper surfaces
- deep charcoal primary text
- warm gray secondary text
- indigo-violet accent

### Dark
- graphite/midnight canvas
- slightly lifted charcoal surfaces
- soft off-white primary text
- muted gray secondary text
- restrained indigo-violet accent

### OLED
- true-black canvas
- subtle tonal hierarchy for controls and navigation
- no neon glow

### Accent presets
Provide a restrained set of accents: indigo (default), violet, sage, rose, sky, graphite. The accent changes interactive state, cursor/selection, active chips, and primary CTA—not the entire interface.

### Writing density presets
- Calm: larger type and spacing
- Modern: balanced default
- Compact: tighter spacing and more visible notes

## Home / Notes Screen
Home is the strongest first impression.

### Header
- compact brand identity, not a giant logo
- contextual greeting based on local time: Good morning / afternoon / evening
- editorial heading such as `Your thoughts, beautifully organized.`
- live note/pinned count

### Search
- integrated floating tonal search surface
- no harsh border
- clear focus state
- voice icon only if existing voice search/capture is actually wired

### Filters
Compact chips only for:
- All
- Pinned
- Favorites
- Recent

Advanced filtering remains in a dedicated filter sheet.

### Pinned section
When pinned notes exist, show a horizontal row of larger premium previews with title, short body excerpt, optional folder label, and timestamp.

### Recent section
Default notes presentation is an **Editorial List**, not Google Keep-style masonry. Rows rely on typography, whitespace, metadata, and subtle separators. Locked notes show no body preview.

### Note row adaptations
- Text: title, preview, metadata
- Checklist: title, up to three checklist lines and completion count when detectable
- Image note: tasteful small thumbnail only if a local image attachment is available
- Locked: private placeholder, no content leak

### Create control
Floating premium pill `+ New note` at rest; it may collapse to a circular `+` while scrolling down and expand on upward scroll. Press opens the create sheet.

### Create sheet
Working options only:
- Text
- Checklist
- Scratch Pad
- Photo
- Voice

Each option has icon, title, one-line explanation.

## Bottom Navigation
Exactly three top-level destinations:
- Notes
- Folders
- More

The bottom bar is tonal, compact, inset-safe, and hidden when the editor keyboard is active.

## Editor — Hero Experience
The editor is the most polished screen.

### Layout
- edge-to-edge canvas
- top row: Back, optional pin state, More
- large borderless title field
- borderless body editor with comfortable line height
- no permanent oversized formatting toolbar
- subtle save state `Saved` / `Saving…`

### Writing behavior
- existing autosave behavior remains intact
- screen should stay stable when IME opens
- RTL and mixed Urdu/English text supported naturally
- no forced English alignment
- focus mode reduces chrome prominence rather than confusingly hiding navigation

### Bottom editor toolbar
Compact floating toolbar above the keyboard with working actions:
- `Aa` format/structure sheet
- Checklist
- Bullets
- Insert `+`

The insert sheet exposes only existing implemented actions: checklist, divider, quote, image, date, reminder, linked note, drawing.

### More menu
Preserve existing V2 tools such as history, tags, folders, reminder, time capsule, expiry, lock, theme, voice, attachments, export, backlinks, template save, delete. Secondary actions may remain in a Compose sheet that invokes existing Java-backed operations.

## Scratch Pad
Dedicated warm-paper Compose surface:
- label `Scratch`
- no title required
- cursor focused immediately
- autosaves continuously
- compact `Discard` and `Convert to note` actions

## Search Experience
Dedicated full-screen Compose search:
- focused search input at top
- live local results from existing repository
- subtle accent treatment for matched phrases
- note title, relevant preview, folder/tag metadata, timestamp
- no bright yellow highlight
- empty state: `Nothing matched “…”`

No command/settings search is included unless fully wired and stable.

## Folders
Compose folder browser:
- editorial header and supporting line
- nested folders shown through indentation/hierarchy
- tasteful desaturated folder accent tokens
- note count per folder
- folder detail shows search-within-folder and editorial note list

Existing repository folder operations remain authoritative.

## More Screen
Replace the current settings-dialog feeling with grouped navigation:

### ORGANIZE
- Favorites
- Tags
- Templates
- Archive
- Trash

### PERSONALIZE
- Appearance
- Editor preferences

### PRIVACY
- Privacy & Lock

### DATA
- Backup & Export

### ABOUT
- About UsmanNotepad

Rows are grouped by spacing and subtle separators, not individual cards.

## Appearance Screen
A Play-Store-quality screen with:
- miniature live preview at top
- Theme: System / Light / Dark / OLED
- Accent selector
- Writing Style: Calm / Modern / Compact
- Text size control

Changes apply immediately and persist.

## Privacy Lock Surface
If app lock is enabled, the entry experience should show a minimal warm lock surface before invoking the existing PIN/device credential logic. Per-note locked previews never expose note body.

## Trash
Compose trash screen:
- calm title/subtitle
- deleted timestamp / remaining retention when available
- Restore
- Delete Forever with confirmation

## Empty States
Use minimal iconography and strong copy. No giant generic illustration.

Examples:
- `No notes yet` / `Your first thought can start here.`
- `Nothing here yet`
- `Nothing matched “invoice”.`

## Motion
- micro interactions: 120–180 ms
- navigation/small transitions: 180–260 ms
- sheets: 250–350 ms
- prefer alpha/translation/scale
- no slow decorative animation
- honor reduced motion where practical

## Haptics
Use small haptics only for high-value interactions: long press, pin/favorite, create note, destructive confirmation, and selected appearance options.

## Edge-to-Edge and Insets
- status bar visually merges with surface
- icon contrast follows theme
- bottom navigation respects gesture/navigation insets
- editor toolbar respects IME insets

## Accessibility
- 44dp+ touch targets where possible
- meaningful content descriptions for icons
- dynamic font compatibility
- no state communicated only with color
- large-text layouts remain usable
- dark/light contrast remains readable

## RTL / Urdu
- support English, Urdu, Roman Urdu, Arabic, and mixed text
- text direction uses content-driven behavior
- note titles/body are not hardcoded left aligned
- UI layout remains usable under RTL locales

## Existing Functionality Preservation
The following V2 capabilities remain available and connected:
- create/edit/save/delete
- SQLite local storage and V1 migration
- search
- folders/tags
- pin/favorite/archive/trash
- scratch/daily notes
- quick copy
- templates
- history
- reminders/time capsule/expiry
- app/per-note lock and Keystore encryption
- wiki links/backlinks/graph
- offline summary/action/related heuristics
- share-to-inbox
- voice capture
- image/camera attachment
- drawing
- widget
- import/export/backup

## Data Safety
- do not change the existing database name or destructive schema behavior for V3 visual migration
- same application ID permits upgrade installation over V2
- appearance preferences are additive
- no note content is deleted during V3 migration
- if the Compose layer fails to parse optional metadata, it falls back to neutral presentation rather than mutating data

## Testing
### Unit
- appearance preference serialization
- note preview formatting
- checklist progress extraction
- filter mapping to repository calls
- title/body privacy masking

### UI/Instrumentation where feasible
- Home renders notes from repository
- filter chips update list
- search opens note
- create sheet routes to working note types
- editor autosave route persists content
- theme changes persist
- locked note preview hides body

### Build verification
CI must:
1. run unit tests
2. build `:app:assembleDebug`
3. copy output to `UsmanNotepad-V3.00.apk`
4. upload artifact exactly `UsmanNotepad-APK`
5. fail if APK missing

## Visual Review
At least three code-level refinement passes are required:
1. Structure: hierarchy, navigation, content placement
2. Polish: typography, spacing, shapes, colors
3. Delight: pressed states, motion, empty states, microcopy

Because CI has no emulator screenshot pipeline, visual review is enforced by centralized design tokens and manual composition review of all major screens; no claim of pixel-perfect device rendering is made without actual device/emulator screenshots.

## Deferred from V3.00
- Room migration
- cloud/self-hosted sync
- real on-device LLM
- OCR
- background location reminders
- floating overlay
- after-call popup
- complex shared-element transitions
- Compose 1.12/API 37 toolchain upgrade

## Acceptance Criteria
- installing V3 over V2 retains existing notes and app data
- app reports version 3.0.0 / versionCode 3
- Home, Editor, Search, Folders, More, Appearance, and Trash use the V3 Compose design language
- light, dark, and OLED modes are intentionally designed
- visible controls are functional
- existing core V2 features remain reachable
- unit tests pass
- debug APK compiles and is signed
- GitHub Actions completes successfully and publishes `UsmanNotepad-APK`
