package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwordDurabilityTest {

	@Test
	void constructor_setsVanillaDefaults() {
		SwordDurability durability = new SwordDurability();
		assertEquals(2, durability.getBlockBreak());
		assertEquals(1, durability.getEntityHit());
	}
}
