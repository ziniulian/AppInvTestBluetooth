package com.invengo.rfidpad.config;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.DebugManager;
import com.invengo.rfidpad.base.OperationArrayAdapter;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.debug.Reader6BTrafficRateDebugActivity;
import com.invengo.rfidpad.debug.Reader6CTrafficRateDebugActivity;
import com.invengo.rfidpad.debug.ReaderBasebandUpgradeDebugActivity;
import com.invengo.rfidpad.debug.ReaderCommonDebugActivity;
import com.invengo.rfidpad.debug.ReaderIntervalScanDebugActivity;
import com.invengo.rfidpad.debug.ReaderPowerVoltageDebugActivity;
import com.invengo.rfidpad.debug.ReaderRestartDebugActivity;
import com.invengo.rfidpad.debug.ReaderStaticDebugActivity;
import com.invengo.rfidpad.debug.ReaderUpgradeDebugActivity;
import com.invengo.rfidpad.entity.OperationEntity;
import com.invengo.rfidpad.other.FoundTagActivity;
import com.invengo.rfidpad.other.StaticQConfigActivity;
import com.invengo.rfidpad.other.TagSessionConfigActivity;

public class MoreActivity extends PowerManagerActivity {

	private static final String TAG = MoreActivity.class.getSimpleName();
	private ListView mOperationListView;
	private List<OperationEntity> mList = new ArrayList<OperationEntity>();
	private DebugManager mDebugManager;
	private ReaderHolder mReaderHolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_operation);
		setTitle(R.string.title_more_operation);
		
		mDebugManager = DebugManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();
		
		initializeListEntity();
		mOperationListView = (ListView) findViewById(R.id.list_more_operation_detail);
		OperationArrayAdapter adapter = new OperationArrayAdapter(this, R.layout.list_reader_main, mList);
		mOperationListView.setAdapter(adapter);
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				OperationEntity entity = (OperationEntity) mOperationListView.getAdapter().getItem(position);
				Intent newIntent = new Intent(MoreActivity.this, entity.getCls());
				startActivity(newIntent);
				InvengoLog.i(TAG, "INFO.Start Activity {%s}", entity.getCls().getSimpleName());
			}
			
		});
	}
	
	private void initializeListEntity() {
		OperationEntity foundTagEntity = new OperationEntity(getString(R.string.text_found_tag));
		foundTagEntity.setCls(FoundTagActivity.class);
		mList.add(foundTagEntity);
		OperationEntity tagSessionEntity = new OperationEntity(getString(R.string.title_tag_session_config));
		tagSessionEntity.setCls(TagSessionConfigActivity.class);
		mList.add(tagSessionEntity);
		OperationEntity staticQEntity = new OperationEntity(getString(R.string.title_static_q_config));
		staticQEntity.setCls(StaticQConfigActivity.class);
		mList.add(staticQEntity);

		
		if(mReaderHolder.getDeviceType() == DeviceType.XC2600){
			OperationEntity idleTimeEntity = new OperationEntity(getString(R.string.title_reader_configuration_idle_time));
			idleTimeEntity.setCls(ReaderIdleTimeConfigurationActivity.class);
			mList.add(idleTimeEntity);
			OperationEntity timeIntervalEntity = new OperationEntity(getString(R.string.title_reader_configuration_time_interval));
			timeIntervalEntity.setCls(ReaderFlashTimeIntervalConfigActivity.class);
			mList.add(timeIntervalEntity);
			OperationEntity timeSynchronizationEntity = new OperationEntity(getString(R.string.title_reader_configuration_time_synchronization));
			timeSynchronizationEntity.setCls(ReaderTimeSynchronizationActivity.class);
			mList.add(timeSynchronizationEntity);
			OperationEntity thresholdVoltageEntity = new OperationEntity(getString(R.string.title_reader_configuration_threshold_voltage));
			thresholdVoltageEntity.setCls(ReaderThresholdVoltageActivity.class);
			mList.add(thresholdVoltageEntity);
			OperationEntity buzzerEntity = new OperationEntity(getString(R.string.title_reader_configuration_buzzer_configuration));
			buzzerEntity.setCls(ReaderBuzzerConfigurationActivity.class);
			mList.add(buzzerEntity);
			OperationEntity flashCacheEntity = new OperationEntity(getString(R.string.title_reader_flash_cache_data));
			flashCacheEntity.setCls(ReaderFlashCacheDataActivity.class);
			mList.add(flashCacheEntity);
			OperationEntity usbEntity = new OperationEntity(getString(R.string.title_reader_configuration_usb_control));
			usbEntity.setCls(ReaderUsbControlActivity.class);
			mList.add(usbEntity);
			OperationEntity utcEntity = new OperationEntity(getString(R.string.title_reader_configuration_utc_control));
			utcEntity.setCls(ReaderUtcControlActivity.class);
			mList.add(utcEntity);
			OperationEntity restartEntity = new OperationEntity(getString(R.string.title_reader_configuration_restart_default));
			restartEntity.setCls(ReaderRestartDebugActivity.class);
			mList.add(restartEntity);
//			OperationEntity bluetoothPasswordEntity = new OperationEntity(getString(R.string.title_reader_configuration_bluetooth_password));
//			bluetoothPasswordEntity.setCls(ReaderBluetoothPasswordActivity.class);
//			mList.add(bluetoothPasswordEntity);
		}
		
		if(mDebugManager.isDebug()){
			OperationEntity frequencyBandSessionEntity = new OperationEntity(getString(R.string.title_reader_frequency_band_config));
			frequencyBandSessionEntity.setCls(ReaderFrequencyBandConfigActivity.class);
			mList.add(frequencyBandSessionEntity);
			OperationEntity staticDebugActivity = new OperationEntity(getString(R.string.title_reader_debug_static_read));
			staticDebugActivity.setCls(ReaderStaticDebugActivity.class);
			mList.add(staticDebugActivity);
//			OperationEntity tagFilterEntity = new OperationEntity(getString(R.string.title_reader_debug_repeat_rw));
//			tagFilterEntity.setCls(RepeatRWDebugActivity.class);
//			mList.add(tagFilterEntity);
			OperationEntity commonDebugActivity = new OperationEntity(getString(R.string.title_reader_debug_common));
			commonDebugActivity.setCls(ReaderCommonDebugActivity.class);
			mList.add(commonDebugActivity);
			OperationEntity lockUserdataEntity = new OperationEntity(getString(R.string.title_reader_debug_traffic_rate));
			lockUserdataEntity.setCls(Reader6CTrafficRateDebugActivity.class);
			mList.add(lockUserdataEntity);
			OperationEntity trafficRate6BEntity = new OperationEntity(getString(R.string.title_reader_debug_traffic_rate_6B));
			trafficRate6BEntity.setCls(Reader6BTrafficRateDebugActivity.class);
			mList.add(trafficRate6BEntity);
			if(mReaderHolder.getDeviceType() == DeviceType.XC2600){
				OperationEntity upgradeEntity = new OperationEntity(getString(R.string.title_reader_debug_upgrade));
				upgradeEntity.setCls(ReaderUpgradeDebugActivity.class);
				mList.add(upgradeEntity);
			}else if(mReaderHolder.getDeviceType() == DeviceType.XC2910_V3){
				OperationEntity basebandUpgradeEntity = new OperationEntity(getString(R.string.title_reader_debug_baseband_upgrade));
				basebandUpgradeEntity.setCls(ReaderBasebandUpgradeDebugActivity.class);
				mList.add(basebandUpgradeEntity);
			}
			OperationEntity powerVoltageEntity = new OperationEntity(getString(R.string.title_reader_debug_power_voltage));
			powerVoltageEntity.setCls(ReaderPowerVoltageDebugActivity.class);
			mList.add(powerVoltageEntity);
			OperationEntity timeEntity = new OperationEntity(getString(R.string.title_reader_debug_interval_scan));
			timeEntity.setCls(ReaderIntervalScanDebugActivity.class);
			mList.add(timeEntity);
		}
//		OperationEntity lockUserdataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_lock));
//		lockUserdataEntity.setCls(TagUserdataLockActivity.class);
//		mList.add(lockUserdataEntity);
//		OperationEntity queryLockStatusEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_lock_status_query));
//		queryLockStatusEntity.setCls(TagLockStatusQueryActivity.class);
//		mList.add(queryLockStatusEntity);
//		OperationEntity readNonFixedUserdataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_non_fixed__read_userdata));
//		readNonFixedUserdataEntity.setCls(TagNonFixedUserdataReadActivity.class);
//		mList.add(readNonFixedUserdataEntity);
//		OperationEntity writeNonFixedUserdataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_non_fixed_write_userdata));
//		writeNonFixedUserdataEntity.setCls(TagNonFixedUserdataWriteActivity.class);
//		mList.add(writeNonFixedUserdataEntity);
	}
	
}
