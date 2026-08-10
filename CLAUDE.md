# CLAUDE.md — AdditionsAPI

## What this is

AdditionsAPI is a Bukkit/Spigot/Paper **server plugin** — specifically a
custom-items **API/library plugin** (`com.chrismin13.additionsapi`). It lets
other plugins define custom items with custom textures/durability without
hardcoded vanilla-item-durability hacks, generates and serves the resource
pack that backs those textures, and provides its own fake-durability,
recipe, and permission systems.

**This is a dependency for other of Bert's `domain.*` plugins.** Read
"API-surface notes for dependent plugins" below before assuming any public
method/field/constant still means what it used to.

See `PLAN.md` for the full modernization plan, milestone status, and the
honest platform/version support matrix.

## Provenance and fork status

- This repo (`bshuler/AdditionsAPI`) is a genuine GitHub **fork** of
  `chrismin13/AdditionsAPI`, not a standalone snapshot.
- Divergence at the start of this modernization pass: 2 commits local-only
  (`Yay MIT License`, `Update README.md`/`Update dev.bat` plus a `Test
  Commit`/`Another Test` pair) vs. 56 commits upstream-only. Merge-base
  analysis confirmed the two histories are cleanly mergeable (no conflicting
  edits to the same lines) — this is genuine, non-adversarial divergence,
  not a hard fork that drifted apart.
- Upstream (`chrismin13/AdditionsAPI`) is confirmed **abandoned** per its own
  README, which states the original author is no longer maintaining it.
  This repo's own `README.md` already says the same
  ("chrismin13 ... is no longer maintaining the project, so it will likely
  only receive version updates here").
- This modernization pass forward-merged upstream's 56 commits into this
  fork's `master` before doing any build/dependency work, so the working
  tree contains both the merge and the modernization changes described
  below, as one combined body of work (see "Git notes").

## Architecture

Single Bukkit `JavaPlugin` (`com.chrismin13.additionsapi.AdditionsAPI`),
event-driven, structured as an API surface other plugins call into:

- `items/` — `CustomItem` (base class: display name/lore, durability,
  permissions, potion-effect visibility flags), `CustomItemStack`,
  `CustomTool`, `CustomArmor`, `CustomFood`, etc. This is the primary public
  API dependent plugins extend/construct.
- `durability/` — pluggable fake-durability mechanics per item category
  (`SpadeDurability`, `HoeDurability`, `FishingRodDurability`, ...).
- `enums/` — `ItemType`, `ToolType`, `ArmorType`: Bukkit `Material` →
  category classification.
- `listeners/vanilla/` and `listeners/custom/` — vanilla Bukkit event
  bridges (`PlayerInteract`, `EntityShootBow`, `PlayerDeath`, ...) and the
  custom-item event bridges they fire (`CustomItemPlayerInteractEvent`,
  `PlayerCustomItemDamageEvent`, `CustomItemFishEvent`, ...).
  `events/item/` holds the actual custom `Event` classes.
  `permissions/` — per-item-category permission-check interfaces
  (`HoePermissions`, `ShieldPermissions`, `SpadePermissions`, ...).
- `recipes/` — `CustomFurnaceRecipe`/`CustomShapedRecipe`/
  `CustomShapelessRecipe`: register custom-item recipes against vanilla
  crafting.
- `utils/` — `MaterialUtils` (the largest file in the project, ~3200 lines:
  per-`Material` hardness/blast-resistance/interactable lookups, with a
  legacy pre-1.13 numeric-durability fallback path — see "Legacy compat" and
  "`useNewMethods`" below), `LangFileUtils`, `NumberUtils`,
  `EquipmentSlotUtils`, `PermissionUtils`.
- `com.comphenix.attribute.NbtFactory` — a vendored NMS-reflection helper for
  reading/writing raw item NBT (needed because Bukkit's public API doesn't
  expose arbitrary NBT tags). This is the single most version-fragile piece
  of the codebase: it works by reflecting into CraftBukkit/NMS internal
  classes by structural shape, not by any stable API contract. It has not
  needed changes in this pass (all target versions still expose the shapes
  it looks for), but a future NMS refactor could break it silently at
  runtime rather than at compile time — see "Open problems" in `PLAN.md`.
- `com.codingforcookies.armorequip` — a vendored `ArmorEquipEvent`
  polyfill (fires a synthetic event for armor-slot changes, which vanilla
  Bukkit doesn't expose directly).
- `us.fihgu.toolbox` — the resource-pack pipeline: `resourcepack/` (builds,
  hashes, and serves the resource pack over a tiny built-in HTTP server —
  `ResourcePackManager`, `ResourcePackServer`, `ResourcePackListener`),
  `file/FileUtils` (folder copy/delete, URL-to-file download), `item/`
  (`DamageableItem`, `ItemUtils`), `reflection/ReflectionUtils`.
- `us.fihgu.toolbox.compat.EnumCompat` (**new this pass**) — the runtime
  enum/constant-name resolution helper described below.
- `legacy-hooks/` — **excluded from the build** (see below).

### Removed dependencies

| Removed | Why | Replacement |
|---|---|---|
| `htmlunit-driver` (pulled in Selenium transitively) | Dead/irrelevant browser-automation dependency with no connection to the plugin's actual function; its only real effect was transitively supplying `commons-io`/`commons-codec` to a few unrelated helper classes | Removed outright |
| `org.apache.commons.io.FileUtils` (via the above) | Transitive-only, no longer available once Selenium was removed | Pure `java.nio.file.Files`/`Files.walkFileTree`/`java.nio.channels.Channels` in `FileUtils.java` (`copyURLtoFile`, `copyFolder`, `copyFile`, `deleteFolder`) |
| `org.apache.commons.codec.digest.DigestUtils` + `javax.xml.bind.DatatypeConverter` (JAXB, removed from the JDK since Java 11) | Transitive-only (commons-codec) / JDK-removed (JAXB) | Plain `java.security.MessageDigest` (MD5 + SHA-1) plus a small local hex-encoding helper in `ResourcePackManager.java` |
| `bstats-bukkit` 1.4 (bStats metrics) | Requires a plugin ID registered at bstats.org that this project doesn't have; dead weight | Removed outright, no replacement |
| Minepack (`minepack.net`) resource-pack hosting integration | Service is defunct | Removed; the built-in HTTP resource-pack server (`us.fihgu.toolbox.resourcepack`) is now always used. `config.yml`'s `use-minepack` key is kept (ignored) so existing configs with it set don't error on load |
| `pom.xml` (Maven) | Replaced wholesale by the Gradle 9.x build, matching the EpicFurnaces/Spigot-InvUnload sibling convention | `build.gradle.kts` |

### Excluded: the Minepack initialization hook

`legacy-hooks/MinePackInitializationMethod.java.txt` (originally
`me.benfah.cu.init.impl.MinePackInitializationMethod`) implemented the
Minepack-hosting init path described above. Relocated out of the source
tree (renamed `.txt` so it's inert, not compiled) rather than deleted, in
case a future maintainer with access to a working Minepack-equivalent
service wants to resurrect the pattern.

### Legacy compat: `MaterialUtils.useNewMethods`

`AdditionsAPI.onEnable()` probes at startup whether
`Material.DIAMOND_BLOCK.isInteractable()/getHardness()/getBlastResistance()`
exist (they were added in Bukkit 1.13) and sets the static
`MaterialUtils.useNewMethods` flag to `false` if not — falling back to a
large legacy switch-statement-based hardness/blast-resistance table for
pre-1.13 servers. Every server version this project actually targets
(1.18.2 and up) has always resolved `useNewMethods = true`; the legacy
switch branches are provably dead code for this project's supported range,
which is why the two enum-rename fixes inside them (see `EnumCompat` below)
used a cheap string-name pre-check rather than the full runtime-resolution
pattern used everywhere else.

### Runtime API-drift compat: `EnumCompat`

Several Bukkit/Paper enum constants (and one non-enum: `Enchantment`) were
renamed at the MC 1.20.5/1.20.6 ItemStack-data-components/Enchantment-
registry refactor, which falls **between** this project's 1.21.11 and 1.20.1
target versions. A single unmodified source tree needs to compile against
Paper API versions on both sides of that boundary, and enum `case` labels /
direct constant references are compile-time-bound to one name or the other
— so each affected constant is resolved **by name, at runtime**, via
`us.fihgu.toolbox.compat.EnumCompat`:

- `EnumCompat.resolve(Class<E>, String...)` — for real Java enums
  (`Material`, `EntityType`, `ItemFlag`), tries `Enum.valueOf` for each
  candidate name in order, returns the first that exists.
- `EnumCompat.resolveStaticField(Class<T>, String...)` — for
  `org.bukkit.enchantments.Enchantment`, which is an **abstract class**
  publishing vanilla enchantments as `public static final` instance fields,
  not a real Java enum — resolved via reflection (`Class.getField`) instead.

Confirmed rename table (empirically verified via `javap` against the actual
resolved `paper-api` jars for both 26.2 and 1.20.1, plus live compiles
against 26.2, 1.21.11, 1.20.1, 1.19.4, and 1.18.2):

| Old name (≤1.20.1) | New name (≥1.21.11) | Resolved via | Usage site(s) |
|---|---|---|---|
| `ItemFlag.HIDE_POTION_EFFECTS` | `ItemFlag.HIDE_ADDITIONAL_TOOLTIP` | `EnumCompat.resolve` | `CustomItem.java` (`HIDE_ADDITIONAL_TOOLTIP_COMPAT`) |
| `Enchantment.DAMAGE_ALL` | `Enchantment.SHARPNESS` | `EnumCompat.resolveStaticField` | `CustomTool.java` (`SHARPNESS_COMPAT`) |
| `Enchantment.DURABILITY` | `Enchantment.UNBREAKING` | `EnumCompat.resolveStaticField` | `PlayerCustomItemDamage.java` (`UNBREAKING_COMPAT`) |
| `EntityType.DROPPED_ITEM` | `EntityType.ITEM` | `EnumCompat.resolve` | `CustomItemFish.java` (`ITEM_COMPAT`) |
| `Material.GRASS` (the plant item) | `Material.SHORT_GRASS` | `EnumCompat.resolve` | `PlayerInteract.java`, `CustomItemPlayerInteract.java` (`SHORT_GRASS_COMPAT`); string-name pre-check in `MaterialUtils.java`'s dead-code legacy branches |
| `Material.SCUTE` | `Material.TURTLE_SCUTE` | (dead code only) | string-name pre-check in `MaterialUtils.java`'s dead-code legacy branches only — no live-code usage site existed |

Two other renames turned out to be **stable across the entire 1.18.2–26.2
target matrix** (confirmed by successful compiles at every version), so they
needed only a one-time direct rename, not the `EnumCompat` treatment:
`Material.GRASS_PATH` → `Material.DIRT_PATH`, and
`Material.ZOMBIE_PIGMAN_SPAWN_EGG` → `Material.ZOMBIFIED_PIGLIN_SPAWN_EGG`.

`Material.LEGACY_*` pre-flattening constants (e.g. `LEGACY_WOOD_SWORD`) are
permanent Bukkit backward-compat fields, unaffected by any of this — they
still exist and compile fine on the latest Paper API.

## API-surface notes for dependent plugins (read this before depending on AdditionsAPI)

- **No public method signatures changed.** The `EnumCompat` fields above are
  all `private static final`, internal to their classes — nothing about
  `CustomItem`, `CustomTool`, etc.'s public API changed shape.
- **Runtime behavior of the resolved constants is unchanged** — e.g.
  `CustomItem.setPotionEffectsVisibility(boolean)` still hides/shows potion
  tooltips exactly as before; it just resolves the correct underlying
  `ItemFlag` constant for whichever server version it's running on, instead
  of assuming one name at compile time.
- **Minepack resource-pack hosting is gone.** Any dependent plugin or server
  config relying on `use-minepack: true` will silently get the built-in
  HTTP server instead (the key is read but ignored, so configs don't error,
  but the *behavior* changes if you were actually using Minepack).
- **bStats metrics are gone.** Nothing observable to a dependent plugin, but
  worth knowing if you were tracking this plugin's usage stats externally.
- **The Maven build (`pom.xml`) is gone**, replaced by Gradle. If any
  dependent plugin's own build referenced AdditionsAPI as a Maven
  dependency/module, it needs to switch to consuming the built jar
  directly (`build/libs/AdditionsAPI-<version>.jar`) or add this project as
  a Gradle dependency instead.
- `plugin.yml`'s `pluginVersion`/`version` is still `1.3.3` (templated from
  `gradle.properties`, unchanged this pass) and `api-version` is still
  `1.13` — **deliberately not bumped** to a newer value (unlike the
  EpicFurnaces sibling, which ships a single 26.2-only target). Because
  AdditionsAPI is a shared dependency and this project's own goal is one
  jar spanning 1.18.2 through 26.2, keeping `api-version` at its original
  low value maximizes the set of servers the jar will actually load on;
  bumping it would make Paper refuse to load the plugin on older target
  servers even though the jar itself builds fine for them.

## Platforms

This is Bukkit-API software. "Platform" here means Bukkit-API server
implementations, not mod loaders:

- **Paper** (primary target) — and by API compatibility, **Purpur**.
- **Spigot** — the plugin uses only stable Bukkit/Spigot API (plus the
  vendored NMS-reflection `NbtFactory`/`ArmorEquipEvent` helpers, which
  reflect into CraftBukkit internals present on Spigot too), so it should
  run on plain Spigot, without any Paper-only optimizations (none are
  currently used).
- **Folia** — **not supported; `folia-supported` is deliberately absent from
  `plugin.yml`.** Static analysis (not just untested guesswork) found three
  concrete blockers: legacy `Bukkit.getScheduler()` calls touching specific
  players/entities across 5 files (`ResourcePackListener`, `DurabilityBar`,
  `AdditionsAPI.onEnable`, `CustomElytraPlayerToggleGlide`,
  `ArrowFromCustomBowHit`); `ElytraDurabilityTask` (a `BukkitRunnable`)
  ticking one player's state every second via the global scheduler instead
  of a per-entity one; and two unsynchronized static `HashMap`s
  (`CustomElytraPlayerToggleGlide.playersGliding`,
  `ArrowFromCustomBowHit.arrowsShotFromCustomBow`) mutated from event
  handlers, which would race under Folia's region-threaded concurrent event
  dispatch. None of these are low-risk fixes while this project keeps
  building one source tree against pre-Folia Paper/Spigot too — see
  "Folia" in `PLAN.md` for the full evidence and reasoning.
- **Fabric / NeoForge / Forge are not applicable.** Bukkit/Spigot/Paper
  plugins run against a stable, server-implementation-maintained API
  (`org.bukkit.*`/`io.papermc.paper.*`) that stays source-compatible across
  MC versions — that is the entire premise of this project's single-jar,
  multi-version build. Fabric/Forge/NeoForge mods compile directly against
  Minecraft's own (Mojang-mapped or obfuscated) internal classes and use
  Mixins to inject into game internals; there is no stable API layer, and a
  mod must be rebuilt per Minecraft version. Porting this project onto a
  mod loader would mean rewriting essentially all of it (every
  `org.bukkit.*` call, plus the already-NMS-reflection-based `NbtFactory`)
  against Fabric/Forge internals from scratch — a rewrite, not a port. See
  `PLAN.md` milestone 3.

See `PLAN.md` for the version matrix and per-version build status.

## Build

```bash
./gradlew build           # compiles, shades, produces build/libs/AdditionsAPI-<version>.jar
./gradlew shadowJar        # same, explicit task name
./gradlew clean compileJava -PpaperApiVersion=1.20.1-R0.1-SNAPSHOT   # test an older target
```

- Gradle 9.x, Java 25 toolchain (auto-provisioned via
  `foojay-resolver-convention` — do not install a system JDK for this; the
  only officially-installed system JDK, Temurin 21, is untouched). Java 25
  is required because the latest `paper-api`'s Gradle module metadata
  declares it as a minimum (same finding as the EpicFurnaces/Spigot-
  InvUnload siblings).
- `compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")`, where
  `paperApiVersion` defaults (in `gradle.properties`) to the latest
  resolvable coordinate (`26.2.build.111-stable` at time of writing — MC is
  calendar-versioned now, re-check `repo.papermc.io`/`fill.papermc.io`
  rather than trusting this number to stay current).
- Single unmodified source tree, no per-version source sets: every target
  version is built by overriding `-PpaperApiVersion=<coord>` on the command
  line. This works because the only genuine version-boundary API breaks
  (the `EnumCompat` rename table above) are handled by runtime name
  resolution rather than by code that differs per version.
- `com.gradleup.shadow` 9.6.1 for the fat jar (`NbtFactory`,
  `ArmorEquipEvent`, `EnumCompat`, and the rest of `us.fihgu.toolbox` all
  need to ship inside the plugin jar since they aren't provided by the
  server).

## Testing

JUnit 5 (Jupiter) + JaCoCo, with MockBukkit for anything that touches live
`org.bukkit.*` behavior and plain JUnit for pure-logic classes. `./gradlew
test` runs the suite; `./gradlew check` additionally runs
`jacocoTestCoverageVerification`, which fails the build if line coverage
drops below 100% on any included class not named in an explicit exception.

- **MockBukkit is pinned separately from the main build's `paperApiVersion`**
  (`mockbukkit-v26.1.2:4.115.0` in the `test` configuration, vs. the
  floating/overridable `paperApiVersion` used by `compileOnly` for the main
  source set). MockBukkit ships a fixed registry snapshot tied to one exact
  server build (`26.1.2.build.74-stable`) — it is not a moving target the
  way `paperApiVersion` is, and pinning it independently is intentional, not
  an oversight.
- **Coverage exclusions are documented, never silent.** Every excluded
  class/package and every partial-coverage exception (`MISSEDCOUNT`/
  `COVEREDRATIO` ceiling) in `build.gradle.kts`'s `jacocoExcludes` and
  `violationRules` blocks carries an inline comment explaining exactly why
  that gap exists (a real bootstrap landmine, provably-dead code, or a
  MockBukkit limitation) — never a bare number. See "Test coverage" in
  `PLAN.md` for the full list, the final coverage numbers, and the bugs
  found (and one deliberately left un-fixed) while writing this suite.
- **A `MISSEDCOUNT`/`COVEREDRATIO` `includes`-rule alone does not except a
  class from the coverage gate.** `build.gradle.kts` also has one blanket
  top-level `CLASS`-element rule (100% `LINE` minimum, no `includes`) that
  applies to every analyzed class except those listed in *its own*
  `excludes`. A new per-class exception needs to be added to both: the
  narrow `includes`-based rule with its exact ceiling, and the blanket
  rule's `excludes` list — otherwise the blanket rule still fires against
  the class at its old 100% bar.
- **A green `./gradlew test` is not sufficient evidence that a
  parameterized/data-driven test actually verified anything.** If an
  individual case throws before its assertion runs, JUnit5's
  `@MethodSource` machinery records that case as **skipped**, not
  **failed** — the run stays `BUILD SUCCESSFUL` with 0 failures while
  silently verifying nothing for that case. Always check the per-class
  test-results XML's `skipped` attribute too (not shown in the Gradle
  console output), and cross-reference it against JaCoCo's line-coverage
  numbers. When a whole category of cases is genuinely untestable under the
  mocking framework in use, don't delete them silently — replace them with
  one explicit `assertThrows`-based test that documents and proves the real
  limitation, so a future fix to the underlying mocking gap will make that
  test start failing (prompting a revisit) instead of leaving a stale
  comment nobody has to keep honest.
- **A green `jacocoTestCoverageVerification` means nothing until the
  analyzed-class count is confirmed nonzero and matches the include list**
  (`grep -o '<class name="[^"]*"' build/reports/jacoco/test/
  jacocoTestReport.xml | wc -l`). A misconfigured `includes`/`excludes`
  filter can make JaCoCo analyze zero classes and still report a trivially
  "passing" verification.
- **MockBukkit registry side effects can produce false-positive passes.**
  Some Bukkit calls (e.g. `Material.getMaxDurability()`) lazily trigger a
  one-time registry/class-init chain requiring `MockBukkit.mock()` to have
  run first. A test class missing the `@BeforeAll MockBukkit.mock()` /
  `@AfterAll MockBukkit.unmock()` lifecycle can still "pass" if an earlier
  test in the same JVM run happened to trigger that init as a side effect —
  don't trust a green run for any class touching Bukkit-registry-lazy-init
  code paths unless it's been run in isolation at least once.
- When in doubt about live MockBukkit/real-object behavior before writing a
  real test against it, probe first with a throwaway `ScratchProbeTest.java`
  (a one-off test that just prints/asserts what actually happens), confirm
  the behavior, then delete the probe — don't guess and don't leave the
  probe committed.

## Porting notes for whoever touches this next

- `NbtFactory`/`ReflectionUtils` reflect into CraftBukkit/NMS internals by
  structural shape (method/field names on the live `CraftItemStack`/
  `net.minecraft.nbt.*` classes), not a stable Bukkit API contract. This is
  the single most likely thing to break on some *future* Minecraft version
  without a compile error — it would fail at runtime (a `NoSuchMethodError`
  or reflection exception inside `NbtFactory`), not at build time. Nothing
  in this pass changed that risk; it is inherited unchanged from upstream.
- If a future MC version renames or removes another enum constant this
  project references directly (search for `Material.`, `Enchantment.`,
  `EntityType.`, `ItemFlag.` literal usages), follow the same `EnumCompat`
  pattern: add the new name as the first candidate, the old name as
  fallback, and reference the resulting `_COMPAT` field instead of the raw
  constant.
- `config.yml`'s `use-minepack` key is dead (see above) — kept only for
  config-file backward compatibility, not because it does anything.
