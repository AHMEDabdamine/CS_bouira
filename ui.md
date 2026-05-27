

You are an expert Android UI/UX designer and Kotlin/Jetpack Compose engineer. Completely redesign the UI of my Android app called **CS Bouira** — an academic document viewer for a Computer Science university department at Université de Bouira, Algeria.

---

## What the app does (do not change any logic)

Students navigate: Years → Semesters → Modules → Files (courses, exams, TDs/TPs, tests, résumés). Files are fetched live from `api.csbouira.xyz` (Google Drive proxy) and displayed in-app via WebView. There are also Bookmarks and Downloads screens.

## Tech stack (keep all of this, only modify UI code)

- Kotlin + Jetpack Compose (BOM 2024.02)
- Material 3 — but subvert and extend it aggressively
- Navigation Compose 2.7.7 with animated transitions
- ViewModel + StateFlow, Room, Retrofit + Gson, OkHttp, WebView
- material-icons-extended

Only touch files inside `ui/` and `ui/theme/`. Leave all `data/` layer code (API, Room, Repository, models) completely untouched.

---

## Design direction: Dark & Sleek, Glassmorphism, Full Creative Freedom

Do NOT produce a generic Material You redesign. This should look like a premium, opinionated product — think Linear, Raycast, or a high-end fintech app, but built for a university document browser. Every screen should feel intentional and cohesive.

---

## Color palette

Define everything in `ui/theme/Color.kt` and wire it into a `DarkColorScheme` in `ui/theme/Theme.kt`. No light theme needed.

- Background: `#0A0A0F` (deep navy-black)
- Surface: `#12121A` (cards), `#1A1A28` (elevated surfaces)
- Primary accent: `#6C63FF` (electric indigo/violet) — used sparingly
- Secondary accent: `#00D4FF` (cyan) — for interactive highlights and active states
- On-background text: `#F0F0FF` (primary), `#8888AA` (secondary/muted)
- Error: `#FF4F6A` with a soft red glow on error states
- Never use pure `#000000` or pure `#FFFFFF`

---

## Global components — create these as reusable composables

**`ui/components/GlassCard.kt`**
A composable that renders a frosted-glass card. Use `Modifier.background` with a low-alpha white brush combined with `BlurMaskFilter` or `Modifier.graphicsLayer` for the blur effect. The card should feel like it's floating over the dark background. Accept `modifier`, `onClick`, and `content` parameters.

**`ui/components/ShimmerBox.kt`**
Replace every `CircularProgressIndicator` loading state with a shimmer skeleton. Animate a `Brush.linearGradient` with `InfiniteTransition` sweeping from dark to slightly lighter dark, matching the card shapes of whichever screen is loading.

**`ui/components/GlowButton.kt`**
A filled button where the background is a gradient from primary to secondary accent, and a soft colored shadow is drawn behind it using `drawBehind` with a blurred `Paint`. Used for all primary CTAs.

---

## TopAppBar (global)

- Transparent background when at top of scroll
- When scrolled: a frosted glass backdrop using `BlurMaskFilter` or a dark semi-transparent overlay with `backdropFilter`-style effect
- Title text rendered with `Brush.linearGradient` from primary to secondary accent (gradient text via `TextStyle`)
- Back button icon in muted color, brightens on press

---

## Bottom navigation

If the app has bottom nav or a drawer, replace it with a compact **floating bottom bar**:
- Pill-shaped container with `#1A1A28` background and a subtle border stroke in `#6C63FF` at 20% alpha
- Icon-only navigation items
- Selected state: a glowing pill indicator behind the icon using `drawBehind` with a blurred circle in the primary accent color
- Animate indicator movement between tabs with `animateFloatAsState`

---

## Screen 1 — Home (Year & Semester selection)

**Year selection:**
- Replace any plain list with large glassy cards laid out in a `LazyColumn`
- Each card spans full width, ~160dp tall, with a subtle `Brush.linearGradient` background going from `#1A1A28` to a faint tint of the primary accent
- Display the year label in bold 28sp, a small decorative icon (a graduation cap or a stack of books from `material-icons-extended`), and a faint large number watermark in the background (e.g. "L1" at 80sp, 5% opacity)
- Cards animate in with staggered `fadeIn + slideInVertically` using `LaunchedEffect` index-based delays

**Header:**
- At the top of the screen, a large greeting section: "CS Bouira" in 32sp bold gradient text, below it the current date formatted in French (e.g. "Mercredi, 28 Mai 2026") in muted 14sp
- Add a subtle horizontal divider with a gradient from transparent → accent → transparent

**Semester selection (after year is picked):**
- Shown as two large pill-shaped toggle buttons side by side ("Semestre 1" / "Semestre 2")
- Selected pill: gradient fill from primary to secondary accent with a glow shadow
- Unselected: outlined with a muted stroke, dark fill
- Animate the selection change with `animateColorAsState`

---

## Screen 2 — Module list

- Use `LazyVerticalStaggeredGrid` with 2 columns
- Each module card is a `GlassCard` with varying heights (staggered effect) — vary height based on module name length
- Inside each card: a colored icon (derive the icon and its color from the module name — e.g. math → a function icon in cyan, algo → a tree icon in violet), the module name in 15sp semi-bold, and a faint type label below in 11sp muted text
- Cards have a thin top border stroke in the accent color at 40% alpha
- On press: scale down to 0.96f using `animateFloatAsState` with `interactionSource`, then navigate

---

## Screen 3 — File list

- `LazyColumn` with horizontal swipeable file rows
- Each row is a `GlassCard` containing:
    - Left: a colored rounded square icon badge (color derived from file type — PDF → red-orange, image → cyan, doc → indigo, quiz/test → amber)
    - Center: file name in 14sp, file type label in 11sp muted
    - Right: a download icon button that triggers progress, replaced by a checkmark when done
- A **sticky header** showing the module name with a frosted glass background using `graphicsLayer` alpha + a `BlurMaskFilter` applied to the backdrop
- A floating `GlowButton` FAB in the bottom-right: "Tout télécharger" with a bookmark+download icon, with a pulsing glow animation using `InfiniteTransition`

---

## Screen 4 — File viewer

- Near full-screen WebView with minimal chrome
- Top bar: starts fully visible, fades out as the user scrolls down using `derivedStateOf` + `animateFloatAsState` on the bar's alpha
- Bottom controls: a floating pill bar (not a standard bottom bar) containing Share, Bookmark, and Download icon buttons, rendered as a `GlassCard` with a subtle border, positioned just above the system navigation bar using `WindowInsets`
- When bookmarked: the bookmark icon fills with a gradient and emits a brief glow pulse animation

---

## Screen 5 — Bookmarks & Downloads

**Empty state:**
- Centered in the screen: a custom `Canvas` composable drawing a simple illustration — a stack of 3 document rectangles with rounded corners, a small bookmark ribbon on the top-right corner of the top document, all drawn in muted accent colors
- Below it: a title in 18sp "Aucun favori pour l'instant" and a subtitle in 14sp muted
- A `GlowButton` below: "Parcourir les modules"

**Filled state:**
- `LazyColumn` with `item` keys set to file IDs for correct `AnimatedVisibility` behavior
- Each item: a card with a 4dp left-side accent strip whose color matches the file type (same color map as Screen 3)
- Swipe-to-delete: use `SwipeToDismiss` — on swipe, wrap removal in `AnimatedVisibility` with `shrinkVertically + fadeOut` for a smooth animated removal
- A subtle "dernièrement ajouté" timestamp in muted text below each file name

---

## Animation rules (apply globally)

- List items enter with `fadeIn(tween(300)) + slideInVertically(tween(300))` with staggered delays per index
- Screen-to-screen transitions: `slideInHorizontally + fadeIn` / `slideOutHorizontally + fadeOut` (already in NavHost — keep and refine)
- All pressable surfaces: scale to `0.96f` on press using `animateFloatAsState`
- Color/state changes: always use `animateColorAsState` with a `tween(200)` spec, never instant snaps

---

## Delivery order

Produce the code in this exact order so the design system is established before any screen:

1. `ui/theme/Color.kt`
2. `ui/theme/Theme.kt`
3. `ui/components/GlassCard.kt`
4. `ui/components/ShimmerBox.kt`
5. `ui/components/GlowButton.kt`
6. Home screen
7. Module list screen
8. File list screen
9. File viewer screen
10. Bookmarks screen
11. Downloads screen

After each file, wait for my confirmation before continuing to the next. If you need to make assumptions about existing composable function signatures, state them clearly at the top of each file.

---