package com.invengo.rfidpad.config;

import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.io.UnsupportedEncodingException;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.DebugManager;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

public class AboutActivity extends PowerManagerActivity {

	private ReaderHolder mReaderHolder;
	private TagScanSettingsCollection mSettingsCollection;
	private DebugManager mDebugManager;

	private TextView mAppVersion;
	private TextView mSystemVersion;
	private TextView mRfidSoftwareVersion;
	private TextView mRfidHardwareVersion;
	private LinearLayout mArmLayout;
	private TextView mArmSoftwareVersion;
	private TextView mArmHardwareVersion;
	private TextView mClick;

//	private LinearLayout mReaderDebugContainer;
//	private ListView mReaderDebugListView;
//	private List<OperationEntity> mList = new ArrayList<OperationEntity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setTitle(R.string.title_about);

		mReaderHolder = ReaderHolder.getInstance();
		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mDebugManager = DebugManager.getInstance();

		mAppVersion = (TextView) findViewById(R.id.text_about_software_version_detail);
		mSystemVersion = (TextView) findViewById(R.id.text_about_system_version_detail);
		mRfidSoftwareVersion = (TextView) findViewById(R.id.text_about_rfid_software_version_detail);
		mRfidHardwareVersion = (TextView) findViewById(R.id.text_about_rfid_hardware_version_detail);
		mArmLayout = (LinearLayout) findViewById(R.id.layout_xc2600_version_id);
		mArmSoftwareVersion = (TextView) findViewById(R.id.text_about_arm_software_version_detail);
		mArmHardwareVersion = (TextView) findViewById(R.id.text_about_arm_hardware_version_detail);
		mClick = (TextView) findViewById(R.id.text_clicked);

		if(mReaderHolder.getDeviceType() == DeviceType.XC2600){
			mArmLayout.setVisibility(View.VISIBLE);
		}else{
			mArmLayout.setVisibility(View.GONE);
		}

//		//Reader Debug
//		mReaderDebugContainer = (LinearLayout) findViewById(R.id.layout_reader_debug_id);
//		initializeListEntity();
//		mReaderDebugListView = (ListView) findViewById(R.id.list_reader_debug);
//		OperationArrayAdapter adapter = new OperationArrayAdapter(this, R.layout.list_reader_main, mList);
//		mReaderDebugListView.setAdapter(adapter);
	}

//	private void initializeListEntity() {
//		OperationEntity tagFilterEntity = new OperationEntity(R.drawable.settings, getString(R.string.title_reader_debug));
//		tagFilterEntity.setCls(ReaderDebugActivity.class);
//		mList.add(tagFilterEntity);
//	}

	@Override
	protected void onResume() {
		super.onResume();
		initAppVersion();
		initSystemVersion();
		queryRfidInfo();
		queryArmInfo();
		addListener();
//		initDebugInfo();
	}

//	private void initDebugInfo() {
//		if(mSettingsCollection.isDebug()){
//			if(mReaderDebugContainer.getVisibility() == View.GONE){
//				mReaderDebugContainer.setVisibility(View.VISIBLE);
//			}
//		}
//	}

	private void addListener() {
		mClick.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				attemptClick(view);
			}
		});

//		mReaderDebugListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				OperationEntity entity = (OperationEntity) mReaderDebugListView.getAdapter().getItem(position);
//				Intent readerDebugIntent = new Intent(AboutActivity.this, entity.getCls());
//				startActivity(readerDebugIntent);
//			}
//		});
	}

	private long[] mClicks = new long[10];
	protected void attemptClick(View view) {
		//每点击一次 实现左移一格数据
		System.arraycopy(mClicks, 1, mClicks, 0, mClicks.length - 1);
		//给数组的最后赋当前时钟值
		mClicks[mClicks.length - 1] = SystemClock.uptimeMillis();
		//当mClicks[0]处的值大于(当前时间-1000)时  证明在1000毫秒内点击了10次
		if(mClicks[0] > SystemClock.uptimeMillis() - 2000){
			if(!mDebugManager.isDebug()){
				mDebugManager.setDebug(true);
				InvengoUtils.showToast(this, R.string.toast_reader_debug_open);
//        		if(mReaderDebugContainer.getVisibility() == View.GONE){
//            		mReaderDebugContainer.setVisibility(View.VISIBLE);
//            	}
			}
		}
	}

	private void queryArmInfo() {
		Thread querySoftwareVersionThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					byte parameter = (byte) 0x89;
					SysQuery_800 queryRateMsg = new SysQuery_800(parameter);
					boolean success = mReaderHolder.getCurrentReader().send(queryRateMsg);
					if (success) {
						if (null != queryRateMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_ARM_SOFTWARE_VERSION;
							msg.obj = queryRateMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		querySoftwareVersionThread.start();

		Thread queryHardwareVersionThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					byte parameter = (byte) 0x8A;
					SysQuery_800 queryRateMsg = new SysQuery_800(parameter);
					boolean success = mReaderHolder.getCurrentReader().send(queryRateMsg);
					if (success) {
						if (null != queryRateMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_ARM_HARDWARE_VERSION;
							msg.obj = queryRateMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryHardwareVersionThread.start();

	}

	//	20H		读写器名称				16字节
//	21H		读写器产品型号			4字节
//	22H  	读写器出厂产品序列号		4字节
//	23H   	读写器基带软件版本号		4字节
//	25H   	基带电路硬件版本号		4字节
	private void queryRfidInfo() {
		Thread querySoftwareVersionThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					byte parameter = 0x23;
					SysQuery_800 queryRateMsg = new SysQuery_800(parameter);
					boolean success = mReaderHolder.getCurrentReader().send(queryRateMsg);
					if (success) {
						if (null != queryRateMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_RFID_SOFTWARE_VERSION;
							msg.obj = queryRateMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		querySoftwareVersionThread.start();

		Thread queryHardwareVersionThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (mReaderHolder.isConnected()) {
					byte parameter = 0x25;
//					ReadUserData_6C msg = new ReadUserData_6C(null, null, null);
					SysQuery_800 queryRateMsg = new SysQuery_800(parameter);
					boolean success = mReaderHolder.getCurrentReader().send(queryRateMsg);
					if (success) {
						if (null != queryRateMsg.getReceivedMessage()) {
							Message msg = new Message();
							msg.what = QUERY_RFID_HARDWARE_VERSION;
							msg.obj = queryRateMsg.getReceivedMessage();
							handler.sendMessage(msg);
						}
					}
				}
			}
		});
		queryHardwareVersionThread.start();

	}

	private static final int QUERY_RFID_SOFTWARE_VERSION = 0;
	private static final int QUERY_RFID_HARDWARE_VERSION = 1;
	private static final int QUERY_ARM_SOFTWARE_VERSION = 2;
	private static final int QUERY_ARM_HARDWARE_VERSION = 3;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_RFID_SOFTWARE_VERSION:
					SysQuery800ReceivedInfo querySoftwareMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] versionSoftwareData = querySoftwareMsg.getQueryData();
					try {
						String versionSoftwareInfo = new String(versionSoftwareData, "UTF-8");
						mRfidSoftwareVersion.setText(versionSoftwareInfo);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case QUERY_RFID_HARDWARE_VERSION:
					SysQuery800ReceivedInfo queryHardwareMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] versionHardwareData = queryHardwareMsg.getQueryData();
					try {
						String versionHardwareInfo = new String(versionHardwareData, "UTF-8");
						mRfidHardwareVersion.setText(versionHardwareInfo);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case QUERY_ARM_SOFTWARE_VERSION:
					SysQuery800ReceivedInfo queryArmSoftwareMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] armVersionSoftwareData = queryArmSoftwareMsg.getQueryData();
					try {
						String versionSoftwareInfo = new String(armVersionSoftwareData, "UTF-8");
						mArmSoftwareVersion.setText(versionSoftwareInfo);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case QUERY_ARM_HARDWARE_VERSION:
					SysQuery800ReceivedInfo queryArmHardwareMsg = (SysQuery800ReceivedInfo) msg.obj;
					byte[] armVersionHardwareData = queryArmHardwareMsg.getQueryData();
					try {
						String versionHardwareInfo = new String(armVersionHardwareData, "UTF-8");
						mArmHardwareVersion.setText(versionHardwareInfo);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		};
	};

	private void initSystemVersion() {
		String sdk = android.os.Build.VERSION.RELEASE;
		int sdkInt = android.os.Build.VERSION.SDK_INT;
		String version = "Android " + sdk + "{" + sdkInt + "}";
		mSystemVersion.setText(version);
	}

	private void initAppVersion() {
		PackageManager packageManager = getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
			String version = packageInfo.versionName;

			mAppVersion.setText(version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

}
