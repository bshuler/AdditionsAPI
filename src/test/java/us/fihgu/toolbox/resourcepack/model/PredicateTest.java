package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@code clone()} is {@code protected}, reachable from this test only
 * because it lives in the same package as {@link Predicate}.
 */
class PredicateTest {

	@Test
	void allFieldsDefaultToNull() {
		Predicate predicate = new Predicate();
		assertNull(predicate.getAngle());
		assertNull(predicate.getBlocking());
		assertNull(predicate.getBroken());
		assertNull(predicate.getCast());
		assertNull(predicate.getCooldown());
		assertNull(predicate.getDamage());
		assertNull(predicate.getDamaged());
		assertNull(predicate.getLefthanded());
		assertNull(predicate.getPull());
		assertNull(predicate.getPulling());
		assertNull(predicate.getTime());
	}

	@Test
	void everySetterReturnsThisAndRoundTrips() {
		Predicate predicate = new Predicate();
		Predicate same = predicate.setAngle(0.5).setBlocking(1).setBroken(0).setCast(1).setCooldown(0.25)
				.setDamage(0.75).setDamaged(1).setLefthanded(0).setPull(0.9).setPulling(1).setTime(0.1);

		assertSame(predicate, same);
		assertEquals(0.5, predicate.getAngle());
		assertEquals(1, predicate.getBlocking());
		assertEquals(0, predicate.getBroken());
		assertEquals(1, predicate.getCast());
		assertEquals(0.25, predicate.getCooldown());
		assertEquals(0.75, predicate.getDamage());
		assertEquals(1, predicate.getDamaged());
		assertEquals(0, predicate.getLefthanded());
		assertEquals(0.9, predicate.getPull());
		assertEquals(1, predicate.getPulling());
		assertEquals(0.1, predicate.getTime());
	}

	@Test
	void clone_producesDistinctInstanceWithEqualFieldValues() {
		Predicate original = new Predicate().setAngle(0.5).setBlocking(1).setBroken(0).setCast(1).setCooldown(0.25)
				.setDamage(0.75).setDamaged(1).setLefthanded(0).setPull(0.9).setPulling(1).setTime(0.1);

		Predicate clone = original.clone();

		assertNotSame(original, clone);
		assertEquals(original.getAngle(), clone.getAngle());
		assertEquals(original.getBlocking(), clone.getBlocking());
		assertEquals(original.getBroken(), clone.getBroken());
		assertEquals(original.getCast(), clone.getCast());
		assertEquals(original.getCooldown(), clone.getCooldown());
		assertEquals(original.getDamage(), clone.getDamage());
		assertEquals(original.getDamaged(), clone.getDamaged());
		assertEquals(original.getLefthanded(), clone.getLefthanded());
		assertEquals(original.getPull(), clone.getPull());
		assertEquals(original.getPulling(), clone.getPulling());
		assertEquals(original.getTime(), clone.getTime());
	}

	@Test
	void clone_ofAllNullPredicateProducesAllNullClone() {
		Predicate clone = new Predicate().clone();
		assertNotSame(new Predicate(), clone);
		assertNull(clone.getAngle());
		assertNull(clone.getPull());
	}
}
