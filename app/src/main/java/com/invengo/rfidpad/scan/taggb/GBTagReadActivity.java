package com.invengo.rfidpad.scan.taggb;

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

import invengo.javaapi.core.GBMemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.GBAccessReadTag;
import invengo.javaapi.protocol.IRP1.GBSelectTag;
import invengo.javaapi.protocol.receivedInfo.GBAccessReadReceivedInfo;

/**
 * 读国标标签
 */
public class GBTagReadActivity extends PowerManagerActivity {

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
	private EditText mUserdataNumberEditText;
	private EditText mAddressEditText;
	private EditText mLenEditText;
	private EditText mDataEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_gb_operation_userdata_read);
		setTitle(R.string.text_tag_gb_operation_read_userdata);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_read_userdata_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_read_userdata_gb);
		mUserdataNumberEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_read_userdata_number);
		mAddressEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_read_userdata_address);
		mLenEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_read_userdata_len);
		mDataEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_read_userdata_data);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_gb_operation_userdata_read, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_gb_operation_userdata_read_id:
				readUserdata();
				break;
			case R.id.menu_tag_gb_operation_userdata_clear_id:
				clearUserdata();
				break;

			default:
				break;
		}
		return true;
	}

	private void readUserdata() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_read_userdata_password);
			return;
		}
		if(TextUtils.isEmpty(mUserdataNumberEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_read_userdata_number_null);
			return;
		}
		int bankId = Integer.parseInt(mUserdataNumberEditText.getText().toString());
		if(bankId < 0 || bankId > 15){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_read_userdata_number_scope);
			return;
		}
		if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_read_userdata_address);
			return;
		}
		if(TextUtils.isEmpty(mLenEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_read_userdata_len);
			return;
		}

		//参数,含天线，访问密码，读取用户区域，首地址，长度
		int antenna = mSettingsCollection.getAntenna();
		String password = mPasswordEditText.getText().toString().trim();
		GBMemoryBank bank = InvengoUtils.getUserBank(bankId);
		int address = Integer.parseInt(mAddressEditText.getText().toString());
		int length = Integer.parseInt(mLenEditText.getText().toString());

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
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_read_gb_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		//发送消息
		final GBAccessReadTag msg = new GBAccessReadTag((byte) antenna, password, bank, address, length);
		if(length <= 0x20){//不需要分段读取,现选择再读取
			new Thread(new Runnable() {
				@Override
				public void run() {
					GBSelectTag selectTagMessage = new GBSelectTag(finalMatchingBank, selectTarget, selectRule, selectHeadAddress, tagID);
					if(mHolder.getCurrentReader().send(selectTagMessage)){
						OperationTask task = new OperationTask(GBTagReadActivity.this);
						task.execute(msg);
					}else{
						Message message = new Message();
						message.what = FAILURE;
						handler.sendMessage(message);
					}
				}
			}).start();
		}else if(length > 0x20){//需要分段读取
			msg.enableSelectTag(tagID, matchingBank, selectTarget, selectRule, selectHeadAddress);
			OperationTask task = new OperationTask(this);
			task.execute(msg);
		}
	}


	private void clearUserdata() {
		Message msg = new Message();
		msg.what = CLEAR_USERDATA;
		handler.sendMessage(msg);
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

		mDataEditText.setText("");

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
			GBAccessReadReceivedInfo obj = (GBAccessReadReceivedInfo) intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
			if(resultCode == 0){
				//读取成功
				InvengoUtils.showToast(GBTagReadActivity.this, R.string.toast_tag_gb_operation_read_userdata_success);
				Message msg = new Message();
				msg.what = UPDATE_USERDATA;
				msg.obj = obj;
				handler.sendMessage(msg);
			}else{
				//读取失败
				InvengoUtils.showToast(GBTagReadActivity.this, R.string.toast_tag_gb_operation_read_userdata_failure);
			}
		}
	}

	private static final int UPDATE_USERDATA = 0;
	private static final int CLEAR_USERDATA = 1;
	private static final int FAILURE = 2;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case UPDATE_USERDATA:
					GBAccessReadReceivedInfo obj = (GBAccessReadReceivedInfo) msg.obj;
					String newUserData = getString(R.string.label_tag_operation_read_gb_userdata_data_show) + Util.convertByteArrayToHexString(obj.getTagData());
					String oldUserData = mDataEditText.getText().toString();
					StringBuffer dataBuffer = new StringBuffer();
					if(TextUtils.isEmpty(oldUserData)){
						dataBuffer.append(newUserData);
					}else{
						dataBuffer.append(oldUserData);
						dataBuffer.append("\n");
						dataBuffer.append(newUserData);
					}
					mDataEditText.setText(dataBuffer.toString());
					break;
				case CLEAR_USERDATA:
					mDataEditText.setText("");
					break;
				case FAILURE:
					int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
					InvengoUtils.showToast(GBTagReadActivity.this, R.string.toast_tag_gb_operation_read_userdata_failure);
					break;
				default:
					break;
			}
		};
	};

}
