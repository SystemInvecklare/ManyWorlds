package com.mattiasselin.libs.multiworld;

import java.util.function.BiConsumer;

public class SingleWorld implements IWorlds {
	private final WorldState state = new WorldState();

	@Override
	public <T> Variable<T> set(Variable<T> variable, IExpression<T> expression) {
		state.setValue(variable, expression.eval(state));
		return variable;
	}

	@Override
	public <T> Variable<T> set(Variable<T> variable, IStochastic<T> expression) {
		doStochastic(expression, (world, outcome) -> {
			world.set(variable, new Constant<T>(outcome));
		});
		return variable;
	}

	@Override
	public void ifTrue(IExpression<Boolean> condition, IClause then) {
		if(condition.eval(state)) {
			then.execute(this);
		}
	}

	@Override
	public void ifTrue(IExpression<Boolean> condition, IClause thenClause, IClause elseClause) {
		if(condition.eval(state)) {
			thenClause.execute(this);
		} else {
			elseClause.execute(this);
		}
	}
	
	@Override
	public void whileLoop(IExpression<Boolean> condition, IClause then) {
		while(condition.eval(state)) {
			then.execute(this);
		}
	}

	@Override
	public void forget(Variable<?> variable) {
		state.forget(variable);
	}

	@Override
	public <T> void doStochastic(IStochastic<T> stochastic, BiConsumer<IWorlds, T> outcomeHandler) {
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

	@Override
	public <T> void doExpression(IExpression<T> expression, BiConsumer<IWorlds, T> outcomeHandler) {
		outcomeHandler.accept(this, expression.eval(state));
	}
}
