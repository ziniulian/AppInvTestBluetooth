package com.invengo.rfidpad.base;

import invengo.javaapi.protocol.IRP1.SysConfig_800;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.lib.util.SysUtil;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

public class InvengoCrashHandler implements UncaughtExceptionHandler {

	private static InvengoCrashHandler mInstance = new InvengoCrashHandler();
	private UncaughtExceptionHandler mDefaultCrashHandler;
	private Context mContext;
	private ReaderHolder mReaderHolder;
	private ActivityExitManager mExitManager;
	
	private static final String TAG = "InvengoCrashHandler";
	private String LOG_FILE_PATH = null;
	private static final String FILE_NAME = "invengo";
	private static final String FILE_NAME_SUFFIX = ".log";
	
	private InvengoCrashHandler(){
		mExitManager = ActivityExitManager.getInstance();
	}
	
	public static InvengoCrashHandler getInstance(){
		return mInstance;
	}
	
	public void init(Context context){
		mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context.getApplicationContext();
		mReaderHolder = ReaderHolder.getInstance();
		LOG_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + SysUtil.getAppName(mContext) + "/";
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
//		dumpExceptionToSDCard(ex);
//		ex.printStackTrace();
		
		if(!dumpExceptionToSDCard(ex) && null != mDefaultCrashHandler){
			mDefaultCrashHandler.uncaughtException(thread, ex);
		}else{
			ex.printStackTrace();
			TagScanSettingsCollection.clearSettings();
			DebugManager.clearSettings();
			if(mReaderHolder.getDeviceType() == DeviceType.XC2600){
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(mReaderHolder.isConnected()){
							byte parameter = (byte) 0x80;
							byte[] data = new byte[]{0x01, 0x00};
							SysConfig_800 shutdownMsg = new SysConfig_800(parameter, data);
							mReaderHolder.sendMessage(shutdownMsg);
						}
					}
				}).start();
			}
			if(mReaderHolder.isConnected()){
				mReaderHolder.disConnect();
			}
			InvengoLog.shutdown();
			mExitManager.exitApp();
		}
	}
	
	private boolean dumpExceptionToSDCard(Throwable ex){
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				InvengoUtils.showToast(mContext, R.string.toast_invengo_crash_info);
				Looper.loop();
			}
		}.start();
		
		try {
			Thread.sleep(2 * 1000);
		} catch (InterruptedException e1) {
			Log.e(TAG, "Interrupted Exception.");
			return false;
		}
		
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Log.w(TAG, "SD-card unmounted!");
			return false;
		}
		
		File dir = new File(LOG_FILE_PATH);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		long currentTime = System.currentTimeMillis();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime));
		
		File file = new File(LOG_FILE_PATH + FILE_NAME + FILE_NAME_SUFFIX);
		
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.println(time);
			writer.println();
			ex.printStackTrace(writer);
		} catch (IOException e) {
			Log.e(TAG, "Dump crash info failed.");
			return false;
		}finally{
			if(null != writer){
				writer.close();
			}
		}
		
		return true;
	}

}
