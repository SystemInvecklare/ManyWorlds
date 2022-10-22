package com.mattiasselin.libs.multiworld.worlds.helpers;

import java.util.ArrayList;
import java.util.List;

import com.mattiasselin.libs.multiworld.expression.Constant;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.worlds.IWorlds;

public class Scope {
	private final IWorlds worlds;
	private final List<Variable<?>> variables = new ArrayList<>();

	public Scope(IWorlds worlds) {
		this.worlds = worlds;
	}
	
	public <T> Variable<T> var(Class<T> type) {
		Variable<T> variable = new Variable<>();
		variables.add(variable);
		return variable;
	}
	
	public <T> Variable<T> var(Class<T> type, IStochasticExpression<T> initialValue) {
		Variable<T> variable = var(type);
		worlds.set(variable, initialValue);
		return variable;
	}
	
	public <T> Variable<T> var(Class<T> type, T initialValue) {
		return var(type, new Constant<T>(initialValue));
	}
	
	public <T> Variable<T> var(Class<T> type, String name) {
		Variable<T> variable = new Variable<>(name);
		variables.add(variable);
		return variable;
	}
	
	public <T> Variable<T> var(Class<T> type, String name, IStochasticExpression<T> initialValue) {
		Variable<T> variable = var(type, name);
		worlds.set(variable, initialValue);
		return variable;
	}
	
	public <T> Variable<T> var(Class<T> type, String name, T initialValue) {
		return var(type, name, new Constant<T>(initialValue));
	}
	
	public void forgetScope() {
		for(Variable<?> var : variables) {
			worlds.forget(var);
		}
		variables.clear();
	}
}
