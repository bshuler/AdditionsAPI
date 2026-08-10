package us.fihgu.toolbox.resourcepack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ModelAxisTest {

	@Test
	void hasExactlyThreeConstantsInDeclarationOrder() {
		assertArrayEquals(new ModelAxis[] {ModelAxis.x, ModelAxis.y, ModelAxis.z}, ModelAxis.values());
	}

	@Test
	void valueOf_resolvesEachConstantByName() {
		assertSame(ModelAxis.x, ModelAxis.valueOf("x"));
		assertSame(ModelAxis.y, ModelAxis.valueOf("y"));
		assertSame(ModelAxis.z, ModelAxis.valueOf("z"));
	}

	@Test
	void name_matchesDeclaredIdentifier() {
		assertEquals("x", ModelAxis.x.name());
	}
}
