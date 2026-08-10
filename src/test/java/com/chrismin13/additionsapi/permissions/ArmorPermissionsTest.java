package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ArmorPermissionsTest {

	@Test
	void constructor_buildsWearNodeAndInheritedNodes() {
		ArmorPermissions permissions = new ArmorPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.wear", permissions.getWear());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setWear_isFluentAndRoundTrips() {
		ArmorPermissions permissions = new ArmorPermissions("p", PermissionType.ALLOW);
		ArmorPermissions same = permissions.setWear("custom.wear");
		assertSame(permissions, same);
		assertEquals("custom.wear", permissions.getWear());
	}
}
