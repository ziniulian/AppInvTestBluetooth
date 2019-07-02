package com.invengo.rfidpad.debug;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 6B协议基带通信速率调试
 */
public class Reader6BTrafficRateDebugActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderHolder mReaderHolder;
	private TrafficRateDebugBroadcastReceiver mReceiver;

	private Spinner mTrafficRateSpinner;
	private static final int[] INDEX = {0, 1, 2, 3};
	private static final String[] NAME = {"LF 40KHZ", "LF 160KHZ", "远望谷默认模式", "自动(AutoSet)模式"};
	private static final String[] RATE = {"00", "01", "02", "FF"};
	private static final byte PARAMETER = 0x31;
	private static final String LENGTH = "01";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_traffic_rate_6b);
		setTitle(R.string.title_reader_debug_traffic_rate_6B);

		mReaderHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mTrafficRateSpinner = (Spinner) findViewById(R.id.spinner_reader_debug_traffic_rate_6B_id);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_debug_traffic_rate, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_reader_debug_traffic_rate_config_id:
				configTrafficRateDebug();
				break;
			case R.id.menu_reader_debug_traffic_rate_query_id:
				queryTrafficRateDebug();
				break;
			default:
				break;
		}
		return true;
	}

	private void queryTrafficRateDebug() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_traffic_rate_query_6B);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		SysQuery_800 message = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(message);
	}

	private void configTrafficRateDebug() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		RateEntity entity = (RateEntity) mTrafficRateSpinner.getSelectedItem();
		String rateHex = entity.getRateHex();
		String dataHex = LENGTH + rateHex;
		byte[] data = Util.convertHexStringToByteArray(dataHex);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_traffic_rate_config_6B);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);

		SysConfig_800 message = new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(message);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeSpinner();
		initializeRate();
		initializeData();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TrafficRateDebugBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private void initializeRate() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryTrafficRate = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(queryTrafficRate);
					if(success){
						if (null != queryTrafficRate.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_ANTENNA_RATE;
							msg.obj = queryTrafficRate.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		}).start();
	}

	private void initializeSpinner() {
		ArrayAdapter<RateEntity> rateAdapter = new ArrayAdapter<Reader6BTrafficRateDebugActivity.RateEntity>(
				this, R.layout.myspinner);
		for(int i = 0; i < NAME.length; i++){
			RateEntity entity = new RateEntity();
			entity.setIndex(INDEX[i]);
			entity.setName(NAME[i]);
			entity.setRateHex(RATE[i]);
			rateAdapter.add(entity);
		}

		mTrafficRateSpinner.setAdapter(rateAdapter);
		mTrafficRateSpinner.setSelection(INDEX[0]);
	}

	private class RateEntity{
		private int index;
		private String name;
		private String rateHex;

		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getRateHex() {
			return rateHex;
		}
		public void setRateHex(String rateHex) {
			this.rateHex = rateHex;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	private static final int QUERY_ANTENNA_RATE = 0;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_ANTENNA_RATE:
					SysQuery800ReceivedInfo queryRateMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] rateByte = queryRateMsg.getQueryData();
					String hexString = Util.convertByteArrayToHexString(rateByte);
					Log.i(getLocalClassName(), "BinaryString:" + hexString);
					for(int i = 0; i < RATE.length; i++){
						if(hexString.equals(RATE[i])){
							mTrafficRateSpinner.setSelection(INDEX[i]);
							break;
						}
					}
					break;
				default:
					break;
			}
		};
	};

	private class TrafficRateDebugBroadcastReceiver extends BroadcastReceiver{

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
					msg.what = QUERY_ANTENNA_RATE;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(Reader6BTrafficRateDebugActivity.this, R.string.toast_reader_debug_traffic_rate_success_6B);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(Reader6BTrafficRateDebugActivity.this, R.string.toast_reader_debug_traffic_rate_failure_6B);
			}
		}
	}


}
