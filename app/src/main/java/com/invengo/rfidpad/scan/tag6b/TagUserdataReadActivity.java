package com.invengo.rfidpad.scan.tag6b;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.scan.Tag6BOperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

import invengo.javaapi.core.ReceivedInfo;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.ReadUserData2_6B;
import invengo.javaapi.protocol.IRP1.ReadUserData_6B;
import invengo.javaapi.protocol.receivedInfo.ReadUserData6BReceivedInfo;
import invengo.javaapi.protocol.receivedInfo.ReadUserDataNonFixed6BReceivedInfo;

/**
 * 读6B标签数据
 */
public class TagUserdataReadActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TextView mMatchDataTextView;
	private TextView mMatchAreaTextView;
	private View mProgressBarStatusView;
	private TagOperationBroadcastReceiver mReceiver;
	private TextView mProgressBarMessageView;
	private EditText mAddressEditText;
	private EditText mLenEditText;
	private EditText mDataEditText;
	//	private CheckBox mTypeCheckBox;
	private RadioButton mFixedRadioButton;
	private RadioButton mNonFixedRadioButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6b_operation_userdata_read);
		setTitle(R.string.text_tag_6b_operation_read_userdata);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mAddressEditText = (EditText) findViewById(R.id.edit_tag_6b_operation_read_userdata_address);
		mLenEditText = (EditText) findViewById(R.id.edit_tag_6b_operation_read_userdata_len);
		mDataEditText = (EditText) findViewById(R.id.edit_tag_6b_operation_read_userdata_data);
		//		mTypeCheckBox = (CheckBox) findViewById(R.id.checkbox_tag_6b_operation_read_userdata);
		mFixedRadioButton = (RadioButton) findViewById(R.id.radio_tag_6b_operation_read_userdata_fixed);
		mNonFixedRadioButton = (RadioButton) findViewById(R.id.radio_radio_tag_6b_operation_read_userdata_non_fixed);


		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
		//		addListener();
	}

	//	private void addListener() {
	//		mTypeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	//
	//			@Override
	//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	//				if(isChecked){
	//					mTypeCheckBox.setText(R.string.label_tag_6b_operation_read_fixed_userdata_checked);
	//				}else{
	//					mTypeCheckBox.setText(R.string.label_tag_6b_operation_read_userdata_checked);
	//				}
	//			}
	//		});
	//	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_operation_userdata_read, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_operation_userdata_read_id:
				readUserdata();
				break;
			case R.id.menu_tag_operation_userdata_clear_id:
				clearUserdata();
				break;

			default:
				break;
		}
		return true;
	}

	private void clearUserdata() {
		Message msg = new Message();
		msg.what = CLEAR_USERDATA;
		handler.sendMessage(msg);
	}

	private void readUserdata() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_read_userdata_address);
			return;
		}
		if(TextUtils.isEmpty(mLenEditText.getText().toString())){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_read_userdata_len);
			return;
		}

		//		int antenna = mSettingsCollection.getAntenna();
		byte antenna = 0x01;
		int address = Integer.parseInt(mAddressEditText.getText().toString());
		int length = Integer.parseInt(mLenEditText.getText().toString());
		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_read_userdata_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		if(mFixedRadioButton.isChecked()){
			ReadUserData_6B msg = new ReadUserData_6B((byte)antenna, tagID, (byte)address, (byte)length);
			OperationTask task = new OperationTask(this);
			task.execute(msg);
		}else if(mNonFixedRadioButton.isChecked()){
			ReadUserData2_6B msg = new ReadUserData2_6B((byte)antenna, tagID, (byte)address, (byte)length);
			OperationTask task = new OperationTask(this);
			task.execute(msg);
		}
	}

	private void initializeData() {
		Intent intent = getIntent();
		String matchData = intent.getStringExtra(Tag6BOperationActivity.TAG_6B_OPERATION_DATA);
		mMatchDataTextView.setText(matchData);
		mMatchAreaTextView.setText(R.string.text_tag_6b_operation_match_area_tid);

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
			ReceivedInfo obj = (ReceivedInfo) intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
			if(resultCode == 0){
				//读取成功
				InvengoUtils.showToast(TagUserdataReadActivity.this, R.string.toast_tag_operation_read_userdata_success);
				Message msg = new Message();
				msg.what = UPDATE_USERDATA;
				msg.obj = obj;
				handler.sendMessage(msg);
			}else{
				//读取失败
				InvengoUtils.showToast(TagUserdataReadActivity.this, R.string.toast_tag_operation_read_userdata_failure);
			}
		}
	}
	private static final int UPDATE_USERDATA = 0;
	private static final int CLEAR_USERDATA = 1;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case UPDATE_USERDATA:
					Object obj = msg.obj;
					String newUserData = "";
					String oldUserData = "";
					if(obj instanceof ReadUserData6BReceivedInfo){
						ReadUserData6BReceivedInfo fixedObj = (ReadUserData6BReceivedInfo) obj;
						newUserData = getString(R.string.label_tag_operation_read_userdata_data_show) + Util.convertByteArrayToHexString(fixedObj.getUserData());
						oldUserData = mDataEditText.getText().toString();
					}else {
						ReadUserDataNonFixed6BReceivedInfo nonFixedObj = (ReadUserDataNonFixed6BReceivedInfo) obj;
						newUserData = getString(R.string.label_tag_operation_read_userdata_data_show) + Util.convertByteArrayToHexString(nonFixedObj.getUserData());
						oldUserData = mDataEditText.getText().toString();
					}
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
				default:
					break;
			}
		};
	};
}
