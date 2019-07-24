package com.invengo.rfidpad.entity;

import java.io.Serializable;

public class BleDeviceEntity implements Serializable {

	private static final long serialVersionUID = 1023021059247324777L;
	private boolean check = false;
	private int rssi;
	private String readerName;
	private String address;
	
	public boolean isCheck() {
		return check;
	}
	public void setCheck(boolean check) {
		this.check = check;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public String getReaderName() {
		return readerName;
	}
	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this){
			return true;
		}
		
		if(!(o instanceof BleDeviceEntity)){
			return false;
		}
		
		BleDeviceEntity entity = (BleDeviceEntity) o;
		
		return (entity.getAddress()).equals(this.address);
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + this.address.hashCode();
		result = result * 31 + this.readerName.hashCode();
		result = result * 31 + this.rssi;
		return result;
	}
}
