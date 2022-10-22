package com.mattiasselin.libs.multiworld;

public interface IWorldState {
	<T> T getValue(Variable<T> variable);
}
