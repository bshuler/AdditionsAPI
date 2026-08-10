package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Full behavioral coverage of {@link OverrideEntry#compareTo}, which
 * cascades through pulling -> pull -> damaged -> damage, with an important
 * asymmetry confirmed by reading the source: the {@code pull} comparison
 * treats "one side null" as a genuine ordering (null pull sorts before a
 * present pull), while {@code pulling}/{@code damaged}/{@code damage} treat
 * "either side null" as "stop comparing, they're equal here" instead.
 */
class OverrideEntryTest {

	private static Predicate predicate() {
		return new Predicate();
	}

	@Test
	void compareTo_sameReference_isEqual() {
		OverrideEntry entry = new OverrideEntry(predicate(), "item/a");
		assertEquals(0, entry.compareTo(entry));
	}

	@Test
	void compareTo_pullingLessThan_isBefore() {
		OverrideEntry a = new OverrideEntry(predicate().setPulling(0), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPulling(1), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_pullingGreaterThan_isAfter() {
		OverrideEntry a = new OverrideEntry(predicate().setPulling(1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPulling(0), "b");
		assertEquals(1, a.compareTo(b));
	}

	@Test
	void compareTo_pullingEqual_fallsThroughToPull() {
		OverrideEntry a = new OverrideEntry(predicate().setPulling(1).setPull(0.1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPulling(1).setPull(0.5), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_pullLessThan_isBefore() {
		OverrideEntry a = new OverrideEntry(predicate().setPull(0.1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPull(0.5), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_pullGreaterThan_isAfter() {
		OverrideEntry a = new OverrideEntry(predicate().setPull(0.5), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPull(0.1), "b");
		assertEquals(1, a.compareTo(b));
	}

	@Test
	void compareTo_pullNullVsPresent_nullSortsBefore() {
		OverrideEntry a = new OverrideEntry(predicate(), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPull(0.5), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_pullPresentVsNull_presentSortsAfter() {
		OverrideEntry a = new OverrideEntry(predicate().setPull(0.5), "a");
		OverrideEntry b = new OverrideEntry(predicate(), "b");
		assertEquals(1, a.compareTo(b));
	}

	@Test
	void compareTo_pullEqual_fallsThroughToDamaged() {
		OverrideEntry a = new OverrideEntry(predicate().setPull(0.5).setDamaged(0), "a");
		OverrideEntry b = new OverrideEntry(predicate().setPull(0.5).setDamaged(1), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_damagedEitherNull_isEqual() {
		OverrideEntry a = new OverrideEntry(predicate(), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1), "b");
		assertEquals(0, a.compareTo(b));
	}

	@Test
	void compareTo_damagedLessThan_isBefore() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(0), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_damagedGreaterThan_isAfter() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(0), "b");
		assertEquals(1, a.compareTo(b));
	}

	@Test
	void compareTo_damagedEqual_fallsThroughToDamage() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(1).setDamage(0.1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1).setDamage(0.9), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_damageEitherNull_isEqual() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1).setDamage(0.9), "b");
		assertEquals(0, a.compareTo(b));
	}

	@Test
	void compareTo_damageLessThan_isBefore() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(1).setDamage(0.1), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1).setDamage(0.9), "b");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	void compareTo_damageGreaterThan_isAfter() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(1).setDamage(0.9), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1).setDamage(0.1), "b");
		assertEquals(1, a.compareTo(b));
	}

	@Test
	void compareTo_damageEqual_isEqual() {
		OverrideEntry a = new OverrideEntry(predicate().setDamaged(1).setDamage(0.5), "a");
		OverrideEntry b = new OverrideEntry(predicate().setDamaged(1).setDamage(0.5), "b");
		assertEquals(0, a.compareTo(b));
	}

	@Test
	void getPredicateAndGetModel_roundTrip() {
		Predicate predicate = predicate();
		OverrideEntry entry = new OverrideEntry(predicate, "item/model");
		assertSame(predicate, entry.getPredicate());
		assertEquals("item/model", entry.getModel());
	}

	@Test
	void setPredicateAndSetModel_roundTrip() {
		OverrideEntry entry = new OverrideEntry(predicate(), "item/a");
		Predicate newPredicate = predicate();
		entry.setPredicate(newPredicate);
		entry.setModel("item/b");
		assertSame(newPredicate, entry.getPredicate());
		assertEquals("item/b", entry.getModel());
	}

	@Test
	void clone_producesDistinctEntryWithClonedPredicateAndSameModelString() {
		Predicate predicate = predicate().setDamage(0.5).setDamaged(1);
		OverrideEntry original = new OverrideEntry(predicate, "item/broken");

		OverrideEntry clone = original.clone();

		assertNotSame(original, clone);
		assertNotSame(predicate, clone.getPredicate());
		assertEquals(predicate.getDamage(), clone.getPredicate().getDamage());
		assertEquals(predicate.getDamaged(), clone.getPredicate().getDamaged());
		assertEquals("item/broken", clone.getModel());
	}
}
