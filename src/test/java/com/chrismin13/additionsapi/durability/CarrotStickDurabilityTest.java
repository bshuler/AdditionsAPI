package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CarrotStickDurabilityTest {

	@Test
	void defaultBoostIsSeven() {
		// Field default is 7, despite the javadoc claiming 1 - locking in
		// actual behavior, not the (stale) javadoc.
		assertEquals(7, new CarrotStickDurability().getBoost());
	}

	@Test
	void setBoost_isFluentAndRoundTrips() {
		CarrotStickDurability durability = new CarrotStickDurability();
		CarrotStickDurability same = durability.setBoost(2);
		assertSame(durability, same);
		assertEquals(2, durability.getBoost());
	}
}
