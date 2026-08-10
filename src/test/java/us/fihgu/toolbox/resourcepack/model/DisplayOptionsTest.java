package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pure getter/setter pass-through, no validation - one round trip per field
 * is full coverage.
 */
class DisplayOptionsTest {

	@Test
	void allFieldsDefaultToNull() {
		DisplayOptions options = new DisplayOptions();
		assertNull(options.getThirdperson_righthand());
		assertNull(options.getThirdperson_lefthand());
		assertNull(options.getFirstperson_righthand());
		assertNull(options.getFirstperson_lefthand());
		assertNull(options.getGui());
		assertNull(options.getHead());
		assertNull(options.getGround());
		assertNull(options.getFixed());
	}

	@Test
	void everyEntrySlotRoundTrips() {
		DisplayOptions options = new DisplayOptions();

		DisplayEntry thirdRight = new DisplayEntry();
		DisplayEntry thirdLeft = new DisplayEntry();
		DisplayEntry firstRight = new DisplayEntry();
		DisplayEntry firstLeft = new DisplayEntry();
		DisplayEntry gui = new DisplayEntry();
		DisplayEntry head = new DisplayEntry();
		DisplayEntry ground = new DisplayEntry();
		DisplayEntry fixed = new DisplayEntry();

		options.setThirdperson_righthand(thirdRight);
		options.setThirdperson_lefthand(thirdLeft);
		options.setFirstperson_righthand(firstRight);
		options.setFirstperson_lefthand(firstLeft);
		options.setGui(gui);
		options.setHead(head);
		options.setGround(ground);
		options.setFixed(fixed);

		assertSame(thirdRight, options.getThirdperson_righthand());
		assertSame(thirdLeft, options.getThirdperson_lefthand());
		assertSame(firstRight, options.getFirstperson_righthand());
		assertSame(firstLeft, options.getFirstperson_lefthand());
		assertSame(gui, options.getGui());
		assertSame(head, options.getHead());
		assertSame(ground, options.getGround());
		assertSame(fixed, options.getFixed());
	}
}
