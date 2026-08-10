package us.fihgu.toolbox.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Full behavioral coverage of {@link EnumCompat}, the runtime name-resolution
 * helper this project uses to survive Bukkit/Paper enum renames (e.g.
 * {@code Material.GRASS} -> {@code SHORT_GRASS}) across a single, unmodified
 * source tree compiled against multiple Paper API versions.
 * <p>
 * Uses two small local fixtures instead of real Bukkit/Paper types so this
 * test needs no MockBukkit/live server: {@link EnumCompat} operates purely
 * via {@link Enum#valueOf} and {@link java.lang.reflect.Field} reflection,
 * with no Bukkit dependency of its own.
 */
class EnumCompatTest {

	/** Stand-in for a real Bukkit/Paper enum that renamed one constant. */
	enum Fixture {
		OLD_NAME,
		OTHER
	}

	/**
	 * Stand-in for a class exposing its instances as public static fields on
	 * itself (like {@code org.bukkit.enchantments.Enchantment}, which is the
	 * real-world reason {@link EnumCompat#resolveStaticField} exists).
	 */
	public static final class FieldFixture {
		public static final FieldFixture PRESENT = new FieldFixture("value");
		public static final FieldFixture ANOTHER = new FieldFixture("other");
		public static final FieldFixture NULL_VALUED = null;
		private static final FieldFixture PRIVATE_FIELD = new FieldFixture("unreachable");

		private final String label;

		private FieldFixture(String label) {
			this.label = label;
		}

		String getLabel() {
			return label;
		}
	}

	// --- resolve(Class<E>, String...) ---

	@Test
	void resolve_findsFirstCandidateWhenPresent() {
		Fixture result = EnumCompat.resolve(Fixture.class, "OLD_NAME", "NEW_NAME");
		assertSame(Fixture.OLD_NAME, result);
	}

	@Test
	void resolve_fallsThroughToSecondCandidateWhenFirstMissing() {
		// Simulates the "new name doesn't exist on this API version yet"
		// direction: NEW_NAME isn't a real constant, OLD_NAME is.
		Fixture result = EnumCompat.resolve(Fixture.class, "NEW_NAME", "OLD_NAME");
		assertSame(Fixture.OLD_NAME, result);
	}

	@Test
	void resolve_bothDirectionsOfARename_oldFirst() {
		// "old API version": only the old name exists.
		Fixture result = EnumCompat.resolve(Fixture.class, "OLD_NAME");
		assertSame(Fixture.OLD_NAME, result);
	}

	@Test
	void resolve_bothDirectionsOfARename_viaRenamedAlias() {
		// "new API version": code asks for candidates in (new, old) order;
		// on this fixture the "new" name doesn't exist, so it must still
		// fall through correctly to the old one, proving the search doesn't
		// stop or throw on the first miss.
		Fixture result = EnumCompat.resolve(Fixture.class, "DOES_NOT_EXIST", "OLD_NAME");
		assertSame(Fixture.OLD_NAME, result);
	}

	@Test
	void resolve_returnsNullWhenNoCandidateMatches() {
		Fixture result = EnumCompat.resolve(Fixture.class, "NOPE", "ALSO_NOPE");
		assertNull(result);
	}

	@Test
	void resolve_returnsNullForEmptyCandidateList() {
		Fixture result = EnumCompat.resolve(Fixture.class);
		assertNull(result);
	}

	@Test
	void resolve_noCachingAcrossCalls_independentLookupsBothSucceed() {
		// There is no cache field in EnumCompat - confirm repeated/varied
		// calls are each resolved fresh and don't interfere with each other.
		Fixture first = EnumCompat.resolve(Fixture.class, "OLD_NAME");
		Fixture second = EnumCompat.resolve(Fixture.class, "OTHER");
		Fixture third = EnumCompat.resolve(Fixture.class, "OLD_NAME");
		assertSame(Fixture.OLD_NAME, first);
		assertSame(Fixture.OTHER, second);
		assertSame(Fixture.OLD_NAME, third);
	}

	// --- resolveStaticField(Class<T>, String...) ---
	//
	// Note on the `ClassCastException` half of this method's multi-catch
	// (`catch (ReflectiveOperationException | ClassCastException ignored)`):
	// because T is an unbounded type variable, `(T) value` erases to a cast
	// to Object and never actually throws ClassCastException inside this
	// method body (the JVM checkcast for the declared return type, if any,
	// is inserted at the *caller*, outside this try/catch, so it can't be
	// exercised or caught here at all). It is a defensive catch clause that
	// cannot be legitimately triggered under any real call - see PLAN.md.
	// It shares its catch body with ReflectiveOperationException though, so
	// the multi-catch's body line is still exercised (and covered) via the
	// NoSuchFieldException case below.

	@Test
	void resolveStaticField_findsFirstCandidateWhenPresent() {
		FieldFixture result = EnumCompat.resolveStaticField(FieldFixture.class, "PRESENT", "ANOTHER");
		assertSame(FieldFixture.PRESENT, result);
	}

	@Test
	void resolveStaticField_fallsThroughToSecondCandidateWhenFirstMissing() {
		FieldFixture result = EnumCompat.resolveStaticField(FieldFixture.class, "MISSING", "ANOTHER");
		assertSame(FieldFixture.ANOTHER, result);
	}

	@Test
	void resolveStaticField_skipsFieldThatExistsButIsNull() {
		// NULL_VALUED exists as a field but its value is null - resolve
		// must treat that as "not found" and keep trying, exercising the
		// `value != null` guard.
		FieldFixture result = EnumCompat.resolveStaticField(FieldFixture.class, "NULL_VALUED", "PRESENT");
		assertSame(FieldFixture.PRESENT, result);
	}

	@Test
	void resolveStaticField_returnsNullWhenNoFieldMatches() {
		FieldFixture result = EnumCompat.resolveStaticField(FieldFixture.class, "NOPE", "ALSO_NOPE");
		assertNull(result);
	}

	@Test
	void resolveStaticField_returnsNullForEmptyCandidateList() {
		FieldFixture result = EnumCompat.resolveStaticField(FieldFixture.class, new String[0]);
		assertNull(result);
	}

	@Test
	void resolveStaticField_skipsPrivateFieldNotAccessibleViaGetField() {
		// Class.getField only finds public fields; PRIVATE_FIELD must be
		// skipped (NoSuchFieldException, caught as ReflectiveOperationException)
		// and the search must fall through to the next candidate.
		FieldFixture result = EnumCompat.resolveStaticField(FieldFixture.class, "PRIVATE_FIELD", "PRESENT");
		assertSame(FieldFixture.PRESENT, result);
	}

	@Test
	void resolveStaticField_noCachingAcrossCalls_independentLookupsBothSucceed() {
		FieldFixture first = EnumCompat.resolveStaticField(FieldFixture.class, "PRESENT");
		FieldFixture second = EnumCompat.resolveStaticField(FieldFixture.class, "ANOTHER");
		assertEquals("value", first.getLabel());
		assertEquals("other", second.getLabel());
	}
}
