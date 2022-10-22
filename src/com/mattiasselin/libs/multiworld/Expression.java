package com.mattiasselin.libs.multiworld;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Expression {
//	public static final BiFunction<Integer, Integer, Integer> INT_ADD = (r1,r2) -> r1+r2;
	public static final BiFunction<Boolean, Boolean, Boolean> AND = (b1,b2) -> b1 && b2;
	public static final BiFunction<Boolean, Boolean, Boolean> OR = (b1,b2) -> b1 || b2;
	public static final Function<Boolean, Boolean> NOT = (b) -> !b;

	private Expression() {}
	
	
	public static <A,B,R> IExpression<R> bifunc(IExpression<A> a, IExpression<B> b, BiFunction<A, B, R> biFunction) {
		return worldState -> biFunction.apply(a.eval(worldState), b.eval(worldState));
	}
	
	public static <T,R> IExpression<R> func(IExpression<T> expression, Function<T, R> function) {
		return worldState -> function.apply(expression.eval(worldState));
	}
	
	public static <T,R> IExpression<R> funcE(IExpression<T> expression, Function<T, IExpression<R>> function) {
		return worldState -> function.apply(expression.eval(worldState)).eval(worldState);
	}


	public static IExpression<Integer> minusOne(IExpression<Integer> expression) {
		return bifunc(expression, Constant.INT_ONE, (a,b) -> a - b);
	}
	
	public static IExpression<Integer> plusOne(IExpression<Integer> expression) {
		return bifunc(expression, Constant.INT_ONE, (a,b) -> a + b);
	}
	
	public static IExpression<Integer> plus_int(IExpression<Integer> a, IExpression<Integer> b) {
		return bifunc(a, b, (aa, bb) -> aa + bb);
	}
	
//	public static IExpression<Integer> addi(IExpression<Integer> a, IExpression<Integer> b) {
//		return bifunc(a, b, (ai,bi) -> ai + bi);
//	}
}
