package com.invengo.rfidpad.config;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
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
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;
import com.invengo.rfidpad.view.CustomTimePickerDialog;
import com.invengo.rfidpad.view.CustomTimePickerDialog.OnTimeSetListener;
import com.invengo.rfidpad.view.TimePicker;

/**
 * XC2600-时间同步
 */
public class ReaderTimeSynchronizationActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private TextView mDateView;
	private TextView mTimeView;

	private Calendar mCalendar;
	private ReaderHolder mReaderHolder;
	private static final byte PARAMETER = (byte) 0x10;
	private static final String TAG = ReaderTimeSynchronizationActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_time_synchronization);
		setTitle(R.string.title_reader_configuration_time_synchronization);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();
		mCalendar = Calendar.getInstance();

		mDateView = (TextView) findViewById(R.id.text_reader_configuration_time_synchronization_date);
		mTimeView = (TextView) findViewById(R.id.text_reader_configuration_time_synchronization_time);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_time_synchronization, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_time_synchronization_config_id:
				if(TextUtils.isEmpty(mDateView.getText().toString())){
					InvengoUtils.showToast(ReaderTimeSynchronizationActivity.this, R.string.toast_reader_configuration_time_synchronization_date_not_null);
					break;
				}
				if(TextUtils.isEmpty(mTimeView.getText().toString())){
					InvengoUtils.showToast(ReaderTimeSynchronizationActivity.this, R.string.toast_reader_configuration_time_synchronization_time_not_null);
					break;
				}

				String[] date = mDateView.getText().toString().split("-");
				String[] time = mTimeView.getText().toString().split(":");
				mCalendar.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1,
						Integer.parseInt(date[2]), Integer.parseInt(time[0]),
						Integer.parseInt(time[1]), Integer.parseInt(time[2]));
				long millisecond = mCalendar.getTimeInMillis();
				attemptConfigReaderTime(millisecond);
				break;
			case R.id.menu_reader_configuration_time_synchronization_query_id:
				attemptQueryTime();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQueryTime() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_time_synchronization_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 message = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(message);
	}

	private void attemptConfigReaderTime(long millisecond) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		long second = millisecond / 1000;
		long mircosecond = (millisecond % 1000) * 1000;
		byte[] data = new byte[9];
		data[0] = 8;

		data[1] = (byte) (second >> 24);
		data[2] = (byte) (second >> 16);
		data[3] = (byte) (second >> 8);
		data[4] = (byte) (second & 0xFF);

		data[5] = (byte) (mircosecond >> 24);
		data[6] = (byte) (mircosecond >> 16);
		data[7] = (byte) (mircosecond >> 8);
		data[8] = (byte) (mircosecond & 0xFF);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_time_synchronization_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 message = new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(message);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		initializeReaderTime();
		initializeData();
		initializeListener();

		InvengoLog.i(TAG, "INFO.onResume()");
	}

	private void initializeListener() {
		mDateView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mCalendar.setTimeInMillis(System.currentTimeMillis());
				int year = mCalendar.get(Calendar.YEAR);
				int month = mCalendar.get(Calendar.MONTH);
				int day = mCalendar.get(Calendar.DAY_OF_MONTH);
				new DatePickerDialog(ReaderTimeSynchronizationActivity.this, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
										  int dayOfMonth) {
						mDateView.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
					}
				}, year, month, day).show();
			}
		});

		mTimeView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCalendar.setTimeInMillis(System.currentTimeMillis());
				int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
				int minute = mCalendar.get(Calendar.MINUTE);
				int second = mCalendar.get(Calendar.SECOND);

				new CustomTimePickerDialog(ReaderTimeSynchronizationActivity.this, new OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute,
										  int seconds) {
						mTimeView.setText(hourOfDay + ":" + minute + ":" + seconds);
					}
				}, hour, minute, second, true).show();
			}
		});
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private void initializeReaderTime() {
		InvengoLog.i(TAG, "INFO.initializeReaderTime()");
		Thread queryReaderTimeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryReaderTimeMsg = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(queryReaderTimeMsg);
					if (success) {
						if (null != queryReaderTimeMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_READER_TIME;
							msg.obj = queryReaderTimeMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryReaderTimeThread.start();
	}

	private static final int QUERY_READER_TIME = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_READER_TIME:
					InvengoLog.i(TAG, "INFO.update Reader Time.");
					SysQuery800ReceivedInfo response = (SysQuery800ReceivedInfo) msg.obj;
					byte[] timeByte = response.getQueryData();
					String[] dateTime = Util.getUtc(timeByte).split(" ");
					mDateView.setText(dateTime[0]);
					mTimeView.setText(dateTime[1]);
					break;
				default:
					break;
			}
		};
	};

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
				//配置/查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof SysQuery800ReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY_READER_TIME;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderTimeSynchronizationActivity.this, R.string.toast_reader_configuration_time_synchronization_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderTimeSynchronizationActivity.this, R.string.toast_reader_configuration_time_synchronization_failure);
			}
		}
	}

}
