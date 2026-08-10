package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ShearPermissionsTest {

	@Test
	void constructor_buildsShearNodeAndInheritedNodes() {
		ShearPermissions permissions = new ShearPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.shear", permissions.getShear());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setShear_isFluentAndRoundTrips() {
		ShearPermissions permissions = new ShearPermissions("p", PermissionType.ALLOW);
		ShearPermissions same = permissions.setShear("custom.shear");
		assertSame(permissions, same);
		assertEquals("custom.shear", permissions.getShear());
	}
}
