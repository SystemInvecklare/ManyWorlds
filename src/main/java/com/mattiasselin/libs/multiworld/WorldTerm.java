package com.mattiasselin.libs.multiworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.mattiasselin.libs.multiworld.worlds.helpers.ITrackerValues;
import com.mattiasselin.libs.multiworld.worlds.helpers.TrackerValues;

public class WorldTerm {
	public final WorldState state;
	public final Probability probability;
	public final TrackerValues trackerValues;
	
	public WorldTerm(WorldState state, Probability probability, TrackerValues trackerValues) {
		this.state = state;
		this.probability = probability;
		this.trackerValues = trackerValues;
	}
	
	public void split(Consumer<WorldTerm> consumer, int ... weights) {
		if(weights.length == 0) {
			return;
		}
		if(weights.length == 1) {
			consumer.accept(this);
		} else {
			for(Probability prob : probability.split(new ArrayList<Probability>(), weights)) {
				consumer.accept(new WorldTerm(state.copy(), prob, trackerValues.copy()));
			}
		}
	}
	
	public static void mergeWorlds(final List<WorldTerm> worlds) {
		if(worlds.size() > 1) {
			Map<WorldState, ProbabilityAccumulator> merged = new HashMap<WorldState, ProbabilityAccumulator>();
			for(WorldTerm worldTerm : worlds) {
				ProbabilityAccumulator probabilityAccumulator = merged.get(worldTerm.state);
				if(probabilityAccumulator == null) {
					merged.put(worldTerm.state, new ProbabilityAccumulator(worldTerm.probability, worldTerm.trackerValues.copy()));
				} else {
					probabilityAccumulator.accumulate(worldTerm.probability, worldTerm.trackerValues);
				}
			}
			if(merged.entrySet().size() < worlds.size()) {
				worlds.clear();
				for(Entry<WorldState, ProbabilityAccumulator> entry : merged.entrySet()) {
					ProbabilityAccumulator probabilityAccumulator = entry.getValue();
					worlds.add(new WorldTerm(entry.getKey(), probabilityAccumulator.probability, probabilityAccumulator.trackerValues));
				}
			}
		}
	}

	public static Probability probabilitySum(List<WorldTerm> worlds) {
		ProbabilityAccumulator accumulator = new ProbabilityAccumulator(Probability.IMPOSSIBLE, null);
		for(WorldTerm worldTerm : worlds) {
			accumulator.accumulate(worldTerm.probability, null);
		}
		return accumulator.probability;
	}
	
	private static class ProbabilityAccumulator {
		private Probability probability;
		private final TrackerValues trackerValues;

		public ProbabilityAccumulator(Probability probability, TrackerValues trackerValues) {
			this.probability = probability;
			this.trackerValues = trackerValues;
		}
		
		public void accumulate(Probability probability, ITrackerValues trackerValues) {
			if(this.trackerValues != null) {
				this.trackerValues.merge(this.probability, probability, trackerValues);
			}
			this.probability = this.probability.add(probability);
		}
	}
}
