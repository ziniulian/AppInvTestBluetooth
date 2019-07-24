package com.invengo.rfidpad.scan.tag6c;

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
import android.widget.RadioButton;
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

import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.BlockWrite_6C;

public class TagBankWriteActivity extends PowerManagerActivity {

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
	private RadioButton mEpcButton;
	private RadioButton mTidButton;
	private RadioButton mUserDataButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_bank_write);
		setTitle(R.string.text_tag_operation_write_bank);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_write_bank_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_write_bank);
		mAddressEditText = (EditText) findViewById(R.id.edit_tag_operation_write_bank_address);
		mLengthEditText = (EditText) findViewById(R.id.edit_tag_operation_write_bank_len);
		mDataEditText = (EditText) findViewById(R.id.edit_tag_operation_write_bank_data);
		//		mDataEditText.addTextChangedListener(new HexTextWatcher(mDataEditText));

		mEpcButton = (RadioButton) findViewById(R.id.radio_tag_operation_write_bank_area_epc);
		mTidButton = (RadioButton) findViewById(R.id.radio_tag_operation_write_bank_area_tid);
		mUserDataButton = (RadioButton) findViewById(R.id.radio_tag_operation_write_bank_area_usedata);

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
		getMenuInflater().inflate(R.menu.menu_tag_operation_bank_write, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_bank_write_id:
				writeBank();
				break;
			default:
				break;
		}
		return true;
	}

	private void writeBank() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_bank_password);
			return;
		}
		if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_bank_address);
			return;
		}
		if(TextUtils.isEmpty(mLengthEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_bank_len);
			return;
		}
		if(TextUtils.isEmpty(mDataEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_write_bank_data);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] password = Util.convertHexStringToByteArray(mPasswordEditText.getText().toString());
		int address = Integer.parseInt(mAddressEditText.getText().toString());
		int length = Integer.parseInt(mLengthEditText.getText().toString());
		MemoryBank bank = null;
		if(mEpcButton.isChecked()){
			bank = MemoryBank.EPCMemory;
		}else if(mTidButton.isChecked()){
			bank = MemoryBank.TIDMemory;
		}else if(mUserDataButton.isChecked()){
			bank = MemoryBank.UserMemory;
		}
		byte[] data = Util.convertHexStringToByteArray(mDataEditText.getText().toString());
		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_write_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		BlockWrite_6C msg = new BlockWrite_6C((byte)antenna, password, bank, address, (byte) length, data, tagID, tagIDType);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void initializeData() {
		Intent intent = getIntent();
		String matchData = intent.getStringExtra(Tag6COperationActivity.TAG_6C_OPERATION_DATA);
		mMatchDataTextView.setText(matchData);
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED) {
			mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_epc);
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
			mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_tid);
		}

		mEpcButton.setChecked(true);
		mTidButton.setChecked(false);
		mUserDataButton.setChecked(false);

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
				InvengoUtils.showToast(TagBankWriteActivity.this, R.string.toast_tag_operation_write_bank_success);
			}else{
				//写入失败
				InvengoUtils.showToast(TagBankWriteActivity.this, R.string.toast_tag_operation_write_bank_failure);
			}
		}
	}

}
