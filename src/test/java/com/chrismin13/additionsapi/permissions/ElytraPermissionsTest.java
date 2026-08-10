package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ElytraPermissionsTest {

	@Test
	void constructor_buildsFlightNodeAndInheritedNodes() {
		ElytraPermissions permissions = new ElytraPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.flight", permissions.getFlight());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setFlight_isFluentAndRoundTrips() {
		ElytraPermissions permissions = new ElytraPermissions("p", PermissionType.ALLOW);
		ElytraPermissions same = permissions.setFlight("custom.flight");
		assertSame(permissions, same);
		assertEquals("custom.flight", permissions.getFlight());
	}
}
