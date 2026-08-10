package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ModelFaceTest {

	@Test
	void hasExactlySixConstantsInDeclarationOrder() {
		assertArrayEquals(new ModelFace[] {
				ModelFace.down, ModelFace.up, ModelFace.north, ModelFace.south, ModelFace.west, ModelFace.east
		}, ModelFace.values());
	}

	@Test
	void valueOf_resolvesEachConstantByName() {
		assertSame(ModelFace.down, ModelFace.valueOf("down"));
		assertSame(ModelFace.up, ModelFace.valueOf("up"));
		assertSame(ModelFace.north, ModelFace.valueOf("north"));
		assertSame(ModelFace.south, ModelFace.valueOf("south"));
		assertSame(ModelFace.west, ModelFace.valueOf("west"));
		assertSame(ModelFace.east, ModelFace.valueOf("east"));
	}
}
