package com.chrismin13.additionsapi.utils;

import com.google.gson.JsonObject;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * KNOWN BUG (documented in PLAN.md, not fixed here since fixing it would
 * change public method behavior for dependent plugins without a spec for
 * what the correct output should be): {@code getRecipeJson} builds a local
 * {@code object} JsonObject with hardcoded placeholder properties
 * ("number"/"latitude"/"longitude"/"text") but never attaches it to, or
 * returns, anything but a permanently empty {@code json} object - the
 * {@code recipe} parameter is entirely unused. This test locks in the
 * actual (buggy) current behavior rather than the presumably-intended one.
 */
class RecipeUtilsTest {

	@Test
	void constructor_isCallable() {
		// RecipeUtils is a static-only helper with no explicit constructor;
		// this just exercises the implicit default constructor for coverage.
		new RecipeUtils();
	}

	@Test
	void getRecipeJson_alwaysReturnsEmptyObject_evenForARealRecipe() {
		Recipe recipe = Mockito.mock(Recipe.class);
		JsonObject result = RecipeUtils.getRecipeJson(recipe);
		assertTrue(result.entrySet().isEmpty());
	}

	@Test
	void getRecipeJson_nullRecipe_stillReturnsEmptyObject() {
		JsonObject result = RecipeUtils.getRecipeJson(null);
		assertEquals(new JsonObject(), result);
	}
}
