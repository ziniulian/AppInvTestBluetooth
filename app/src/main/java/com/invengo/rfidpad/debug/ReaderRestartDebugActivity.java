package com.invengo.rfidpad.debug;

import invengo.javaapi.protocol.IRP1.ResetReader_800;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * XC2600-重启、恢复出厂设置
 */
public class ReaderRestartDebugActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private ReaderHolder mReaderHolder;
	private static final byte PARAMETER_RESET = (byte) 0x00;
	private static final String TAG = ReaderRestartDebugActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_restart_factory_reset);
		setTitle(R.string.title_reader_configuration_restart_default);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_restart_factory_reset, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_restart_id:
				attemptRestart();
				break;
			case R.id.menu_reader_configuration_factory_reset_id:
				attemptReset();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptRestart(){
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.attemptRestart().");

		new Thread(new Runnable() {

			@Override
			public void run() {
				ResetReader_800 msg= new ResetReader_800();
				mReaderHolder.getCurrentReader().send(msg);
			}
		}).start();
		InvengoUtils.showToast(this, R.string.progress_bar_configuration_restart_message);

//		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
//		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_restart_message);
//		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
//		ResetReader_800 msg= new ResetReader_800();
//		OperationTask task = new OperationTask(this);
//		task.execute(msg);
	}

	private void attemptReset(){
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.attemptReset().");

		byte[] data = new byte[0];

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_default_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 msg= new SysConfig_800(PARAMETER_RESET, data);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(ReaderRestartDebugActivity.this, R.string.toast_reader_configuration_restart_default_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderRestartDebugActivity.this, R.string.toast_reader_configuration_restart_default_failure);
			}
		}
	}

}
