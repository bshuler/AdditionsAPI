package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpadePermissionsTest {

	@Test
	void constructor_buildsTileNodeAndInheritedNodes() {
		SpadePermissions permissions = new SpadePermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.tile", permissions.getTile());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setTile_isFluentAndRoundTrips() {
		SpadePermissions permissions = new SpadePermissions("p", PermissionType.ALLOW);
		SpadePermissions same = permissions.setTile("custom.tile");
		assertSame(permissions, same);
		assertEquals("custom.tile", permissions.getTile());
	}
}
