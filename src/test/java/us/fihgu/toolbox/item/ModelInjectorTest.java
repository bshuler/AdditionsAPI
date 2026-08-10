package us.fihgu.toolbox.item;

import com.chrismin13.additionsapi.durability.ItemDurability;
import com.chrismin13.additionsapi.items.CustomItem;
import com.chrismin13.additionsapi.permissions.ItemPermissions;
import com.chrismin13.additionsapi.permissions.PermissionType;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import us.fihgu.toolbox.resourcepack.model.DisplayOptions;
import us.fihgu.toolbox.resourcepack.model.ItemModel;
import us.fihgu.toolbox.resourcepack.model.Predicate;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code ModelInjector}'s default methods assume {@code this instanceof
 * CustomItem}. {@code CustomItem} itself is a concrete (non-abstract) class,
 * so - avoiding the 4/5-arg constructors that eagerly call
 * {@code ConfigFile.getInstance()} (the plugin-singleton static-init
 * landmine documented elsewhere) - it can be constructed directly via its
 * 6-arg constructor with a plain {@link ItemPermissions} instance, letting a
 * minimal test-only subclass exercise every default method for real instead
 * of excluding this interface from coverage.
 *
 * <p>{@code inject()} calls {@code Material.getMaxDurability()}, which under
 * MockBukkit lazily triggers a one-time registry/class-init chain
 * ({@code Tag}/{@code MaterialTags}/{@code BlockStateMetaMock}) that needs
 * {@code Bukkit.server} to be non-null. Without an explicit
 * {@code MockBukkit.mock()} here, whichever {@code inject_*} test happened to
 * run first would fail with an {@code IncompatiblePaperVersionException}/NPE
 * while later tests appeared to "pass" only because the partially-populated
 * registry left behind by that failure no longer looked empty on retry - not
 * because they genuinely worked. Same MockBukkit lifecycle pattern as
 * {@code ItemUtilsTest}/{@code DamageableItemTest} fixes this for real.
 */
class ModelInjectorTest {

	@TempDir
	File tempDir;

	@BeforeAll
	static void setUp() {
		MockBukkit.mock();
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}

	static class TestInjector extends CustomItem implements ModelInjector {
		private final HashMap<ModelInjection, Short> overrideEntries = new HashMap<>();
		private final HashMap<String, Short> allTextures = new HashMap<>();

		TestInjector(Material material, String idName) {
			super(material, 1, (short) 0, idName, new ItemDurability(),
					new ItemPermissions("test", PermissionType.ALLOW));
		}

		@Override
		public HashMap<ModelInjection, Short> getOverrideEntries() {
			return overrideEntries;
		}

		@Override
		public HashMap<String, Short> getAllTextures() {
			return allTextures;
		}

		@Override
		public String getDefaultTexture() {
			return "default";
		}
	}

	static class NotACustomItem implements ModelInjector {
		@Override
		public HashMap<ModelInjection, Short> getOverrideEntries() {
			return new HashMap<>();
		}

		@Override
		public HashMap<String, Short> getAllTextures() {
			return new HashMap<>();
		}

		@Override
		public String getDefaultTexture() {
			return "default";
		}
	}

	@Test
	void checkCustomItem_onCustomItemSubclass_doesNotThrow() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");
		injector.checkCustomItem();
	}

	@Test
	void checkCustomItem_onNonCustomItem_throwsRuntimeException() {
		NotACustomItem notCustomItem = new NotACustomItem();
		assertThrows(RuntimeException.class, notCustomItem::checkCustomItem);
	}

	@Test
	void addTexture_thenGetTexture_roundTripsByDurabilityValue() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");
		injector.addTexture("item/bow_pulling_0");
		injector.getAllTextures().put("item/bow_pulling_0", (short) 5);

		assertEquals("item/bow_pulling_0", injector.getTexture((short) 5));
	}

	@Test
	void getTexture_unknownDurability_returnsNull() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");
		assertNull(injector.getTexture((short) 42));
	}

	@Test
	void getTexture_nonMatchingEntryPresent_iteratesPastItAndReturnsNull() {
		// A non-empty map that still has no matching entry forces the loop
		// to actually run its body and loop back around (as opposed to the
		// empty-map case above, which never enters the loop body at all, or
		// a first-try match, which returns before ever looping back).
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");
		injector.getAllTextures().put("item/bow_pulling_0", (short) 5);

		assertNull(injector.getTexture((short) 42));
	}

	@Test
	void getDamageableItem_resolvesFromCustomItemMaterial() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");
		assertEquals(DamageableItem.BOW, injector.getDamageableItem());
	}

	@Test
	void getDamageableItem_onNonCustomItem_throwsRuntimeException() {
		NotACustomItem notCustomItem = new NotACustomItem();
		assertThrows(RuntimeException.class, notCustomItem::getDamageableItem);
	}

	@Test
	void inject_addsOverrideIntoBaseModelAndWritesModelJsonToWorkspace() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");

		ModelInjection entry = new ModelInjection(new Predicate(), "test/custom_bow",
				ItemModel.createSimpleItemModel("item/generated", "item/custom_bow"));
		injector.getOverrideEntries().put(entry, (short) 10);

		ItemModel baseModel = new ItemModel();
		injector.inject(baseModel, tempDir);

		assertEquals(1, baseModel.getOverrides().size());
		File written = new File(tempDir, "assets/test/models/item/custom_bow.json");
		assertTrue(written.exists());
	}

	@Test
	void inject_modelJsonAlreadyExists_doesNotOverwriteIt() throws Exception {
		// The `if (!json.exists())` guard's false branch (skip writing) is
		// only exercised when the model json is already present in the
		// workspace from a previous injection - pre-creating the file with
		// known content and confirming it's untouched proves the write was
		// genuinely skipped, not just that inject() didn't throw.
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");

		ModelInjection entry = new ModelInjection(new Predicate(), "test/custom_bow",
				ItemModel.createSimpleItemModel("item/generated", "item/custom_bow"));
		injector.getOverrideEntries().put(entry, (short) 10);

		File existing = new File(tempDir, "assets/test/models/item/custom_bow.json");
		existing.getParentFile().mkdirs();
		String sentinel = "{\"pre-existing\":true}";
		java.nio.file.Files.writeString(existing.toPath(), sentinel);

		ItemModel baseModel = new ItemModel();
		injector.inject(baseModel, tempDir);

		assertEquals(sentinel, java.nio.file.Files.readString(existing.toPath()));
	}

	@Test
	void inject_unbreakableItem_setsDamagedPredicateToZero() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");
		injector.setUnbreakable(true);

		Predicate predicate = new Predicate();
		ModelInjection entry = new ModelInjection(predicate, "test/item/unbreakable_bow",
				ItemModel.createSimpleItemModel("item/generated", "item/custom_bow"));
		injector.getOverrideEntries().put(entry, (short) 0);

		ItemModel baseModel = new ItemModel();
		injector.inject(baseModel, tempDir);

		assertEquals(0, predicate.getDamaged());
	}

	@Test
	void inject_standbyBowModelInjection_setsFullDisplayOptionsOnEntryModel() {
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");

		ItemModel entryModel = ItemModel.createSimpleItemModel("item/generated", "item/custom_bow_standby");
		BowModelInjection entry = new BowModelInjection(new Predicate(), "test/item/custom_bow_standby",
				entryModel, true);
		injector.getOverrideEntries().put(entry, (short) 0);

		ItemModel baseModel = new ItemModel();
		injector.inject(baseModel, tempDir);

		DisplayOptions display = entryModel.getDisplay();
		assertEquals(-80, display.getThirdperson_righthand().getRotation()[0]);
		assertEquals(-80, display.getThirdperson_lefthand().getRotation()[0]);
		assertEquals(0, display.getFirstperson_righthand().getRotation()[0]);
		assertEquals(0, display.getFirstperson_lefthand().getRotation()[0]);
	}

	@Test
	void inject_nonStandbyBowModelInjection_doesNotSetDisplayOptions() {
		// isStandby() == false must skip the bow-display block entirely,
		// distinguishing this from the standby case above.
		TestInjector injector = new TestInjector(Material.BOW, "test:bow");

		ItemModel entryModel = ItemModel.createSimpleItemModel("item/generated", "item/custom_bow_pulling");
		BowModelInjection entry = new BowModelInjection(new Predicate(), "test/item/custom_bow_pulling",
				entryModel, false);
		injector.getOverrideEntries().put(entry, (short) 1);

		ItemModel baseModel = new ItemModel();
		injector.inject(baseModel, tempDir);

		assertNull(entryModel.getDisplay());
	}
}
