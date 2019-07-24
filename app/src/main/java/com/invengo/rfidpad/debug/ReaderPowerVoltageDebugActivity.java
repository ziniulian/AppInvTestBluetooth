package com.invengo.rfidpad.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

/**
 * 后向功率电压检测
 */
public class ReaderPowerVoltageDebugActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mVoltageEditText;
	private ReaderHolder mReaderHolder;
	private static final byte PARAMETER = (byte) 0x1D;
	private static final String TAG = ReaderPowerVoltageDebugActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_power_voltage);
		setTitle(R.string.title_reader_debug_power_voltage);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mVoltageEditText = (EditText) findViewById(R.id.edit_text_reader_debug_power_voltage_id);
		mVoltageEditText.setEnabled(false);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_debug_power_voltage, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_debug_power_voltage_query_id:
				attemptQueryVoltage();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQueryVoltage() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.attemptQueryVoltage().");

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_power_voltage_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryVoltageMsg = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(queryVoltageMsg);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeVoltage();
		initializeData();

		InvengoLog.i(TAG, "INFO.onResume()");
	}

	private void initializeVoltage() {
		InvengoLog.i(TAG, "INFO.initializeVoltage()");
		Thread queryVoltageThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryVoltageMsg = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(queryVoltageMsg);
					if (success) {
						if (null != queryVoltageMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_POWER_VOLTAGE;
							msg.obj = queryVoltageMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryVoltageThread.start();
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

	private static final int QUERY_POWER_VOLTAGE = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_POWER_VOLTAGE:
					InvengoLog.i(TAG, "INFO.update voltage.");
					SysQuery800ReceivedInfo response = (SysQuery800ReceivedInfo) msg.obj;
					byte[] voltageByte = response.getQueryData();
					int voltage = voltageByte[1] & 0xFF;
					mVoltageEditText.setText(String.valueOf(voltage));
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
				//查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof SysQuery800ReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY_POWER_VOLTAGE;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderPowerVoltageDebugActivity.this, R.string.toast_reader_debug_query_power_voltage_success);
			}else{
				//查询失败
				InvengoUtils.showToast(ReaderPowerVoltageDebugActivity.this, R.string.toast_reader_debug_query_power_voltage_failure);
			}
		}
	}


}
