package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class HoePermissionsTest {

	@Test
	void constructor_buildsHoeNodeAndInheritedNodes() {
		HoePermissions permissions = new HoePermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.hoe", permissions.getHoe());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setHoe_isFluentAndRoundTrips() {
		HoePermissions permissions = new HoePermissions("p", PermissionType.ALLOW);
		HoePermissions same = permissions.setHoe("custom.hoe");
		assertSame(permissions, same);
		assertEquals("custom.hoe", permissions.getHoe());
	}
}
