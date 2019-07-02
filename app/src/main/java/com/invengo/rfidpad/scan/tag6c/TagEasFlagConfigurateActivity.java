package com.invengo.rfidpad.scan.tag6c;

import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.EasConfig_6C;
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

public class TagEasFlagConfigurateActivity extends PowerManagerActivity {

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
	private RadioButton mCancleButton;
	private RadioButton mSettingButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_easflag_configurate);
		setTitle(R.string.text_tag_operation_easflag_configurate);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_easflag_configurate_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_easflag_configurate);
		mCancleButton = (RadioButton) findViewById(R.id.radio_tag_operation_eas_flag_cancle);
		mSettingButton = (RadioButton) findViewById(R.id.radio_tag_operation_eas_flat_setting);

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
		getMenuInflater().inflate(R.menu.menu_tag_operation_eas_flag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_eas_flag_id:
				configEasFlag();
				break;
			default:
				break;
		}
		return true;
	}

	private void configEasFlag() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_eas_flag_password);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] password = Util.convertHexStringToByteArray(mPasswordEditText.getText().toString());
		int flag = -1;
		if(mCancleButton.isChecked()){
			flag = 0;
		}else if(mSettingButton.isChecked()){
			flag = 1;
		}
		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_eas_flag_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		EasConfig_6C msg = new EasConfig_6C((byte)antenna, password, (byte)flag, tagID, tagIDType);
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

		mCancleButton.setChecked(true);
		mSettingButton.setChecked(false);

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
				InvengoUtils.showToast(TagEasFlagConfigurateActivity.this, R.string.toast_tag_operation_eas_flag_success);
			}else{
				//写入失败
				InvengoUtils.showToast(TagEasFlagConfigurateActivity.this, R.string.toast_tag_operation_eas_flag_failure);
			}
		}
	}

}
