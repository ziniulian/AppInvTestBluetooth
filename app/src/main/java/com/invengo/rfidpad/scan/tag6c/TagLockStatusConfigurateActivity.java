package com.invengo.rfidpad.scan.tag6c;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import invengo.javaapi.protocol.IRP1.LockMemoryBank_6C;

public class TagLockStatusConfigurateActivity extends PowerManagerActivity {

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
	private RadioButton mLockTypeButton;
	private RadioButton mUnlockTypeButton;
	private Spinner mLockAreaSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_lock_status);
		setTitle(R.string.text_tag_operation_lock_status);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_lock_status_access_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_lock_status);
		mLockTypeButton = (RadioButton) findViewById(R.id.radio_tag_operation_lock_status_lock);
		mUnlockTypeButton = (RadioButton) findViewById(R.id.radio_tag_operation_lock_status_unlock);
		mLockAreaSpinner = (Spinner) findViewById(R.id.spinner_tag_operation_lock_status_area);

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
		getMenuInflater().inflate(R.menu.menu_tag_operation_lock_status, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_lock_status_config_id:
				configurateLockStatus();
				break;
			default:
				break;
		}
		return true;
	}

	private void configurateLockStatus() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_lock_status_access_password);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] accessPassword = Util.convertHexStringToByteArray(mPasswordEditText.getText().toString());
		int type = -1;
		if(mLockTypeButton.isChecked()){
			type = 0;
		}else if(mUnlockTypeButton.isChecked()){
			type = 1;
		}

		int bank = mLockAreaSpinner.getSelectedItemPosition();
		Log.i(getLocalClassName(), "bank-" + bank);

		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_password_configurate_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		LockMemoryBank_6C msg = new LockMemoryBank_6C((byte)antenna, accessPassword, (byte)type, (byte)bank, tagID, tagIDType);
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

		mLockTypeButton.setChecked(true);
		mUnlockTypeButton.setChecked(false);

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerAdapter.add(getString(R.string.label_spinner_tag_operation_lock_status_area_all));
		spinnerAdapter.add(getString(R.string.label_spinner_tag_operation_lock_status_area_tid));
		spinnerAdapter.add(getString(R.string.label_spinner_tag_operation_lock_status_area_epc));
		spinnerAdapter.add(getString(R.string.label_spinner_tag_operation_lock_status_area_userdata));
		spinnerAdapter.add(getString(R.string.label_spinner_tag_operation_lock_status_area_access_password));
		spinnerAdapter.add(getString(R.string.label_spinner_tag_operation_lock_status_area_destroy_password));
		mLockAreaSpinner.setAdapter(spinnerAdapter);
		mLockAreaSpinner.setSelection(2);

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
				InvengoUtils.showToast(TagLockStatusConfigurateActivity.this, R.string.toast_tag_operation_lock_status_configurate_success);
			}else{
				//写入失败
				InvengoUtils.showToast(TagLockStatusConfigurateActivity.this, R.string.toast_tag_operation_lock_status_configurate_failure);
			}
		}
	}
}
