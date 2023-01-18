package com.mattiasselin.libs.multiworld.trackers;

import org.junit.Assert;
import org.junit.Test;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.expression.Stochastic;
import com.mattiasselin.libs.multiworld.worlds.MultiWorlds;

public class FlagTrackerTest {
	@Test
	public void testBasic() {
		FlagTracker tracker = new FlagTracker();
		{
			MultiWorlds multiWorlds = new MultiWorlds();
			multiWorlds.setTracker(tracker, false);
			Assert.assertEquals(0, multiWorlds.sampleTracker(tracker).getPercentChance(), 0.00000001f);
		}
		{
			MultiWorlds multiWorlds = new MultiWorlds();
			multiWorlds.setTracker(tracker, true);
			Assert.assertEquals(100, multiWorlds.sampleTracker(tracker).getPercentChance(), 0.00000001f);
		}
		{
			MultiWorlds multiWorlds = new MultiWorlds();
			multiWorlds.setTracker(tracker, Probability.CERTAIN.splitEqually(3));
			Assert.assertEquals(33.33333333f, multiWorlds.sampleTracker(tracker).getPercentChance(), 0.000001f);
		}
	}
	
	@Test
	public void testGeneral() {
		FlagTracker tracker = new FlagTracker();
		MultiWorlds multiWorlds = new MultiWorlds();
		multiWorlds.doStochastic(Stochastic.even(1,2), (w1, v1) -> {
			if(v1 == 1) {
				w1.setTracker(tracker, true);
			}
			w1.doStochastic(Stochastic.even(0,1), (w2, v2) -> {
				if(v2 == 1) {
					w2.setTracker(tracker, false);
				}
			});
		});
		Assert.assertEquals(25f, multiWorlds.sampleTracker(tracker).getPercentChance(), 0.00000001f);
	}
	
	@Test
	public void testMultiply() {
		FlagTracker tracker = new FlagTracker();
		MultiWorlds multiWorlds = new MultiWorlds();
		multiWorlds.setTracker(tracker, true);
		multiWorlds.doStochastic(Stochastic.even(1,2), (w1, v1) -> {
			if(v1 == 1) {
				w1.modifyTracker(tracker).multiply(1, 3);
			}
			w1.doStochastic(Stochastic.even(0,1), (w2, v2) -> {
				if(v2 == 1) {
					w2.modifyTracker(tracker).multiply(1, 2);
				}
			});
		});
		Assert.assertEquals(50, multiWorlds.sampleTracker(tracker).getPercentChance(), 0.000001f);
	}
}
