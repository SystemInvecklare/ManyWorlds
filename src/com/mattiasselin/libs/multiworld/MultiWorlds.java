package com.mattiasselin.libs.multiworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

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

	@Override
	public <T> Variable<T> set(Variable<T> variable, IExpression<T> expression) {
		if(worlds.isEmpty()) {
			return variable;
		}
		for(WorldTerm worldTerm : worlds) {
			worldTerm.state.setValue(variable, expression.eval(worldTerm.state));
		}
		mergeWorlds();
		return variable;
	}

	@Override
	public void ifTrue(IExpression<Boolean> condition, IClause then) {
		ifTrue(condition, then, null);
	}

	@Override
	public void ifTrue(IExpression<Boolean> condition, IClause thenClause, IClause elseClause) {
		List<WorldTerm> matching = new ArrayList<>();
		List<WorldTerm> notMatching = new ArrayList<>();
		Iterator<WorldTerm> it = worlds.iterator();
		while(it.hasNext()) {
			WorldTerm worldTerm = it.next();
			if(condition.eval(worldTerm.state)) {
				matching.add(worldTerm);
				it.remove();
			} else if(elseClause != null) {
				notMatching.add(worldTerm);
				it.remove();
			}
		}
		if(!matching.isEmpty()) {
			MultiWorlds thenWorlds = new MultiWorlds(matching);
			thenClause.execute(thenWorlds);
			thenWorlds.mergeWorlds();
			worlds.addAll(thenWorlds.worlds);
		}
		if(elseClause != null && !notMatching.isEmpty()) {
			MultiWorlds elseWorlds = new MultiWorlds(notMatching);
			elseClause.execute(elseWorlds);
			elseWorlds.mergeWorlds();
			worlds.addAll(elseWorlds.worlds);
		}
		mergeWorlds();
	}
	
	private static void mergeWorlds(final List<WorldTerm> worlds) {
		if(worlds.size() > 1) {
			Map<WorldState, ProbabilityAccumulator> merged = new HashMap<WorldState, ProbabilityAccumulator>();
			for(WorldTerm worldTerm : worlds) {
				ProbabilityAccumulator probabilityAccumulator = merged.get(worldTerm.state);
				if(probabilityAccumulator == null) {
					merged.put(worldTerm.state, new ProbabilityAccumulator(worldTerm.probability));
				} else {
					probabilityAccumulator.accumulate(worldTerm.probability);
				}
			}
			if(merged.entrySet().size() < worlds.size()) {
				worlds.clear();
				for(Entry<WorldState, ProbabilityAccumulator> entry : merged.entrySet()) {
					worlds.add(new WorldTerm(entry.getKey(), entry.getValue().probability));
				}
			}
		}
	}
	
	private void mergeWorlds() {
		mergeWorlds(this.worlds);
	}

	@Override
	public <T> Variable<T> set(Variable<T> variable, IStochastic<T> expression) {
		doStochastic(expression, (world, outcome) -> {
			world.set(variable, new Constant<T>(outcome));
		});
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
	public <T> void doStochastic(IStochastic<T> stochastic, BiConsumer<IWorlds, T> outcomeHandler) {
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
				mergeWorlds(subWorldCollector);
				newWorlds.addAll(subWorldCollector);
			}
		}
		worlds.addAll(newWorlds);
		mergeWorlds();
	}
	
	@Override
	public <T> void doExpression(IExpression<T> expression, BiConsumer<IWorlds, T> outcomeHandler) {
		List<WorldTerm> newWorlds = new ArrayList<>();
		for(WorldTerm worldTerm : worlds) {
			MultiWorlds subWorld = new MultiWorlds(Arrays.asList(worldTerm));
			outcomeHandler.accept(subWorld, expression.eval(worldTerm.state));
			newWorlds.addAll(subWorld.worlds);
		}
		worlds.clear();
		worlds.addAll(newWorlds);
		mergeWorlds();
	}
	
	@Override
	public void whileLoop(IExpression<Boolean> condition, IClause then) {
		boolean[] continueLooping = new boolean[] {true};
		while(continueLooping[0]) {
			continueLooping[0] = false;
			ifTrue(condition, then);
			ifTrue(condition, set -> continueLooping[0] = true); //If condition true in any world
		}
	}
	
	//TODO expose like this?
	public <C extends Collection<WorldTerm>> C getWorlds(C result) {
		result.addAll(worlds);
		return result;
	}

	private static class ProbabilityAccumulator {
		private Probability probability;

		public ProbabilityAccumulator(Probability probability) {
			this.probability = probability;
		}
		
		public void accumulate(Probability probability) {
			this.probability = this.probability.add(probability);
		}
	}
}
