package com.chrismin13.additionsapi.durability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FishingRodDurabilityTest {

	@Test
	void defaultsMatchVanilla() {
		FishingRodDurability durability = new FishingRodDurability();
		assertEquals(1, durability.getFishCatch());
		assertEquals(2, durability.getBlockReel());
		assertEquals(3, durability.getItemReel());
		assertEquals(5, durability.getEntityReel());
	}

	@Test
	void setFishCatch_isFluentAndRoundTrips() {
		FishingRodDurability durability = new FishingRodDurability();
		FishingRodDurability same = durability.setFishCatch(9);
		assertSame(durability, same);
		assertEquals(9, durability.getFishCatch());
	}

	@Test
	void setBlockReel_isFluentAndRoundTrips() {
		FishingRodDurability durability = new FishingRodDurability();
		FishingRodDurability same = durability.setBlockReel(9);
		assertSame(durability, same);
		assertEquals(9, durability.getBlockReel());
	}

	@Test
	void setItemReel_isFluentAndRoundTrips() {
		FishingRodDurability durability = new FishingRodDurability();
		FishingRodDurability same = durability.setItemReel(9);
		assertSame(durability, same);
		assertEquals(9, durability.getItemReel());
	}

	@Test
	void setEntityReel_isFluentAndRoundTrips() {
		FishingRodDurability durability = new FishingRodDurability();
		FishingRodDurability same = durability.setEntityReel(9);
		assertSame(durability, same);
		assertEquals(9, durability.getEntityReel());
	}
}
