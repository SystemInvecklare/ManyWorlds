package com.mattiasselin.libs.multiworld.opt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.mattiasselin.libs.multiworld.Variable;

public class OrderedListVariableValues implements IVariableValues {
	private final List<Entry> entries = new ArrayList<OrderedListVariableValues.Entry>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Variable<T> variable) {
		for(Entry entry : entries) {
			if(entry.variable == variable) {
				return (T) entry.value;
			}
		}
		return null;
	}

	@Override
	public boolean containsKey(Variable<?> variable) {
		for(Entry entry : entries) {
			if(entry.variable == variable) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T put(Variable<T> variable, T value) {
		for(int i = 0, size = entries.size(); i < size; ++i) {
			Entry entry = entries.get(i);
			int cmp = compare(variable, entry.variable);
			if(cmp == 0) {
				Object oldValue = entry.value;
				entry.value = value;
				return (T) oldValue;
			} else if(cmp < 0){
				entries.add(i, new Entry(variable, value));
				return null;
			}
		}
		entries.add(new Entry(variable, value));
		return null;
	}

	private static int compare(Variable<?> a, Variable<?> b) {
		if(a == b) {
			return 0;
		}
		int cmp = Integer.compare(a.hashCode(), b.hashCode());
		if(cmp == 0) {
			cmp = a.toString().compareTo(b.toString());
			if(cmp == 0) {
				cmp = a.getName().compareTo(b.getName());
			}
		}
		return cmp;
	}

	@Override
	public void setTo(IVariableValues variableValues) {
		OrderedListVariableValues listVariableValues = (OrderedListVariableValues) variableValues;
		if(!entries.isEmpty()) {
			entries.clear();
		}
		for(Entry entry : listVariableValues.entries) {
			entries.add(new Entry(entry.variable, entry.value));
		}
	}

	@Override
	public boolean remove(Variable<?> variable) {
		Iterator<Entry> iterator = entries.iterator();
		while(iterator.hasNext()) {
			Entry entry = iterator.next();
			if(entry.variable == variable) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return entries.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof OrderedListVariableValues ? equalsOwn((OrderedListVariableValues) obj) : false;
	}

	public boolean equalsOwn(OrderedListVariableValues other) {
		if(entries.size() != other.entries.size()) {
			return false;
		}
		Iterator<Entry> iterator = entries.iterator();
		Iterator<Entry> otherIterator = other.entries.iterator();
		while(iterator.hasNext() && otherIterator.hasNext()) {
			Entry entry = iterator.next();
			Entry otherEntry = otherIterator.next();
			if(entry.variable != otherEntry.variable) { //TODO maybe first compare all existing variables? (i.e. check that both lists have the same variables first)
				return false;
			}
			if(!Objects.equals(entry.value, otherEntry.value)) {
				return false;
			}
		}
		if(iterator.hasNext() || otherIterator.hasNext()) {
			return false;
		}
		return true;
	}

	private static class Entry {
		private final Variable<?> variable;
		private Object value;
		
		public Entry(Variable<?> variable, Object value) {
			this.variable = variable;
			this.value = value;
		}
		
		@Override
		public int hashCode() {
			return variable.hashCode() ^ (31*(value == null ? 0 : value.hashCode()));
		}
	}
}
