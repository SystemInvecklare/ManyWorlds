package com.mattiasselin.libs.multiworld.worlds.helpers;

import com.mattiasselin.libs.multiworld.expression.Variable;

public interface IVariableValues {
	<T> T get(Variable<T> variable);
	boolean containsKey(Variable<?> variable);
	<T> T put(Variable<T> variable, T value);
	void setTo(IVariableValues variableValues);
	boolean remove(Variable<?> variable);
}
