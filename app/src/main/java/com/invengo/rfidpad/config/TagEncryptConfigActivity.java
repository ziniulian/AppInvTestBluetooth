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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
 * 标签加密配置
 */
public class TagEncryptConfigActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private ReaderHolder mReaderHolder;
	private Spinner mSchemeSpinner;
	private EditText mPasswordEditText;

	private TagEncryptConfigurationBroadcastReceiver mReceiver;
	private static final byte PARAMETER = 0x40;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_encrypt_configuration);
		setTitle(R.string.title_tag_encrypt_configuration);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mSchemeSpinner = (Spinner) findViewById(R.id.spinner_tag_encrypt_configuration_scheme);
		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_encrypt_configuration_password);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		intializeWidget();
		initializeData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagEncryptConfigurationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private void intializeWidget() {
		ArrayAdapter<String> schemeAdapter = new ArrayAdapter<String>(TagEncryptConfigActivity.this, R.layout.myspinner);
		schemeAdapter.add("0");
		schemeAdapter.add("1");
		mSchemeSpinner.setAdapter(schemeAdapter);
		mSchemeSpinner.setSelection(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_encrypt_configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selectedItemId = item.getItemId();
		switch (selectedItemId) {
			case R.id.menu_tag_encrypt_configuration_config_id:
				if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
					InvengoUtils.showToast(TagEncryptConfigActivity.this, R.string.toast_tag_encrypt_configuration_password_not_null);
					break;
				}

				attemptConfig(mSchemeSpinner.getSelectedItemPosition(), mPasswordEditText.getText().toString().trim());
				break;
			case R.id.menu_tag_encrypt_configuration_query_id:
				attemptQuery();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQuery() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_tag_encrypt_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 msg = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void attemptConfig(int scheme, String passwordHex) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		byte[] password = Util.convertHexStringToByteArray(passwordHex);
		int len = 3;
		byte[] data = new byte[]{(byte) len, (byte) scheme, password[0], password[1]};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_config_tag_encrypt_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 msg = new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private static final int QUERY = 0;
	private static final int CONFIG = 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY:
					SysQuery800ReceivedInfo querySchemeMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] data = querySchemeMsg.getQueryData();
					int schemeIndex = data[0];
					byte[] password = new byte[2];
					password[0] = data[2];
					password[1] = data[1];
					mSchemeSpinner.setSelection(schemeIndex);
					mPasswordEditText.setText(Util.convertByteArrayToHexString(password));
					break;
				case CONFIG:
					//do nothing
					break;
				default:
					break;
			}

		};
	};

	private class TagEncryptConfigurationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//配置/查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof SysQuery800ReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY;
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
				InvengoUtils.showToast(TagEncryptConfigActivity.this, R.string.toast_tag_encrypt_configuration_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(TagEncryptConfigActivity.this, R.string.toast_tag_encrypt_configuration_failure);
			}
		}
	}

}
