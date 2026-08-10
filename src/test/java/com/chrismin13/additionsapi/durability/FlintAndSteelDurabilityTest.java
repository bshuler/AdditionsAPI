package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FlintAndSteelDurabilityTest {

	@Test
	void defaultFireIsOne() {
		assertEquals(1, new FlintAndSteelDurability().getFire());
	}

	@Test
	void setFire_isFluentAndRoundTrips() {
		FlintAndSteelDurability durability = new FlintAndSteelDurability();
		FlintAndSteelDurability same = durability.setFire(6);
		assertSame(durability, same);
		assertEquals(6, durability.getFire());
	}
}
