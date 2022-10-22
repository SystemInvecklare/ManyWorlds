package com.mattiasselin.libs.multiworld;

import java.util.function.BiConsumer;

public interface IStochastic<T> {
	void split(WorldTerm worldTerm, BiConsumer<WorldTerm, T> termHandler);
}
