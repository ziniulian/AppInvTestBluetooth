package com.invengo.rfidpad.scan.taggb;

import invengo.javaapi.core.GBMemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.GBConfigTagLockOrSafeMode;
import invengo.javaapi.protocol.IRP1.GBConfigTagLockOrSafeMode.LockAction;
import invengo.javaapi.protocol.IRP1.GBSelectTag;

import java.util.ArrayList;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import com.invengo.rfidpad.utils.IntegerString;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 国标标签模式配置
 */
public class GBTagModeConfigurationActivity extends PowerManagerActivity {

	private static final int SPINNER_STYLE = android.R.layout.simple_spinner_dropdown_item;
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
	private RadioButton mLockRadioButton;
	private RadioButton mSafetyRadioButton;
	private Spinner mAreaSpinner;
	private ArrayList<IntegerString> mAreaArray = new ArrayList<IntegerString>();
	private Spinner mActionSpinner;
	private LinearLayout mUserdataNoLayout;
	private EditText mUserdataNo;
	private GBMemoryBank bank = GBMemoryBank.GBEPCMemory;

	private ArrayList<IntegerString> mLockModeActionArray = new ArrayList<IntegerString>();
	private ArrayList<IntegerString> mSafetyModeActionArray = new ArrayList<IntegerString>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_gb_operation_mode_configuration);
		setTitle(R.string.text_tag_gb_operation_mode_configuration);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mPasswordEditText = (EditText) findViewById(R.id.edit_gb_operation_mode_configuration_password);
		mPasswordVisibilityButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_gb_operation_mode_configuration_password);
		mLockRadioButton = (RadioButton) findViewById(R.id.radio_gb_operation_mode_configuration_lock_id);
		mSafetyRadioButton = (RadioButton) findViewById(R.id.radio_gb_operation_mode_configuration_safety_id);
		mAreaSpinner = (Spinner) findViewById(R.id.spinner_gb_operation_mode_configuration_area_id);
		mActionSpinner = (Spinner) findViewById(R.id.spinner_gb_operation_mode_configuration_action_id);
		mUserdataNoLayout = (LinearLayout) findViewById(R.id.layout_gb_operation_mode_configuration_userdata_num);
		mUserdataNo = (EditText) findViewById(R.id.edit_gb_operation_mode_configuration_userdata_num);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		initializeWidget();
	}

	private void initializeWidget() {
		String[] lockModeActionArray = getResources().getStringArray(R.array.lock_mode_action);
		ArrayAdapter<IntegerString> lockModeActionAdapter = new ArrayAdapter<IntegerString>(this, R.layout.myspinner);
		lockModeActionAdapter.setDropDownViewResource(SPINNER_STYLE);
		int index = 0;//parameter
		for(String target : lockModeActionArray){
			IntegerString temp = new IntegerString(index++, target);
			mLockModeActionArray.add(temp);
		}
		String[] safeModeActionArray = getResources().getStringArray(R.array.safety_mode_action);
		index = 0;
		for(String target : safeModeActionArray){
			IntegerString temp = new IntegerString(index++, target);
			mSafetyModeActionArray.add(temp);
		}

		lockModeActionAdapter.addAll(mLockModeActionArray);
		mActionSpinner.setAdapter(lockModeActionAdapter);

		String[] areaArray = getResources().getStringArray(R.array.mode_configuration_area);
		ArrayAdapter<IntegerString> areaAdapter = new ArrayAdapter<IntegerString>(this, R.layout.myspinner);
		areaAdapter.setDropDownViewResource(SPINNER_STYLE);
		index = 0;//parameter
		for(String session : areaArray){
			IntegerString temp = new IntegerString(index++, session);
			mAreaArray.add(temp);
		}
		areaAdapter.addAll(mAreaArray);
		mAreaSpinner.setAdapter(areaAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_gb_operation_mode_configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_gb_operation_mode_configuration_config_id:
				configMode();
				break;
			default:
				break;
		}
		return true;
	}

	private void configMode() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		if(TextUtils.isEmpty(mPasswordEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_mode_configuration_password);
			return;
		}
		IntegerString areaSelected = (IntegerString) mAreaSpinner.getSelectedItem();
		if(areaSelected.getIndex() == 3){
			if(TextUtils.isEmpty(mUserdataNo.getText().toString())){
				InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_mode_configuration_password);
				return;
			}
			int bankId = Integer.parseInt(mUserdataNo.getText().toString());
			if(bankId < 0 || bankId > 15){
				InvengoUtils.showToast(this, R.string.toast_tag_gb_operation_mode_configuration_num_scope);
				return;
			}
			bank = InvengoUtils.getUserBank(bankId);
		}

		//参数
		int antenna = mSettingsCollection.getAntenna();
		String password = mPasswordEditText.getText().toString().trim();
		GBMemoryBank area = bank;

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
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_gb_mode_configuration_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		GBConfigTagLockOrSafeMode message = null;
		int index = ((IntegerString)mActionSpinner.getSelectedItem()).getIndex();
		if(mLockRadioButton.isChecked()){//锁模式
			LockAction action = null;
			if(index == 0){
				action = LockAction.Read_Write_GB;
			}else if(index == 1){
				action = LockAction.Read_Only_GB;
			}else if(index == 2){
				action = LockAction.Write_Only_GB;
			}else if(index == 3){
				action = LockAction.No_Read_Write_GB;
			}
			message = new GBConfigTagLockOrSafeMode(antenna, password, area, action);
		}else if(mSafetyRadioButton.isChecked()){//安全模式
			message = new GBConfigTagLockOrSafeMode(antenna, password, area, index);
		}

		final GBConfigTagLockOrSafeMode msg = message;

		new Thread(new Runnable() {
			@Override
			public void run() {
				GBSelectTag selectTagMessage = new GBSelectTag(finalMatchingBank, selectTarget, selectRule, selectHeadAddress, tagID);
				if(mHolder.getCurrentReader().send(selectTagMessage)){
					OperationTask task = new OperationTask(GBTagModeConfigurationActivity.this);
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

	private void addListener() {
		mAreaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				IntegerString selected = (IntegerString) mAreaSpinner.getItemAtPosition(position);
				if(selected.getIndex() == 0){
					bank = GBMemoryBank.GBEPCMemory;
					mUserdataNoLayout.setVisibility(View.GONE);
				}else if(selected.getIndex() == 1){
					bank = GBMemoryBank.GBTidMemory;
					mUserdataNoLayout.setVisibility(View.GONE);
				}else if(selected.getIndex() == 2){
					bank = GBMemoryBank.GBReservedMemory;
					mUserdataNoLayout.setVisibility(View.GONE);
				}else if (selected.getIndex() == 3) {
					bank = GBMemoryBank.GBUser1Memory;
					mUserdataNoLayout.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

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

		mLockRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					ArrayAdapter<IntegerString> lockModeActionAdapter = ((ArrayAdapter<IntegerString>)mActionSpinner.getAdapter());
					lockModeActionAdapter.clear();
					lockModeActionAdapter.addAll(mLockModeActionArray);
					mActionSpinner.setAdapter(lockModeActionAdapter);
				}
			}
		});

		mSafetyRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					ArrayAdapter<IntegerString> safetyModeActionAdapter = ((ArrayAdapter<IntegerString>)mActionSpinner.getAdapter());
					safetyModeActionAdapter.clear();
					safetyModeActionAdapter.addAll(mSafetyModeActionArray);
					mActionSpinner.setAdapter(safetyModeActionAdapter);
				}
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

		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}


	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private static final int FAILURE = 1;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case FAILURE:
					int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
					InvengoUtils.showToast(GBTagModeConfigurationActivity.this, R.string.toast_tag_gb_operation_mode_configuration_failure);
					break;
				default:
					break;
			}
		};
	};

	private class TagOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//读取成功
				InvengoUtils.showToast(GBTagModeConfigurationActivity.this, R.string.toast_tag_gb_operation_mode_configuration_success);
			}else{
				//读取失败
				InvengoUtils.showToast(GBTagModeConfigurationActivity.this, R.string.toast_tag_gb_operation_mode_configuration_failure);
			}
		}
	}
}
