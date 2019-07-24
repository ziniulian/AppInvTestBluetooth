package com.invengo.rfidpad.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.entity.FrequencyBandEntity;
import com.invengo.rfidpad.utils.IntegerEntity;
import com.invengo.rfidpad.utils.InvengoUtils;

import java.util.ArrayList;
import java.util.HashMap;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

/**
 * 注意频点list未刷新
 */
public class ReaderFrequencyBandConfigActivity extends PowerManagerActivity {

	private static final String TAG = ReaderFrequencyBandConfigActivity.class.getSimpleName();
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private TextView mFrequencyBandView;
	private Spinner mFrequencyBandSpinner;
	private ListView mFrequencyListView;
	private ArrayList<FrequencyEntity> mList = new ArrayList<FrequencyEntity>();
	private HashMap<String, ArrayList<FrequencyEntity>> mFrequencyMap = new HashMap<String, ArrayList<FrequencyEntity>>();

	private byte mCurrentFrequencyBand;
	private static final byte FREQUENCY_BAND_PARAMETER = 0x15;
	//	private boolean enabledTriggerSpinnerItemSelectedListener = false;
	private ReaderOperationBroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_frequency_band_config);
		setTitle(R.string.title_reader_frequency_band_config);

		mProgressBarManager = ProgressBarManager.getInstance();
		mHolder = ReaderHolder.getInstance();

		mFrequencyBandView = (TextView) findViewById(R.id.text_reader_frequency_band_id);
		mFrequencyBandSpinner = (Spinner) findViewById(R.id.spinner_reader_frequency_band_id);
		mFrequencyListView = (ListView) findViewById(R.id.list_reader_frequency_id);
		mList.add(new FrequencyEntity());
		FrequencyArrayAdapter adapter = new FrequencyArrayAdapter(this, R.layout.list_reader_frequency_detail, mList);
		mFrequencyListView.setAdapter(adapter);
		mFrequencyListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				FrequencyArrayAdapter.ViewHolder viewHolder = (FrequencyArrayAdapter.ViewHolder) view.getTag();
				viewHolder.checkView.toggle();
			}
		});

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_frequency_band, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_frequency_band_config_id:
				attemptConfigFrequency();
				break;
			case R.id.menu_frequency_band_query_id:
				attemptQueryFrequencyBand();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptConfigFrequency() {
		InvengoLog.i(TAG, "INFO.attemptConfigFrequency()");

		if(mList.size() == 0){
			return;
		}

		ArrayList<FrequencyEntity> temp = new ArrayList<FrequencyEntity>();
		for(FrequencyEntity entity : mList){
			if(entity.isCheck()){
				temp.add(entity);
			}
		}
		final byte[] frequency = new byte[temp.size() + 1];
		frequency[0] = (byte) temp.size();
		for(int i = 0; i < temp.size(); i++){
			frequency[i + 1] = (byte) temp.get(i).getFrequency();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mHolder.isConnected()) {
					SysConfig_800 configFrequencyMsg = new SysConfig_800((byte) 0x04, frequency);
					boolean success = mHolder.getCurrentReader().send(configFrequencyMsg);
					boolean result = false;
					if (success) {
						result = true;
					}
					Message msg = new Message();
					msg.what = CONFIG_FREQUENCY;
					msg.obj = result;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void attemptQueryFrequencyBand() {
		InvengoLog.i(TAG, "INFO.attemptQueryFrequencyBand()");

		//		enabledTriggerSpinnerItemSelectedListener = false;
		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_frequency_band_query_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SysQuery_800 queryFrequencyBandMsg = new SysQuery_800(FREQUENCY_BAND_PARAMETER);
		OperationTask task = new OperationTask(this);
		task.execute(queryFrequencyBandMsg);
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				//查询成功
				Object obj = intent.getSerializableExtra(OperationTask.RECEIVED_INFO_EXTRA);
				if(obj instanceof SysQuery800ReceivedInfo){//查询后的更新
					Message msg = new Message();
					msg.what = QUERY_FREQUENCY_BAND;
					msg.obj = obj;
					handler.sendMessage(msg);
				}
				InvengoUtils.showToast(ReaderFrequencyBandConfigActivity.this, R.string.toast_reader_frequency_band_query_success);
			}else{
				//查询失败
				InvengoUtils.showToast(ReaderFrequencyBandConfigActivity.this, R.string.toast_reader_frequency_band_query_failure);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeFrequencyList();
		initializeFrequencyBandView();//初始化频段
		//		initializeFrequencyBandData();//查询当前频段
		initializeData();
		InvengoLog.i(TAG, "INFO.onResume()");
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

	private void initializeFrequencyList() {
		InvengoLog.i(TAG, "INFO.initializeFrequencyList()");
		String[] parameters = getResources().getStringArray(R.array.parameter);
		for(String parameter : parameters){
			byte band = Util.convertHexStringToByteArray(parameter)[0];
			if("00".equals(parameter)){
				//				<item >(China1)920.625MHz-924.375MHz</item>
				ArrayList<FrequencyEntity> cn1List = new ArrayList<FrequencyEntity>();
				String[] cn1Frequency = getResources().getStringArray(R.array.frequency_china1);
				int i = 0;
				for(String frequency : cn1Frequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					cn1List.add(entity);
				}
				mFrequencyMap.put(parameter, cn1List);
			}else if("05".equals(parameter)){
				//		        <item >(China2)840.625MHz-844.375MHz</item>
				ArrayList<FrequencyEntity> cn2List = new ArrayList<FrequencyEntity>();
				String[] cn2Frequency = getResources().getStringArray(R.array.frequency_china2);
				int i = 0;
				for(String frequency : cn2Frequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					cn2List.add(entity);
				}
				mFrequencyMap.put(parameter, cn2List);
			}else if("06".equals(parameter)){
				//		        <item >(China3)840.625MHz-844.375MHz &#038; 920.625MHz-924.375MHz</item>
				ArrayList<FrequencyEntity> cn3List = new ArrayList<FrequencyEntity>();
				String[] cn3Frequency = getResources().getStringArray(R.array.frequency_china3);
				int i = 0;
				for(String frequency : cn3Frequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					cn3List.add(entity);
				}
				mFrequencyMap.put(parameter, cn3List);
			}else if("01".equals(parameter)){
				//		        <item >(America)902.75MHz-927.25MHz</item>
				ArrayList<FrequencyEntity> usaList = new ArrayList<FrequencyEntity>();
				String[] usaFrequency = getResources().getStringArray(R.array.frequency_America);
				int i = 0;
				for(String frequency : usaFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					usaList.add(entity);
				}
				mFrequencyMap.put(parameter, usaList);
			}else if("02".equals(parameter)){
				//		        <item >(Europe)865.7MHz-867.5MHz</item>
				ArrayList<FrequencyEntity> eurList = new ArrayList<FrequencyEntity>();
				String[] eurFrequency = getResources().getStringArray(R.array.frequency_Europe);
				int i = 0;
				for(String frequency : eurFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					eurList.add(entity);
				}
				mFrequencyMap.put(parameter, eurList);
			}else if("08".equals(parameter)){
				//		        <item >(South Africa)915.6MHz-918.8MHz</item>
				ArrayList<FrequencyEntity> saList = new ArrayList<FrequencyEntity>();
				String[] saFrequency = getResources().getStringArray(R.array.frequency_South_Africa);
				int i = 0;
				for(String frequency : saFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					saList.add(entity);
				}
				mFrequencyMap.put(parameter, saList);
			}else if("BA".equals(parameter)){
				//		        <item >(Uruguay)916.25MHz-927.25MHz</item>
				ArrayList<FrequencyEntity> uruList = new ArrayList<FrequencyEntity>();
				String[] uruFrequency = getResources().getStringArray(R.array.frequency_Uruguay);
				int i = 0;
				for(String frequency : uruFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					uruList.add(entity);
				}
				mFrequencyMap.put(parameter, uruList);
			}else if("BB".equals(parameter)){
				//		        <item >(Peru)915.25MHz-927.25MHz</item>
				ArrayList<FrequencyEntity> peruList = new ArrayList<FrequencyEntity>();
				String[] peruFrequency = getResources().getStringArray(R.array.frequency_Peru);
				int i = 0;
				for(String frequency : peruFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					peruList.add(entity);
				}
				mFrequencyMap.put(parameter, peruList);
			}else if("89".equals(parameter)){
				//		        <item >(Singapore)920.25MHz-924.75MHz</item>
				ArrayList<FrequencyEntity> sinList = new ArrayList<FrequencyEntity>();
				String[] sinFrequency = getResources().getStringArray(R.array.frequency_Singapore);
				int i = 0;
				for(String frequency : sinFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					sinList.add(entity);
				}
				mFrequencyMap.put(parameter, sinList);
			}else if("8A".equals(parameter)){
				//		        <item >(Malaysia)919.25MHz-922.75MHz</item>
				ArrayList<FrequencyEntity> malList = new ArrayList<FrequencyEntity>();
				String[] malFrequency = getResources().getStringArray(R.array.frequency_Malaysia);
				int i = 0;
				for(String frequency : malFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					malList.add(entity);
				}
				mFrequencyMap.put(parameter, malList);
			}else if("8B".equals(parameter)){
				//		        <item >(Australia)920.25MHz-925.75MHz</item>
				ArrayList<FrequencyEntity> ausList = new ArrayList<FrequencyEntity>();
				String[] ausFrequency = getResources().getStringArray(R.array.frequency_Australia);
				int i = 0;
				for(String frequency : ausFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					ausList.add(entity);
				}
				mFrequencyMap.put(parameter, ausList);
			}else if("8C".equals(parameter)){
				//		        <item >(HongKong China)920.25MHz-924.75MHz</item>
				ArrayList<FrequencyEntity> hkList = new ArrayList<FrequencyEntity>();
				String[] hkFrequency = getResources().getStringArray(R.array.frequency_HongKong_China);
				int i = 0;
				for(String frequency : hkFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					hkList.add(entity);
				}
				mFrequencyMap.put(parameter, hkList);
			}else if("BC".equals(parameter)){
				//		        <item >(Brazil)902.75MHz-907.25MHz &#038; 915.25MHz-927.25MHz</item>
				ArrayList<FrequencyEntity> braList = new ArrayList<FrequencyEntity>();
				String[] braFrequency = getResources().getStringArray(R.array.frequency_Brazil);
				int i = 0;
				for(String frequency : braFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					braList.add(entity);
				}
				mFrequencyMap.put(parameter, braList);
			}else if("8D".equals(parameter)){
				//		        <item >(New Zealand)922.25MHz-927.25MHz</item>
				ArrayList<FrequencyEntity> nzList = new ArrayList<FrequencyEntity>();
				String[] nzFrequency = getResources().getStringArray(R.array.frequency_New_Zealand);
				int i = 0;
				for(String frequency : nzFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					nzList.add(entity);
				}
				mFrequencyMap.put(parameter, nzList);
			}else if("07".equals(parameter)){
				//		        <item >(Taiwan)922.25MHz-927.25MHz</item>
				ArrayList<FrequencyEntity> twList = new ArrayList<FrequencyEntity>();
				String[] twFrequency = getResources().getStringArray(R.array.frequency_Taiwan);
				int i = 0;
				for(String frequency : twFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					twList.add(entity);
				}
				mFrequencyMap.put(parameter, twList);
			}else if("8E".equals(parameter)){
				//		        <item >(Indonesia)923.25MHz-924.75MHz</item>
				ArrayList<FrequencyEntity> indList = new ArrayList<FrequencyEntity>();
				String[] indFrequency = getResources().getStringArray(R.array.frequency_Indonesia);
				int i = 0;
				for(String frequency : indFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					indList.add(entity);
				}
				mFrequencyMap.put(parameter, indList);
			}else if("03".equals(parameter)){
				//		        <item >(Koren)917.25MHz-920.25MHz</item>
				ArrayList<FrequencyEntity> korList = new ArrayList<FrequencyEntity>();
				String[] korFrequency = getResources().getStringArray(R.array.frequency_Koren);
				int i = 0;
				for(String frequency : korFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					korList.add(entity);
				}
				mFrequencyMap.put(parameter, korList);
			}else if("04".equals(parameter)){
				//		        <item >(Japan)916.8MHz-920.4MHz</item>
				ArrayList<FrequencyEntity> japList = new ArrayList<FrequencyEntity>();
				String[] japFrequency = getResources().getStringArray(R.array.frequency_Japan);
				int i = 0;
				for(String frequency : japFrequency){
					FrequencyEntity entity = new FrequencyEntity();
					entity.setFrequencyBand(band);
					entity.setFrequency(i++);
					entity.setFrequencyName(frequency);
					japList.add(entity);
				}
				mFrequencyMap.put(parameter, japList);
			}
		}
	}

	private void initializeFrequencyBandData() {
		InvengoLog.i(TAG, "INFO.initializeFrequencyBandData()");
		//		enabledTriggerSpinnerItemSelectedListener = false;
		Thread queryFrequencyBandThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mHolder.isConnected()) {
					SysQuery_800 queryFrequencyBandMsg = new SysQuery_800(FREQUENCY_BAND_PARAMETER);
					boolean success = mHolder.getCurrentReader().send(queryFrequencyBandMsg);
					if (success) {
						if (null != queryFrequencyBandMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_FREQUENCY_BAND;
							msg.obj = queryFrequencyBandMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryFrequencyBandThread.start();
	}

	private void initializeFrequencyData() {
		InvengoLog.i(TAG, "INFO.initializeFrequencyData()");
		Thread queryFrequencyThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mHolder.isConnected()) {
					byte parameter = 0x04;
					SysQuery_800 queryFrequencyMsg = new SysQuery_800(parameter);
					boolean success = mHolder.getCurrentReader().send(queryFrequencyMsg);
					if (success) {
						if (null != queryFrequencyMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_FREQUENCY;
							msg.obj = queryFrequencyMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryFrequencyThread.start();
	}

	private static final int QUERY_FREQUENCY_BAND = 0;
	private static final int QUERY_FREQUENCY = 1;
	private static final int CONFIG_FREQUENCY = 2;
	private static final int CONFIG_FREQUENCY_BAND = 3;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_FREQUENCY_BAND:
					InvengoLog.i(TAG, "INFO.query frequency band.");
					SysQuery800ReceivedInfo bandInfo = (SysQuery800ReceivedInfo) msg.obj;
					byte parameter = bandInfo.getQueryData()[0];
					mCurrentFrequencyBand = parameter;
					//				initializeFrequencyListData(mCurrentFrequencyBand);
					for(int i = 0; i <= mFrequencyBandSpinner.getCount(); i++){
						IntegerEntity entity = (IntegerEntity) mFrequencyBandSpinner.getAdapter().getItem(i);
						if(parameter == entity.getEntity().getParameter()){
							mFrequencyBandSpinner.setSelection(entity.getIndex(), false);
							break;
						}
					}
					//查询频段后查询频点
					initializeFrequencyData();
					break;
				case QUERY_FREQUENCY:
					InvengoLog.i(TAG, "INFO.refresh frequency.");
					mList.clear();
					SysQuery800ReceivedInfo frequencyInfo = (SysQuery800ReceivedInfo) msg.obj;
					byte[] frequencyArray = frequencyInfo.getQueryData();
					ArrayList<FrequencyEntity> currentFrequencyList = mFrequencyMap.get(Util.convertByteToHexWordString(mCurrentFrequencyBand));
					for(FrequencyEntity entity : currentFrequencyList){
						for(byte frequency : frequencyArray){
							int temp = frequency & 0xFF;
							if(temp == entity.getFrequency()){
								entity.setCheck(true);
								break;
							}
						}
						mList.add(entity);
					}
					//				mList = currentFrequencyList;
					((FrequencyArrayAdapter)mFrequencyListView.getAdapter()).notifyDataSetChanged();
					//				enabledTriggerSpinnerItemSelectedListener = true;
					break;
				case CONFIG_FREQUENCY:
					boolean resultFrequency = (Boolean) msg.obj;
					if(resultFrequency){
						InvengoUtils.showToast(ReaderFrequencyBandConfigActivity.this, R.string.toast_reader_frequency_config_success);
						initializeFrequencyData();
					}else {
						InvengoUtils.showToast(ReaderFrequencyBandConfigActivity.this, R.string.toast_reader_frequency_config_failure);
					}
					break;
				case CONFIG_FREQUENCY_BAND:
					InvengoLog.i(TAG, "INFO.config frequency band.");
					boolean result = (Boolean) msg.obj;
					if(result){
						//toast
						InvengoUtils.showToast(ReaderFrequencyBandConfigActivity.this, R.string.toast_reader_frequency_band_config_success);
						initializeFrequencyData();
					}else {
						//toast
						InvengoUtils.showToast(ReaderFrequencyBandConfigActivity.this, R.string.toast_reader_frequency_band_config_failure);
					}
					break;
				default:
					break;
			}
		};
	};

	private void initializeFrequencyBandView() {
		ArrayAdapter<IntegerEntity> frequencyBandAdapter = new ArrayAdapter<IntegerEntity>(this, R.layout.myspinner);
		String[] frequencyBand = getResources().getStringArray(R.array.frequency_band);
		String[] parameter = getResources().getStringArray(R.array.parameter);
		for(int i = 0; i < frequencyBand.length; i++){
			FrequencyBandEntity bandEntity = new FrequencyBandEntity();
			bandEntity.setName(frequencyBand[i]);
			bandEntity.setParameter(Util.convertHexStringToByteArray(parameter[i])[0]);
			IntegerEntity entity = new IntegerEntity(i, bandEntity);
			frequencyBandAdapter.add(entity);
		}

		mFrequencyBandSpinner.setAdapter(frequencyBandAdapter);
		mFrequencyBandSpinner.setSelection(0, false);
		mFrequencyBandSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				//				if(enabledTriggerSpinnerItemSelectedListener){
				InvengoLog.i(TAG, "INFO.onItemSelected()");
				IntegerEntity selectedItem = (IntegerEntity) mFrequencyBandSpinner.getItemAtPosition(position);
				FrequencyBandEntity frequencyBandEntity = selectedItem.getEntity();
				byte frequencyBand = frequencyBandEntity.getParameter();
				mCurrentFrequencyBand = frequencyBand;
				attemptConfigFrequencyBand(frequencyBand);
				//				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				InvengoLog.i(TAG, "INFO.onNothingSelected()");
			}
		});
	}

	private void attemptConfigFrequencyBand(final byte data) {
		InvengoLog.i(TAG, "INFO.attemptConfigFrequencyBand()");
		Thread configFrequencyBandThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mHolder.isConnected()) {
					SysConfig_800 configFrequencyBandMsg = new SysConfig_800(FREQUENCY_BAND_PARAMETER, new byte[]{0x01, data});
					boolean success = mHolder.getCurrentReader().send(configFrequencyBandMsg);
					boolean result = false;
					if (success) {
						result = true;
					}
					Message msg = new Message();
					msg.what = CONFIG_FREQUENCY_BAND;
					msg.obj = result;
					handler.sendMessage(msg);
				}
			}
		});
		configFrequencyBandThread.start();
	}

	//	protected void initializeFrequencyListData(byte frequencyBand) {
	//
	//	}

	private class FrequencyArrayAdapter extends ArrayAdapter<FrequencyEntity>{

		private int resourceId;
		public FrequencyArrayAdapter(Context context, int textViewResourceId,
									 ArrayList<FrequencyEntity> objects) {
			super(context, textViewResourceId, objects);
			this.resourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FrequencyEntity entity = getItem(position);

			ViewHolder holder;
			if(null == convertView){
				convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
				holder = new ViewHolder();

				holder.checkView = (CheckBox) convertView.findViewById(R.id.check_reader_frequency_id);
				holder.nameView = (TextView) convertView.findViewById(R.id.text_reader_frequency_id);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.nameView.setText(entity.getFrequencyName());
			holder.checkView.setId(position);
			holder.checkView.setChecked(entity.isCheck());
			holder.checkView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int position = buttonView.getId();
					FrequencyEntity frequencyEntity = FrequencyArrayAdapter.this.getItem(position);
					frequencyEntity.setCheck(isChecked);
				}
			});

			return convertView;
		}

		class ViewHolder{
			CheckBox checkView;
			TextView nameView;
		}

	}

	private class FrequencyEntity{
		private boolean check = false;
		private byte frequencyBand;
		private int frequency;
		private String frequencyName;

		public boolean isCheck() {
			return check;
		}
		public void setCheck(boolean check) {
			this.check = check;
		}
		public byte getFrequencyBand() {
			return frequencyBand;
		}
		public void setFrequencyBand(byte frequencyBand) {
			this.frequencyBand = frequencyBand;
		}
		public int getFrequency() {
			return frequency;
		}
		public void setFrequency(int frequency) {
			this.frequency = frequency;
		}
		public String getFrequencyName() {
			return frequencyName;
		}
		public void setFrequencyName(String frequencyName) {
			this.frequencyName = frequencyName;
		}
	}
}
