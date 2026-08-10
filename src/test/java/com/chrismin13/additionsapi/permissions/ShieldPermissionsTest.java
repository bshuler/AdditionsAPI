package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShieldPermissionsTest {

	@Test
	void constructor_buildsBlockNodeAndInheritedNodes() {
		ShieldPermissions permissions = new ShieldPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.block", permissions.getBlock());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setBlock_isVoidAndRoundTrips() {
		ShieldPermissions permissions = new ShieldPermissions("p", PermissionType.ALLOW);
		permissions.setBlock("custom.block");
		assertEquals("custom.block", permissions.getBlock());
	}
}
