package com.chrismin13.additionsapi.enums;

import com.chrismin13.additionsapi.recipes.CustomShapedRecipe;
import com.chrismin13.additionsapi.recipes.RecipeIngredient;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@code toIngameString}/{@code toIngameLeatherString} are intentionally not
 * exercised here: both route through {@code LangFileUtils.get} ->
 * {@code LangFile.getInstance()}, whose static initializer eagerly calls
 * {@code AdditionsAPI.getInstance()} - a real plugin instance that only
 * exists after a full Bukkit plugin bootstrap. Same landmine, same
 * exclusion pattern as {@code EquipmentSlotUtilsTest}. See the
 * jacocoTestCoverageVerification exception for this class in
 * build.gradle.kts and PLAN.md for the documented gap.
 *
 * <p>Every {@code get*Material()}/{@code getEquipmentSlot()} method's
 * {@code default: return null;} branch is provably dead code: {@code
 * switch(this)} over an enum with an explicit case for every constant can
 * never reach its default. Same for {@code getCustomShapedRecipe
 * (RecipeIngredient)}'s {@code default: Debug.sayError(...)}. These are
 * covered by an exact-count MISSEDCOUNT exception, not by unreachable test
 * code. {@code getArmorType(Material)}'s default IS reachable (any
 * non-armor Material argument hits it) and is tested directly below.
 */
class ArmorTypeTest {

	@BeforeAll
	static void setUp() {
		org.mockbukkit.mockbukkit.MockBukkit.mock();
	}

	@AfterAll
	static void tearDown() {
		org.mockbukkit.mockbukkit.MockBukkit.unmock();
	}

	@Test
	void getLeatherMaterial_returnsCorrectMaterialPerSlot() {
		assertEquals(Material.LEATHER_HELMET, ArmorType.HELMET.getLeatherMaterial());
		assertEquals(Material.LEATHER_CHESTPLATE, ArmorType.CHESTPLATE.getLeatherMaterial());
		assertEquals(Material.LEATHER_LEGGINGS, ArmorType.LEGGINGS.getLeatherMaterial());
		assertEquals(Material.LEATHER_BOOTS, ArmorType.BOOTS.getLeatherMaterial());
	}

	@Test
	void getChainmailMaterial_returnsCorrectMaterialPerSlot() {
		assertEquals(Material.CHAINMAIL_HELMET, ArmorType.HELMET.getChainmailMaterial());
		assertEquals(Material.CHAINMAIL_CHESTPLATE, ArmorType.CHESTPLATE.getChainmailMaterial());
		assertEquals(Material.CHAINMAIL_LEGGINGS, ArmorType.LEGGINGS.getChainmailMaterial());
		assertEquals(Material.CHAINMAIL_BOOTS, ArmorType.BOOTS.getChainmailMaterial());
	}

	@Test
	void getGoldMaterial_returnsCorrectMaterialPerSlot() {
		assertEquals(Material.GOLDEN_HELMET, ArmorType.HELMET.getGoldMaterial());
		assertEquals(Material.GOLDEN_CHESTPLATE, ArmorType.CHESTPLATE.getGoldMaterial());
		assertEquals(Material.GOLDEN_LEGGINGS, ArmorType.LEGGINGS.getGoldMaterial());
		assertEquals(Material.GOLDEN_BOOTS, ArmorType.BOOTS.getGoldMaterial());
	}

	@Test
	void getIronMaterial_returnsCorrectMaterialPerSlot() {
		assertEquals(Material.IRON_HELMET, ArmorType.HELMET.getIronMaterial());
		assertEquals(Material.IRON_CHESTPLATE, ArmorType.CHESTPLATE.getIronMaterial());
		assertEquals(Material.IRON_LEGGINGS, ArmorType.LEGGINGS.getIronMaterial());
		assertEquals(Material.IRON_BOOTS, ArmorType.BOOTS.getIronMaterial());
	}

	@Test
	void getDiamondMaterial_returnsCorrectMaterialPerSlot() {
		assertEquals(Material.DIAMOND_HELMET, ArmorType.HELMET.getDiamondMaterial());
		assertEquals(Material.DIAMOND_CHESTPLATE, ArmorType.CHESTPLATE.getDiamondMaterial());
		assertEquals(Material.DIAMOND_LEGGINGS, ArmorType.LEGGINGS.getDiamondMaterial());
		assertEquals(Material.DIAMOND_BOOTS, ArmorType.BOOTS.getDiamondMaterial());
	}

	@Test
	void getEquipmentSlot_returnsCorrectSlotPerArmorType() {
		assertEquals(EquipmentSlot.HEAD, ArmorType.HELMET.getEquipmentSlot());
		assertEquals(EquipmentSlot.CHEST, ArmorType.CHESTPLATE.getEquipmentSlot());
		assertEquals(EquipmentSlot.LEGS, ArmorType.LEGGINGS.getEquipmentSlot());
		assertEquals(EquipmentSlot.FEET, ArmorType.BOOTS.getEquipmentSlot());
	}

	@Test
	void getSlot_returnsSlotIndexPassedToConstructor() {
		assertEquals(5, ArmorType.HELMET.getSlot());
		assertEquals(6, ArmorType.CHESTPLATE.getSlot());
		assertEquals(7, ArmorType.LEGGINGS.getSlot());
		assertEquals(8, ArmorType.BOOTS.getSlot());
	}

	@Test
	void getCustomShapedRecipe_materialOverload_delegatesToIngredientOverload() {
		List<CustomShapedRecipe> recipes = ArmorType.HELMET.getCustomShapedRecipe(Material.IRON_INGOT);

		assertEquals(1, recipes.size());
		assertEquals(Material.IRON_INGOT, recipes.get(0).getIngredients().get('1').getMaterial());
	}

	@Test
	void getCustomShapedRecipe_helmet_buildsHelmetShape() {
		List<CustomShapedRecipe> recipes = ArmorType.HELMET
				.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "111", "101" }, recipes.get(0).getShape());
	}

	@Test
	void getCustomShapedRecipe_chestplate_buildsChestplateShape() {
		List<CustomShapedRecipe> recipes = ArmorType.CHESTPLATE
				.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "101", "111", "111" }, recipes.get(0).getShape());
	}

	@Test
	void getCustomShapedRecipe_leggings_buildsLeggingsShape() {
		List<CustomShapedRecipe> recipes = ArmorType.LEGGINGS
				.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "111", "101", "101" }, recipes.get(0).getShape());
	}

	@Test
	void getCustomShapedRecipe_boots_buildsBootsShape() {
		List<CustomShapedRecipe> recipes = ArmorType.BOOTS
				.getCustomShapedRecipe(new RecipeIngredient(Material.IRON_INGOT));

		assertEquals(1, recipes.size());
		assertArrayEquals(new String[] { "101", "101" }, recipes.get(0).getShape());
	}

	@Test
	void getArmorType_fromNullItemStack_returnsNull() {
		assertNull(ArmorType.getArmorType((ItemStack) null));
	}

	@Test
	void getArmorType_fromItemStack_delegatesToMaterialOverload() {
		ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
		assertEquals(ArmorType.HELMET, ArmorType.getArmorType(item));
	}

	@Test
	void getArmorType_fromMaterial_recognizesEveryHelmetVariant() {
		assertEquals(ArmorType.HELMET, ArmorType.getArmorType(Material.DIAMOND_HELMET));
		assertEquals(ArmorType.HELMET, ArmorType.getArmorType(Material.GOLDEN_HELMET));
		assertEquals(ArmorType.HELMET, ArmorType.getArmorType(Material.IRON_HELMET));
		assertEquals(ArmorType.HELMET, ArmorType.getArmorType(Material.CHAINMAIL_HELMET));
		assertEquals(ArmorType.HELMET, ArmorType.getArmorType(Material.LEATHER_HELMET));
	}

	@Test
	void getArmorType_fromMaterial_recognizesEveryChestplateVariant() {
		assertEquals(ArmorType.CHESTPLATE, ArmorType.getArmorType(Material.DIAMOND_CHESTPLATE));
		assertEquals(ArmorType.CHESTPLATE, ArmorType.getArmorType(Material.GOLDEN_CHESTPLATE));
		assertEquals(ArmorType.CHESTPLATE, ArmorType.getArmorType(Material.IRON_CHESTPLATE));
		assertEquals(ArmorType.CHESTPLATE, ArmorType.getArmorType(Material.CHAINMAIL_CHESTPLATE));
		assertEquals(ArmorType.CHESTPLATE, ArmorType.getArmorType(Material.LEATHER_CHESTPLATE));
	}

	@Test
	void getArmorType_fromMaterial_recognizesEveryLeggingsVariant() {
		assertEquals(ArmorType.LEGGINGS, ArmorType.getArmorType(Material.DIAMOND_LEGGINGS));
		assertEquals(ArmorType.LEGGINGS, ArmorType.getArmorType(Material.GOLDEN_LEGGINGS));
		assertEquals(ArmorType.LEGGINGS, ArmorType.getArmorType(Material.IRON_LEGGINGS));
		assertEquals(ArmorType.LEGGINGS, ArmorType.getArmorType(Material.CHAINMAIL_LEGGINGS));
		assertEquals(ArmorType.LEGGINGS, ArmorType.getArmorType(Material.LEATHER_LEGGINGS));
	}

	@Test
	void getArmorType_fromMaterial_recognizesEveryBootsVariant() {
		assertEquals(ArmorType.BOOTS, ArmorType.getArmorType(Material.DIAMOND_BOOTS));
		assertEquals(ArmorType.BOOTS, ArmorType.getArmorType(Material.GOLDEN_BOOTS));
		assertEquals(ArmorType.BOOTS, ArmorType.getArmorType(Material.IRON_BOOTS));
		assertEquals(ArmorType.BOOTS, ArmorType.getArmorType(Material.CHAINMAIL_BOOTS));
		assertEquals(ArmorType.BOOTS, ArmorType.getArmorType(Material.LEATHER_BOOTS));
	}

	@Test
	void getArmorType_fromNonArmorMaterial_returnsNullViaReachableDefault() {
		assertNull(ArmorType.getArmorType(Material.DIRT));
	}
}
