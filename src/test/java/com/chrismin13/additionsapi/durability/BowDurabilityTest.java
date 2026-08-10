package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BowDurabilityTest {

	@Test
	void defaultFireArrowIsOne() {
		assertEquals(1, new BowDurability().getFireArrow());
	}

	@Test
	void setFireArrow_isFluentAndRoundTrips() {
		BowDurability durability = new BowDurability();
		BowDurability same = durability.setFireArrow(3);
		assertSame(durability, same);
		assertEquals(3, durability.getFireArrow());
	}
}
