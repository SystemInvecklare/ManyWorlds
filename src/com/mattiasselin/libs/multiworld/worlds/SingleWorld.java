package com.mattiasselin.libs.multiworld.worlds;

import java.util.Comparator;
import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.IWorldState;
import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.WorldState;
import com.mattiasselin.libs.multiworld.WorldTerm;
import com.mattiasselin.libs.multiworld.expression.Expression;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.worlds.helpers.IClause;
import com.mattiasselin.libs.multiworld.worlds.helpers.WhileLoop;

public class SingleWorld implements IWorlds {
	private final WorldState state = new WorldState();

	@Override
	public <T> Variable<T> set(Variable<T> variable, IStochasticExpression<T> expression) {
		if(expression instanceof Expression) {
			state.setValue(variable, Expression.evalExpression(expression, state));
		} else {
			doStochastic(expression, (world, outcome) -> {
				world.setConst(variable, outcome);
			});
		}
		return variable;
	}

	@Override
	public void forget(Variable<?> variable) {
		state.forget(variable);
	}

	@Override
	public <T> void doStochastic(IStochasticExpression<T> stochastic, BiConsumer<IWorlds, T> outcomeHandler) {
		if(stochastic instanceof Expression) {
			outcomeHandler.accept(this, Expression.evalExpression(stochastic, state));
		} else {
			final float[] percent = new float[] {(float) (Math.random()*100)};
			stochastic.split(new WorldTerm(state, Probability.CERTAIN), (subState, val) -> {
				if(percent[0] >= 0) {
					percent[0] -= subState.probability.getPercentChance();
					if(percent[0] < 0) {
						outcomeHandler.accept(this, val);
					}
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T evaluate(IStochasticExpression<T> expression) {
		if(expression instanceof Variable) {
			return state.getValue((Variable<T>) expression);
		} else {
			final Object[] result = new Object[] {null}; 
			doStochastic(expression, (worlds, value) -> {
				result[0] = value;
			});
			return (T) result[0];
		}
	}
	
	public IWorldState asWorldState() {
		return state;
	}

	@Override
	public void whileLoop(IStochasticExpression<Boolean> condition, IClause then) {
		doStochastic(condition, new WhileLoop(condition, then));
	}
	
	@Override
	public <T> void splitOn(IStochasticExpression<T> value, BiConsumer<IWorlds, T> outcomeHandler, Comparator<IWorlds> ignoredOrder) {
		doStochastic(value, outcomeHandler);
	}
	
	@Override
	public Probability getTotalProbability() {
		return Probability.CERTAIN;
	}
	
	public static <T> T collapseStochastic(IStochasticExpression<T> stochastic, WorldState state) {
		if(stochastic instanceof Expression) {
			return Expression.evalExpression(stochastic, state);
		} else {
			final float[] percent = new float[] {(float) (Math.random()*100)};
			final Object[] finalResult = new Object[] {null};
			stochastic.split(new WorldTerm(state, Probability.CERTAIN), (subState, val) -> {
				if(percent[0] >= 0) {
					percent[0] -= subState.probability.getPercentChance();
					if(percent[0] < 0) {
						finalResult[0] = val;
					}
				}
			});
			return (T) finalResult[0];
		}
	}
}
