package com.mattiasselin.libs.multiworld.worlds;

import java.util.Comparator;
import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.expression.Constant;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.trackers.MeanTracker;
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
	
	default <T> void splitOn(IStochasticExpression<T> value, BiConsumer<IWorlds, T> outcomeHandler) {
		splitOn(value, outcomeHandler, null);
	}
	
	<T> void splitOn(IStochasticExpression<T> value, BiConsumer<IWorlds, T> outcomeHandler, Comparator<IWorlds> order);
	
	Probability getTotalProbability();
	
	void setTracker(MeanTracker tracker, float value);
	void multiplyTracker(MeanTracker tracker, float factor);
	void addToTracker(MeanTracker tracker, float delta);
	
	void setTracker(FlagTracker tracker, boolean value);
	void setTracker(FlagTracker tracker, Probability value);
	FlagTrackerModifier modifyTracker(FlagTracker tracker);

	static Comparator<IWorlds> ORDER_BY_MOST_LIKELY = (w1,w2) -> Float.compare(w2.getTotalProbability().getPercentChance(), w1.getTotalProbability().getPercentChance());
	static Comparator<IWorlds> ORDER_BY_LEAST_LIKELY = (w1,w2) -> Float.compare(w1.getTotalProbability().getPercentChance(), w2.getTotalProbability().getPercentChance());
}
