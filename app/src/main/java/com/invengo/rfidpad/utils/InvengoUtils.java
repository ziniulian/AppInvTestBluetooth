package com.invengo.rfidpad.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.Random;

import invengo.javaapi.core.GBMemoryBank;

public class InvengoUtils {

	private static final int SHOW_TIME = 800;
	public static void showToast(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Activity activity, int resId){
		showToast(activity, resId, SHOW_TIME);
	}

	public static void showToast(final Activity activity, final int resId, final int showTime){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final Toast toast = Toast.makeText(activity, resId, Toast.LENGTH_LONG);
				toast.show();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						toast.cancel();
					}
				}, showTime);
			}
		});
	}

	public static void showToast(Activity activity, String msg){
		showToast(activity, msg, SHOW_TIME);
	}

	public static void showToast(final Activity activity, final String msg, final int showTime){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
				toast.show();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						toast.cancel();
					}
				}, showTime);
			}
		});
	}

	/**
	 * 随机生成16进制字符串
	 */
	public static String randomHexString(int len)  {
		try {
			StringBuffer result = new StringBuffer();
			for(int i = 0; i < len; i++) {
				result.append(Integer.toHexString(new Random().nextInt(16)) + Integer.toHexString(new Random().nextInt(16)));
			}
			return result.toString().toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static GBMemoryBank getUserBank(int userBank) {
		GBMemoryBank bank = GBMemoryBank.GBEPCMemory;

		if(userBank == 0){
			bank = GBMemoryBank.GBUser1Memory;
		}else if(userBank == 1){
			bank = GBMemoryBank.GBUser2Memory;
		}else if(userBank == 2){
			bank = GBMemoryBank.GBUser3Memory;
		}else if(userBank == 3){
			bank = GBMemoryBank.GBUser4Memory;
		}else if(userBank == 4){
			bank = GBMemoryBank.GBUser5Memory;
		}else if(userBank == 5){
			bank = GBMemoryBank.GBUser6Memory;
		}else if(userBank == 6){
			bank = GBMemoryBank.GBUser7Memory;
		}else if(userBank == 7){
			bank = GBMemoryBank.GBUser8Memory;
		}else if(userBank == 8){
			bank = GBMemoryBank.GBUser9Memory;
		}else if(userBank == 9){
			bank = GBMemoryBank.GBUser10Memory;
		}else if(userBank == 10){
			bank = GBMemoryBank.GBUser11Memory;
		}else if(userBank == 11){
			bank = GBMemoryBank.GBUser12Memory;
		}else if(userBank == 12){
			bank = GBMemoryBank.GBUser13Memory;
		}else if(userBank == 13){
			bank = GBMemoryBank.GBUser14Memory;
		}else if(userBank == 14){
			bank = GBMemoryBank.GBUser15Memory;
		}else if(userBank == 15){
			bank = GBMemoryBank.GBUser16Memory;
		}
		return bank;
	}


}
