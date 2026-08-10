package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishingRodPermissionsTest {

	@Test
	void constructor_buildsReelNodeAndInheritedNodes() {
		FishingRodPermissions permissions = new FishingRodPermissions("p", PermissionType.ALLOW);
		assertEquals("p.allow.reel", permissions.getReel());
		assertEquals("p.allow.break", permissions.getBreak());
	}

	@Test
	void setReel_isVoidAndRoundTrips() {
		FishingRodPermissions permissions = new FishingRodPermissions("p", PermissionType.ALLOW);
		permissions.setReel("custom.reel");
		assertEquals("custom.reel", permissions.getReel());
	}
}
