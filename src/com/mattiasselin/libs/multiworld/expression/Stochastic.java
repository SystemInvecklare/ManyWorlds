package com.mattiasselin.libs.multiworld.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mattiasselin.libs.multiworld.WorldTerm;

public abstract class Stochastic<T> implements IStochasticExpression<T> {
	private final int[] weights;
	
	private Stochastic(int[] weights) {
		this.weights = weights;
	}
	
	public static <T> Stochastic<T> of(int[] weights, T[] values) {
		return new Stochastic.WithArray<>(weights, values);
	}
	
	private static class WithArray<T> extends Stochastic<T> {
		private final T[] values;
		
		private WithArray(int[] weights, T[] values) {
			super(weights);
			this.values = values;
			if(weights.length != values.length) {
				throw new IllegalArgumentException("Differing lengths: "+weights.length+" vs "+values.length);
			}
		}

		@Override
		protected Iterator<T> newValuesIterator() {
			class ValuesIterator implements Iterator<T> {
				private int i = 0;
				
				@Override
				public T next() {
					return values[i++];
				}

				@Override
				public boolean hasNext() {
					return i < values.length;
				}
			}
			return new ValuesIterator();
		}
	}
	
	private static class WithList<T> extends Stochastic<T> {
		private final List<T> values;
		
		private WithList(int[] weights, List<T> values) {
			super(weights);
			this.values = values;
			if(weights.length != values.size()) {
				throw new IllegalArgumentException("Differing lengths: "+weights.length+" vs "+values.size());
			}
		}
		
		@Override
		protected Iterator<T> newValuesIterator() {
			return values.iterator();
		}
	}
	
	protected abstract Iterator<T> newValuesIterator();
	

	@Override
	public void split(WorldTerm worldTerm, BiConsumer<WorldTerm, T> termHandler) {
		Iterator<T> valuesIterator = newValuesIterator();
		worldTerm.split((newWorld) -> {
			termHandler.accept(newWorld, valuesIterator.next());
		}, weights);
	}


	@SuppressWarnings("unchecked")
	public static <T> Stochastic<T> even(T ... values) {
		return new Stochastic.WithArray<>(evenWeights(values.length), values);
	}
	
	public static <T> Stochastic<T> evenList(List<T> values) {
		return new Stochastic.WithList<>(evenWeights(values.size()), values);
	}
	
	private static int[] evenWeights(int length) {
		int[] weights = new int[length];
		for(int i = 0; i < weights.length; ++i) {
			weights[i] = 1;
		}
		return weights;
	}
	
	public static Stochastic<Integer> intRange(int from, int to) {
		final int length = to-from+1;
		Integer[] values = new Integer[length];
		int[] weights = new int[length];
		for(int i = 0; i < length; ++i) {
			values[i] = from + i;
			weights[i] = 1;
		}
		return new Stochastic.WithArray<Integer>(weights, values);
	}
	
	private static <T> Stochastic<T> fromMap(Map<T, Integer> valueToWeight) {
		int[] weights = new int[valueToWeight.size()];
		List<T> values = new ArrayList<>();
		int i = 0;
		for(Map.Entry<T, Integer> entry : valueToWeight.entrySet()) {
			values.add(entry.getKey());
			weights[i++] = entry.getValue();
		}
		return new Stochastic.WithList<T>(weights, values);
	}


	public static <T,R> Stochastic<R> precompute(Stochastic<T> stochastic, Function<T, R> function) {
		Map<R, Integer> valueToWeight = new HashMap<>();
		Iterator<T> iterator = stochastic.newValuesIterator();
		for(int i = 0; i < stochastic.weights.length; ++i) {
			R result = function.apply(iterator.next());
			Integer weight = valueToWeight.get(result);
			if(weight == null) {
				weight = 0;
			}
			valueToWeight.put(result, weight + stochastic.weights[i]);
		}
		return fromMap(valueToWeight);
	}
	
	public static <S, T, R> Stochastic<R> precompute(Stochastic<S> a, Stochastic<T> b, BiFunction<S, T, R> biFunction) {
		Map<R, Integer> valueToWeight = new HashMap<>();
		Iterator<S> aIterator = a.newValuesIterator();
		for(int i = 0; i < a.weights.length; ++i) {
			final S aValue = aIterator.next();
			Iterator<T> bIterator = b.newValuesIterator();
			for(int j = 0; j < b.weights.length; ++j) {
				R result = biFunction.apply(aValue, bIterator.next());
				Integer weight = valueToWeight.get(result);
				if(weight == null) {
					weight = 0;
				}
				valueToWeight.put(result, weight + a.weights[i]*b.weights[j]);
			}
		}
		return fromMap(valueToWeight);
	}
}
