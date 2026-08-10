package com.chrismin13.additionsapi.durability;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ShearDurabilityTest {

	@Test
	void constructor_setsInstantBlockBreakAndSpecialBlocks() {
		ShearDurability durability = new ShearDurability();
		assertEquals(1, durability.getInstantBlockBreak());
		assertEquals(1, durability.getSpecialBlockDurability(Material.COBWEB));
		assertEquals(1, durability.getSpecialBlockDurability(Material.OAK_LEAVES));
		assertEquals(1, durability.getSpecialBlockDurability(Material.WHITE_WOOL));
		assertEquals(1, durability.getSpecialBlockDurability(Material.VINE));
		// 23 total special blocks registered by the constructor: cobweb (1)
		// + 5 leaves types + 17 wool colors.
		assertEquals(23, durability.getAllSpecialBlocks().size());
	}

	@Test
	void defaultShearSheepIsOne() {
		assertEquals(1, new ShearDurability().getShearSheep());
	}

	@Test
	void setShearSheep_isFluentAndRoundTrips() {
		ShearDurability durability = new ShearDurability();
		ShearDurability same = durability.setShearSheep(3);
		assertSame(durability, same);
		assertEquals(3, durability.getShearSheep());
	}
}
