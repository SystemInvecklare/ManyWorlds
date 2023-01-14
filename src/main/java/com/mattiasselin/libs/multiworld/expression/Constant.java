package com.mattiasselin.libs.multiworld.expression;

import java.util.Objects;

public class Constant<T> extends Expression<T> {
	public static final Expression<Integer> INT_ZERO = new Constant<Integer>(0);
	public static final Expression<Integer> INT_ONE = new Constant<Integer>(1);
	
	private final T value;
	
	public Constant(T value) {
		super(worldState -> value);
		this.value = value;
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
