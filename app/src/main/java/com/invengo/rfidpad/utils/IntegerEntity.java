package com.invengo.rfidpad.utils;

import com.invengo.rfidpad.entity.FrequencyBandEntity;

public class IntegerEntity {

	private int index;
	private FrequencyBandEntity entity;
	
	public IntegerEntity(int index, FrequencyBandEntity entity) {
		this.index = index;
		this.entity = entity;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public FrequencyBandEntity getEntity() {
		return entity;
	}

	public void setEntity(FrequencyBandEntity entity) {
		this.entity = entity;
	}

	@Override
	public String toString() {
		return this.entity.getName();
	}
	
}
