package com.invengo.rfidpad.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

import invengo.javaapi.protocol.IRP1.FilterByTime_6C;

/**
 * 配置或取消重复标签按时间过滤功能
 */
public class TagFilterConfigActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private TagFilterConfigBroadcastReceiver mReceiver;

	private ReaderHolder mReaderHolder;
	private EditText mIntervalTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_filter_configuration);
		setTitle(R.string.title_tag_filter_configuration);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mIntervalTime = (EditText) findViewById(R.id.edit_tag_filter_configuration_interval_time);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_filter_configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_tag_filter_configuration_config_id:
				String timeString = mIntervalTime.getText().toString();
				if(TextUtils.isEmpty(timeString)){
					InvengoUtils.showToast(this, R.string.toast_tag_filter_configuration_interval_time_not_null);
					break;
				}
				int time = Integer.valueOf(mIntervalTime.getText().toString());
				if(time > 65535){
					InvengoUtils.showToast(this, R.string.toast_tag_filter_configuration_interval_time_out_of_bounds);
					break;
				}
				attemptConfig(time);
				break;
			case R.id.menu_tag_filter_configuration_cancel_id:
				attemptCancel();
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagFilterConfigBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private void attemptConfig(int time) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		byte[] timeData = new byte[2];
		timeData[0] = (byte) (time >> 8);
		timeData[1] = (byte) (time & 0xFF);

		byte type = 0x00;

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_tag_filter_configuration_config_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		FilterByTime_6C msg = new FilterByTime_6C(type, timeData);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void attemptCancel() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		byte type = 0x01;

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_tag_filter_configuration_cancel_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		FilterByTime_6C msg = new FilterByTime_6C(type, null);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private class TagFilterConfigBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(TagFilterConfigActivity.this, R.string.toast_tag_filter_configuration_success);
			}else{
				//失败
				InvengoUtils.showToast(TagFilterConfigActivity.this, R.string.toast_tag_filter_configuration_failure);
			}
		}
	}
}
