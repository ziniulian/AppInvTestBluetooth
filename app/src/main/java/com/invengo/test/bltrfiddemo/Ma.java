package com.invengo.test.bltrfiddemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.invengo.test.bltrfiddemo.enums.EmUh;
import com.invengo.test.bltrfiddemo.enums.EmUrl;

import invengo.javaapi.communication.Ble;
import tk.ziniulian.job.inv.webapp.WebiRfidBle;
import tk.ziniulian.util.EnumMgr;
import tk.ziniulian.util.webapp.WebHd;
import tk.ziniulian.util.webapp.WebiUtil;

public class Ma extends Activity {
	private WebView wv;
	private WebHd h = new WebHd();
	private Ble b = new Ble(this);
//	private Web w = new Web(h, b);
	private WebiRfidBle w = new WebiRfidBle(h, b);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ma);

		wv = (WebView)findViewById(R.id.wv);
		init();

		// 防止应用奔溃
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.i ("--- Crash ---", "-- S!");
				e.printStackTrace();
				Log.i ("--- Crash ---", "-- E!");
			}
		});
	}

	// 初始化
	private void init () {
		// 枚举设置
		EnumMgr.add(EmUh.class);
		EnumMgr.add(EmUrl.class);

		// 页面设置
		h.initWv(wv);
		wv.addJavascriptInterface(w, "LwaRfdo");	// 同名的不同对象会被后者覆盖，不同名的不同对象可以叠加使用。
		wv.addJavascriptInterface(new WebiUtil(), "LwaUtil");

		h.sendUrl(EmUrl.Home);	// 开机页面

		// 蓝牙开启事件的处理，主要处理页面跳转。
		b.setCheckEvt(new Ble.OnBleCheckEvt() {
			@Override
			public void onCheck(boolean ok) {
				if (ok) {
					h.sendUrl(EmUrl.RfConnectErr);
				} else {
					h.sendUrl(EmUrl.Err);
				}
			}
		});

		// 初始化RFID模块
		w.initRd();

		// 开始检查蓝牙设备
		b.check();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		b.bldActCb(requestCode, resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		w.close(); // 蓝牙每次连接，耗时较长，少则一两秒，多则十几秒，连接不稳定，故不适合在 onPause 中断开。所以只在 onDestroy 时才断开连接，只要程序不退出，就尽量不断开连接。
//		w.closeDb();
		super.onDestroy();
	}

	// 按键处理
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				EmUrl e = (EmUrl)h.getCurUi();
				if (e != null) {
					switch (e) {
						case Home:
						case Err:
						case RfConnectErr:
							return super.onKeyDown(keyCode, event);
						default:
							h.sendUrl(EmUrl.Back);
							break;
					}
				} else {
					wv.goBack();
				}
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

}
