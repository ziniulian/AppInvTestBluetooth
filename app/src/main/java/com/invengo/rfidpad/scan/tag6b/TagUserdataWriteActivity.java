package com.invengo.rfidpad.scan.tag6b;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.WriteUserData2_6B;
import invengo.javaapi.protocol.IRP1.WriteUserData_6B;
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
import android.widget.RadioButton;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.scan.Tag6BOperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 写6B标签数据
 */
public class TagUserdataWriteActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TextView mMatchDataTextView;
	private TextView mMatchAreaTextView;
	private TagOperationBroadcastReceiver mReceiver;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private EditText mAddressEditText;
	private EditText mDataEditText;
	//	private CheckBox mTypeCheckBox;
	private RadioButton mFixedRadioButton;
	private RadioButton mNonFixedRadioButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6b_operation_userdata_write);
		setTitle(R.string.text_tag_6b_operation_write_userdata);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mAddressEditText = (EditText) findViewById(R.id.edit_tag_6b_operation_write_userdata_address);
		mDataEditText = (EditText) findViewById(R.id.edit_tag_6b_operation_write_userdata_data);
//		mTypeCheckBox = (CheckBox) findViewById(R.id.checkbox_tag_6b_operation_write_userdata);
		mFixedRadioButton = (RadioButton) findViewById(R.id.radio_tag_6b_operation_write_userdata_fixed);
		mNonFixedRadioButton = (RadioButton) findViewById(R.id.radio_radio_tag_6b_operation_write_userdata_non_fixed);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
//		addListener();
	}

//	private void addListener() {
//		mTypeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				if(isChecked){
//					mTypeCheckBox.setText(R.string.label_tag_6b_operation_write_fixed_userdata_checked);
//				}else{
//					mTypeCheckBox.setText(R.string.label_tag_6b_operation_write_userdata_checked);
//				}
//			}
//		});
//	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_operation_userdata_write, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_userdata_write_id:
				writeUserData();
				break;
			default:
				break;
		}
		return true;
	}

	private void writeUserData() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_userdata_address);
			return;
		}
		int address = Integer.parseInt(mAddressEditText.getText().toString());
//		if(address < 8){
//			InvengoUtils.showToast(this, R.string.toast_tag_6b_operation_write_userdata_address_min_eight);
//			return;
//		}
		if(mFixedRadioButton.isChecked()){//写固定数据时地址必须为4的倍数
			if((address % 4) != 0){
				InvengoUtils.showToast(this, R.string.toast_tag_6b_operation_write_userdata_address_notice);
				return;
			}
		}

		if(TextUtils.isEmpty(mDataEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_userdata_data);
			return;
		}
		byte[] userData = Util.convertHexStringToByteArray(mDataEditText.getText().toString());

//		int antenna = mSettingsCollection.getAntenna();
		byte antenna = 0x01;
		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_write_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		if(mFixedRadioButton.isChecked()){//write fixed data
			WriteUserData_6B msg = new WriteUserData_6B((byte)antenna, tagID, (byte)address, userData);
			OperationTask task = new OperationTask(this);
			task.execute(msg);
		}else if(mNonFixedRadioButton.isChecked()){//write non-fixed
			WriteUserData2_6B msg = new WriteUserData2_6B((byte)antenna, tagID, (byte)address, userData);
			OperationTask task = new OperationTask(this);
			task.execute(msg);
		}
	}

	private void initializeData() {
		Intent intent = getIntent();
		String matchData = intent.getStringExtra(Tag6BOperationActivity.TAG_6B_OPERATION_DATA);
		mMatchDataTextView.setText(matchData);
		mMatchAreaTextView.setText(R.string.text_tag_6b_operation_match_area_tid);

		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private class TagOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//写入成功
				InvengoUtils.showToast(TagUserdataWriteActivity.this, R.string.toast_tag_operation_write_userdata_success);
			}else{
				//写入失败
				InvengoUtils.showToast(TagUserdataWriteActivity.this, R.string.toast_tag_operation_write_userdata_failure);
			}
		}
	}
}
