package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisplayEntryTest {

	@Test
	void noArgConstructor_allFieldsNull() {
		DisplayEntry entry = new DisplayEntry();
		assertNull(entry.getRotation());
		assertNull(entry.getTranslation());
		assertNull(entry.getScale());
	}

	@Test
	void threeArgConstructor_setsAllThreeFields() {
		double[] rotation = {1, 2, 3};
		double[] translation = {4, 5, 6};
		double[] scale = {7, 8, 9};
		DisplayEntry entry = new DisplayEntry(rotation, translation, scale);
		assertArrayEquals(rotation, entry.getRotation());
		assertArrayEquals(translation, entry.getTranslation());
		assertArrayEquals(scale, entry.getScale());
	}

	@Test
	void setRotation_nullIsAllowed() {
		DisplayEntry entry = new DisplayEntry();
		entry.setRotation(null);
		assertNull(entry.getRotation());
	}

	@Test
	void setRotation_wrongLengthThrows() {
		DisplayEntry entry = new DisplayEntry();
		assertThrows(IllegalArgumentException.class, () -> entry.setRotation(new double[] {1, 2}));
	}

	@Test
	void setTranslation_nullIsAllowed() {
		DisplayEntry entry = new DisplayEntry();
		entry.setTranslation(null);
		assertNull(entry.getTranslation());
	}

	@Test
	void setTranslation_wrongLengthThrows() {
		DisplayEntry entry = new DisplayEntry();
		assertThrows(IllegalArgumentException.class, () -> entry.setTranslation(new double[] {1, 2, 3, 4}));
	}

	@Test
	void setScale_nullIsAllowed() {
		DisplayEntry entry = new DisplayEntry();
		entry.setScale(null);
		assertNull(entry.getScale());
	}

	@Test
	void setScale_wrongLengthThrows() {
		DisplayEntry entry = new DisplayEntry();
		assertThrows(IllegalArgumentException.class, () -> entry.setScale(new double[0]));
	}

	@Test
	void setScale_correctLengthAccepted() {
		DisplayEntry entry = new DisplayEntry();
		entry.setScale(new double[] {1, 1, 1});
		assertArrayEquals(new double[] {1, 1, 1}, entry.getScale());
	}
}
