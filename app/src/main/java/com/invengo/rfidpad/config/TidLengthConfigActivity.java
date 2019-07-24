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

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

import invengo.javaapi.protocol.IRP1.TagOperationConfig_6C;
import invengo.javaapi.protocol.IRP1.TagOperationQuery_6C;
import invengo.javaapi.protocol.receivedInfo.TagOperationQuery6CReceivedInfo;

public class TidLengthConfigActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private ReaderHolder mReaderHolder;
	private EditText mTidLengthEditText;

	private TidLengthConfigurationBroadcastReceiver mReceiver;
	private static final byte PARAMETER = 0x15;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tid_length_configuration);
		setTitle(R.string.title_tid_length_configuration);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mTidLengthEditText = (EditText) findViewById(R.id.edit_tid_length_configuration_max_length);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TidLengthConfigurationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tid_length_configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selectedItemId = item.getItemId();
		switch (selectedItemId) {
			case R.id.menu_tid_length_configuration_config_id:
				if(TextUtils.isEmpty(mTidLengthEditText.getText().toString())){
					InvengoUtils.showToast(TidLengthConfigActivity.this, R.string.toast_tid_length_configuration_password_not_null);
					break;
				}

				attemptConfig(Integer.parseInt(mTidLengthEditText.getText().toString().trim()));
				break;
			case R.id.menu_tid_length_configuration_query_id:
				attemptQuery();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptConfig(int tidLength) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		byte[] data = new byte[]{(byte) tidLength};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_config_tid_length_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		TagOperationConfig_6C msg = new TagOperationConfig_6C(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void attemptQuery() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_tid_length_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		TagOperationQuery_6C msg = new TagOperationQuery_6C(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private static final int QUERY = 0;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY:
					TagOperationQuery6CReceivedInfo queryMsg = (TagOperationQuery6CReceivedInfo) msg.obj;
					byte[] data = queryMsg.getQueryData();
					int schemeIndex = data[0];
					mTidLengthEditText.setText(String.valueOf(schemeIndex));
					break;
				default:
					break;
			}
		};
	};

	private class TidLengthConfigurationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//配置/查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof TagOperationQuery6CReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY;
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
				InvengoUtils.showToast(TidLengthConfigActivity.this, R.string.toast_tid_length_configuration_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(TidLengthConfigActivity.this, R.string.toast_tid_length_configuration_failure);
			}
		}
	}
}
