package com.invengo.rfidpad.debug;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.ConfigTagPassword;
import invengo.javaapi.protocol.IRP1.QueryTagPassword;
import invengo.javaapi.protocol.receivedInfo.QueryTagPasswordReceivedInfo;
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
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 标签密码配置
 */
public class TagPasswordConfigDebugActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private ReaderHolder mReaderHolder;
	private EditText mSpecialCodeEditText;
	private EditText mDataAddressEditText;
	private EditText mTagDataEditText;

	private TagPasswordConfigurationBroadcastReceiver mReceiver;
	private static final byte PARAMETER = 0x40;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_password_configuration);
		setTitle(R.string.title_tag_password_configuration);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mSpecialCodeEditText = (EditText) findViewById(R.id.edit_tag_password_configuration_special_code);
		mDataAddressEditText = (EditText) findViewById(R.id.edit_tag_password_configuration_data_address);
		mTagDataEditText = (EditText) findViewById(R.id.edit_tag_password_configuration_tag_data);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
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
		mReceiver = new TagPasswordConfigurationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_password_configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selectedItemId = item.getItemId();
		switch (selectedItemId) {
			case R.id.menu_tag_password_configuration_config_id:
				if(TextUtils.isEmpty(mSpecialCodeEditText.getText().toString())){
					InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_special_code_not_null);
					break;
				}

				if(TextUtils.isEmpty(mDataAddressEditText.getText().toString())){
					InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_data_address_not_null);
					break;
				}

				if(TextUtils.isEmpty(mTagDataEditText.getText().toString())){
					InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_tag_data_not_null);
					break;
				}

				attemptConfig(mSpecialCodeEditText.getText().toString().trim(), mDataAddressEditText.getText().toString().trim(), mTagDataEditText.getText().toString().trim());
				break;
			case R.id.menu_tag_password_configuration_query_id:
				if(TextUtils.isEmpty(mSpecialCodeEditText.getText().toString())){
					InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_special_code_not_null);
					break;
				}

				if(TextUtils.isEmpty(mDataAddressEditText.getText().toString())){
					InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_data_address_not_null);
					break;
				}

				attemptQuery(mSpecialCodeEditText.getText().toString().trim(), mDataAddressEditText.getText().toString().trim());
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQuery(String specialCodeHex, String dataAddressHex) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] code = Util.convertHexStringToByteArray(specialCodeHex);
		byte[] address = Util.convertHexStringToByteArray(dataAddressHex);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_tag_password_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		QueryTagPassword msg = new QueryTagPassword((byte) antenna, code, address);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void attemptConfig(String specialCodeHex, String dataAddressHex, String tagDataHex) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] code = Util.convertHexStringToByteArray(specialCodeHex);
		byte[] address = Util.convertHexStringToByteArray(dataAddressHex);
		byte[] data = Util.convertHexStringToByteArray(tagDataHex);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_config_tag_password_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		ConfigTagPassword msg = new ConfigTagPassword((byte) antenna, code, address, data);
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
					QueryTagPasswordReceivedInfo response = (QueryTagPasswordReceivedInfo) msg.obj;
					byte[] data = response.getTagPassword();
					mTagDataEditText.setText(Util.convertByteArrayToHexString(data));
					break;
				case CONFIG:
					//do nothing
					break;
				default:
					break;
			}

		};
	};

	private class TagPasswordConfigurationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//配置/查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof QueryTagPasswordReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY;
					msg.obj = obj;
					mHandler.sendMessage(msg);
				}
				InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(TagPasswordConfigDebugActivity.this, R.string.toast_tag_password_configuration_failure);
			}
		}
	}

}
