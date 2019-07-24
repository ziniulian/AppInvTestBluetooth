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

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

/**
 * XC2600-蓝牙配对密码配置
 */
public class ReaderBluetoothPasswordActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mPasswordText;
	private ReaderHolder mReaderHolder;
	private static final byte PARAMETER = (byte) 0x83;
	private static final String TAG = ReaderBluetoothPasswordActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_bluetooth_password);
		setTitle(R.string.title_reader_configuration_bluetooth_password);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mPasswordText = (EditText) findViewById(R.id.edit_text_reader_configuration_bluetooth_password);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_bluetooth_password, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_bluetooth_password_config_id:
				String password = mPasswordText.getText().toString();
				if(TextUtils.isEmpty(password)){
					InvengoUtils.showToast(ReaderBluetoothPasswordActivity.this, R.string.toast_reader_configuration_bluetooth_password_not_null);
					break;
				}

				if(password.length() != 4){
					InvengoUtils.showToast(ReaderBluetoothPasswordActivity.this, R.string.toast_reader_configuration_bluetooth_password_four);
					break;
				}

				attemptConfigPassword(password);
				break;
			case R.id.menu_reader_configuration_bluetooth_password_query_id:
				attemptQueryPassword();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptConfigPassword(String password) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.attemptConfigPassword().");

		byte[] temp = Util.convertHexStringToByteArray(password);
		byte length = (byte) temp.length;
		byte[] data = new byte[length + 1];
		data[0] = length;
		System.arraycopy(temp, 0, data, 1, length);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_bluetooth_password_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 configPasswordMsg= new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(configPasswordMsg);
	}

	private void attemptQueryPassword() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.attemptConfigPassword().");

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_bluetooth_password_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryPasswordMsg = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(queryPasswordMsg);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializePassword();
		initializeData();

		InvengoLog.i(TAG, "INFO.onResume()");
	}

	private void initializePassword() {
		InvengoLog.i(TAG, "INFO.initializePassword()");
		Thread queryPasswordThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryPasswordMsg = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(queryPasswordMsg);
					if (success) {
						if (null != queryPasswordMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_PASSWORD;
							msg.obj = queryPasswordMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryPasswordThread.start();
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

	private static final int QUERY_PASSWORD = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_PASSWORD:
					InvengoLog.i(TAG, "INFO.update password.");
					SysQuery800ReceivedInfo response = (SysQuery800ReceivedInfo) msg.obj;
					byte[] passwordByte = response.getQueryData();
					mPasswordText.setText(String.valueOf(Integer.parseInt(Util.convertByteArrayToHexString(passwordByte), 16)));
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
					msg.what = QUERY_PASSWORD;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderBluetoothPasswordActivity.this, R.string.toast_reader_configuration_bluetooth_password_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderBluetoothPasswordActivity.this, R.string.toast_reader_configuration_bluetooth_password_failure);
			}
		}
	}

}
