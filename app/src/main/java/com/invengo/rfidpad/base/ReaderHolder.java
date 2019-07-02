package com.invengo.rfidpad.base;

import invengo.javaapi.core.IMessage;
import invengo.javaapi.protocol.IRP1.Reader;

import java.util.HashMap;
import java.util.Map;

import com.invengo.lib.system.device.type.DeviceType;

public class ReaderHolder {

	private ReaderHolder(){
		//
		
		loadBatteryIcon();
	}
	
	private void loadBatteryIcon() {
		mIconCache.put(0, com.invengo.rfidpad.R.drawable.stat_sys_battery_0);
		mIconCache.put(1, com.invengo.rfidpad.R.drawable.stat_sys_battery_1);
		mIconCache.put(2, com.invengo.rfidpad.R.drawable.stat_sys_battery_2);
		mIconCache.put(3, com.invengo.rfidpad.R.drawable.stat_sys_battery_3);
		mIconCache.put(4, com.invengo.rfidpad.R.drawable.stat_sys_battery_4);
		mIconCache.put(5, com.invengo.rfidpad.R.drawable.stat_sys_battery_5);
		mIconCache.put(6, com.invengo.rfidpad.R.drawable.stat_sys_battery_6);
		mIconCache.put(7, com.invengo.rfidpad.R.drawable.stat_sys_battery_7);
		mIconCache.put(8, com.invengo.rfidpad.R.drawable.stat_sys_battery_8);
		mIconCache.put(9, com.invengo.rfidpad.R.drawable.stat_sys_battery_9);
		
		mIconCache.put(10, com.invengo.rfidpad.R.drawable.stat_sys_battery_10);
		mIconCache.put(11, com.invengo.rfidpad.R.drawable.stat_sys_battery_11);
		mIconCache.put(12, com.invengo.rfidpad.R.drawable.stat_sys_battery_12);
		mIconCache.put(13, com.invengo.rfidpad.R.drawable.stat_sys_battery_13);
		mIconCache.put(14, com.invengo.rfidpad.R.drawable.stat_sys_battery_14);
		mIconCache.put(15, com.invengo.rfidpad.R.drawable.stat_sys_battery_15);
		mIconCache.put(16, com.invengo.rfidpad.R.drawable.stat_sys_battery_16);
		mIconCache.put(17, com.invengo.rfidpad.R.drawable.stat_sys_battery_17);
		mIconCache.put(18, com.invengo.rfidpad.R.drawable.stat_sys_battery_18);
		mIconCache.put(19, com.invengo.rfidpad.R.drawable.stat_sys_battery_19);
		
		mIconCache.put(20, com.invengo.rfidpad.R.drawable.stat_sys_battery_20);
		mIconCache.put(21, com.invengo.rfidpad.R.drawable.stat_sys_battery_21);
		mIconCache.put(22, com.invengo.rfidpad.R.drawable.stat_sys_battery_22);
		mIconCache.put(23, com.invengo.rfidpad.R.drawable.stat_sys_battery_23);
		mIconCache.put(24, com.invengo.rfidpad.R.drawable.stat_sys_battery_24);
		mIconCache.put(25, com.invengo.rfidpad.R.drawable.stat_sys_battery_25);
		mIconCache.put(26, com.invengo.rfidpad.R.drawable.stat_sys_battery_26);
		mIconCache.put(27, com.invengo.rfidpad.R.drawable.stat_sys_battery_27);
		mIconCache.put(28, com.invengo.rfidpad.R.drawable.stat_sys_battery_28);
		mIconCache.put(29, com.invengo.rfidpad.R.drawable.stat_sys_battery_29);

		mIconCache.put(30, com.invengo.rfidpad.R.drawable.stat_sys_battery_30);
		mIconCache.put(31, com.invengo.rfidpad.R.drawable.stat_sys_battery_31);
		mIconCache.put(32, com.invengo.rfidpad.R.drawable.stat_sys_battery_32);
		mIconCache.put(33, com.invengo.rfidpad.R.drawable.stat_sys_battery_33);
		mIconCache.put(34, com.invengo.rfidpad.R.drawable.stat_sys_battery_34);
		mIconCache.put(35, com.invengo.rfidpad.R.drawable.stat_sys_battery_35);
		mIconCache.put(36, com.invengo.rfidpad.R.drawable.stat_sys_battery_36);
		mIconCache.put(37, com.invengo.rfidpad.R.drawable.stat_sys_battery_37);
		mIconCache.put(38, com.invengo.rfidpad.R.drawable.stat_sys_battery_38);
		mIconCache.put(39, com.invengo.rfidpad.R.drawable.stat_sys_battery_39);

		mIconCache.put(40, com.invengo.rfidpad.R.drawable.stat_sys_battery_40);
		mIconCache.put(41, com.invengo.rfidpad.R.drawable.stat_sys_battery_41);
		mIconCache.put(42, com.invengo.rfidpad.R.drawable.stat_sys_battery_42);
		mIconCache.put(43, com.invengo.rfidpad.R.drawable.stat_sys_battery_43);
		mIconCache.put(44, com.invengo.rfidpad.R.drawable.stat_sys_battery_44);
		mIconCache.put(45, com.invengo.rfidpad.R.drawable.stat_sys_battery_45);
		mIconCache.put(46, com.invengo.rfidpad.R.drawable.stat_sys_battery_46);
		mIconCache.put(47, com.invengo.rfidpad.R.drawable.stat_sys_battery_47);
		mIconCache.put(48, com.invengo.rfidpad.R.drawable.stat_sys_battery_48);
		mIconCache.put(49, com.invengo.rfidpad.R.drawable.stat_sys_battery_49);

		mIconCache.put(50, com.invengo.rfidpad.R.drawable.stat_sys_battery_50);
		mIconCache.put(51, com.invengo.rfidpad.R.drawable.stat_sys_battery_51);
		mIconCache.put(52, com.invengo.rfidpad.R.drawable.stat_sys_battery_52);
		mIconCache.put(53, com.invengo.rfidpad.R.drawable.stat_sys_battery_53);
		mIconCache.put(54, com.invengo.rfidpad.R.drawable.stat_sys_battery_54);
		mIconCache.put(55, com.invengo.rfidpad.R.drawable.stat_sys_battery_55);
		mIconCache.put(56, com.invengo.rfidpad.R.drawable.stat_sys_battery_56);
		mIconCache.put(57, com.invengo.rfidpad.R.drawable.stat_sys_battery_57);
		mIconCache.put(58, com.invengo.rfidpad.R.drawable.stat_sys_battery_58);
		mIconCache.put(59, com.invengo.rfidpad.R.drawable.stat_sys_battery_59);

		mIconCache.put(60, com.invengo.rfidpad.R.drawable.stat_sys_battery_60);
		mIconCache.put(61, com.invengo.rfidpad.R.drawable.stat_sys_battery_61);
		mIconCache.put(62, com.invengo.rfidpad.R.drawable.stat_sys_battery_62);
		mIconCache.put(63, com.invengo.rfidpad.R.drawable.stat_sys_battery_63);
		mIconCache.put(64, com.invengo.rfidpad.R.drawable.stat_sys_battery_64);
		mIconCache.put(65, com.invengo.rfidpad.R.drawable.stat_sys_battery_65);
		mIconCache.put(66, com.invengo.rfidpad.R.drawable.stat_sys_battery_66);
		mIconCache.put(67, com.invengo.rfidpad.R.drawable.stat_sys_battery_67);
		mIconCache.put(68, com.invengo.rfidpad.R.drawable.stat_sys_battery_68);
		mIconCache.put(69, com.invengo.rfidpad.R.drawable.stat_sys_battery_69);

		mIconCache.put(70, com.invengo.rfidpad.R.drawable.stat_sys_battery_70);
		mIconCache.put(71, com.invengo.rfidpad.R.drawable.stat_sys_battery_71);
		mIconCache.put(72, com.invengo.rfidpad.R.drawable.stat_sys_battery_72);
		mIconCache.put(73, com.invengo.rfidpad.R.drawable.stat_sys_battery_73);
		mIconCache.put(74, com.invengo.rfidpad.R.drawable.stat_sys_battery_74);
		mIconCache.put(75, com.invengo.rfidpad.R.drawable.stat_sys_battery_75);
		mIconCache.put(76, com.invengo.rfidpad.R.drawable.stat_sys_battery_76);
		mIconCache.put(77, com.invengo.rfidpad.R.drawable.stat_sys_battery_77);
		mIconCache.put(78, com.invengo.rfidpad.R.drawable.stat_sys_battery_78);
		mIconCache.put(79, com.invengo.rfidpad.R.drawable.stat_sys_battery_79);

		mIconCache.put(80, com.invengo.rfidpad.R.drawable.stat_sys_battery_80);
		mIconCache.put(81, com.invengo.rfidpad.R.drawable.stat_sys_battery_81);
		mIconCache.put(82, com.invengo.rfidpad.R.drawable.stat_sys_battery_82);
		mIconCache.put(83, com.invengo.rfidpad.R.drawable.stat_sys_battery_83);
		mIconCache.put(84, com.invengo.rfidpad.R.drawable.stat_sys_battery_84);
		mIconCache.put(85, com.invengo.rfidpad.R.drawable.stat_sys_battery_85);
		mIconCache.put(86, com.invengo.rfidpad.R.drawable.stat_sys_battery_86);
		mIconCache.put(87, com.invengo.rfidpad.R.drawable.stat_sys_battery_87);
		mIconCache.put(88, com.invengo.rfidpad.R.drawable.stat_sys_battery_88);
		mIconCache.put(89, com.invengo.rfidpad.R.drawable.stat_sys_battery_89);

		mIconCache.put(90, com.invengo.rfidpad.R.drawable.stat_sys_battery_90);
		mIconCache.put(91, com.invengo.rfidpad.R.drawable.stat_sys_battery_91);
		mIconCache.put(92, com.invengo.rfidpad.R.drawable.stat_sys_battery_92);
		mIconCache.put(93, com.invengo.rfidpad.R.drawable.stat_sys_battery_93);
		mIconCache.put(94, com.invengo.rfidpad.R.drawable.stat_sys_battery_94);
		mIconCache.put(95, com.invengo.rfidpad.R.drawable.stat_sys_battery_95);
		mIconCache.put(96, com.invengo.rfidpad.R.drawable.stat_sys_battery_96);
		mIconCache.put(97, com.invengo.rfidpad.R.drawable.stat_sys_battery_97);
		mIconCache.put(98, com.invengo.rfidpad.R.drawable.stat_sys_battery_98);
		mIconCache.put(99, com.invengo.rfidpad.R.drawable.stat_sys_battery_99);

		mIconCache.put(100, com.invengo.rfidpad.R.drawable.stat_sys_battery_100);
	}
	
	public int getBatteryIcon(int key){
		if(null != mIconCache && mIconCache.containsKey(key)){
			return mIconCache.get(key);
		}
		return -1;
	}

	private static ReaderHolder instance = null;
	
	public static synchronized ReaderHolder getInstance(){
		if(null == instance){
			instance  = new ReaderHolder();
		}
		return instance;
	}
	
	private Map<Integer, Integer> mIconCache = new HashMap<Integer, Integer>();
	
	public static final int RFID_CHANNEL_TYPE = 0;
	public static final int BARCODE_CHANNEL_TYPE = 1;
	public static final int IDLE_CHANNEL_TYPE = 2;
	private Reader currentReader;
	private String readerName;
	private boolean isConnected;
	private String deviceName;
	private int channelType;
	
	private DeviceType deviceType = DeviceType.XC2600;
	
	public Reader createBluetoothReader(String readerName, String deviceName){
		this.readerName = readerName;
		this.deviceName = deviceName;
		
		currentReader = new Reader(readerName, "Bluetooth", deviceName);
		return currentReader;
	}

	public Reader getCurrentReader() {
		return currentReader;
	}
	public void setCurrentReader(Reader currentReader) {
		this.currentReader = currentReader;
	}
	public String getReaderName() {
		return readerName;
	}
	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}
	public boolean isConnected() {
		return isConnected;
	}
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public int getChannelType() {
		return channelType;
	}
	public void setChannelType(int channelType) {
		this.channelType = channelType;
	}
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	public void wakeup(){
		if(null != currentReader){
			if(currentReader.wakeUp()){
				this.setConnected(true);
			}
		}
	}
	
	public void sleep(){
		if(null != currentReader){
			currentReader.sleep();
		}
	}

	public void disposeReader(){
		if(null != currentReader){
			currentReader = null;
		}
	}
	
	public void disConnect(){
		if(null != currentReader){
			currentReader.disConnect();
		}
		this.isConnected = false;
	}
	
	public void sendNodificationMessage(IMessage message){
		if(isConnected){
			if(currentReader != null){
				currentReader.send(message);
			}
		}
	}
	
	public IMessage sendMessage(IMessage message){
		if(isConnected){
			if(currentReader != null){
				if(currentReader.send(message)){
					return message;
				}
			}
		}
		return null;
	}
	
}
