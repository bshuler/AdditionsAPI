package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AxeDurabilityTest {

	@Test
	void constructor_setsVanillaDefaults() {
		AxeDurability durability = new AxeDurability();
		assertEquals(1, durability.getBlockBreak());
		assertEquals(2, durability.getEntityHit());
	}
}
