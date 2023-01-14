package com.mattiasselin.libs.multiworld.expression;

import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.WorldTerm;

public interface IStochasticExpression<T> {
	void split(WorldTerm worldTerm, BiConsumer<WorldTerm, T> termHandler);
}
