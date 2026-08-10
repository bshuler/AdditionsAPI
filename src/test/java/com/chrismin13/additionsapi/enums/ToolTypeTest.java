package com.chrismin13.additionsapi.enums;

import com.chrismin13.additionsapi.durability.AxeDurability;
import com.chrismin13.additionsapi.durability.HoeDurability;
import com.chrismin13.additionsapi.durability.ItemDurability;
import com.chrismin13.additionsapi.durability.PickaxeDurability;
import com.chrismin13.additionsapi.durability.SpadeDurability;
import com.chrismin13.additionsapi.durability.SwordDurability;
import com.chrismin13.additionsapi.permissions.HoePermissions;
import com.chrismin13.additionsapi.permissions.ItemPermissions;
import com.chrismin13.additionsapi.permissions.PermissionType;
import com.chrismin13.additionsapi.permissions.SpadePermissions;
import com.chrismin13.additionsapi.recipes.CustomShapedRecipe;
import com.chrismin13.additionsapi.recipes.RecipeIngredient;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@code toIngameString} is intentionally not exercised here: it routes
 * through {@code LangFileUtils.get} -> {@code LangFile.getInstance()}, whose
 * static initializer eagerly calls {@code AdditionsAPI.getInstance()} - a
 * real plugin instance that only exists after a full Bukkit plugin
 * bootstrap. Same landmine, same exclusion pattern as {@code
 * EquipmentSlotUtilsTest}/{@code ArmorTypeTest}. See the
 * jacocoTestCoverageVerification exception for this class in
 * build.gradle.kts and PLAN.md for the documented gap.
 *
 * <p>{@code getDurabilityMechanics()}'s {@code default: return new
 * ItemDurability();} is provably dead code: every one of the 5 enum
 * constants has its own explicit case, so {@code switch(this)} can never
 * reach the default. Same for {@code getCustomShapedRecipe(RecipeIngredient,
 * RecipeIngredient)}'s {@code default: Debug.sayError(...)}. Both are
 * covered by an exact-count MISSEDCOUNT exception, not unreachable test
 * code. By contrast, {@code getItemPermissions}'s default IS reachable:
 * AXE/PICKAXE/SWORD explicitly fall through to it, and {@code
 * getToolType(Material)}'s default is reachable via any non-tool Material.
 */
class ToolTypeTest {

	@BeforeAll
	static void setUp() {
		MockBukkit.mock();
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void getDurabilityMechanics_axe_returnsAxeDurability() {
		assertInstanceOf(AxeDurability.class, ToolType.AXE.getDurabilityMechanics());
	}

	@Test
	void getDurabilityMechanics_hoe_returnsHoeDurability() {
		assertInstanceOf(HoeDurability.class, ToolType.HOE.getDurabilityMechanics());
	}

	@Test
	void getDurabilityMechanics_pickaxe_returnsPickaxeDurability() {
		assertInstanceOf(PickaxeDurability.class, ToolType.PICKAXE.getDurabilityMechanics());
	}

	@Test
	void getDurabilityMechanics_spade_returnsSpadeDurability() {
		assertInstanceOf(SpadeDurability.class, ToolType.SPADE.getDurabilityMechanics());
	}

	@Test
	void getDurabilityMechanics_sword_returnsSwordDurability() {
		assertInstanceOf(SwordDurability.class, ToolType.SWORD.getDurabilityMechanics());
	}

	@Test
	void getItemPermissions_hoe_returnsHoePermissions() {
		assertInstanceOf(HoePermissions.class, ToolType.HOE.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_spade_returnsSpadePermissions() {
		assertInstanceOf(SpadePermissions.class, ToolType.SPADE.getItemPermissions("test", PermissionType.ALLOW));
	}

	@Test
	void getItemPermissions_axePickaxeSword_fallThroughToPlainItemPermissions() {
		// AXE/PICKAXE/SWORD have no case body of their own and explicitly
		// fall through to the shared default - a genuinely reachable
		// branch, unlike the dead defaults elsewhere in this class.
		ItemPermissions axe = ToolType.AXE.getItemPermissions("test", PermissionType.ALLOW);
		ItemPermissions pickaxe = ToolType.PICKAXE.getItemPermissions("test", PermissionType.ALLOW);
		ItemPermissions sword = ToolType.SWORD.getItemPermissions("test", PermissionType.ALLOW);

		assertEquals(ItemPermissions.class, axe.getClass());
		assertEquals(ItemPermissions.class, pickaxe.getClass());
		assertEquals(ItemPermissions.class, sword.getClass());
	}

	@Test
	void getCustomShapedRecipe_materialOverload_delegatesToIngredientOverload() {
		List<CustomShapedRecipe> recipes = ToolType.SWORD.getCustomShapedRecipe(Material.IRON_INGOT, Material.STICK);

		assertEquals(1, recipes.size());
		assertEquals(Material.IRON_INGOT, recipes.get(0).getIngredients().get('1').getMaterial());
		assertEquals(Material.STICK, recipes.get(0).getIngredients().get('2').getMaterial());
	}

	@Test
	void getCustomShapedRecipe_sword_buildsSingleRecipe() {
		List<CustomShapedRecipe> recipes = ToolType.SWORD.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT),
				new RecipeIngredient(Material.STICK));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "1", "1", "2" }, recipes.get(0).getShape());
	}

	@Test
	void getCustomShapedRecipe_axe_buildsTwoRecipes() {
		List<CustomShapedRecipe> recipes = ToolType.AXE.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT),
				new RecipeIngredient(Material.STICK));

		assertEquals(2, recipes.size());
		assertArrayEquals(new String[] { "11", "21", "20" }, recipes.get(0).getShape());
		assertArrayEquals(new String[] { "11", "12", "02" }, recipes.get(1).getShape());
	}

	@Test
	void getCustomShapedRecipe_pickaxe_buildsSingleRecipe() {
		List<CustomShapedRecipe> recipes = ToolType.PICKAXE
				.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT), new RecipeIngredient(Material.STICK));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "111", "020", "020" }, recipes.get(0).getShape());
	}

	@Test
	void getCustomShapedRecipe_spade_buildsSingleRecipe() {
		List<CustomShapedRecipe> recipes = ToolType.SPADE.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT),
				new RecipeIngredient(Material.STICK));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "1", "2", "2" }, recipes.get(0).getShape());
	}

	@Test
	void getCustomShapedRecipe_hoe_buildsTwoRecipes() {
		List<CustomShapedRecipe> recipes = ToolType.HOE.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT),
				new RecipeIngredient(Material.STICK));

		assertEquals(2, recipes.size());
		assertArrayEquals(new String[] { "11", "20", "20" }, recipes.get(0).getShape());
		assertArrayEquals(new String[] { "11", "02", "02" }, recipes.get(1).getShape());
	}

	@ParameterizedTest(name = "getToolType({0}) == {1}")
	@MethodSource("toolMaterials")
	void getToolType_coversEveryMappedMaterial(Material material, ToolType expected) {
		assertEquals(expected, ToolType.getToolType(material));
	}

	static Stream<Arguments> toolMaterials() {
		return Stream.of(
				Arguments.of(Material.WOODEN_SWORD, ToolType.SWORD),
				Arguments.of(Material.GOLDEN_SWORD, ToolType.SWORD),
				Arguments.of(Material.STONE_SWORD, ToolType.SWORD),
				Arguments.of(Material.IRON_SWORD, ToolType.SWORD),
				Arguments.of(Material.DIAMOND_SWORD, ToolType.SWORD),
				Arguments.of(Material.WOODEN_AXE, ToolType.AXE),
				Arguments.of(Material.GOLDEN_AXE, ToolType.AXE),
				Arguments.of(Material.STONE_AXE, ToolType.AXE),
				Arguments.of(Material.IRON_AXE, ToolType.AXE),
				Arguments.of(Material.DIAMOND_AXE, ToolType.AXE),
				Arguments.of(Material.WOODEN_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.STONE_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.IRON_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.GOLDEN_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.DIAMOND_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.WOODEN_SHOVEL, ToolType.SPADE),
				Arguments.of(Material.STONE_SHOVEL, ToolType.SPADE),
				Arguments.of(Material.IRON_SHOVEL, ToolType.SPADE),
				Arguments.of(Material.GOLDEN_SHOVEL, ToolType.SPADE),
				Arguments.of(Material.DIAMOND_SHOVEL, ToolType.SPADE),
				Arguments.of(Material.WOODEN_HOE, ToolType.HOE),
				Arguments.of(Material.STONE_HOE, ToolType.HOE),
				Arguments.of(Material.IRON_HOE, ToolType.HOE),
				Arguments.of(Material.GOLDEN_HOE, ToolType.HOE),
				Arguments.of(Material.DIAMOND_HOE, ToolType.HOE),
				Arguments.of(Material.LEGACY_WOOD_SWORD, ToolType.SWORD),
				Arguments.of(Material.LEGACY_GOLD_SWORD, ToolType.SWORD),
				Arguments.of(Material.LEGACY_STONE_SWORD, ToolType.SWORD),
				Arguments.of(Material.LEGACY_IRON_SWORD, ToolType.SWORD),
				Arguments.of(Material.LEGACY_DIAMOND_SWORD, ToolType.SWORD),
				Arguments.of(Material.LEGACY_WOOD_AXE, ToolType.AXE),
				Arguments.of(Material.LEGACY_GOLD_AXE, ToolType.AXE),
				Arguments.of(Material.LEGACY_STONE_AXE, ToolType.AXE),
				Arguments.of(Material.LEGACY_IRON_AXE, ToolType.AXE),
				Arguments.of(Material.LEGACY_DIAMOND_AXE, ToolType.AXE),
				Arguments.of(Material.LEGACY_WOOD_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.LEGACY_STONE_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.LEGACY_IRON_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.LEGACY_GOLD_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.LEGACY_DIAMOND_PICKAXE, ToolType.PICKAXE),
				Arguments.of(Material.LEGACY_WOOD_SPADE, ToolType.SPADE),
				Arguments.of(Material.LEGACY_STONE_SPADE, ToolType.SPADE),
				Arguments.of(Material.LEGACY_IRON_SPADE, ToolType.SPADE),
				Arguments.of(Material.LEGACY_GOLD_SPADE, ToolType.SPADE),
				Arguments.of(Material.LEGACY_DIAMOND_SPADE, ToolType.SPADE),
				Arguments.of(Material.LEGACY_WOOD_HOE, ToolType.HOE),
				Arguments.of(Material.LEGACY_STONE_HOE, ToolType.HOE),
				Arguments.of(Material.LEGACY_IRON_HOE, ToolType.HOE),
				Arguments.of(Material.LEGACY_GOLD_HOE, ToolType.HOE),
				Arguments.of(Material.LEGACY_DIAMOND_HOE, ToolType.HOE)
		);
	}

	@Test
	void getToolType_nonToolMaterial_returnsNullViaReachableDefault() {
		assertNull(ToolType.getToolType(Material.DIRT));
	}
}
