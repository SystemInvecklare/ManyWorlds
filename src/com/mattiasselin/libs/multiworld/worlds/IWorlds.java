package com.mattiasselin.libs.multiworld.worlds;

import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.expression.Constant;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.worlds.helpers.IClause;

public interface IWorlds {
	<T> Variable<T> set(Variable<T> variable, IStochasticExpression<T> expression);
	default <T> Variable<T> setConst(Variable<T> variable, T constant) {
		return set(variable, new Constant<T>(constant));
	}
	default void ifTrue(IStochasticExpression<Boolean> condition, IClause then) {
		doStochastic(condition, (splitWorlds, conditionValue) -> {
			if(conditionValue) {
				then.execute(splitWorlds);
			}
		});
	}
	default void ifTrue(IStochasticExpression<Boolean> condition, IClause thenClause, IClause elseClause) {
		doStochastic(condition, (splitWorlds, conditionValue) -> {
			if(conditionValue) {
				thenClause.execute(splitWorlds);
			} else {
				elseClause.execute(splitWorlds);
			}
		});
	}
	void forget(Variable<?> variable);
	
	<T> void doStochastic(IStochasticExpression<T> stochastic, BiConsumer<IWorlds, T> outcomeHandler);
	
	void whileLoop(IStochasticExpression<Boolean> condition, IClause then);
}
