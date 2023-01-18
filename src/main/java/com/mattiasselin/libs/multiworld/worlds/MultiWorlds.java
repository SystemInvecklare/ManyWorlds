package com.mattiasselin.libs.multiworld.worlds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.WorldState;
import com.mattiasselin.libs.multiworld.WorldTerm;
import com.mattiasselin.libs.multiworld.expression.Expression;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.trackers.MeanTracker;
import com.mattiasselin.libs.multiworld.worlds.helpers.IClause;
import com.mattiasselin.libs.multiworld.worlds.helpers.TrackerValues;
import com.mattiasselin.libs.multiworld.worlds.helpers.WhileLoop;

public class MultiWorlds implements IWorlds {
	private final List<WorldTerm> worlds = new ArrayList<WorldTerm>();
	
	public MultiWorlds() {
		this(new WorldTerm(new WorldState(), Probability.CERTAIN, new TrackerValues()));
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
			boolean worldStateSplitOrChanged = subWorldCollector.size() > 1;
			if(subWorldCollector.size() == 1 && !subWorldCollector.get(0).state.equals(worldTerm.state)) {
				worldStateSplitOrChanged = true; //Changed!
			}
			if(worldStateSplitOrChanged) {
				worldsIterator.remove();
				WorldTerm.mergeWorlds(subWorldCollector);
				newWorlds.addAll(subWorldCollector);
			} else if(subWorldCollector.size() == 1) {
				WorldTerm subWorldTerm = subWorldCollector.get(0);
				if(worldTerm != subWorldTerm) {
					worldTerm.trackerValues.setTo(subWorldTerm.trackerValues);
				}
			}
		}
		worlds.addAll(newWorlds);
		mergeWorlds();
	}
	

	@Override
	public void whileLoop(IStochasticExpression<Boolean> condition, IClause then) {
		doStochastic(condition, new WhileLoop(condition, then));
	}
	
	@Override
	public <T> void splitOn(IStochasticExpression<T> value, BiConsumer<IWorlds, T> outcomeHandler, Comparator<IWorlds> order) {
		Map<T, List<WorldTerm>> worldMap = new HashMap<T, List<WorldTerm>>();
		doStochastic(value, (splitWorld, val) -> {
			List<WorldTerm> existingTerms = worldMap.get(val);
			if(existingTerms == null) {
				existingTerms = new ArrayList<>();
				worldMap.put(val, existingTerms);
			}
			((MultiWorlds) splitWorld).getWorlds(existingTerms);
		});
		worlds.clear();
		class Outcome {
			public final T value;
			public final MultiWorlds subWorld;
			
			public Outcome(T value, MultiWorlds subWorld) {
				this.value = value;
				this.subWorld = subWorld;
			}
		}
		List<Outcome> outcomes = new ArrayList<>();
		for(Map.Entry<T, List<WorldTerm>> entry : worldMap.entrySet()) {
			WorldTerm.mergeWorlds(entry.getValue());
			outcomes.add(new Outcome(entry.getKey(), new MultiWorlds(entry.getValue())));
		}
		if(order != null) {
			Collections.sort(outcomes, Comparator.comparing(outcome -> outcome.subWorld, order));
		}
		for(Outcome result : outcomes) {
			outcomeHandler.accept(result.subWorld, result.value);
			result.subWorld.getWorlds(worlds);
		}
		mergeWorlds();
	}
	
	@Override
	public Probability getTotalProbability() {
		return WorldTerm.probabilitySum(worlds);
	}
	
	/**
	 * This method promises to just add worlds to result, no clearing or anything.
	 * 
	 * @param result
	 * @return result with worlds added
	 */
	public <C extends Collection<WorldTerm>> C getWorlds(C result) {
		result.addAll(worlds);
		return result;
	}
	
	@Override
	public void setTracker(MeanTracker tracker, float value) {
		for(WorldTerm term : worlds) {
			term.trackerValues.setTracker(tracker, value);
		}
	}
	
	@Override
	public void addToTracker(MeanTracker tracker, float delta) {
		for(WorldTerm term : worlds) {
			term.trackerValues.addToTracker(tracker, delta);
		}
	}
	
	@Override
	public void multiplyTracker(MeanTracker tracker, float factor) {
		for(WorldTerm term : worlds) {
			term.trackerValues.multiplyTracker(tracker, factor);
		}
	}
	
	@Override
	public void setTracker(FlagTracker tracker, boolean value) {
		setTracker(tracker, value ? Probability.CERTAIN : Probability.IMPOSSIBLE);
	}
	
	@Override
	public void setTracker(FlagTracker tracker, Probability value) {
		for(WorldTerm term : worlds) {
			term.trackerValues.setTracker(tracker, value);
		}
	}
	
	@Override
	public FlagTrackerModifier modifyTracker(FlagTracker tracker) {
		return FlagTrackerModifier.from(worlds, tracker);
	}
	
	/**
	 * Get's the mean value of a MeanTracker. Intentionally not part of IWorlds interface to reduce chance of reading this while simulating.
	 */
	public float sampleTracker(MeanTracker tracker) {
		float sumValue = 0;
		Probability sumProbability = Probability.IMPOSSIBLE;
		for(WorldTerm term : worlds) {
			sumValue += term.probability.weight(term.trackerValues.getValue(tracker));
			sumProbability = sumProbability.add(term.probability);
		}
		return sumValue*100/sumProbability.getPercentChance();
	}
	
	public Probability sampleTracker(FlagTracker tracker) {
		Probability sumValue = Probability.IMPOSSIBLE;
		Probability sumProbability = Probability.IMPOSSIBLE;
		for(WorldTerm term : worlds) {
			sumValue = sumValue.add(term.trackerValues.getValue(tracker));
			sumProbability = sumProbability.add(term.probability);
		}
		return sumValue.relativeTo(sumProbability);
	}
}
