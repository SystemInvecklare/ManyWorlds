package com.mattiasselin.libs.multiworld.opt;

import com.mattiasselin.libs.multiworld.Variable;

public interface IVariableValues {
	<T> T get(Variable<T> variable);
	boolean containsKey(Variable<?> variable);
	<T> T put(Variable<T> variable, T value);
	void setTo(IVariableValues variableValues);
	boolean remove(Variable<?> variable);
}
