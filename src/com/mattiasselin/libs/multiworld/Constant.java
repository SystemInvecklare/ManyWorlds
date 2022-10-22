package com.mattiasselin.libs.multiworld;

import java.util.Objects;

public class Constant<T> implements IExpression<T> {
	public static final IExpression<Integer> INT_ZERO = new Constant<Integer>(0);
	public static final IExpression<Integer> INT_ONE = new Constant<Integer>(1);
	
	private final T value;
	
	public Constant(T value) {
		this.value = value;
	}

	@Override
	public T eval(IWorldState worldState) {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Constant) {
			return Objects.equals(this.value, ((Constant<?>) obj).value);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
