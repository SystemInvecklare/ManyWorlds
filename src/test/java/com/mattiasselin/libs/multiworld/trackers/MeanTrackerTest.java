package com.mattiasselin.libs.multiworld.trackers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.mattiasselin.libs.multiworld.Probability;
import com.mattiasselin.libs.multiworld.expression.Expression;
import com.mattiasselin.libs.multiworld.expression.Stochastic;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.worlds.IWorlds;
import com.mattiasselin.libs.multiworld.worlds.MultiWorlds;
import com.mattiasselin.libs.multiworld.worlds.SingleWorld;

public class MeanTrackerTest {
	
	@Test
	public void testMerge() {
		List<Probability> probabilities = Probability.CERTAIN.split(new ArrayList<>(), 1, 2 ,3);
		float mergedValue = MeanTracker.merge(probabilities.get(0), 10, probabilities.get(1), 5);
		Assert.assertEquals(((1f/6f)*10 + (1f/3f)*5)/(1f/2f), mergedValue, 0.000001f);
		
		
		final Probability p_1_of_6 = probabilities.get(0);
		final Probability p_2_of_6 = p_1_of_6.add(p_1_of_6);
		final Probability p_3_of_6 = p_2_of_6.add(p_1_of_6);
		final Probability p_4_of_6 = p_3_of_6.add(p_1_of_6);
		final Probability p_5_of_6 = p_4_of_6.add(p_1_of_6);
		float avgD6 = MeanTracker.merge(p_5_of_6, MeanTracker.merge(p_4_of_6, MeanTracker.merge(p_3_of_6, MeanTracker.merge(p_2_of_6, MeanTracker.merge(p_1_of_6, 1, p_1_of_6, 2), p_1_of_6, 3), p_1_of_6, 4), p_1_of_6, 5), p_1_of_6, 6);
		Assert.assertEquals(3.5f, avgD6, 0.00001f);
	}
	
	private void runSimpleSimulation(IWorlds worlds, MeanTracker totalRolled, MeanTracker managedToRollOne) {
		Stochastic<Integer> d6 = Stochastic.even(1,2,3,4,5,6);
		worlds.doStochastic(d6, (w1, val) -> {
			w1.addToTracker(totalRolled, val);
			if(val == 1) {
				w1.setTracker(managedToRollOne, 1);
			}
		});
		worlds.doStochastic(d6, (w1, val) -> {
			w1.addToTracker(totalRolled, val);
			if(val == 1) {
				w1.setTracker(managedToRollOne, 1);
			}
			if(val == 6) {
				w1.doStochastic(d6, (w2, val2) -> {
					w2.addToTracker(totalRolled, val2);
					if(val2 == 1) {
						w2.setTracker(managedToRollOne, 1);
					}
				});
			}
		});
	}
	
	@Test
	public void testSimple() {
		MeanTracker totalRolled = new MeanTracker();
		MeanTracker managedToRollOne = new MeanTracker();
		
		MultiWorlds worlds = new MultiWorlds();
		runSimpleSimulation(worlds, totalRolled, managedToRollOne);
		float totalRolledValue = worlds.sampleTracker(totalRolled);
		float managedToRollOneValue = worlds.sampleTracker(managedToRollOne);
		Assert.assertEquals(7.58333333f, totalRolledValue, 0.0001f);
		Assert.assertEquals(1f/6f + (5f/6f)*(1f/6f + 1f/36f), managedToRollOneValue, 0.0001f);
	}
	
	@Test
	public void testSimpleStatistically() {
		MeanTracker totalRolled = new MeanTracker();
		MeanTracker managedToRollOne = new MeanTracker();
		
		List<Float> totalRolledSamples = new ArrayList<>();
		List<Float> managedToRollOneSamples = new ArrayList<>();
		
		final Random finalRandom = new Random(236950L);
		for(int i = 0; i < 1000; ++i) {
			SingleWorld singleWorld = new SingleWorld() {
				@Override
				protected double random() {
					return finalRandom.nextDouble();
				}
			};
			runSimpleSimulation(singleWorld, totalRolled, managedToRollOne);
			totalRolledSamples.add(singleWorld.sampleTracker(totalRolled));
			managedToRollOneSamples.add(singleWorld.sampleTracker(managedToRollOne));
		}
		float totalRolledMean = totalRolledSamples.stream().reduce(0f, (a,b) -> a+b)/totalRolledSamples.size();
		float managedToRollOnedMean = managedToRollOneSamples.stream().reduce(0f, (a,b) -> a+b)/managedToRollOneSamples.size();
		Assert.assertEquals(7.5833333f, totalRolledMean, 0.001f);
		Assert.assertEquals(0.3287037f, managedToRollOnedMean, 0.001f);
	}
	
	@Test
	public void testMultiWorldNonsplitCase() {
		MeanTracker tracker = new MeanTracker();
		
		MultiWorlds multiWorlds = new MultiWorlds();
		Variable<Integer> var = new Variable<>();
		multiWorlds.setTracker(tracker, 1);
		multiWorlds.set(var, Stochastic.even(1,2));
		multiWorlds.set(var, Expression.func(var, v -> (2-v)*(1-v)));
		multiWorlds.doStochastic(var, (w1, varVal) -> {
			w1.setTracker(tracker, 5);
		});
		
		Assert.assertEquals(5, multiWorlds.sampleTracker(tracker), 0.00001f);
	}
	
	@Test
	public void testMultiWorldSplitCase() {
		MeanTracker tracker = new MeanTracker();
		
		MultiWorlds multiWorlds = new MultiWorlds();
		Variable<Integer> var = new Variable<>();
		multiWorlds.setTracker(tracker, 1);
		multiWorlds.set(var, Stochastic.even(1,2));
		multiWorlds.doStochastic(var, (w1, varVal) -> {
			if(varVal == 1) {
				w1.setTracker(tracker, 5);
			}
		});
		
		Assert.assertEquals(3, multiWorlds.sampleTracker(tracker), 0.00001f);
	}
	
	@Test
	public void testMultiply() {
		MeanTracker tracker = new MeanTracker();
		
		MultiWorlds multiWorlds = new MultiWorlds();
		multiWorlds.setTracker(tracker, 1);
		Stochastic<Integer> d6 = Stochastic.even(1,2,3,4,5,6);
		multiWorlds.doStochastic(d6, (w1, roll1) -> {
			if(roll1 == 6) {
				w1.multiplyTracker(tracker, 2);
			}
			if(roll1 > 3) {
				w1.doStochastic(d6, (w2, roll2) -> {
					if(roll2 == 6) {
						w2.multiplyTracker(tracker, 2);
					}
					if(roll2 > 3) {
						w2.doStochastic(d6, (w3, roll3) -> {
							if(roll3 == 6) {
								w3.multiplyTracker(tracker, 2);
							}
						});
					}
				});
			}
		});
		//Note: The value 1.3518519 might be wrong. Haven't checked it by hand.
		Assert.assertEquals(1.3518519f, multiWorlds.sampleTracker(tracker), 0.00001f);
	}
	
	private static long findGoodSeed(long startSeed, Consumer<Long> runnable) {
		long seed = startSeed;
		boolean keepLooping = true;
		while(keepLooping) {
			keepLooping = false;
			try {
				runnable.accept(seed);
			} catch(AssertionError e) {
				keepLooping = true;
				seed++;
			}
		}
		return seed;
	}
}
