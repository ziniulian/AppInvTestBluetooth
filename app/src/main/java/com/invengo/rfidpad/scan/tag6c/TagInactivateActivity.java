package com.invengo.rfidpad.scan.tag6c;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.KillTag_6C;
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
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

public class TagInactivateActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TagOperationBroadcastReceiver mReceiver;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private EditText mPasswordEditText;
	private ToggleButton mPasswordVisibilityButton;
	private EditText mEpcEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_inactivate);
		setTitle(R.string.text_tag_operation_inactivate_tag);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_operation_inactivate_tag_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_inactivate_tag);
		mEpcEditText = (EditText) findViewById(R.id.edit_tag_operation_inactivate_tag_epc);

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
		getMenuInflater().inflate(R.menu.menu_tag_operation_inactivate_tag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_inactivate_tag_config_id:
				inactivateTag();
				break;
			default:
				break;
		}
		return true;
	}

	private void inactivateTag() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_inactivate_tag_password);
			return;
		}
		if(TextUtils.isEmpty(mEpcEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_inactivate_tag_epc);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		byte[] accessPassword = Util.convertHexStringToByteArray(mPasswordEditText.getText().toString());
		byte[] epc = Util.convertHexStringToByteArray(mEpcEditText.getText().toString());

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_inactivate_tag_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		KillTag_6C msg = new KillTag_6C((byte) antenna, accessPassword, epc);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void initializeData() {
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
				InvengoUtils.showToast(TagInactivateActivity.this, R.string.toast_tag_operation_inactivate_tag_success);
			}else{
				//写入失败
				InvengoUtils.showToast(TagInactivateActivity.this, R.string.toast_tag_operation_inactivate_tag_failure);
			}
		}
	}

}
