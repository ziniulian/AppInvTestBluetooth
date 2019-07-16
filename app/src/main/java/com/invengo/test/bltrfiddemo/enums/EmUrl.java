package com.invengo.test.bltrfiddemo.enums;

/**
 * 页面信息
 * Created by 李泽荣 on 2019/7/2.
 */

public enum EmUrl {
	// 主页
	Home("file:///android_asset/web/s01/home.html"),
	Err("file:///android_asset/web/s01/err.html"),
	Back("javascript: dat.back();"),

	// RFID
	RfScaning("javascript: rfid.scan();"),
	RfStoped("javascript: rfid.stop();"),
	RfWrtOk("javascript: rfid.hdWrt(true);"),
	RfWrtErr("javascript: rfid.hdWrt(false);"),
	RfConnectErr("file:///android_asset/web/s01/link.html"),		// 跳转到选择连接的界面
	RfConnected("file:///android_asset/web/s01/testBle.html"),		// 跳转到连接成功的页面
	RfDisConnected("file:///android_asset/web/s01/link.html"),		// 跳转到选择连接的界面

	Linking("file:///android_asset/web/s01/linking.html"),
	BleScanDev("javascript: rfid.bleScanDev(<0>);"),
	BleScanEnd("javascript: rfid.bleScanEnd();"),
	;

	private final String url;
	EmUrl(String u) {
		url = u;
	}

	@Override
	public String toString() {
		return url;
	}
}
