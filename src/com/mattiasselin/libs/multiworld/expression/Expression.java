package com.mattiasselin.libs.multiworld.expression;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mattiasselin.libs.multiworld.IWorldState;
import com.mattiasselin.libs.multiworld.WorldTerm;

public class Expression<T> implements IStochasticExpression<T> {
	protected enum UseSpecial {
		USE_SPECIAL;
	}
	
	private final Function<IWorldState, T> evaluator;
	
	public Expression(Function<IWorldState, T> evaluator) {
		this.evaluator = evaluator;
	}
	
	protected Expression(UseSpecial useSpecial) {
		this.evaluator = new Function<IWorldState, T>() {
			@Override
			public T apply(IWorldState t) {
				return specialApply(t);
			}
		};
	}
	
	protected T specialApply(IWorldState worldState) {
		throw new UnsupportedOperationException("Only allowed for subtypes overriding this method");
	}

	@Override
	public void split(WorldTerm worldTerm, BiConsumer<WorldTerm, T> termHandler) {
		termHandler.accept(worldTerm, evaluator.apply(worldTerm.state));
	}
	
	public static <T> T evalExpression(IStochasticExpression<T> stochasticExpression, IWorldState worldState) {
		if(!(stochasticExpression instanceof Expression)) {
			throw new IllegalArgumentException();
		}
		Expression<T> expression = (Expression<T>) stochasticExpression;
		return expression.evaluator.apply(worldState);
	}
	
	
	public static <T,R> IStochasticExpression<R> func(IStochasticExpression<T> expression, Function<T, R> function) {
		if(expression instanceof Expression) {
			return new Expression<R>((worldState) -> function.apply(evalExpression(expression, worldState)));
		} else {
			return (worldTerm, termHandler) -> expression.split(worldTerm, (splitTerm, value) -> termHandler.accept(splitTerm, function.apply(value)));
		}
	}
	
	
	public static <A,B,R> IStochasticExpression<R> func(IStochasticExpression<A> a, IStochasticExpression<B> b, BiFunction<A, B, R> biFunction) {
		if(a instanceof Expression) {
			if(b instanceof Expression) {
				return new Expression<>(worldState -> biFunction.apply(evalExpression(a, worldState), evalExpression(b, worldState)));
			} else {
				return (worldTerm, termHandler) -> b.split(worldTerm, (splitTerm, bValue) -> termHandler.accept(splitTerm, biFunction.apply(evalExpression(a, splitTerm.state), bValue)));
			}
		} else if(b instanceof Expression) {
			return (worldTerm, termHandler) -> a.split(worldTerm, (splitTerm, aValue) -> termHandler.accept(splitTerm, biFunction.apply(aValue, evalExpression(b, splitTerm.state))));
		} else {
			return (worldTerm, termHandler) -> a.split(worldTerm, (splitOnce, aValue) -> b.split(splitOnce, (splitTwice, bValue) -> termHandler.accept(splitTwice, biFunction.apply(aValue, bValue))));
		}
	}
	
	public static <T,R> IStochasticExpression<R> funcToStochastic(IStochasticExpression<T> expression, Function<T, IStochasticExpression<R>> function) {
		if(expression instanceof Expression) {
			return (worldTerm, termHandler) -> function.apply(evalExpression(expression, worldTerm.state)).split(worldTerm, termHandler);
		} else {
			return (worldTerm, termHandler) -> expression.split(worldTerm, (splitTerm, value) -> function.apply(value).split(splitTerm, termHandler));
		}
	} 
	
	public static <T,R> IStochasticExpression<R> funcToExpression(IStochasticExpression<T> expression, Function<T, Expression<R>> function) {
		if(expression instanceof Expression) {
			return new Expression<R>(worldState -> function.apply(evalExpression(expression, worldState)).evaluator.apply(worldState));
		} else {
			return (worldTerm, termHandler) -> expression.split(worldTerm, (splitTerm, value) -> termHandler.accept(splitTerm, function.apply(value).evaluator.apply(splitTerm.state)));
		}
	}

	public static IStochasticExpression<Integer> minusOne(IStochasticExpression<Integer> expression) {
		return func(expression, Constant.INT_ONE, (a,b) -> a - b);
	}
	
	public static IStochasticExpression<Integer> plusOne(IStochasticExpression<Integer> expression) {
		return func(expression, Constant.INT_ONE, (a,b) -> a + b);
	}
	
	public static final class Boolean_ {
		private Boolean_() {}
		
		public static final IStochasticExpression<Boolean> and(IStochasticExpression<Boolean> a, IStochasticExpression<Boolean> b) {
			return func(a, b, (aa, bb) -> aa && bb);
		}
		
		public static final IStochasticExpression<Boolean> or(IStochasticExpression<Boolean> a, IStochasticExpression<Boolean> b) {
			return func(a, b, (aa, bb) -> aa || bb);
		}
		
		public static final IStochasticExpression<Boolean> not(IStochasticExpression<Boolean> expression) {
			return func(expression, val -> !val);
		}
	}
	
	public static final class Int {
		private Int() {}
		
		public static IStochasticExpression<Integer> plus(IStochasticExpression<Integer> a, IStochasticExpression<Integer> b) {
			return func(a, b, (aa, bb) -> aa + bb);
		}
		
		public static IStochasticExpression<Integer> minus(IStochasticExpression<Integer> a, IStochasticExpression<Integer> b) {
			return func(a, b, (aa, bb) -> aa - bb);
		}
		
		public static IStochasticExpression<Integer> mult(IStochasticExpression<Integer> a, IStochasticExpression<Integer> b) {
			return func(a, b, (aa, bb) -> aa*bb);
		}
		
		public static IStochasticExpression<Boolean> greaterThan(IStochasticExpression<Integer> expression, int constant) {
			return greaterThan(expression, new Constant<Integer>(constant));
		}
		
		public static IStochasticExpression<Boolean> greaterThan(IStochasticExpression<Integer> expression, IStochasticExpression<Integer> other) {
			return func(expression, other, (a,b) -> a > b);
		}
		
		public static IStochasticExpression<Boolean> greaterThanOrEqual(IStochasticExpression<Integer> expression, int constant) {
			return greaterThanOrEqual(expression, new Constant<>(constant));
		}
		
		public static IStochasticExpression<Boolean> greaterThanOrEqual(IStochasticExpression<Integer> expression, IStochasticExpression<Integer> other) {
			return func(expression, other, (a,b) -> a >= b);
		}
		
		public static IStochasticExpression<Boolean> lessThan(IStochasticExpression<Integer> expression, int constant) {
			return lessThan(expression, new Constant<>(constant));
		}
		
		public static IStochasticExpression<Boolean> lessThan(IStochasticExpression<Integer> expression, IStochasticExpression<Integer> other) {
			return func(expression, other, (a,b) -> a < b);
		}
		
		public static IStochasticExpression<Boolean> lessThanOrEqual(IStochasticExpression<Integer> expression, int constant) {
			return lessThanOrEqual(expression, new Constant<>(constant));
		}
		
		public static IStochasticExpression<Boolean> lessThanOrEqual(IStochasticExpression<Integer> expression, IStochasticExpression<Integer> other) {
			return func(expression, other, (a,b) -> a <= b);
		}
		
		public static IStochasticExpression<Boolean> equalTo(IStochasticExpression<Integer> expression, int constant) {
			return equalTo(expression, new Constant<>(constant));
		}
		
		public static IStochasticExpression<Boolean> equalTo(IStochasticExpression<Integer> expression, IStochasticExpression<Integer> other) {
			return func(expression, other, (a,b) -> a == b);
		}
		
		public static IStochasticExpression<Boolean> notEqualTo(IStochasticExpression<Integer> expression, int constant) {
			return notEqualTo(expression, new Constant<>(constant));
		}
		
		public static IStochasticExpression<Boolean> notEqualTo(IStochasticExpression<Integer> expression, IStochasticExpression<Integer> other) {
			return func(expression, other, (a,b) -> a != b);
		}
	}
	
}
