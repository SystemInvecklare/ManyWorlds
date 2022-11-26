package com.mattiasselin.libs.multiworld.worlds.helpers;

import java.util.ArrayList;
import java.util.List;

import com.mattiasselin.libs.multiworld.expression.Constant;
import com.mattiasselin.libs.multiworld.expression.Expression;
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
	
	public <T> Variable<List<T>> listVar(Class<T> entryType) {
		Variable<List<T>> variable = new Variable<>();
		variables.add(variable);
		return variable;
	}
	
	public <T> Variable<List<T>> listVar(Class<T> entryType, String name) {
		Variable<List<T>> variable = new Variable<>(name);
		variables.add(variable);
		return variable;
	}
	
	public <T> Variable<List<T>> listVar(Class<T> entryType, IStochasticExpression<? extends List<T>> initialValue) {
		Variable<List<T>> variable = listVar(entryType);
		worlds.set(variable, Expression.cast(initialValue));
		return variable;
	}
	
	public <T> Variable<List<T>> listVar(Class<T> entryType, List<T> initialValue) {
		return listVar(entryType, new Constant<>(initialValue));
	}
	
	public <T> Variable<List<T>> listVar(Class<T> entryType, String name, IStochasticExpression<? extends List<T>> initialValue) {
		Variable<List<T>> variable = listVar(entryType, name);
		worlds.set(variable, Expression.cast(initialValue));
		return variable;
	}
	
	public <T> Variable<List<T>> listVar(Class<T> entryType, String name, List<T> initialValue) {
		return listVar(entryType, name, new Constant<>(initialValue));
	}
	
	public void forgetScope() {
		for(Variable<?> var : variables) {
			worlds.forget(var);
		}
		variables.clear();
	}
}
