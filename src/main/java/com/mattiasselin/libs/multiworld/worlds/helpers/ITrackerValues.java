package com.mattiasselin.libs.multiworld.worlds.helpers;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.trackers.MeanTracker;

public interface ITrackerValues {
	TrackerValues copy();
	float getValue(MeanTracker meanTracker);
	Probability getValue(FlagTracker flagTracker);
	void collectTrackers(ITrackerCollector collector);
}
