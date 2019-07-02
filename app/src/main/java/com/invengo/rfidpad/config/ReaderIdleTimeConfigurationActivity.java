package com.invengo.rfidpad.config;

import invengo.javaapi.core.Util;
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
 * 待机时间配置
 */
public class ReaderIdleTimeConfigurationActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mIdleTimeEditText;
	private ReaderHolder mReaderHolder;
	private static final String TAG = ReaderIdleTimeConfigurationActivity.class.getSimpleName();
	private static final byte PARAMETER = (byte) 0x81;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_idle_time);
		setTitle(R.string.title_reader_configuration_idle_time);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mIdleTimeEditText = (EditText) findViewById(R.id.edit_text_reader_configuration_idle_time);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_idle_time, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_idle_time_config_id:
				if(TextUtils.isEmpty(mIdleTimeEditText.getText().toString())){
					InvengoUtils.showToast(ReaderIdleTimeConfigurationActivity.this, R.string.toast_reader_configuration_idle_time_not_null);
					break;
				}
				int idleTime = Integer.parseInt(mIdleTimeEditText.getText().toString());
				attemptConfigIdleTime(idleTime);
				break;
			case R.id.menu_reader_configuration_idle_time_query_id:
				attemptQueryIdleTime();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQueryIdleTime() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_idle_time_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryIdleTimeMsg = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(queryIdleTimeMsg);
	}

	private void attemptConfigIdleTime(int idleTime) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		byte length = 0x02;
		byte[] data = {length, (byte) (idleTime >> 8), (byte) (idleTime & 0xFF)};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_idle_time_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 configIdleTimeMsg= new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(configIdleTimeMsg);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeIdleTime();
		initializeData();

		InvengoLog.i(TAG, "INFO.onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeIdleTime() {
		InvengoLog.i(TAG, "INFO.initializeIdleTime()");
		Thread queryIdleTimeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryIdleTimeMsg = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(queryIdleTimeMsg);
					if (success) {
						if (null != queryIdleTimeMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_IDLE_TIME;
							msg.obj = queryIdleTimeMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryIdleTimeThread.start();
	}

	private static final int QUERY_IDLE_TIME = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_IDLE_TIME:
					InvengoLog.i(TAG, "INFO.update Idle Time.");
					SysQuery800ReceivedInfo queryRateMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] idleTimeByte = queryRateMsg.getQueryData();
//				int idleTime = idleTimeByte[0] << 8 | idleTimeByte[1] & 0xFF;
					int idleTime = Integer.parseInt(Util.convertByteArrayToHexString(idleTimeByte), 16);
					mIdleTimeEditText.setText(String.valueOf(idleTime));
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
					msg.what = QUERY_IDLE_TIME;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderIdleTimeConfigurationActivity.this, R.string.toast_reader_configuration_idle_time_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderIdleTimeConfigurationActivity.this, R.string.toast_reader_configuration_idle_time_failure);
			}
		}
	}
}
