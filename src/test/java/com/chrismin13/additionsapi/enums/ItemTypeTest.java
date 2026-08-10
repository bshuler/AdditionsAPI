package com.chrismin13.additionsapi.enums;

import com.chrismin13.additionsapi.durability.ArmorDurability;
import com.chrismin13.additionsapi.durability.BowDurability;
import com.chrismin13.additionsapi.durability.ElytraDurability;
import com.chrismin13.additionsapi.durability.FishingRodDurability;
import com.chrismin13.additionsapi.durability.FlintAndSteelDurability;
import com.chrismin13.additionsapi.durability.ItemDurability;
import com.chrismin13.additionsapi.durability.ShearDurability;
import com.chrismin13.additionsapi.durability.ShieldDurability;
import com.chrismin13.additionsapi.permissions.ArmorPermissions;
import com.chrismin13.additionsapi.permissions.BowPermissions;
import com.chrismin13.additionsapi.permissions.ElytraPermissions;
import com.chrismin13.additionsapi.permissions.FishingRodPermissions;
import com.chrismin13.additionsapi.permissions.FlintAndSteelPermissions;
import com.chrismin13.additionsapi.permissions.ItemPermissions;
import com.chrismin13.additionsapi.permissions.PermissionType;
import com.chrismin13.additionsapi.permissions.ShearPermissions;
import com.chrismin13.additionsapi.permissions.ShieldPermissions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@code getItemType(Material)}'s early-return guards ({@code isBlock()},
 * {@code isEdible()}, {@code isRecord()}) and its 216-case switch both need
 * live {@code Material} metadata, so this whole class runs under MockBukkit
 * (same lifecycle pattern as {@code ItemUtilsTest}/{@code DamageableItemTest}
 * - see build.gradle.kts's {@code ModelInjector} note for why omitting this
 * is a false-positive-test-pass trap).
 */
class ItemTypeTest {

	@BeforeAll
	static void setUp() {
		MockBukkit.mock();
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void getItemType_fromItemStack_delegatesToMaterialOverload() {
		ItemStack item = new ItemStack(Material.BOW);
		assertEquals(ItemType.BOW, ItemType.getItemType(item));
	}

	@Test
	void getItemType_blockMaterial_returnsBlockViaEarlyGuard() {
		// STONE.isBlock() == true short-circuits before the switch is ever
		// reached, regardless of whether STONE also appears in the switch.
		assertEquals(ItemType.BLOCK, ItemType.getItemType(Material.STONE));
	}

	@Test
	void getItemType_edibleMaterial_returnsFoodViaEarlyGuard() {
		assertEquals(ItemType.FOOD, ItemType.getItemType(Material.APPLE));
	}

	@Test
	void getItemType_recordMaterial_returnsRecordViaEarlyGuard() {
		assertEquals(ItemType.RECORD, ItemType.getItemType(Material.MUSIC_DISC_13));
	}

	@Test
	void getItemType_unmappedNonBlockNonEdibleNonRecordMaterial_returnsNull() {
		// ARMOR_STAND is not a block/edible/record and has no switch case of
		// its own, so it falls through to the switch's `default: break;`
		// and the trailing `return null;` - the switch's one genuinely
		// reachable default path (verified empirically: not present in any
		// of the 216 case labels, and isBlock()/isEdible()/isRecord() are
		// all false for it under MockBukkit).
		assertNull(ItemType.getItemType(Material.ARMOR_STAND));
	}

	@ParameterizedTest(name = "getItemType({0}) == {1}")
	@MethodSource("switchCaseMaterials")
	void getItemType_coversEverySwitchCase(Material material, ItemType expected) {
		assertEquals(expected, ItemType.getItemType(material));
	}

	// The switch also has 39 `Material.LEGACY_*` case labels (ItemType.java
	// lines 440-516) that are deliberately NOT in switchCaseMaterials() below.
	// They are not dead code in production - on a real server they're
	// perfectly reachable - but they cannot be exercised under MockBukkit:
	// isBlock()/isEdible()/isRecord() (the guards that run before the switch,
	// for every material including these) route legacy-material metadata
	// resolution through UnsafeValuesMock.fromLegacy(), which MockBukkit
	// v4.115.0 has not implemented, throwing UnimplementedOperationException.
	// A first attempt included all 39 LEGACY_* cases directly inside the
	// @ParameterizedTest above; every one of them threw before its assertion
	// ran, and JUnit5's @MethodSource machinery recorded each as a *skipped*
	// test case rather than a failure - the build stayed green
	// (`BUILD SUCCESSFUL`, 0 failures) while silently verifying nothing for
	// those 39 cases. That is a false-positive-coverage trap, not a passing
	// test, so they were pulled out and replaced with the explicit,
	// non-silent demonstration below: this documents the real limitation
	// with a real assertion (one that will start failing, and need revisiting,
	// the day a future MockBukkit version implements legacy-material
	// resolution) instead of a comment nobody has to keep honest. See the
	// jacocoTestCoverageVerification MISSEDCOUNT exception for ItemType in
	// build.gradle.kts and the "Test coverage" section of PLAN.md.
	@Test
	void getItemType_legacyMaterial_isUntestableUnderMockBukkit() {
		assertThrows(org.mockbukkit.mockbukkit.exception.UnimplementedOperationException.class,
				() -> ItemType.getItemType(Material.LEGACY_WOOD_SWORD));
	}

	static Stream<Arguments> switchCaseMaterials() {
		return Stream.of(
				Arguments.of(Material.BLAZE_POWDER, ItemType.RAW_MATERIAL),
				Arguments.of(Material.BLAZE_ROD, ItemType.RAW_MATERIAL),
				Arguments.of(Material.OAK_BOAT, ItemType.BOAT),
				Arguments.of(Material.ACACIA_BOAT, ItemType.BOAT),
				Arguments.of(Material.BIRCH_BOAT, ItemType.BOAT),
				Arguments.of(Material.DARK_OAK_BOAT, ItemType.BOAT),
				Arguments.of(Material.JUNGLE_BOAT, ItemType.BOAT),
				Arguments.of(Material.SPRUCE_BOAT, ItemType.BOAT),
				Arguments.of(Material.BONE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.BOOK, ItemType.BOOK),
				Arguments.of(Material.WRITABLE_BOOK, ItemType.BOOK),
				Arguments.of(Material.BOW, ItemType.BOW),
				Arguments.of(Material.BOWL, ItemType.RAW_MATERIAL),
				Arguments.of(Material.BUCKET, ItemType.BUCKET),
				Arguments.of(Material.CARROT_ON_A_STICK, ItemType.UTILITY),
				Arguments.of(Material.CHAINMAIL_BOOTS, ItemType.ARMOR),
				Arguments.of(Material.CHAINMAIL_CHESTPLATE, ItemType.ARMOR),
				Arguments.of(Material.CHAINMAIL_HELMET, ItemType.ARMOR),
				Arguments.of(Material.CHAINMAIL_LEGGINGS, ItemType.ARMOR),
				Arguments.of(Material.CLAY_BALL, ItemType.RAW_MATERIAL),
				Arguments.of(Material.COAL, ItemType.RAW_MATERIAL),
				Arguments.of(Material.COMPASS, ItemType.UTILITY),
				Arguments.of(Material.DIAMOND, ItemType.RAW_MATERIAL),
				Arguments.of(Material.DIAMOND_AXE, ItemType.TOOL),
				Arguments.of(Material.DIAMOND_HORSE_ARMOR, ItemType.BARDING),
				Arguments.of(Material.DIAMOND_BOOTS, ItemType.ARMOR),
				Arguments.of(Material.DIAMOND_CHESTPLATE, ItemType.ARMOR),
				Arguments.of(Material.DIAMOND_HELMET, ItemType.ARMOR),
				Arguments.of(Material.DIAMOND_HOE, ItemType.TOOL),
				Arguments.of(Material.DIAMOND_LEGGINGS, ItemType.ARMOR),
				Arguments.of(Material.DIAMOND_PICKAXE, ItemType.TOOL),
				Arguments.of(Material.DIAMOND_SHOVEL, ItemType.TOOL),
				Arguments.of(Material.DIAMOND_SWORD, ItemType.TOOL),
				Arguments.of(Material.DRAGON_BREATH, ItemType.POTION),
				Arguments.of(Material.EGG, ItemType.PROJECTILE),
				Arguments.of(Material.ELYTRA, ItemType.ELYTRA),
				Arguments.of(Material.ENCHANTED_BOOK, ItemType.BOOK),
				Arguments.of(Material.ENDER_PEARL, ItemType.UTILITY),
				Arguments.of(Material.ENDER_EYE, ItemType.UTILITY),
				Arguments.of(Material.FEATHER, ItemType.RAW_MATERIAL),
				Arguments.of(Material.FIRE_CHARGE, ItemType.UTILITY),
				Arguments.of(Material.FIREWORK_ROCKET, ItemType.UTILITY),
				Arguments.of(Material.FIREWORK_STAR, ItemType.RAW_MATERIAL),
				Arguments.of(Material.FISHING_ROD, ItemType.FISHING_ROD),
				Arguments.of(Material.FLINT, ItemType.RAW_MATERIAL),
				Arguments.of(Material.FLINT_AND_STEEL, ItemType.FLINT_AND_STEEL),
				Arguments.of(Material.GHAST_TEAR, ItemType.RAW_MATERIAL),
				Arguments.of(Material.GLASS_BOTTLE, ItemType.UTILITY),
				Arguments.of(Material.GLOWSTONE_DUST, ItemType.RAW_MATERIAL),
				Arguments.of(Material.GOLDEN_AXE, ItemType.TOOL),
				Arguments.of(Material.GOLDEN_HORSE_ARMOR, ItemType.BARDING),
				Arguments.of(Material.GOLDEN_BOOTS, ItemType.ARMOR),
				Arguments.of(Material.GOLDEN_CHESTPLATE, ItemType.ARMOR),
				Arguments.of(Material.GOLDEN_HELMET, ItemType.ARMOR),
				Arguments.of(Material.GOLDEN_HOE, ItemType.TOOL),
				Arguments.of(Material.GOLD_INGOT, ItemType.RAW_MATERIAL),
				Arguments.of(Material.GOLDEN_LEGGINGS, ItemType.ARMOR),
				Arguments.of(Material.GOLD_NUGGET, ItemType.RAW_MATERIAL),
				Arguments.of(Material.GOLDEN_PICKAXE, ItemType.TOOL),
				Arguments.of(Material.GOLDEN_SHOVEL, ItemType.TOOL),
				Arguments.of(Material.GOLDEN_SWORD, ItemType.TOOL),
				Arguments.of(Material.INK_SAC, ItemType.RAW_MATERIAL),
				Arguments.of(Material.CYAN_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.GRAY_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.LIME_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.MAGENTA_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.ORANGE_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.PINK_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.PURPLE_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.LIGHT_BLUE_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.LIGHT_GRAY_DYE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.IRON_AXE, ItemType.TOOL),
				Arguments.of(Material.IRON_HORSE_ARMOR, ItemType.BARDING),
				Arguments.of(Material.IRON_BOOTS, ItemType.ARMOR),
				Arguments.of(Material.IRON_CHESTPLATE, ItemType.ARMOR),
				Arguments.of(Material.IRON_HELMET, ItemType.ARMOR),
				Arguments.of(Material.IRON_HOE, ItemType.TOOL),
				Arguments.of(Material.IRON_INGOT, ItemType.RAW_MATERIAL),
				Arguments.of(Material.IRON_LEGGINGS, ItemType.ARMOR),
				Arguments.of(Material.IRON_PICKAXE, ItemType.TOOL),
				Arguments.of(Material.IRON_SHOVEL, ItemType.TOOL),
				Arguments.of(Material.IRON_SWORD, ItemType.TOOL),
				Arguments.of(Material.LAVA_BUCKET, ItemType.BUCKET),
				Arguments.of(Material.LEAD, ItemType.UTILITY),
				Arguments.of(Material.LEATHER, ItemType.RAW_MATERIAL),
				Arguments.of(Material.LEATHER_BOOTS, ItemType.ARMOR),
				Arguments.of(Material.LEATHER_CHESTPLATE, ItemType.ARMOR),
				Arguments.of(Material.LEATHER_HELMET, ItemType.ARMOR),
				Arguments.of(Material.LEATHER_LEGGINGS, ItemType.ARMOR),
				Arguments.of(Material.LINGERING_POTION, ItemType.POTION),
				Arguments.of(Material.MAGMA_CREAM, ItemType.RAW_MATERIAL),
				Arguments.of(Material.MAP, ItemType.UTILITY),
				Arguments.of(Material.MILK_BUCKET, ItemType.BUCKET),
				Arguments.of(Material.BAT_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.COD_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.CHICKEN_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.BLAZE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.COW_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.CREEPER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.DOLPHIN_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.DONKEY_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.DROWNED_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ENDERMAN_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ENDERMITE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.EVOKER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.GHAST_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.GUARDIAN_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.HORSE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.HUSK_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.LLAMA_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.MOOSHROOM_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.MULE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.OCELOT_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.PARROT_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.PHANTOM_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.PIG_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.PUFFERFISH_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.RABBIT_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SALMON_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SHEEP_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SHULKER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SILVERFISH_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SKELETON_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SLIME_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SPIDER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SQUID_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.STRAY_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.TURTLE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.VEX_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.VILLAGER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.VINDICATOR_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.WITCH_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.WOLF_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ZOMBIE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.CAVE_SPIDER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ELDER_GUARDIAN_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.MAGMA_CUBE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.POLAR_BEAR_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.SKELETON_HORSE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.TROPICAL_FISH_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.WITHER_SKELETON_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ZOMBIE_HORSE_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.ZOMBIE_VILLAGER_SPAWN_EGG, ItemType.UTILITY),
				Arguments.of(Material.NAME_TAG, ItemType.UTILITY),
				Arguments.of(Material.NETHER_STAR, ItemType.RAW_MATERIAL),
				Arguments.of(Material.PAPER, ItemType.RAW_MATERIAL),
				Arguments.of(Material.POTION, ItemType.POTION),
				Arguments.of(Material.PRISMARINE_CRYSTALS, ItemType.RAW_MATERIAL),
				Arguments.of(Material.PRISMARINE_SHARD, ItemType.RAW_MATERIAL),
				Arguments.of(Material.QUARTZ, ItemType.RAW_MATERIAL),
				Arguments.of(Material.REDSTONE, ItemType.RAW_MATERIAL),
				Arguments.of(Material.SADDLE, ItemType.UTILITY),
				Arguments.of(Material.SHEARS, ItemType.SHEARS),
				Arguments.of(Material.SHIELD, ItemType.SHIELD),
				Arguments.of(Material.SLIME_BALL, ItemType.RAW_MATERIAL),
				Arguments.of(Material.SNOWBALL, ItemType.PROJECTILE),
				Arguments.of(Material.SPECTRAL_ARROW, ItemType.ARROW),
				Arguments.of(Material.SPLASH_POTION, ItemType.POTION),
				Arguments.of(Material.STICK, ItemType.RAW_MATERIAL),
				Arguments.of(Material.STONE_AXE, ItemType.TOOL),
				Arguments.of(Material.STONE_HOE, ItemType.TOOL),
				Arguments.of(Material.STONE_PICKAXE, ItemType.TOOL),
				Arguments.of(Material.STONE_SHOVEL, ItemType.TOOL),
				Arguments.of(Material.STONE_SWORD, ItemType.TOOL),
				Arguments.of(Material.TIPPED_ARROW, ItemType.ARROW),
				Arguments.of(Material.CLOCK, ItemType.UTILITY),
				Arguments.of(Material.WATER_BUCKET, ItemType.BUCKET),
				Arguments.of(Material.WOODEN_AXE, ItemType.TOOL),
				Arguments.of(Material.WOODEN_HOE, ItemType.TOOL),
				Arguments.of(Material.WOODEN_PICKAXE, ItemType.TOOL),
				Arguments.of(Material.WOODEN_SHOVEL, ItemType.TOOL),
				Arguments.of(Material.WOODEN_SWORD, ItemType.TOOL),
				Arguments.of(Material.WRITTEN_BOOK, ItemType.BOOK)
		);
	}

	// Three of the switch's own case labels - BROWN_MUSHROOM, OBSIDIAN, and
	// SPIDER_EYE - are permanently unreachable dead code: all three are
	// intercepted by the isBlock()/isEdible() early-return guards above
	// (BROWN_MUSHROOM and OBSIDIAN are placeable blocks; SPIDER_EYE is
	// edible) before the switch is ever reached, so `getItemType` actually
	// returns BLOCK/BLOCK/FOOD for them, never the RAW_MATERIAL the switch's
	// own case label says. Confirmed by direct probe against real MockBukkit
	// Material data (not a source-reading guess). See the
	// jacocoTestCoverageVerification MISSEDCOUNT exception for this class in
	// build.gradle.kts and PLAN.md.
	@Test
	void getItemType_deadSwitchCases_areActuallyInterceptedByEarlyGuards() {
		assertEquals(ItemType.BLOCK, ItemType.getItemType(Material.BROWN_MUSHROOM));
		assertEquals(ItemType.BLOCK, ItemType.getItemType(Material.OBSIDIAN));
		assertEquals(ItemType.FOOD, ItemType.getItemType(Material.SPIDER_EYE));
	}

	@Test
	void getItemDurability_bow_returnsBowDurability() {
		assertInstanceOf(BowDurability.class, ItemType.BOW.getItemDurability());
	}

	@Test
	void getItemDurability_elytra_returnsElytraDurability() {
		assertInstanceOf(ElytraDurability.class, ItemType.ELYTRA.getItemDurability());
	}

	@Test
	void getItemDurability_fishingRod_returnsFishingRodDurability() {
		assertInstanceOf(FishingRodDurability.class, ItemType.FISHING_ROD.getItemDurability());
	}

	@Test
	void getItemDurability_flintAndSteel_returnsFlintAndSteelDurability() {
		assertInstanceOf(FlintAndSteelDurability.class, ItemType.FLINT_AND_STEEL.getItemDurability());
	}

	@Test
	void getItemDurability_shears_returnsShearDurability() {
		assertInstanceOf(ShearDurability.class, ItemType.SHEARS.getItemDurability());
	}

	@Test
	void getItemDurability_shield_returnsShieldDurability() {
		assertInstanceOf(ShieldDurability.class, ItemType.SHIELD.getItemDurability());
	}

	@Test
	void getItemDurability_armor_returnsArmorDurability() {
		assertInstanceOf(ArmorDurability.class, ItemType.ARMOR.getItemDurability());
	}

	@Test
	void getItemDurability_unmappedType_returnsPlainItemDurability() {
		// TOOL has no case of its own, exercising the (reachable, since
		// external ItemType values other than the 7 mapped ones can be
		// passed as `this`) default branch.
		ItemDurability durability = ItemType.TOOL.getItemDurability();
		assertEquals(ItemDurability.class, durability.getClass());
	}

	@Test
	void getItemPermissions_bow_returnsBowPermissions() {
		assertInstanceOf(BowPermissions.class, ItemType.BOW.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_elytra_returnsElytraPermissions() {
		assertInstanceOf(ElytraPermissions.class, ItemType.ELYTRA.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_fishingRod_returnsFishingRodPermissions() {
		assertInstanceOf(FishingRodPermissions.class,
				ItemType.FISHING_ROD.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_flintAndSteel_returnsFlintAndSteelPermissions() {
		assertInstanceOf(FlintAndSteelPermissions.class,
				ItemType.FLINT_AND_STEEL.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_shears_returnsShearPermissions() {
		assertInstanceOf(ShearPermissions.class, ItemType.SHEARS.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_shield_returnsShieldPermissions() {
		assertInstanceOf(ShieldPermissions.class, ItemType.SHIELD.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_armor_returnsArmorPermissions() {
		assertInstanceOf(ArmorPermissions.class, ItemType.ARMOR.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_unmappedType_returnsPlainItemPermissions() {
		ItemPermissions permissions = ItemType.TOOL.getItemPermissions("test", PermissionType.ALLOW);
		assertEquals(ItemPermissions.class, permissions.getClass());
	}
}
