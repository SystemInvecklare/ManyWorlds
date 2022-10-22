package com.mattiasselin.libs.multiworld.worlds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.WorldState;
import com.mattiasselin.libs.multiworld.WorldTerm;
import com.mattiasselin.libs.multiworld.expression.Expression;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.worlds.helpers.IClause;
import com.mattiasselin.libs.multiworld.worlds.helpers.WhileLoop;

public class MultiWorlds implements IWorlds {
	private final List<WorldTerm> worlds = new ArrayList<WorldTerm>();
	
	public MultiWorlds() {
		this(new WorldTerm(new WorldState(), Probability.CERTAIN));
	}
	
	public MultiWorlds(List<WorldTerm> worlds) {
		this.worlds.addAll(worlds);
	}
	
	public MultiWorlds(WorldTerm worldTerm) {
		this.worlds.add(worldTerm);
	}
	
	private void mergeWorlds() {
		WorldTerm.mergeWorlds(this.worlds);
	}

	@Override
	public <T> Variable<T> set(Variable<T> variable, IStochasticExpression<T> expression) {
		if(expression instanceof Expression) {
			for(WorldTerm worldTerm : worlds) {
				worldTerm.state.setValue(variable, Expression.evalExpression(expression, worldTerm.state));
			}
			mergeWorlds();
		} else {
			doStochastic(expression, (world, outcome) -> {
				world.setConst(variable, outcome);
			});
		}
		return variable;
	}
	
	@Override
	public void forget(Variable<?> variable) {
		for(WorldTerm worldTerm : worlds) {
			worldTerm.state.forget(variable);
		}
		mergeWorlds();
	}
	

	@Override
	public <T> void doStochastic(IStochasticExpression<T> stochastic, BiConsumer<IWorlds, T> outcomeHandler) {
		List<WorldTerm> newWorlds = new ArrayList<>();
		List<WorldTerm> subWorldCollector = new ArrayList<>();
		Iterator<WorldTerm> worldsIterator = worlds.iterator();
		while(worldsIterator.hasNext()) {
			if(!subWorldCollector.isEmpty()) {
				subWorldCollector.clear();
			}
			WorldTerm worldTerm = worldsIterator.next();
			stochastic.split(worldTerm, (subWorldTerm, outcome) -> {
				MultiWorlds subWorld = new MultiWorlds(subWorldTerm);
				outcomeHandler.accept(subWorld, outcome);
				subWorldCollector.addAll(subWorld.worlds);
			});
			if(subWorldCollector.size() > 1) {
				worldsIterator.remove();
				WorldTerm.mergeWorlds(subWorldCollector);
				newWorlds.addAll(subWorldCollector);
			}
		}
		worlds.addAll(newWorlds);
		mergeWorlds();
	}
	

	@Override
	public void whileLoop(IStochasticExpression<Boolean> condition, IClause then) {
		doStochastic(condition, new WhileLoop(condition, then));
	}
	
	public <C extends Collection<WorldTerm>> C getWorlds(C result) {
		result.addAll(worlds);
		return result;
	}
}
