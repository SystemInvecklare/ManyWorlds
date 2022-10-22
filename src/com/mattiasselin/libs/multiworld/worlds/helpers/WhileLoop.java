package com.mattiasselin.libs.multiworld.worlds.helpers;

import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.worlds.IWorlds;

public class WhileLoop implements BiConsumer<IWorlds, Boolean> {
	private final IStochasticExpression<Boolean> condition;
	private final IClause then;
	
	public WhileLoop(IStochasticExpression<Boolean> condition, IClause then) {
		this.condition = condition;
		this.then = then;
	}

	@Override
	public void accept(IWorlds worlds, Boolean conditionValue) {
		if(conditionValue) {
			then.execute(worlds);
			worlds.doStochastic(condition, this);
		}
	}
}
