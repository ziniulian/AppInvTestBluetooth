package com.invengo.rfidpad.entity;


public class OperationEntity {

	private int imageId;
	private String operationName;
	private Class<?> cls;
	
	public OperationEntity(String operationName){
		this.operationName = operationName;
	}
	
//	public OperationEntity(int imageId, String operationName) {
//		this.imageId = imageId;
//		this.operationName = operationName;
//	}
	
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public Class<?> getCls() {
		return cls;
	}
	public void setCls(Class<?> cls) {
		this.cls = cls;
	}
}
