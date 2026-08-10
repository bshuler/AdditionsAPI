package com.chrismin13.additionsapi.durability;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ItemDurabilityTest {

	@Test
	void defaultsAreAllZero() {
		ItemDurability durability = new ItemDurability();
		assertEquals(0, durability.getBlockBreak());
		assertEquals(0, durability.getInstantBlockBreak());
		assertEquals(0, durability.getEntityHit());
		assertNull(durability.getAllSpecialBlocks());
	}

	@Test
	void setBlockBreak_isFluentAndRoundTrips() {
		ItemDurability durability = new ItemDurability();
		ItemDurability same = durability.setBlockBreak(3);
		assertSame(durability, same);
		assertEquals(3, durability.getBlockBreak());
	}

	@Test
	void setInstantBlockBreak_isFluentAndRoundTrips() {
		ItemDurability durability = new ItemDurability();
		ItemDurability same = durability.setInstantBlockBreak(4);
		assertSame(durability, same);
		assertEquals(4, durability.getInstantBlockBreak());
	}

	@Test
	void setEntityHit_isFluentAndRoundTrips() {
		ItemDurability durability = new ItemDurability();
		ItemDurability same = durability.setEntityHit(5);
		assertSame(durability, same);
		assertEquals(5, durability.getEntityHit());
	}

	@Test
	void getBlockBreakForBlock_withNoSpecialBlocks_returnsGenericBlockBreak() {
		ItemDurability durability = new ItemDurability().setBlockBreak(2);
		Block block = Mockito.mock(Block.class);
		Mockito.when(block.getType()).thenReturn(Material.STONE);
		assertEquals(2, durability.getBlockBreak(block));
	}

	@Test
	void addSpecialBlock_createsMapLazilyAndRoundTrips() {
		ItemDurability durability = new ItemDurability();
		ItemDurability same = durability.addSpecialBlock(Material.COBWEB, 1);
		assertSame(durability, same);
		assertEquals(1, durability.getAllSpecialBlocks().size());
		assertEquals(1, durability.getSpecialBlockDurability(Material.COBWEB));
	}

	@Test
	void getBlockBreakForBlock_withMatchingSpecialBlock_overridesGeneric() {
		ItemDurability durability = new ItemDurability().setBlockBreak(2).addSpecialBlock(Material.COBWEB, 7);
		Block block = Mockito.mock(Block.class);
		Mockito.when(block.getType()).thenReturn(Material.COBWEB);
		assertEquals(7, durability.getBlockBreak(block));
	}

	@Test
	void getSpecialBlockDurability_forUnknownMaterial_returnsNull() {
		ItemDurability durability = new ItemDurability().addSpecialBlock(Material.COBWEB, 1);
		assertNull(durability.getSpecialBlockDurability(Material.STONE));
	}

	@Test
	void getSpecialBlockDurability_whenMapNeverCreated_returnsNull() {
		ItemDurability durability = new ItemDurability();
		assertNull(durability.getSpecialBlockDurability(Material.STONE));
	}
}
