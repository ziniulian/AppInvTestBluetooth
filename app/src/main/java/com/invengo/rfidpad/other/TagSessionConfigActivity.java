package com.invengo.rfidpad.other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

import invengo.javaapi.protocol.IRP1.TagOperationConfig_6C;
import invengo.javaapi.protocol.IRP1.TagOperationQuery_6C;
import invengo.javaapi.protocol.receivedInfo.TagOperationQuery6CReceivedInfo;

/**
 * 标签session配置界面
 */
public class TagSessionConfigActivity extends PowerManagerActivity {

	private static final String TAG = TagSessionConfigActivity.class.getSimpleName();
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private Spinner mSessionSpinner;
	private Spinner mFlagSpinner;

	private static final String[] SESSION = { "S0", "S1", "S2", "S3"};
	private static final String[] FLAG = { "A", "B", "A&B"};
	//	private static final String[] FLAG = { "A标志", "B标志"};
	private static final byte PARAMETER = 0x12;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_session_config);
		setTitle(R.string.title_tag_session_config);

		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mSessionSpinner = (Spinner) findViewById(R.id.spinner_tag_session_config_session_id);
		mFlagSpinner = (Spinner) findViewById(R.id.spinner_tag_session_config_flag_id);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_session, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_tag_session_config_id:
				int session = mSessionSpinner.getSelectedItemPosition();
				int flag = mFlagSpinner.getSelectedItemPosition();
				attemptConfigSession(session, flag);
				break;
			case R.id.menu_tag_session_query_id:
				attemptQuerySession();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQuerySession() {
		InvengoLog.i(TAG, "INFO.Query session");

		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_tag_session_flag_query_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		TagOperationQuery_6C msg = new TagOperationQuery_6C(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void attemptConfigSession(int session, int flag) {
		InvengoLog.i(TAG, "INFO.Configurate session");

		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		final byte[] data = {(byte) session, (byte) flag};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_tag_session_flag_config_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		TagOperationConfig_6C msg = new TagOperationConfig_6C(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	@Override
	protected void onResume() {
		InvengoLog.i(TAG, "INFO.onResume()");

		initializeSpinner();
		initializeSession();
		initializeData();

		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeSession() {
		Thread querySessionThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mHolder.isConnected()) {
					TagOperationQuery_6C querySessionMsg = new TagOperationQuery_6C(PARAMETER);
					boolean success = mHolder.getCurrentReader().send(querySessionMsg);
					if (success) {
						if (null != querySessionMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = SESSION_REFRESH;
							msg.obj = querySessionMsg.getReceivedMessage();
							mSessionHandler.sendMessage(msg);
						}
					}
				}
			}
		});
		querySessionThread.start();
	}

	private void initializeSpinner() {
		ArrayAdapter<String> sessionAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		for(String session : SESSION){
			sessionAdapter.add(session);
		}
		mSessionSpinner.setAdapter(sessionAdapter);
		mSessionSpinner.setSelection(0);

		ArrayAdapter<String> flagAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		for(String flag : FLAG){
			flagAdapter.add(flag);
		}
		mFlagSpinner.setAdapter(flagAdapter);
		mFlagSpinner.setSelection(0);
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//配置/查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof TagOperationQuery6CReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = SESSION_REFRESH;
					msg.obj = obj;
					mSessionHandler.sendMessage(msg);
				}
				InvengoUtils.showToast(TagSessionConfigActivity.this, R.string.toast_tag_session_flag_config_query_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(TagSessionConfigActivity.this, R.string.toast_tag_session_flag_config_query_failure);
			}
		}
	}

	private static final int SESSION_REFRESH = 0;
	private Handler mSessionHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case SESSION_REFRESH:
					TagOperationQuery6CReceivedInfo responseInfo = (TagOperationQuery6CReceivedInfo) msg.obj;
					byte[] responseData = responseInfo.getQueryData();
					int session = responseData[0] & 0xFF;
					int flag = responseData[1] & 0xFF;
					mSessionSpinner.setSelection(session);
					mFlagSpinner.setSelection(flag);
					break;
				default:
					break;
			}
		};
	};

}
