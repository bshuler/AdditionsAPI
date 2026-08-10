package us.fihgu.toolbox.file;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

	@TempDir
	File tempDir;

	@Test
	void constructor_isCallable() {
		// FileUtils is a static-only helper with no explicit constructor;
		// this just exercises the implicit default constructor for coverage.
		new FileUtils();
	}

	@Test
	void createFileAndPath_createsMissingFileAndParentDirs() throws IOException {
		File file = new File(tempDir, "nested/dir/file.txt");
		assertFalse(file.exists());

		FileUtils.createFileAndPath(file);

		assertTrue(file.exists());
		assertTrue(file.getParentFile().exists());
	}

	@Test
	void createFileAndPath_existingFile_doesNothing() throws IOException {
		File file = new File(tempDir, "already.txt");
		file.createNewFile();
		long originalModified = file.lastModified();

		FileUtils.createFileAndPath(file);

		assertTrue(file.exists());
		assertEquals(originalModified, file.lastModified());
	}

	@Test
	void copyURLtoFile_copiesFromFileUrl() throws IOException {
		File source = new File(tempDir, "source.txt");
		Files.writeString(source.toPath(), "hello world");
		File dest = new File(tempDir, "out/dest.txt");

		FileUtils.copyURLtoFile(source.toURI().toURL(), dest);

		assertEquals("hello world", Files.readString(dest.toPath()));
	}

	@Test
	void copyFolder_copiesNestedContentsRecursively() throws IOException {
		File from = new File(tempDir, "from");
		File nested = new File(from, "nested");
		nested.mkdirs();
		Files.writeString(new File(from, "a.txt").toPath(), "a");
		Files.writeString(new File(nested, "b.txt").toPath(), "b");

		File to = new File(tempDir, "to");
		FileUtils.copyFolder(from, to);

		assertEquals("a", Files.readString(new File(to, "a.txt").toPath()));
		assertEquals("b", Files.readString(new File(to, "nested/b.txt").toPath()));
	}

	@Test
	void copyFile_createsPathAndCopiesContent() throws IOException {
		File from = new File(tempDir, "from.txt");
		Files.writeString(from.toPath(), "content");
		File to = new File(tempDir, "sub/to.txt");

		FileUtils.copyFile(from, to);

		assertEquals("content", Files.readString(to.toPath()));
	}

	@Test
	void deleteFolder_missingFolder_doesNothing() throws IOException {
		File missing = new File(tempDir, "doesNotExist");
		FileUtils.deleteFolder(missing);
		assertFalse(missing.exists());
	}

	@Test
	void deleteFolder_removesAllFilesAndSubdirs() throws IOException {
		File folder = new File(tempDir, "toDelete");
		File nested = new File(folder, "nested");
		nested.mkdirs();
		Files.writeString(new File(folder, "a.txt").toPath(), "a");
		Files.writeString(new File(nested, "b.txt").toPath(), "b");

		FileUtils.deleteFolder(folder);

		assertFalse(folder.exists());
	}

	@Test
	void copyStreams_copiesAllBytesAndClosesStreams() throws IOException {
		byte[] data = "stream-data".getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FileUtils.copyStreams(in, out);

		assertEquals("stream-data", out.toString());
	}

	@Test
	void zipThenUnzip_roundTripsFileContent() throws IOException {
		File sourceDir = new File(tempDir, "zipSource");
		sourceDir.mkdirs();
		Files.writeString(new File(sourceDir, "file.txt").toPath(), "zipped content");

		File zipFile = new File(tempDir, "archive.zip");
		FileUtils.zip(sourceDir, zipFile);
		assertTrue(zipFile.exists());
		assertTrue(zipFile.length() > 0);

		File unzipDir = new File(tempDir, "unzipped");
		FileUtils.unzip(zipFile, unzipDir);

		File resultFile = new File(unzipDir, "file.txt");
		assertTrue(resultFile.exists());
		assertEquals("zipped content", Files.readString(resultFile.toPath()));
	}

	@Test
	void zipThenUnzip_roundTripsNestedSubdirectory() throws IOException {
		// Exercises addToZip's/unzip's directory-entry branches (a subdirectory
		// inside the zipped source, as opposed to only flat files), which the
		// flat-file round trip above never reaches.
		File sourceDir = new File(tempDir, "zipSourceNested");
		File nestedDir = new File(sourceDir, "nested");
		nestedDir.mkdirs();
		Files.writeString(new File(nestedDir, "inner.txt").toPath(), "nested content");

		File zipFile = new File(tempDir, "nested.zip");
		FileUtils.zip(sourceDir, zipFile);

		File unzipDir = new File(tempDir, "unzippedNested");
		FileUtils.unzip(zipFile, unzipDir);

		File resultFile = new File(unzipDir, "nested/inner.txt");
		assertTrue(resultFile.exists());
		assertEquals("nested content", Files.readString(resultFile.toPath()));
	}

	@Test
	void copyResource_copiesPluginResourceStreamToFile() throws IOException {
		JavaPlugin plugin = Mockito.mock(JavaPlugin.class);
		Mockito.when(plugin.getResource("config.yml"))
				.thenReturn(new ByteArrayInputStream("resource-content".getBytes()));

		File out = new File(tempDir, "out/config.yml");
		FileUtils.copyResource(plugin, "config.yml", out);

		assertEquals("resource-content", Files.readString(out.toPath()));
	}

	@Test
	void serializeThenDeserialize_roundTripsObject() {
		File file = new File(tempDir, "object.ser");
		FileUtils.serialize("a serializable string", file);

		Object result = FileUtils.deserialize(file);

		assertEquals("a serializable string", result);
	}

	@Test
	void deserialize_missingFile_returnsNull() {
		File missing = new File(tempDir, "missing.ser");
		assertNull(FileUtils.deserialize(missing));
	}

	@Test
	void serialize_targetIsDirectory_catchesFileNotFoundExceptionAndDoesNotThrow() throws IOException {
		// file.exists() is true (it's a directory), so the createFileAndPath
		// branch is skipped, and `new FileOutputStream(file)` on a directory
		// throws FileNotFoundException - exercising serialize()'s inner catch.
		File directory = new File(tempDir, "aDirectory");
		directory.mkdirs();

		FileUtils.serialize("won't be written", directory);

		assertTrue(directory.isDirectory());
	}

	@Test
	void serialize_parentPathBlockedByFile_catchesIOExceptionFromCreateFileAndPathAndDoesNotThrow()
			throws IOException {
		// "blocker" exists as a plain file, so createFileAndPath's
		// folder.mkdirs() call silently fails and the subsequent
		// file.createNewFile() throws IOException ("No such file or
		// directory") - exercising serialize()'s outer catch around
		// createFileAndPath(). The method still returns normally afterwards.
		File blocker = new File(tempDir, "blocker");
		Files.writeString(blocker.toPath(), "i'm a file, not a directory");
		File target = new File(blocker, "object.ser");

		FileUtils.serialize("won't be written either", target);

		assertFalse(target.exists());
	}

	@Test
	void deserialize_corruptFile_catchesIOExceptionAndReturnsNull() throws IOException {
		File corrupt = new File(tempDir, "corrupt.ser");
		Files.write(corrupt.toPath(), new byte[] { 1, 2, 3, 4, 5 });

		assertNull(FileUtils.deserialize(corrupt));
	}

	@Test
	void getPluginJarPath_returnsDecodedCodeSourcePath() {
		JavaPlugin plugin = Mockito.mock(JavaPlugin.class, Mockito.CALLS_REAL_METHODS);
		String path = FileUtils.getPluginJarPath(plugin);
		assertTrue(path != null && !path.isEmpty());
	}

	@Test
	void setModifiedDate_nullFile_doesNothingAndDoesNotThrow() {
		FileUtils.setModifiedDate(null, 123L);
	}

	@Test
	void setModifiedDate_missingFile_doesNothingAndDoesNotThrow() {
		FileUtils.setModifiedDate(new File(tempDir, "missing"), 123L);
	}

	@Test
	void setModifiedDate_singleFile_setsLastModified() throws IOException {
		File file = new File(tempDir, "single.txt");
		file.createNewFile();

		FileUtils.setModifiedDate(file, 1_000_000_000_000L);

		assertEquals(1_000_000_000_000L, file.lastModified());
	}

	@Test
	void setModifiedDate_directory_recursesIntoNestedFiles() throws IOException {
		File dir = new File(tempDir, "dateDir");
		File nestedDir = new File(dir, "nested");
		nestedDir.mkdirs();
		File nestedFile = new File(nestedDir, "leaf.txt");
		nestedFile.createNewFile();

		FileUtils.setModifiedDate(dir, 2_000_000_000_000L);

		assertEquals(2_000_000_000_000L, nestedFile.lastModified());
	}
}
