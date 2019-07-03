package com.invengo.test.bltrfiddemo.enums;

/**
 * 页面信息
 * Created by 李泽荣 on 2019/7/2.
 */

public enum EmUrl {
	// 主页
	Home("file:///android_asset/web/s01/home.html"),
	Test("file:///android_asset/web/s01/testBle.html"),
	Back("javascript: dat.back();"),
	Err("file:///android_asset/web/s01/err.html");

	private final String url;
	EmUrl(String u) {
		url = u;
	}

	@Override
	public String toString() {
		return url;
	}
}
