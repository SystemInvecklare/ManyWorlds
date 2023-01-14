package com.mattiasselin.libs.multiworld.worlds;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.mattiasselin.libs.multiworld.expression.Constant;
import com.mattiasselin.libs.multiworld.expression.Expression;
import com.mattiasselin.libs.multiworld.expression.Stochastic;
import com.mattiasselin.libs.multiworld.expression.Variable;

public class WorldTest {
	@Test
	public void testVariables() {
		MultiWorlds multiWorlds = new MultiWorlds();
		Variable<Integer> var = new Variable<>();
		multiWorlds.doStochastic(Stochastic.even(1,2,3,4,5), (w, val) -> {
			w.setConst(var, val);
			if(val >= 4) {
				w.doStochastic(Stochastic.even(1,3), (w2, mul) -> {
					w2.set(var, Expression.Int.mult(var, new Constant<>(mul)));
				});
			}
		});
		Map<Integer, String> probs = new HashMap<Integer, String>();
		multiWorlds.splitOn(var, (w, val) -> {
			probs.put(val, w.getTotalProbability().toString());
		});
		Map<Integer, String> expected = new HashMap<>();
		expected.put(1, "20.0%");
		expected.put(2, "20.0%");
		expected.put(3, "20.0%");
		expected.put(4, "10.0%");
		expected.put(5, "10.0%");
		expected.put(12, "10.0%");
		expected.put(15, "10.0%");
		Assert.assertEquals(expected, probs);
	}
}
