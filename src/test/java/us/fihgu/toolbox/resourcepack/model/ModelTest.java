package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTest {

	@Test
	void allFieldsDefaultToNull() {
		Model model = new Model();
		assertNull(model.getParent());
		assertNull(model.getTextures());
		assertNull(model.getDisplay());
		assertNull(model.getElements());
	}

	@Test
	void setParent_roundTrips() {
		Model model = new Model();
		model.setParent("item/generated");
		assertEquals("item/generated", model.getParent());
	}

	@Test
	void putTexture_createsMapLazilyOnFirstCall() {
		Model model = new Model();
		model.putTexture("layer0", "item/my_item");
		assertEquals("item/my_item", model.getTextures().get("layer0"));
	}

	@Test
	void putTexture_reusesExistingMapOnSubsequentCalls() {
		Model model = new Model();
		model.putTexture("layer0", "item/first");
		model.putTexture("particle", "item/second");
		assertEquals(2, model.getTextures().size());
		assertEquals("item/first", model.getTextures().get("layer0"));
		assertEquals("item/second", model.getTextures().get("particle"));
	}

	@Test
	void setTextures_replacesWholeMap() {
		Model model = new Model();
		HashMap<String, String> textures = new HashMap<>();
		textures.put("all", "block/stone");
		model.setTextures(textures);
		assertSame(textures, model.getTextures());
	}

	@Test
	void setDisplay_roundTrips() {
		Model model = new Model();
		DisplayOptions display = new DisplayOptions();
		model.setDisplay(display);
		assertSame(display, model.getDisplay());
	}

	@Test
	void addElements_createsListLazilyOnFirstCall() {
		Model model = new Model();
		ModelElement element = new ModelElement();
		model.addElements(element);
		assertEquals(1, model.getElements().size());
		assertSame(element, model.getElements().get(0));
	}

	@Test
	void addElements_appendsToExistingListOnSubsequentCalls() {
		Model model = new Model();
		ModelElement first = new ModelElement();
		ModelElement second = new ModelElement();
		model.addElements(first);
		model.addElements(second);
		assertEquals(2, model.getElements().size());
		assertSame(first, model.getElements().get(0));
		assertSame(second, model.getElements().get(1));
	}

	@Test
	void setElements_replacesWholeList() {
		Model model = new Model();
		List<ModelElement> elements = new LinkedList<>();
		elements.add(new ModelElement());
		model.setElements(elements);
		assertSame(elements, model.getElements());
		assertTrue(model.getElements().size() == 1);
	}
}
