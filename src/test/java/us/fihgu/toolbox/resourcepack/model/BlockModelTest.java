package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link BlockModel} adds exactly one field ({@code ambientocclusion}) beyond
 * {@link Model}, with no getter/setter - only direct (protected) field
 * access, reachable from this test because it lives in the same package.
 */
class BlockModelTest {

	@Test
	void ambientocclusionDefaultsToNull() {
		BlockModel model = new BlockModel();
		assertNull(model.ambientocclusion);
	}

	@Test
	void ambientocclusionCanBeSetDirectly() {
		BlockModel model = new BlockModel();
		model.ambientocclusion = Boolean.FALSE;
		assertEquals(Boolean.FALSE, model.ambientocclusion);
	}

	@Test
	void inheritsModelFieldsViaPublicAccessors() {
		BlockModel model = new BlockModel();
		model.setParent("block/cube_all");
		model.putTexture("all", "block/stone");
		assertEquals("block/cube_all", model.getParent());
		assertEquals("block/stone", model.getTextures().get("all"));
	}
}
