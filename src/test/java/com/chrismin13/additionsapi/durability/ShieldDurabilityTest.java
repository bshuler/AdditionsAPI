package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ShieldDurabilityTest {

	@Test
	void defaultsMatchVanilla() {
		ShieldDurability durability = new ShieldDurability();
		assertEquals(1, durability.getExtraDurabilityTaken());
		assertEquals(1F, durability.getDamageMultiplier());
	}

	@Test
	void setExtraDurabilityTaken_isFluentAndRoundTrips() {
		ShieldDurability durability = new ShieldDurability();
		ShieldDurability same = durability.setExtraDurabilityTaken(4);
		assertSame(durability, same);
		assertEquals(4, durability.getExtraDurabilityTaken());
	}

	@Test
	void setDamageMultiplier_isFluentAndRoundTrips() {
		ShieldDurability durability = new ShieldDurability();
		ShieldDurability same = durability.setDamageMultiplier(2.5F);
		assertSame(durability, same);
		assertEquals(2.5F, durability.getDamageMultiplier());
	}
}
