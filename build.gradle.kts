import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    jacoco
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.chrismin13"
version = providers.gradleProperty("pluginVersion").get()

// Java 25: the latest resolvable paper-api (26.2.build.111-stable, matching
// Minecraft's current calendar-versioned release) publishes Gradle module
// metadata requiring JVM 25 (same finding as the EpicFurnaces/InvUnload
// siblings). The foojay-resolver-convention plugin (declared in
// settings.gradle.kts) auto-provisions this JDK into Gradle's own toolchain
// cache; it does not touch the system/Homebrew JDK. See PLAN.md/CLAUDE.md.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperApiVersion").get()}")

    // NBT/attribute reflection helper (com.comphenix.attribute.NbtFactory) and
    // the ArmorEquip event bridge only import stable org.bukkit.* types plus
    // java.lang.reflect - no third-party runtime dependency needed for those.

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Test infrastructure. JUnit (Jupiter) for everything; MockBukkit for
    // simulating a Bukkit server so utils code touching org.bukkit.* (e.g.
    // Bukkit.getItemFactory(), Player.hasPermission()) can be exercised
    // without a real server. MockBukkit renamed its Maven group from
    // `com.github.seeseemelk` to `org.mockbukkit.mockbukkit` (still
    // published live on Maven Central); the newest per-version artifact
    // under the new group is `mockbukkit-v26.1.2`. Tests deliberately pin
    // `paper-api:26.1.2.build.74-stable` here rather than inheriting
    // whichever `-PpaperApiVersion` the main sourceSet is compiled against
    // (default 26.2.build.111-stable): MockBukkit ships its own bundled
    // registry-data snapshot (materials, items, etc.) captured from that
    // exact Paper build, and letting Gradle resolve the newer main-sourceSet
    // paper-api onto the test classpath throws a real
    // `InternalDataLoadException: ... MockBukkit / MC version mismatch?` at
    // ServerMock construction - confirmed by hitting exactly that failure
    // before pinning this. So: tests run against exactly ONE fixed target,
    // 26.1.2, independent of whatever -PpaperApiVersion the main build is
    // using. See CLAUDE.md "Testing".
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0")
    testImplementation("org.mockito:mockito-core:5.20.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
    // This is a 2015-era codebase carrying its own vendored reflection/NMS
    // helpers (com.comphenix.attribute, us.fihgu.toolbox.reflection) and a
    // large amount of pre-1.13 "flattening" Material-handling code that
    // intentionally still references now-deprecated LEGACY_* constants and
    // numeric-durability APIs for older-version compatibility. Do not fail
    // the build on deprecation/unchecked/rawtypes warnings - see CLAUDE.md.
    options.compilerArgs.addAll(listOf("-Xlint:none"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("AdditionsAPI")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// Classes that are not included in the enforced 100% coverage bar. Each
// exclusion is documented with its reason in PLAN.md ("Test coverage" /
// exclusions table) - never silent. Coverage for these classes is still
// measured and visible in the HTML/XML report, just not gated by `check`.
val jacocoExcludes = listOf(
    // Main plugin class: onEnable() starts a real HTTP resource-pack
    // server, does NbtFactory NMS reflection, and registers ~30 listeners -
    // not safely bootstrappable in a unit test.
    "com/chrismin13/additionsapi/AdditionsAPI.class",
    // Static-initializer landmine: each of these has a
    // `private static JavaPlugin plugin = AdditionsAPI.getInstance();`
    // field initialized at class-load time, and stays effectively
    // unusable (null config/data) until the real onEnable() calls its
    // setup()/reload() - untestable without a full plugin bootstrap.
    "com/chrismin13/additionsapi/files/**",
    // Routes every call through LangFile's static bootstrap (see above).
    "com/chrismin13/additionsapi/utils/LangFileUtils.class",
    // 3486-line legacy compatibility shim: the majority of its switch
    // tables are the pre-1.13 "flattening" fallback branch, confirmed dead
    // on every one of the 5 Paper targets this project builds against
    // (useNewMethods is always true on all of them) - disproportionate
    // effort for code that cannot actually execute on any supported target.
    "com/chrismin13/additionsapi/utils/MaterialUtils.class",
    // The primary public API surface (CustomItem and its subclasses) is
    // entangled with ConfigFile's static AdditionsAPI-instance bootstrap
    // (excluded above) and with Bukkit ItemMeta/Attribute construction in
    // ways that go well beyond what a unit test can safely drive without a
    // fuller plugin bootstrap. Flagged as future work, not attempted here.
    "com/chrismin13/additionsapi/items/**",
    // Every listener calls AdditionsAPI.isCustomItem()/getInstance()
    // (static bootstrap, see above) as its first line.
    "com/chrismin13/additionsapi/listeners/**",
    // Thin Bukkit-event wrapper POJOs (delegate isCancelled/setCancelled/
    // getters to the wrapped vanilla event) exercised only through the
    // excluded listeners above; no independent logic of their own.
    "com/chrismin13/additionsapi/events/**",
    // Tied to Bukkit recipe registration and to the excluded items/*
    // package (RecipeIngredient itself, which has no such dependency, is
    // tested and NOT covered by this exclusion).
    "com/chrismin13/additionsapi/recipes/CustomRecipe.class",
    "com/chrismin13/additionsapi/recipes/CustomShapedRecipe.class",
    "com/chrismin13/additionsapi/recipes/CustomShapelessRecipe.class",
    "com/chrismin13/additionsapi/recipes/CustomFurnaceRecipe.class",
    // Builds/serves the resource pack over a real HTTP listener and derives
    // its working-directory File from AdditionsAPI's static instance -
    // needs a live server and network, not unit-testable. Wildcard (not an
    // exact .class match) so the anonymous SimpleFileVisitor/Runnable inner
    // classes these three generate (e.g. ResourcePackManager$1) are also
    // excluded - an exact-match pattern here previously missed those and
    // left them counted as un-excluded-but-untested in the analyzed-class
    // list.
    "us/fihgu/toolbox/resourcepack/ResourcePackManager*",
    "us/fihgu/toolbox/resourcepack/ResourcePackServer*",
    "us/fihgu/toolbox/resourcepack/ResourcePackListener*",
    // NMS/CraftBukkit package-name reflection: getMinecraftVersion() parses
    // Bukkit.getServer().getClass()'s package name expecting CraftBukkit's
    // "org.bukkit.craftbukkit.vX_Y_RZ.*" layout, which MockBukkit's
    // ServerMock does not reproduce (it lives in
    // org.mockbukkit.mockbukkit.*) - untestable without a live NMS-backed
    // server.
    "us/fihgu/toolbox/reflection/ReflectionUtils.class",
    // Vendored NMS-reflection helpers (structurally reflect into
    // CraftBukkit/NMS internals by shape, not a stable API contract) - the
    // single most version-fragile part of this codebase per PLAN.md, and
    // needs a live NMS-backed server to exercise meaningfully.
    "com/comphenix/attribute/**",
    // Vendored Bukkit event/listener bridge for armor-equip detection -
    // needs a live server to register/fire.
    "com/codingforcookies/armorequip/**",
    // Third method (valueFromLangFile) routes through LangFile's static
    // bootstrap; the other two methods on this class are tested. See the
    // per-class violationRules exception below for the precise partial
    // bar - this entry only affects the jacocoTestReport HTML/XML view.
    //
    // Same ConfigFile/AdditionsAPI-instance static-bootstrap landmine as
    // files/** above: `Debug.log`/`Debug.type` are initialized at
    // class-load time from Bukkit.getLogger()/ConfigFile.getInstance().
    "com/chrismin13/additionsapi/utils/Debug.class",
    // BukkitRunnable whose run() calls a live PluginManager.callEvent(...)
    // and reaches into the excluded items/** and listeners/**-adjacent
    // event classes - needs a live server tick loop, not unit-testable.
    "com/chrismin13/additionsapi/utils/ElytraDurabilityTask.class",
    // Both commands route every real branch through
    // AdditionsAPI.isCustomItem()/getInstance() (the same static bootstrap
    // as the listeners package) and construct CustomItemStack (excluded
    // items/** package) - same blocker, same exclusion rationale.
    "com/chrismin13/additionsapi/commands/**",
    // getExternalIP() makes a real outbound HTTP call and, on failure,
    // calls Debug.sayError (the ConfigFile/AdditionsAPI-instance bootstrap
    // landmine excluded above) - exercising it in a sandboxed test
    // environment with no network access would deterministically hit that
    // landmine and NPE. getLocalIP() alone is pure NetworkInterface
    // enumeration and would be testable in isolation, but it is bundled in
    // the same class as its untestable sibling; excluded as one unit
    // rather than fragmenting the exclude list further for one method.
    "us/fihgu/toolbox/network/NetworkUtils.class",
    // Hand-rolled NIO HTTP server (accept/selector loop, request parsing,
    // response writing) backing the resource-pack server above - needs
    // live sockets and a running event loop, not unit-testable.
    "us/fihgu/toolbox/http/**",
    // Hand-rolled NIO selector-thread web framework the HTTP server above
    // is built on - same live-socket/event-loop blocker.
    "us/fihgu/toolbox/web/**",
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExcludes) }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExcludes) }
        })
    )
    violationRules {
        // Every included class must be 100% line-covered, with a handful of
        // documented exceptions below - each is genuinely unreachable
        // defensive code, not a test gap, and is spelled out in PLAN.md
        // ("Test coverage" / exclusions table).
        rule {
            element = "CLASS"
            excludes = listOf(
                "com.chrismin13.additionsapi.utils.EquipmentSlotUtils",
                "com.chrismin13.additionsapi.durability.ArmorDurability",
                "com.chrismin13.additionsapi.recipes.RecipeIngredient",
                "us.fihgu.toolbox.file.FileUtils",
                "com.chrismin13.additionsapi.enums.ArmorType",
                "com.chrismin13.additionsapi.enums.ToolType",
                "com.chrismin13.additionsapi.enums.ItemType",
            )
            limit {
                counter = "LINE"
                minimum = "1.00".toBigDecimal()
            }
        }
        // EquipmentSlotUtils.valueFromLangFile(...) routes through
        // LangFile's static plugin-bootstrap field and cannot be exercised
        // without a full plugin bootstrap (see PLAN.md). Its other two
        // methods (toAttributeString, valueFromAttribute) ARE tested and
        // fully covered.
        rule {
            element = "CLASS"
            includes = listOf("com.chrismin13.additionsapi.utils.EquipmentSlotUtils")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                // toAttributeString + valueFromAttribute are the two small,
                // fully-covered methods; valueFromLangFile's switch (7 case
                // lines + return) is the documented gap.
                minimum = "0.55".toBigDecimal()
            }
        }
        // ArmorDurability's constructor guards two enum constants
        // (DamageCause.HOT_FLOOR, DamageCause.ENTITY_SWEEP_ATTACK) with
        // `catch (NoSuchFieldError ignore)`, in case a target server is old
        // enough not to have added them yet. Both constants exist on every
        // Bukkit/Paper version this project actually targets (1.18.2+), so
        // both catch blocks are provably unreachable here - exact count via
        // JaCoCo XML, not an estimate.
        rule {
            element = "CLASS"
            includes = listOf("com.chrismin13.additionsapi.durability.ArmorDurability")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = "2".toBigDecimal()
            }
        }
        // RecipeIngredient.clone()'s `catch (CloneNotSupportedException e)`
        // is unreachable: RecipeIngredient implements Cloneable, and
        // Object.clone() only ever throws CloneNotSupportedException for a
        // non-Cloneable receiver.
        rule {
            element = "CLASS"
            includes = listOf("com.chrismin13.additionsapi.recipes.RecipeIngredient")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = "4".toBigDecimal()
            }
        }
        // FileUtils has two genuinely-unreachable defensive catches:
        // deserialize()'s `catch (ClassNotFoundException e)` (would require
        // a serialized stream referencing a class that existed when written
        // but not when read back - not realistically triggerable from a
        // test without contrivance far past the "misuse a real file" tricks
        // used for its sibling catches elsewhere in this class) and
        // getPluginJarPath()'s `catch (UnsupportedEncodingException e)`
        // ("UTF-8" is a JVM-guaranteed-valid encoding name, so this can
        // never actually throw).
        rule {
            element = "CLASS"
            includes = listOf("us.fihgu.toolbox.file.FileUtils")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = "4".toBigDecimal()
            }
        }
        // ArmorType: toIngameString/toIngameLeatherString route through
        // LangFileUtils' static plugin-bootstrap landmine (2 lines), and six
        // getters (getLeatherMaterial/getChainmailMaterial/getGoldMaterial/
        // getIronMaterial/getDiamondMaterial/getEquipmentSlot) plus
        // getCustomShapedRecipe(RecipeIngredient) each have a provably-dead
        // `default:` branch - switch(this) over an enum with an explicit case
        // for every constant can never reach it. getArmorType(Material)'s
        // default IS reachable and is tested directly. Exact count via JaCoCo
        // XML (9), not an estimate. See ArmorTypeTest and PLAN.md.
        rule {
            element = "CLASS"
            includes = listOf("com.chrismin13.additionsapi.enums.ArmorType")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = "9".toBigDecimal()
            }
        }
        // ToolType: toIngameString has the same LangFileUtils landmine (1
        // line), and getDurabilityMechanics()/getCustomShapedRecipe
        // (RecipeIngredient, RecipeIngredient) each have a provably-dead
        // `default:` branch, same reasoning as ArmorType above.
        // getItemPermissions()'s AXE/PICKAXE/SWORD fallthrough-to-default and
        // getToolType(Material)'s default (any non-tool Material) are both
        // genuinely reachable and are tested directly. Exact count via
        // JaCoCo XML (3), not an estimate. See ToolTypeTest and PLAN.md.
        rule {
            element = "CLASS"
            includes = listOf("com.chrismin13.additionsapi.enums.ToolType")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = "3".toBigDecimal()
            }
        }
        // ItemType.getItemType(Material): 3 of its switch's own case labels
        // (BROWN_MUSHROOM, OBSIDIAN, SPIDER_EYE) are provably dead - all
        // three are intercepted by the isBlock()/isEdible() early-return
        // guards above the switch before it is ever reached (confirmed by
        // direct probe against real MockBukkit Material data, not a
        // source-reading guess; see ItemTypeTest). The other 39 missed lines
        // are the switch's Material.LEGACY_* case labels: reachable on a
        // real server, but untestable under MockBukkit v4.115.0, whose
        // UnsafeValuesMock does not implement legacy-material metadata
        // resolution (isBlock()/isEdible()/isRecord() throw
        // UnimplementedOperationException for any LEGACY_* argument before
        // the switch runs) - demonstrated with a real assertThrows, not
        // silently omitted; see
        // getItemType_legacyMaterial_isUntestableUnderMockBukkit in
        // ItemTypeTest. 3 + 39 = 42, exact count via JaCoCo XML, not an
        // estimate. See PLAN.md.
        rule {
            element = "CLASS"
            includes = listOf("com.chrismin13.additionsapi.enums.ItemType")
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = "42".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

sourceSets {
    main {
        java {
            // legacy-hooks/ holds dead/defunct integrations kept for reference
            // only (currently none under src/, they live in the top-level
            // legacy-hooks/ directory which is not part of any source set).
        }
    }
}

// Tier 2: boots a real headless Paper server with the packaged jar installed.
// Opt-in (needs -PpaperServerJar=...), deliberately not wired into `check`.
// See gradle/paper-boot-test.gradle.kts and PLAN.md.
apply(from = "gradle/paper-boot-test.gradle.kts")
