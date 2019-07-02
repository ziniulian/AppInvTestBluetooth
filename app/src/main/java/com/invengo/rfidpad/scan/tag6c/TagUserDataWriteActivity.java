package com.invengo.rfidpad.scan.tag6c;

import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.scan.Tag6COperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

public class TagUserDataWriteActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TextView mMatchDataTextView;
	private TextView mMatchAreaTextView;
	private TagOperationBroadcastReceiver mReceiver;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private EditText mPasswordEditText;
	private ToggleButton mPasswordVisibilityButton;
	private EditText mAddressEditText;
	private EditText mLengthEditText;
	private EditText mDataEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_userdata_write);
		setTitle(R.string.text_tag_operation_write_userdata);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_write_userdata_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_write_userdata);
		mAddressEditText = (EditText) findViewById(R.id.edit_tag_operation_write_userdata_address);
		mLengthEditText = (EditText) findViewById(R.id.edit_tag_operation_write_userdata_len);
		mDataEditText = (EditText) findViewById(R.id.edit_tag_operation_write_userdata_data);
//		mDataEditText.addTextChangedListener(new HexTextWatcher(mDataEditText));

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
		addListener();
	}

	private void addListener() {
		mPasswordVisibilityButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mPasswordEditText.setSelection(mPasswordEditText.length());
			}
		});
	}


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
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_userdata_password);
			return;
		}
		if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_userdata_address);
			return;
		}
		if(TextUtils.isEmpty(mLengthEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_userdata_len);
			return;
		}
		if(TextUtils.isEmpty(mDataEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_userdata_data);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] password = Util.convertHexStringToByteArray(mPasswordEditText.getText().toString());
		int address = Integer.parseInt(mAddressEditText.getText().toString());
		byte[] userData = Util.convertHexStringToByteArray(mDataEditText.getText().toString());
		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_write_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		WriteUserData_6C msg = new WriteUserData_6C((byte)antenna, password, (byte) address, userData, tagID, tagIDType);
//		msg.setTimeOut(3 * 1000);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void initializeData() {
		Intent intent = getIntent();
		String matchData = intent.getStringExtra(Tag6COperationActivity.TAG_6C_OPERATION_DATA);
		mMatchDataTextView.setText(matchData);
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
			mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_epc);
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
			mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_tid);
		}

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
				InvengoUtils.showToast(TagUserDataWriteActivity.this, R.string.toast_tag_operation_write_userdata_success);
			}else{
				//写入失败
				InvengoUtils.showToast(TagUserDataWriteActivity.this, R.string.toast_tag_operation_write_userdata_failure);
			}
		}
	}

}
