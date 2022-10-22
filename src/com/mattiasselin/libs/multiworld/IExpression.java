package com.mattiasselin.libs.multiworld;

public interface IExpression<T> {
	T eval(IWorldState worldState);
}
