package com.invengo.rfidpad.base;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.handle.IMessageNotificationReceivedHandle;
import invengo.javaapi.protocol.IRP1.RXD_ReaderChargeStatus;
import invengo.javaapi.protocol.IRP1.RXD_ReaderElectricQuantity;
import invengo.javaapi.protocol.IRP1.RXD_VoltageAlarm;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.lib.util.SysUtil;
import com.invengo.rfidpad.R;

/**
 * 屏幕电源管理
 */
public class PowerManagerActivity extends Activity implements IMessageNotificationReceivedHandle{

	private ActivityExitManager mExitManager;
	private ReaderHolder mReaderHolder;
	private NotificationManager mNotificationManager;
	private static final int mNotificationId = 0;
	private NotificationCompat.Builder mBuilder = null;
	private boolean mBatteryChargeStatus = false;//充电状态{true-开始充电,false-停止充电}
	private int mCurrentBatteryQuantity = 0;//当前电量百分比
	private boolean isLowBatteryQuantityAlarm = false;//低电压告警{开始充电后为false}
	private IntentFilter mIntentFilter;
	private static final String TAG = PowerManagerActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mExitManager = ActivityExitManager.getInstance();
		mExitManager.addActivity(this);
		mReaderHolder = ReaderHolder.getInstance();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		registerBroadcastReceiver();
	}

	private BroadcastReceiver mShutdownBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Intent.ACTION_SHUTDOWN)){
				InvengoLog.i(TAG, "INFO.PDA shutdown");
				mReaderHolder.disConnect();
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		InvengoLog.i(TAG, "INFO.onStart().");
		if (mReaderHolder.getDeviceType() == DeviceType.XC2910
				|| mReaderHolder.getDeviceType() == DeviceType.XC9910
				|| mReaderHolder.getDeviceType() == DeviceType.XC2900
				|| mReaderHolder.getDeviceType() == DeviceType.XC2903
				|| mReaderHolder.getDeviceType() == DeviceType.XC2910_V3) {
			mReaderHolder.wakeup();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		InvengoLog.i(TAG, "INFO.onResume().");
		String appName = SysUtil.getAppName(this);
		SysUtil.wakeLock(this, appName);
		addReaderCallback();

	}

	private void registerBroadcastReceiver() {
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Intent.ACTION_SHUTDOWN);
		//		filter.addCategory("android.intent.category.HOME");
		mIntentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
		registerReceiver(mShutdownBroadcastReceiver, mIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		InvengoLog.i(TAG, "INFO.onPause().");
		SysUtil.wakeUnlock();
		removeReaderCallback();
	}

	@Override
	protected void onStop() {
		super.onStop();
		InvengoLog.i(TAG, "INFO.onStop().");
		if (mReaderHolder.getDeviceType() == DeviceType.XC2910
				|| mReaderHolder.getDeviceType() == DeviceType.XC9910
				|| mReaderHolder.getDeviceType() == DeviceType.XC2900
				|| mReaderHolder.getDeviceType() == DeviceType.XC2903
				|| mReaderHolder.getDeviceType() == DeviceType.XC2910_V3) {
			mReaderHolder.sleep();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		InvengoLog.i(TAG, "INFO.onDestroy().");
		//		cancelNotificationMessage();
		if(null != mIntentFilter){
			unregisterReceiver(mShutdownBroadcastReceiver);
		}

		mExitManager.removeActivity(this);
	}

	protected void addReaderCallback(){
		if(null != mReaderHolder.getCurrentReader()){
			mReaderHolder.getCurrentReader().onMessageNotificationReceived.add(this);//注册
		}
	}

	protected void removeReaderCallback(){
		if(null != mReaderHolder.getCurrentReader()){
			mReaderHolder.getCurrentReader().onMessageNotificationReceived.remove(this);//取消注册
		}
	}

	public void handleNotificationMessage(BaseReader reader, IMessageNotification msg){
		//do something
	}

	@Override
	public void messageNotificationReceivedHandle(BaseReader reader,
												  IMessageNotification msg) {
		if(mReaderHolder.isConnected()){
			if(msg instanceof RXD_VoltageAlarm){//电压告警
				InvengoLog.i(TAG, "VoltageAlarm");
				isLowBatteryQuantityAlarm = true;
				createNotificationMessage(R.drawable.reader_idle_low_battery);
			}else if(msg instanceof RXD_ReaderElectricQuantity){//电量
				RXD_ReaderElectricQuantity response = (RXD_ReaderElectricQuantity) msg;
				if(null != response.getReceivedMessage()){
					mCurrentBatteryQuantity = response.getReceivedMessage().getElectricQuantityPercent();
					int iconResId = mReaderHolder.getBatteryIcon(mCurrentBatteryQuantity);
					if(mBatteryChargeStatus){
						if(mCurrentBatteryQuantity == 100){
							iconResId = R.drawable.reader_battery_charge_animfull;
						}else{
							iconResId = R.drawable.reader_idle_charging;
						}
					}else{
						if(isLowBatteryQuantityAlarm){
							iconResId = R.drawable.reader_idle_low_battery;
						}
					}
					InvengoLog.i(TAG, "ReaderElectricQuantity");
					createNotificationMessage(iconResId);
				}
			}else if(msg instanceof RXD_ReaderChargeStatus){//充电状态
				InvengoLog.i(TAG, "ReaderChargeStatus");
				RXD_ReaderChargeStatus response = (RXD_ReaderChargeStatus) msg;
				if(null != response.getReceivedMessage()){
					int status = response.getReceivedMessage().getChargeStatus();
					if(status == 0){//开始充电
						mBatteryChargeStatus = true;
						isLowBatteryQuantityAlarm = false;
						createNotificationMessage(R.drawable.reader_idle_charging);
					}else if(status == 255){//停止充电
						mBatteryChargeStatus = false;
						createNotificationMessage(mReaderHolder.getBatteryIcon(mCurrentBatteryQuantity));
					}
				}
			}else{
				handleNotificationMessage(reader, msg);
			}
		}
	}

	private void createNotificationMessage(int iconResId){
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setSmallIcon(iconResId);
		mBuilder.setPriority(Notification.PRIORITY_HIGH);
		mBuilder.setContentTitle(getString(R.string.title_reader_battery_manager));
		mBuilder.setContentText(getString(R.string.title_reader_battery_quantity, mCurrentBatteryQuantity));

		mNotificationManager.notify(mNotificationId, mBuilder.build());
	}

	protected void cancelNotificationMessage(){
		if(null != mNotificationManager){
			mNotificationManager.cancel(mNotificationId);
		}
	}

}
