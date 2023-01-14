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
	
	public static <T> IStochasticExpression<Boolean> notNull(IStochasticExpression<T> expression) {
		return func(expression, a -> a != null);
	}
	
	public static <T> IStochasticExpression<Boolean> isNull(IStochasticExpression<T> expression) {
		return func(expression, a -> a == null);
	}

	public static <Super,Sub extends Super> IStochasticExpression<Super> cast(IStochasticExpression<Sub> expression) {
		return (worldTerm, termHandler) -> expression.split(worldTerm, termHandler::accept);
	}
	
	public static <T> IStochasticExpression<T> conditional(IStochasticExpression<Boolean> condition, T ifTrue, T ifFalse) {
		return conditional(condition, new Constant<>(ifTrue), new Constant<>(ifFalse));
	}
	
	public static <T> IStochasticExpression<T> conditional(IStochasticExpression<Boolean> condition, IStochasticExpression<? extends T> ifTrue, IStochasticExpression<? extends T> ifFalse) {
		if(condition instanceof Expression && ifTrue instanceof Expression && ifFalse instanceof Expression) {
			return new Expression<T>(worldState -> evalExpression(condition, worldState) ? evalExpression(ifTrue, worldState) : evalExpression(ifFalse, worldState));
		} else {
			return (worldTerm, termHandler) -> {
				condition.split(worldTerm, (conditionSplitWorld, conditionValue) -> {
					IStochasticExpression<T> result = (conditionValue ? cast(ifTrue) : cast(ifFalse));
					result.split(conditionSplitWorld, termHandler);
				});
			};
		}
	}
	
	public static final class Boolean_ {
		private Boolean_() {}
		
		public static final IStochasticExpression<Boolean> and(IStochasticExpression<Boolean> a, IStochasticExpression<Boolean> b) {
			//Should be faster since we only calculate second if first is true. Note: Could be further improved by checking if a or b Expression and evaluating expression before any splitting
			return (worldTerm, termHandler) -> {
				a.split(worldTerm, (splitWorld, aVal) -> {
					if(aVal) {
						b.split(splitWorld, termHandler);
					} else {
						termHandler.accept(splitWorld, false);
					}
				});
			};
//			return func(a, b, (aa, bb) -> aa && bb);
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
		
		public static IStochasticExpression<Integer> plus(IStochasticExpression<Integer> a, int b) {
			return func(a, aa -> aa + b);
		}
		
		public static IStochasticExpression<Integer> plus(IStochasticExpression<Integer> a, IStochasticExpression<Integer> b) {
			return func(a, b, (aa, bb) -> aa + bb);
		}
		
		public static IStochasticExpression<Integer> minus(IStochasticExpression<Integer> a, int b) {
			return func(a, aa -> aa - b);
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
