package com.mattiasselin.libs.multiworld.trackers;

import com.mattiasselin.libs.multiworld.Probability;

public final class MeanTracker {
	public static float merge(Probability probabilityA, float valueA, Probability probabilityB, float valueB) {
		if(probabilityA.getPercentChance() == 0 && probabilityB.getPercentChance() == 0) {
			return 0;
		}
		return (probabilityA.getPercentChance()*valueA+probabilityB.getPercentChance()*valueB)/(probabilityA.getPercentChance()+probabilityB.getPercentChance());
	}
}
