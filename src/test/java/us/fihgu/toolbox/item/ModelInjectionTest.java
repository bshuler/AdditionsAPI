package us.fihgu.toolbox.item;

import org.junit.jupiter.api.Test;
import us.fihgu.toolbox.resourcepack.model.ItemModel;
import us.fihgu.toolbox.resourcepack.model.Predicate;

import static org.junit.jupiter.api.Assertions.assertSame;

class ModelInjectionTest {

	@Test
	void constructor_setsAllThreeFields() {
		Predicate predicate = new Predicate();
		ItemModel model = new ItemModel();

		ModelInjection injection = new ModelInjection(predicate, "item/example", model);

		assertSame(predicate, injection.getPredicate());
		assertSame("item/example", injection.getModelName());
		assertSame(model, injection.getModel());
	}
}
