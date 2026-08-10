package us.fihgu.toolbox.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every method here either constructs a real {@code ItemStack}/{@code
 * ItemMeta} or calls {@code Bukkit.getItemFactory()}/{@code
 * Bukkit.createInventory(...)} directly, all of which need a live or mocked
 * {@code Bukkit.getServer()} - MockBukkit is mocked/unmocked around this
 * whole class, same pattern as {@code DamageableItemTest}.
 */
class ItemUtilsTest {

	@BeforeAll
	static void setUp() {
		MockBukkit.mock();
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void constructor_isCallable() {
		// ItemUtils is a static-only helper with no explicit constructor;
		// this just exercises the implicit default constructor for coverage.
		new ItemUtils();
	}

	@Test
	void notNullorAir_nullItem_isFalse() {
		assertFalse(ItemUtils.notNullorAir(null));
	}

	@Test
	void notNullorAir_airItem_isFalse() {
		assertFalse(ItemUtils.notNullorAir(new ItemStack(Material.AIR)));
	}

	@Test
	void notNullorAir_realItem_isTrue() {
		assertTrue(ItemUtils.notNullorAir(new ItemStack(Material.STONE)));
	}

	@Test
	void getSlot_itemPresent_returnsMatchingIndex() {
		Inventory inv = Bukkit.createInventory(null, 9);
		ItemStack target = new ItemStack(Material.DIAMOND_SWORD);
		inv.setItem(3, target);

		assertEquals(3, ItemUtils.getSlot(inv, target));
	}

	@Test
	void getSlot_itemAbsent_returnsNegativeOne() {
		Inventory inv = Bukkit.createInventory(null, 9);
		assertEquals(-1, ItemUtils.getSlot(inv, new ItemStack(Material.DIAMOND_SWORD)));
	}

	@Test
	void removeAll_removesEveryMatchingStackAndReturnsTotalAmount() {
		Inventory inv = Bukkit.createInventory(null, 9);
		ItemStack stoneA = new ItemStack(Material.STONE, 5);
		ItemStack stoneB = new ItemStack(Material.STONE, 2);
		ItemStack dirt = new ItemStack(Material.DIRT, 1);
		inv.setItem(0, stoneA);
		inv.setItem(1, stoneB);
		inv.setItem(2, dirt);

		int removed = ItemUtils.removeAll(inv, new ItemStack(Material.STONE));

		assertEquals(7, removed);
		assertEquals(null, inv.getItem(0));
		assertEquals(null, inv.getItem(1));
		assertNotNull(inv.getItem(2));
	}

	@Test
	void removeAll_noMatches_returnsZero() {
		Inventory inv = Bukkit.createInventory(null, 9);
		inv.setItem(0, new ItemStack(Material.DIRT));

		assertEquals(0, ItemUtils.removeAll(inv, new ItemStack(Material.STONE)));
	}

	@Test
	void setLore_arrayOverload_setsLoreOnFreshItem() {
		ItemStack item = new ItemStack(Material.STONE);

		ItemStack result = ItemUtils.setLore(item, new String[] { "line one", "line two" });

		assertEquals(Arrays.asList("line one", "line two"), result.getItemMeta().getLore());
	}

	@Test
	void setLore_listOverload_replacesExistingLore() {
		ItemStack item = new ItemStack(Material.STONE);
		ItemUtils.setLore(item, "first");

		ItemStack result = ItemUtils.setLore(item, List.of("second", "third"));

		assertEquals(List.of("second", "third"), result.getItemMeta().getLore());
	}

	@Test
	void setLore_singleStringOverload_wrapsIntoSingleLineLore() {
		ItemStack item = new ItemStack(Material.STONE);

		ItemStack result = ItemUtils.setLore(item, "only line");

		assertEquals(List.of("only line"), result.getItemMeta().getLore());
	}

	@Test
	void setLore_itemStartingWithoutItemMeta_createsMetaViaItemMetaNullCheck() {
		// Under MockBukkit, new ItemStack(Material) already comes back with
		// hasItemMeta() == true, so itemMetaNullCheck's meta-creation branch
		// (the `if (!item.hasItemMeta())` body) is never exercised by any
		// real ItemStack construction - confirmed empirically. On a real
		// server plenty of freshly-obtained ItemStacks have no meta yet, so
		// this uses a stateful Mockito mock (tracking hasItemMeta/getItemMeta
		// /setItemMeta consistently, the way a real ItemStack does) to
		// genuinely simulate that starting condition rather than skip it.
		//
		// This also exercises a real bug fix: itemMetaNullCheck used to run
		// *after* the caller had already captured item.getItemMeta() into a
		// local variable, so when hasItemMeta() was genuinely false that
		// local stayed null and the next line threw a NullPointerException.
		// Fixed by calling itemMetaNullCheck(item) before fetching meta.
		ItemStack item = Mockito.mock(ItemStack.class);
		AtomicReference<ItemMeta> metaRef = new AtomicReference<>();
		Mockito.when(item.getType()).thenReturn(Material.STONE);
		Mockito.when(item.hasItemMeta()).thenAnswer(inv -> metaRef.get() != null);
		Mockito.when(item.getItemMeta()).thenAnswer(inv -> metaRef.get());
		Mockito.doAnswer(inv -> {
			metaRef.set(inv.getArgument(0));
			return null;
		}).when(item).setItemMeta(Mockito.any());

		assertFalse(item.hasItemMeta());

		ItemStack result = ItemUtils.setLore(item, "created via null check");

		assertTrue(item.hasItemMeta());
		assertEquals(List.of("created via null check"), result.getItemMeta().getLore());
	}

	@Test
	void setDisplayName_withName_setsExactName() {
		ItemStack item = new ItemStack(Material.STONE);

		ItemStack result = ItemUtils.setDisplayName(item, "Custom Name");

		assertEquals("Custom Name", result.getItemMeta().getDisplayName());
	}

	@Test
	void setDisplayName_nullName_fallsBackToDefaultOrEmpty() {
		ItemStack item = new ItemStack(Material.STONE);

		ItemStack result = ItemUtils.setDisplayName(item, null);

		// Whatever the material's default item-factory display name is (or
		// "" if there isn't one), the call must not throw and must leave the
		// item meta non-null.
		assertNotNull(result.getItemMeta());
	}

	@Test
	void setDisplayName_nullNameAndNullDefaultName_fallsBackToEmptyString() {
		// Under MockBukkit, every real Material's item-factory default meta
		// returns "" (never null) from getDisplayName(), so the "default name
		// really is null" branch can't be reached with any real Material -
		// confirmed empirically. It's still real, reachable behavior on an
		// actual server (plenty of materials have no translated display
		// name), so this uses a genuine Mockito static mock of
		// Bukkit.getItemFactory() to force that condition rather than
		// skipping the branch or excluding it from coverage.
		ItemStack item = new ItemStack(Material.STONE);
		ItemFactory realFactory = Bukkit.getItemFactory();
		ItemMeta realMeta = realFactory.getItemMeta(Material.STONE);
		ItemMeta nullDisplayNameMeta = Mockito.spy(realMeta);
		Mockito.when(nullDisplayNameMeta.getDisplayName()).thenReturn(null);
		ItemFactory factoryWithNullDisplayName = Mockito.spy(realFactory);
		Mockito.when(factoryWithNullDisplayName.getItemMeta(Material.STONE)).thenReturn(nullDisplayNameMeta);

		try (MockedStatic<Bukkit> mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.CALLS_REAL_METHODS)) {
			mockedBukkit.when(Bukkit::getItemFactory).thenReturn(factoryWithNullDisplayName);

			ItemStack result = ItemUtils.setDisplayName(item, null);

			assertEquals("", result.getItemMeta().getDisplayName());
		}
	}

	@Test
	void addLore_arrayOverload_appendsToExistingLore() {
		ItemStack item = new ItemStack(Material.STONE);
		ItemUtils.setLore(item, "existing");

		ItemStack result = ItemUtils.addLore(item, new String[] { "added one", "added two" });

		assertEquals(List.of("existing", "added one", "added two"), result.getItemMeta().getLore());
	}

	@Test
	void addLore_arrayOverload_onItemWithNoLoreYet_startsFreshList() {
		ItemStack item = new ItemStack(Material.STONE);

		ItemStack result = ItemUtils.addLore(item, new String[] { "first ever line" });

		assertEquals(List.of("first ever line"), result.getItemMeta().getLore());
	}

	@Test
	void addLore_singleStringOverload_appendsOneLine() {
		ItemStack item = new ItemStack(Material.STONE);
		ItemUtils.setLore(item, "existing");

		ItemStack result = ItemUtils.addLore(item, "appended");

		assertEquals(List.of("existing", "appended"), result.getItemMeta().getLore());
	}

	@Test
	void getVisibleName_noItemMeta_returnsLowercasedMaterialName() {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		assertEquals("diamond_sword", ItemUtils.getVisibleName(item));
	}

	@Test
	void getVisibleName_hasDisplayName_returnsDisplayName() {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		ItemUtils.setDisplayName(item, "Excalibur");

		assertEquals("Excalibur", ItemUtils.getVisibleName(item));
	}

	@Test
	void getVisibleName_hasMetaButNoDisplayName_returnsLowercasedMaterialName() {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		// Give it item meta (via lore) without ever setting a display name.
		ItemUtils.setLore(item, "just some lore");

		assertEquals("diamond_sword", ItemUtils.getVisibleName(item));
	}
}
