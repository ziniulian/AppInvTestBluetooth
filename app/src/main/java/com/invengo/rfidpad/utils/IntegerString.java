package com.invengo.rfidpad.utils;

public class IntegerString {

	private int index;
	private String name;
	
	public IntegerString(int index, String name) {
		this.index = index;
		this.name = name;
	}
	public int getIndex() {
		return index;
	}
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
