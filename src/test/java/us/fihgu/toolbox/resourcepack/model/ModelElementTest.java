package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelElementTest {

	@Test
	void noArgConstructor_allFieldsNull() {
		ModelElement element = new ModelElement();
		assertNull(element.getFrom());
		assertNull(element.getTo());
		assertNull(element.getRotation());
		assertNull(element.getShade());
		assertNull(element.getFaces());
	}

	@Test
	void fullConstructor_setsAllFields() {
		double[] from = {0, 0, 0};
		double[] to = {16, 16, 16};
		ElementRotation rotation = new ElementRotation();
		ElementFaceOptions faces = new ElementFaceOptions();
		ModelElement element = new ModelElement(from, to, rotation, true, faces);
		assertArrayEquals(from, element.getFrom());
		assertArrayEquals(to, element.getTo());
		assertSame(rotation, element.getRotation());
		assertEquals(Boolean.TRUE, element.getShade());
		assertSame(faces, element.getFaces());
	}

	@Test
	void setFrom_nullIsAllowed() {
		ModelElement element = new ModelElement();
		element.setFrom(null);
		assertNull(element.getFrom());
	}

	@Test
	void setFrom_wrongLengthThrows() {
		ModelElement element = new ModelElement();
		assertThrows(IllegalArgumentException.class, () -> element.setFrom(new double[] {1, 2}));
	}

	@Test
	void setTo_nullIsAllowed() {
		ModelElement element = new ModelElement();
		element.setTo(null);
		assertNull(element.getTo());
	}

	@Test
	void setTo_wrongLengthThrows() {
		ModelElement element = new ModelElement();
		assertThrows(IllegalArgumentException.class, () -> element.setTo(new double[] {1, 2, 3, 4}));
	}

	@Test
	void setShade_roundTrips() {
		ModelElement element = new ModelElement();
		element.setShade(false);
		assertEquals(Boolean.FALSE, element.getShade());
	}

	@Test
	void setFaces_roundTrips() {
		ModelElement element = new ModelElement();
		ElementFaceOptions faces = new ElementFaceOptions();
		element.setFaces(faces);
		assertSame(faces, element.getFaces());
	}

	@Test
	void setRotation_roundTrips() {
		ModelElement element = new ModelElement();
		ElementRotation rotation = new ElementRotation();
		element.setRotation(rotation);
		assertSame(rotation, element.getRotation());
	}
}
