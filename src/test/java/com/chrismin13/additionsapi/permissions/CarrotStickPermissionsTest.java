package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CarrotStickPermissionsTest {

	@Test
	void constructor_buildsBoostNodeAndInheritedNodes() {
		CarrotStickPermissions permissions = new CarrotStickPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.boost", permissions.getBoost());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setBoost_isFluentAndRoundTrips() {
		CarrotStickPermissions permissions = new CarrotStickPermissions("p", PermissionType.ALLOW);
		CarrotStickPermissions same = permissions.setBoost("custom.boost");
		assertSame(permissions, same);
		assertEquals("custom.boost", permissions.getBoost());
	}
}
