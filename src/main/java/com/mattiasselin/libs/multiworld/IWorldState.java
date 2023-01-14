package com.mattiasselin.libs.multiworld;

import com.mattiasselin.libs.multiworld.expression.Variable;

public interface IWorldState {
	<T> T getValue(Variable<T> variable);
}
