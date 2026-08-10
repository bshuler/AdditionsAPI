package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ElementFaceOptionsTest {

	@Test
	void allFacesDefaultToNull() {
		ElementFaceOptions options = new ElementFaceOptions();
		assertNull(options.getDown());
		assertNull(options.getUp());
		assertNull(options.getNorth());
		assertNull(options.getSouth());
		assertNull(options.getWest());
		assertNull(options.getEast());
	}

	@Test
	void everyFaceSlotRoundTrips() {
		ElementFaceOptions options = new ElementFaceOptions();
		ElementFaceEntry down = new ElementFaceEntry();
		ElementFaceEntry up = new ElementFaceEntry();
		ElementFaceEntry north = new ElementFaceEntry();
		ElementFaceEntry south = new ElementFaceEntry();
		ElementFaceEntry west = new ElementFaceEntry();
		ElementFaceEntry east = new ElementFaceEntry();

		options.setDown(down);
		options.setUp(up);
		options.setNorth(north);
		options.setSouth(south);
		options.setWest(west);
		options.setEast(east);

		assertSame(down, options.getDown());
		assertSame(up, options.getUp());
		assertSame(north, options.getNorth());
		assertSame(south, options.getSouth());
		assertSame(west, options.getWest());
		assertSame(east, options.getEast());
	}
}
