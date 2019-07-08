package com.invengo.test.bltrfiddemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.invengo.test.bltrfiddemo.entity.Web;
import com.invengo.test.bltrfiddemo.enums.EmUh;
import com.invengo.test.bltrfiddemo.enums.EmUrl;

import tk.ziniulian.util.Str;
import tk.ziniulian.util.communication.Blutos.BlutosLE;

public class Ma extends Activity {
	private Web w = new Web(this);
	private BlutosLE ble = new BlutosLE();
	private WebView wv;
	private Handler uh = new UiHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ma);

		// 页面设置
		wv = (WebView)findViewById(R.id.wv);
		wv.setWebChromeClient(new WebChromeClient());
		WebSettings ws = wv.getSettings();
		ws.setDefaultTextEncodingName("UTF-8");
		ws.setJavaScriptEnabled(true);
		wv.addJavascriptInterface(w, "rfdo");

		sendUrl(EmUrl.Home);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ble.bldActCb(requestCode, resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
//		w.open();
		w.initBLE(ble);
		super.onResume();
	}

	@Override
	protected void onPause() {
//		w.close();
		ble.closeDevice();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
//		w.close();
//		w.closeDb();
		ble.closeDevice();
		super.onDestroy();
	}

	// 获取当前页面信息
	private EmUrl getCurUi () {
		try {
			return EmUrl.valueOf(wv.getTitle());
		} catch (Exception e) {
			return null;
		}
	}

	// 页面跳转
	public void sendUrl (String url) {
		uh.sendMessage(uh.obtainMessage(EmUh.Url.ordinal(), 0, 0, url));
	}

	// 页面跳转
	public void sendUrl (EmUrl e) {
		sendUrl(e.toString());
	}

	// 页面跳转
	public void sendUrl (EmUrl e, String... args) {
		sendUrl(Str.meg(e.toString(), args));
	}

	// 发送页面处理消息
	public void sendUh (EmUh e) {
		uh.sendMessage(uh.obtainMessage(e.ordinal()));
	}

	// 页面处理器
	private class UiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			EmUh e = EmUh.values()[msg.what];
			switch (e) {
				case Url:
					wv.loadUrl((String)msg.obj);
					break;
				case Connected:
					switch (getCurUi()) {
						case Err:
							sendUrl(EmUrl.Home);
							break;
					}
				default:
					break;
			}
		}
	}
}
