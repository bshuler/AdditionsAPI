package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PickaxeDurabilityTest {

	@Test
	void constructor_setsVanillaDefaults() {
		PickaxeDurability durability = new PickaxeDurability();
		assertEquals(1, durability.getBlockBreak());
		assertEquals(2, durability.getEntityHit());
	}
}
