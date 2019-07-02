package com.invengo.rfidpad;

import invengo.javaapi.protocol.IRP1.IntegrateReaderManager;
import invengo.javaapi.protocol.IRP1.Reader;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.lib.system.device.DeviceManager;
import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.base.ReaderMainActivity;

/**
 * 设备选择
 */
public class MainActivity extends Activity {

	private View mSelectStatusView;
	private TextView mSelectStatusMessageView;
	private ReaderHolder mHolder;
	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_type_select);

		mHolder = ReaderHolder.getInstance();
		mSelectStatusView = findViewById(R.id.device_type_select_status);
		mSelectStatusMessageView = (TextView) findViewById(R.id.device_type_select_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		//		selectDevice();
		showProgress(true);
		DeviceType deviceType = DeviceManager.getDeviceType();
		//		if(deviceType.toString().equals(DeviceType.XC2910.toString())){
		//			throw new RuntimeException("This is Test!");
		//		}
		DeviceSelectAsyncTask task = new DeviceSelectAsyncTask();
		task.execute(deviceType.toString());
		InvengoLog.i(TAG, "INFO.onResume()");
	}
	// Show Error Alert Dialog

	private void showDeviceDialog(int messageId) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		alert.setPositiveButton(R.string.ok_button,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setTitle(R.string.device_error);
		alert.setMessage(messageId);
		alert.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		InvengoLog.i(TAG, "INFO.onDestroy()");
	}

	private class DeviceSelectAsyncTask extends AsyncTask<String, Void, Integer>{

		private static final int SUCCESS = 0;
		private static final int READER_CONNECT_FAILURE = 1;
		private static final int UNSUPPORT_MODULE = 2;
		private static final int APPLICATION_RERUN = 3;
		private static final int UNSUPPORT_BLUETOOTH_LE = 4;
		private String mDeviceType;

		@Override
		protected Integer doInBackground(String... params) {
			this.mDeviceType = params[0];

			if (!mDeviceType.equals(DeviceType.XC2910.toString())
					&& !mDeviceType.equals(DeviceType.XC9910.toString())
					&& !mDeviceType.equals(DeviceType.XC2900.toString())
					&& !mDeviceType.equals(DeviceType.XC2903.toString())
					&& !mDeviceType.equals(DeviceType.XC2910_V3.toString())) {
				//				if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
				//					return UNSUPPORT_BLUETOOTH_LE;
				//				}
				if(mHolder.isConnected()){
					return APPLICATION_RERUN;
				}

				mHolder.setDeviceType(DeviceType.XC2600);
			}else if(mDeviceType.equals(DeviceType.XC2910.toString())
					|| mDeviceType.equals(DeviceType.XC9910.toString())){
				/*
				 * if(mHolder.isConnected())用于设备通过home键退出demo,然后通过点击图标进入demo的逻辑处理.
				 */
				if(mHolder.isConnected()){
					return APPLICATION_RERUN;
				}
				DeviceType deviceType = DeviceType.XC2910;
				String deviceName = DeviceType.XC2910.toString();
				if(mDeviceType.equals(DeviceType.XC2910.toString())){
					//
				}else if(mDeviceType.equals(DeviceType.XC9910.toString())){
					deviceType = DeviceType.XC9910;
					deviceName = DeviceType.XC9910.toString();
				}

				Reader reader = IntegrateReaderManager.getInstance();
				if(reader == null){
					return UNSUPPORT_MODULE;
				}
				boolean success = reader.connect();
				if(success){
					mHolder.setDeviceType(deviceType);
					mHolder.setCurrentReader(reader);
					mHolder.setConnected(true);
					mHolder.setReaderName(reader.getReaderName());
					mHolder.setDeviceName(deviceName);
				}else{
					return READER_CONNECT_FAILURE;
				}
			}else if(mDeviceType.equals(DeviceType.XC2900.toString())
					|| mDeviceType.equals(DeviceType.XC2903.toString())
					|| mDeviceType.equals(DeviceType.XC2910_V3.toString())){
				if(mHolder.isConnected()){
					return APPLICATION_RERUN;
				}
				DeviceType deviceType = DeviceType.XC2900;
				String deviceName = DeviceType.XC2900.toString();
				if(mDeviceType.equals(DeviceType.XC2900.toString())){
					//
				}else if(mDeviceType.equals(DeviceType.XC2903.toString())){
					deviceType = DeviceType.XC2903;
					deviceName = DeviceType.XC2903.toString();
				}else if(mDeviceType.equals(DeviceType.XC2910_V3.toString())){
					deviceType = DeviceType.XC2910_V3;
					deviceName = DeviceType.XC2910_V3.toString();
				}

				Reader reader = new Reader("Reader1", "RS232", String.format("%s,%s", IntegrateReaderManager.getPortName(), String.valueOf(115200)));
				boolean success = reader.connect();
				if(success){
					mHolder.setDeviceType(deviceType);
					mHolder.setCurrentReader(reader);
					mHolder.setConnected(true);
					mHolder.setReaderName(reader.getReaderName());
					mHolder.setDeviceName(deviceName);
				}else{
					return READER_CONNECT_FAILURE;
				}
			}
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			showProgress(false);
			if (!mDeviceType.equals(DeviceType.XC2910.toString())
					&& !mDeviceType.equals(DeviceType.XC9910.toString())
					&& !mDeviceType.equals(DeviceType.XC2900.toString())
					&& !mDeviceType.equals(DeviceType.XC2903.toString())
					&& !mDeviceType.equals(DeviceType.XC2910_V3.toString())) {
				if(result.intValue() == SUCCESS){
					InvengoLog.i(TAG, "Enter XC2600 Login Activity.");
					Intent intent = new Intent(MainActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}else if(result.intValue() == UNSUPPORT_BLUETOOTH_LE){
					InvengoLog.w(TAG, "Unsupport Bluetooth_le");
					showDeviceDialog(R.string.unsupport_bluetooth_le);
				}else if(result.intValue() == APPLICATION_RERUN){
					InvengoLog.i(TAG, "Re-enter the application by app icon.");
					finish();
				}
			} else if(mDeviceType.equals(DeviceType.XC2910.toString())
					|| mDeviceType.equals(DeviceType.XC9910.toString())
					|| mDeviceType.equals(DeviceType.XC2900.toString())
					|| mDeviceType.equals(DeviceType.XC2903.toString())
					|| mDeviceType.equals(DeviceType.XC2910_V3.toString())) {
				if(result.intValue() == SUCCESS){
					InvengoLog.i(TAG, "Enter XC2910 & XC9910 & XC2900 & XC2903 & XC2910-A Scanner Activity.");
					Intent intent = new Intent(MainActivity.this, ReaderMainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}else if(result.intValue() == UNSUPPORT_MODULE){
					InvengoLog.w(TAG, "Unsupport Module");
					showDeviceDialog(R.string.filaed_to_device);
				}else if(result.intValue() == READER_CONNECT_FAILURE){
					InvengoLog.w(TAG, "Reader connect failure.");
					showDeviceDialog(R.string.filaed_to_connect);
				}else if(result.intValue() == APPLICATION_RERUN){
					InvengoLog.i(TAG, "Re-enter the application by app icon.");
					finish();
				}
			}
		};
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mSelectStatusView.setVisibility(View.VISIBLE);
			mSelectStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mSelectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

		} else {
			mSelectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

}
