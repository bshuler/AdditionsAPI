package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElementFaceEntryTest {

	@Test
	void noArgConstructor_allFieldsNull() {
		ElementFaceEntry entry = new ElementFaceEntry();
		assertNull(entry.getUv());
		assertNull(entry.getTexture());
		assertNull(entry.getCullface());
		assertNull(entry.getRotation());
		assertNull(entry.getTintindex());
	}

	@Test
	void fullConstructor_setsAllFields() {
		double[] uv = {0, 0, 16, 16};
		ElementFaceEntry entry = new ElementFaceEntry(uv, "#all", ModelFace.up, 90.0, 1);
		assertArrayEquals(uv, entry.getUv());
		assertEquals("#all", entry.getTexture());
		assertEquals(ModelFace.up, entry.getCullface());
		assertEquals(90.0, entry.getRotation());
		assertEquals(1, entry.getTintindex());
	}

	@Test
	void setUv_nullIsAllowed() {
		ElementFaceEntry entry = new ElementFaceEntry();
		entry.setUv(null);
		assertNull(entry.getUv());
	}

	@Test
	void setUv_wrongLengthThrows() {
		ElementFaceEntry entry = new ElementFaceEntry();
		assertThrows(IllegalArgumentException.class, () -> entry.setUv(new double[] {0, 0, 16}));
	}

	@Test
	void setTexture_roundTrips() {
		ElementFaceEntry entry = new ElementFaceEntry();
		entry.setTexture("#side");
		assertEquals("#side", entry.getTexture());
	}

	@Test
	void setCullface_roundTrips() {
		ElementFaceEntry entry = new ElementFaceEntry();
		entry.setCullface(ModelFace.down);
		assertEquals(ModelFace.down, entry.getCullface());
	}

	@Test
	void setRotation_roundTrips() {
		ElementFaceEntry entry = new ElementFaceEntry();
		entry.setRotation(180.0);
		assertEquals(180.0, entry.getRotation());
	}

	@Test
	void setTintindex_roundTrips() {
		ElementFaceEntry entry = new ElementFaceEntry();
		entry.setTintindex(0);
		assertEquals(0, entry.getTintindex());
	}
}
