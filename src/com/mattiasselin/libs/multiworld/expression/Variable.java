package com.mattiasselin.libs.multiworld.expression;

import com.mattiasselin.libs.multiworld.IWorldState;

public class Variable<T> extends Expression<T> {
	private final String name;
	
	public Variable() {
		this(String.valueOf(Math.random()));
	}
	
	public Variable(String name) {
		super(UseSpecial.USE_SPECIAL);
		this.name = name;
	}
	
	
	@Override
	protected T specialApply(IWorldState worldState) {
		return worldState.getValue(this);
	}

	public String getName() {
		return name;
	}
}
