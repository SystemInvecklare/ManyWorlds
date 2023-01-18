package com.mattiasselin.libs.multiworld.worlds.helpers;

import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.trackers.MeanTracker;

/*package-protected*/ interface ITrackerCollector {
	void onMeanTracker(MeanTracker meanTracker);
	void onFlagTracker(FlagTracker flagTracker);
}
