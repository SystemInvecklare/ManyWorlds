# ManyWorlds

The `ManyWorlds` library provides a way to make complex and exact (as opposed to brute force sampling) probability calculations in an imperative way.

The same code that computes the probability distribution can also be used to run a single simulation.

## Conversion example

Consider the following:
```java
Random rand = new Random();
int value = rand.nextInt(7)+1;
int moneys = rand.nextInt(3);
if(value == 1) {
	moneys += 5;
} else if(value == 7) {
	moneys *= 2;
} else {
	moneys += 2*value - 1;
}
System.out.println("You have "+moneys+" money");
```
* What is the possible values of moneys?
* What is the maximum?
* What is the expected value?

Instead of executing the code multiple times and getting approximative answers to these questions we can reformulate it using the `ManyWorlds` library:
```java
public static void runCalculation(IWorlds worlds) {
	Variable<Integer> value = worlds.set(new Variable<>(), Stochastic.intRange(1,7));
	Variable<Integer> moneys = worlds.set(new Variable<>(), Stochastic.intRange(0,2));
	worlds.ifTrue(Expression.Int.equalTo(value, 1), then_ -> {
		then_.set(moneys, Expression.func(moneys, m -> m + 5));
	}, else_ -> {
		else_.ifTrue(Expression.Int.equalTo(value, 7), then2_ -> {
			then2_.set(moneys, Expression.func(moneys, m -> 2*m));
		}, else2_ -> {
			else2_.set(moneys, Expression.func(moneys, value, (m,v) -> m + 2*v - 1));
		});
	});
	worlds.splitOn(moneys, (world, moneysValue) -> {
		if(world.getTotalProbability() == Probability.CERTAIN) {
			System.out.println("You have "+moneysValue+" money");
		} else {
			System.out.println("In "+world.getTotalProbability()+" of worlds you have "+moneysValue+" money");
		}
	}, IWorlds.ORDER_BY_MOST_LIKELY);
}
```
If we use call this method using `runCalculation(new SingleWorld())` we will get the same output as before, with the same probability. But if we run it with `runCalculation(new MultiWorlds())` we get the following:
```
In 14.285714% of worlds you have 5 money
In 14.285714% of worlds you have 7 money
In 9.523809% of worlds you have 4 money
In 9.523809% of worlds you have 6 money
In 9.523809% of worlds you have 9 money
In 9.523809% of worlds you have 11 money
In 4.7619047% of worlds you have 0 money
In 4.7619047% of worlds you have 2 money
In 4.7619047% of worlds you have 3 money
In 4.7619047% of worlds you have 8 money
In 4.7619047% of worlds you have 10 money
In 4.7619047% of worlds you have 12 money
In 4.7619047% of worlds you have 13 money
```

Now let use see how we would write a snippet that answers the questions from before.

First we edit the `runCalculation` method slightly: 
1. Add a parameter for the moneys Variabel so we can reference it from outside.
2. Remove the printing part at the end

```java
public static void runCalculation(IWorlds worlds, Variable<Integer> moneys) {
	Variable<Integer> value = worlds.set(new Variable<>(), Stochastic.intRange(1,7));
	worlds.set(moneys, Stochastic.intRange(0,2));
	worlds.ifTrue(Expression.Int.equalTo(value, 1), then_ -> {
		then_.set(moneys, Expression.func(moneys, m -> m + 5));
	}, else_ -> {
		else_.ifTrue(Expression.Int.equalTo(value, 7), then2_ -> {
			then2_.set(moneys, Expression.func(moneys, m -> 2*m));
		}, else2_ -> {
			else2_.set(moneys, Expression.func(moneys, value, (m,v) -> m + 2*v - 1));
		});
	});
}
```
Then we just run this snippet:
```java
Variable<Integer> moneys = new Variable<>();
		
MultiWorlds worlds = new MultiWorlds();
runCalculation(worlds, moneys);

List<Integer> possibleValues = new ArrayList<>();
final int[] maximumValuePointer = new int[] {Integer.MIN_VALUE};
final float[] expectedValuePointer = new float[] {0};
worlds.splitOn(moneys, (splitWorld, moneysValue) -> {
	possibleValues.add(moneysValue);
	maximumValuePointer[0] = Math.max(maximumValuePointer[0], moneysValue);
	expectedValuePointer[0] += moneysValue*splitWorld.getTotalProbability().getPercentChance()/100;
});
Collections.sort(possibleValues);
System.out.println("Possible moneys values: "+possibleValues);
System.out.println("Max moneys: "+maximumValuePointer[0]);
System.out.println("Expected moneys: "+expectedValuePointer[0]);
```
and we get the answers:
```
Possible moneys values: [0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
Max moneys: 13
Expected moneys: 6.857143
```

## Basics

### IWorlds
A `IWorlds` represents an abstraction for one or many worlds where code can be executed. If it is a `SingleWorld`, code will execute as normal. If is `MultiWorlds` certain 'stochastic' expressions may 'split' the worlds.

### IWorldState
A `IWorldState` contains the current state of a world. This state is comprised by a set of `Variable`s known to the world and their values in that world.

### WorldTerm
A `WorldTerm` is a `WorldState` together with a `Probability`.

### IStochasticExpression<T>
A `IStochasticExpression<T>` is an expression that may 'split' worlds. For example, the `IStochasticExpression<Integer>`  `Stochastic.intRange(1,3)`, will split every world into 3 equally likely parts and evaluate to 1,2 and 3 respectively in each part.

##### Example
```java
IWorlds worlds = new MultiWorlds(); //Initially the MultiWorld only contains one WorldTerm with 100% probability
IStochasticExpression<Integer> exp = Stochastic.intRange(1,3);
Variable<Integer> oneTwoOrThree = new Variable<>();
worlds.set(oneTwoOrThree, exp); //To set the value of oneTwoOrThree, exp is evaluated
//While exp is evaluated the MultiWorld gets 'split' into three WorldTerms,
//One with probability 33.33333% and a `WorldState` where exp is equal to 1
//One with probability 33.33333% and a `WorldState` where exp is equal to 2
//And one with probability 33.33333% and a `WorldState` where exp is equal to 3
```

### Expression<T>
An `Expression<T>` is a deterministic `IStochasticExpression<T>` (i.e. it does NOT split the worlds). `Expression<T>` can be evaluated for a `IWorldState` using `Expression.evalExpression(someExpression, worldState)`, but please note that `evalExpression` will throw an `IllegalArgumentException` if `someExpression` is not an instance of `Expression`!

## Short Example
```java
import com.mattiasselin.libs.multiworld.expression.Expression;
import com.mattiasselin.libs.multiworld.expression.IStochasticExpression;
import com.mattiasselin.libs.multiworld.expression.Stochastic;
import com.mattiasselin.libs.multiworld.expression.Variable;
import com.mattiasselin.libs.multiworld.worlds.IWorlds;
import com.mattiasselin.libs.multiworld.worlds.MultiWorlds;
import com.mattiasselin.libs.multiworld.worlds.SingleWorld;

public class Example {
	public static void main(String[] args) {
		runScenario(new SingleWorld());
		
		System.out.println();
		System.out.println("Actual distribution:");
		
		runScenario(new MultiWorlds());
	}

	private static void runScenario(IWorlds worlds) {
		Stochastic<Integer> D20 = Stochastic.intRange(1, 20);
		Stochastic<Integer> D6 = Stochastic.intRange(1, 6);
		
		Stochastic<Integer> _2D6 = Stochastic.precompute(D6, D6, (a,b) -> a+b); //Value of rolling two six-sided dice
		
		//Roll a six-sided die and the health to "result + 3" 
		Variable<Integer> health = worlds.set(new Variable<>("health"), Expression.func(D6, d6Value -> d6Value + 3));
		
		//Define expression alive as 'health > 0'
		IStochasticExpression<Boolean> alive = Expression.Int.greaterThan(health, 0); 
		
		//If health greater than 5
		worlds.ifTrue(Expression.Int.greaterThan(health, 5), then -> {
			//Critters run away in fear!
			
		}, else_ -> {
			//Fight racoon!
			
			//Set racoon health to random value between 3 and 7
			Variable<Integer> racoonHealth = else_.set(new Variable<>(), Stochastic.intRange(3, 7));
			
			//Define a stochastic expression for the racoon attack:
			//Roll a D20. If less than or equal to 3 it's critical.
			//Roll for base damage in rage [1,3]. If it was critical, double and add 2
			Stochastic<Integer> racoonAttack = Stochastic.precompute(Stochastic.precompute(D20, d20Value -> d20Value <= 3), Stochastic.intRange(1, 3), (criticalHit, baseAttack) -> criticalHit ? 2+baseAttack*2 : baseAttack);
			
			//While player alive and racoonHealth > 0
			else_.whileLoop(Expression.Boolean_.and(alive, Expression.Int.greaterThan(racoonHealth, 0)), racoonWorld -> {
				//Racoon strikes
				racoonWorld.set(health, Expression.func(health, racoonAttack, (h, dmg) -> Math.max(0, h - dmg)));
				
				//If we are still alive
				racoonWorld.ifTrue(alive, retaliateWorld -> {
					//Strike racoon! Roll 2 six-sided die and subtract that from racoonHealth (don't go under 0) 
					retaliateWorld.set(racoonHealth, Expression.func(racoonHealth, _2D6, (rh, playerDmg) -> Math.max(0, rh - playerDmg)));
				});
			});
			
			//Forget variable racoonHealth
			else_.forget(racoonHealth);
		});
		
		//store value of expression 'alive' in 'survived' variable.
		Variable<Boolean> survived = worlds.set(new Variable<>(), alive);
		
		//Forget the variable health
		worlds.forget(health);
		
		worlds.splitOn(survived, (surviveWorld, didSurvive) -> {
			//Print distribution of worlds where player survived and where player died 
			System.out.println(surviveWorld.getTotalProbability() + " -> "+((didSurvive) ? "You survived" : "You died"));
		}, IWorlds.ORDER_BY_MOST_LIKELY);
	}
}
```
Outputs:
```
100% -> You survived
```
93.05388% of the times, and
```
100% -> You died
```
6.9461117% of the times.

But always followed by:
```
Actual distribution:
93.05388% -> You survived
6.9461117% -> You died
```

## Trackers

Trackers are like variables, with one very important distinction: *they may not alter the simulation*. They may only be read after (using `sampleTracker` on a MultiWorlds or SingleWorld).