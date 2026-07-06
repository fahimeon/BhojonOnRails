# BhojonOnRails — Engineering Audit & Premium Redesign Plan

> Audit date: 2026-07-06 · Scope: full codebase (40 Java files, 17 FXML views, 1 CSS file, SQLite DB)
> Role: senior backend engineer + UI/UX designer review. **Plan only — no code changed.**

---

## 1. Backend Flaws

### 1.1 Security — Critical 🔴

| # | Flaw | Where | Why it matters | Fix |
|---|------|-------|----------------|-----|
| S1 | **Live Gmail app password committed** (`j84531918@gmail.com` / app password) | `service/EmailService.java:18-19` | Already public on GitHub; anyone can send mail as this account. History retains it forever. | Revoke the app password NOW. Move creds to a gitignored `config.properties` / env vars. Purge history (`git filter-repo`) if you care. |
| S2 | **OCR.space API key committed** | `service/OCRSpaceService.java:18` | Public; quota can be burned by anyone. | Regenerate key; load from config. |
| S3 | **Hardcoded admin credentials** `admin@gmail.com` / `admin@gmail.com` compared in plaintext | `controller/AdminLoginController.java:43` | Anyone reading the repo is admin. Password == email is guessable anyway. | Store an admin row in DB with a real hash; seed on first run. |
| S4 | **Password "hashing" is `String.hashCode()`** — 32-bit, unsalted, trivially collidable and reversible by brute force in milliseconds | `data/DatabaseHelper.java:545-548` | Every restaurant-owner password is effectively plaintext. | Use BCrypt (`org.mindrot:jbcrypt`) or Argon2. Migrate existing rows on next login. |
| S5 | No brute-force protection, no session timeout, `RestaurantSession` is a global singleton that never expires | `util/RestaurantSession.java` | Low stakes for a demo, but trivial to add a lockout counter. | Add attempt counter + lockout; clear session on logout everywhere. |

### 1.2 Architecture & Correctness 🟠

| # | Flaw | Where | Detail |
|---|------|-------|--------|
| A1 | **`new DatabaseHelper()` constructed 9 times**, and every construction runs the full `CREATE TABLE` + `ALTER TABLE` + station-seeding routine. Startup log shows `Database initialized...` **8 times**. | 9 call sites across controllers/`DataManager` | Make it a single shared instance (or at least make `initializeDatabase()` a one-time static guard). Each call also opens a fresh JDBC connection per query — fine for SQLite demo scale, but wasteful. |
| A2 | **Relative DB path** `jdbc:sqlite:railkhabar.db` and relative `uploads/`, `tessdata/` paths | `DatabaseHelper.java:14`, `FileUtil` | App breaks (silently creates empty DBs) when launched from any other working directory — this already bit you in IntelliJ. Resolve to `%APPDATA%/BhojonOnRails/` or `user.home`. |
| A3 | **Two parallel data sources that disagree**: `DataManager` hardcodes trains/restaurants/food in Java while `DatabaseHelper` serves the same entities from SQLite, glued with a `+1000` ID-offset hack | `DataManager.java` vs `DatabaseHelper.java:311, 499` | Single source of truth: seed everything into SQLite on first run, delete the hardcoded lists and the offset hack. This is the biggest refactor and the most valuable. |
| A4 | **God class**: `DatabaseHelper` is ~550 lines mixing schema migration, orders, stations, owners, food items, auth | `data/DatabaseHelper.java` | Split into `OrderDao`, `StationDao`, `RestaurantDao`, `FoodItemDao` + one `Database` (connection/schema) class. |
| A5 | **Raw `new Thread(...)`** for OCR and email instead of JavaFX `Task`/`Service` | `TicketUploadController.java:119`, `OrderConfirmationController.java:136` | No cancellation, no unified error handling, threads leak if user navigates away mid-operation. Use `Task` + a shared `ExecutorService`. |
| A6 | **No logging framework** — 19 `printStackTrace()` calls, `System.out/err` everywhere | project-wide | Add SLF4J + Logback (or at minimum `java.util.logging`). Errors currently vanish when not run from a console. |
| A7 | **Swallowed exceptions & boolean error returns** — e.g. `ALTER TABLE` failure silently ignored; `saveOrder` returns `false` and the cause is lost | `DatabaseHelper.java:72-76` etc. | Ignore-if-exists is OK but check the message; elsewhere, propagate typed exceptions so the UI can show *why*. |
| A8 | **Dead heavyweight dependency**: `tess4j` (native Tesseract, ~30MB with `tessdata/eng.traineddata` committed) is declared in `pom.xml` and `module-info.java`, but OCR is actually done via the OCR.space **web API**. It also causes the "filename-based automodule" build warning. | `pom.xml:42-46`, `module-info.java:5`, `tessdata/` | Remove tess4j + tessdata entirely, or keep it as a deliberate offline fallback — not both by accident. |
| A9 | **Typo bug**: food image URL `"hhttps://..."` (double h) → broken image at runtime | `DataManager.java:148` | Fix URL; better, see U12 (bundle images locally). |
| A10 | **Hotlinked runtime images** from `i.pinimg.com` — all food items share literally the same Pinterest JPG; offline = broken UI | `DataManager.java:125-152` | Bundle curated images as resources or store per-item files in the uploads dir. |
| A11 | **Un-synchronized lazy singletons** (`DataManager`, `CartManager`, `NavigationManager`) | `getInstance()` in each | Race is unlikely on the FX thread but background threads (A5) do touch them. Use eager init or a holder idiom. |
| A12 | **No transactions** for multi-step writes; no `UNIQUE` constraints beyond owner email (stations can duplicate if seeding races) | `DatabaseHelper` | Wrap seed + multi-insert in a transaction; add `UNIQUE(code)` on stations. |
| A13 | **Zero tests** despite JUnit 5 on the classpath | `pom.xml:49-61` | Start with pure-logic targets: `CartManager` totals, OCR text parsing regexes, DAO round-trips on a temp DB. |
| A14 | **Repo hygiene**: `railkhabar.db`, `uploads/`, `error_log.txt`, stray `HeroSection.tsx` (React file!) and `TestDB.java` committed at root | project root | Add to `.gitignore`, remove from repo. |
| A15 | **Old platform**: JavaFX 17.0.6 (2022), javafx-maven-plugin 0.0.8; no `jpackage` distribution story | `pom.xml` | Bump to JavaFX 21 LTS, plugin 0.0.8→newer; add `jpackage` profile so the app ships as a real Windows installer — that alone makes it feel "premium". |
| A16 | **Email failures are invisible to the user** — `sendOrderConfirmation` catches, prints, and moves on; customer thinks an email is coming | `EmailService.java:80-83` | Surface a non-blocking toast: "Confirmation email could not be sent". |
| A17 | Navigation history stack grows unbounded; every `navigateTo` re-parses FXML from scratch (state loss on Back) | `util/NavigationManager.java` | Acceptable for app size, but cap the stack and consider caching heavy views (food menu). |

---

## 2. UI/UX Flaws

### 2.1 The core problem: **five competing design languages**

`styles.css` documents its own identity crisis — each section header declares a different palette:

| Screen group | Palette declared in CSS | Accent |
|---|---|---|
| Legacy screens (buttons, lists) | Material teal/green | `#009688` |
| Admin dashboard | "SaaS" indigo | `#4f6ef7` + green + purple |
| Restaurant dashboard | Different navy + teal | `#0ea5a0` + orange |
| Role selection | Purple + azure | `#5b4ff7`, `#0B6EF5` |
| Passenger flow / login / menus | Tailwind blue | `#2563eb`, `#3b82f6` |

Result: navigating the app feels like visiting five different products. **This is the #1 thing to fix for a premium feel** — see §3 for the single brand palette.

### 2.2 Specific flaws

| # | Flaw | Evidence | Fix direction |
|---|------|----------|---------------|
| U1 | **191 inline `style="..."` attributes** across 17 FXML files (admin-dashboard 35, restaurant-dashboard 30, order-confirmation 20). Colors are baked into markup — a re-theme requires editing every screen. | grep count per file | Move every inline style into `styles.css` classes; then a palette swap = one file. |
| U2 | **Typography is split-brained**: Plus Jakarta Sans (5 weights) is loaded at startup, but the CSS overrides it with `'Segoe UI'` in 6 places (`ts-page-title`, `menu-root`, `login-root`, `rs-brand-rail`, `fm-page-title`…). | `styles.css:1183,1341,1451,1512,862` vs `HelloApplication.java:14-23` | One `.root` font-family declaration, delete all per-screen overrides. Use the loaded Jakarta weights via `-fx-font-weight`. |
| U3 | **Contrast failures (WCAG AA)**: subtitle/muted grays `#9ca3af` (~2.8:1), `#94a3b8`, `#8b96b5` on white all fail 4.5:1 for body text. | `rs-card-subtitle`, `admin-header-sub`, `pf-side-desc`… | New palette's muted text is `#55645C` (≥5:1 on cream/white). |
| U4 | **Focus rings deliberately stripped** (`-fx-focus-color: transparent`) with no replacement | `styles.css:1632-1636` | Replace with a visible 2px brand-color focus ring. Keyboard users currently navigate blind. |
| U5 | **No pressed/disabled states anywhere** — only `:hover` exists; buttons feel dead on click | whole CSS (zero `:pressed` rules) | Define `:pressed` (darken + translate-y 1px) and `:disabled` (40% opacity) for every button class. |
| U6 | **Default JavaFX `Alert` dialogs** (8 usages) clash with the styled app — gray Windows chrome mid-flow | 8 controllers | Custom styled dialog component + non-blocking toast/snackbar for successes. |
| U7 | **Emoji used as nav icons** (sidebar `nav-icon` labels) render inconsistently across Windows versions and can't be recolored | dashboard FXMLs | Adopt **Ikonli** (FontAwesome/Material) — vector icons, colorable via CSS, one dependency. |
| U8 | **Rigid layouts**: sidebar locked to 220px, `rs-card` locked to 300px, headers max-height 66–68px, app force-maximized at startup | `styles.css:242-243,947-948`, `HelloApplication.java:32` | Use min sizes + grow constraints; let the window be resizable sanely; test at 1280×720. |
| U9 | **No loading feedback pattern**: OCR shows a text label; buttons don't show progress; no skeletons | `TicketUploadController` | Standard pattern: button → spinner-in-button + disabled form; skeleton cards for lists. |
| U10 | **No designed empty states** (empty cart, station with zero restaurants, admin with no pending owners) | all list views | Friendly illustration + one-line copy + primary action. Empty states are where "fun" lives. |
| U11 | **Splash is a 3s fixed wait** on a blue radial gradient that matches nothing else | splash view/controller | Splash should reflect the new brand gradient and dismiss when init *actually* finishes. |
| U12 | **All food photos are the same hotlinked Pinterest image** (and one URL is the `hhttps` typo) | `DataManager.java` | Food app with no appetizing photography can't feel premium. Bundle real per-dish images. |
| U13 | **Invalid/ignored CSS**: `-fx-letter-spacing` doesn't exist in JavaFX (`rs-get-started`); an old log shows a CSS parse error for `-fx-background-image` missing `url()`. | `styles.css:954`; `error_log.txt:39` | Clean the sheet; JavaFX silently ignores unknown properties so errors accumulate unnoticed. |
| U14 | **TabPane hacked to hide its header** for the auth toggle | `ra-tab-pane`, `styles.css:1133-1145` | Use a `StackPane` with two panes + the toggle buttons; less fragile. |
| U15 | **No Bangla (বাংলা) anywhere** in a Bangladesh Railway food brand | all views | Even EN-primary with Bangla brand moments ("ভোজন অন রেইলস", "খাবার আসছে!") adds authenticity + fun. Full i18n via `ResourceBundle` is the stretch goal. |
| U16 | **Zero motion**: only one hover translate (menu cards). No screen transitions, no cart-add feedback, no count-up on stats | whole app | See §4.4 motion system. |
| U17 | Currency formatting scattered (`String.format("৳%.0f")` in `CartManager`) — risk of ৳/Tk inconsistency | `CartManager.java:167` | One `MoneyFormat` utility used everywhere. |

---

## 3. The Brand Palette — "Pastel Rail" (IMPLEMENTED)

> Per the updated brief, the shipped palette is **soft pastel light-green + warm cream + light red/coral**, replacing all five previous palettes. These are the exact tokens now used across `styles.css`:

| Token | Hex | Role |
|---|---|---|
| Primary green | `#46A67B` | Buttons, active states, links, focus ring (white text OK on bold controls) |
| Primary hover | `#3A8E68` | Button hover |
| Primary pressed | `#2F7657` | Button pressed (with 1px dip) |
| Deep green | `#245C45` | Deepest hover, hero gradient end, dark accents |
| Mint | `#57B98A` | Secondary "success" accent (add-to-cart, stat card) |
| Sidebar deep green | `#2C4A3E` | Dashboard sidebars + deep-green headings (keeps white nav text readable) |
| Cream | `#FBF6EC` | Page backgrounds |
| Soft green tint | `#F1F7F3` / `#EAF3EC` | Inputs, subtle fills |
| Row hover / selected | `#EEF7F1` / `#DCEFE4` | List & table hover/selection |
| Coral / light red | `#EF6F61` | Badges, spicy/CTA accents (was purple/orange) |
| Danger text | `#C0392B` | Error labels (AA-compliant on white) |
| Soft red tint | `#FBEAE7` | Error/spicy backgrounds |
| Surface | `#FFFFFF` | Cards, tables, forms |

Splash / login / train-search heroes now use a **green radial gradient** (`#46A67B → #245C45`) in place of the old blue. Neutral grays/borders were intentionally left untouched to preserve text contrast in both light and dark (sidebar) contexts.

<details><summary>Original proposed palette — "Rail & Turmeric" (deep green + turmeric gold), superseded by the pastel brief above</summary>

**Concept:** Bangladesh Railway's deep locomotive green (heritage, trust, the national color) paired with turmeric/mustard gold (food warmth, appetite, fun) on warm cream — instead of the current five borrowed SaaS palettes. Premium comes from restraint (one green, one gold, lots of cream and white); fun comes from the gold accents, chili-red badges, and motion.

### 3.1 Core tokens

| Token | Hex | Role |
|---|---|---|
| `--bhojon-green-900` | `#0B3D2E` | Splash/hero gradient end, deepest text on tint |
| `--bhojon-green-800` | `#0A5238` | Button **pressed** |
| `--bhojon-green-700` | `#0C5F41` | Button **hover**, links hover |
| `--bhojon-green-600` ★ | `#0E6B4A` | **Primary** — buttons, active states, links, focus ring (4.9:1 on white ✓) |
| `--bhojon-green-100` | `#D9EEE3` | Selected rows, active tint backgrounds |
| `--bhojon-green-50` | `#EEF7F2` | Hover tint on lists/cards |
| `--turmeric-600` | `#B97D0B` | Warning text, gold used *as text* on white (4.6:1 ✓) |
| `--turmeric-500` ★ | `#F7B32B` | **Accent** — CTAs like "Add to cart", cart badge, sidebar active pill, highlights. Always with dark ink text (`#231A00`, 9:1 ✓) |
| `--turmeric-400` | `#FFC351` | Accent hover |
| `--chili-600` | `#C0392B` | Danger/destructive, spicy tags, error text (5.0:1 on white ✓) |
| `--chili-50` | `#FBEAE7` | Error field/banner background |
| `--ink-900` | `#17231D` | Headings, primary text (14.9:1 ✓) |
| `--ink-600` | `#55645C` | Muted/secondary text (5.4:1 ✓ — replaces every failing gray) |
| `--rail-charcoal` | `#10201A` | **Sidebar / dark surfaces** (green-tinted charcoal, both dashboards) |
| `--rail-charcoal-hover` | `#1B2F26` | Sidebar item hover |
| `--sidebar-muted` | `#9DB4A8` | Sidebar inactive labels (5.9:1 on charcoal ✓) |
| `--cream-bg` | `#FAF6EE` | **Page background** (replaces all 4 different gray-blues) |
| `--surface` | `#FFFFFF` | Cards, tables, forms |
| `--border` | `#E3E0D5` | Card/input borders (warm, matches cream) |
| `--success` | `#0E6B4A` | = primary green (keeps palette tight) |
| `--info` | `#1F6F8B` | Rare informational accents (tracking timer) |

★ = the two colors that define the brand. Everything else supports them.

### 3.2 Signature usages

- **Splash / hero screens:** `linear-gradient(to bottom right, #0B3D2E, #0E6B4A)` with the logo and a turmeric progress bar. Replaces the blue radial gradient.
- **Primary button:** green-600 bg / white text → hover green-700 + soft green shadow → pressed green-800 + translate-y 1px.
- **Accent button ("Add to cart", "Place order"):** turmeric-500 bg / `#231A00` text — the gold is what makes it feel like a *food* app, not a bank.
- **Sidebar (both dashboards, unified):** rail-charcoal bg; active item = **turmeric pill with dark text** (distinctive, replaces indigo/teal); inactive = `--sidebar-muted`.
- **Cart badge:** chili-600 circle, white number, springs on add.
- **Stat cards:** white on cream, 4px left border in green / turmeric / chili instead of blue / green / purple.
- **Selection & hover in tables/lists:** green-50 hover, green-100 selected — everywhere, no more per-screen blues/teals.

### 3.3 Typography (with the palette)

- **Plus Jakarta Sans everywhere** (already bundled — just stop overriding it).
- Scale: Display 34/ExtraBold · H1 24/Bold · H2 18/Bold · Body 14/Regular · Caption 12/Medium · Overline 11/Bold uppercase.
- Numerals in prices/timers: Bold, green-600 or ink-900 — never gray.

</details>

---

## 4. Upgrade Plan — Premium *and* Fun

### 4.1 P0 — Stop the bleeding (½ day)
1. Revoke Gmail app password + regenerate OCR key (they're already public). Externalize to gitignored config (S1, S2).
2. Move admin credentials into DB with BCrypt; replace `hashCode()` hashing (S3, S4).
3. Fix `hhttps` typo (A9). Remove `HeroSection.tsx`, `TestDB.java`, `error_log.txt`, DB, uploads from the repo; extend `.gitignore` (A14).

### 4.2 P1 — Architecture hardening (2–3 days)
4. Single `Database` class + DAOs; one-time schema init; absolute app-data DB path (A1, A2, A4).
5. Kill the dual data source: seed trains/restaurants/food into SQLite, delete hardcoded lists + `+1000` hack (A3).
6. Replace raw threads with `Task` + executor; route all errors to a single UI error handler (A5, A16).
7. SLF4J logging; drop tess4j or make it a real offline fallback (A6, A8).
8. First tests: cart math, OCR parsing, DAO round-trip (A13). Bump JavaFX to 21; add `jpackage` Windows installer (A15).

### 4.3 P2 — Design system rollout (3–4 days)
9. Create the §3 token palette at the top of `styles.css`; define the component library: 4 button variants × 3 states, inputs, cards, table, badge, toast, dialog, empty state.
10. **Delete all 191 inline FXML styles**, screen by screen (order: role-select → passenger flow → cart/confirmation → dashboards). One palette, every screen (U1).
11. One font declaration; fix contrast grays; restore focus rings; add pressed/disabled states (U2–U5).
12. Ikonli icons replace emoji; styled dialogs/toasts replace default Alerts (U6, U7).
13. Real food photography bundled locally; designed empty states (U10, U12).

### 4.4 P3 — The "fun" layer (2 days)
14. **Motion system** (150–250ms, ease-out): screen fade+slide 16px on navigate; cart badge spring-scale on add; stat count-up on dashboard load; button press micro-dip; tracking-step dots pulse while active.
15. **Splash with personality:** brand gradient, train glides across, turmeric progress bar; dismisses on real init completion (U11).
16. **Order tracking as the hero moment:** train icon moves along the active line between step dots — this screen is the brand.
17. Bangla brand moments + celebratory order-confirmation ("খাবার আসছে! Your food is on the way 🍛→🚆") (U15).
18. Stretch: full EN/BN `ResourceBundle` toggle; dark mode from the same tokens.

### Priority cheat-sheet

| Phase | Effort | Outcome |
|---|---|---|
| P0 Security & hygiene | ~½ day | No leaked secrets, honest repo |
| P1 Architecture | 2–3 days | One data source, real hashing, robust threading, installer |
| P2 Design system | 3–4 days | **One premium brand across all 17 screens** |
| P3 Delight | 2 days | Motion, hero moments, Bangla personality |

---

*Everything above is a plan — no code, styles, or assets were modified.*
