package com.invengo.rfidpad.base;

import android.app.Application;

public class RfidApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		InvengoCrashHandler crashHandler = InvengoCrashHandler.getInstance();
		crashHandler.init(this);
		VoiceManager.getInstance(getApplicationContext());
		ReaderHolder.getInstance();
	}
	
}
