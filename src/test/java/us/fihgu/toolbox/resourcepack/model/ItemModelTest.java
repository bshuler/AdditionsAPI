package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ItemModelTest {

	@Test
	void overridesDefaultsToNull() {
		ItemModel model = new ItemModel();
		assertNull(model.getOverrides());
	}

	@Test
	void addOverride_createsListLazilyOnFirstCall() {
		ItemModel model = new ItemModel();
		OverrideEntry entry = new OverrideEntry(new Predicate(), "item/broken");
		model.addOverride(entry);
		assertEquals(1, model.getOverrides().size());
		assertSame(entry, model.getOverrides().get(0));
	}

	@Test
	void addOverride_appendsToExistingListOnSubsequentCalls() {
		ItemModel model = new ItemModel();
		OverrideEntry first = new OverrideEntry(new Predicate(), "item/a");
		OverrideEntry second = new OverrideEntry(new Predicate(), "item/b");
		model.addOverride(first);
		model.addOverride(second);
		assertEquals(2, model.getOverrides().size());
	}

	@Test
	void setOverrides_replacesWholeList() {
		ItemModel model = new ItemModel();
		List<OverrideEntry> overrides = new LinkedList<>();
		model.setOverrides(overrides);
		assertSame(overrides, model.getOverrides());
	}

	@Test
	void createSimpleItemModel_setsParentAndLayer0Texture() {
		ItemModel model = ItemModel.createSimpleItemModel("item/generated", "item/my_item");
		assertEquals("item/generated", model.getParent());
		assertEquals("item/my_item", model.getTextures().get("layer0"));
	}
}
