package com.invengo.test.bltrfiddemo.entity;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.invengo.test.bltrfiddemo.Ma;
import com.invengo.test.bltrfiddemo.enums.EmUrl;

import tk.ziniulian.util.communication.Blutos.BlutosDev;
import tk.ziniulian.util.communication.Blutos.BlutosLE;
import tk.ziniulian.util.communication.Blutos.EmBlutos;
import tk.ziniulian.util.communication.Blutos.InfBlutosEvt;

import static tk.ziniulian.util.Str.Bytes2Hexstr;

/**
 * 业务接口
 * Created by 李泽荣 on 2019/7/2.
 */

public class Web {
	private Ma ma;
	private BlutosLE ble;

	public Web (Ma m) {
		this.ma = m;
	}

	public void  initBLE (BlutosLE b) {
		this.ble = b;
		InfBlutosEvt e = new InfBlutosEvt() {
			@Override
			public void onBldOk(BlutosLE self) {
				Log.i("----- 1. onBldOk -----", "onBldOk");

				BluetoothDevice d = ble.getDev("CD:48:5F:69:D7:09");
				ble.connectDevice(ma, d);

//				ble.scanDevice();
			}

			@Override
			public void onErr(BlutosLE self, EmBlutos msg) {
				Log.i("----- 2. onErr -----", msg.toString());
			}

			@Override
			public void onScanBegin(BlutosLE self) {
				Log.i("--- 3. onScanBegin ---", "onScanBegin");
			}

			@Override
			public void onScanOne(BlutosLE self, BlutosDev dev) {
				Log.i("----- 4.onScanOne -----", dev.getD().getAddress() + " , " + dev.getD().getBondState());
			}

			@Override
			public void onScanEnd(BlutosLE self) {
				Log.i("--- 5. onScanEnd ---", self.jsonScanDevices());
			}

			@Override
			public void onConnectBegin(BlutosLE self) {
				Log.i("-- 6. onConnectBegin --", "onConnectBegin");
			}

			@Override
			public void onConnected(BlutosLE self) {
				Log.i("---- 9.onConnected ----", "onConnected");
				ma.sendUrl(EmUrl.Test);
			}

			@Override
			public void onDisConnected(BlutosLE self) {
				Log.i("-- 10.onDisConnected --", "onDisConnected");
			}

			@Override
			public void onReceive(BlutosLE self, byte[] dat) {
				Log.i("---- 12. onReceive ----", Bytes2Hexstr(dat));
			}
		};
		b.setEvt(e).bld(ma);
	}

	@JavascriptInterface
	public void testWrt (String hex) {
		ble.wrt(hex);
	}

/*------------------- RFID ---------------------*/

/*------------------- 数据库 ---------------------*/

/*------------------- 其它 ---------------------*/

	@JavascriptInterface
	public void log(String msg) {
		Log.i("---- Web ----", msg);
	}

}
