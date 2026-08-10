package com.chrismin13.additionsapi.utils;

import com.chrismin13.additionsapi.items.CustomItemStack;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;

class BowStackContainerTest {

	@Test
	void constructor_setsBowStackAndOwner() {
		CustomItemStack stack = Mockito.mock(CustomItemStack.class);
		LivingEntity owner = Mockito.mock(LivingEntity.class);
		BowStackContainer container = new BowStackContainer(stack, owner);
		assertSame(stack, container.getBowStack());
		assertSame(owner, container.getOwner());
	}

	@Test
	void setBowStack_isFluentAndRoundTrips() {
		CustomItemStack first = Mockito.mock(CustomItemStack.class);
		CustomItemStack second = Mockito.mock(CustomItemStack.class);
		LivingEntity owner = Mockito.mock(LivingEntity.class);
		BowStackContainer container = new BowStackContainer(first, owner);

		BowStackContainer same = container.setBowStack(second);

		assertSame(container, same);
		assertSame(second, container.getBowStack());
	}

	@Test
	void setOwner_isFluentAndRoundTrips() {
		CustomItemStack stack = Mockito.mock(CustomItemStack.class);
		LivingEntity firstOwner = Mockito.mock(LivingEntity.class);
		LivingEntity secondOwner = Mockito.mock(LivingEntity.class);
		BowStackContainer container = new BowStackContainer(stack, firstOwner);

		BowStackContainer same = container.setOwner(secondOwner);

		assertSame(container, same);
		assertSame(secondOwner, container.getOwner());
	}
}
