package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BowPermissionsTest {

	@Test
	void constructor_buildsShootNodeAndInheritedNodes() {
		BowPermissions permissions = new BowPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.shoot", permissions.getShoot());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setShoot_isFluentAndRoundTrips() {
		BowPermissions permissions = new BowPermissions("p", PermissionType.ALLOW);
		BowPermissions same = permissions.setShoot("custom.shoot");
		assertSame(permissions, same);
		assertEquals("custom.shoot", permissions.getShoot());
	}
}
