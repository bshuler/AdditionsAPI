package com.chrismin13.additionsapi.utils;

import com.chrismin13.additionsapi.permissions.PermissionType;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionUtilsTest {

	@Test
	void constructor_isCallable() {
		// PermissionUtils is a static-only helper with no explicit
		// constructor; this just exercises the implicit default constructor
		// for coverage.
		new PermissionUtils();
	}

	@Test
	void allow_withPermission_isAllowed() {
		Player player = Mockito.mock(Player.class);
		Mockito.when(player.hasPermission("perm.node")).thenReturn(true);
		assertTrue(PermissionUtils.allowedAction(player, PermissionType.ALLOW, "perm.node"));
	}

	@Test
	void allow_withoutPermission_isDenied() {
		Player player = Mockito.mock(Player.class);
		Mockito.when(player.hasPermission("perm.node")).thenReturn(false);
		assertFalse(PermissionUtils.allowedAction(player, PermissionType.ALLOW, "perm.node"));
	}

	@Test
	void deny_withoutPermission_isAllowed() {
		Player player = Mockito.mock(Player.class);
		Mockito.when(player.hasPermission("perm.node")).thenReturn(false);
		Mockito.when(player.isOp()).thenReturn(false);
		Mockito.when(player.hasPermission("*")).thenReturn(false);
		assertTrue(PermissionUtils.allowedAction(player, PermissionType.DENY, "perm.node"));
	}

	@Test
	void deny_withPermissionButOp_isAllowed() {
		Player player = Mockito.mock(Player.class);
		Mockito.when(player.hasPermission("perm.node")).thenReturn(true);
		Mockito.when(player.isOp()).thenReturn(true);
		assertTrue(PermissionUtils.allowedAction(player, PermissionType.DENY, "perm.node"));
	}

	@Test
	void deny_withPermissionButWildcard_isAllowed() {
		Player player = Mockito.mock(Player.class);
		Mockito.when(player.hasPermission("perm.node")).thenReturn(true);
		Mockito.when(player.isOp()).thenReturn(false);
		Mockito.when(player.hasPermission("*")).thenReturn(true);
		assertTrue(PermissionUtils.allowedAction(player, PermissionType.DENY, "perm.node"));
	}

	@Test
	void deny_withPermissionNoOpNoWildcard_isDenied() {
		Player player = Mockito.mock(Player.class);
		Mockito.when(player.hasPermission("perm.node")).thenReturn(true);
		Mockito.when(player.isOp()).thenReturn(false);
		Mockito.when(player.hasPermission("*")).thenReturn(false);
		assertFalse(PermissionUtils.allowedAction(player, PermissionType.DENY, "perm.node"));
	}
}
