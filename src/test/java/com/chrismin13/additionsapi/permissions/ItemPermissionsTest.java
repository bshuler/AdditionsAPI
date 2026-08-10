package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ItemPermissionsTest {

	@Test
	void constructor_buildsAllFivePermissionNodes() {
		ItemPermissions permissions = new ItemPermissions("myplugin.myitem", PermissionType.ALLOW);
		assertEquals("myplugin.myitem", permissions.getPermissionPrefix());
		assertEquals(PermissionType.ALLOW, permissions.getType());
		assertEquals("myplugin.myitem.allow.break", permissions.getBreak());
		assertEquals("myplugin.myitem.allow.hit", permissions.getHit());
		assertEquals("myplugin.myitem.allow.craft", permissions.getCraft());
		assertEquals("myplugin.myitem.allow.drop", permissions.getDrop());
		assertEquals("myplugin.myitem.allow.pickup", permissions.getPickup());
	}

	@Test
	void constructor_withDenyType_buildsDenyNodes() {
		ItemPermissions permissions = new ItemPermissions("myplugin.myitem", PermissionType.DENY);
		assertEquals("myplugin.myitem.deny.break", permissions.getBreak());
	}

	@Test
	void setBreak_isFluentAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		ItemPermissions same = permissions.setBreak("custom.break");
		assertSame(permissions, same);
		assertEquals("custom.break", permissions.getBreak());
	}

	@Test
	void setHit_isFluentAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		ItemPermissions same = permissions.setHit("custom.hit");
		assertSame(permissions, same);
		assertEquals("custom.hit", permissions.getHit());
	}

	@Test
	void setPermissionPrefix_isVoidAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		permissions.setPermissionPrefix("newprefix");
		assertEquals("newprefix", permissions.getPermissionPrefix());
	}

	@Test
	void setType_isVoidAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		permissions.setType(PermissionType.DENY);
		assertEquals(PermissionType.DENY, permissions.getType());
	}

	@Test
	void setCraft_isVoidAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		permissions.setCraft("custom.craft");
		assertEquals("custom.craft", permissions.getCraft());
	}

	@Test
	void setDrop_isVoidAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		permissions.setDrop("custom.drop");
		assertEquals("custom.drop", permissions.getDrop());
	}

	@Test
	void setPickup_isVoidAndRoundTrips() {
		ItemPermissions permissions = new ItemPermissions("p", PermissionType.ALLOW);
		permissions.setPickup("custom.pickup");
		assertEquals("custom.pickup", permissions.getPickup());
	}
}
