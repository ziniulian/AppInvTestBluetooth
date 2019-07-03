package com.invengo.test.bltrfiddemo.entity;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.invengo.test.bltrfiddemo.Ma;

/**
 * 业务接口
 * Created by 李泽荣 on 2019/7/2.
 */

public class Web {
	private Ma ma;
	private TestBlt tb;

	public Web (Ma m) {
		this.ma = m;
		this.tb = new TestBlt(m);	// 蓝牙BLE测试
	}

	public void testOpen () {
		if (tb.init()) {
			tb.scanDevice();
		}
	}

	public void testClose () {
		tb.close();
	}

	@JavascriptInterface
	public void testWrt (String hex) {
		tb.wrt(hex);
	}

/*------------------- RFID ---------------------*/

/*------------------- 数据库 ---------------------*/

/*------------------- 其它 ---------------------*/

	@JavascriptInterface
	public void log(String msg) {
		Log.i("---- Web ----", msg);
	}

}
