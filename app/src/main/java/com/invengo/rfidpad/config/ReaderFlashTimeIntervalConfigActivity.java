package com.invengo.rfidpad.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

/**
 * XC2600-Flash保存时间间隔
 */
public class ReaderFlashTimeIntervalConfigActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mTimeIntervalEditText;
	private ReaderHolder mReaderHolder;
	private static final byte PARAMETER = (byte) 0x55;
	private static final String TAG = ReaderFlashTimeIntervalConfigActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_time_interval);
		setTitle(R.string.title_reader_configuration_time_interval);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mTimeIntervalEditText = (EditText) findViewById(R.id.edit_text_reader_configuration_time_interval);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_time_interval, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_time_interval_config_id:
				if(TextUtils.isEmpty(mTimeIntervalEditText.getText().toString())){
					InvengoUtils.showToast(ReaderFlashTimeIntervalConfigActivity.this, R.string.toast_reader_configuration_time_interval_not_null);
					break;
				}
				int timeInterval = Integer.parseInt(mTimeIntervalEditText.getText().toString());
				attemptConfigTimeInterval(timeInterval);
				break;
			case R.id.menu_reader_configuration_time_interval_query_id:
				attemptQueryTimeInterval();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptConfigTimeInterval(int timeInterval) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		byte length = 0x02;
		byte[] data = {length, (byte) (timeInterval >> 8), (byte) (timeInterval & 0xFF)};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_time_interval_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 configTimeIntervalMsg= new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(configTimeIntervalMsg);
	}

	private void attemptQueryTimeInterval() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_time_interval_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryTimeIntervalMsg = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(queryTimeIntervalMsg);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeTimeInterval();
		initializeData();

		InvengoLog.i(TAG, "INFO.onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeTimeInterval() {
		InvengoLog.i(TAG, "INFO.initializeTimeInterval()");
		Thread queryTimeIntervalThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryTimeIntervalMsg = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(queryTimeIntervalMsg);
					if (success) {
						if (null != queryTimeIntervalMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_TIME_INTERVAL;
							msg.obj = queryTimeIntervalMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryTimeIntervalThread.start();
	}

	private static final int QUERY_TIME_INTERVAL = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_TIME_INTERVAL:
					InvengoLog.i(TAG, "INFO.update Time Interval.");
					SysQuery800ReceivedInfo response = (SysQuery800ReceivedInfo) msg.obj;
					byte[] timeIntervalByte = response.getQueryData();
					int timeInterval = timeIntervalByte[0] << 8 | timeIntervalByte[1] & 0xFF;
					mTimeIntervalEditText.setText(String.valueOf(timeInterval));
					break;
				default:
					break;
			}
		};
	};

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}


	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//配置/查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof SysQuery800ReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY_TIME_INTERVAL;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderFlashTimeIntervalConfigActivity.this, R.string.toast_reader_configuration_time_interval_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderFlashTimeIntervalConfigActivity.this, R.string.toast_reader_configuration_time_interval_failure);
			}
		}
	}
}
