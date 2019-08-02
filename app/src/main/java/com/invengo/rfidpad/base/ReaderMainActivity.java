package com.invengo.rfidpad.base;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.lib.system.device.type.DeviceType;
import com.invengo.lib.util.SysUtil;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.config.AboutActivity;
import com.invengo.rfidpad.config.MoreActivity;
import com.invengo.rfidpad.config.ReaderPowerConfigurationActivity;
import com.invengo.rfidpad.config.TagFilterConfigActivity;
import com.invengo.rfidpad.entity.OperationEntity;
import com.invengo.rfidpad.entity.TagScanInfoEntity;
import com.invengo.rfidpad.scan.Tag6BOperationActivity;
import com.invengo.rfidpad.scan.Tag6COperationActivity;
import com.invengo.rfidpad.scan.TagGBOperationActivity;
import com.invengo.rfidpad.scan.TagScanSettingsActivity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.GBMemoryBank;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.BaseMessage;
import invengo.javaapi.protocol.IRP1.GBAccessReadTag;
import invengo.javaapi.protocol.IRP1.GBAccessReadTag.GBReadMemoryBank;
import invengo.javaapi.protocol.IRP1.GBCombinationReadTag;
import invengo.javaapi.protocol.IRP1.GBInventoryTag;
import invengo.javaapi.protocol.IRP1.GBReadAllBank;
import invengo.javaapi.protocol.IRP1.Keepalive;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.IRP1.RXD_BARCODE;
import invengo.javaapi.protocol.IRP1.RXD_ReaderTriggerStatus;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadBarcode;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.Reader;
import invengo.javaapi.protocol.IRP1.SysConfig_800;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

public class ReaderMainActivity extends AbstractBaseActivity {

	private static final String TAG = ReaderMainActivity.class.getSimpleName();
	private static final String MODEL_XC2910 = "XC2910";
	private static final String MODEL_XC2600 = "XC2600";
	private static final int KEYCODE_TRIGGER = 300;//扳机键
	private static final int KEYCODE_SIDE_TRIGGER = 299;//侧键
	private static boolean ENABLE_LOG = true;
	private DeviceType mDeviceType;
	private boolean mIsExit = false;
	private boolean mContinual = false;

	private ExecutorService mExecutorService;

	private ReaderHolder mReaderHolder;
	private BluetoothAdapter mAdapter;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;//用于监听抽屉导航栏动作
	private FrameLayout mDrawerLeftFrame;
	private FrameLayout mContentRightFrame;

	/*
	 * Start drawer
	 */
	//	private ImageView mReaderStatus;
	private TextView mReaderStatus;
	private TextView mDeviceInfo;
	private ListView mTagScanListView;
	private List<OperationEntity> mTagScanList = new ArrayList<OperationEntity>();
	//	private CheckBox mChannelSettingsCheckBox;
	private RadioGroup mRadioGroup;
	private RadioButton mRfidSettingsButton;
	private RadioButton mBarcodeSettingsButton;
	private Button mDisconnectButton;
	private Button mExitButton;

	private View mConnectStatusView;
	private TextView mProgressBarStatusView;
	private ReaderConnectBroadcastReceiver mBroadcastReceiver;
	private CustomBluetoothBroadcastReceiver mCustomBluetoothBroadcastReceiver;

	private static final byte PARAMETER_RFID_1D2D = (byte) 0x84;
	private static final byte[] RFID_DATA = new byte[]{0x01, 0x00};
	private static final byte[] BARCODE_DATA = new byte[]{0x01, 0x01};
	private boolean DRAWER_OPEN_CLOSE_STATUS = false;//True-open,False-close
	/*
	 * End drawer
	 */

	/*
	 * Start Main Content
	 */
	private TagScanSettingsCollection mSettingsCollection;
	private VoiceManager mVoiceManager;
	private View mTagNumberLayout;
	private TextView mTagNumberView;
	private ListView mTagInfoListView;
	private List<TagScanInfoEntity> mList = new ArrayList<TagScanInfoEntity>();
	public static final String TAG_DATA = "TAG_DATA";
	private static final String TAG_6C = "6C";
	private static final String TAG_GB = "GB";
	private boolean reading = false;

	private LinearLayout mConnectLayout;
	private Button mConnectButton;
	/*
	 * End Main Content
	 */

	private boolean mIsBLE = false;//for Hand-Ring Reader.false-non BLE,true-BLE
	private int devpow = -1;	// 显示的电量值
	private int devpowTv = -1;	// 电量的实际值
	private int devpowWait = 1;	// 等待电量信息稳定的标记

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(R.string.title_tag_scan);

		String appName = SysUtil.getAppName(this);
		if(ENABLE_LOG){//保存InvengoLog输出的日志信息,后续需要修改所有操作的日志信息
			InvengoLog.startUp("Log", appName);
		}
		SysUtil.wakeLock(this, appName);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mVoiceManager = VoiceManager.getInstance(getApplicationContext());
		mReaderHolder = ReaderHolder.getInstance();
		mReaderHolder.setConnected(false);
		mDeviceType = mReaderHolder.getDeviceType();
		mExecutorService = Executors.newFixedThreadPool(5);

		mIsBLE = (mReaderHolder.getCurrentReader().getContext() == null) ? false : true;//for Hand-Ring Reader

		initializeDrawer();
		initializeContent();
		if(mDeviceType == DeviceType.XC2600){
			mAdapter = BluetoothAdapter.getDefaultAdapter();
			registerReaderConnectBroadcastReceiver();
		}
		//		attemptQueryRssi();

		powT.start();	// 启动电量渐变线程
	}

	/**
	 * 初始化导航栏
	 */
	private void initializeDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);//设置拉开导航栏后主界面的显示界面
		mDrawerLeftFrame = (FrameLayout) findViewById(R.id.left_frame);
		mContentRightFrame = (FrameLayout) findViewById(R.id.content_frame);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
			@Override
			public void onDrawerOpened(View drawerView) {
				DRAWER_OPEN_CLOSE_STATUS = true;
				getActionBar().setTitle(mReaderHolder.getReaderName());
				stopScanTag(true);//打开drawer,自动停止读卡
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				DRAWER_OPEN_CLOSE_STATUS = false;
				getActionBar().setTitle(R.string.title_tag_scan);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mReaderStatus = (TextView) findViewById(R.id.text_connect_status_description);
		mDeviceInfo = (TextView) findViewById(R.id.text_device_info);

		initializeListEntity();
		mTagScanListView = (ListView) findViewById(R.id.list_tag_scan);
		mTagScanListView.setAdapter(new OperationArrayAdapter(this, R.layout.list_reader_main, mTagScanList));
		mTagScanListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				OperationEntity selected = (OperationEntity) mTagScanListView.getAdapter().getItem(position);
				Intent intent = new Intent(ReaderMainActivity.this, selected.getCls());
				startActivity(intent);
			}
		});

		//		mChannelSettingsCheckBox = (CheckBox) findViewById(R.id.check_rfid_1d2d_settings_id);
		mRadioGroup = (RadioGroup) findViewById(R.id.radio_group_settings_id);
		mRfidSettingsButton = (RadioButton) findViewById(R.id.radio_rfid_settings_id);
		mBarcodeSettingsButton = (RadioButton) findViewById(R.id.radio_barcode_settings_id);
		mDisconnectButton = (Button) findViewById(R.id.button_reader_main_disconnect_id);

		if(mDeviceType != DeviceType.XC2600){
			//			mChannelSettingsCheckBox.setVisibility(View.GONE);
			mRadioGroup.setVisibility(View.GONE);
			mDisconnectButton.setVisibility(View.GONE);
		}else if(mDeviceType == DeviceType.XC2600 && mIsBLE){//for Hand-Ring Reader
			mRadioGroup.setVisibility(View.GONE);
			mDisconnectButton.setVisibility(View.GONE);
		}else {
			//			mChannelSettingsCheckBox.setVisibility(View.VISIBLE);
			mRadioGroup.setVisibility(View.VISIBLE);
			mDisconnectButton.setVisibility(View.VISIBLE);
			int channelType = mReaderHolder.getChannelType();
			if(channelType == ReaderHolder.RFID_CHANNEL_TYPE){//RFID
				//				mChannelSettingsCheckBox.setChecked(true);
				//				mChannelSettingsCheckBox.setText(R.string.check_rfid_label);
				mRfidSettingsButton.setChecked(true);
			}else if(channelType == ReaderHolder.BARCODE_CHANNEL_TYPE){
				//				mChannelSettingsCheckBox.setChecked(false);
				//				mChannelSettingsCheckBox.setText(R.string.check_1d2d_label);
				mBarcodeSettingsButton.setChecked(true);
			}
		}

		mExitButton = (Button) findViewById(R.id.button_reader_main_exit_id);

		mConnectStatusView = findViewById(R.id.connect_status);
		mProgressBarStatusView = (TextView) findViewById(R.id.progressbar_status_message);

	}

	/**
	 * 初始化主界面布局,主界面为扫描界面,包含扫描标签及读取条码
	 */
	private void initializeContent() {
		mTagNumberLayout = findViewById(R.id.layout_tag_scan_number_id);
		mTagNumberView = (TextView) findViewById(R.id.textview_tag_scan_number_id);
		mTagInfoListView = (ListView) findViewById(R.id.list_tag_scan_detail);
		mTagInfoListView.setEmptyView(findViewById(R.id.text_tag_scan_empty));
		TagInfoArrayAdapter adapter = new TagInfoArrayAdapter(this, R.layout.list_tag_scan_detail_item, mList);
		mTagInfoListView.setAdapter(adapter);
		mTagInfoListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(reading){
					return;
				}
				if(mReaderHolder.getChannelType() == ReaderHolder.BARCODE_CHANNEL_TYPE){
					//do something
					return;
				}
				TagScanInfoEntity selected = (TagScanInfoEntity) mTagInfoListView.getAdapter().getItem(position);
				String tagType = selected.getType();
				Intent newIntent = null;
				if(tagType.startsWith(TAG_6C)){//6C
					newIntent = new Intent(ReaderMainActivity.this, Tag6COperationActivity.class);
					String transmitData = "";
					if(mSettingsCollection.getEpcChecked() == Contants.CHECKED){
						transmitData = selected.getEpc();
					}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED || mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
						transmitData = selected.getTid();
					}

					newIntent.putExtra(TAG_DATA, transmitData);
				}else if(tagType.startsWith(TAG_GB)){
					newIntent = new Intent(ReaderMainActivity.this, TagGBOperationActivity.class);
					String transmitData = selected.getEpc();
					if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){
						if(mSettingsCollection.isGbTid()){
							transmitData = selected.getTid();
						}else if(mSettingsCollection.isGbUserdata()){
							transmitData = selected.getUserdata();
						}
					}
					newIntent.putExtra(TAG_DATA, transmitData);
					//					return;
				}else{//6B
					newIntent = new Intent(ReaderMainActivity.this, Tag6BOperationActivity.class);
					String transmitData = selected.getTid();//ID Data
					newIntent.putExtra(TAG_DATA, transmitData);
				}

				startActivity(newIntent);
			}
		});

		mConnectLayout = (LinearLayout) findViewById(R.id.layout_connect_id);
		mConnectButton = (Button) findViewById(R.id.button_connect_id);
		mConnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mIsBLE) {
					attemptConnect(true);
				}else {
					attemptConnect(false);
				}
			}
		});
	}

	/*
	 * 响应导航栏监听器中invalidateOptionsMenu()方法
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//隐藏动作菜单
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLeftFrame);
		menu.findItem(R.id.menu_tag_scan_detail_start).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_tag_scan_detail_clear).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * 初始化导航栏中ListView列
	 */
	private void initializeListEntity() {
		OperationEntity settingsEntity = new OperationEntity(getString(R.string.text_tag_scan_settings));
		settingsEntity.setCls(TagScanSettingsActivity.class);
		mTagScanList.add(settingsEntity);

		OperationEntity rateEntity = new OperationEntity(getString(R.string.text_antenna_rate_setting));
		rateEntity.setCls(ReaderPowerConfigurationActivity.class);
		mTagScanList.add(rateEntity);

		OperationEntity tagFilterEntity = new OperationEntity(getString(R.string.title_tag_filter_configuration));
		tagFilterEntity.setCls(TagFilterConfigActivity.class);
		mTagScanList.add(tagFilterEntity);

		OperationEntity moreEntity = new OperationEntity(getString(R.string.text_more_settings));
		moreEntity.setCls(MoreActivity.class);
		mTagScanList.add(moreEntity);

		OperationEntity aboutEntity = new OperationEntity(getString(R.string.title_about));
		aboutEntity.setCls(AboutActivity.class);
		mTagScanList.add(aboutEntity);
	}

	/**
	 * 初始化导航栏中读写器连接状态及蓝牙设备信息
	 */
	private void initializeReaderStatus() {
		mReaderStatus.setText(mReaderHolder.isConnected() ? R.string.text_reader_connect_status : R.string.text_reader_disconnect_status);
		mDeviceInfo.setText(mReaderHolder.getDeviceName());
	}

	private Menu mOperationMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		InvengoLog.i(TAG, "INFO.onCreateOptionsMenu()");
		this.mOperationMenu = menu;
		getMenuInflater().inflate(R.menu.menu_tag_scan_detail, menu);
		flushPow();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}

		playBeep();
		int selectedItemId = item.getItemId();
		switch (selectedItemId) {
			case R.id.menu_tag_scan_detail_start:
				//			if(!mReaderHolder.isConnected()){
				//				InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
				//				break;
				//			}
				//
				////			/*
				////			 * For test CrashHandler
				////			 */
				////			if(true){
				////				throw new RuntimeException("This is Test!");
				////			}
				//
				//
				//			if(!reading){//start reading tags
				//				if(mDeviceType == DeviceType.XC2600){
				//					if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
				//						startScanTag();
				//					}else if(mReaderHolder.getChannelType() == ReaderHolder.BARCODE_CHANNEL_TYPE){
				//						startScanBarcode();
				//					}
				//				}else{
				//					if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){
				//						startScanGBTag();
				//					}else{
				//						startScanTag();
				//					}
				//				}
				//				item.setTitle(R.string.menu_tag_scan_detail_stop_title);
				//
				//			}else{//stop reading tags
				//				stopScanTag();
				//				item.setTitle(R.string.menu_tag_scan_detail_start_title);
				//			}
				attemptScan(true);
				break;
			case R.id.menu_tag_scan_detail_clear:
				clearScanTag();
				mTagNumberLayout.setVisibility(View.GONE);
				break;
			//		case R.id.menu_connect://connect_menu
			//			if(!mAdapter.isEnabled()){
			//				InvengoUtils.showToast(this, getString(R.string.toast_bluetooth_close));
			//				break;
			//			}
			//			if(!mReaderHolder.isConnected()){
			////				mProgressBarStatusView.setText(R.string.progress_bar_hint_reconnecting);
			////				showProgress(true);
			//				mConnectButton.setEnabled(false);
			//				InvengoUtils.showToast(this, R.string.progress_bar_hint_reconnecting);
			//				Reader mNewReader = mReaderHolder.createNewReader(mReaderHolder.getReaderName(), mReaderHolder.getDeviceName());
			//				ReaderOperationTask task = new ReaderOperationTask(this);
			//				task.execute(mNewReader);
			//			}else {
			//				InvengoUtils.showToast(this, getString(R.string.toast_reader_connected));
			//			}
			//			break;
			default:
				break;
		}
		return true;
	}

	private void attemptScan(boolean isUIThread) {
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		//		/*
		//		 * For test CrashHandler
		//		 */
		//		if(true){
		//			throw new RuntimeException("This is Test!");
		//		}

		if(!reading){//start reading tags
			if(mDeviceType == DeviceType.XC2600){
				if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){
					startScanGBTag();
					//					InvengoUtils.showToast(this, R.string.toast_tag_scan_gb_unsupport);
					//					return;
				}else{
					if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
						startScanTag();
					}else if(mReaderHolder.getChannelType() == ReaderHolder.BARCODE_CHANNEL_TYPE){
						startScanBarcode();
					}
				}
			}else{
				if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){
					startScanGBTag();
				}else{
					startScanTag();
				}
			}
			if(isUIThread){
				mOperationMenu.findItem(R.id.menu_tag_scan_detail_start).setTitle(R.string.menu_tag_scan_detail_stop_title);
			}else{
				Message msg = new Message();
				msg.what = MENUITEM_UPDATE;
				msg.obj = R.string.menu_tag_scan_detail_stop_title;
				readerHandler.sendMessage(msg);
			}
		}else{//stop reading tags
			stopScanTag();
			if(isUIThread){
				mOperationMenu.findItem(R.id.menu_tag_scan_detail_start).setTitle(R.string.menu_tag_scan_detail_start_title);
			}else{
				Message msg = new Message();
				msg.what = MENUITEM_UPDATE;
				msg.obj = R.string.menu_tag_scan_detail_start_title;
				readerHandler.sendMessage(msg);
			}
		}
	}

	private void startScanGBTag() {
		reading = true;
		int antenna = mSettingsCollection.getAntenna();
		//		int operationType = 1;
		int operationType = mSettingsCollection.isLoop() ? 1 : 0;

		if(mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED){//inventory
			int target = mSettingsCollection.getTarget();
			int session = mSettingsCollection.getSession();
			int condition = mSettingsCollection.getCondition();

			final GBInventoryTag message = new GBInventoryTag((byte) antenna, target, session, condition);
			sendGBMessage(message);
		}else if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){//access read
			GBReadMemoryBank bank = GBReadMemoryBank.EPC_GB_Access;
			GBMemoryBank matchingBank = GBMemoryBank.GBUser1Memory;
			if(mSettingsCollection.isGbEpc()){
				//
			}else if(mSettingsCollection.isGbTid()){
				bank = GBReadMemoryBank.TID_GB_Access;
			}else if(mSettingsCollection.isGbUserdata()){
				bank = GBReadMemoryBank.Sub_UserData_GB_Access;
				matchingBank = InvengoUtils.getUserBank(mSettingsCollection.getGbUserdataNo());
			}
			GBAccessReadTag message = new GBAccessReadTag(bank, matchingBank);

			String password = mSettingsCollection.getGbPassword();
			int address = mSettingsCollection.getGbAddress();
			int len = mSettingsCollection.getGbLen();

			message.setOperationType(operationType);
			message.setAntenna((byte) antenna);
			message.setDefaultAccessPassword(password);
			message.setHeadAddress(address);
			message.setLength(len);

			sendGBMessage(message);
		}else if(mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED){//combination read
			String tidPassword = mSettingsCollection.getGbTidPassword();
			int tidLength = mSettingsCollection.getGbTidLen();

			GBCombinationReadTag message = new GBCombinationReadTag((byte)antenna, operationType, tidPassword, tidLength);
			sendGBMessage(message);
		}else if(mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED){//all read
			byte tidLength = (byte) mSettingsCollection.getGbAllTidLen();
			byte[] tidPwd = Util.convertHexStringToByteArray(mSettingsCollection.getGbAllTidPassword());
			byte epcLength = (byte) mSettingsCollection.getGbAllEpcLen();
			GBMemoryBank userdataBank = InvengoUtils.getUserBank(mSettingsCollection.getGbAllUserdataNo());
			byte userdataAddress = (byte) mSettingsCollection.getGbAllUserdataAddress();
			byte userdataLen = (byte) mSettingsCollection.getGbAllUserdataLen();
			byte[] userdataPassword = Util.convertHexStringToByteArray(mSettingsCollection.getGbAllUserdataPassword());

			//			550011 29 81 01 06 00000000 08 30 00000000 00040688D4
			GBReadAllBank message = new GBReadAllBank((byte)antenna, tidLength, tidPwd, epcLength, userdataBank, userdataAddress, userdataLen, userdataPassword);
			sendGBMessage(message);
		}
	}

	private void sendGBMessage(final BaseMessage message){
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					mReaderHolder.getCurrentReader().send(message);
					Message message = new Message();
					message.what = START;
					message.obj = getString(R.string.toast_tag_scan_start);
					readerHandler.sendMessage(message);
				}
			}
		}).start();
	}

	private void attemptConnect(boolean isBLE){
		if (!mAdapter.isEnabled()) {
			InvengoUtils.showToast(this, getString(R.string.toast_bluetooth_close));
			return;
		}

		mConnectButton.setEnabled(false);
		InvengoUtils.showToast(this, R.string.progress_bar_hint_reconnecting);
		Reader mNewReader = null;
		if(!isBLE) {//标准蓝牙
			mNewReader = mReaderHolder.createBluetoothReader(mReaderHolder.getReaderName(), mReaderHolder.getDeviceName());
			mNewReader.setChannelType((mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE) ? BaseReader.ReaderChannelType.RFID_CHANNEL_TYPE : BaseReader.ReaderChannelType.BARCODE_CHANNEL_TYPE);
		}else {//低功耗蓝牙
			mNewReader = mReaderHolder.createBluetoothLeReader(mReaderHolder.getReaderName(), mReaderHolder.getDeviceName(), this);
		}
		ReaderOperationTask task = new ReaderOperationTask(this);
		task.execute(mNewReader);
	}

	private void attemptDisconnect() {
		if (mReaderHolder.isConnected()) {

			mProgressBarStatusView.setText(R.string.progress_bar_hint_disconnecting);
			showProgress(true);
			mTagScanListView.setEnabled(false);
			//			mChannelSettingsCheckBox.setEnabled(false);
			mRfidSettingsButton.setEnabled(false);
			mBarcodeSettingsButton.setEnabled(false);
			mDisconnectButton.setEnabled(false);
			mExitButton.setEnabled(false);

			ReaderDisconnectTask task = new ReaderDisconnectTask();
			task.execute();
		} else {
			InvengoUtils.showToast(this, getString(R.string.toast_reader_disconnected));
		}
	}

	protected void attemptSelectChannel() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean success = false;
				if(mReaderHolder.isConnected()){
					SysConfig_800 message = null;
					if(mRfidSettingsButton.isChecked()){
						message = new SysConfig_800(PARAMETER_RFID_1D2D, RFID_DATA);
					}else if(mBarcodeSettingsButton.isChecked()){
						message = new SysConfig_800(PARAMETER_RFID_1D2D, BARCODE_DATA);
					}
					success = mReaderHolder.getCurrentReader().send(message);
				}else{
					success = false;
				}
				Message msg = new Message();
				msg.what = CHANNEL_SELECTED;
				msg.obj = success;
				//				msg.obj = true;
				readerHandler.sendMessage(msg);
			}
		}).start();
	}

	//	private void attemptOpenChannel(){
	//		new Thread(new Runnable() {
	//
	//			@Override
	//			public void run() {
	//				SysConfig_800 message = null;
	//				if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
	//					message = new SysConfig_800(PARAMETER_RFID, DATA);
	//				}else if(mReaderHolder.getChannelType() == ReaderHolder.BARCODE_CHANNEL_TYPE){
	//					message = new SysConfig_800(PARAMETER_1D2D, DATA);
	//				}
	//				boolean success = mReaderHolder.getCurrentReader().send(message);
	//				Message msg = new Message();
	//				msg.what = CHANNEL_OPEN;
	//				msg.obj = success;
	////				msg.obj = true;
	//				readerHandler.sendMessage(msg);
	//			}
	//		}).start();
	//	}

	/**
	 * 退出demo
	 */
	protected void attemptExit(){
		//需要发送关机指令0x80
		if(mDeviceType == DeviceType.XC2600 && mIsBLE == false){
			showDeviceDialog(R.string.message_shutdown_xc2600);
		}else {
			if(mReaderHolder.isConnected()){
				mReaderHolder.disConnect();
			}
			// unregisterReaderConnectBroadcastReceiver();
			finish();
		}
	}

	private static final int CONNECTED = 0;
	private static final int DISCONNECTED = 1;
	private static final int START = 2;
	private static final int STOP = 3;
	private static final int RFID_REFRESH = 4;
	private static final int CHANNEL_SELECTED = 5;
	private static final int CHANNEL_OPEN = 6;
	private static final int BARCODE_REFRESH = 7;
	private static final int QUERY_RSSI = 8;
	private static final int QUERY_UTC = 9;
	private static final int TIMER_RESPONSE = 10;
	private static final int MENUITEM_UPDATE = 11;
	private static final int EXIT = 12;
	private static final int DEVPOW = 13;
	private Handler readerHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case CONNECTED:
					//				attemptOpenChannel();
					//				mReaderStatus.setText(R.string.text_reader_connect_status);
					//				mReaderHolder.setConnected(true);

					boolean success = (Boolean) msg.obj;
					mConnectButton.setEnabled(true);
					if(success){
						if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
							//						mChannelSettingsCheckBox.setChecked(true);
							//						mChannelSettingsCheckBox.setText(R.string.check_rfid_label);
							mRfidSettingsButton.setChecked(true);
						}else if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
							//						mChannelSettingsCheckBox.setChecked(false);
							//						mChannelSettingsCheckBox.setText(R.string.check_1d2d_label);
							mBarcodeSettingsButton.setChecked(true);
						}
						if(!mReaderHolder.isConnected()){
							mReaderStatus.setText(R.string.text_reader_connect_status);
							mReaderHolder.setConnected(true);
							mConnectLayout.setVisibility(View.GONE);
							mRadioGroup.setEnabled(true);
							mRfidSettingsButton.setEnabled(true);
							mBarcodeSettingsButton.setEnabled(true);
							addReaderCallback();
						}
						InvengoUtils.showToast(ReaderMainActivity.this, getString(R.string.toast_bluetooth_connect));
					}else{//通道打开失败，自动断开连接
						mReaderHolder.disConnect();
						mReaderHolder.disposeReader();
						mReaderHolder.setConnected(false);
						InvengoUtils.showToast(ReaderMainActivity.this, getString(R.string.toast_connect_failure));
					}
					break;
				case DISCONNECTED:
					mReaderStatus.setText(R.string.text_reader_disconnect_status);
					mReaderHolder.setConnected(false);
					mReaderHolder.disposeReader();
					mConnectLayout.setVisibility(View.VISIBLE);

					mTagScanListView.setEnabled(true);
					//				mChannelSettingsCheckBox.setEnabled(true);
					mRadioGroup.setEnabled(true);
					//				mRfidSettingsButton.setEnabled(true);
					//				mBarcodeSettingsButton.setEnabled(true);
					mDisconnectButton.setEnabled(true);
					mExitButton.setEnabled(true);
					removeReaderCallback();;
					break;
				case START:
					String message = (String) msg.obj;
					InvengoUtils.showToast(ReaderMainActivity.this, message);
					break;
				case STOP:
					if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
						InvengoUtils.showToast(ReaderMainActivity.this, R.string.toast_tag_scan_stop);
					}else{
						InvengoUtils.showToast(ReaderMainActivity.this, R.string.toast_barcode_scan_stop);
					}
					break;
				case RFID_REFRESH:
					if(mTagNumberLayout.getVisibility() == View.GONE){
						mTagNumberLayout.setVisibility(View.VISIBLE);
					}

					Bundle data = msg.getData();
					String type = data.getString("type").toUpperCase()
							.equals(TAG_6C) ? getString(R.string.label_tag_scan_type_6c)
							: (data.getString("type").toUpperCase().equals(TAG_GB) ? getString(R.string.label_tag_scan_type_gb)
							: getString(R.string.label_tag_scan_type_6b));
					String epc = data.getString("epc");
					String tid = data.getString("tid");
					String userData = data.getString("userData");
					String rssi = data.getString("rssi");
					String utc = data.getString("utc");
					boolean isExists = false;
					for(TagScanInfoEntity entity : mList){
						if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){//6C
							if(mSettingsCollection.getEpcChecked() == Contants.CHECKED){
								String oldEpc = entity.getEpc();
								if(epc.equals(oldEpc)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setRssi(rssi);
									entity.setUtc(utc);
									break;
								}
							}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
								String oldTid = entity.getTid();
								if(tid.equals(oldTid)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setUtc(utc);
									break;
								}
							}else if(mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
								String oldData = entity.getEpc() + entity.getTid() + entity.getUserdata();
								if((epc + tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setRssi(rssi);
									entity.setUtc(utc);
									break;
								}
							}
						}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){//6B
							if(mSettingsCollection.getId6BChecked() == Contants.CHECKED){
								String oldTid = entity.getTid();
								if(tid.equals(oldTid)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setRssi(rssi);
									break;
								}
							}else if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){
								String oldData = entity.getTid() + entity.getUserdata();
								if((epc + tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setUtc(utc);
									break;
								}
							}
						}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){//6c标签、6B标签混读如何返回数据
							if(type.toUpperCase().equals(TAG_6C)){//6C
								String oldData = entity.getEpc() + entity.getTid() + entity.getUserdata();
								if((epc + tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setRssi(rssi);
									entity.setUtc(utc);
									break;
								}
							}else{//6B
								String oldData = entity.getTid() + entity.getUserdata();
								if((tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setUtc(utc);
									break;
								}
							}
						}else if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){//GB
							if(mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED){//inventory
								String oldData = entity.getEpc();
								if(epc.equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setUtc(utc);
									break;
								}
							}else if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){//access read
								String oldData = "";
								if(mSettingsCollection.isGbEpc()){//编码区
									oldData = entity.getEpc();
									if(epc.equals(oldData)){
										isExists = true;
										int oldNumber = entity.getNumber();
										entity.setNumber(oldNumber + 1);
										entity.setUtc(utc);
										break;
									}
								}else if(mSettingsCollection.isGbTid()){//信息区
									oldData = entity.getTid();
									if(tid.equals(oldData)){
										isExists = true;
										int oldNumber = entity.getNumber();
										entity.setNumber(oldNumber + 1);
										entity.setUtc(utc);
										break;
									}
								}else if(mSettingsCollection.isGbUserdata()){//用户子区
									oldData = entity.getUserdata();
									if(userData.equals(oldData)){
										isExists = true;
										int oldNumber = entity.getNumber();
										entity.setNumber(oldNumber + 1);
										entity.setUtc(utc);
										break;
									}
								}

							}else if(mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED){//combination read
								String oldData = entity.getEpc() + entity.getTid();
								if((epc + tid).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setUtc(utc);
									break;
								}
							}else if(mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED){//all read
								String oldData = entity.getEpc() + entity.getTid() + entity.getUserdata();
								if((epc + tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									entity.setUtc(utc);
									break;
								}
							}
						}
					}

					if(!isExists){
						TagScanInfoEntity newEntity = new TagScanInfoEntity();
						newEntity.setType(type);
						newEntity.setEpc(epc);
						newEntity.setTid(tid);
						newEntity.setUserdata(userData);
						newEntity.setRssi(rssi);
						newEntity.setUtc(utc);
						newEntity.setNumber(1);
						mList.add(newEntity);
					}
					((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
					mTagNumberView.setText(String.valueOf(mList.size()));
					break;
				case CHANNEL_SELECTED:
					boolean selectedResult = (Boolean) msg.obj;
					if(selectedResult){
						clearScanTag();//切换通道成功后将旧数据清空
						mTagNumberLayout.setVisibility(View.GONE);
						if(mRfidSettingsButton.isChecked()){//RFID
							mReaderHolder.setChannelType(ReaderHolder.RFID_CHANNEL_TYPE);
							//						mChannelSettingsCheckBox.setText(R.string.check_rfid_label);
						}else if(mBarcodeSettingsButton.isChecked()){//1D2D
							mReaderHolder.setChannelType(ReaderHolder.BARCODE_CHANNEL_TYPE);
							//						mChannelSettingsCheckBox.setText(R.string.check_1d2d_label);
						}
						InvengoUtils.showToast(ReaderMainActivity.this, R.string.toast_channel_selected_success);
					}else{
						if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
							//						mChannelSettingsCheckBox.setChecked(!mChannelSettingsCheckBox.isChecked());//返回原来的状态
							mRfidSettingsButton.setChecked(true);
						}else if(mReaderHolder.getChannelType() == ReaderHolder.BARCODE_CHANNEL_TYPE){
							mBarcodeSettingsButton.setChecked(true);
						}
						InvengoUtils.showToast(ReaderMainActivity.this, R.string.toast_channel_selected_failure);
					}
					break;
				case CHANNEL_OPEN:
					boolean openResult = (Boolean) msg.obj;
					mConnectButton.setEnabled(true);
					if(openResult){
						if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
							//						mChannelSettingsCheckBox.setChecked(true);
							//						mChannelSettingsCheckBox.setText(R.string.check_rfid_label);
							mRfidSettingsButton.setChecked(true);
						}else if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
							//						mChannelSettingsCheckBox.setChecked(false);
							//						mChannelSettingsCheckBox.setText(R.string.check_1d2d_label);
							mBarcodeSettingsButton.setChecked(true);
						}
						if(!mReaderHolder.isConnected()){
							mReaderStatus.setText(R.string.text_reader_connect_status);
							mReaderHolder.setConnected(true);
							mConnectLayout.setVisibility(View.GONE);
							addReaderCallback();
						}
						InvengoUtils.showToast(ReaderMainActivity.this, getString(R.string.toast_bluetooth_connect));
					}else{//通道打开失败，自动断开连接
						mReaderHolder.disConnect();
						mReaderHolder.disposeReader();
						mReaderHolder.setConnected(false);
						InvengoUtils.showToast(ReaderMainActivity.this, getString(R.string.toast_connect_failure));
					}
					break;
				case BARCODE_REFRESH:
					mOperationMenu.findItem(R.id.menu_tag_scan_detail_start)
							.setTitle(R.string.menu_tag_scan_detail_start_title);
					reading = false;
					cancelTask();

					String barcodeType = getString(R.string.label_barcode_scan_type);
					Bundle barcodeData = (Bundle) msg.getData();
					String barcode = barcodeData.getString("barcode");
					String barcodeUtc = barcodeData.getString("utc");
					isExists = false;
					for(TagScanInfoEntity entity : mList){
						String oldData = entity.getBarcode();
						if(barcode.equals(oldData)){
							isExists = true;
							int oldNumber = entity.getNumber();
							entity.setNumber(oldNumber + 1);
							entity.setUtc(barcodeUtc);
							break;
						}
					}

					if(!isExists){
						TagScanInfoEntity newEntity = new TagScanInfoEntity();
						newEntity.setType(barcodeType);
						newEntity.setBarcode(barcode);
						newEntity.setUtc(barcodeUtc);
						newEntity.setNumber(1);
						mList.add(newEntity);
					}
					((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
					break;
				case DEVPOW:
					 mOperationMenu.findItem(R.id.menu_tag_scan_detail_devpow).setIcon(getResources().getIdentifier("stat_sys_battery_" + devpow, "drawable", getPackageName()));
					break;
				case QUERY_RSSI:
					boolean result = (Boolean) msg.obj;
					mSettingsCollection.setRssi(result);
					break;
				case QUERY_UTC:
					boolean utcEnabled = (Boolean) msg.obj;
					mSettingsCollection.setUtc(utcEnabled);
					break;
				case TIMER_RESPONSE:
					mOperationMenu.findItem(R.id.menu_tag_scan_detail_start)
							.setTitle(R.string.menu_tag_scan_detail_start_title);
					reading = false;
					break;
				case MENUITEM_UPDATE:
					int resId = (Integer) msg.obj;
					mOperationMenu.findItem(R.id.menu_tag_scan_detail_start).setTitle(resId);
					break;
				case EXIT:
					mIsExit = false;
					break;
				default:
					break;
			}
		};
	};

	/*
	 * ActionBarDrawerToggle配置
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	};

	/*
	 * ActionBarDrawerToggle配置
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		InvengoLog.i(TAG, "INFO.onResume()");
		initializeReaderStatus();
		initializeWidgetData();
		addListener();
		//		addReaderCallback();
		attemptQueryRssi();
		attemptQueryUtc();
		attemptHeartbeat();
	};

	private void initializeWidgetData() {
		reading = false;
		if(null != mOperationMenu){
			mOperationMenu.findItem(R.id.menu_tag_scan_detail_start).setTitle(R.string.menu_tag_scan_detail_start_title);
		}
	}

	private void attemptHeartbeat() {

	}

	//	private void addReaderCallback(){
	//		if(null != mReaderHolder.getCurrentReader()){
	//			mReaderHolder.getCurrentReader().onMessageNotificationReceived.add(this);//注册
	//		}
	//	}
	//
	//	private void removeReaderCallback(){
	//		if(null != mReaderHolder.getCurrentReader()){
	//			mReaderHolder.getCurrentReader().onMessageNotificationReceived.remove(this);//取消注册
	//		}
	//	}

	private void addListener() {
		//		mChannelSettingsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		//			@Override
		//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//				attemptSelectChannel();
		//			}
		//		});
		mRfidSettingsButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					attemptSelectChannel();
				}
			}
		});
		mBarcodeSettingsButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					attemptSelectChannel();
				}
			}
		});
		mDisconnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				attemptDisconnect();
			}
		});
		mExitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				attemptExit();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		InvengoLog.i(TAG, "INFO.onPause()");
		//		removeReaderCallback();
	}

	@Override
	protected void onDestroy() {
		InvengoLog.i(TAG, "INFO.onDestroy()");
		reading = false;
		TagScanSettingsCollection.clearSettings();
		DebugManager.clearSettings();
		//		removeReaderCallback();

		unregisterReaderConnectBroadcastReceiver();
		powT.interrupt();	// 关闭电量渐变线程

		InvengoLog.shutdown();
		super.cancelNotificationMessage();//暂时正常退出才能关闭Notification
		super.onDestroy();
	}

	/**
	 * XC2600关机指令
	 */
	private void shutdownReader() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					byte parameter = (byte) 0x80;
					byte[] data = new byte[]{0x01, 0x00};
					SysConfig_800 shutdownMsg = new SysConfig_800(parameter, data);
					//					mReaderHolder.sendMessage(shutdownMsg);
					InvengoLog.i(TAG, "INFO.Shutdown Reader {%s}" ,mReaderHolder.getCurrentReader().send(shutdownMsg));
				}
			}
		}).start();
	}

	private void showDeviceDialog(int messageId) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setNegativeButton(R.string.no_button,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(mReaderHolder.isConnected()){
							mReaderHolder.disConnect();
						}
						unregisterReaderConnectBroadcastReceiver();
						finish();
					}

				});
		alert.setPositiveButton(R.string.ok_button,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						unregisterReaderConnectBroadcastReceiver();
						shutdownReader();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(mReaderHolder.isConnected()){
							mReaderHolder.disConnect();
						}
						finish();
					}
				});
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setTitle(R.string.device_notice);
		alert.setMessage(messageId);
		alert.show();
	}

	//	@Override
	//	public void onBackPressed() {
	//		super.onBackPressed();
	//		InvengoLog.i(TAG, "INFO.onBackPressed()");
	//
	//		if(mDeviceType == DeviceType.XC2600){
	////			showDeviceDialog(R.string.message_shutdown_xc2600);
	//			//提示再次点击后退按钮将退出
	//		}else {
	//			finish();
	//		}
	//	}

	/**
	 * 注册蓝牙连接状态广播监听器以及读写器连接状态广播监听器
	 */
	private void registerReaderConnectBroadcastReceiver() {
		IntentFilter connectFilter = new IntentFilter(ReaderOperationTask.ACTION_TASK_BROADCAST);
		mBroadcastReceiver = new ReaderConnectBroadcastReceiver();
		registerReceiver(mBroadcastReceiver, connectFilter);
		IntentFilter bluetoothFilter = new IntentFilter(ACTION_CUSTOM_BLUETOOTH_BROADCAST);
		mCustomBluetoothBroadcastReceiver = new CustomBluetoothBroadcastReceiver();
		registerReceiver(mCustomBluetoothBroadcastReceiver, bluetoothFilter);
	}

	/**
	 * 取消注册蓝牙连接状态广播监听器以及读写器连接状态广播监听器
	 */
	private void unregisterReaderConnectBroadcastReceiver(){
		unregisterReceiver(mBroadcastReceiver);
		unregisterReceiver(mCustomBluetoothBroadcastReceiver);
	}

	private class ReaderConnectBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean success = intent.getBooleanExtra(ReaderOperationTask.RESULT, false);
			if (!success) {
				InvengoUtils.showToast(ReaderMainActivity.this, getString(R.string.toast_connect_failure));
			}
		}

	}

	private class CustomBluetoothBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String result = intent.getStringExtra(NAME);
//			Log.i(getLocalClassName(), "onReceive()." + result + this.hashCode());
//			if(BLUETOOTH_ON.equals(result)){
//				showToast(getString(R.string.toast_bluetooth_open));
//			}else if(BLUETOOTH_OFF.equals(result)){
//				showToast(getString(R.string.toast_bluetooth_close));
//			}else
			if (result.startsWith("pow_")) {
				flushPow(Integer.parseInt(result.substring(4)));
			} else if(BLUETOOTH_CONNECTED.equals(result)){
				Message connectMsg = new Message();
				connectMsg.what = CONNECTED;
				connectMsg.obj = true;
				readerHandler.sendMessage(connectMsg);
			}else if(BLUETOOTH_DISCONNECTED.equals(result)){
				InvengoUtils.showToast(ReaderMainActivity.this, getString(R.string.toast_bluetooth_disconnect));
				Message disconnectMsg = new Message();
				disconnectMsg.what = DISCONNECTED;
				readerHandler.sendMessage(disconnectMsg);
			}
		}

	}

	private class ReaderDisconnectTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			mReaderHolder.disConnect();
			try {
				Thread.sleep(45 * 100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			showProgress(false);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mConnectStatusView.setVisibility(View.VISIBLE);
			mConnectStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mConnectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});
			mConnectStatusView.setBackgroundColor(Color.parseColor("#86222222"));
		} else {
			mConnectStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	private class TagInfoArrayAdapter extends ArrayAdapter<TagScanInfoEntity>{

		private int resourceId;
		public TagInfoArrayAdapter(Context context, int textViewResourceId,
								   List<TagScanInfoEntity> objects) {
			super(context, textViewResourceId, objects);
			this.resourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TagScanInfoEntity entity = getItem(position);

			ViewHolder holder;
			if(null == convertView){
				convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
				holder = new ViewHolder();

				holder.typeView = (TextView) convertView.findViewById(R.id.text_tag_scan_type);
				holder.timesView = (TextView) convertView.findViewById(R.id.text_tag_scan_times);

				holder.epcLayout = (LinearLayout) convertView.findViewById(R.id.layout_epc);
				holder.tidLayout = (LinearLayout) convertView.findViewById(R.id.layout_tid);
				holder.userdataLayout = (LinearLayout) convertView.findViewById(R.id.layout_userdata);

				holder.epcView = (TextView) convertView.findViewById(R.id.text_tag_scan_epc);
				holder.tidView = (TextView) convertView.findViewById(R.id.text_tag_scan_tid);
				holder.userDataView = (TextView) convertView.findViewById(R.id.text_tag_scan_userdata);

				holder.barcodeLayout = (LinearLayout) convertView.findViewById(R.id.layout_barcode);
				holder.barcodeView = (TextView) convertView.findViewById(R.id.text_barcode_scan_barcode);

				holder.rssiLayout = (LinearLayout) convertView.findViewById(R.id.layout_tag_scan_rssi);
				holder.rssiView = (TextView) convertView.findViewById(R.id.text_tag_scan_rssi);

				holder.utcLayout = (LinearLayout) convertView.findViewById(R.id.layout_tag_scan_utc);
				holder.utcView = (TextView) convertView.findViewById(R.id.text_tag_scan_utc);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.typeView.setText(entity.getType());
			holder.timesView.setText(String.valueOf(entity.getNumber()));

			if(mReaderHolder.getChannelType() == ReaderHolder.RFID_CHANNEL_TYPE){
				holder.barcodeLayout.setVisibility(View.GONE);

				holder.epcView.setText(entity.getEpc());
				holder.tidView.setText(entity.getTid());
				holder.userDataView.setText(entity.getUserdata());
				holder.rssiView.setText(entity.getRssi());
				holder.utcView.setText(entity.getUtc());
				if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){
					if (mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {//userdata & epc & tid
						holder.epcLayout.setVisibility(mSettingsCollection.getUserdataChecked());
						holder.tidLayout.setVisibility(mSettingsCollection.getUserdataChecked());
						holder.userdataLayout.setVisibility(mSettingsCollection.getUserdataChecked());
						if(mSettingsCollection.isRssi()){//RSSI跟随EPC
							holder.rssiLayout.setVisibility(mSettingsCollection.getUserdataChecked());
						}else{
							holder.rssiLayout.setVisibility(View.GONE);
						}
						if(mSettingsCollection.isUtc()){
							holder.utcLayout.setVisibility(mSettingsCollection.getUserdataChecked());
						}else {
							holder.utcLayout.setVisibility(View.GONE);
						}
					}else{//epc | tid
						holder.epcLayout.setVisibility(mSettingsCollection.getEpcChecked());
						holder.tidLayout.setVisibility(mSettingsCollection.getTidChecked());
						holder.userdataLayout.setVisibility(mSettingsCollection.getUserdataChecked());
						if(mSettingsCollection.getEpcChecked() == Contants.CHECKED){//RSSI跟随EPC
							if(mSettingsCollection.isRssi()){
								holder.rssiLayout.setVisibility(mSettingsCollection.getEpcChecked());
							}else{
								holder.rssiLayout.setVisibility(View.GONE);
							}
							if(mSettingsCollection.isUtc()){
								holder.utcLayout.setVisibility(View.VISIBLE);
							}else {
								holder.utcLayout.setVisibility(View.GONE);
							}
						}else{//tid
							holder.rssiLayout.setVisibility(View.GONE);
							if(mSettingsCollection.isUtc()){
								holder.utcLayout.setVisibility(View.VISIBLE);
							}else {
								holder.utcLayout.setVisibility(View.GONE);
							}
							//							holder.utcLayout.setVisibility(View.GONE);
						}
					}
				}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){
					if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){//ID & Userdata
						holder.epcLayout.setVisibility(View.GONE);
						holder.tidLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
						holder.userdataLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
						//						if(mSettingsCollection.isRssi()){
						//							holder.rssiLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
						//						}else{
						//							holder.rssiLayout.setVisibility(View.GONE);
						//						}
						//						if(mSettingsCollection.isUtc()){
						//							holder.utcLayout.setVisibility(View.VISIBLE);
						//						}else{
						//							holder.utcLayout.setVisibility(View.GONE);
						//						}
						holder.rssiLayout.setVisibility(View.GONE);
						holder.utcLayout.setVisibility(View.GONE);
					}else{//ID
						holder.epcLayout.setVisibility(View.GONE);
						holder.tidLayout.setVisibility(mSettingsCollection.getId6BChecked());
						holder.userdataLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
						if(mSettingsCollection.isRssi()){
							holder.rssiLayout.setVisibility(mSettingsCollection.getId6BChecked());
						}else{
							holder.rssiLayout.setVisibility(View.GONE);
						}
						holder.utcLayout.setVisibility(View.GONE);
					}
				}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){
					holder.epcLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
					holder.tidLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
					holder.userdataLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
					if(mSettingsCollection.isRssi()){//RSSI跟随EPC
						holder.rssiLayout.setVisibility(mSettingsCollection.getUserdataChecked());
					}else{
						holder.rssiLayout.setVisibility(View.GONE);
					}
					if(mSettingsCollection.isUtc()){
						holder.utcLayout.setVisibility(View.VISIBLE);
					}else{
						holder.utcLayout.setVisibility(View.GONE);
					}
				}else if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){
					if(mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED){//inventory
						holder.epcLayout.setVisibility(mSettingsCollection.getVisibility_GB());
						holder.tidLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
						holder.userdataLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
						holder.rssiLayout.setVisibility(View.GONE);
						if(mSettingsCollection.isUtc()){
							holder.utcLayout.setVisibility(View.VISIBLE);
						}else{
							holder.utcLayout.setVisibility(View.GONE);
						}
					}else if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){//access read
						if(mSettingsCollection.isGbEpc()){
							holder.epcLayout.setVisibility(View.VISIBLE);
							holder.tidLayout.setVisibility(View.GONE);
							holder.userdataLayout.setVisibility(View.GONE);
						}else if(mSettingsCollection.isGbTid()){
							holder.epcLayout.setVisibility(View.GONE);
							holder.tidLayout.setVisibility(View.VISIBLE);
							holder.userdataLayout.setVisibility(View.GONE);
						}else if(mSettingsCollection.isGbUserdata()){
							holder.epcLayout.setVisibility(View.GONE);
							holder.tidLayout.setVisibility(View.GONE);
							holder.userdataLayout.setVisibility(View.VISIBLE);
						}
						holder.rssiLayout.setVisibility(View.GONE);
						if(mSettingsCollection.isUtc()){
							holder.utcLayout.setVisibility(View.VISIBLE);
						}else{
							holder.utcLayout.setVisibility(View.GONE);
						}
					}else if(mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED){//combination read
						holder.epcLayout.setVisibility(View.VISIBLE);
						holder.tidLayout.setVisibility(View.VISIBLE);
						holder.userdataLayout.setVisibility(View.GONE);
						holder.rssiLayout.setVisibility(View.GONE);
						if(mSettingsCollection.isUtc()){
							holder.utcLayout.setVisibility(View.VISIBLE);
						}else{
							holder.utcLayout.setVisibility(View.GONE);
						}
					}else if(mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED){//all read
						holder.epcLayout.setVisibility(View.VISIBLE);
						holder.tidLayout.setVisibility(View.VISIBLE);
						holder.userdataLayout.setVisibility(View.VISIBLE);
						holder.rssiLayout.setVisibility(View.GONE);
						if(mSettingsCollection.isUtc()){
							holder.utcLayout.setVisibility(View.VISIBLE);
						}else{
							holder.utcLayout.setVisibility(View.GONE);
						}
					}
				}
			}else if(mReaderHolder.getChannelType() == ReaderHolder.BARCODE_CHANNEL_TYPE){
				holder.epcLayout.setVisibility(View.GONE);
				holder.tidLayout.setVisibility(View.GONE);
				holder.userdataLayout.setVisibility(View.GONE);
				holder.rssiLayout.setVisibility(View.GONE);

				holder.barcodeLayout.setVisibility(View.VISIBLE);
				holder.barcodeView.setText(entity.getBarcode());
				holder.utcView.setText(entity.getUtc());
				if(mSettingsCollection.isUtc()){
					holder.utcLayout.setVisibility(View.VISIBLE);
				}else{
					holder.utcLayout.setVisibility(View.GONE);
				}
			}

			return convertView;
		}

		class ViewHolder{
			LinearLayout epcLayout;
			LinearLayout tidLayout;
			LinearLayout userdataLayout;
			LinearLayout barcodeLayout;
			LinearLayout rssiLayout;
			LinearLayout utcLayout;
			TextView typeView;
			TextView timesView;
			TextView epcView;
			TextView tidView;
			TextView userDataView;
			TextView barcodeView;
			TextView rssiView;
			TextView utcView;
		}
	}

	private void clearScanTag() {
		mList.clear();
		((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
	}

	private void stopScanTag(boolean immediately){
		reading = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					mReaderHolder.getCurrentReader().send(new PowerOff_800());
				}
			}
		}).start();
	}

	private void stopScanTag() {
		reading = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					mReaderHolder.getCurrentReader().send(new PowerOff_800());
					Message message = new Message();
					message.what = STOP;
					readerHandler.sendMessage(message);
				}
			}
		}).start();
	}

	/**
	 * 读标签
	 */
	private void startScanTag() {
		reading = true;
		//		String antennaEnabled = "1000" + (mSettingsCollection.isAntennaFour() ? "1" : "0")
		//				+ (mSettingsCollection.isAntennaThree() ? "1" : "0")
		//				+ (mSettingsCollection.isAntennaTwo() ? "1" : "0")
		//				+ (mSettingsCollection.isAntennaOne() ? "1" : "0");

		int antenna = mSettingsCollection.getAntenna();
		antenna = 0x01;
		int q = mSettingsCollection.getQ();
		InvengoLog.i(TAG, "INFO.startScanTag.Q - {%s}", q);
		//		boolean loop = mSettingsCollection.isLoop();

		ReadMemoryBank bank = null;

		if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){//6C
			if(mSettingsCollection.getEpcChecked() == Contants.CHECKED){
				bank = ReadMemoryBank.EPC_6C;//读EPC
			}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
				bank = ReadMemoryBank.TID_6C;//读TID
			}else if(mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
				bank = ReadMemoryBank.EPC_TID_UserData_6C_2;//通用读
			}
		}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){//6B
			if(mSettingsCollection.getId6BChecked() == Contants.CHECKED){
				bank = ReadMemoryBank.ID_6B;//读ID
			}else if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){
				bank = ReadMemoryBank.ID_UserData_6B;//读ID&Userdata
			}
		}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){//6C_6B
			bank = ReadMemoryBank.EPC_TID_UserData_Reserved_6C_ID_UserData_6B;//6C6B通用读
		}
		final ReadTag msg = new ReadTag(bank);

		msg.setAntenna((byte) antenna);
		if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){//6C
			msg.setQ((byte) q);
			//			msg.setLoop(loop);
			if(mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
				msg.setType((byte) 0x01);
				msg.setTidLen((byte) mSettingsCollection.getTidLen());
				msg.setUserDataPtr_6C(mSettingsCollection.getUserDataAddress());
				msg.setUserDataLen_6C((byte) mSettingsCollection.getUserDataLen());
			}
		}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){
			msg.setQ((byte) q);
			if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){
				//				msg.setLoop(loop);
				msg.setReadTimes_6C((byte) 0x00);
				msg.setTidLen((byte) mSettingsCollection.getTidLen6B());
				msg.setUserDataPtr_6C((byte) mSettingsCollection.getUserDataAddress6B());
				msg.setUserDataLen_6C((byte) mSettingsCollection.getUserDataLen6B());
				msg.setUserDataPtr_6B((byte) mSettingsCollection.getUserDataAddress6B());
				msg.setUserDataLen_6B((byte) mSettingsCollection.getUserDataLen6B());
			}else{
				msg.setAntenna((byte) 0x00);
			}
		}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){
			msg.setQ((byte) q);
			//			msg.setLoop(loop);
			msg.setTidLen((byte) mSettingsCollection.getTidLen6C6B());
			msg.setUserDataPtr_6C((byte) mSettingsCollection.getUserDataAddress6C6B());
			msg.setUserDataLen_6C((byte) mSettingsCollection.getUserDataLen6C6B());
			msg.setUserDataPtr_6B((byte) mSettingsCollection.getUserDataAddress6C6B());
			msg.setUserDataLen_6B((byte) mSettingsCollection.getUserDataLen6C6B());
		}
		//
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					Log.i(getLocalClassName(), mReaderHolder.getCurrentReader().toString());
					mReaderHolder.getCurrentReader().send(msg);
					Message message = new Message();
					message.what = START;
					message.obj = getString(R.string.toast_tag_scan_start);
					readerHandler.sendMessage(message);
				}
			}
		}).start();
	}

	/**
	 * 读条码
	 */
	private void startScanBarcode(){
		reading = true;

		final ReadBarcode msg = new ReadBarcode(new byte[]{0x00});

		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					startTask();

					mReaderHolder.getCurrentReader().send(msg);
					Message message = new Message();
					message.what = START;
					message.obj = getString(R.string.toast_barcode_scan_start);
					readerHandler.sendMessage(message);
				}
			}
		}).start();
	}

	private Timer mTimerScheduler = null;
	private class ReadBarcodeTimerTask extends TimerTask{
		@Override
		public void run() {
			Message stopMsg = new Message();
			stopMsg.what = TIMER_RESPONSE;
			readerHandler.sendMessage(stopMsg);
		}
	}
	//	private TimerTask mStopReadBarcodeTask = new TimerTask() {
	//
	//		@Override
	//		public void run() {
	//			Message stopMsg = new Message();
	//			stopMsg.what = TIMER_RESPONSE;
	//			readerHandler.sendMessage(stopMsg);
	//		}
	//	};

	private void startTask(){
		if(null == mTimerScheduler){
			mTimerScheduler = new Timer();
		}
		mTimerScheduler.schedule(new ReadBarcodeTimerTask(), 4 * 1000);
	}

	private void cancelTask(){
		if(null != mTimerScheduler){
			mTimerScheduler.cancel();
			mTimerScheduler.purge();
			mTimerScheduler = null;
		}
	}

	@Override
	public void handleNotificationMessage(BaseReader reader,
										  IMessageNotification msg) {
		super.handleNotificationMessage(reader, msg);

		if(mReaderHolder.isConnected()){
			if(msg instanceof RXD_TagData){//RFID tag
				if(reading){
					RXD_TagData data = (RXD_TagData) msg;
					if(null != data.getReceivedMessage()){
						playTagSound();
						//						InvengoLog.i(TAG, String.valueOf(((Reader)reader).isRssiEnable()));
						String type = data.getReceivedMessage().getTagType();
						String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
						String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
						String userData = Util.convertByteArrayToHexString(data.getReceivedMessage().getUserData());
						int rssi = 0;
						byte[] rssiData = data.getReceivedMessage().getRSSI();
						if(null != rssiData){
							rssi = rssiData[0] & 0xFF;
						}
						String utc = "1970-01-01 00:00:00";
						byte[] utcData = data.getReceivedMessage().getRXDTime();
						//						InvengoLog.i(TAG, "utcHexString {%s}", Util.convertByteArrayToHexString(utcData));
						if(null != utcData){
							utc = Util.getUtc(utcData);
						}

						Message notifyMessage = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("type", type);
						bundle.putString("epc", epc);
						bundle.putString("tid", tid);
						bundle.putString("userData", userData);
						bundle.putString("rssi", String.valueOf(rssi));
						bundle.putString("utc", utc);
						notifyMessage.setData(bundle);
						notifyMessage.what = RFID_REFRESH;
						readerHandler.sendMessage(notifyMessage);
					}
				}
			}else if(msg instanceof RXD_BARCODE){//barcode
				//					if(mSettingsCollection.isVoiced()){
				//						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
				//					}
				if(reading){
					RXD_BARCODE response = (RXD_BARCODE) msg;
					if(response.getReceivedMessage() != null){
						playTagSound();
						//						String barcode = Util.convertByteArrayToHexString(response.getReceivedMessage().getBarcodeData());
						String barcode = "";
						try {
							barcode = new String(response.getReceivedMessage().getBarcodeData(), "UTF-8");
							//							barcode = new String(response.getReceivedMessage().getBarcodeData(), "ASCII");
							//							barcode = new String(response.getReceivedMessage().getBarcodeData(), "GB2312");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						String utc = Util.getUtc(response.getReceivedMessage().getUtc());
						Message notifyMessage = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("barcode", barcode);
						bundle.putString("utc", utc);
						notifyMessage.setData(bundle);
						notifyMessage.what = BARCODE_REFRESH;
						readerHandler.sendMessage(notifyMessage);
					}
				}
			}else if(msg instanceof GBInventoryTag){
				if(reading){
					GBInventoryTag data = (GBInventoryTag) msg;
					if(null != data.getReceivedMessage()){
						playTagSound();
						String type = TAG_GB;
						String tagData = Util.convertByteArrayToHexString(data.getReceivedMessage().getTagData());
						String utc = "1970-01-01 00:00:00";
						byte[] utcData = data.getReceivedMessage().getUTC();
						//						InvengoLog.i(TAG, "utcHexString {%s}", Util.convertByteArrayToHexString(utcData));
						if(null != utcData){
							utc = Util.getUtc(utcData);
						}
						Message notifyMessage = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("type", type);
						bundle.putString("epc", tagData);
						bundle.putString("tid", "");
						bundle.putString("userData", "");
						bundle.putString("utc", utc);
						notifyMessage.setData(bundle);
						notifyMessage.what = RFID_REFRESH;
						readerHandler.sendMessage(notifyMessage);
					}
				}
			}else if(msg instanceof GBAccessReadTag){
				GBAccessReadTag data = (GBAccessReadTag) msg;
				if(null != data.getReceivedMessage()){
					playTagSound();
					String type = TAG_GB;
					String tagData = Util.convertByteArrayToHexString(data.getReceivedMessage().getTagData());
					String utc = "1970-01-01 00:00:00";
					byte[] utcData = data.getReceivedMessage().getUTC();
					if(null != utcData){
						utc = Util.getUtc(utcData);
					}
					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("utc", utc);
					if(mSettingsCollection.isGbEpc()){
						bundle.putString("epc", tagData);
						bundle.putString("tid", "");
						bundle.putString("userData", "");
					}else if(mSettingsCollection.isGbTid()){
						bundle.putString("epc", "");
						bundle.putString("tid", tagData);
						bundle.putString("userData", "");
					}else if(mSettingsCollection.isGbUserdata()){
						bundle.putString("epc", "");
						bundle.putString("tid", "");
						bundle.putString("userData", tagData);
					}
					notifyMessage.setData(bundle);
					notifyMessage.what = RFID_REFRESH;
					readerHandler.sendMessage(notifyMessage);
				}
			}else if(msg instanceof GBCombinationReadTag){
				GBCombinationReadTag data = (GBCombinationReadTag) msg;
				if(null != data.getReceivedMessage()){
					playTagSound();
					String type = TAG_GB;
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getGBEpcData());
					String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getGBTidData());
					String utc = "1970-01-01 00:00:00";
					byte[] utcData = data.getReceivedMessage().getUTC();
					if(null != utcData){
						utc = Util.getUtc(utcData);
					}
					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("utc", utc);
					bundle.putString("epc", epc);
					bundle.putString("tid", tid);
					bundle.putString("userData", "");
					notifyMessage.setData(bundle);
					notifyMessage.what = RFID_REFRESH;
					readerHandler.sendMessage(notifyMessage);
				}
			}else if(msg instanceof GBReadAllBank){
				GBReadAllBank data = (GBReadAllBank) msg;
				if(null != data.getReceivedMessage()){
					playTagSound();
					String type = TAG_GB;
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getGBEpcData());
					String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getGBTidData());
					String userdata = Util.convertByteArrayToHexString(data.getReceivedMessage().getGBUserdata());
					String utc = "1970-01-01 00:00:00";
					byte[] utcData = data.getReceivedMessage().getUTC();
					if(null != utcData){
						utc = Util.getUtc(utcData);
					}
					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("utc", utc);
					bundle.putString("epc", epc);
					bundle.putString("tid", tid);
					bundle.putString("userData", userdata);
					notifyMessage.setData(bundle);
					notifyMessage.what = RFID_REFRESH;
					readerHandler.sendMessage(notifyMessage);
				}
			}
			else if(msg instanceof RXD_ReaderTriggerStatus){
				attemptScan(false);
			}else if(msg instanceof Keepalive){
				Keepalive m = (Keepalive) msg;
				long time = System.currentTimeMillis();
				//				byte[] data = longToByte(time);
				byte[] data = new byte[8];
				data[0] = (byte) (time >> 56);
				data[1] = (byte) (time >> 48);
				data[2] = (byte) (time >> 40);
				data[3] = (byte) (time >> 32);
				data[4] = (byte) (time >> 24);
				data[5] = (byte) (time >> 16);
				data[6] = (byte) (time >> 8);
				data[7] = (byte) (time & 0xFF);

				Keepalive keepalive = new Keepalive(m.getReceivedMessage().getSequence(), data);
				executeHeartbeat(keepalive);
			}
		}
	}

	//	@Override
	//	public void messageNotificationReceivedHandle(BaseReader reader,
	//			IMessageNotification msg) {
	//		if(reading){
	//			if(mReaderHolder.isConnected()){
	//
	////				Log.i(getLocalClassName(), "Message incoming." + msg.getMessageType());
	//				if(msg instanceof RXD_TagData){//RFID tag
	////					android.util.Log.i("TagScanActivity", Util.convertByteArrayToHexString(((RXD_TagData)msg).getReceivedMessage().getEPC()));
	////					if(mSettingsCollection.isVoiced()){
	////						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
	////					}
	//					RXD_TagData data = (RXD_TagData) msg;
	//					if(null != data.getReceivedMessage()){
	//						playTagSound();
	//						String type = data.getReceivedMessage().getTagType();
	//						String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
	//						String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
	//						String userData = Util.convertByteArrayToHexString(data.getReceivedMessage().getUserData());
	//						int rssi = 0;
	//						byte[] rssiData = data.getReceivedMessage().getRSSI();
	//						if(null != rssiData){
	//							rssi = rssiData[0] & 0xFF;
	//						}
	//						String utc = "1970-01-01 00:00:00";
	//						byte[] utcData = data.getReceivedMessage().getRXDTime();
	//						InvengoLog.i(TAG, "utcHexString {%s}", Util.convertByteArrayToHexString(utcData));
	//						if(null != utcData){
	//							utc = Util.getUtc(utcData);
	//						}
	//
	//						Message notifyMessage = new Message();
	//						Bundle bundle = new Bundle();
	//						bundle.putString("type", type);
	//						bundle.putString("epc", epc);
	//						bundle.putString("tid", tid);
	//						bundle.putString("userData", userData);
	//						bundle.putString("rssi", String.valueOf(rssi));
	//						bundle.putString("utc", utc);
	//						notifyMessage.setData(bundle);
	//						notifyMessage.what = RFID_REFRESH;
	//						readerHandler.sendMessage(notifyMessage);
	//					}
	//				}else if(msg instanceof RXD_BARCODE){//barcode
	////					if(mSettingsCollection.isVoiced()){
	////						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
	////					}
	//					RXD_BARCODE response = (RXD_BARCODE) msg;
	//					if(response.getReceivedMessage() != null){
	//						playTagSound();
	////						String barcode = Util.convertByteArrayToHexString(response.getReceivedMessage().getBarcodeData());
	//						String barcode = "";
	//						try {
	//							barcode = new String(response.getReceivedMessage().getBarcodeData(), "UTF-8");
	//							barcode = new String(response.getReceivedMessage().getBarcodeData(), "ASCII");
	//						} catch (UnsupportedEncodingException e) {
	//							e.printStackTrace();
	//						}
	//						String utc = Util.getUtc(response.getReceivedMessage().getUtc());
	//						Message notifyMessage = new Message();
	//						Bundle bundle = new Bundle();
	//						bundle.putString("barcode", barcode);
	//						bundle.putString("utc", utc);
	//						notifyMessage.setData(bundle);
	//						notifyMessage.what = BARCODE_REFRESH;
	//						readerHandler.sendMessage(notifyMessage);
	//					}
	//				}else if(msg instanceof GBInventoryTag){
	//					GBInventoryTag data = (GBInventoryTag) msg;
	//					if(null != data.getReceivedMessage()){
	//						playTagSound();
	//						String type = TAG_GB;
	//						String tagData = Util.convertByteArrayToHexString(data.getReceivedMessage().getTagData());
	//						Message notifyMessage = new Message();
	//						Bundle bundle = new Bundle();
	//						bundle.putString("type", type);
	//						bundle.putString("epc", tagData);
	//						bundle.putString("tid", "");
	//						bundle.putString("userData", "");
	//						bundle.putString("rssi", "");
	//						notifyMessage.setData(bundle);
	//						notifyMessage.what = RFID_REFRESH;
	//						readerHandler.sendMessage(notifyMessage);
	//					}
	//				}
	//				else if(msg instanceof Keepalive){
	//					Keepalive m = (Keepalive) msg;
	//					long time = System.currentTimeMillis();
	////					byte[] data = longToByte(time);
	//					byte[] data = new byte[8];
	//					data[0] = (byte) (time >> 56);
	//					data[1] = (byte) (time >> 48);
	//					data[2] = (byte) (time >> 40);
	//					data[3] = (byte) (time >> 32);
	//					data[4] = (byte) (time >> 24);
	//					data[5] = (byte) (time >> 16);
	//					data[6] = (byte) (time >> 8);
	//					data[7] = (byte) (time & 0xFF);
	//
	//					Keepalive keepalive = new Keepalive(m.getReceivedMessage().getSequence(), data);
	//					executeHeartbeat(keepalive);
	//				}
	//			}
	//		}
	//	}

	private void executeHeartbeat(final Keepalive msg){
		mExecutorService.execute(new Runnable() {

			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					mReaderHolder.getCurrentReader().send(msg);
				}
			}
		});
	}

	public byte[] longToByte(long number) {
		long temp = number;
		byte[] b = new byte[8];
		for (int i = b.length - 1; i >= 0 ; i--) {
			b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		InvengoLog.i(TAG, "Drawer status - {%s}  RepeatCount - {%s} onKeyDow", DRAWER_OPEN_CLOSE_STATUS, event.getRepeatCount());
		InvengoLog.e (TAG, "INFO.onKeyDow(%s, %s)", keyCode, event.getAction());
		if ((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KEYCODE_TRIGGER
				|| keyCode == KEYCODE_SIDE_TRIGGER)
				&& event.getRepeatCount() <= 0
				&& mReaderHolder.isConnected()
				&& !DRAWER_OPEN_CLOSE_STATUS
				&& null != this.mOperationMenu
				&& !mContinual) {
			attemptScan(true);
			//			if(reading == false){
			//				startScanTag();
			//				this.mOperationMenu.findItem(R.id.menu_tag_scan_detail_start)
			//						.setTitle(R.string.menu_tag_scan_detail_stop_title);
			//			}else if(reading == true){
			//				stopScanTag();
			//				this.mOperationMenu.findItem(R.id.menu_tag_scan_detail_start)
			//						.setTitle(R.string.menu_tag_scan_detail_start_title);
			//			}

			InvengoLog.i(TAG, "Drawer status - {%s}  RepeatCount - {%s} attemptScan", DRAWER_OPEN_CLOSE_STATUS, event.getRepeatCount());
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_BACK){//1秒内连按两次后退键将退出扫描界面
			if(!mIsExit){
				mIsExit = true;
				InvengoUtils.showToast(this, R.string.toast_exit);
				Message msg = new Message();
				msg.what = EXIT;
				readerHandler.sendMessageDelayed(msg, 1 * 1000);
			}else{
				attemptExit();
			}
			return false;
		}else if((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KEYCODE_TRIGGER
				|| keyCode == KEYCODE_SIDE_TRIGGER)
				&& event.getRepeatCount() >= 10
				&& mReaderHolder.isConnected()
				&& !DRAWER_OPEN_CLOSE_STATUS
				&& null != this.mOperationMenu
				&& !mContinual){//repeatCount >= 10后默认为长按扳机键
			InvengoLog.i(TAG, "mContinual true");
			mContinual = true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KEYCODE_TRIGGER
				|| keyCode == KEYCODE_SIDE_TRIGGER)
				&& event.getRepeatCount() <= 0
				&& mReaderHolder.isConnected()
				&& !DRAWER_OPEN_CLOSE_STATUS
				&& null != this.mOperationMenu
				&& !mContinual) {
			//			InvengoLog.i(TAG, "INFO.onKeyUp(%s, %s)", keyCode, event.getAction());
			InvengoLog.i(TAG, "Drawer status - {%s}  RepeatCount - {%s} onKeyUp", DRAWER_OPEN_CLOSE_STATUS, event.getRepeatCount());
			return true;
		}else if((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KEYCODE_TRIGGER
				|| keyCode == KEYCODE_SIDE_TRIGGER)
				&& event.getRepeatCount() <= 0
				&& mReaderHolder.isConnected()
				&& !DRAWER_OPEN_CLOSE_STATUS
				&& null != this.mOperationMenu
				&& mContinual){
			InvengoLog.i(TAG, "mContinual false");
			mContinual = false;
			attemptScan(true);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void playTagSound(){
		if(mSettingsCollection.isVoiced()){
			mVoiceManager.playSound(Contants.TAG_SOUND, Contants.SOUND_NO_LOOP_MODE);
		}
	}

	private void playBeep(){
		if(mSettingsCollection.isVoiced()){
			mVoiceManager.playSound(Contants.BEEP_SOUND, Contants.SOUND_NO_LOOP_MODE);
		}
	}

	private void attemptQueryRssi(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				InvengoLog.d(TAG, "INFO.Query Rssi.");
				byte parameter = 0x14;
				SysQuery_800 msg = new SysQuery_800(parameter);
				msg.setTimeOut(2 * 1000);
				if(mReaderHolder.isConnected()){
					boolean success = mReaderHolder.getCurrentReader().send(msg);
					if(success){
						SysQuery800ReceivedInfo response = msg.getReceivedMessage();
						if(null != response){
							byte[] data = response.getQueryData();
							InvengoLog.i(TAG, "INFO.Query Rssi Value{%s}", Util.convertByteArrayToHexString(data));
							int rssi = data[0] & 0xFF;
							Message message = new Message();
							message.what = QUERY_RSSI;
							message.obj = (rssi == 1) ? true : false;
							readerHandler.sendMessage(message);
						}
					}else{
						InvengoLog.w(TAG, "WARN.Query Rssi Failure");
					}
				}else{
					InvengoLog.w(TAG, "WARN.Query Rssi-Reader Disconnect");
				}
			}
		}).start();
	}

	private void attemptQueryUtc(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				InvengoLog.d(TAG, "INFO.Query Utc.");
				byte parameter = 0x18;
				SysQuery_800 msg = new SysQuery_800(parameter);
				msg.setTimeOut(2 * 1000);
				if(mReaderHolder.isConnected()){
					boolean success = mReaderHolder.getCurrentReader().send(msg);
					if(success){
						SysQuery800ReceivedInfo response = msg.getReceivedMessage();
						if(null != response){
							byte[] data = response.getQueryData();
							InvengoLog.i(TAG, "INFO.Query Utc Value{%s}", Util.convertByteArrayToHexString(data));
							int utc = data[0] & 0xFF;
							Message message = new Message();
							message.what = QUERY_UTC;
							message.obj = (utc == 1) ? true : false;
							readerHandler.sendMessage(message);
						}
					}else{
						InvengoLog.w(TAG, "WARN.Query Utc Failure");
					}
				}else{
					InvengoLog.w(TAG, "WARN.Query Utc-Reader Disconnect");
				}
			}
		}).start();
	}

	// 刷新电量
	private void flushPow (int p) {
		devpowTv = p;
//Log.i("-----", p + ",");
		if (devpowWait > 0) {
			devpow = p;
			flushPow ();
		}
	}
	private void flushPow () {
		if (devpow > -1) {
			readerHandler.sendMessage(readerHandler.obtainMessage(DEVPOW));
		}
	}

	// 逐步变化的电量线程
	private Thread powT = new Thread() {
		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					Thread.sleep(3000);
//Log.i("--- powT ---", devpow + " , " + reading + " , " + devpowTv + " , " + devpowWait);
					if (devpowWait > 0) {
						devpowWait --;
					} else if (!reading && devpow != devpowTv) {
						if (devpow > devpowTv) {
							devpow --;
						} else if (devpow < devpowTv) {
							devpow ++;
						}
						flushPow ();
					}
				}
			} catch (Exception e) {}
		}
	};
}
