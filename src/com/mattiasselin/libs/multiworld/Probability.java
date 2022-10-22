package com.mattiasselin.libs.multiworld;

import java.util.Collection;

public class Probability {
	public static final Probability CERTAIN = new Probability(100);
	public static final Probability IMPOSSIBLE = new Probability(0);
	
	private final float percent; //TODO can be nested splits instead! --> Better numerical stability
	
	private Probability(float percent) {
		this.percent = percent;
	}

	public <T extends Collection<Probability>> T splitEqually(T result, int parts) {
		float newPercent = -1;
		if(parts > 0) {
			newPercent = percent/parts;
		}
		for(int i = 0; i < parts; ++i) {
			result.add(new Probability(newPercent));
		}
		return result;
	}
	
	

	public <T extends Collection<Probability>> T split(T result, int ... weights) {
		int sum = 0;
		for(int w : weights) {
			sum += w;
		}
		for(int w : weights) {
			result.add(new Probability(((float) w*percent)/sum));
		}
		return result;
	}

	public Probability add(Probability other) {
		return new Probability(this.percent+other.percent);
	}

	public float getPercentChance() {
		return percent;
	}
}
