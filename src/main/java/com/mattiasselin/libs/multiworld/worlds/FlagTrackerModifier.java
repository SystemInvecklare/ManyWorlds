package com.mattiasselin.libs.multiworld.worlds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.WorldTerm;
import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.worlds.helpers.TrackerValues;

public abstract class FlagTrackerModifier {
	private FlagTrackerModifier() {
	}
	
	public abstract void and(boolean value);
	public abstract void or(boolean value);
	public abstract void not();
	/**
	 * <p>Clamps factor between 0 and 1 before multiplying.</p>
	 */
	public abstract void multiply(int numerator, int denominator);
	/**
	 * <p>Clamps factor between 0 and 1 before multiplying.</p>
	 * <p>Multiply complement:</p>
	 * <p>tracker = 1-factor*(1-tracker)</p>
	 */
	public abstract void multiplyComplement(int numerator, int denominator);
	
	
	/*package-protected*/ static FlagTrackerModifier from(TrackerValues trackerValues, FlagTracker tracker) {
		return from(op -> op.accept(trackerValues), tracker);
	}
	
	/*package-protected*/ static FlagTrackerModifier from(List<WorldTerm> worlds, FlagTracker tracker) {
		return from(op -> {
			for(WorldTerm worldTerm : worlds) {
				op.accept(worldTerm.trackerValues);
			}
		}, tracker);
	}

	/*package-protected*/ static FlagTrackerModifier from(Consumer<Consumer<TrackerValues>> opHandler, FlagTracker tracker) {
		return new FlagTrackerModifier() {
			@Override
			public void and(boolean value) {
				opHandler.accept(trackerValues -> {
					if(!value) {
						trackerValues.setTracker(tracker, Probability.IMPOSSIBLE);
					}
				});
			}

			@Override
			public void or(boolean value) {
				opHandler.accept(trackerValues -> {
					if(value) {
						trackerValues.setTracker(tracker, Probability.CERTAIN);
					}
				});
			}

			@Override
			public void not() {
				opHandler.accept(trackerValues -> {
					trackerValues.setTracker(tracker, trackerValues.getValue(tracker).complement());
				});
			}

			@Override
			public void multiply(int numerator, int denominator) {
				final RationalNumber rationalNumber = new RationalNumber(numerator, denominator).clamp();
				opHandler.accept(trackerValues -> {
					trackerValues.setTracker(tracker, rationalNumber.multiply(trackerValues.getValue(tracker)));
				});
			}
			
			@Override
			public void multiplyComplement(int numerator, int denominator) {
				final RationalNumber rationalNumber = new RationalNumber(numerator, denominator).clamp();
				opHandler.accept(trackerValues -> {
					trackerValues.setTracker(tracker, rationalNumber.multiply(trackerValues.getValue(tracker).complement()).complement());
				});
			}
		};
	}
	
	private static class RationalNumber {
		private static final RationalNumber ZERO = new RationalNumber(0, 1);
		private static final RationalNumber ONE = new RationalNumber(1, 1);
		
		private final int n;
		private final int d;
		
		public RationalNumber(int n, int d) {
			this.n = n;
			this.d = d;
		}
		
		public Probability multiply(Probability value) {
			if(n == d) {
				return value;
			} else if(n == 0) {
				return Probability.IMPOSSIBLE;
			}
			// Split p with weights a and b.  then p1 will have size p*(a/(a+b))
			// Since we want 'value' we can let a == n and a+b == d --> b = d - n
			return value.split(new ArrayList<>(), n, d - n).get(0);
		}

		public RationalNumber clamp() {
			if(d == 0) {
				return ONE;
			}
			if(n == 0) {
				return ZERO;
			}
			if(n < 0 && d < 0) {
				return new RationalNumber(-n, -d);
			}
			if(n < 0 || d < 0) {
				return ZERO;
			}
			if(d < n) {
				return ONE;
			}
			return this;
		}
	}
}
