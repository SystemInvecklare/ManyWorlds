package com.mattiasselin.libs.multiworld;

import java.util.ArrayList;
import java.util.Collection;
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
}
