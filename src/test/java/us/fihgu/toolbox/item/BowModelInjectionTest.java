package us.fihgu.toolbox.item;

import org.junit.jupiter.api.Test;
import us.fihgu.toolbox.resourcepack.model.ItemModel;
import us.fihgu.toolbox.resourcepack.model.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BowModelInjectionTest {

	@Test
	void constructor_delegatesToSuperAndSetsStandby() {
		Predicate predicate = new Predicate();
		ItemModel model = new ItemModel();

		BowModelInjection injection = new BowModelInjection(predicate, "item/bow_pulling_0", model, true);

		assertSame(predicate, injection.getPredicate());
		assertSame("item/bow_pulling_0", injection.getModelName());
		assertSame(model, injection.getModel());
		assertTrue(injection.isStandby());
	}

	@Test
	void constructor_falseStandby_isStandbyFalse() {
		BowModelInjection injection = new BowModelInjection(new Predicate(), "item/bow", new ItemModel(), false);
		assertFalse(injection.isStandby());
	}

	@Test
	void setStandby_isFluentAndRoundTrips() {
		BowModelInjection injection = new BowModelInjection(new Predicate(), "item/bow", new ItemModel(), false);

		BowModelInjection same = injection.setStandby(true);

		assertSame(injection, same);
		assertTrue(injection.isStandby());
	}
}
