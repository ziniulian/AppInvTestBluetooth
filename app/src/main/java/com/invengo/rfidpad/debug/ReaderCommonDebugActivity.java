package com.invengo.rfidpad.debug;

import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.PowerOn;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 *	读写器普通功能调试-开功放、关功放、老化模式、正常模式
 */
public class ReaderCommonDebugActivity extends PowerManagerActivity {

	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TagScanSettingsCollection mSettingsCollection;
	private CommonDebugBroadcastReceiver mReceiver;
	private View mContentView;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private Button mPowerOnButton;
	private Button mPowerOffButton;
//	private Button mAgeingModeButton;
//	private Button mCommonModeButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_common);
		setTitle(R.string.title_reader_debug_common);

		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();
		mSettingsCollection = TagScanSettingsCollection.getInstance();

		mPowerOnButton = (Button) findViewById(R.id.button_reader_debug_common_power_on);
		mPowerOffButton = (Button) findViewById(R.id.button_reader_debug_common_power_off);
//		mAgeingModeButton = (Button) findViewById(R.id.button_reader_debug_common_age_mode);
//		mCommonModeButton = (Button) findViewById(R.id.button_reader_debug_common_common_mode);

		mContentView = findViewById(R.id.content_reader_debug_common_id);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		addListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void addListener() {
		mPowerOnButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				powerOnDebug();
			}
		});
		mPowerOffButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				powerOffDebug();
			}
		});
//		mAgeingModeButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				ageReaderDebug();
//			}
//		});
//		mCommonModeButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				commonReaderDebug();
//			}
//		});

		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new CommonDebugBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	protected void commonReaderDebug() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_common_common_mode_message);
		mProgressBarManager.showProgressBar(true, mContentView, mProgressBarStatusView, shortAnimTime);
		setWidgetEnabled(false);

	}

	protected void ageReaderDebug() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_common_ageing_mode_message);
		mProgressBarManager.showProgressBar(true, mContentView, mProgressBarStatusView, shortAnimTime);
		setWidgetEnabled(false);


	}

	protected void powerOffDebug() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_common_power_off_message);
		mProgressBarManager.showProgressBar(true, mContentView, mProgressBarStatusView, shortAnimTime);
		setWidgetEnabled(false);

		PowerOff msg = new PowerOff();
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	protected void powerOnDebug() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_common_power_on_message);
		mProgressBarManager.showProgressBar(true, mContentView, mProgressBarStatusView, shortAnimTime);
		setWidgetEnabled(false);

		int antenna = mSettingsCollection.getAntenna();
		PowerOn msg = new PowerOn((byte) antenna);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void setWidgetEnabled(boolean enabled){
		mPowerOnButton.setEnabled(enabled);
		mPowerOffButton.setEnabled(enabled);
//		mAgeingModeButton.setEnabled(enabled);
//		mCommonModeButton.setEnabled(enabled);
	}

	private class CommonDebugBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			setWidgetEnabled(true);
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//写入成功
				InvengoUtils.showToast(ReaderCommonDebugActivity.this, R.string.toast_reader_debug_common_operation_success);
			}else{
				//写入失败
				InvengoUtils.showToast(ReaderCommonDebugActivity.this, R.string.toast_reader_debug_common_operation_failure);
			}
		}
	}

}
