package com.chrismin13.additionsapi.permissions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionTypeTest {

	@Test
	void hasExactlyTwoConstantsInDeclarationOrder() {
		assertArrayEquals(new PermissionType[] {PermissionType.ALLOW, PermissionType.DENY}, PermissionType.values());
	}

	@Test
	void getPermission_returnsLowercaseName() {
		assertEquals("allow", PermissionType.ALLOW.getPermission());
		assertEquals("deny", PermissionType.DENY.getPermission());
	}
}
