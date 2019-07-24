package com.invengo.rfidpad.other;

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
 * 静态Q值配置界面
 */
public class StaticQConfigActivity extends PowerManagerActivity {

	private static final String TAG = StaticQConfigActivity.class.getSimpleName();
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mQEditText;
	private static final byte PARAMETER = 0x10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_static_q_config);
		setTitle(R.string.title_static_q_config);

		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mQEditText = (EditText) findViewById(R.id.edit_text_q);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_static_q, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_static_q_config_id:
				if(TextUtils.isEmpty(mQEditText.getText().toString().trim())){
					InvengoUtils.showToast(StaticQConfigActivity.this, R.string.toast_static_q_not_null);
					break;
				}
				int q = Integer.parseInt(mQEditText.getText().toString().trim());
				attemptConfigQValue(q);
				break;
			case R.id.menu_static_q_query_id:
				attemptQueryQValue();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQueryQValue() {
		InvengoLog.i(TAG, "INFO.Query Static-Q value");

		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_static_q_query_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		TagOperationQuery_6C msg = new TagOperationQuery_6C(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void attemptConfigQValue(int q) {
		InvengoLog.i(TAG, "INFO.Configurate Static-Q value");

		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		final byte[] data = {(byte) q};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_static_q_config_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		TagOperationConfig_6C msg = new TagOperationConfig_6C(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	@Override
	protected void onResume() {
		InvengoLog.i(TAG, "INFO.onResume()");

		initializeQValue();
		initializeData();

		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeQValue() {
		Thread querySessionThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mHolder.isConnected()) {
					TagOperationQuery_6C queryQMsg = new TagOperationQuery_6C(PARAMETER);
					boolean success = mHolder.getCurrentReader().send(queryQMsg);
					if (success) {
						if (null != queryQMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = Q_VALUE_REFRESH;
							msg.obj = queryQMsg.getReceivedMessage();
							mSessionHandler.sendMessage(msg);
						}
					}
				}
			}
		});
		querySessionThread.start();
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
					msg.what = Q_VALUE_REFRESH;
					msg.obj = obj;
					mSessionHandler.sendMessage(msg);
				}
				InvengoUtils.showToast(StaticQConfigActivity.this, R.string.toast_tag_session_flag_config_query_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(StaticQConfigActivity.this, R.string.toast_tag_session_flag_config_query_failure);
			}
		}
	}

	private static final int Q_VALUE_REFRESH = 0;
	private Handler mSessionHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case Q_VALUE_REFRESH:
					TagOperationQuery6CReceivedInfo responseInfo = (TagOperationQuery6CReceivedInfo) msg.obj;
					byte[] responseData = responseInfo.getQueryData();
					int q = responseData[0] & 0xFF;
					mQEditText.setText(String.valueOf(q));
					break;
				default:
					break;
			}
		};
	};

}
