---
name: Nocturnal Clarity
colors:
  surface: '#0f131d'
  surface-dim: '#0f131d'
  surface-bright: '#353944'
  surface-container-lowest: '#0a0e18'
  surface-container-low: '#171b26'
  surface-container: '#1c1f2a'
  surface-container-high: '#262a35'
  surface-container-highest: '#313540'
  on-surface: '#dfe2f1'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#dfe2f1'
  inverse-on-surface: '#2c303b'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#ffb95f'
  on-secondary: '#472a00'
  secondary-container: '#ee9800'
  on-secondary-container: '#5b3800'
  tertiary: '#ffb3ad'
  on-tertiary: '#68000a'
  tertiary-container: '#ff7a73'
  on-tertiary-container: '#79000e'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#ffddb8'
  secondary-fixed-dim: '#ffb95f'
  on-secondary-fixed: '#2a1700'
  on-secondary-fixed-variant: '#653e00'
  tertiary-fixed: '#ffdad7'
  tertiary-fixed-dim: '#ffb3ad'
  on-tertiary-fixed: '#410004'
  on-tertiary-fixed-variant: '#930013'
  background: '#0f131d'
  on-background: '#dfe2f1'
  surface-variant: '#313540'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 18px
    letterSpacing: 0.04em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.25rem
  space-xs: 0.375rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.25rem
---

## Brand & Style

This design system is engineered for high-stakes, low-light behavioral monitoring and harm reduction. Its aesthetic balances an immersive nightlife mood with uncompromising, immediate clarity. The experience feels like a high-end personal concierge rather than an austere medical utility. 

Key attributes:
- **Tone:** Vigilant, non-judgmental, protective, and sleek.
- **Visual Style:** Modern Dark Glassmorphism infused with high-contrast tactical neon accents. Deep slate backdrops absorb glare, while luminous status indicators command attention even through sensory overload or physical fatigue.
- **Physicality:** Extra-generous tap targets, prominent visual hierarchies, and tactile feedback metaphors designed explicitly for compromised fine-motor skills and dim venue lighting.

## Colors

The palette uses absolute darkness as its canvas, deploying targeted chromatic energy to communicate cognitive safety tiers instantly:

- **Base Slate (Neutral - `#0B0F19`, `#111827`, `#1F2937`):** Forms the foundational canvas, modal sheets, and glass surfaces. These tones minimize OLED battery consumption and eliminate screen glare in pitch-black settings.
- **Sober & Safe (Primary - `#10B981` Emerald):** Represents control, baseline sobriety, safe transportation windows, and validated cognitive status.
- **Cautionary Limit (Secondary - `#F59E0B` Amber / `#FB923C` Warm Orange):** Signifies the transition zone, metabolization delays, hydration advisories, and approaching BAC limits.
- **Lockdown & Danger (Tertiary - `#EF4444` Crimson / `#F43F5E` Electric Rose):** Applied exclusively to intoxication locks, designated contact emergency pings, vehicle key lock-boxes, and failed sobriety checks.
- **Text & Accents:** High-legibility off-white (`#F9FAFB`) for primary text and muted cool gray (`#9CA3AF`) for auxiliary labels to maintain stark typographic contrast.

## Typography

Typography prioritizes rapid decipherability under visual distortion or reduced cognitive bandwidth.

- **Primary Typeface:** Plus Jakarta Sans is implemented across all roles for its wide apertures, distinct geometric curves, and tall x-height, which together resist blur in dark, high-vibration nightclub environments.
- **Hierarchical Discipline:** Numbers, BAC metrics, and timer count-downs use `display-lg` and `display-lg-mobile` with tight tracking and heavy weights (`800`) to enable glanceable consumption from arm's length.
- **Labels:** Micro-copy, alert tags, and drink taxonomy metadata utilize uppercase styling with wide tracking (`0.04em` to `0.05em`) to prevent adjacent characters from bleeding together on high-brightness OLED displays.

## Layout & Spacing

The layout is built on a mobile-first fluid single-column structure that expands up to a maximum container width of 480px on larger displays, keeping all interactive triggers within immediate thumb reach.

- **Touch Ergonomics:** All primary interactive areas (logging actions, emergency triggers, cognitive check-ins) conform to a minimum 56px height, insulated by generous outer margins (`margin`: `1.25rem`) to eliminate edge-screen tap failures.
- **Rhythm:** Spacing follows a modular 4px/8px rhythm. Component internal padding strictly isolates dense vital stats (`space-sm`) from actionable touch perimeters (`space-lg`), giving visual breathing room to high-stress screens.

## Elevation & Depth

Visual depth is achieved through layered frosted glass and luminous edge-diffusions rather than traditional drop shadows:

- **Surface 0 (Base Canvas):** Solid `#0B0F19`.
- **Surface 1 (Card Backdrop):** Translucent `#111827` at 70% opacity with a `20px` background blur filter and a `1px` subtle rim border (`rgba(255, 255, 255, 0.08)`).
- **Surface 2 (Floating Action Trays / Modals):** Translucent `#1F2937` at 85% opacity with a `28px` background blur, edged with a `1px` gradient border reflecting the active status color.
- **Luminescence (Neon Glow):** Danger and active safety metrics emit a soft ambient halo (`box-shadow: 0 0 24px -4px var(--status-color-alpha-30)`), indicating interactive readiness without causing visual clutter.

## Shapes

The geometric personality features oversized, pill-influenced curvatures to communicate comfort, organic safety, and tactile softness.

- **Standard Containers:** Cards, modal sheets, and system drawers utilize standard rounded corners (`rounded-lg`, 16px to 20px).
- **Interactive Elements:** Action chips, quick incrementers, status badges, and emergency controls utilize full pill radius (`9999px`) to invite natural finger presses and prevent sharp visual boundaries.

## Components

### Action Triggers & Buttons
- **Primary "+1 Drink" Button:** A high-profile 64px tall pill or floating circle element. Features a high-contrast gradient fill with an active tactile scale transform (`scale(0.96)`) on press.
- **Emergency Slide-to-Unlock:** Full-width friction track with an illuminated thumb toggle requiring sustained directional swipe to unlock apps or initiate cab calls, preventing accidental misfires.

### Glassmorphism Cards
- Encased within 16px rounded borders with a delicate 1px light-catching perimeter (`rgba(255, 255, 255, 0.08)`). Content retains high contrast text against the dark, semi-transparent background.

### Glowing Status Rings
- Concentric circular SVG meters tracking real-time Blood Alcohol Concentration (BAC) dissipation and sober countdowns. Rings utilize a clean linear gradient terminating in a soft glowing drop-shadow tip.

### Chips & Taxonomies
- 36px tall drink selector chips (Beer, Wine, Spirits, Water) featuring distinct iconographic pairings, subtle border styling when unselected, and high-visibility neon backfills when active.

### Checkboxes, Radios & Inputs
- Scaled up to 28px square/circle footprints with bold 3px internal borders. Checked states fill with the emerald primary tone and an emphatic white glyph. Text inputs utilize 56px height with inset dark backgrounds (`#0B0F19`) and prominent active border rings.

### Cognitive Check-in Interfaces
- Reaction-time pads, pattern matchers, and balance inputs built with minimum 72px touch targets to remain accessible under impaired conditions.