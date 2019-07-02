package com.invengo.rfidpad.config;

import invengo.javaapi.protocol.IRP1.SysConfig_800;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * XC2600-USB供电口控制
 */
public class ReaderUsbControlActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private ReaderHolder mReaderHolder;
	private static final byte PARAMETER = (byte) 0x8B;
	private static final String TAG = ReaderUsbControlActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_usb_control);
		setTitle(R.string.title_reader_configuration_usb_control);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_usb_control, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_usb_control_open_id:
				attemptContorlUsbPort(true);
				break;
			case R.id.menu_reader_configuration_usb_control_close_id:
				attemptContorlUsbPort(false);
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptContorlUsbPort(boolean enabled){
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.attemptContorlUstPort().");

		byte length = 0x01;
		byte[] data = {length, (byte) (enabled ? 0x01 : 0x00)};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, enabled ? R.string.progress_bar_configuration_usb_control_open_message : R.string.progress_bar_configuration_usb_control_close_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 configVoltageMsg= new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(configVoltageMsg);
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(ReaderUsbControlActivity.this, R.string.toast_reader_configuration_usb_control_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderUsbControlActivity.this, R.string.toast_reader_configuration_usb_control_failure);
			}
		}
	}
}
