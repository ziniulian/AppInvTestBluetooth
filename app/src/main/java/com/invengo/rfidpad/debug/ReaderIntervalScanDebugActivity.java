package com.invengo.rfidpad.debug;

import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;
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

/**
 * 读写器间歇读卡配置
 */
public class ReaderIntervalScanDebugActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mTimeEditText;
	private EditText mMinIntervalTimeEditText;
	private EditText mMaxIntervalTimeEditText;
	private ReaderHolder mReaderHolder;
	private static final String TAG = ReaderIntervalScanDebugActivity.class.getSimpleName();
	private static final byte PARAMETER = (byte) 0x34;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_interval_scan);
		setTitle(R.string.title_reader_debug_interval_scan);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mTimeEditText = (EditText) findViewById(R.id.edit_text_reader_debug_interval_scan_time_id);
		mMinIntervalTimeEditText = (EditText) findViewById(R.id.edit_text_reader_debug_interval_scan_min_time_id);
		mMaxIntervalTimeEditText = (EditText) findViewById(R.id.edit_text_reader_debug_interval_scan_max_time_id);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_debug_interval_time, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_debug_interval_scan_time_config_id:
				if(TextUtils.isEmpty(mTimeEditText.getText().toString())){
					InvengoUtils.showToast(ReaderIntervalScanDebugActivity.this, R.string.toast_reader_debug_interval_scan_time_null);
					break;
				}
				if(TextUtils.isEmpty(mMinIntervalTimeEditText.getText().toString())){
					InvengoUtils.showToast(ReaderIntervalScanDebugActivity.this, R.string.toast_reader_debug_interval_scan_min_time_null);
					break;
				}
				if(TextUtils.isEmpty(mMaxIntervalTimeEditText.getText().toString())){
					InvengoUtils.showToast(ReaderIntervalScanDebugActivity.this, R.string.toast_reader_debug_interval_scan_max_time_null);
					break;
				}
				int time = Integer.parseInt(mTimeEditText.getText().toString());
				int minTime = Integer.parseInt(mMinIntervalTimeEditText.getText().toString());
				int maxTime = Integer.parseInt(mMaxIntervalTimeEditText.getText().toString());
				if(maxTime < minTime){
					InvengoUtils.showToast(ReaderIntervalScanDebugActivity.this, R.string.toast_reader_debug_interval_scan_max_min_time_null);
					break;
				}
				attemptConfigTime(time, minTime, maxTime);
				break;
			case R.id.menu_reader_debug_interval_scan_time_query_id:
				attemptQueryTime();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQueryTime() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_interval_scan_time_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryIdleTimeMsg = new SysQuery_800(PARAMETER, (byte) 0x01);
		OperationTask task = new OperationTask(this);
		task.execute(queryIdleTimeMsg);
	}

	private void attemptConfigTime(int time, int minTime, int maxTime) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		byte[] data = new byte[8];
		data[0] = 0x07;//length
		data[1] = 0x01;//antenna

		data[2] = (byte) (time >> 8);
		data[3] = (byte) (time & 0xFF);

		data[4] = (byte) (minTime >> 8);
		data[5] = (byte) (minTime & 0xFF);

		data[6] = (byte) (maxTime >> 8);
		data[7] = (byte) (maxTime & 0xFF);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_config_interval_scan_time_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 configIdleTimeMsg= new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(configIdleTimeMsg);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeTime();
		initializeData();

		InvengoLog.i(TAG, "INFO.onResume()");
	}

	private void initializeTime() {
		InvengoLog.i(TAG, "INFO.initializeTime()");
		Thread queryTimeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryTimeMsg = new SysQuery_800(PARAMETER, (byte) 0x01);
					boolean success = mReaderHolder.getCurrentReader().send(queryTimeMsg);
					if (success) {
						if (null != queryTimeMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_INTERVAL_TIME;
							msg.obj = queryTimeMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryTimeThread.start();
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

	private static final int QUERY_INTERVAL_TIME = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_INTERVAL_TIME:
					InvengoLog.i(TAG, "INFO.update interval Time.");
					SysQuery800ReceivedInfo queryTimeMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] idleTimeByte = queryTimeMsg.getQueryData();
					int time = idleTimeByte[0] << 8 | idleTimeByte[1] & 0xFF;//使用时间
					int minTime = idleTimeByte[2] << 8 | idleTimeByte[3] & 0xFF;//最小空闲时间
					int maxTime = idleTimeByte[4] << 8 | idleTimeByte[5] & 0xFF;//最大空闲时间
					mTimeEditText.setText(String.valueOf(time));
					mMinIntervalTimeEditText.setText(String.valueOf(minTime));
					mMaxIntervalTimeEditText.setText(String.valueOf(maxTime));
					break;
				default:
					break;
			}
		};
	};

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
					msg.what = QUERY_INTERVAL_TIME;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderIntervalScanDebugActivity.this, R.string.toast_reader_debug_interval_scan_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderIntervalScanDebugActivity.this, R.string.toast_reader_debug_interval_scan_failure);
			}
		}
	}
}
