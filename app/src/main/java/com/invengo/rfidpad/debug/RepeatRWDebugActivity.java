package com.invengo.rfidpad.debug;

import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.BaseMessage;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.IRP1.ReadUserData_6C;
import invengo.javaapi.protocol.IRP1.Reader;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;
import invengo.javaapi.protocol.receivedInfo.ReadUserData6CReceivedInfo;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.base.VoiceManager;
import com.invengo.rfidpad.scan.Tag6COperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 重复读写调试-针对用户数据区
 */
public class RepeatRWDebugActivity extends PowerManagerActivity {

	private VoiceManager mVoiceManager;
	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TextView mMatchDataTextView;
	private TextView mMatchAreaTextView;
	//	private RepeatRWDebugBroadcastReceiver mReceiver;
	private View mContentView;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private RadioButton mReadRadioButton;
	private RadioButton mWriteRadioButton;
	//	private RadioButton mEpcRadioButton;
	private RadioButton mUserdataButton;
	private EditText mPasswordEditText;
	private EditText mAddressEditText;
	private EditText mLengthEditText;
	private EditText mTimesEditText;
	private EditText mDataEditText;
	private ReaderRWRepeatTask mTask = null;

	private static final int READ_TYPE = 0;
	private static final int WRITE_TYPE = 1;

	private static final int TIMES_RW_KEY = 1;
	private static final int MESSAGE_RW_KEY = 2;
	private static final int TYPE_RW_KEY = 3;
	private static final int DATA_RW_KEY = 4;

	private static final int TIME_TASK_KEY = 1;
	private static final int MESSAGE_SHOW_TASK_KEY = 2;
	private static final int DATA_TASK_KEY = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_repeat_rw);
		setTitle(R.string.title_reader_debug_repeat_rw);

		mVoiceManager = VoiceManager.getInstance(this);
		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mMatchDataTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_data);
		mMatchAreaTextView = (TextView) findViewById(R.id.text_tag_operation_write_epc_match_area);

		mReadRadioButton = (RadioButton) findViewById(R.id.radio_reader_debug_repeat_rw_read);
		mWriteRadioButton = (RadioButton) findViewById(R.id.radio_reader_debug_repeat_rw_write);
//		mEpcRadioButton = (RadioButton) findViewById(R.id.radio_reader_debug_repeat_rw_epc);
		mUserdataButton = (RadioButton) findViewById(R.id.radio_reader_debug_repeat_rw_userdata);
		mPasswordEditText = (EditText) findViewById(R.id.edit_reader_debug_repeat_rw_pwd);
		mAddressEditText = (EditText) findViewById(R.id.edit_reader_debug_repeat_rw_address);
		mLengthEditText = (EditText) findViewById(R.id.edit_reader_debug_repeat_rw_len);
		mTimesEditText = (EditText) findViewById(R.id.edit_reader_debug_repeat_rw_times);
		mDataEditText = (EditText) findViewById(R.id.edit_reader_debug_repeat_rw_data);

		mContentView = findViewById(R.id.content_reader_debug_repeat_rw_id);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_debug_repeat_rw, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_reader_debug_repeat_start_id:
				if(mReadRadioButton.isChecked()){//重复读
					startRRepeatDebug();
				}else if(mWriteRadioButton.isChecked()){//重复写
					startWRepeatDebug();
				}
				break;
			default:
				break;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void startWRepeatDebug() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		String password = mPasswordEditText.getText().toString();
		if(TextUtils.isEmpty(password)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_password);
			return;
		}
		String address = mAddressEditText.getText().toString();
		if(TextUtils.isEmpty(address)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_address);
			return;
		}
		String length = mLengthEditText.getText().toString();
		if(TextUtils.isEmpty(length)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_len);
			return;
		}
		String times = mTimesEditText.getText().toString();
		if(TextUtils.isEmpty(times)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_times);
			return;
		}

		mDataEditText.setText("");

		int antenna = mSettingsCollection.getAntenna();
		byte[] passwordByte = Util.convertHexStringToByteArray(password);
		int addressInt = Integer.parseInt(address);

		String hexString = InvengoUtils.randomHexString(Integer.parseInt(length) * 2);//双字节
		Log.i(getLocalClassName(), hexString);
		byte[] userData = Util.convertHexStringToByteArray(hexString);

		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_write_userdata_message);
		mProgressBarManager.showProgressBar(true, mContentView, mProgressBarStatusView, shortAnimTime);
		setWidgetEnabled(false);

		BaseMessage msg = null;
		if(mWriteRadioButton.isChecked()){//write
			msg = new WriteUserData_6C((byte)antenna, passwordByte, (byte) addressInt, userData, tagID, tagIDType);
		}

		Map<Integer, Object> map = new HashMap<Integer, Object>();
		map.put(TIMES_RW_KEY, times);
		map.put(MESSAGE_RW_KEY, msg);
		map.put(TYPE_RW_KEY, WRITE_TYPE);
		map.put(DATA_RW_KEY, hexString);
		ReaderRWRepeatTask task = new ReaderRWRepeatTask();
		this.mTask = task;
		task.execute(map);
	}

	@SuppressLint("UseSparseArrays")
	@SuppressWarnings("unchecked")
	private void startRRepeatDebug() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		String address = mAddressEditText.getText().toString();
		if(TextUtils.isEmpty(address)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_address);
			return;
		}
		String length = mLengthEditText.getText().toString();
		if(TextUtils.isEmpty(length)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_len);
			return;
		}
		String times = mTimesEditText.getText().toString();
		if(TextUtils.isEmpty(times)){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_repeat_rw_times);
			return;
		}

		mDataEditText.setText("");

		int antenna = mSettingsCollection.getAntenna();
		int addressInt = Integer.parseInt(address);

		byte[] tagID = Util.convertHexStringToByteArray(mMatchDataTextView.getText().toString());
		MemoryBank tagIDType = null;
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
			tagIDType = MemoryBank.EPCMemory;
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
			tagIDType = MemoryBank.TIDMemory;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_read_userdata_message);
		mProgressBarManager.showProgressBar(true, mContentView, mProgressBarStatusView, shortAnimTime);
		setWidgetEnabled(false);

		BaseMessage msg = null;
		if(mReadRadioButton.isChecked()){//read
			msg = new ReadUserData_6C((byte)antenna, addressInt, (byte)(Integer.parseInt(length)), tagID, tagIDType);
		}

		Map<Integer, Object> map = new HashMap<Integer, Object>();
		map.put(TIMES_RW_KEY, times);
		map.put(MESSAGE_RW_KEY, msg);
		map.put(TYPE_RW_KEY, READ_TYPE);
		ReaderRWRepeatTask task = new ReaderRWRepeatTask();
		this.mTask = task;
		task.execute(map);
	}

	@SuppressLint("UseSparseArrays")
	private class ReaderRWRepeatTask extends AsyncTask<Map<Integer, Object>, Map<Integer, Object>, Boolean>{

		private int loop = 0;

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(Map<Integer, Object>... params) {
			Map<Integer, Object> map = params[0];
			String times = (String) map.get(TIMES_RW_KEY);
			BaseMessage msg = (BaseMessage) map.get(MESSAGE_RW_KEY);
			int type = (Integer) map.get(TYPE_RW_KEY);
			this.loop = Integer.parseInt(times);
			for(int i = 0; i < loop; i++){
				if(isCancelled()){
					return false;
				}
				String data = "";
				String format = "%s-%s";
				String messageShow = "";
				if(mHolder.isConnected()){
					Reader reader = mHolder.getCurrentReader();
					Map<Integer, Object> single = new HashMap<Integer, Object>();
					if(reader.send(msg)){
						if(type == READ_TYPE){//Read_repeat
							if(null != msg.getReceivedMessage()){
								ReadUserData6CReceivedInfo info = (ReadUserData6CReceivedInfo) msg.getReceivedMessage();
								data = Util.convertByteArrayToHexString(info.getUserData());
							}
							messageShow = String.format(format, (i + 1), getString(R.string.toast_reader_debug_repeat_rw_success_read));
						}else if(type == WRITE_TYPE){//Write_repeat
							data = (String) map.get(DATA_RW_KEY);
							messageShow = String.format(format, (i + 1), getString(R.string.toast_reader_debug_repeat_rw_success_write));
						}
					}else{
						if(type == READ_TYPE){
							messageShow = String.format(format, (i + 1), getString(R.string.toast_reader_debug_repeat_rw_failure_read));
						}else if(type == WRITE_TYPE){
							messageShow = String.format(format, (i + 1), getString(R.string.toast_reader_debug_repeat_rw_failure_write));
						}
					}
					single.put(TIME_TASK_KEY, Integer.valueOf((i + 1)));
					single.put(MESSAGE_SHOW_TASK_KEY, messageShow);
					single.put(DATA_TASK_KEY, data);
					publishProgress(single);
				}
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Map<Integer, Object>... values) {
			Map<Integer, Object> single = values[0];
			int i = (Integer) single.get(TIME_TASK_KEY);
			int percent = ((i / this.loop) * 100);
			setProgress(percent);
			String messageShow = (String) single.get(MESSAGE_SHOW_TASK_KEY);
			String data = (String) single.get(DATA_TASK_KEY);
			mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, messageShow);
			Message msg = new Message();
			msg.what = SINGLE;
			msg.obj = messageShow + " " + data;
			processHandler.sendMessage(msg);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				mVoiceManager.playSound(Contants.SUCCESS_SOUND, 0);
			}else{
				mVoiceManager.playSound(Contants.ERROR_SOUND, 0);
			}
			Message msg = new Message();
			msg.what = OVER;
			msg.obj = getString(R.string.toast_reader_debug_repeat_rw_read_over);
			processHandler.sendMessage(msg);
		}

		@Override
		protected void onCancelled(Boolean result) {
			super.onCancelled(result);
		}
	}

	private static final int SINGLE = 0;
	private static final int OVER = 1;
	private Handler processHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			String messageShow = (String) msg.obj;
			switch (what) {
				case SINGLE:
					String oldContent = mDataEditText.getText().toString();
					StringBuffer newContent = new StringBuffer();
					if(TextUtils.isEmpty(oldContent)){
						newContent.append(messageShow);
					}else{
						newContent.append(oldContent);
						newContent.append("\n");
						newContent.append(messageShow);
					}
					mDataEditText.setText(newContent.toString());
					break;
				case OVER:
					mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, messageShow);
					int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
					mProgressBarManager.showProgressBar(false, mContentView, mProgressBarStatusView, shortAnimTime);
					setWidgetEnabled(true);
					break;
				default:
					break;
			}
		};
	};

	private void setWidgetEnabled(boolean enabled){
		mReadRadioButton.setEnabled(enabled);;
		mWriteRadioButton.setEnabled(enabled);;
//		mEpcRadioButton.setEnabled(enabled);;
		mUserdataButton.setEnabled(enabled);;
		mPasswordEditText.setEnabled(enabled);;
		mAddressEditText.setEnabled(enabled);;
		mLengthEditText.setEnabled(enabled);;
		mTimesEditText.setEnabled(enabled);;
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.i(getLocalClassName(), "onBackPressed()");
		if(null != this.mTask && this.mTask.getStatus() != AsyncTask.Status.FINISHED){
			this.mTask.cancel(true);
			this.mTask = null;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(mHolder.isConnected()){
					mHolder.sendMessage(new PowerOff_800());
				}
			}
		}).start();
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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
