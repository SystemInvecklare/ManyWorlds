package com.mattiasselin.libs.multiworld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class WorldTerm {
	public final WorldState state;
	public final Probability probability;
	
	public WorldTerm(WorldState state, Probability probability) {
		this.state = state;
		this.probability = probability;
	}
	
	public <T extends Collection<WorldTerm>> T splitEqually(T result, int parts) {
		for(Probability prob : probability.splitEqually(new ArrayList<Probability>(), parts)) {
			result.add(new WorldTerm(state.copy(), prob));
		}
		return result;
	}
	
	public void split(Consumer<WorldTerm> consumer, int ... weights) {
		if(weights.length == 0) {
			return;
		}
		if(weights.length == 1) {
			consumer.accept(this);
		} else {
			for(Probability prob : probability.split(new ArrayList<Probability>(), weights)) {
				consumer.accept(new WorldTerm(state.copy(), prob));
			}
		}
	}
	
	public static void mergeWorlds(final List<WorldTerm> worlds) {
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
