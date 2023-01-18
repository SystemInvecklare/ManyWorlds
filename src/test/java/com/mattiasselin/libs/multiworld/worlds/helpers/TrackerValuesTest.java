package com.mattiasselin.libs.multiworld.worlds.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.trackers.FlagTracker;
import com.mattiasselin.libs.multiworld.trackers.MeanTracker;

public class TrackerValuesTest {
	@Test
	public void testDefaultValues() {
		TrackerValues trackerValues = new TrackerValues();
		Assert.assertEquals(0, trackerValues.getValue(new FlagTracker()).getPercentChance(), 0f);
		
		Assert.assertEquals(0, trackerValues.getValue(new MeanTracker()), 0f);
	}
	
	@Test
	public void testSetters() {
		TrackerValues trackerValues = new TrackerValues();
		MeanTracker meanTracker1 = new MeanTracker();
		MeanTracker meanTracker2 = new MeanTracker();
		FlagTracker flagTracker1 = new FlagTracker();
		FlagTracker flagTracker2 = new FlagTracker();
		
		trackerValues.setTracker(meanTracker1, 123);
		trackerValues.setTracker(flagTracker2, Probability.CERTAIN.splitEqually(5));
		
		Assert.assertEquals(123, trackerValues.getValue(meanTracker1), 0.000001f);
		Assert.assertEquals(20, trackerValues.getValue(flagTracker2).getPercentChance(), 0.000001f);
		
		trackerValues.setTracker(meanTracker1, 1000);
		Assert.assertEquals(1000, trackerValues.getValue(meanTracker1), 0.000001f);
		Assert.assertEquals(0, trackerValues.getValue(meanTracker2), 0.000001f);
		Assert.assertEquals(20, trackerValues.getValue(flagTracker2).getPercentChance(), 0.000001f);
		Assert.assertEquals(0, trackerValues.getValue(flagTracker1).getPercentChance(), 0f);
		
		FlagTracker flagTracker3 = new FlagTracker();
		trackerValues.setTracker(flagTracker3, Probability.CERTAIN);
		
		Assert.assertEquals(100, trackerValues.getValue(flagTracker3).getPercentChance(), 0f);
	}
	
	@Test
	public void testCollect() {
		TrackerValues trackerValues = new TrackerValues();
		MeanTracker meanTracker1 = new MeanTracker();
		FlagTracker flagTracker1 = new FlagTracker();
		FlagTracker flagTracker2 = new FlagTracker();
		
		trackerValues.setTracker(meanTracker1, 123);
		trackerValues.setTracker(flagTracker1, Probability.CERTAIN.splitEqually(5));
		trackerValues.setTracker(meanTracker1, 1000);
		trackerValues.setTracker(flagTracker2, Probability.CERTAIN);
		
		Map<MeanTracker, Integer> meanTrackers = new HashMap<MeanTracker, Integer>();
		Map<FlagTracker, Integer> flagTrackers = new HashMap<FlagTracker, Integer>();
		trackerValues.collectTrackers(new ITrackerCollector() {
			@Override
			public void onMeanTracker(MeanTracker meanTracker) {
				Integer count = meanTrackers.get(meanTracker);
				if(count == null) {
					count = 0;
				}
				meanTrackers.put(meanTracker, count + 1);
			}
			
			@Override
			public void onFlagTracker(FlagTracker flagTracker) {
				Integer count = flagTrackers.get(flagTracker);
				if(count == null) {
					count = 0;
				}
				flagTrackers.put(flagTracker, count + 1);
			}
		});
		
		Assert.assertEquals(1, meanTrackers.entrySet().size());
		Assert.assertEquals(2, flagTrackers.entrySet().size());
		
		Assert.assertTrue(meanTrackers.containsKey(meanTracker1));
		Assert.assertEquals(1, meanTrackers.get(meanTracker1).intValue());
		
		Assert.assertTrue(flagTrackers.containsKey(flagTracker1));
		Assert.assertTrue(flagTrackers.containsKey(flagTracker2));
		Assert.assertEquals(1, flagTrackers.get(flagTracker1).intValue());
		Assert.assertEquals(1, flagTrackers.get(flagTracker2).intValue());
	}
	
	@Test
	public void testCopy() {
		TrackerValues trackerValues = new TrackerValues();
		
		MeanTracker meanTracker1 = new MeanTracker();
		MeanTracker meanTracker2 = new MeanTracker();
		FlagTracker flagTracker1 = new FlagTracker();
		FlagTracker flagTracker2 = new FlagTracker();
		
		trackerValues.setTracker(meanTracker1, 123);
		trackerValues.setTracker(flagTracker1, Probability.CERTAIN.splitEqually(5));
		trackerValues.setTracker(meanTracker1, 1000);
		trackerValues.setTracker(flagTracker2, Probability.CERTAIN);
		
		TrackerValues trackerValuesCopy = trackerValues.copy();
		Assert.assertEquals(1000, trackerValuesCopy.getValue(meanTracker1), 0.000001f);
		Assert.assertEquals(0, trackerValuesCopy.getValue(meanTracker2), 0.000001f);
		Assert.assertEquals(20, trackerValuesCopy.getValue(flagTracker1).getPercentChance(), 0.000001f);
		Assert.assertEquals(100, trackerValuesCopy.getValue(flagTracker2).getPercentChance(), 0f);
		
		trackerValuesCopy.setTracker(flagTracker1, Probability.IMPOSSIBLE);
		trackerValuesCopy.setTracker(meanTracker1, 9);
		trackerValuesCopy.setTracker(meanTracker2, 10);
		
		Assert.assertEquals(1000, trackerValues.getValue(meanTracker1), 0.000001f);
		Assert.assertEquals(0, trackerValues.getValue(meanTracker2), 0.000001f);
		Assert.assertEquals(20, trackerValues.getValue(flagTracker1).getPercentChance(), 0.000001f);
		Assert.assertEquals(100, trackerValues.getValue(flagTracker2).getPercentChance(), 0f);
		
		Assert.assertEquals(9, trackerValuesCopy.getValue(meanTracker1), 0.000001f);
		Assert.assertEquals(10, trackerValuesCopy.getValue(meanTracker2), 0.000001f);
		Assert.assertEquals(0, trackerValuesCopy.getValue(flagTracker1).getPercentChance(), 0.000001f);
		Assert.assertEquals(100, trackerValuesCopy.getValue(flagTracker2).getPercentChance(), 0f);
		
	}
}
