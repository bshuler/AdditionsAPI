package us.fihgu.toolbox.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilsTest {

	@TempDir
	File tempDir;

	@Test
	void constructor_isCallable() {
		// JsonUtils is a static-only helper with no explicit constructor;
		// this just exercises the implicit default constructor for coverage.
		new JsonUtils();
	}

	static class Sample {
		String name;
		int value;

		Sample() {
		}

		Sample(String name, int value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Sample))
				return false;
			Sample other = (Sample) o;
			return value == other.value && (name == null ? other.name == null : name.equals(other.name));
		}
	}

	@Test
	void toJson_producesPrettyPrintedJsonString() {
		String json = JsonUtils.toJson(new Sample("foo", 42));
		assertTrue(json.contains("foo"));
		assertTrue(json.contains("42"));
	}

	@Test
	void fromJson_parsesBackToEquivalentObject() {
		Sample original = new Sample("bar", 7);
		String json = JsonUtils.toJson(original);

		Sample parsed = JsonUtils.fromJson(json, Sample.class);

		assertEquals(original, parsed);
	}

	@Test
	void fromFile_missingFile_returnsNull() {
		File missing = new File(tempDir, "missing.json");
		assertNull(JsonUtils.fromFile(missing, Sample.class));
	}

	@Test
	void toFileThenFromFile_roundTripsObject() {
		File file = new File(tempDir, "nested/sample.json");
		Sample original = new Sample("baz", 99);

		JsonUtils.toFile(file, original);
		Sample result = JsonUtils.fromFile(file, Sample.class);

		assertEquals(original, result);
	}

	@Test
	void toFile_overwritesExistingFileContent() throws Exception {
		File file = new File(tempDir, "overwrite.json");
		Files.writeString(file.toPath(), "stale content that should be discarded");

		JsonUtils.toFile(file, new Sample("fresh", 1));
		Sample result = JsonUtils.fromFile(file, Sample.class);

		assertEquals("fresh", result.name);
		assertEquals(1, result.value);
	}

	@Test
	void fromFile_targetIsDirectory_catchesFileNotFoundExceptionAndReturnsNull() {
		// file.exists() is true (it's a directory), so `new
		// FileInputStream(file)` throws FileNotFoundException ("Is a
		// directory") - exercising fromFile()'s documented "should never
		// happen" catch.
		File directory = new File(tempDir, "aDirectory");
		directory.mkdirs();

		assertNull(JsonUtils.fromFile(directory, Sample.class));
	}

	@Test
	void toFile_targetIsDirectory_catchesFileNotFoundExceptionAndDoesNotThrow() {
		// file.exists() is true (it's a directory), so the createFileAndPath
		// branch is skipped, and `new FileOutputStream(file, false)` on a
		// directory throws FileNotFoundException - exercising toFile()'s
		// catch block.
		File directory = new File(tempDir, "anotherDirectory");
		directory.mkdirs();

		JsonUtils.toFile(directory, new Sample("ignored", 0));

		assertTrue(directory.isDirectory());
	}

	@Test
	void toFile_parentPathBlockedByFile_catchesIOExceptionAndDoesNotThrow() throws Exception {
		// "blocker" exists as a plain file, so createFileAndPath's
		// folder.mkdirs() call silently fails and the subsequent
		// file.createNewFile() throws IOException ("No such file or
		// directory") - exercising toFile()'s catch block around
		// FileUtils.createFileAndPath(file).
		File blocker = new File(tempDir, "blocker");
		Files.writeString(blocker.toPath(), "i'm a file, not a directory");
		File target = new File(blocker, "sample.json");

		JsonUtils.toFile(target, new Sample("ignored", 0));

		assertTrue(!target.exists());
	}
}
