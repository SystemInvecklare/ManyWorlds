package com.mattiasselin.libs.multiworld.trackers;

import com.mattiasselin.libs.multiworld.Probability;

/**
 * <p>Let pa and pb be probabilities. Let x -> op(x) be an operation.</p>
 * <p>Then op is a valid tracker operation iff:</p> 
 * <p>res(op(a),op(b)) = op(res(a,b)) where res(a,b) = (a*pa+b*pb)/(pa+pb)</p>
 * <br>
 * 
 * 'and y':
 * op(x) = x*y (multiplication by constant so we know it's true.)
 * 
 * 'or false'
 * op(x) = x
 * 'or true'
 * op(x) = 1
 * 
 */
public final class FlagTracker {
	public static Probability merge(Probability probabilityA, Probability valueA, Probability probabilityB, Probability valueB) {
		if(probabilityA.getPercentChance() == 0 && probabilityB.getPercentChance() == 0) {
			return Probability.IMPOSSIBLE;
		}
		return probabilityA.multiply(valueA).add(probabilityB.multiply(valueB)).relativeTo(probabilityA.add(probabilityB));
	}
}
