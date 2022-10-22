package com.mattiasselin.libs.multiworld;

public class Variable<T> implements IExpression<T> {
	private final String name;
	
	public Variable() {
		this(String.valueOf(Math.random()));
	}
	
	public Variable(String name) {
		this.name = name;
	}

	@Override
	public T eval(IWorldState worldState) {
		return worldState.getValue(this);
	}
	
	public String getName() {
		return name;
	}
}
