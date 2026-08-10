package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpadeDurabilityTest {

	@Test
	void constructor_setsVanillaDefaults() {
		SpadeDurability durability = new SpadeDurability();
		assertEquals(1, durability.getBlockBreak());
		assertEquals(2, durability.getEntityHit());
		assertEquals(1, durability.getPathTile());
	}

	@Test
	void setPathTile_isFluentAndRoundTrips() {
		SpadeDurability durability = new SpadeDurability();
		SpadeDurability same = durability.setPathTile(3);
		assertSame(durability, same);
		assertEquals(3, durability.getPathTile());
	}
}
