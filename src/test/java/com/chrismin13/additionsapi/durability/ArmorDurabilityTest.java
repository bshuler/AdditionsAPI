package com.chrismin13.additionsapi.durability;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The constructor's two try/catch(NoSuchFieldError) blocks guard against
 * {@code DamageCause.HOT_FLOOR}/{@code ENTITY_SWEEP_ATTACK} not existing on
 * very old server versions; both constants exist on every Paper API version
 * this project actually targets, so those catch bodies are unreachable here
 * - this is a documented, not silently ignored, coverage gap (see PLAN.md).
 */
class ArmorDurabilityTest {

	@Test
	void defaultsMatchVanilla() {
		ArmorDurability durability = new ArmorDurability();
		assertEquals(1, durability.getThornsExtraDamage());
		assertEquals(2, durability.getThornsExtraDamageOnHit());
	}

	@Test
	void constructor_populatesDefaultDamageCauses() {
		ArmorDurability durability = new ArmorDurability();
		List<DamageCause> causes = durability.getDamageCuasesWithDurability();
		assertTrue(causes.contains(DamageCause.BLOCK_EXPLOSION));
		assertTrue(causes.contains(DamageCause.CONTACT));
		assertTrue(causes.contains(DamageCause.ENTITY_ATTACK));
		assertTrue(causes.contains(DamageCause.ENTITY_EXPLOSION));
		assertTrue(causes.contains(DamageCause.FIRE));
		assertTrue(causes.contains(DamageCause.LAVA));
		assertTrue(causes.contains(DamageCause.LIGHTNING));
		assertTrue(causes.contains(DamageCause.PROJECTILE));
		assertTrue(causes.contains(DamageCause.THORNS));
		assertTrue(causes.contains(DamageCause.WITHER));
		assertTrue(causes.contains(DamageCause.HOT_FLOOR));
		assertTrue(causes.contains(DamageCause.ENTITY_SWEEP_ATTACK));
	}

	@Test
	void setThornsExtraDamage_isFluentAndRoundTrips() {
		ArmorDurability durability = new ArmorDurability();
		ArmorDurability same = durability.setThornsExtraDamage(9);
		assertSame(durability, same);
		assertEquals(9, durability.getThornsExtraDamage());
	}

	@Test
	void setThornsExtraDamageOnHit_isFluentAndRoundTrips() {
		ArmorDurability durability = new ArmorDurability();
		ArmorDurability same = durability.setThornsExtraDamageOnHit(9);
		assertSame(durability, same);
		assertEquals(9, durability.getThornsExtraDamageOnHit());
	}

	@Test
	void setDamageCuasesWithDurability_replacesWholeList() {
		ArmorDurability durability = new ArmorDurability();
		ArrayList<DamageCause> onlyFire = new ArrayList<>();
		onlyFire.add(DamageCause.FIRE);
		ArmorDurability same = durability.setDamageCuasesWithDurability(onlyFire);
		assertSame(durability, same);
		assertSame(onlyFire, durability.getDamageCuasesWithDurability());
	}

	@Test
	void getArmorDamage_forDisallowedCause_returnsZero() {
		ArmorDurability durability = new ArmorDurability();
		ArrayList<DamageCause> onlyFire = new ArrayList<>();
		onlyFire.add(DamageCause.FIRE);
		durability.setDamageCuasesWithDurability(onlyFire);
		assertEquals(0, durability.getArmorDamage(DamageCause.DROWNING, 20F, false));
	}

	@Test
	void getArmorDamage_dividesByFourAndFloors() {
		ArmorDurability durability = new ArmorDurability();
		// 8 / 4 = 2.0 -> 2
		assertEquals(2, durability.getArmorDamage(DamageCause.FIRE, 8F, false));
	}

	@Test
	void getArmorDamage_clampsToMinimumOfOneWhenBelowOne() {
		ArmorDurability durability = new ArmorDurability();
		// 1 / 4 = 0.25 -> clamped to 1.0 -> 1
		assertEquals(1, durability.getArmorDamage(DamageCause.FIRE, 1F, false));
	}

	@Test
	void getArmorDamage_addsThornsExtraDamageWhenHadThorns() {
		ArmorDurability durability = new ArmorDurability();
		// 8 / 4 = 2.0, + thornsExtraDamage (1) = 3
		assertEquals(3, durability.getArmorDamage(DamageCause.FIRE, 8F, true));
		assertFalse(durability.getArmorDamage(DamageCause.FIRE, 8F, true) == durability.getArmorDamage(DamageCause.FIRE, 8F, false));
	}
}
