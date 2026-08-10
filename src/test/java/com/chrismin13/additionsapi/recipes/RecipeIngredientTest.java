package com.chrismin13.additionsapi.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeIngredientTest {

	@Test
	void materialConstructor_defaultsBlockDataToZero() {
		RecipeIngredient ingredient = new RecipeIngredient(Material.STONE);
		assertEquals(Material.STONE, ingredient.getMaterial());
		assertEquals((byte) 0, ingredient.getBlockData());
	}

	@Test
	void materialAndBlockDataConstructor_setsBoth() {
		RecipeIngredient ingredient = new RecipeIngredient(Material.STONE, (byte) 3);
		assertEquals(Material.STONE, ingredient.getMaterial());
		assertEquals((byte) 3, ingredient.getBlockData());
	}

	/**
	 * KNOWN QUIRK (documented in PLAN.md, not fixed here): the null-ItemStack
	 * constructor sets the boxed {@code blockData} field to {@code null}, but
	 * {@code getBlockData()}'s return type is the primitive {@code byte} - a
	 * public API signature this modernization pass must not change - so
	 * unboxing a null field throws {@link NullPointerException} rather than
	 * returning some sentinel value. Only {@code getMaterial()} is safely
	 * nullable here.
	 */
	@Test
	void itemStackConstructor_withNullItemStack_setsMaterialNullAndBlockDataUnreadable() {
		RecipeIngredient ingredient = new RecipeIngredient((ItemStack) null);
		assertNull(ingredient.getMaterial());
		assertThrows(NullPointerException.class, ingredient::getBlockData);
	}

	@Test
	@SuppressWarnings("deprecation")
	void itemStackConstructor_withRealItemStack_readsTypeAndLegacyBlockData() {
		ItemStack itemStack = Mockito.mock(ItemStack.class);
		org.bukkit.material.MaterialData materialData = Mockito.mock(org.bukkit.material.MaterialData.class);
		Mockito.when(itemStack.getType()).thenReturn(Material.STONE);
		Mockito.when(itemStack.getData()).thenReturn(materialData);
		Mockito.when(materialData.getData()).thenReturn((byte) 4);

		RecipeIngredient ingredient = new RecipeIngredient(itemStack);

		assertEquals(Material.STONE, ingredient.getMaterial());
		assertEquals((byte) 4, ingredient.getBlockData());
	}

	@Test
	void setMaterial_isFluentAndRoundTrips() {
		RecipeIngredient ingredient = new RecipeIngredient(Material.STONE);
		RecipeIngredient same = ingredient.setMaterial(Material.DIRT);
		assertEquals(ingredient, same);
		assertEquals(Material.DIRT, ingredient.getMaterial());
	}

	@Test
	void setBlockData_isFluentAndRoundTrips() {
		RecipeIngredient ingredient = new RecipeIngredient(Material.STONE);
		RecipeIngredient same = ingredient.setBlockData((byte) 9);
		assertEquals(ingredient, same);
		assertEquals((byte) 9, ingredient.getBlockData());
	}

	@Test
	void clone_producesDistinctEqualCopy() {
		RecipeIngredient original = new RecipeIngredient(Material.STONE, (byte) 2);
		RecipeIngredient clone = original.clone();
		assertNotSame(original, clone);
		assertEquals(original, clone);
	}

	@Test
	void equals_sameMaterialAndBlockData_isTrue() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		RecipeIngredient b = new RecipeIngredient(Material.STONE, (byte) 1);
		assertEquals(a, b);
	}

	@Test
	void equals_null_isFalse() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		assertFalse(a.equals(null));
	}

	@Test
	void equals_differentClass_isFalse() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		assertFalse(a.equals("not a RecipeIngredient"));
	}

	@Test
	void equals_differentMaterial_isFalse() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		RecipeIngredient b = new RecipeIngredient(Material.DIRT, (byte) 1);
		assertFalse(a.equals(b));
	}

	@Test
	void equals_differentBlockData_isFalse() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		RecipeIngredient b = new RecipeIngredient(Material.STONE, (byte) 2);
		assertFalse(a.equals(b));
	}

	@Test
	void materialEquals_bothNull_isTrue() {
		RecipeIngredient a = new RecipeIngredient((ItemStack) null);
		RecipeIngredient b = new RecipeIngredient((ItemStack) null);
		assertTrue(a.materialEquals(b));
	}

	@Test
	void materialEquals_oneNull_isFalse() {
		RecipeIngredient a = new RecipeIngredient((ItemStack) null);
		RecipeIngredient b = new RecipeIngredient(Material.STONE);
		assertFalse(a.materialEquals(b));
		assertFalse(b.materialEquals(a));
	}

	@Test
	void blockDataEquals_bothNull_isTrue() {
		RecipeIngredient a = new RecipeIngredient((ItemStack) null);
		RecipeIngredient b = new RecipeIngredient((ItemStack) null);
		assertTrue(a.blockDataEquals(b));
	}

	@Test
	void blockDataEquals_oneNull_isFalse() {
		RecipeIngredient a = new RecipeIngredient((ItemStack) null);
		RecipeIngredient b = new RecipeIngredient(Material.STONE);
		assertFalse(a.blockDataEquals(b));
		assertFalse(b.blockDataEquals(a));
	}

	@Test
	void hashCode_equalInstances_haveEqualHashCodes() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		RecipeIngredient b = new RecipeIngredient(Material.STONE, (byte) 1);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void hashCode_allNullFields_isStableConstant() {
		RecipeIngredient a = new RecipeIngredient((ItemStack) null);
		RecipeIngredient b = new RecipeIngredient((ItemStack) null);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void compareTo_sameReference_isZero() {
		RecipeIngredient ingredient = new RecipeIngredient(Material.STONE);
		assertEquals(0, ingredient.compareTo(ingredient));
	}

	@Test
	void compareTo_sameMaterialDifferentBlockData_ordersByBlockData() {
		RecipeIngredient lower = new RecipeIngredient(Material.STONE, (byte) 1);
		RecipeIngredient higher = new RecipeIngredient(Material.STONE, (byte) 2);
		assertEquals(-1, lower.compareTo(higher));
		assertEquals(1, higher.compareTo(lower));
	}

	@Test
	void compareTo_sameMaterialSameBlockData_isZero() {
		RecipeIngredient a = new RecipeIngredient(Material.STONE, (byte) 1);
		RecipeIngredient b = new RecipeIngredient(Material.STONE, (byte) 1);
		assertEquals(0, a.compareTo(b));
	}

	/**
	 * Cross-material ordering: fixed bug, see RecipeIngredient.compareTo() and
	 * PLAN.md. Was previously untestable because the old getId()-based
	 * comparison unconditionally threw IllegalArgumentException for any two
	 * non-identical RecipeIngredients on modern (post-1.13-flattening)
	 * servers - i.e. compareTo() was completely broken, not just for
	 * cross-material pairs. Now ordered by Material's own natural
	 * (declaration-order) ordering.
	 */
	@Test
	void compareTo_differentMaterial_ordersByMaterialNaturalOrder() {
		RecipeIngredient stone = new RecipeIngredient(Material.STONE);
		RecipeIngredient dirt = new RecipeIngredient(Material.DIRT);

		int expected = Integer.signum(Material.STONE.compareTo(Material.DIRT));
		assertEquals(expected, Integer.signum(stone.compareTo(dirt)));
		assertEquals(-expected, Integer.signum(dirt.compareTo(stone)));
	}
}
