package com.invengo.rfidpad.scan.taggb;

import invengo.javaapi.core.GBMemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.GBInactivateTag;
import invengo.javaapi.protocol.IRP1.GBSelectTag;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.invengo.rfidpad.scan.TagGBOperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 标签灭活
 */
public class GBTagInactivateActivity extends PowerManagerActivity {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_gb_operation_inactivate);
		setTitle(R.string.text_tag_gb_operation_inactivate);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_inactivate_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_inactivate_gb);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_gb_operation_inactivate_tag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_gb_operation_inactivate_tag_id:
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
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_password);
			return;
		}

		int antenna = mSettingsCollection.getAntenna();
		String password = mPasswordEditText.getText().toString().trim();

		//标签匹配,含匹配数据,匹配区域,匹配目标、匹配规则、匹配首地址
		final byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		GBMemoryBank matchingBank = GBMemoryBank.GBEPCMemory;
		if (mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED
				|| mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED
				|| mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED) {//盘存、组合读、全区域读为EPC
			//
		}else if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){
			if(mSettingsCollection.isGbEpc()){
				//
			}else if(mSettingsCollection.isGbTid()){
				matchingBank = GBMemoryBank.GBTidMemory;
			}else if(mSettingsCollection.isGbUserdata()){
				matchingBank = InvengoUtils.getUserBank(mSettingsCollection.getGbUserdataNo());
			}
		}
		final GBMemoryBank finalMatchingBank = matchingBank;

		final int selectTarget = 4;
		final int selectRule = 0;
		final int selectHeadAddress = 0;

		//进度条
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_inactivate_gb_tag_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		final GBInactivateTag msg = new GBInactivateTag(antenna, password);
		new Thread(new Runnable() {
			@Override
			public void run() {
				GBSelectTag selectTagMessage = new GBSelectTag(finalMatchingBank, selectTarget, selectRule, selectHeadAddress, tagID);
				if(mHolder.getCurrentReader().send(selectTagMessage)){
					OperationTask task = new OperationTask(GBTagInactivateActivity.this);
					task.execute(msg);
				}else{
					Message message = new Message();
					message.what = FAILURE;
					handler.sendMessage(message);
				}
			}
		}).start();

	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeData();
		addListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeData() {
		Intent intent = getIntent();
		String matchData = intent.getStringExtra(TagGBOperationActivity.TAG_GB_OPERATION_DATA);
		mMatchDataTextView.setText(matchData);
		if (mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED
				|| mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED
				|| mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED) {//盘存、组合读、全区域读为EPC
			mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_epc_gb);
		}else if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){
			if(mSettingsCollection.isGbEpc()){
				mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_epc_gb);
			}else if(mSettingsCollection.isGbTid()){
				mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_tid_gb);
			}else if(mSettingsCollection.isGbUserdata()){
				mMatchAreaTextView.setText(R.string.text_tag_operation_match_area_userdata_gb);
			}
		}

		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
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

	private class TagOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//读取成功
				InvengoUtils.showToast(GBTagInactivateActivity.this, R.string.toast_tag_gb_operation_inactivate_success);
			}else{
				//读取失败
				InvengoUtils.showToast(GBTagInactivateActivity.this, R.string.toast_tag_gb_operation_inactivate_failure);
			}
		}
	}

	private static final int FAILURE = 0;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case FAILURE:
					int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
					InvengoUtils.showToast(GBTagInactivateActivity.this, R.string.toast_tag_gb_operation_write_userdata_select_failure);
					break;
				default:
					break;
			}
		};
	};

}
