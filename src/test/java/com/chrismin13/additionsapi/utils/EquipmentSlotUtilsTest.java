package com.chrismin13.additionsapi.utils;

import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code valueFromLangFile} is intentionally not exercised here: it routes
 * through {@code LangFileUtils.get} -> {@code LangFile.getInstance()},
 * whose static initializer eagerly calls {@code AdditionsAPI.getInstance()}
 * - a real plugin instance that only exists after a full Bukkit plugin
 * bootstrap. See build.gradle.kts's jacocoTestCoverageVerification
 * exception for this class and PLAN.md for the documented gap.
 */
class EquipmentSlotUtilsTest {

	@Test
	void toAttributeString_hand_returnsMainhand() {
		assertEquals("mainhand", EquipmentSlotUtils.toAttributeString(EquipmentSlot.HAND));
	}

	@Test
	void toAttributeString_offHand_returnsOffhand() {
		assertEquals("offhand", EquipmentSlotUtils.toAttributeString(EquipmentSlot.OFF_HAND));
	}

	@Test
	void toAttributeString_otherSlots_returnsLowercasedEnumName() {
		assertEquals("head", EquipmentSlotUtils.toAttributeString(EquipmentSlot.HEAD));
		assertEquals("chest", EquipmentSlotUtils.toAttributeString(EquipmentSlot.CHEST));
		assertEquals("legs", EquipmentSlotUtils.toAttributeString(EquipmentSlot.LEGS));
		assertEquals("feet", EquipmentSlotUtils.toAttributeString(EquipmentSlot.FEET));
	}

	@Test
	void valueFromAttribute_mainhand_returnsHand() {
		assertEquals(EquipmentSlot.HAND, EquipmentSlotUtils.valueFromAttribute("mainhand"));
	}

	@Test
	void valueFromAttribute_offhand_returnsOffHand() {
		assertEquals(EquipmentSlot.OFF_HAND, EquipmentSlotUtils.valueFromAttribute("offhand"));
	}

	@Test
	void valueFromAttribute_head_returnsHead() {
		assertEquals(EquipmentSlot.HEAD, EquipmentSlotUtils.valueFromAttribute("head"));
	}

	@Test
	void valueFromAttribute_chest_returnsChest() {
		assertEquals(EquipmentSlot.CHEST, EquipmentSlotUtils.valueFromAttribute("chest"));
	}

	@Test
	void valueFromAttribute_feet_returnsFeet() {
		assertEquals(EquipmentSlot.FEET, EquipmentSlotUtils.valueFromAttribute("feet"));
	}

	@Test
	void valueFromAttribute_legs_returnsLegs() {
		assertEquals(EquipmentSlot.LEGS, EquipmentSlotUtils.valueFromAttribute("legs"));
	}

	@Test
	void valueFromAttribute_unknown_defaultsToHand() {
		assertEquals(EquipmentSlot.HAND, EquipmentSlotUtils.valueFromAttribute("nonsense"));
	}
}
