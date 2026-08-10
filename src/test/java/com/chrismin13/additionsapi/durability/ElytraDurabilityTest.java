package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ElytraDurabilityTest {

	@Test
	void defaultFlightOneSecondIsOne() {
		assertEquals(1, new ElytraDurability().getFlightOneSecond());
	}

	@Test
	void setFlightOneSecond_isFluentAndRoundTrips() {
		ElytraDurability durability = new ElytraDurability();
		ElytraDurability same = durability.setFlightOneSecond(4);
		assertSame(durability, same);
		assertEquals(4, durability.getFlightOneSecond());
	}
}
