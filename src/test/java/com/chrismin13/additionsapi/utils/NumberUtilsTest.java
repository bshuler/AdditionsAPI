package com.chrismin13.additionsapi.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberUtilsTest {

	@Test
	void constructor_isCallable() {
		// NumberUtils is a static-only helper with no explicit constructor;
		// this just exercises the implicit default constructor for coverage.
		new NumberUtils();
	}

	@Test
	void calculateChance_zeroPercent_alwaysFalse() {
		for (int i = 0; i < 50; i++) {
			assertFalse(NumberUtils.calculateChance(0.0));
		}
	}

	@Test
	void calculateChance_hundredPercent_alwaysTrue() {
		for (int i = 0; i < 50; i++) {
			assertTrue(NumberUtils.calculateChance(1.0));
		}
	}

	@Test
	void safeLongToInt_withinRange_castsSuccessfully() {
		assertEquals(42, NumberUtils.safeLongToInt(42L));
		assertEquals(Integer.MAX_VALUE, NumberUtils.safeLongToInt((long) Integer.MAX_VALUE));
		assertEquals(Integer.MIN_VALUE, NumberUtils.safeLongToInt((long) Integer.MIN_VALUE));
	}

	@Test
	void safeLongToInt_aboveMax_throws() {
		assertThrows(IllegalArgumentException.class,
				() -> NumberUtils.safeLongToInt((long) Integer.MAX_VALUE + 1));
	}

	@Test
	void safeLongToInt_belowMin_throws() {
		assertThrows(IllegalArgumentException.class,
				() -> NumberUtils.safeLongToInt((long) Integer.MIN_VALUE - 1));
	}

	@Test
	void isValidShort_withinExclusiveBounds_isTrue() {
		assertTrue(NumberUtils.isValidShort(0));
		assertTrue(NumberUtils.isValidShort(Short.MAX_VALUE - 1));
		assertTrue(NumberUtils.isValidShort(Short.MIN_VALUE + 1));
	}

	@Test
	void isValidShort_atExclusiveBoundaries_isFalse() {
		// Strict, non-inclusive check confirmed by reading the source:
		// integer < MAX && integer > MIN, so the boundary values themselves
		// are rejected, not accepted.
		assertFalse(NumberUtils.isValidShort(Short.MAX_VALUE));
		assertFalse(NumberUtils.isValidShort(Short.MIN_VALUE));
	}

	@Test
	void isValidShort_beyondBounds_isFalse() {
		assertFalse(NumberUtils.isValidShort(Short.MAX_VALUE + 1));
		assertFalse(NumberUtils.isValidShort(Short.MIN_VALUE - 1));
	}

	@Test
	void round_negativePlaces_throws() {
		assertThrows(IllegalArgumentException.class, () -> NumberUtils.round(1.2345, -1));
	}

	@Test
	void round_zeroPlaces_roundsToWholeNumber() {
		assertEquals(2.0, NumberUtils.round(1.5, 0));
	}

	@Test
	void round_halfUp_roundsAwayFromZeroAtHalfway() {
		assertEquals(1.24, NumberUtils.round(1.235, 2));
	}

	@Test
	void round_exactValueUnaffectedByExtraPlaces() {
		assertEquals(1.5, NumberUtils.round(1.5, 4));
	}

	@Test
	void randomInt_isInclusiveOfBothBounds() {
		for (int i = 0; i < 200; i++) {
			int value = NumberUtils.randomInt(1, 3);
			assertTrue(value >= 1 && value <= 3, "value " + value + " out of [1,3]");
		}
	}

	@Test
	void randomInt_equalMinAndMax_alwaysReturnsThatValue() {
		for (int i = 0; i < 20; i++) {
			assertEquals(5, NumberUtils.randomInt(5, 5));
		}
	}

	@Test
	void isInteger_validIntegerString_isTrue() {
		assertTrue(NumberUtils.isInteger("42"));
		assertTrue(NumberUtils.isInteger("-42"));
	}

	@Test
	void isInteger_nonNumericString_isFalse() {
		assertFalse(NumberUtils.isInteger("not a number"));
	}

	@Test
	void isInteger_decimalString_isFalse() {
		assertFalse(NumberUtils.isInteger("4.2"));
	}
}
