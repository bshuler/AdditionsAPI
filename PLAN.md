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

## Open problems / honest blockers

- `NbtFactory`/`ReflectionUtils` reflect into CraftBukkit/NMS internals by
  structural shape, not a stable API contract — the single most likely
  thing to break on some future MC version, and it would fail at runtime,
  not at compile time. Inherited unchanged from upstream; not exercised by
  a runtime smoke-test in this pass (see "Verification" above).
- Folia compatibility is unverified (see the platform matrix above).
- No Paper server smoke-boot was performed (optional per the task brief) —
  verification here is build-green + jar-content-verified only.
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
