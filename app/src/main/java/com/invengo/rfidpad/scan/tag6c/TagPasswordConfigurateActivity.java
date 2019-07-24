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
import invengo.javaapi.protocol.IRP1.AccessPwdConfig_6C;
import invengo.javaapi.protocol.IRP1.BaseMessage;
import invengo.javaapi.protocol.IRP1.KillPwdConfig_6C;

public class TagPasswordConfigurateActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TextView mMatchDataTextView;
	private TextView mMatchAreaTextView;
	private TagOperationBroadcastReceiver mReceiver;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private EditText mAccessPasswordEditText;
	private ToggleButton mPasswordVisibilityButton;
	private EditText mNewPasswordEditText;
	private ToggleButton mNewPasswordVisibilityButton;
	private EditText mConfirmPasswordEditText;
	private ToggleButton mConfirmPasswordVisibilityButton;
	private RadioButton mAccessTypeButton;
	private RadioButton mDestroyTypeButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_password_configurate);
		setTitle(R.string.text_tag_operation_password_configurate);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mAccessTypeButton = (RadioButton) findViewById(R.id.radio_tag_operation_password_configurate_access);
		mDestroyTypeButton = (RadioButton) findViewById(R.id.radio_tag_operation_password_configurate_destroy);
		mAccessPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_password_configurate_access_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_password_configurate_old);
		mNewPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_password_configurate_new_password);
		mNewPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_password_configurate_new);
		mConfirmPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_password_configurate_confirm_password);
		mConfirmPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_password_configurate_confirm);

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
					mAccessPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mAccessPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mAccessPasswordEditText.setSelection(mAccessPasswordEditText.length());
			}
		});
		mNewPasswordVisibilityButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mNewPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mNewPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mNewPasswordEditText.setSelection(mNewPasswordEditText.length());
			}
		});
		mConfirmPasswordVisibilityButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mConfirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mConfirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mConfirmPasswordEditText.setSelection(mConfirmPasswordEditText.length());
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
		getMenuInflater().inflate(R.menu.menu_tag_operation_password_configurate, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_password_configurate_config_id:
				configuratePassword();
				break;
			default:
				break;
		}
		return true;
	}

	private void configuratePassword() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mAccessPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_password_configurate_access_password);
			return;
		}
		if(TextUtils.isEmpty(mNewPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_password_configurate_new_password);
			return;
		}
		if(TextUtils.isEmpty(mConfirmPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_password_configurate_confirm_password);
			return;
		}
		if(!(mNewPasswordEditText.getText().toString()).equals(mConfirmPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_password_configurate_different_password);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] accessPassword = Util.convertHexStringToByteArray(mAccessPasswordEditText.getText().toString());
		byte[] newPassword = Util.convertHexStringToByteArray(mNewPasswordEditText.getText().toString());

		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}
		BaseMessage msg = null;
		if(mAccessTypeButton.isChecked()){
			msg = new AccessPwdConfig_6C((byte)antenna, accessPassword, newPassword, tagID, tagIDType);
		}else if(mDestroyTypeButton.isChecked()){
			msg = new KillPwdConfig_6C((byte)antenna, accessPassword, newPassword, tagID, tagIDType);
		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_password_configurate_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
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

		mAccessTypeButton.setChecked(true);
		mDestroyTypeButton.setChecked(false);

		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private class TagOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {

			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//写入成功
				if(mAccessTypeButton.isChecked()){
					InvengoUtils.showToast(TagPasswordConfigurateActivity.this, R.string.toast_tag_operation_password_configurate_access_success);
				}else if(mDestroyTypeButton.isChecked()){
					InvengoUtils.showToast(TagPasswordConfigurateActivity.this, R.string.toast_tag_operation_password_configurate_destroy_success);
				}
			}else{
				//写入失败
				if(mAccessTypeButton.isChecked()){
					InvengoUtils.showToast(TagPasswordConfigurateActivity.this, R.string.toast_tag_operation_password_configurate_access_failure);
				}else if(mDestroyTypeButton.isChecked()){
					InvengoUtils.showToast(TagPasswordConfigurateActivity.this, R.string.toast_tag_operation_password_configurate_destroy_failure);
				}
			}
		}
	}

}
