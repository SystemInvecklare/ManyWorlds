package com.mattiasselin.libs.multiworld;

import java.util.function.BiConsumer;

public interface IWorlds {
	<T> Variable<T> set(Variable<T> variable, IExpression<T> expression);
	<T> Variable<T> set(Variable<T> variable, IStochastic<T> expression);
	default <T> Variable<T> setConst(Variable<T> variable, T constant) {
		return set(variable, new Constant<T>(constant));
	}
	void ifTrue(IExpression<Boolean> condition, IClause then);
	void ifTrue(IExpression<Boolean> condition, IClause thenClause, IClause elseClause);
	void forget(Variable<?> variable);
	
	//TODO Experimental!
	<T> void doStochastic(IStochastic<T> stochastic, BiConsumer<IWorlds, T> outcomeHandler);
	<T> void doExpression(IExpression<T> expression, BiConsumer<IWorlds, T> outcomeHandler);
	
	void whileLoop(IExpression<Boolean> condition, IClause then);
}
