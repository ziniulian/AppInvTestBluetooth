package com.invengo.rfidpad.config;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.util.ArrayList;
import java.util.List;

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
import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.IntegerString;
import com.invengo.rfidpad.utils.InvengoUtils;

public class ReaderPowerConfigurationActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private ReaderHolder mReaderHolder;
	private Spinner mAntennaOneSpinner;
	private static final String[] RATE_SCOPE_DEFAULT = { "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
			"24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36" };

	private List<IntegerString> mPowerGainList;//确定功率等级与spinner中的位置索引的对应关系
	private static final String TAG = ReaderPowerConfigurationActivity.class.getSimpleName();
	private static final byte PARAMETER_XC2910 = 0x0C;//XC2910功率校准查询
	private static final byte PARAMETER_XC2600_XC2903 = 0x68;//XC2600功率校准查询
	private static final byte PARAMETER = 0x65;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_configuration_power);
		setTitle(R.string.title_reader_configuration_power);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();
		mPowerGainList = new ArrayList<IntegerString>();

		mAntennaOneSpinner = (Spinner) findViewById(R.id.spinner_reader_configuration_antenna_one);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_power, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_power_config_id:
				IntegerString selectItem = (IntegerString) mAntennaOneSpinner.getSelectedItem();
				int rate = Integer.parseInt(selectItem.getName());
				attemptConfigRate(0, rate);
				break;
			case R.id.menu_reader_configuration_power_query_id:
				queryAntennaRate();
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(mReaderHolder.getDeviceType() == DeviceType.XC2910){
			initializeRateScope(PARAMETER_XC2910);
		}else{
			initializeRateScope(PARAMETER_XC2600_XC2903);
		}
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
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	protected void attemptConfigRate(int antenna, int rate) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
//		final byte parameter = 0x65;
		byte len = 0x02;
		final byte[] data = {len, (byte) antenna, (byte) rate};

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_configuration_power_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysConfig_800 configRateMsg = new SysConfig_800(PARAMETER, data);
		OperationTask task = new OperationTask(this);
		task.execute(configRateMsg);
	}

	private void queryAntennaRate() {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

//		byte parameter = 0x65;
		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_query_power_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryRateMsg = new SysQuery_800(PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(queryRateMsg);
	}

	private void initializeRate(){
		Thread queryAntennaRateThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
//					byte parameter = 0x65;
					SysQuery_800 queryRateMsg = new SysQuery_800(PARAMETER);
					boolean success = mReaderHolder.getCurrentReader().send(
							queryRateMsg);
					if (success) {
						if (null != queryRateMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_ANTENNA_RATE;
							msg.obj = queryRateMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryAntennaRateThread.start();
	}

	private void initializeRateScope(final byte parameter) {
		InvengoLog.i(TAG, "INFO.initializeRateScope()");
		Thread queryRateScopeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					SysQuery_800 queryRateScopeMsg = new SysQuery_800(parameter);
					boolean success = mReaderHolder.getCurrentReader().send(queryRateScopeMsg);
					Message msg = new Message();
					if (success) {
						if (null != queryRateScopeMsg.getReceivedMessage()) {
							msg.what = INIT_RATE_SCOPE_SUCCESS;
							msg.obj = queryRateScopeMsg.getReceivedMessage();
						}
					}else{
						msg.what = INIT_RATE_SCOPE_FAILURE;
					}
					handler.sendMessage(msg);
				}else{
					Message msg = new Message();
					msg.what = INIT_RATE_SCOPE_FAILURE;
					handler.sendMessage(msg);
				}
			}
		});
		queryRateScopeThread.start();
	}

	private static final int QUERY_ANTENNA_RATE = 0;
	private static final int INIT_RATE_SCOPE_SUCCESS = 1;
	private static final int INIT_RATE_SCOPE_FAILURE = 2;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_ANTENNA_RATE:
					SysQuery800ReceivedInfo queryRateMsg = (SysQuery800ReceivedInfo) msg.obj;
//				Log.i(getLocalClassName(), "BinaryString:" + message + "-----HexString" + message);
//				BinaryString:181E1E1E-----HexString181E1E1E
					byte[] rateByte = queryRateMsg.getQueryData();
					int[] rate = new int[4];
					for(int i = 0; i < rate.length; i++){
						rate[i] = Integer.parseInt(Util.convertByteToHexWordString(rateByte[i]), 16);
					}
					int currentPower = rate[0];
					int selection = 0;
					for(IntegerString history : mPowerGainList){
						if(currentPower == Integer.parseInt(history.getName())){
							selection = history.getIndex();
							break;
						}
					}
					mAntennaOneSpinner.setSelection(selection);
					break;
				case INIT_RATE_SCOPE_SUCCESS:
					SysQuery800ReceivedInfo queryRateScopeMsg = (SysQuery800ReceivedInfo) msg.obj;
//				InvengoLog.i(TAG, "INFO.RateScope HexString:{%s}", Util.convertByteArrayToHexString(queryRateScopeMsg.getQueryData()));
					byte[] powerGain = queryRateScopeMsg.getQueryData();//功率校准后的增益值数组,非零表示经过功率校准的,需要将其对应功率等级显示
					int spinnerIndex = 0;//在mPowerGainList中的索引值,即下面放入Spinner时的索引值(位置)
					for(int i = 0; i < RATE_SCOPE_DEFAULT.length; i++){
						int power = powerGain[i] & 0xFF;
						if(power > 0){
							IntegerString temp = new IntegerString(spinnerIndex++, RATE_SCOPE_DEFAULT[i]);
							mPowerGainList.add(temp);
						}
					}
					ArrayAdapter<IntegerString> antennaAdapter = new ArrayAdapter<IntegerString>(ReaderPowerConfigurationActivity.this, R.layout.myspinner);
					for(IntegerString temp : mPowerGainList){
						antennaAdapter.add(temp);
					}
					mAntennaOneSpinner.setAdapter(antennaAdapter);
					mAntennaOneSpinner.setSelection(0);
					break;
				case INIT_RATE_SCOPE_FAILURE:
					ArrayAdapter<IntegerString> errorAdapter = new ArrayAdapter<IntegerString>(ReaderPowerConfigurationActivity.this, R.layout.myspinner);
					for(int i = 0; i < RATE_SCOPE_DEFAULT.length; i++){
						IntegerString temp = new IntegerString(i, RATE_SCOPE_DEFAULT[i]);
						errorAdapter.add(temp);
					}

					mAntennaOneSpinner.setAdapter(errorAdapter);
					mAntennaOneSpinner.setSelection(0);
					break;
				default:
					break;
			}
		};
	};

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
					msg.what = QUERY_ANTENNA_RATE;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderPowerConfigurationActivity.this, R.string.toast_reader_configuration_antenna_rate_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderPowerConfigurationActivity.this, R.string.toast_reader_configuration_antenna_rate_failure);
			}
		}
	}
}
