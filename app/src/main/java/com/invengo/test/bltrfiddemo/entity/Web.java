package com.invengo.test.bltrfiddemo.entity;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.invengo.test.bltrfiddemo.enums.EmUrl;

import invengo.javaapi.communication.Ble;
import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.handle.IMessageNotificationReceivedHandle;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.Reader;
import tk.ziniulian.util.webapp.WebHd;

import static tk.ziniulian.util.Str.Bytes2Hexstr;

/**
 * 业务接口 不使用JOB库
 * Created by 李泽荣 on 2019/7/2.
 */

public class Web implements IMessageNotificationReceivedHandle {
	private WebHd h;
	private Ble b;
	private Reader rd;

	public Web (WebHd wh, Ble ble) {
		this.h = wh;
		this.b = ble;
	}

	public void  initRd () {
		// 蓝牙连接事件处理
		b.setOpenEvt(new Ble.OnBleOpenEvt() {
			@Override
			public void onOpen(boolean ok) {
				Log.i("---- open ----", "open : " + ok);
				if (ok) {
					// 跳转到连接OK的页面
					h.sendUrl(EmUrl.RfConnected);
				} else {
					// TODO: 2019/7/9 跳转到选择连接的界面
				}
			}
		});

		rd = new Reader("BLE", "CD:48:5F:69:D7:09", b);
		rd.onMessageNotificationReceived.add(this);
	}

	public void open() {
		rd.connect();
	}

	public void close() {
		rd.disConnect();
	}

/*------------------- RFID ---------------------*/

	@Override
	public void messageNotificationReceivedHandle(BaseReader reader, IMessageNotification msg) {
		if (msg instanceof RXD_TagData) {
			Log.i("---", Bytes2Hexstr(msg.getReceivedData()));
		}
	}

	@JavascriptInterface
	public void rfidScan () {
		rd.send(new ReadTag (ReadTag.ReadMemoryBank.EPC_6C));
	}

	@JavascriptInterface
	public void rfidStop () {
		rd.send(new PowerOff());
	}

/*------------------- 数据库 ---------------------*/

}
