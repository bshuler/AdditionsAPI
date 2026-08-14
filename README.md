# AdditionsAPI

AdditionsAPI is a Bukkit/Spigot/Paper **custom-items API library plugin**.
It lets other plugins define custom items with custom textures and
durability without hardcoded vanilla-durability hacks: it generates and
serves the resource pack that backs those textures, provides its own
fake-durability system for unbreakable items, custom recipes, per-item
permissions, and a full set of custom-item events bridged from vanilla
Bukkit events.

JavaDoc: https://bshuler.github.io/AdditionsAPI/index.html

## Provenance

This repo is a genuine GitHub fork of
[chrismin13/AdditionsAPI](https://github.com/chrismin13/AdditionsAPI).
Upstream is **abandoned** (its own README says the original author no
longer maintains it), so this fork is where version updates happen. All 56
upstream commits that postdated the fork were forward-merged here before
modernization began — nothing upstream was lost.

## Supported Paper versions

One codebase, no version branches. The default build targets the newest
stable Paper API (currently **26.2**); the same source also compiles
cleanly against older API lines via a property override:

```sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew build                                             # 26.2 (default)
./gradlew build -PpaperApiVersion=1.21.11-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.20.1-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.19.4-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.18.2-R0.1-SNAPSHOT
```

All five targets are verified builds, not aspirational. The jar lands in
`build/libs/`. `plugin.yml` deliberately keeps `api-version: 1.13` so one
jar loads on every server from 1.13 through current — see `CLAUDE.md` for
the reasoning and the compatibility work behind it (e.g. the `EnumCompat`
fix that keeps 1.20.1-era enum lookups working).

## Modernization work

- Forward-merged all upstream commits, then moved the build to modern
  Gradle (9.x wrapper, Java toolchains — no system JDK installs needed).
- Large JUnit unit-test suite added across the API surface: durability
  mechanics per item category, item/armor/tool type classification,
  permissions, recipes, resource-pack model serialization, and utility
  classes.
- Full history, per-milestone status, and the honest platform matrix are
  in `PLAN.md`; architecture and API-surface notes for dependent plugins
  are in `CLAUDE.md`.

## Testing

Two tiers:

1. **Unit tests** — `./gradlew check` (runs on every build). JUnit 5 over
   the headless-testable API surface listed above.
2. **Headless Paper boot smoke test** (opt-in — needs a real Paper server
   jar, e.g. from https://fill.papermc.io/v3/projects/paper):

   ```sh
   ./gradlew paperBootTest -PpaperServerJar=/path/to/paper-26.2-111.jar
   ```

   Boots a real headless Paper server with the packaged jar in `plugins/`
   and asserts: the jar loads, `onEnable` doesn't throw, the expected
   command (`/additions`) is registered in the live command map and doesn't
   throw when invoked, the plugin shows in `plugins`, and `onDisable` exits
   cleanly. Without a server jar the task reports
   `SKIPPED (this is a skip, not a pass)`. Transcript:
   `build/paper-boot/paper-boot-test.log`.
