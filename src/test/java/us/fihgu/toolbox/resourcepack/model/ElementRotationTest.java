package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unlike {@link DisplayEntry}/{@link ModelElement}, {@code origin} here has
 * no length validation despite the javadoc mentioning a [x, y, z] default -
 * confirmed by reading the source; this test locks in that actual (permissive)
 * behavior rather than assuming validation exists.
 */
class ElementRotationTest {

	@Test
	void noArgConstructor_allFieldsNull() {
		ElementRotation rotation = new ElementRotation();
		assertNull(rotation.getOrigin());
		assertNull(rotation.getAxis());
		assertNull(rotation.getAngle());
		assertNull(rotation.getRescale());
	}

	@Test
	void fullConstructor_setsAllFields() {
		double[] origin = {8, 8, 8};
		ElementRotation rotation = new ElementRotation(origin, ModelAxis.y, 22.5, true);
		assertArrayEquals(origin, rotation.getOrigin());
		assertEquals(ModelAxis.y, rotation.getAxis());
		assertEquals(22.5, rotation.getAngle());
		assertEquals(Boolean.TRUE, rotation.getRescale());
	}

	@Test
	void setOrigin_doesNotValidateLength() {
		ElementRotation rotation = new ElementRotation();
		double[] wrongLength = {1, 2};
		rotation.setOrigin(wrongLength);
		assertArrayEquals(wrongLength, rotation.getOrigin());
	}

	@Test
	void setAxis_roundTrips() {
		ElementRotation rotation = new ElementRotation();
		rotation.setAxis(ModelAxis.z);
		assertEquals(ModelAxis.z, rotation.getAxis());
	}

	@Test
	void setAngle_roundTrips() {
		ElementRotation rotation = new ElementRotation();
		rotation.setAngle(-45.0);
		assertEquals(-45.0, rotation.getAngle());
	}

	@Test
	void setRescale_roundTrips() {
		ElementRotation rotation = new ElementRotation();
		rotation.setRescale(false);
		assertEquals(Boolean.FALSE, rotation.getRescale());
	}
}
