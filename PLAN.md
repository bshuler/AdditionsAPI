# PLAN.md — AdditionsAPI modernization

## Goal

Get this fork building and running on the latest Paper API, forward-merge
the abandoned upstream first, then walk backward through older Minecraft
versions as far as practical, and give an honest answer on cross-platform
(mod-loader) support. See `CLAUDE.md` for architecture, fork provenance, and
the API-surface notes dependent plugins need to read.

**Java version note:** live build against the latest `paper-api`
(26.2.build.111-stable) requires JVM 25 per its Gradle module metadata
(same finding as the EpicFurnaces/Spigot-InvUnload siblings). The root
`build.gradle.kts` toolchain is Java 25, auto-provisioned by
`foojay-resolver-convention` (Gradle's own toolchain cache, not the system/
Homebrew JDK — the only installed system JDK, Temurin 21, is untouched).

## Milestones

### 1. Docs + fork-divergence inventory — DONE

- [x] Investigated provenance: confirmed a genuine GitHub fork of
      `chrismin13/AdditionsAPI`, 2 commits local-only vs. 56 upstream-only,
      merge-base analysis confirmed clean/non-conflicting divergence.
      Confirmed upstream is abandoned per its own README (matches this
      fork's own README, which already says so).
- [x] `CLAUDE.md` written.
- [x] `PLAN.md` written (this file).

### 2. Forward-merge + modern build (latest Paper API) — DONE

- [x] Merged upstream's 56 commits into this fork's `master`.
- [x] Replaced `pom.xml` (Maven) with a Gradle 9.x `build.gradle.kts` +
      `settings.gradle.kts`, matching the EpicFurnaces/Spigot-InvUnload
      sibling convention (`foojay-resolver-convention` 1.0.0,
      `com.gradleup.shadow` 9.6.1, Java 25 toolchain).
- [x] `paper-api` at the latest resolvable version — **26.2** /
      `26.2.build.111-stable` at time of writing; re-check
      `repo.papermc.io`/`fill.papermc.io` before trusting this number to
      stay current, Minecraft is calendar-versioned now.
- [x] Removed dependencies that no longer resolve or no longer make sense:
      `htmlunit-driver` (Selenium — dead weight, was only ever supplying
      `commons-io`/`commons-codec` transitively to two unrelated helper
      classes), `bstats-bukkit` (no registered plugin ID), the Minepack
      resource-pack-hosting integration (service defunct). See `CLAUDE.md`
      "Removed dependencies" table for the full list and replacements.
  - `FileUtils.java`: `org.apache.commons.io.FileUtils` → pure
    `java.nio.file.Files`/`Files.walkFileTree`/`java.nio.channels.Channels`.
  - `ResourcePackManager.java`: `org.apache.commons.codec.digest.DigestUtils`
    + `javax.xml.bind.DatatypeConverter` (JAXB, removed from the JDK since
    Java 11) → plain `java.security.MessageDigest` (MD5 + SHA-1) with a
    small local hex-encoding helper.
  - Relocated `me.benfah.cu.init.impl.MinePackInitializationMethod` to
    `legacy-hooks/MinePackInitializationMethod.java.txt` (excluded from the
    build, not deleted).
- [x] Confirmed (via extracting the actual resolved `paper-api-26.2` jar and
      running `javap`) that `Material.LEGACY_*` pre-flattening constants —
      the biggest open compile-risk flagged going into this pass — still
      exist unchanged; no code changes were needed for that concern.
- [x] Found and fixed 8 renamed-Bukkit-constant compile errors against 26.2
      (`ItemFlag.HIDE_POTION_EFFECTS`, `Enchantment.DAMAGE_ALL`/
      `DURABILITY`, `EntityType.DROPPED_ITEM`, `Material.GRASS`/`SCUTE`/
      `GRASS_PATH`/`ZOMBIE_PIGMAN_SPAWN_EGG`) by extracting the actual
      resolved jar and using `javap` for ground truth rather than guessing.
- [x] Green build (`./gradlew build`), verified jar contents (`unzip -l`) —
      plugin classes, `NbtFactory`, `EnumCompat`, templated `plugin.yml`
      all present; no leaked bStats/Selenium/Minepack classes.

### 3. Cross-platform assessment

**Conclusion: Fabric/NeoForge/Forge are not applicable to this plugin.**

Same reasoning as every other Bukkit-API plugin in this program (see the
EpicFurnaces/Spigot-InvUnload `PLAN.md`s for the fuller version of this
argument): Bukkit/Spigot/Paper plugins run against a stable, server-
implementation-maintained API that stays source-compatible across MC
versions — that's the entire premise of this project's one-jar,
multi-version build (milestone 4). Fabric/Forge/NeoForge mods compile
directly against Minecraft's own classes and use Mixins to inject into game
internals; there is no stable API layer, and a mod must be rebuilt per MC
version. Porting this project onto a mod loader would mean rewriting nearly
all of it — every `org.bukkit.*` call, plus the already-NMS-reflection-based
`NbtFactory`/`ArmorEquipEvent` helpers — from scratch against Fabric/Forge
internals. That is a rewrite, not a port, and is out of scope here; the
honest answer for a genuinely inapplicable platform is to say so, not to
fake partial support.

`critical-orientation` (`~/code/minecraft-mods/critical-orientation`), this
program's Stonecutter/Stonecraft/Architectury-Loom template for mod-loader
multi-version support, was re-confirmed as having no applicability here for
the same reason as prior sibling repos: it generates per-version,
per-loader Fabric/Forge/NeoForge subprojects, and AdditionsAPI is not a mod.

| Server implementation | Status | Notes |
|---|---|---|
| Paper | Supported (primary target) | Built and tested against this |
| Purpur | Expected-compatible | Purpur is a Paper fork with a superset API; nothing here uses anything Purpur would break |
| Spigot | Expected-compatible | Only stable Bukkit/Spigot API (+ CraftBukkit-internal reflection in `NbtFactory`, present on Spigot too) is used, no Paper-only calls |
| Folia | Untested, possible friction | The built-in resource-pack HTTP server (`ResourcePackServer`) and scheduler usage are the likeliest friction points under Folia's region-threaded model; not smoke-tested, flagged as an open problem below |
| CraftBukkit | Expected-compatible (same reasoning as Spigot) | Not a realistic deployment target in practice |

### 4. Backward version walk — DONE

Paper publishes `paper-api` for older MC versions using the classic
`X.Y.Z-R0.1-SNAPSHOT` Maven coordinate, still resolvable from
`repo.papermc.io/repository/maven-public/` (confirmed live:
`1.18.2-R0.1-SNAPSHOT`, `1.19.4-R0.1-SNAPSHOT`, `1.20.1-R0.1-SNAPSHOT`,
`1.21.11-R0.1-SNAPSHOT` all present).

Testing against 1.20.1 (the first older target tried) surfaced **15
compile errors that did not exist against 26.2/1.21.11** — the 6 constant
renames listed in `CLAUDE.md`'s `EnumCompat` table turned out to be genuine
version-boundary renames (the MC 1.20.5/1.20.6 ItemStack-data-components/
Enchantment-registry refactor), not stable back through the whole matrix
the way `DIRT_PATH`/`ZOMBIFIED_PIGLIN_SPAWN_EGG` are. Rather than reverting
those two names (which would break the 26.2/1.21.11 build) or adding
per-version source sets (unnecessary complexity for a handful of constant
lookups), each was resolved by name at runtime via the new
`us.fihgu.toolbox.compat.EnumCompat` helper — the same technique this
project's task brief already called for regarding `Particle` renames,
generalized to `Material`/`EntityType`/`ItemFlag`/`Enchantment`. Two of the
6 (`Material.GRASS`/`Material.SCUTE`) only appeared as `case` labels inside
provably-dead legacy code (`MaterialUtils`'s pre-1.13 fallback path, see
`CLAUDE.md` "`useNewMethods`") — those got a cheap `name().equals(...)`
string pre-check instead, since a compile-time enum case label can't hold a
runtime-resolved value.

After applying `EnumCompat` to all 5 remaining live-code usage sites
(`CustomTool.java`, `PlayerCustomItemDamage.java`, `PlayerInteract.java`,
`CustomItemPlayerInteract.java`, `CustomItemFish.java` — `CustomItem.java`'s
`ItemFlag` fix had already been done), all 5 target versions compile clean
with **zero regressions** on the newer versions (confirmed by re-running
26.2 and 1.21.11 after the fix, not just assuming the fallback-resolution
order is safe).

The plugin has no per-version code branching — a single source set targets
whichever `paper-api` is on the compile classpath — so each target was
tested by overriding the `paperApiVersion` Gradle property on the command
line, e.g.:

```bash
./gradlew clean build -PpaperApiVersion=1.18.2-R0.1-SNAPSHOT
```

All 5 builds succeeded and every resulting jar was verified non-empty and
correct via `unzip -l`/`unzip -p` (plugin classes, `NbtFactory`,
`EnumCompat`, correctly-templated `plugin.yml`, no leaked
bStats/Selenium/Minepack/commons-io/commons-codec/JAXB classes) — a
genuinely-tested result, not an assumption from "it's just Bukkit API so it
must work."

| Target | `-PpaperApiVersion` | Status | Jar verified |
|---|---|---|---|
| 26.2 (latest) | *(default, no override)* | **Built.** | Yes — classes + resources present, no leaked classes |
| 1.21.11 (1.21.x) | `1.21.11-R0.1-SNAPSHOT` | **Built.** | Yes |
| 1.20.1 | `1.20.1-R0.1-SNAPSHOT` | **Built.** Required the `EnumCompat` fix (15 errors before, 0 after). | Yes |
| 1.19.4 | `1.19.4-R0.1-SNAPSHOT` | **Built.** | Yes |
| 1.18.2 | `1.18.2-R0.1-SNAPSHOT` | **Built.** | Yes |

The committed `gradle.properties` defaults to the latest (26.2) coordinate.
`plugin.yml`'s `api-version` is deliberately left at its original `1.13`
value rather than bumped — see `CLAUDE.md` "API-surface notes for dependent
plugins" for why: AdditionsAPI is a shared dependency, and the goal here is
one jar that actually *loads* across the whole target matrix, not just
compiles for it.

### 5. Verification — DONE

- [x] `unzip -l`/`unzip -p` on all 5 built jars: plugin classes,
      `com.comphenix.attribute.NbtFactory` (and its inner classes),
      `us.fihgu.toolbox.compat.EnumCompat`, and a correctly-version-
      templated `plugin.yml` all present in every jar; no leaked
      bStats/Selenium/Minepack/commons-io/commons-codec/JAXB classes in
      any of them.
- [ ] Optional: download a Paper server jar for a target version into the
      session scratchpad only (never committed), `java -jar paper.jar
      --nogui` smoke boot with the plugin dropped in `plugins/`, confirm it
      loads without exceptions. Not attempted this pass (optional per the
      task brief) — build+jar-content verification only.

### 6. Test coverage — DONE (94.68% line coverage of included scope, gated at 100% minus 7 documented exceptions)

JUnit 5 (Jupiter) + JaCoCo, with MockBukkit for anything touching live
`org.bukkit.*` behavior and plain JUnit for pure-logic classes. `./gradlew
check` runs `jacocoTestCoverageVerification`, which enforces 100% line
coverage on every included class except the 7 explicitly-excepted classes
below — each with an exact, JaCoCo-XML-measured `MISSEDCOUNT` ceiling, never
an estimate. 606 tests, 0 failures, 0 errors, **0 skipped** (see the
MockBukkit/LEGACY_* finding below for why the skip count specifically had to
be checked, not just the pass/fail count).

**Numbers** (full clean `./gradlew clean test jacocoTestReport
jacocoTestCoverageVerification`, all green):

- 59 classes analyzed (nonzero, matches the include/exclude lists — see the
  "always re-check the analyzed-class count" gotcha below).
- LINE: 1298/1371 covered (94.68%) across the included scope.
- Every one of the 73 missed lines is accounted for by one of the 7
  `MISSEDCOUNT`/`COVEREDRATIO` exceptions below (9+2+4+9+3+42+4 = 73,
  reconciled exactly against the per-class JaCoCo XML, not assumed).

**Classes excluded from the coverage bar entirely** (measured/visible in the
HTML report, not gated by `check` — see `build.gradle.kts`'s `jacocoExcludes`
for the authoritative list and per-class reasoning):
`AdditionsAPI` (main class — real HTTP server, NMS reflection, ~30 listener
registrations in `onEnable()`), `files/**` and `Debug`/`LangFileUtils`
(static `AdditionsAPI.getInstance()` bootstrap landmine at class-load time),
`MaterialUtils` (3486-line legacy shim whose non-`useNewMethods` branches are
dead on every supported target), `items/**` (entangled with the same
bootstrap landmine plus Bukkit `ItemMeta`/`Attribute` construction),
`listeners/**` and `events/**` (same bootstrap landmine, or thin wrappers
only exercised through it), `recipes/CustomRecipe`/`CustomShapedRecipe`/
`CustomShapelessRecipe`/`CustomFurnaceRecipe` (Bukkit recipe registration +
excluded `items/**`), `ResourcePackManager`/`ResourcePackServer`/
`ResourcePackListener` (real HTTP server + network), `ReflectionUtils`
(parses live CraftBukkit package names MockBukkit doesn't reproduce),
`com.comphenix.attribute.**` and `com.codingforcookies.armorequip.**`
(vendored NMS reflection, needs a live server), `NetworkUtils` (real
outbound HTTP call), `commands/**` (same bootstrap landmine),
`us.fihgu.toolbox.http/**` and `us.fihgu.toolbox.web/**` (hand-rolled NIO
socket server/framework).

**Classes with a partial coverage bar** (included, but with a documented,
exact gap):

| Class | Ceiling | Reason |
|---|---|---|
| `EquipmentSlotUtils` | `COVEREDRATIO` ≥ 0.55 | `valueFromLangFile` routes through the `LangFile`/`AdditionsAPI`-instance bootstrap landmine; its other two methods are fully tested |
| `ArmorDurability` | `MISSEDCOUNT` ≤ 2 | Constructor's two `catch (NoSuchFieldError ignore)` guards (`DamageCause.HOT_FLOOR`/`ENTITY_SWEEP_ATTACK`) are unreachable — both constants exist on every target this project actually builds for (1.18.2+) |
| `RecipeIngredient` | `MISSEDCOUNT` ≤ 4 | `clone()`'s `catch (CloneNotSupportedException e)` is unreachable (the class implements `Cloneable`) |
| `FileUtils` | `MISSEDCOUNT` ≤ 4 | `deserialize()`'s `catch (ClassNotFoundException e)` and `getPluginJarPath()`'s `catch (UnsupportedEncodingException e)` ("UTF-8" is JVM-guaranteed valid) are both unreachable without contrivance |
| `ArmorType` | `MISSEDCOUNT` ≤ 9 | `toIngameString`/`toIngameLeatherString` hit the same `LangFileUtils` landmine (2 lines); six getters plus `getCustomShapedRecipe(RecipeIngredient)` each have a provably-dead `default:` (switch-over-enum with an explicit case for every constant — 7 lines). `getArmorType(Material)`'s default IS reachable and is tested |
| `ToolType` | `MISSEDCOUNT` ≤ 3 | Same `LangFileUtils` landmine in `toIngameString` (1 line); `getDurabilityMechanics()` and `getCustomShapedRecipe(RecipeIngredient, RecipeIngredient)` each have one dead `default:` (2 lines). `getItemPermissions()`'s reachable AXE/PICKAXE/SWORD fallthrough and `getToolType(Material)`'s reachable default are both tested |
| `ItemType` | `MISSEDCOUNT` ≤ 42 | 3 dead switch-case labels (`BROWN_MUSHROOM`, `OBSIDIAN`, `SPIDER_EYE` — confirmed by direct probe to be intercepted by the `isBlock()`/`isEdible()` early-return guards before the switch runs) + 39 `Material.LEGACY_*` case labels that are reachable on a real server but untestable under MockBukkit v4.115.0 (see finding below) |

**Bugs found and fixed while writing tests** (all committed in `f0547ae`):

- `RecipeIngredient.compareTo()` called the deprecated `Material.getId()`,
  which throws `IllegalArgumentException("Cannot get ID of Modern
  Material")` for every Material added since the 1.13 flattening — i.e. it
  threw for any comparison between two non-identical, non-pre-flattening
  ingredients on every server version this project targets. Fixed to use
  `Material.compareTo()`'s natural ordering instead (total, stable, doesn't
  depend on the removed numeric ID). No public signature change.
- `ItemUtils.setLore`/`setDisplayName`/`addLore` each called
  `item.getItemMeta()` *before* `itemMetaNullCheck(item)` ran — capturing a
  (possibly null) `ItemMeta` reference ahead of the guard meant to catch
  exactly that case, so any item whose `getItemMeta()` legitimately returns
  null (e.g. `Material.AIR`) would NPE on the very next line regardless of
  the null check. Fixed by reordering: null-check first, capture the
  `ItemMeta` reference second.
- `ModelInjectorTest` initially had no `MockBukkit.mock()`/`unmock()`
  lifecycle. `ModelInjector.inject()` calls `Material.getMaxDurability()`,
  which lazily triggers a one-time MockBukkit registry/class-init chain
  needing `Bukkit.server` to be non-null — without the lifecycle, whichever
  `inject_*` test ran first failed, and later tests only "passed" because
  they rode on the half-initialized registry state left behind by that
  failure, not because they genuinely worked. This is a false-positive-test
  trap, not a production bug — fixed in the test, not the source.

**Known bugs left un-fixed** (found, deliberately not changed — see each for
why fixing is not obviously safe):

- `RecipeUtils.getRecipeJson(Recipe)` builds a local `JsonObject` with four
  hardcoded placeholder properties (`number`, `latitude`, `longitude`,
  `text`) that it never attaches to anything, then returns a completely
  empty `JsonObject` regardless of the `Recipe` argument — looks like
  unfinished upstream scaffolding, not a regression from this pass. Not
  fixed: the intended output schema for a "recipe as JSON" isn't recoverable
  from the placeholder property names, so any fix would be a guess at new
  behavior for a public method, not a correctness fix.
- `RecipeIngredient.getBlockData()` returns primitive `byte` from a boxed
  `Byte` field that the `RecipeIngredient(ItemStack)` constructor explicitly
  sets to `null` when given a null `ItemStack` — the next call to
  `getBlockData()` (or `compareTo()`'s `blockData` comparison) then NPEs on
  auto-unboxing. Not fixed: changing the return type to `Byte` to allow a
  real null would change this public method's signature, which is out of
  scope for this pass.

**Coverage-measurement gotcha found this pass, worth remembering**:
`ItemType.getItemType(Material)`'s exhaustive parameterized test originally
included all 39 `Material.LEGACY_*` switch cases directly. Every one of them
threw `org.mockbukkit.mockbukkit.exception.UnimplementedOperationException`
(from `UnsafeValuesMock.fromLegacy()`, which MockBukkit v4.115.0 has not
implemented) inside `isBlock()`/`isEdible()`/`isRecord()` — the early-return
guards that run before the switch, for every material including these. The
critical trap: JUnit5's `@MethodSource`-driven `@ParameterizedTest` recorded
each of those 39 thrown cases as a **skipped** test, not a **failed** one —
`./gradlew test` reported `BUILD SUCCESSFUL` with 0 failures throughout,
and only cross-referencing JaCoCo's line-level XML (39 unexpectedly-missed
lines) against the test-results XML's `skipped="39"` attribute (not visible
from the Gradle console output at all) surfaced it. **A green
`./gradlew test` is not sufficient evidence of real coverage — the
per-class test-results XML's `skipped` count must also be checked, not just
the overall pass/fail.** Fixed by replacing the 39 silently-skipped
parameterized cases with one explicit, honestly-worded
`assertThrows(UnimplementedOperationException.class, ...)` test
(`getItemType_legacyMaterial_isUntestableUnderMockBukkit`) that documents the
real MockBukkit limitation with a real assertion — one that will start
failing (and prompt a revisit) the day a future MockBukkit version
implements legacy-material resolution — instead of a comment nobody has to
keep honest.

**Standing gotcha reconfirmed**: a green `jacocoTestCoverageVerification`
means nothing until the analyzed-class count is confirmed nonzero and
matches the include list (`grep -o '<class name="[^"]*"'
build/reports/jacoco/test/jacocoTestReport.xml | wc -l` — 59 here, matching
the 59-class filtered list, confirmed above).

## Open problems / honest blockers

- `NbtFactory`/`ReflectionUtils` reflect into CraftBukkit/NMS internals by
  structural shape, not a stable API contract — the single most likely
  thing to break on some future MC version, and it would fail at runtime,
  not at compile time. Inherited unchanged from upstream; not exercised by
  a runtime smoke-test in this pass (see "Verification" above).
- No Paper server smoke-boot was performed (optional per the task brief) —
  verification here is build-green + jar-content-verified only.

### Folia — NOT SUPPORTED, `folia-supported` flag deliberately not added

Static analysis of every `Bukkit.getScheduler()`/`BukkitRunnable` usage and
every mutable static field in `src/main/java` found concrete, non-trivial
blockers — not fixable as a low-risk change while this project keeps
building one unmodified source tree against Paper/Spigot versions from
1.18.2 through 26.2 (Folia's per-entity/per-region scheduler API classes
don't exist on any of those non-Folia targets, so calling them directly
would break every other build):

1. **Legacy global-scheduler calls that touch specific players/entities**,
   across 5 files: `AdditionsAPI.onEnable()`
   (`scheduleSyncDelayedTask(this, () -> load())`), `DurabilityBar.java`
   (5x `scheduleSyncDelayedTask`, armor-stand/display updates),
   `ResourcePackListener.java` (`runTaskLater` to call
   `player.setResourcePack(...)`, `runTask` to call
   `player.kickPlayer(...)`), `CustomElytraPlayerToggleGlide.java`
   (`Bukkit.getScheduler().cancelTask(...)`), and
   `ArrowFromCustomBowHit.java` (`runTask` to remove a projectile entity).
   Folia disables or repurposes most of `BukkitScheduler`'s methods and
   requires entity-touching work to go through `Entity#getScheduler()` /
   `Bukkit.getRegionScheduler()` instead — none of which exist in the Bukkit
   API on pre-Folia servers.
2. **`ElytraDurabilityTask extends BukkitRunnable`**, scheduled via
   `task.runTaskTimer(AdditionsAPI.getInstance(), 0L, 20L)` in
   `CustomElytraPlayerToggleGlide.java`: its `run()` calls
   `player.isGliding()` every second for a specific player. Under Folia this
   is a direct cross-region entity access from what would be the global
   region thread — the textbook case Folia's threading model forbids.
   Rewriting this as Folia's per-entity repeating task (while keeping the
   plugin buildable against pre-Folia Paper too) is a real re-architecture,
   not a tweak.
3. **Two unsynchronized static `HashMap` fields keyed by per-entity data**:
   `CustomElytraPlayerToggleGlide.playersGliding`
   (`HashMap<UUID, Integer>`) and `ArrowFromCustomBowHit.arrowsShotFromCustomBow`
   (`HashMap<Entity, BowStackContainer>`), both mutated from event handlers
   with no synchronization. Under Paper's single main-thread model this is
   harmless; under Folia, events for different players/entities can fire
   concurrently on different region threads, making concurrent mutation of a
   plain `HashMap` a genuine data race, not a theoretical one.

None of the three are "make it safe" fixes at low risk — (1) and (2) both
need version-conditional (reflection-gated) dispatch to Folia-only scheduler
classes that would otherwise fail to resolve on every other target version
this single jar also builds for, and (2) additionally needs the repeating-
elytra-tick pattern re-architected around Folia's per-entity scheduler
semantics. (3) alone (swap to `ConcurrentHashMap`) would be low-risk, but
doing it in isolation would not make the plugin Folia-safe while (1)/(2)
remain, so it was left undone rather than given as partial/misleading
progress toward a flag that isn't actually being added. Verdict: **do not
add `folia-supported: true`** to `plugin.yml`; Paper/Purpur/Spigot remain the
supported platforms (see the platform matrix in `CLAUDE.md`).
- The relocated `legacy-hooks/MinePackInitializationMethod.java.txt` would
  need a working Minepack-equivalent hosting service to ever be restored;
  none is known to exist. Effectively permanent, not a near-term TODO.
- Fixed the stale `use-minepack` doc-comment block in `config.yml` (lines
  ~12-14 and ~27-29) that still described the old Minepack behavior even
  though the functional code path had already been removed/no-opped in an
  earlier pass — a documentation-only inconsistency, now resolved.

## Repository / git notes

- Repo is a fork of `chrismin13/AdditionsAPI`; never push to upstream.
- Branch `master` renamed to `main` (target for this pass; see commit
  section below for actual execution order), GitHub default branch
  updated via `gh repo edit bshuler/AdditionsAPI --default-branch main`.
  `master` (old default) left in place, not deleted.
- Do not commit anything under `.github/workflows/`.
- Commits authored as `Bert Shuler <BertShuler@proton.me>`, signed via the
  1Password SSH agent. **Signing was confirmed down this pass** (`op
  whoami` failed) — each commit is attempted exactly once; on failure the
  prepared commit message is appended to the session scratchpad's
  `AdditionsAPI-commit-msg.txt` instead of being force-committed unsigned,
  and all other non-commit work continues. See the final task report for
  which commits landed vs. were parked.
