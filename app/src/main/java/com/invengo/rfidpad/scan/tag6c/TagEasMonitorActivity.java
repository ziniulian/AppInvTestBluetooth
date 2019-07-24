package com.invengo.rfidpad.scan.tag6c;

import invengo.javaapi.protocol.IRP1.EasAlarm_6C;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.receivedInfo.EasAlarm6CReceivedInfo;
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
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

public class TagEasMonitorActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TagOperationBroadcastReceiver mReceiver;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private boolean start = false;
	private boolean stop = false;
	private TextView mResultView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_eas_monitor);
		setTitle(R.string.text_tag_operation_eas_monitor);
		
		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();
		
		mResultView = (TextView) findViewById(R.id.text_tag_operation_eas_monitor_result);
		
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_operation_eas_monitor, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
		case R.id.menu_tag_operation_eas_monitor_start_id:
			startMonitor();
			break;
		case R.id.menu_tag_operation_eas_monitor_stop_id:
			stopMonitor();
			break;
		default:
			break;
		}
		return true;
	}

	private void stopMonitor() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(stop || start){
			return;
		}
		stop = true;
		PowerOff_800 msg = new PowerOff_800();
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void startMonitor() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		
		if(stop || start){
			return;
		}
		start = true;
		int antenna = mSettingsCollection.getAntenna();
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_eas_monitor_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		EasAlarm_6C msg = new EasAlarm_6C((byte)antenna, (byte)1);
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
			if(stop){
				stop = false;
				if(resultCode == 0){
					InvengoUtils.showToast(TagEasMonitorActivity.this, R.string.toast_tag_operation_eas_monitor_stop_success);
				}else{
					InvengoUtils.showToast(TagEasMonitorActivity.this, R.string.toast_tag_operation_eas_monitor_stop_failure);
				}
				return;
			}
			if(start){
				start = false;
				if(resultCode == 0){
					InvengoUtils.showToast(TagEasMonitorActivity.this, R.string.toast_tag_operation_eas_monitor_start_success);
					EasAlarm6CReceivedInfo info = (EasAlarm6CReceivedInfo) intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
					Message msg = new Message();
					msg.what = REFRESH;
					msg.obj = info;
					handler.sendMessage(msg);
				}else{
					InvengoUtils.showToast(TagEasMonitorActivity.this, R.string.toast_tag_operation_eas_monitor_start_failure);
				}
			}
		}
	}
	
	private static final int REFRESH = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case REFRESH:
				EasAlarm6CReceivedInfo info = (EasAlarm6CReceivedInfo) msg.obj;
				byte type = info.getAnswerType();
				String result = "";
				if(type == 0x00){
					result = getString(R.string.toast_tag_operation_eas_monitor_start_success);
				}else if(type == 0xA0){
					result = getString(R.string.toast_tag_operation_eas_monitor_found_flag);
				}
				StringBuffer show = new StringBuffer();
				if(TextUtils.isEmpty(mResultView.getText())){
					show.append(result);
				}else{
					show.append(mResultView.getText());
					show.append("\n");
					show.append(result);
				}
				mResultView.setText(show.toString());
				break;
			default:
				break;
			}
		};
	};
	
}
