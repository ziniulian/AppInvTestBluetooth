package com.invengo.rfidpad.scan;

import com.invengo.rfidpad.utils.Contants;


public class TagScanSettingsCollection {

	private boolean isAntennaOne = true;
	private int q = 3;
	private boolean loop = true;
	private boolean voiced = true;//提示音
	private boolean rssi = false;
	private boolean utc = false;

	/*
	 * 标签读取类型
	 */
	private int Visibility_6C = Contants.CHECKED;
	private int Visibility_6B = Contants.UNCHECKED;
	private int Visibility_6C_6B = Contants.UNCHECKED;
	private int Visibility_GB = Contants.UNCHECKED;

	/*
	 * 以下为Visibility_6C时配置参数
	 */
	private int epcChecked = Contants.CHECKED;
	private int tidChecked = Contants.UNCHECKED;
	private int userdataChecked = Contants.UNCHECKED;

	private int tidLen = 6;
	private int userDataAddress = 0;
	private int userDataLen = 8;

	/*
	 * 以下为Visibility_6B时配置参数
	 */
	private int id6BChecked = Contants.CHECKED;
	private int userdata6BChecked = Contants.UNCHECKED;

	private int tidLen6B = 6;
	private int userDataAddress6B = 0;
	private int userDataLen6B = 8;

	/*
	 * 以下为Visibility_6C_6B时配置参数
	 */
	private int tidLen6C6B = 6;
	private int userDataAddress6C6B = 0;
	private int userDataLen6C6B = 8;

	/*
	 * 一下为GB_Inventory时配置参数
	 */
	private int gbInventoryChecked = Contants.CHECKED;
	private int target = 0;
	private int session = 0;
	private int condition = 0;

	/*
	 * 一下为GB_Access_Read时配置参数，默认持续读卡
	 */
	private int gbAccessReadChecked = Contants.UNCHECKED;
	private String gbPassword = "00000000";
	private int gbAddress = 0;
	private int gbLen = 8;
	//	private int gbBank = 0;
	private boolean gbEpc = true;
	private boolean gbTid = false;
	private boolean gbUserdata = false;
	private int gbUserdataNo = -1;//用户区编号


	/*
	 * 一下为GB_Combination_Read时配置参数，默认持续读卡
	 */
	private int gbCombinationReadChecked = Contants.UNCHECKED;
	private String gbTidPassword = "00000000";
	private int gbTidLen = 12;


	/*
	 * 一下为GB_Read_All时配置参数
	 */
	private int gbAllReadChecked = Contants.UNCHECKED;
	private int gbAllTidLen = 8;
	private String gbAllTidPassword = "00000000";
	private int gbAllEpcLen = 6;
	private int gbAllUserdataNo = 0;
	private int gbAllUserdataAddress = 4;
	private int gbAllUserdataLen = 6;
	private String gbAllUserdataPassword = "00000000";


	private TagScanSettingsCollection(){
		//
	}

	private static TagScanSettingsCollection instance = null;

	public static synchronized TagScanSettingsCollection getInstance(){
		if(null == instance){
			instance  = new TagScanSettingsCollection();
		}
		return instance;
	}

	/**
	 * 退出
	 */
	public static synchronized void clearSettings(){
		if(null != instance){
			instance  = null;
		}
	}

	public int getAntenna(){
		return Integer.parseInt("1000000" + (this.isAntennaOne() ? "1" : "0"), 2);
	}

	public boolean isAntennaOne() {
		return isAntennaOne;
	}
	public void setAntennaOne(boolean isAntennaOne) {
		this.isAntennaOne = isAntennaOne;
	}
	public int getQ() {
		return q;
	}
	public void setQ(int q) {
		this.q = q;
	}
	public boolean isLoop() {
		return loop;
	}
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	public boolean isVoiced() {
		return voiced;
	}
	public void setVoiced(boolean voiced) {
		this.voiced = voiced;
	}
	public int getVisibility_6C() {
		return Visibility_6C;
	}
	public void setVisibility_6C(int visibility_6c) {
		Visibility_6C = visibility_6c;
	}
	public int getEpcChecked() {
		return epcChecked;
	}
	public void setEpcChecked(int epcChecked) {
		this.epcChecked = epcChecked;
	}
	public int getTidChecked() {
		return tidChecked;
	}
	public void setTidChecked(int tidChecked) {
		this.tidChecked = tidChecked;
	}
	public int getUserdataChecked() {
		return userdataChecked;
	}
	public void setUserdataChecked(int userdataChecked) {
		this.userdataChecked = userdataChecked;
	}
	public int getTidLen() {
		return tidLen;
	}
	public void setTidLen(int tidLen) {
		this.tidLen = tidLen;
	}
	public int getUserDataAddress() {
		return userDataAddress;
	}
	public void setUserDataAddress(int userDataAddress) {
		this.userDataAddress = userDataAddress;
	}
	public int getUserDataLen() {
		return userDataLen;
	}
	public void setUserDataLen(int userDataLen) {
		this.userDataLen = userDataLen;
	}
	public int getVisibility_6B() {
		return Visibility_6B;
	}
	public void setVisibility_6B(int visibility_6b) {
		Visibility_6B = visibility_6b;
	}
	public int getId6BChecked() {
		return id6BChecked;
	}
	public void setId6BChecked(int id6bChecked) {
		id6BChecked = id6bChecked;
	}
	public int getUserdata6BChecked() {
		return userdata6BChecked;
	}
	public void setUserdata6BChecked(int userdata6bChecked) {
		userdata6BChecked = userdata6bChecked;
	}
	public int getTidLen6B() {
		return tidLen6B;
	}
	public void setTidLen6B(int tidLen6B) {
		this.tidLen6B = tidLen6B;
	}
	public int getUserDataAddress6B() {
		return userDataAddress6B;
	}
	public void setUserDataAddress6B(int userDataAddress6B) {
		this.userDataAddress6B = userDataAddress6B;
	}
	public int getUserDataLen6B() {
		return userDataLen6B;
	}
	public void setUserDataLen6B(int userDataLen6B) {
		this.userDataLen6B = userDataLen6B;
	}
	public int getVisibility_6C_6B() {
		return Visibility_6C_6B;
	}
	public void setVisibility_6C_6B(int visibility_6c_6b) {
		Visibility_6C_6B = visibility_6c_6b;
	}
	public int getVisibility_GB() {
		return Visibility_GB;
	}
	public void setVisibility_GB(int visibility_GB) {
		Visibility_GB = visibility_GB;
	}
	public int getTidLen6C6B() {
		return tidLen6C6B;
	}
	public void setTidLen6C6B(int tidLen6C6B) {
		this.tidLen6C6B = tidLen6C6B;
	}
	public int getUserDataAddress6C6B() {
		return userDataAddress6C6B;
	}
	public void setUserDataAddress6C6B(int userDataAddress6C6B) {
		this.userDataAddress6C6B = userDataAddress6C6B;
	}
	public int getUserDataLen6C6B() {
		return userDataLen6C6B;
	}
	public void setUserDataLen6C6B(int userDataLen6C6B) {
		this.userDataLen6C6B = userDataLen6C6B;
	}
	public boolean isRssi() {
		return rssi;
	}
	public void setRssi(boolean rssi) {
		this.rssi = rssi;
	}
	public boolean isUtc() {
		return utc;
	}
	public void setUtc(boolean utc) {
		this.utc = utc;
	}
	public int getGbInventoryChecked() {
		return gbInventoryChecked;
	}
	public void setGbInventoryChecked(int gbInventoryChecked) {
		this.gbInventoryChecked = gbInventoryChecked;
	}
	public int getTarget() {
		return target;
	}
	public void setTarget(int target) {
		this.target = target;
	}
	public int getSession() {
		return session;
	}
	public void setSession(int session) {
		this.session = session;
	}
	public int getCondition() {
		return condition;
	}
	public void setCondition(int condition) {
		this.condition = condition;
	}
	public int getGbAccessReadChecked() {
		return gbAccessReadChecked;
	}
	public void setGbAccessReadChecked(int gbAccessReadChecked) {
		this.gbAccessReadChecked = gbAccessReadChecked;
	}
	public String getGbPassword() {
		return gbPassword;
	}
	public void setGbPassword(String gbPassword) {
		this.gbPassword = gbPassword;
	}
	public int getGbAddress() {
		return gbAddress;
	}
	public void setGbAddress(int gbAddress) {
		this.gbAddress = gbAddress;
	}
	public int getGbLen() {
		return gbLen;
	}
	public void setGbLen(int gbLen) {
		this.gbLen = gbLen;
	}
	public boolean isGbEpc() {
		return gbEpc;
	}
	public void setGbEpc(boolean gbEpc) {
		this.gbEpc = gbEpc;
	}
	public boolean isGbTid() {
		return gbTid;
	}
	public void setGbTid(boolean gbTid) {
		this.gbTid = gbTid;
	}
	public boolean isGbUserdata() {
		return gbUserdata;
	}
	public void setGbUserdata(boolean gbUserdata) {
		this.gbUserdata = gbUserdata;
	}
	public int getGbUserdataNo() {
		return gbUserdataNo;
	}
	public void setGbUserdataNo(int gbUserdataNo) {
		this.gbUserdataNo = gbUserdataNo;
	}
	public int getGbCombinationReadChecked() {
		return gbCombinationReadChecked;
	}
	public void setGbCombinationReadChecked(int gbCombinationReadChecked) {
		this.gbCombinationReadChecked = gbCombinationReadChecked;
	}
	public String getGbTidPassword() {
		return gbTidPassword;
	}
	public void setGbTidPassword(String gbTidPassword) {
		this.gbTidPassword = gbTidPassword;
	}
	public int getGbTidLen() {
		return gbTidLen;
	}
	public void setGbTidLen(int gbTidLen) {
		this.gbTidLen = gbTidLen;
	}
	public int getGbAllReadChecked() {
		return gbAllReadChecked;
	}
	public void setGbAllReadChecked(int gbAllReadChecked) {
		this.gbAllReadChecked = gbAllReadChecked;
	}
	public int getGbAllTidLen() {
		return gbAllTidLen;
	}
	public void setGbAllTidLen(int gbAllTidLen) {
		this.gbAllTidLen = gbAllTidLen;
	}
	public String getGbAllTidPassword() {
		return gbAllTidPassword;
	}
	public void setGbAllTidPassword(String gbAllTidPassword) {
		this.gbAllTidPassword = gbAllTidPassword;
	}
	public int getGbAllEpcLen() {
		return gbAllEpcLen;
	}
	public void setGbAllEpcLen(int gbAllEpcLen) {
		this.gbAllEpcLen = gbAllEpcLen;
	}
	public int getGbAllUserdataNo() {
		return gbAllUserdataNo;
	}
	public void setGbAllUserdataNo(int gbAllUserdataNo) {
		this.gbAllUserdataNo = gbAllUserdataNo;
	}
	public int getGbAllUserdataAddress() {
		return gbAllUserdataAddress;
	}
	public void setGbAllUserdataAddress(int gbAllUserdataAddress) {
		this.gbAllUserdataAddress = gbAllUserdataAddress;
	}
	public int getGbAllUserdataLen() {
		return gbAllUserdataLen;
	}
	public void setGbAllUserdataLen(int gbAllUserdataLen) {
		this.gbAllUserdataLen = gbAllUserdataLen;
	}
	public String getGbAllUserdataPassword() {
		return gbAllUserdataPassword;
	}
	public void setGbAllUserdataPassword(String gbAllUserdataPassword) {
		this.gbAllUserdataPassword = gbAllUserdataPassword;
	}
}
