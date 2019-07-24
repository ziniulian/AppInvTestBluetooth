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
import com.invengo.rfidpad.base.VoiceManager;
import com.invengo.rfidpad.scan.TagGBOperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.GBMemoryBank;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.GBDynamicWriteTag;
import invengo.javaapi.protocol.IRP1.GBEraseTag;
import invengo.javaapi.protocol.IRP1.GBSelectTag;
import invengo.javaapi.protocol.IRP1.GBWriteTag;
import invengo.javaapi.protocol.IRP1.PowerOff_800;

/**
 * 写国标标签
 */
public class GBTagWriteActivity extends PowerManagerActivity {

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
	private EditText mLengthEditText;
	private EditText mDataEditText;
	private VoiceManager mVoiceManager;
	private TextView mNumberTextView;

	private boolean isReading = false;
	private int result = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_gb_operation_userdata_write);
		setTitle(R.string.text_tag_gb_operation_write_userdata);

		mVoiceManager = VoiceManager.getInstance(getApplicationContext());
		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_write_userdata_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_write_userdata_gb);
		mUserdataNumberEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_write_userdata_number);
		mAddressEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_write_userdata_address);
		mLengthEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_write_userdata_len);
		mDataEditText = (EditText) findViewById(R.id.edit_tag_gb_operation_write_userdata_data);
		mNumberTextView = (TextView) findViewById(R.id.textview_tag_gb_operation_dynamic_write_number_id);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	private Menu mOperationMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.mOperationMenu = menu;
		getMenuInflater().inflate(R.menu.menu_tag_gb_operation_userdata_write, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_gb_operation_userdata_write_id:
				writeUserData();
				break;
			case R.id.menu_tag_gb_operation_userdata_erase_id:
				eraseUserData();
				break;
			case R.id.menu_tag_gb_operation_userdata_dynamic_write_id:
				if(!isReading){//开始
					dynamicWriteUserdata();
				}else{//停止
					stopDynamicWriteUserdata();
					//				item.setTitle(R.string.menu_tag_gb_operation_userdata_dynamic_write_label);
				}
				break;
			default:
				break;
		}

		return true;
	}

	private void stopDynamicWriteUserdata() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				isReading = false;
				mHolder.getCurrentReader().send(new PowerOff_800());
				mHolder.getCurrentReader().send(new PowerOff_800());
				mHolder.getCurrentReader().send(new PowerOff_800());
				Message message = new Message();
				message.what = STOP_SUCCESS;
				handler.sendMessage(message);
			}
		}).start();
	}

	private void dynamicWriteUserdata() {
		if(!validateParamData()){
			return;
		}

		if(TextUtils.isEmpty(mDataEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_data);
			return;
		}
		//参数,含天线，访问密码，读取用户区域，首地址，写入的数据
		byte[] userData = Util.convertHexStringToByteArray(mDataEditText.getText().toString());
		if((userData.length % 2) != 0){//双字节
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_data_double_byte);
			return;
		}
		int antenna = mSettingsCollection.getAntenna();
		String password = mPasswordEditText.getText().toString().trim();
		int bankId = Integer.parseInt(mUserdataNumberEditText.getText().toString());
		GBMemoryBank bank = InvengoUtils.getUserBank(bankId);
		int address = Integer.parseInt(mAddressEditText.getText().toString());

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

		final GBDynamicWriteTag msg = new GBDynamicWriteTag(antenna, password, bank, address, userData);
		new Thread(new Runnable() {
			@Override
			public void run() {
				GBSelectTag selectTagMessage = new GBSelectTag(finalMatchingBank, selectTarget, selectRule, selectHeadAddress, tagID);
				if(mHolder.getCurrentReader().send(selectTagMessage)){
					mHolder.getCurrentReader().send(msg);
					isReading = true;
					result = 0;
					Message message = new Message();
					message.what = START_SUCCESS;
					handler.sendMessage(message);
				}else{
					Message message = new Message();
					message.what = FAILURE;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	private boolean validateParamData(){
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return false;
		}
		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_password);
			return false;
		}
		if(TextUtils.isEmpty(mUserdataNumberEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_number_null);
			return false;
		}
		int bankId = Integer.parseInt(mUserdataNumberEditText.getText().toString());
		if(bankId < 0 || bankId > 15){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_number_scope);
			return false;
		}
		if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_address);
			return false;
		}
		return true;
	}

	private void eraseUserData() {
		if(!validateParamData()){
			return;
		}

		if(TextUtils.isEmpty(mLengthEditText.getText().toString().trim())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_len_even_number);
			return;
		}
		int length = Integer.parseInt(mLengthEditText.getText().toString().trim());
		if((length % 2) != 0){//偶数
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_len_even_number);
			return;
		}
		//参数,含天线，访问密码，首地址，写入的数据
		int bankId = Integer.parseInt(mUserdataNumberEditText.getText().toString());
		int antenna = mSettingsCollection.getAntenna();
		String password = mPasswordEditText.getText().toString().trim();
		GBMemoryBank bank = InvengoUtils.getUserBank(bankId);
		int address = Integer.parseInt(mAddressEditText.getText().toString());

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
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_write_gb_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		final GBEraseTag msg = new GBEraseTag(antenna, password, bank, address, length);
		new Thread(new Runnable() {
			@Override
			public void run() {
				GBSelectTag selectTagMessage = new GBSelectTag(finalMatchingBank, selectTarget, selectRule, selectHeadAddress, tagID);
				if(mHolder.getCurrentReader().send(selectTagMessage)){
					OperationTask task = new OperationTask(GBTagWriteActivity.this);
					task.execute(msg);
				}else{
					Message message = new Message();
					message.what = FAILURE;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	private void writeUserData() {
		if(!validateParamData()){
			return;
		}

		if(TextUtils.isEmpty(mDataEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_data);
			return;
		}
		//参数,含天线，访问密码，读取用户区域，首地址，写入的数据
		byte[] userData = Util.convertHexStringToByteArray(mDataEditText.getText().toString());
		if((userData.length % 2) != 0){//双字节
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_write_userdata_data_double_byte);
			return;
		}
		int antenna = mSettingsCollection.getAntenna();
		String password = mPasswordEditText.getText().toString().trim();
		int bankId = Integer.parseInt(mUserdataNumberEditText.getText().toString());
		GBMemoryBank bank = InvengoUtils.getUserBank(bankId);
		int address = Integer.parseInt(mAddressEditText.getText().toString());

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
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_write_gb_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		//发送消息
		final GBWriteTag msg = new GBWriteTag(antenna, password, bank, address, userData);
		if((userData.length / 2) <= 0x20){//不需要分段写入,先选择再写入
			new Thread(new Runnable() {
				@Override
				public void run() {
					GBSelectTag selectTagMessage = new GBSelectTag(finalMatchingBank, selectTarget, selectRule, selectHeadAddress, tagID);
					if(mHolder.getCurrentReader().send(selectTagMessage)){
						OperationTask task = new OperationTask(GBTagWriteActivity.this);
						task.execute(msg);
					}else{
						Message message = new Message();
						message.what = FAILURE;
						handler.sendMessage(message);
					}
				}
			}).start();
		}else if((userData.length / 2) > 0x20){//需要分段写入
			msg.enableSelectTag(tagID, matchingBank, selectTarget, selectRule, selectHeadAddress);
			OperationTask task = new OperationTask(this);
			task.execute(msg);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopDynamicWriteUserdata();
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
				InvengoUtils.showToast(GBTagWriteActivity.this, R.string.toast_tag_gb_operation_write_userdata_success);
			}else{
				//读取失败
				InvengoUtils.showToast(GBTagWriteActivity.this, R.string.toast_tag_gb_operation_write_userdata_failure);
			}
		}
	}

	private static final int FAILURE = 0;
	private static final int START_SUCCESS = 1;
	private static final int RESULT = 2;
	private static final int STOP_SUCCESS = 3;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case START_SUCCESS:
					mOperationMenu.findItem(R.id.menu_tag_gb_operation_userdata_dynamic_write_id).setTitle(R.string.menu_tag_gb_operation_userdata_dynamic_write_stop_label);
					InvengoUtils.showToast(GBTagWriteActivity.this, R.string.toast_tag_gb_operation_write_userdata_start_dynamic);
					break;
				case FAILURE:
					int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
					InvengoUtils.showToast(GBTagWriteActivity.this, R.string.toast_tag_gb_operation_write_userdata_select_failure);
					break;
				case RESULT:
					int result = (Integer) msg.obj;
					mNumberTextView.setText(String.valueOf(result));
					break;
				case STOP_SUCCESS:
					mOperationMenu.findItem(R.id.menu_tag_gb_operation_userdata_dynamic_write_id).setTitle(R.string.menu_tag_gb_operation_userdata_dynamic_write_label);
					break;
				default:
					break;
			}
		};
	};

	private void playTagSound(){
		if(mSettingsCollection.isVoiced()){
			mVoiceManager.playSound(Contants.TAG_SOUND, Contants.SOUND_NO_LOOP_MODE);
		}
	}

	@Override
	public void handleNotificationMessage(BaseReader reader, IMessageNotification msg) {
		if(isReading){
			if(msg instanceof GBDynamicWriteTag){
				playTagSound();
				result += 1;
				Message message = new Message();
				message.what = RESULT;
				message.obj = result;
				handler.sendMessage(message);
			}
		}
	};

}
