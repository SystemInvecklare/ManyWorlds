package com.mattiasselin.libs.multiworld.worlds.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.trackers.MeanTracker;

public class TrackerValues implements ITrackerValues {
	private final Map<MeanTracker, Float> meanValues = new HashMap<MeanTracker, Float>();
	private final Map<FlagTracker, Probability> flagValues = new HashMap<FlagTracker, Probability>();

	@Override
	public TrackerValues copy() {
		TrackerValues copy = new TrackerValues();
		copy.setTo(this);
		return copy;
	}
	

	public void setTo(ITrackerValues other) {
		if(this == other) {
			return;
		}
		this.meanValues.clear();
		this.flagValues.clear();
		other.collectTrackers(new ITrackerCollector() {
			@Override
			public void onMeanTracker(MeanTracker meanTracker) {
				meanValues.put(meanTracker, other.getValue(meanTracker));
			}

			@Override
			public void onFlagTracker(FlagTracker flagTracker) {
				flagValues.put(flagTracker, other.getValue(flagTracker));
			}
		});
	}

	public void merge(Probability myProbability, Probability otherProbability, ITrackerValues other) {
		final Set<MeanTracker> meanTrackers = new HashSet<>(meanValues.keySet());
		final Set<FlagTracker> flagTrackers = new HashSet<>(flagValues.keySet());
		other.collectTrackers(new ITrackerCollector() {
			@Override
			public void onMeanTracker(MeanTracker meanTracker) {
				meanTrackers.add(meanTracker);
			}

			@Override
			public void onFlagTracker(FlagTracker flagTracker) {
				flagTrackers.add(flagTracker);
			}
		});
		
		for(MeanTracker meanTracker : meanTrackers) {
			float value = MeanTracker.merge(myProbability, this.getValue(meanTracker), otherProbability, other.getValue(meanTracker));
			if(value == 0) {
				this.meanValues.remove(meanTracker);
			} else {
				this.meanValues.put(meanTracker, value);
			}
		}
		
		for(FlagTracker flagTracker : flagTrackers) {
			Probability value = FlagTracker.merge(myProbability, this.getValue(flagTracker), otherProbability, other.getValue(flagTracker));
			if(value.getPercentChance() == 0) {
				this.flagValues.remove(flagTracker);
			} else {
				this.flagValues.put(flagTracker, value);
			}
		}
	}

	public void setTracker(MeanTracker tracker, float value) {
		meanValues.put(tracker, value);
	}

	public void multiplyTracker(MeanTracker tracker, float factor) {
		meanValues.put(tracker, getValue(tracker)*factor);
	}

	public void addToTracker(MeanTracker tracker, float delta) {
		meanValues.put(tracker, getValue(tracker) + delta);
	}
	
	public void setTracker(FlagTracker tracker, Probability value) {
		flagValues.put(tracker, value);
	}
	
	@Override
	public float getValue(MeanTracker meanTracker) {
		Float value = meanValues.get(meanTracker);
		return value != null ? value : 0;
	}
	
	@Override
	public Probability getValue(FlagTracker flagTracker) {
		Probability value = flagValues.get(flagTracker);
		return value != null ? value : Probability.IMPOSSIBLE;
	}
	
	@Override
	public void collectTrackers(ITrackerCollector collector) {
		for(MeanTracker meanTracker : meanValues.keySet()) {
			collector.onMeanTracker(meanTracker);
		}
		for(FlagTracker flagTracker : flagValues.keySet()) {
			collector.onFlagTracker(flagTracker);
		}
	}
}
