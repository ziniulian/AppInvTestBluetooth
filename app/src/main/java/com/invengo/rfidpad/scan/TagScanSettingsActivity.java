package com.invengo.rfidpad.scan;

import invengo.javaapi.protocol.IRP1.SysConfig_800;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.IntegerString;
import com.invengo.rfidpad.utils.InvengoUtils;

public class TagScanSettingsActivity extends PowerManagerActivity {

	private static final String TAG = TagScanSettingsActivity.class.getSimpleName();
	private static final int SPINNER_STYLE = android.R.layout.simple_spinner_dropdown_item;
	private ReaderHolder mReaderHolder;
	private Bundle mSavedInstanceState;
	//	private CheckBox mAntennaOne;
	private CheckBox mVoiced;
	private CheckBox mRssi;
	//	private CheckBox mUtc;
	private EditText mQText;
	private RadioButton mSingleRadioButton;
	private RadioButton mLoopRadioButton;
	private RadioButton mTag6CRadioButton;
	private RadioButton mTag6BRadioButton;
	private RadioButton mTag6C6BRadioButton;
	private RadioButton mTagGBRadioButton;
	private Switch mReadEpc;
	private Switch mReadTid;
	private Switch mReadAll;
	private EditText mTidLen;
	private EditText mUserDataAddress;
	private EditText mUserDataLen;
	private TagScanSettingsCollection mSettingsCollection;

	private LinearLayout mContainer6C;
	private LinearLayout mContainer6B;
	private LinearLayout mContainer6C6B;
	private LinearLayout mContainerGB;

	private Switch mReadId6B;
	private Switch mReadIdUserData6B;
	private EditText mTidLen6B;
	private EditText mUserDataAddress6B;
	private EditText mUserDataLen6B;

	private EditText mTidLen6C6B;
	private EditText mUserDataAddress6C6B;
	private EditText mUserDataLen6C6B;

	private Switch mInventory;
	private Switch mAccessRead;
	private Switch mCombinationRead;
	private Switch mAllRead;

	private LinearLayout mGBInventory;
	private Spinner mTargetSpinner;
	private ArrayList<IntegerString> mTargetArray = new ArrayList<IntegerString>();
	private Spinner mSessionSpinner;
	private ArrayList<IntegerString> mSessionArray = new ArrayList<IntegerString>();
	private Spinner mConditionSpinner;
	private ArrayList<IntegerString> mConditionArray = new ArrayList<IntegerString>();

	private LinearLayout mGBAccessRead;
	private EditText mAccessReadPassword;
	private ToggleButton mAccessReadPasswordButton;
	//	private Spinner mAccessReadBank;
	private RadioButton mAccessReadGBEpc;
	private RadioButton mAccessReadGBTid;
	private RadioButton mAccessReadGBUserdata;
	private EditText mAccessReadAddress;
	private EditText mAccessReadLen;
	private LinearLayout mAccessReadNoLayout;
	private EditText mAccessReadNo;

	private LinearLayout mGBCombinationRead;
	private EditText mCombinationReadTidPassword;
	private ToggleButton mCombinationReadTidPasswordButton;
	private EditText mCombinationReadTidLen;

	private LinearLayout mGBAllRead;
	private EditText mAllReadTidLen;
	private EditText mAllReadTidPassword;
	private ToggleButton mAllReadTidPasswordButton;
	private EditText mAllReadEpcLen;
	private Spinner mAllReadUserdataBankNoSpinner;
	private ArrayList<IntegerString> mAllReadUserdataBankNoArray = new ArrayList<IntegerString>();
	private EditText mALlReadUserdataAddress;
	private EditText mAllReadUserdataLen;
	private EditText mAllReadUserdataPassword;
	private ToggleButton mAllReadUserdataPasswordButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		InvengoLog.i(TAG, "INFO.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_scan_settings);
		setTitle(R.string.title_tag_scan_settings);

		mReaderHolder = ReaderHolder.getInstance();
		mSettingsCollection = TagScanSettingsCollection.getInstance();

		//		mAntennaOne = (CheckBox) findViewById(R.id.checkBox_antenna_one);
		mVoiced = (CheckBox) findViewById(R.id.checkBox_notice_voice);
		mRssi = (CheckBox) findViewById(R.id.checkBox_rssi);
		//		mUtc = (CheckBox) findViewById(R.id.checkBox_utc);
		mQText = (EditText) findViewById(R.id.edit_text_q);

		mSingleRadioButton = (RadioButton) findViewById(R.id.radio_tag_scan_setting_single);
		mLoopRadioButton = (RadioButton) findViewById(R.id.radio_tag_scan_setting_loop);
		mTag6CRadioButton = (RadioButton) findViewById(R.id.radio_tag_scan_6c);
		mTag6BRadioButton = (RadioButton) findViewById(R.id.radio_tag_scan_6b);
		mTag6C6BRadioButton = (RadioButton) findViewById(R.id.radio_tag_scan_6c_6b);
		mTagGBRadioButton = (RadioButton) findViewById(R.id.radio_tag_scan_gb);

		mContainer6C = (LinearLayout) findViewById(R.id.container6c);
		mContainer6B = (LinearLayout) findViewById(R.id.container6b);
		mContainer6C6B = (LinearLayout) findViewById(R.id.container6c6b);
		mContainerGB = (LinearLayout) findViewById(R.id.containergb);

		mReadEpc = (Switch) findViewById(R.id.switch_read_epc);
		mReadTid = (Switch) findViewById(R.id.switch_read_tid);
		mReadAll = (Switch) findViewById(R.id.switch_read_all);

		mTidLen = (EditText) findViewById(R.id.edit_text_tid_len);
		mUserDataAddress = (EditText) findViewById(R.id.edit_text_userdata_address);
		mUserDataLen = (EditText) findViewById(R.id.edit_text_userdata_len);

		mReadId6B = (Switch) findViewById(R.id.switch_read_id_6b);
		mReadIdUserData6B = (Switch) findViewById(R.id.switch_read_userdata_6b);
		mTidLen6B = (EditText) findViewById(R.id.edit_text_id_len_6b);
		mUserDataAddress6B = (EditText) findViewById(R.id.edit_text_userdata_address_6b);
		mUserDataLen6B = (EditText) findViewById(R.id.edit_text_userdata_len_6b);

		mTidLen6C6B = (EditText) findViewById(R.id.edit_text_id_len_6c_6b);
		mUserDataAddress6C6B = (EditText) findViewById(R.id.edit_text_userdata_address_6c_6b);
		mUserDataLen6C6B = (EditText) findViewById(R.id.edit_text_userdata_len_6c_6b);

		mInventory = (Switch) findViewById(R.id.switch_gb_inventory);
		mGBInventory = (LinearLayout) findViewById(R.id.layout_gb_inventory_parameter);
		mTargetSpinner = (Spinner) findViewById(R.id.spinner_gb_inventory_parameter_target_id);
		mSessionSpinner = (Spinner) findViewById(R.id.spinner_gb_inventory_parameter_session_id);
		mConditionSpinner = (Spinner) findViewById(R.id.spinner_gb_inventory_parameter_condition_id);

		mAccessRead = (Switch) findViewById(R.id.switch_gb_access_read);
		mGBAccessRead = (LinearLayout) findViewById(R.id.layout_gb_access_read_parameter);
		mAccessReadPassword = (EditText) findViewById(R.id.edit_gb_access_read_parameter_password);
		mAccessReadPasswordButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_gb_access_read_parameter_password);
		//		mAccessReadBank = (Spinner) findViewById(R.id.spinner_gb_access_read_parameter_bank_id);
		mAccessReadGBEpc = (RadioButton) findViewById(R.id.radio_gb_access_read_parameter_epc);
		mAccessReadGBTid = (RadioButton) findViewById(R.id.radio_gb_access_read_parameter_tid);
		mAccessReadGBUserdata = (RadioButton) findViewById(R.id.radio_gb_access_read_parameter_userdata);
		mAccessReadAddress = (EditText) findViewById(R.id.edit_gb_access_read_parameter_address);
		mAccessReadLen = (EditText) findViewById(R.id.edit_gb_access_read_parameter_len);
		mAccessReadNoLayout = (LinearLayout) findViewById(R.id.layout_gb_access_read_parameter_userdata_num);
		mAccessReadNo = (EditText) findViewById(R.id.edit_gb_access_read_parameter_userdata_num);

		mCombinationRead = (Switch) findViewById(R.id.switch_gb_combination_read);
		mGBCombinationRead = (LinearLayout) findViewById(R.id.layout_gb_combination_read_parameter);
		mCombinationReadTidPassword = (EditText) findViewById(R.id.edit_gb_combination_read_parameter_password);
		mCombinationReadTidPasswordButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_gb_combination_read_parameter_password);
		mCombinationReadTidLen = (EditText) findViewById(R.id.edit_gb_combination_read_parameter_len);

		mAllRead = (Switch) findViewById(R.id.switch_gb_read_all);
		mGBAllRead = (LinearLayout) findViewById(R.id.layout_gb_read_all_parameter);
		mAllReadTidLen = (EditText) findViewById(R.id.edit_gb_read_all_parameter_tid_len);
		mAllReadTidPassword = (EditText) findViewById(R.id.edit_gb_read_all_parameter_tid_password);
		mAllReadTidPasswordButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_gb_read_all_parameter_tid_password);
		mAllReadEpcLen = (EditText) findViewById(R.id.edit_gb_read_all_parameter_epc_len);
		mAllReadUserdataBankNoSpinner = (Spinner) findViewById(R.id.spinner_gb_read_all_parameter_userdata_bank_id);
		mALlReadUserdataAddress = (EditText) findViewById(R.id.edit_gb_read_all_parameter_userdata_address);
		mAllReadUserdataLen = (EditText) findViewById(R.id.edit_gb_read_all_parameter_userdata_len);
		mAllReadUserdataPassword = (EditText) findViewById(R.id.edit_gb_read_all_parameter_userdata_password);
		mAllReadUserdataPasswordButton = (ToggleButton) findViewById(R.id.toggle_button_eye_visibility_gb_read_all_parameter_userdata_password);

		initializeWidget();
	}

	@Override
	protected void onResume() {
		super.onResume();
		InvengoLog.i(TAG, "INFO.onResume()");

		initializeSettings();
		if(null != mSavedInstanceState){
			//			mAntennaOne.setChecked(this.mSavedInstanceState.getBoolean("ANTENNA"));
			mVoiced.setChecked(this.mSavedInstanceState.getBoolean("VOICED"));
			mRssi.setChecked(this.mSavedInstanceState.getBoolean("RSSI"));
			//			mUtc.setChecked(this.mSavedInstanceState.getBoolean("UTC"));
			mQText.setText(String.valueOf(this.mSavedInstanceState.getInt("QVALUE")));
			mSingleRadioButton.setChecked(!this.mSavedInstanceState.getBoolean("LOOP"));
			mLoopRadioButton.setChecked(this.mSavedInstanceState.getBoolean("LOOP"));
			if(this.mSavedInstanceState.getBoolean("TAGGB")){
				mSingleRadioButton.setEnabled(true);
				mLoopRadioButton.setEnabled(true);
			}else{
				mSingleRadioButton.setEnabled(false);
				mLoopRadioButton.setEnabled(false);
			}

			//6C
			mTag6CRadioButton.setChecked(this.mSavedInstanceState.getBoolean("TAG6C"));
			mReadEpc.setChecked(this.mSavedInstanceState.getBoolean("READEPC6C"));
			mReadTid.setChecked(this.mSavedInstanceState.getBoolean("READTID6C"));
			mReadAll.setChecked(this.mSavedInstanceState.getBoolean("READALL6C"));
			mTidLen.setText(String.valueOf(this.mSavedInstanceState.getInt("TIDLEN6C")));
			mUserDataAddress.setText(String.valueOf(this.mSavedInstanceState.getInt("ADDRESS6C")));
			mUserDataLen.setText(String.valueOf(this.mSavedInstanceState.getInt("USERDATALEN6C")));
			//6B
			mTag6BRadioButton.setChecked(this.mSavedInstanceState.getBoolean("TAG6B"));
			mReadId6B.setChecked(this.mSavedInstanceState.getBoolean("READID6B"));
			mReadIdUserData6B.setChecked(this.mSavedInstanceState.getBoolean("READALL6B"));
			mTidLen6B.setText(String.valueOf(this.mSavedInstanceState.getInt("IDLEN6B")));
			mUserDataAddress6B.setText(String.valueOf(this.mSavedInstanceState.getInt("ADDRESS6B")));
			mUserDataLen6B.setText(String.valueOf(this.mSavedInstanceState.getInt("USERDATALEN6B")));
			//6C&6B
			mTag6C6BRadioButton.setChecked(this.mSavedInstanceState.getBoolean("TAG6C6B"));
			mTidLen6C6B.setText(String.valueOf(this.mSavedInstanceState.getInt("TIDLEN6C6B")));
			mUserDataAddress6C6B.setText(String.valueOf(this.mSavedInstanceState.getInt("ADDRESS6C6B")));
			mUserDataLen6C6B.setText(String.valueOf(this.mSavedInstanceState.getInt("USERDATALEN6C6B")));

			//GB
			mTagGBRadioButton.setChecked(this.mSavedInstanceState.getBoolean("TAGGB"));

			//Inventory
			boolean inventoryVisible = this.mSavedInstanceState.getBoolean("GBINVENTORY");
			mInventory.setChecked(inventoryVisible);
			mGBInventory.setVisibility(inventoryVisible ? View.VISIBLE : View.GONE);
			mTargetSpinner.setSelection(this.mSavedInstanceState.getInt("TARGETINVENTORY"));
			mSessionSpinner.setSelection(this.mSavedInstanceState.getInt("SESSIONINVENTORY"));
			mConditionSpinner.setSelection(this.mSavedInstanceState.getInt("CONDITIONINVENTORY"));

			//Access Read
			boolean accessRead = this.mSavedInstanceState.getBoolean("GBACCESS");
			mAccessRead.setChecked(accessRead);
			mGBAccessRead.setVisibility(accessRead ? View.VISIBLE : View.GONE);
			mAccessReadPassword.setText(this.mSavedInstanceState.getString("PASSWORDACCESS"));
			mAccessReadAddress.setText(String.valueOf(this.mSavedInstanceState.getInt("ADDRESSACCESS")));
			mAccessReadLen.setText(String.valueOf(this.mSavedInstanceState.getInt("LENACCESS")));
			mAccessReadGBEpc.setChecked(this.mSavedInstanceState.getBoolean("EPCACCESS"));
			mAccessReadGBTid.setChecked(this.mSavedInstanceState.getBoolean("TIDACCESS"));
			mAccessReadGBUserdata.setChecked(this.mSavedInstanceState.getBoolean("USERDATAACCESS"));
			if(this.mSavedInstanceState.getBoolean("USERDATAACCESS")){
				mAccessReadNoLayout.setVisibility(View.VISIBLE);
				mAccessReadNo.setText(String.valueOf(this.mSavedInstanceState.getInt("NOACCESS")));
			}else{
				mAccessReadNoLayout.setVisibility(View.GONE);
			}

			//Combination Read
			boolean combinationRead = this.mSavedInstanceState.getBoolean("GBCOMBINATION");
			mCombinationRead.setChecked(combinationRead);
			mGBCombinationRead.setVisibility(combinationRead ? View.VISIBLE : View.GONE);
			mCombinationReadTidPassword.setText(this.mSavedInstanceState.getString("TIDPASSWORDCOMBINATION"));
			mCombinationReadTidLen.setText(String.valueOf(this.mSavedInstanceState.getInt("TIDLENCOMBINATION")));

			//All Read
			boolean allRead = this.mSavedInstanceState.getBoolean("GBALL");
			mAllRead.setChecked(allRead);
			mGBAllRead.setVisibility(allRead ? View.VISIBLE : View.GONE);
			mAllReadTidLen.setText(String.valueOf(this.mSavedInstanceState.getInt("TIDLENALL")));
			mAllReadTidPassword.setText(this.mSavedInstanceState.getString("TIDPASSWORDALL"));
			mAllReadEpcLen.setText(String.valueOf(this.mSavedInstanceState.getInt("EPCLENALL")));
			mAllReadUserdataBankNoSpinner.setSelection(this.mSavedInstanceState.getInt("USERDATANOALL"));
			mALlReadUserdataAddress.setText(String.valueOf(this.mSavedInstanceState.getInt("USERDATAADDRESSALL")));
			mAllReadUserdataLen.setText(String.valueOf(this.mSavedInstanceState.getInt("USERDATALENALL")));
			mAllReadUserdataPassword.setText(this.mSavedInstanceState.getString("USERDATAPASSWORDALL"));

			int container6CVisibility = this.mSavedInstanceState.getBoolean("TAG6C") == true ? View.VISIBLE : View.GONE;
			int container6BVisibility = this.mSavedInstanceState.getBoolean("TAG6B") == true ? View.VISIBLE : View.GONE;
			int container6C6BVisibility = this.mSavedInstanceState.getBoolean("TAG6C6B") == true ? View.VISIBLE : View.GONE;
			int containerGBVisibility = this.mSavedInstanceState.getBoolean("TAGGB") == true ? View.VISIBLE : View.GONE;
			mContainer6C.setVisibility(container6CVisibility);
			mContainer6B.setVisibility(container6BVisibility);
			mContainer6C6B.setVisibility(container6C6BVisibility);
			mContainerGB.setVisibility(containerGBVisibility);
		}
		addListener();
		//		attemptQueryRssi();
	}

	private void initializeWidget() {
		//		//parameter
		//		int[] parameterArray = getResources().getIntArray(R.array.inventory_parameter);
		//Inventory
		String[] targetArray = getResources().getStringArray(R.array.inventory_target);
		ArrayAdapter<IntegerString> targetAdapter = new ArrayAdapter<IntegerString>(this, R.layout.myspinner);
		targetAdapter.setDropDownViewResource(SPINNER_STYLE);
		int index = 0;//parameter
		for(String target : targetArray){
			IntegerString temp = new IntegerString(index++, target);
			mTargetArray.add(temp);
		}
		targetAdapter.addAll(mTargetArray);
		mTargetSpinner.setAdapter(targetAdapter);

		String[] sessionArray = getResources().getStringArray(R.array.inventory_session);
		ArrayAdapter<IntegerString> sessionAdapter = new ArrayAdapter<IntegerString>(this, R.layout.myspinner);
		sessionAdapter.setDropDownViewResource(SPINNER_STYLE);
		index = 0;//parameter
		for(String session : sessionArray){
			IntegerString temp = new IntegerString(index++, session);
			mSessionArray.add(temp);
		}
		sessionAdapter.addAll(mSessionArray);
		mSessionSpinner.setAdapter(sessionAdapter);

		String[] conditionArray = getResources().getStringArray(R.array.inventory_condition);
		ArrayAdapter<IntegerString> conditionAdapter = new ArrayAdapter<IntegerString>(this, R.layout.myspinner);
		conditionAdapter.setDropDownViewResource(SPINNER_STYLE);
		index = 0;//parameter
		for(String condition : conditionArray){
			IntegerString temp = new IntegerString(index++, condition);
			mConditionArray.add(temp);
		}
		conditionAdapter.addAll(mConditionArray);
		mConditionSpinner.setAdapter(conditionAdapter);

		String[] allReadUserdataBankArray = getResources().getStringArray(R.array.all_read_userdata_bank);
		ArrayAdapter<IntegerString> allReadUserdataBankAdapter = new ArrayAdapter<IntegerString>(this, R.layout.myspinner);
		allReadUserdataBankAdapter.setDropDownViewResource(SPINNER_STYLE);
		index = 0;//parameter
		for(String condition : allReadUserdataBankArray){
			IntegerString temp = new IntegerString(index++, condition);
			mAllReadUserdataBankNoArray.add(temp);
		}
		allReadUserdataBankAdapter.addAll(mAllReadUserdataBankNoArray);
		mAllReadUserdataBankNoSpinner.setAdapter(allReadUserdataBankAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_scan_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_scan_settings_save:
				if(mTag6CRadioButton.isChecked()){
					if(!mReadEpc.isChecked() && !mReadTid.isChecked() && !mReadAll.isChecked()){
						InvengoUtils.showToast(this, R.string.toast_tag_scan_settings_read_type_select_one);
						break;
					}
				}else if(mTag6BRadioButton.isChecked()){
					if(!mReadId6B.isChecked() && !mReadIdUserData6B.isChecked()){
						InvengoUtils.showToast(this, R.string.toast_tag_scan_settings_read_type_select_one);
						break;
					}
				}else if(mTagGBRadioButton.isChecked()){
					if(!mInventory.isChecked() && !mAccessRead.isChecked() && !mCombinationRead.isChecked() && !mAllRead.isChecked()){
						InvengoUtils.showToast(this, R.string.toast_tag_scan_settings_read_type_select_one);
						break;
					}
				}
				attemptConfigRssi(mRssi.isChecked());
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptSettingsSave(){
		//		mSettingsCollection.setAntennaOne(mAntennaOne.isChecked());//Antenna
		mSettingsCollection.setVoiced(mVoiced.isChecked());//Voiced
		mSettingsCollection.setRssi(mRssi.isChecked());
		//		mSettingsCollection.setUtc(mUtc.isChecked());

		int q = 6;
		if(!TextUtils.isEmpty(mQText.getText().toString().trim())){
			q = Integer.parseInt(mQText.getText().toString().trim());
		}
		InvengoLog.i(TAG, "INFO.attemptSettingsSave.Q - {%s}", q);
		mSettingsCollection.setQ(q);
		boolean loop = false;
		if(mSingleRadioButton.isChecked()){
			loop = false;
		}else if(mLoopRadioButton.isChecked()){
			loop = true;
		}
		mSettingsCollection.setLoop(loop);//op_type

		int visibility6C = mTag6CRadioButton.isChecked() == true ? View.VISIBLE : View.GONE;
		int visibility6B = mTag6BRadioButton.isChecked() == true ? View.VISIBLE : View.GONE;
		int visibility6C6B = mTag6C6BRadioButton.isChecked() == true ? View.VISIBLE : View.GONE;
		int visibilityGB = mTagGBRadioButton.isChecked() == true ? View.VISIBLE : View.GONE;

		mSettingsCollection.setVisibility_6C(visibility6C);
		mSettingsCollection.setVisibility_6B(visibility6B);
		mSettingsCollection.setVisibility_6C_6B(visibility6C6B);
		mSettingsCollection.setVisibility_GB(visibilityGB);

		//6C
		int readEpcChecked = mReadEpc.isChecked() == true ? Contants.CHECKED : Contants.UNCHECKED;
		int readTidChecked = mReadTid.isChecked() == true ? Contants.CHECKED : Contants.UNCHECKED;
		int readAllChecked = mReadAll.isChecked() == true ? Contants.CHECKED : Contants.UNCHECKED;

		mSettingsCollection.setEpcChecked(readEpcChecked);
		mSettingsCollection.setTidChecked(readTidChecked);
		mSettingsCollection.setUserdataChecked(readAllChecked);

		int tidLen = 8;
		if(!TextUtils.isEmpty(mTidLen.getText().toString().trim())){
			tidLen = Integer.parseInt(mTidLen.getText().toString().trim());
		}
		mSettingsCollection.setTidLen(tidLen);
		int userDataAddress = 0;
		if(!TextUtils.isEmpty(mUserDataAddress.getText().toString().trim())){
			userDataAddress = Integer.parseInt(mUserDataAddress.getText().toString().trim());
		}
		mSettingsCollection.setUserDataAddress(userDataAddress);
		int userDataLen = 8;
		if(!TextUtils.isEmpty(mUserDataLen.getText().toString().trim())){
			userDataLen = Integer.parseInt(mUserDataLen.getText().toString().trim());
		}
		mSettingsCollection.setUserDataLen(userDataLen);

		//6B
		int readIdChecked = mReadId6B.isChecked() == true ? Contants.CHECKED : Contants.UNCHECKED;
		int readIdUserdataChecked = mReadIdUserData6B.isChecked() == true ? Contants.CHECKED : Contants.UNCHECKED;

		mSettingsCollection.setId6BChecked(readIdChecked);
		mSettingsCollection.setUserdata6BChecked(readIdUserdataChecked);

		tidLen = 8;
		if(!TextUtils.isEmpty(mTidLen6B.getText().toString().trim())){
			tidLen = Integer.parseInt(mTidLen6B.getText().toString().trim());
		}
		mSettingsCollection.setTidLen6B(tidLen);
		userDataAddress = 0;
		if(!TextUtils.isEmpty(mUserDataAddress6B.getText().toString().trim())){
			userDataAddress = Integer.parseInt(mUserDataAddress6B.getText().toString().trim());
		}
		mSettingsCollection.setUserDataAddress6B(userDataAddress);
		userDataLen = 8;
		if(!TextUtils.isEmpty(mUserDataLen6B.getText().toString().trim())){
			userDataLen = Integer.parseInt(mUserDataLen6B.getText().toString().trim());
		}
		mSettingsCollection.setUserDataLen6B(userDataLen);

		//6C&6B
		tidLen = 8;
		if(!TextUtils.isEmpty(mTidLen6C6B.getText().toString().trim())){
			tidLen = Integer.parseInt(mTidLen6C6B.getText().toString().trim());
		}
		mSettingsCollection.setTidLen6C6B(tidLen);
		userDataAddress = 0;
		if(!TextUtils.isEmpty(mUserDataAddress6C6B.getText().toString().trim())){
			userDataAddress = Integer.parseInt(mUserDataAddress6C6B.getText().toString().trim());
		}
		mSettingsCollection.setUserDataAddress6C6B(userDataAddress);
		userDataLen = 8;
		if(!TextUtils.isEmpty(mUserDataLen6C6B.getText().toString().trim())){
			userDataLen = Integer.parseInt(mUserDataLen6C6B.getText().toString().trim());
		}
		mSettingsCollection.setUserDataLen6C6B(userDataLen);

		//Inventory
		int inventoryChecked = mInventory.isChecked() ? Contants.CHECKED : Contants.UNCHECKED;
		int target = ((IntegerString)mTargetSpinner.getSelectedItem()).getIndex();
		int session = ((IntegerString)mSessionSpinner.getSelectedItem()).getIndex();
		int condition = ((IntegerString)mConditionSpinner.getSelectedItem()).getIndex();

		mSettingsCollection.setGbInventoryChecked(inventoryChecked);
		mSettingsCollection.setTarget(target);
		mSettingsCollection.setSession(session);
		mSettingsCollection.setCondition(condition);

		//Access Read
		int accessReadChecked = mAccessRead.isChecked() ? Contants.CHECKED : Contants.UNCHECKED;
		String accessReadPassword = TextUtils.isEmpty(mAccessReadPassword
				.getText().toString().trim()) ? "00000000"
				: mAccessReadPassword.getText().toString().trim();
		int accessReadAddress = TextUtils.isEmpty(mAccessReadAddress.getText()
				.toString().trim()) ? 4 : Integer.parseInt(mAccessReadAddress
				.getText().toString().trim());
		int accessReadLen = TextUtils.isEmpty(mAccessReadLen.getText()
				.toString().trim()) ? 32 : Integer.parseInt(mAccessReadLen
				.getText().toString().trim());

		boolean accessReadEpc = mAccessReadGBEpc.isChecked();
		boolean accessReadTid = mAccessReadGBTid.isChecked();
		boolean accessReadUserdata = mAccessReadGBUserdata.isChecked();
		int accessReadUserdataNum = TextUtils.isEmpty(mAccessReadNo.getText()
				.toString().trim()) ? 0 : Integer.parseInt(mAccessReadNo
				.getText().toString().trim());

		mSettingsCollection.setGbAccessReadChecked(accessReadChecked);
		mSettingsCollection.setGbPassword(accessReadPassword);
		mSettingsCollection.setGbAddress(accessReadAddress);
		mSettingsCollection.setGbLen(accessReadLen);
		mSettingsCollection.setGbEpc(accessReadEpc);
		mSettingsCollection.setGbTid(accessReadTid);
		mSettingsCollection.setGbUserdata(accessReadUserdata);
		mSettingsCollection.setGbUserdataNo(accessReadUserdataNum);

		//Combination Read
		int combinationReadChecked = mCombinationRead.isChecked() ? Contants.CHECKED : Contants.UNCHECKED;
		String combinationReadPassword = TextUtils.isEmpty(mCombinationReadTidPassword
				.getText().toString().trim()) ? "00000000"
				: mCombinationReadTidPassword.getText().toString().trim();
		int combinationReadLen = TextUtils.isEmpty(mCombinationReadTidLen.getText()
				.toString().trim()) ? 12 : Integer.parseInt(mCombinationReadTidLen
				.getText().toString().trim());

		mSettingsCollection.setGbCombinationReadChecked(combinationReadChecked);
		mSettingsCollection.setGbTidPassword(combinationReadPassword);
		mSettingsCollection.setGbTidLen(combinationReadLen);

		//All Read
		int allReadChecked = mAllRead.isChecked() ? Contants.CHECKED : Contants.UNCHECKED;
		String allReadTidPassword = TextUtils.isEmpty(mAllReadTidPassword
				.getText().toString().trim()) ? "00000000"
				: mAllReadTidPassword.getText().toString().trim();
		int allReadTidLen = TextUtils.isEmpty(mAllReadTidLen.getText()
				.toString().trim()) ? 8 : Integer.parseInt(mAllReadTidLen
				.getText().toString().trim());
		int allReadEpcLen = TextUtils.isEmpty(mAllReadEpcLen.getText()
				.toString().trim()) ? 6 : Integer.parseInt(mAllReadEpcLen
				.getText().toString().trim());
		int bank = ((IntegerString)mAllReadUserdataBankNoSpinner.getSelectedItem()).getIndex();
		String allReadUserdataPassword = TextUtils.isEmpty(mAllReadUserdataPassword
				.getText().toString().trim()) ? "00000000"
				: mAllReadUserdataPassword.getText().toString().trim();
		int allReadUserdataAddress = TextUtils.isEmpty(mALlReadUserdataAddress
				.getText().toString().trim()) ? 4
				: Integer.parseInt(mALlReadUserdataAddress.getText().toString().trim());
		int allReadUserdataLen = TextUtils.isEmpty(mAllReadUserdataLen
				.getText().toString().trim()) ? 6
				: Integer.parseInt(mAllReadUserdataLen.getText().toString().trim());

		mSettingsCollection.setGbAllReadChecked(allReadChecked);
		mSettingsCollection.setGbAllTidPassword(allReadTidPassword);
		mSettingsCollection.setGbAllTidLen(allReadTidLen);
		mSettingsCollection.setGbAllEpcLen(allReadEpcLen);
		mSettingsCollection.setGbAllUserdataNo(bank);
		mSettingsCollection.setGbAllUserdataPassword(allReadUserdataPassword);
		mSettingsCollection.setGbAllUserdataAddress(allReadUserdataAddress);
		mSettingsCollection.setGbAllUserdataLen(allReadUserdataLen);
	}

	private void addListener() {
		mTag6CRadioButton.setOnClickListener(new OnClickListener() {//6C
			@Override
			public void onClick(View v) {
				if(mTag6CRadioButton.isChecked()){
					mContainer6C.setVisibility(View.VISIBLE);
					mContainer6B.setVisibility(View.GONE);
					mContainer6C6B.setVisibility(View.GONE);
					mContainerGB.setVisibility(View.GONE);
					mSingleRadioButton.setEnabled(false);
					mLoopRadioButton.setEnabled(false);
				}
			}
		});
		mTag6BRadioButton.setOnClickListener(new OnClickListener() {//6B

			@Override
			public void onClick(View v) {
				mContainer6C.setVisibility(View.GONE);
				mContainer6B.setVisibility(View.VISIBLE);
				mContainer6C6B.setVisibility(View.GONE);
				mContainerGB.setVisibility(View.GONE);
				mSingleRadioButton.setEnabled(false);
				mLoopRadioButton.setEnabled(false);
			}
		});
		mTag6C6BRadioButton.setOnClickListener(new OnClickListener() {//6C&6B

			@Override
			public void onClick(View v) {
				mContainer6C.setVisibility(View.GONE);
				mContainer6B.setVisibility(View.GONE);
				mContainer6C6B.setVisibility(View.VISIBLE);
				mContainerGB.setVisibility(View.GONE);
				mSingleRadioButton.setEnabled(false);
				mLoopRadioButton.setEnabled(false);
			}
		});
		mTagGBRadioButton.setOnClickListener(new OnClickListener() {//GB

			@Override
			public void onClick(View v) {
				mContainer6C.setVisibility(View.GONE);
				mContainer6B.setVisibility(View.GONE);
				mContainer6C6B.setVisibility(View.GONE);
				mContainerGB.setVisibility(View.VISIBLE);
				mSingleRadioButton.setEnabled(true);
				mLoopRadioButton.setEnabled(true);
				//				mLoopRadioButton.setSelected(true);
			}
		});
		mReadEpc.setOnCheckedChangeListener(mEpc6C);
		mReadTid.setOnCheckedChangeListener(mTid6C);
		mReadAll.setOnCheckedChangeListener(mAll6C);

		mReadId6B.setOnCheckedChangeListener(mId6B);
		mReadIdUserData6B.setOnCheckedChangeListener(mUserData6B);

		mInventory.setOnCheckedChangeListener(mInventoryListener);
		mAccessRead.setOnCheckedChangeListener(mAccessReadListener);
		mCombinationRead.setOnCheckedChangeListener(mCombinationReadListener);
		mAllRead.setOnCheckedChangeListener(mAllReadListener);

		mAccessReadPasswordButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mAccessReadPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mAccessReadPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mAccessReadPassword.setSelection(mAccessReadPassword.length());
			}
		});

		mCombinationReadTidPasswordButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mCombinationReadTidPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mCombinationReadTidPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mCombinationReadTidPassword.setSelection(mCombinationReadTidPassword.length());
			}
		});

		mAllReadTidPasswordButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mAllReadTidPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mAllReadTidPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mAllReadTidPassword.setSelection(mAllReadTidPassword.length());
			}
		});

		mAllReadUserdataPasswordButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mAllReadUserdataPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else{
					mAllReadUserdataPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mAllReadUserdataPassword.setSelection(mAllReadUserdataPassword.length());
			}
		});

		mAccessReadGBEpc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mAccessReadNoLayout.setVisibility(View.GONE);
			}
		});
		mAccessReadGBTid.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAccessReadNoLayout.setVisibility(View.GONE);
			}
		});
		mAccessReadGBUserdata.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAccessReadNoLayout.setVisibility(View.VISIBLE);
			}
		});
	}

	private void attemptConfigRssi(final boolean isChecked) {
		if(!mReaderHolder.isConnected()){
			InvengoLog.w(TAG, "WARN.Configurate Rssi Failure-Reader Disconnect");
			Message msg = new Message();
			msg.what = CONFIG_RSSI_FAILURE;
			msg.obj = getString(R.string.toast_tag_scan_settings_rssi_failure_reader_disconnect);
			mRssiHandler.sendMessage(msg);
			return;
		}

		InvengoLog.i(TAG, "INFO.Configurate Rssi");
		byte parameter = 0x14;
		byte[] data = new byte[2];
		data[0] = 0x01;
		if(isChecked){
			data[1] = 0x01;
		}else{
			data[1] = 0x00;
		}
		final SysConfig_800 msg = new SysConfig_800(parameter, data);
		msg.setTimeOut(2 * 1000);
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					Message message = new Message();
					if(mReaderHolder.getCurrentReader().send(msg)){
						message.what = CONFIG_RSSI_SUCCESS;
						InvengoLog.i(TAG, "INFO.Configurate Rssi Success");
					}else{
						message.what = CONFIG_RSSI_FAILURE;
						message.obj = getString(R.string.toast_tag_scan_settings_rssi_failure);
						InvengoLog.w(TAG, "WARN.Configurate Rssi Failure");
					}
					mRssiHandler.sendMessage(message);
				}else{
					InvengoLog.w(TAG, "WARN.Configurate RSSI Failure-Reader Disconnect");
				}
			}
		}).start();
	}

	//	private void attemptConfigUtc(final boolean isChecked) {
	//		if(!mReaderHolder.isConnected()){
	//			InvengoLog.w(TAG, "WARN.Configurate Utc Failure-Reader Disconnect");
	//			Message msg = new Message();
	//			msg.what = CONFIG_UTC_FAILURE;
	//			msg.obj = getString(R.string.toast_tag_scan_settings_utc_failure_reader_disconnect);
	//			mRssiHandler.sendMessage(msg);
	//			return;
	//		}
	//
	//		InvengoLog.i(TAG, "INFO.Configurate Utc");
	//		byte parameter = 0x18;
	//		byte[] data = new byte[2];
	//		data[0] = 0x01;
	//		if(isChecked){
	//			data[1] = 0x01;
	//		}else{
	//			data[1] = 0x00;
	//		}
	//		final SysConfig_800 msg = new SysConfig_800(parameter, data);
	//		msg.setTimeOut(2 * 1000);
	//		new Thread(new Runnable() {
	//
	//			@Override
	//			public void run() {
	//				if(mReaderHolder.isConnected()){
	//					Message message = new Message();
	//					if(mReaderHolder.getCurrentReader().send(msg)){
	//						message.what = CONFIG_UTC_SUCCESS;
	//						InvengoLog.i(TAG, "INFO.Configurate UTC Success");
	//					}else{
	//						message.what = CONFIG_UTC_FAILURE;
	//						message.obj = getString(R.string.toast_tag_scan_settings_utc_failure);
	//						InvengoLog.w(TAG, "WARN.Configurate Utc Failure");
	//					}
	//					mRssiHandler.sendMessage(message);
	//				}else{
	//					InvengoLog.w(TAG, "WARN.Configurate UTC Failure-Reader Disconnect");
	//				}
	//			}
	//		}).start();
	//	}

	//	private static final int QUERY_RSSI = 0;
	private static final int CONFIG_RSSI_SUCCESS = 1;
	private static final int CONFIG_RSSI_FAILURE = 2;
	//	private static final int CONFIG_UTC_SUCCESS = 3;
	//	private static final int CONFIG_UTC_FAILURE = 4;
	private Handler mRssiHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				//			case QUERY_RSSI:
				//				boolean result = (Boolean) msg.obj;
				//				mRssi.setChecked(result);
				//				break;
				case CONFIG_RSSI_SUCCESS:
					//				attemptConfigUtc(mUtc.isChecked());
					attemptSettingsSave();
					InvengoUtils.showToast(TagScanSettingsActivity.this, R.string.toast_tag_scan_settings_save_success);
					break;
				case CONFIG_RSSI_FAILURE:
					if(mSettingsCollection.isRssi() != mRssi.isChecked()){//不一致说明Rssi选择框修改了,RSSI选择框返回配置前状态
						mRssi.setChecked(!mRssi.isChecked());
					}
					//提示后不做任何处理
					String reason = (String) msg.obj;
					InvengoUtils.showToast(TagScanSettingsActivity.this, reason);
					break;
				//			case CONFIG_UTC_SUCCESS:
				//				attemptSettingsSave();
				//				InvengoUtils.showToast(TagScanSettingsActivity.this, R.string.toast_tag_scan_settings_save_success);
				//				break;
				//			case CONFIG_UTC_FAILURE:
				//				if(mSettingsCollection.isUtc() != mUtc.isChecked()){//不一致说明Utc选择框修改了,UTC选择框返回配置前状态
				//					mUtc.setChecked(!mUtc.isChecked());
				//				}
				//				attemptRollbackRssi(mSettingsCollection.isRssi());
				//				//提示后不做任何处理
				//				String result = (String) msg.obj;
				//				InvengoUtils.showToast(TagScanSettingsActivity.this, result);
				//				break;
				default:
					break;
			}
		};
	};

	//	protected void attemptRollbackRssi(final boolean rssi) {
	//		new Thread(new Runnable() {
	//
	//			@Override
	//			public void run() {
	//
	//				byte parameter = 0x14;
	//				byte[] data = new byte[2];
	//				data[0] = 0x01;
	//				if(rssi){
	//					data[1] = 0x01;
	//				}else{
	//					data[1] = 0x00;
	//				}
	//				final SysConfig_800 msg = new SysConfig_800(parameter, data);
	//				msg.setTimeOut(2 * 1000);
	//				mReaderHolder.getCurrentReader().send(msg);
	//			}
	//		}).start();
	//	}

	private OnCheckedChangeListener mId6B = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mReadId6B.setChecked(true);
				mReadIdUserData6B.setChecked(false);
				mTidLen6B.setEnabled(false);
				mUserDataAddress6B.setEnabled(false);
				mUserDataLen6B.setEnabled(false);
			}
		}
	};

	private OnCheckedChangeListener mUserData6B = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mReadId6B.setChecked(false);
				mReadIdUserData6B.setChecked(true);
				mTidLen6B.setEnabled(true);
				mUserDataAddress6B.setEnabled(true);
				mUserDataLen6B.setEnabled(true);
			}
		}
	};

	private OnCheckedChangeListener mEpc6C = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mReadAll.setChecked(false);
				mReadTid.setChecked(false);
				mTidLen.setEnabled(false);
				mUserDataAddress.setEnabled(false);
				mUserDataLen.setEnabled(false);
			}
		}
	};

	private OnCheckedChangeListener mTid6C = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mReadEpc.setChecked(false);
				mReadAll.setChecked(false);
				mTidLen.setEnabled(false);
				mUserDataAddress.setEnabled(false);
				mUserDataLen.setEnabled(false);
			}
		}
	};

	private OnCheckedChangeListener mAll6C = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mReadEpc.setChecked(false);
				mReadTid.setChecked(false);
				mTidLen.setEnabled(true);
				mUserDataAddress.setEnabled(true);
				mUserDataLen.setEnabled(true);
			}
		}
	};

	private OnCheckedChangeListener mInventoryListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mGBInventory.setVisibility(View.VISIBLE);
				mGBAccessRead.setVisibility(View.GONE);
				mGBCombinationRead.setVisibility(View.GONE);
				mGBAllRead.setVisibility(View.GONE);

				mAccessRead.setChecked(!isChecked);
				mCombinationRead.setChecked(!isChecked);
				mAllRead.setChecked(!isChecked);
			}else{
				mGBInventory.setVisibility(View.GONE);
			}
		}
	};

	private OnCheckedChangeListener mAccessReadListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mGBInventory.setVisibility(View.GONE);
				mGBAccessRead.setVisibility(View.VISIBLE);
				mGBCombinationRead.setVisibility(View.GONE);
				mGBAllRead.setVisibility(View.GONE);

				mInventory.setChecked(!isChecked);
				mCombinationRead.setChecked(!isChecked);
				mAllRead.setChecked(!isChecked);
			}else{
				mGBAccessRead.setVisibility(View.GONE);
			}
		}
	};

	private OnCheckedChangeListener mCombinationReadListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mGBInventory.setVisibility(View.GONE);
				mGBAccessRead.setVisibility(View.GONE);
				mGBCombinationRead.setVisibility(View.VISIBLE);
				mGBAllRead.setVisibility(View.GONE);

				mInventory.setChecked(!isChecked);
				mAccessRead.setChecked(!isChecked);
				mAllRead.setChecked(!isChecked);
			}else{
				mGBCombinationRead.setVisibility(View.GONE);
			}
		}
	};

	private OnCheckedChangeListener mAllReadListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				mGBInventory.setVisibility(View.GONE);
				mGBAccessRead.setVisibility(View.GONE);
				mGBCombinationRead.setVisibility(View.GONE);
				mGBAllRead.setVisibility(View.VISIBLE);

				mInventory.setChecked(!isChecked);
				mAccessRead.setChecked(!isChecked);
				mCombinationRead.setChecked(!isChecked);
			}else{
				mGBAllRead.setVisibility(View.GONE);
			}
		}
	};

	@SuppressWarnings("unchecked")
	private void initializeSettings() {
		//		mAntennaOne.setChecked(mSettingsCollection.isAntennaOne());
		mVoiced.setChecked(mSettingsCollection.isVoiced());
		mRssi.setChecked(mSettingsCollection.isRssi());
		//		mUtc.setChecked(mSettingsCollection.isUtc());
		mQText.setText(String.valueOf(mSettingsCollection.getQ()));

		boolean loop = mSettingsCollection.isLoop();
		mSingleRadioButton.setChecked(!loop);
		mLoopRadioButton.setChecked(loop);
		if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){
			mSingleRadioButton.setEnabled(true);
			mLoopRadioButton.setEnabled(true);
		}else{
			mSingleRadioButton.setEnabled(false);
			mLoopRadioButton.setEnabled(false);
		}

		mContainer6C.setVisibility(mSettingsCollection.getVisibility_6C());
		mContainer6B.setVisibility(mSettingsCollection.getVisibility_6B());
		mContainer6C6B.setVisibility(mSettingsCollection.getVisibility_6C_6B());
		mContainerGB.setVisibility(mSettingsCollection.getVisibility_GB());

		boolean tag6CSelected = (mSettingsCollection.getVisibility_6C() == View.VISIBLE);
		boolean tag6BSelected = (mSettingsCollection.getVisibility_6B() == View.VISIBLE);
		boolean tag6C6BSelected = (mSettingsCollection.getVisibility_6C_6B() == View.VISIBLE);
		boolean tagGBSelected = (mSettingsCollection.getVisibility_GB() == View.VISIBLE);
		mTag6CRadioButton.setChecked(tag6CSelected);
		mTag6BRadioButton.setChecked(tag6BSelected);
		mTag6C6BRadioButton.setChecked(tag6C6BSelected);
		mTagGBRadioButton.setChecked(tagGBSelected);

		//6C
		boolean readEpcChecked = (mSettingsCollection.getEpcChecked() == Contants.CHECKED);
		boolean readTidChecked = (mSettingsCollection.getTidChecked() == Contants.CHECKED);
		boolean readAllChecked = (mSettingsCollection.getUserdataChecked() == Contants.CHECKED);
		mReadEpc.setChecked(readEpcChecked);
		mReadTid.setChecked(readTidChecked);
		mReadAll.setChecked(readAllChecked);
		if(readAllChecked){
			mTidLen.setEnabled(true);
			mUserDataAddress.setEnabled(true);
			mUserDataLen.setEnabled(true);
		}else{
			mTidLen.setEnabled(false);
			mUserDataAddress.setEnabled(false);
			mUserDataLen.setEnabled(false);
		}
		mTidLen.setText(String.valueOf(mSettingsCollection.getTidLen()));
		mUserDataAddress.setText(String.valueOf(mSettingsCollection.getUserDataAddress()));
		mUserDataLen.setText(String.valueOf(mSettingsCollection.getUserDataLen()));

		//6B
		boolean readId6BChecked = (mSettingsCollection.getId6BChecked() == Contants.CHECKED);
		boolean readUserdata6BChecked = (mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED);
		mReadId6B.setChecked(readId6BChecked);
		mReadIdUserData6B.setChecked(readUserdata6BChecked);
		if(readUserdata6BChecked){
			mTidLen6B.setEnabled(true);
			mUserDataAddress6B.setEnabled(true);
			mUserDataLen6B.setEnabled(true);
		}else{
			mTidLen6B.setEnabled(false);
			mUserDataAddress6B.setEnabled(false);
			mUserDataLen6B.setEnabled(false);
		}
		mTidLen6B.setText(String.valueOf(mSettingsCollection.getTidLen6B()));
		mUserDataAddress6B.setText(String.valueOf(mSettingsCollection.getUserDataAddress6B()));
		mUserDataLen6B.setText(String.valueOf(mSettingsCollection.getUserDataLen6B()));

		//6C&6B
		mTidLen6C6B.setText(String.valueOf(mSettingsCollection.getTidLen6C6B()));
		mUserDataAddress6C6B.setText(String.valueOf(mSettingsCollection.getUserDataAddress6C6B()));
		mUserDataLen6C6B.setText(String.valueOf(mSettingsCollection.getUserDataLen6C6B()));

		//GB
		//Inventory
		boolean inventory = (mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED);
		mInventory.setChecked(inventory);
		mGBInventory.setVisibility(mSettingsCollection.getGbInventoryChecked());
		ArrayAdapter<IntegerString> targetAdapter = (ArrayAdapter<IntegerString>) mTargetSpinner.getAdapter();
		for(IntegerString item : mTargetArray){
			if(item.getIndex() == mSettingsCollection.getTarget()){
				mTargetSpinner.setSelection(targetAdapter.getPosition(item));
				break;
			}
		}
		ArrayAdapter<IntegerString> sessionAdapter = (ArrayAdapter<IntegerString>) mSessionSpinner.getAdapter();
		for(IntegerString item : mSessionArray){
			if(item.getIndex() == mSettingsCollection.getSession()){
				mSessionSpinner.setSelection(sessionAdapter.getPosition(item));
				break;
			}
		}
		ArrayAdapter<IntegerString> conditionAdapter = (ArrayAdapter<IntegerString>) mConditionSpinner.getAdapter();
		for(IntegerString item : mConditionArray){
			if(item.getIndex() == mSettingsCollection.getCondition()){
				mConditionSpinner.setSelection(conditionAdapter.getPosition(item));
				break;
			}
		}

		//Access Read
		boolean accessRead = (mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED);
		mAccessRead.setChecked(accessRead);
		mGBAccessRead.setVisibility(mSettingsCollection.getGbAccessReadChecked());
		mAccessReadPassword.setText(mSettingsCollection.getGbPassword());
		mAccessReadAddress.setText(String.valueOf(mSettingsCollection.getGbAddress()));
		mAccessReadLen.setText(String.valueOf(mSettingsCollection.getGbLen()));
		mAccessReadGBEpc.setChecked(mSettingsCollection.isGbEpc());
		mAccessReadGBTid.setChecked(mSettingsCollection.isGbTid());
		mAccessReadGBUserdata.setChecked(mSettingsCollection.isGbUserdata());
		if(mSettingsCollection.isGbUserdata()){
			mAccessReadNoLayout.setVisibility(View.VISIBLE);
			mAccessReadNo.setText(String.valueOf(mSettingsCollection.getGbUserdataNo()));
		}else{
			mAccessReadNoLayout.setVisibility(View.GONE);
		}

		//Combination Read
		boolean combinationRead = (mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED);
		mCombinationRead.setChecked(combinationRead);
		mGBCombinationRead.setVisibility(mSettingsCollection.getGbCombinationReadChecked());
		mCombinationReadTidPassword.setText(mSettingsCollection.getGbTidPassword());
		mCombinationReadTidLen.setText(String.valueOf(mSettingsCollection.getGbTidLen()));

		//All Read
		boolean allRead = (mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED);
		mAllRead.setChecked(allRead);
		mGBAllRead.setVisibility(mSettingsCollection.getGbAllReadChecked());
		mAllReadTidLen.setText(String.valueOf(mSettingsCollection.getGbAllTidLen()));
		mAllReadTidPassword.setText(mSettingsCollection.getGbAllTidPassword());
		mAllReadEpcLen.setText(String.valueOf(mSettingsCollection.getGbAllEpcLen()));

		ArrayAdapter<IntegerString> allReadUserdataBankAdapter = (ArrayAdapter<IntegerString>) mAllReadUserdataBankNoSpinner.getAdapter();
		for(IntegerString item : mAllReadUserdataBankNoArray){
			if(item.getIndex() == mSettingsCollection.getGbAllUserdataNo()){
				mAllReadUserdataBankNoSpinner.setSelection(allReadUserdataBankAdapter.getPosition(item));
				break;
			}
		}
		mALlReadUserdataAddress.setText(String.valueOf(mSettingsCollection.getGbAllUserdataAddress()));
		mAllReadUserdataLen.setText(String.valueOf(mSettingsCollection.getGbAllUserdataLen()));
		mAllReadUserdataPassword.setText(mSettingsCollection.getGbAllUserdataPassword());

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(getLocalClassName(), "onSaveInstanceState()");
		//		outState.putBoolean("ANTENNA", mAntennaOne.isChecked());//天线
		outState.putBoolean("VOICED", mVoiced.isChecked());//提示音
		outState.putBoolean("RSSI", mRssi.isChecked());
		//		outState.putBoolean("UTC", mUtc.isChecked());
		outState.putInt("QVALUE", Integer.parseInt(mQText.getText().toString().trim()));
		outState.putBoolean("LOOP", mLoopRadioButton.isChecked());//操作类型
		//6C
		outState.putBoolean("TAG6C", mTag6CRadioButton.isChecked());
		outState.putBoolean("READEPC6C", mReadEpc.isChecked());
		outState.putBoolean("READTID6C", mReadTid.isChecked());
		outState.putBoolean("READALL6C", mReadAll.isChecked());
		outState.putInt("TIDLEN6C", Integer.parseInt(mTidLen.getText().toString().trim()));
		outState.putInt("ADDRESS6C", Integer.parseInt(mUserDataAddress.getText().toString().trim()));
		outState.putInt("USERDATALEN6C", Integer.parseInt(mUserDataLen.getText().toString().trim()));
		//6B
		outState.putBoolean("TAG6B", mTag6BRadioButton.isChecked());
		outState.putBoolean("READID6B", mReadId6B.isChecked());
		outState.putBoolean("READALL6B", mReadIdUserData6B.isChecked());
		outState.putInt("IDLEN6B", Integer.parseInt(mTidLen6B.getText().toString().trim()));
		outState.putInt("ADDRESS6B", Integer.parseInt(mUserDataAddress6B.getText().toString().trim()));
		outState.putInt("USERDATALEN6B", Integer.parseInt(mUserDataLen6B.getText().toString().trim()));
		//6C&6B
		outState.putBoolean("TAG6C6B", mTag6C6BRadioButton.isChecked());
		outState.putInt("TIDLEN6C6B", Integer.parseInt(mTidLen6C6B.getText().toString().trim()));
		outState.putInt("ADDRESS6C6B", Integer.parseInt(mUserDataAddress6C6B.getText().toString().trim()));
		outState.putInt("USERDATALEN6C6B", Integer.parseInt(mUserDataLen6C6B.getText().toString().trim()));

		//GB
		outState.putBoolean("TAGGB", mTagGBRadioButton.isChecked());

		//Inventory
		outState.putBoolean("GBINVENTORY", mInventory.isChecked());
		outState.putInt("TARGETINVENTORY", mTargetSpinner.getSelectedItemPosition());
		outState.putInt("SESSIONINVENTORY", mSessionSpinner.getSelectedItemPosition());
		outState.putInt("CONDITIONINVENTORY", mConditionSpinner.getSelectedItemPosition());

		//Access Read
		outState.putBoolean("GBACCESS", mAccessRead.isChecked());
		outState.putString("PASSWORDACCESS", mAccessReadPassword.getText().toString());
		outState.putInt("ADDRESSACCESS", TextUtils.isEmpty(mAccessReadAddress.getText().toString()) ? 0 : Integer.parseInt(mAccessReadAddress.getText().toString()));
		outState.putInt("LENACCESS", TextUtils.isEmpty(mAccessReadLen.getText().toString()) ? 0 : Integer.parseInt(mAccessReadLen.getText().toString()));
		outState.putBoolean("EPCACCESS", mAccessReadGBEpc.isChecked());
		outState.putBoolean("TIDACCESS", mAccessReadGBTid.isChecked());
		outState.putBoolean("USERDATAACCESS", mAccessReadGBUserdata.isChecked());
		outState.putInt("NOACCESS", TextUtils.isEmpty(mAccessReadNo.getText().toString()) ? 0 : Integer.parseInt(mAccessReadNo.getText().toString()));

		//Combination Read
		outState.putBoolean("GBCOMBINATION", mCombinationRead.isChecked());
		outState.putString("TIDPASSWORDCOMBINATION", mCombinationReadTidPassword.getText().toString());
		outState.putInt("TIDLENCOMBINATION", TextUtils.isEmpty(mCombinationReadTidLen.getText().toString()) ? 0 : Integer.parseInt(mCombinationReadTidLen.getText().toString()));

		//All Read
		outState.putBoolean("GBALL", mAllRead.isChecked());
		outState.putInt("TIDLENALL", TextUtils.isEmpty(mAllReadTidLen.getText().toString()) ? 0 : Integer.parseInt(mAllReadTidLen.getText().toString()));
		outState.putString("TIDPASSWORDALL", mAllReadTidPassword.getText().toString());
		outState.putInt("EPCLENALL", TextUtils.isEmpty(mAllReadEpcLen.getText().toString()) ? 0 : Integer.parseInt(mAllReadEpcLen.getText().toString()));
		outState.putInt("USERDATANOALL", mAllReadUserdataBankNoSpinner.getSelectedItemPosition());
		outState.putInt("USERDATAADDRESSALL", TextUtils.isEmpty(mALlReadUserdataAddress.getText().toString()) ? 0 : Integer.parseInt(mALlReadUserdataAddress.getText().toString()));
		outState.putInt("USERDATALENALL", TextUtils.isEmpty(mAllReadUserdataLen.getText().toString()) ? 0 : Integer.parseInt(mAllReadUserdataLen.getText().toString()));
		outState.putString("USERDATAPASSWORDALL", mAllReadUserdataPassword.getText().toString());

		this.mSavedInstanceState = outState;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
