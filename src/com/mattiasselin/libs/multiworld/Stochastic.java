package com.mattiasselin.libs.multiworld;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Stochastic<T> implements IStochastic<T> {
	private final int[] weights;
	private final T[] values;
	
	public Stochastic(int[] weights, T[] values) {
		this.weights = weights;
		this.values = values;
		if(weights.length != values.length) {
			throw new IllegalArgumentException("Differing lengths: "+weights.length+" vs "+values.length);
		}
	}
	

	@Override
	public void split(WorldTerm worldTerm, BiConsumer<WorldTerm, T> termHandler) {
		class ValuesIterator {
			int i = 0;
			public T next() {
				return values[i++];
			}
		}
		ValuesIterator valuesIterator = new ValuesIterator();
		worldTerm.split((newWorld) -> {
			termHandler.accept(newWorld, valuesIterator.next());
		}, weights);
	}


	@SuppressWarnings("unchecked")
	public static <T> Stochastic<T> even(T ... values) {
		int[] weights = new int[values.length];
		for(int i = 0; i < weights.length; ++i) {
			weights[i] = 1;
		}
		return new Stochastic<>(weights, values);
	}

	public static <T,R> IStochastic<R> func(IExpression<T> expression, Function<T, IStochastic<R>> function) {
		return (worldTerm, consumer) -> function.apply(expression.eval(worldTerm.state)).split(worldTerm, consumer);
	}

	public static <T,R> IStochastic<R> func(IStochastic<T> stochastic, Function<T, R> function) {
		return (worldTerm, consumer) -> stochastic.split(worldTerm, (termy, outcome) -> consumer.accept(termy, function.apply(outcome)));
	}
	
	public static <T,S,R> IStochastic<R> func(IStochastic<T> stochastic, IExpression<S> expression, BiFunction<T, S, R> bifunction) {
		return (worldTerm, consumer) -> stochastic.split(worldTerm, (termy, outcome) -> consumer.accept(termy, bifunction.apply(outcome, expression.eval(termy.state))));
	}

}
