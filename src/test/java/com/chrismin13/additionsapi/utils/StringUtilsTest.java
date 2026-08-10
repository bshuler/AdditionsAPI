package com.chrismin13.additionsapi.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StringUtilsTest {

	@Test
	void constructor_isCallable() {
		// StringUtils is a static-only helper with no explicit constructor;
		// this just exercises the implicit default constructor for coverage.
		new com.chrismin13.additionsapi.utils.StringUtils();
	}

	@Test
	void startsWith_nullStart_returnsSameListInstance() {
		List<String> subcommands = new ArrayList<>(Arrays.asList("foo", "bar"));
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(subcommands, null);
		// Confirmed via source: this guard path returns the original
		// reference, not a defensive copy.
		assertSame(subcommands, result);
	}

	@Test
	void startsWith_emptyStart_returnsSameListInstance() {
		List<String> subcommands = new ArrayList<>(Arrays.asList("foo", "bar"));
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(subcommands, "");
		assertSame(subcommands, result);
	}

	@Test
	void startsWith_nullSubcommands_returnsSameNullReference() {
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(null, "f");
		assertEquals(null, result);
	}

	@Test
	void startsWith_emptySubcommands_returnsSameListInstance() {
		List<String> subcommands = new ArrayList<>();
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(subcommands, "f");
		assertSame(subcommands, result);
	}

	@Test
	void startsWith_filtersToMatchingPrefixOnly() {
		List<String> subcommands = new ArrayList<>(Arrays.asList("foo", "bar", "foobar"));
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(subcommands, "foo");
		assertEquals(Arrays.asList("foo", "foobar"), result);
	}

	@Test
	void startsWith_noMatches_returnsEmptyList() {
		List<String> subcommands = new ArrayList<>(Arrays.asList("foo", "bar"));
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(subcommands, "zzz");
		assertEquals(0, result.size());
	}

	@Test
	void startsWith_matchingCase_returnsNewListNotSameInstance() {
		List<String> subcommands = new ArrayList<>(Arrays.asList("foo", "bar"));
		List<String> result = com.chrismin13.additionsapi.utils.StringUtils.startsWith(subcommands, "foo");
		assertEquals(subcommands != result, true);
	}
}
