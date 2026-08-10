package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FlintAndSteelPermissionsTest {

	@Test
	void constructor_buildsFireNodeAndInheritedNodes() {
		FlintAndSteelPermissions permissions = new FlintAndSteelPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.fire", permissions.getFire());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setFire_isFluentAndRoundTrips() {
		FlintAndSteelPermissions permissions = new FlintAndSteelPermissions("p", PermissionType.ALLOW);
		FlintAndSteelPermissions same = permissions.setFire("custom.fire");
		assertSame(permissions, same);
		assertEquals("custom.fire", permissions.getFire());
	}
}
