package com.mattiasselin.libs.multiworld;

import java.util.Objects;

import com.mattiasselin.libs.multiworld.opt.IVariableValues;
import com.mattiasselin.libs.multiworld.opt.OrderedListVariableValues;

public class WorldState implements IWorldState {
	private final IVariableValues variableValues = new OrderedListVariableValues();
	private int cachedHash;
	private boolean cachedHashDirty = true;
	private Variable<?> latestChanged = null;

	@Override
	public <T> T getValue(Variable<T> variable) {
		T value = variableValues.get(variable);
		if(value == null && !variableValues.containsKey(variable)) {
			throw new IllegalArgumentException("Variable "+variable.getName()+" does not exist in this WorldState! (uninitialized or forgotten)");
		}
		return value;
	}
	
	public <T> void setValue(Variable<T> variable, T value) {
		Object oldValue = variableValues.put(variable, value);
		if(oldValue != value) {
			cachedHashDirty = true;
			latestChanged = variable;
		}
	}
	
	public WorldState copy() {
		WorldState worldState = new WorldState();
		worldState.variableValues.setTo(this.variableValues);
		worldState.cachedHash = this.cachedHash;
		worldState.cachedHashDirty = this.cachedHashDirty;
		return worldState;
	}
	
	@Override
	public int hashCode() {
		if(cachedHashDirty) {
			cachedHash = variableValues.hashCode();
			cachedHashDirty = false;
		}
		return cachedHash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof WorldState) {
			return equalsOwn((WorldState) obj);
		}
		return false;
	}

	private boolean equalsOwn(WorldState other) {
		//TODO maybe we can have a changenumber, cache worldstate+changenumber that we are equal to, and reuse?
		if(this == other) {
			return true;
		}
		if(this.latestChanged == other.latestChanged) {
			if(this.latestChanged != null) {
				Object thisLatestValue = this.variableValues.get(this.latestChanged);
				Object otherLatestValue = other.variableValues.get(this.latestChanged);
				if(!Objects.equals(thisLatestValue, otherLatestValue)) {
					return false;
				}
			}
		}
		return this.variableValues.equals(other.variableValues);
	}

	public void forget(Variable<?> variable) {
		if(variableValues.remove(variable)) {
			cachedHashDirty = true;
		}
	}
}
