package us.fihgu.toolbox.item;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import us.fihgu.toolbox.resourcepack.model.ItemModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code Material.getMaxDurability()} (used by {@code getMaxDurability()})
 * internally resolves the modern {@code ItemType}/registry machinery, which
 * needs a live or mocked {@code Bukkit.getServer()} - without it, it throws
 * (empirically confirmed: {@code NullPointerException} inside
 * {@code Tag.<clinit>} because {@code Bukkit.server} is null). MockBukkit is
 * mocked/unmocked around this whole class so that one test can exercise it.
 */
class DamageableItemTest {

	@BeforeAll
	static void setUp() {
		MockBukkit.mock();
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void getMaterial_resolvesRealBukkitMaterialByEnumName() {
		assertEquals(Material.DIAMOND_SWORD, DamageableItem.DIAMOND_SWORD.getMaterial());
		assertEquals(Material.BOW, DamageableItem.BOW.getMaterial());
		assertEquals(Material.SHEARS, DamageableItem.SHEARS.getMaterial());
	}

	@Test
	void getModelName_isLowercasedEnumName() {
		assertEquals("wooden_sword", DamageableItem.WOODEN_SWORD.getModelName());
		assertEquals("bow", DamageableItem.BOW.getModelName());
	}

	@Test
	void getTextureName_isLowercasedEnumName() {
		assertEquals("diamond_pickaxe", DamageableItem.DIAMOND_PICKAXE.getTextureName());
	}

	@Test
	void getMaxDurability_delegatesToMaterial() {
		assertEquals(Material.DIAMOND_SWORD.getMaxDurability(), DamageableItem.DIAMOND_SWORD.getMaxDurability());
		assertTrue(DamageableItem.DIAMOND_SWORD.getMaxDurability() > 0);
	}

	@Test
	void getDamageableItem_knownMaterial_returnsMatchingConstant() {
		assertEquals(DamageableItem.IRON_AXE, DamageableItem.getDamageableItem(Material.IRON_AXE));
		assertEquals(DamageableItem.ELYTRA, DamageableItem.getDamageableItem(Material.ELYTRA));
	}

	@Test
	void getDamageableItem_unmappedMaterial_returnsNull() {
		assertNull(DamageableItem.getDamageableItem(Material.STONE));
	}

	@Test
	void getAttackDamage_andSpeed_delegateToMaterialUtils() {
		assertEquals(6F, DamageableItem.IRON_SWORD.getAttackDamage());
		assertEquals(1.6F, DamageableItem.IRON_SWORD.getAttackSpeed());
	}

	@Test
	void getAttackDamage_andSpeed_defaultBranchForNonWeapon() {
		assertEquals(1F, DamageableItem.SHEARS.getAttackDamage());
	}

	@Test
	void getDefaultItemModel_bow_hasGeneratedParentAndThreePullingOverrides() {
		ItemModel model = DamageableItem.BOW.getDefaultItemModel();
		assertEquals("item/generated", model.getParent());
		assertEquals("item/bow", model.getTextures().get("layer0"));
		assertEquals(3, model.getOverrides().size());
	}

	@Test
	void getDefaultItemModel_fishingRod_hasHandheldRodParentAndCastOverride() {
		ItemModel model = DamageableItem.FISHING_ROD.getDefaultItemModel();
		assertEquals("item/handheld_rod", model.getParent());
		assertEquals("item/fishing_rod_uncast", model.getTextures().get("layer0"));
		assertEquals(1, model.getOverrides().size());
	}

	@Test
	void getDefaultItemModel_carrotOnAStick_isSimpleHandheldRodModel() {
		ItemModel model = DamageableItem.CARROT_ON_A_STICK.getDefaultItemModel();
		assertEquals("item/handheld_rod", model.getParent());
		assertEquals("item/carrot_on_a_stick", model.getTextures().get("layer0"));
		assertNull(model.getOverrides());
	}

	@Test
	void getDefaultItemModel_elytra_hasGeneratedParentAndBrokenOverride() {
		ItemModel model = DamageableItem.ELYTRA.getDefaultItemModel();
		assertEquals("item/generated", model.getParent());
		assertEquals("item/elytra", model.getTextures().get("layer0"));
		assertEquals(1, model.getOverrides().size());
	}

	@Test
	void getDefaultItemModel_handHeldTool_usesHandheldParent() {
		ItemModel model = DamageableItem.WOODEN_SWORD.getDefaultItemModel();
		assertEquals("item/handheld", model.getParent());
		assertEquals("item/wooden_sword", model.getTextures().get("layer0"));
	}

	@Test
	void getDefaultItemModel_nonHandHeldItem_usesGeneratedParent() {
		ItemModel model = DamageableItem.LEATHER_HELMET.getDefaultItemModel();
		assertEquals("item/generated", model.getParent());
		assertEquals("item/leather_helmet", model.getTextures().get("layer0"));
	}

	@Test
	void handHeldItems_containsToolsButNotArmorOrSpecialCases() {
		assertTrue(DamageableItem.handHeldItems.contains(DamageableItem.DIAMOND_AXE));
		assertTrue(DamageableItem.handHeldItems.contains(DamageableItem.WOODEN_HOE));
		assertTrue(!DamageableItem.handHeldItems.contains(DamageableItem.LEATHER_HELMET));
		assertTrue(!DamageableItem.handHeldItems.contains(DamageableItem.BOW));
	}
}
