package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class HoeDurabilityTest {

	@Test
	void constructor_setsVanillaDefaults() {
		HoeDurability durability = new HoeDurability();
		assertEquals(1, durability.getEntityHit());
		assertEquals(1, durability.getHoe());
	}

	@Test
	void setHoe_isFluentAndRoundTrips() {
		HoeDurability durability = new HoeDurability();
		HoeDurability same = durability.setHoe(5);
		assertSame(durability, same);
		assertEquals(5, durability.getHoe());
	}
}
