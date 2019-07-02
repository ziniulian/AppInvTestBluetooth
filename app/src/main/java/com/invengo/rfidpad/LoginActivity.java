package com.invengo.rfidpad;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.protocol.IRP1.Reader;

import java.util.Iterator;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.invengo.rfidpad.base.AbstractBaseActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.base.ReaderMainActivity;
import com.invengo.rfidpad.base.ReaderOperationTask;
import com.invengo.rfidpad.utils.InvengoUtils;

import static android.content.Context.MODE_PRIVATE;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AbstractBaseActivity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private ReaderOperationTask mAuthTask = null;
	private static final String TAG = LoginActivity.class.getSimpleName();

	// UI references.
	private Spinner mDeviceSpinner;
	private EditText mDeviceName;
	private static final int SPINNER_STYLE = android.R.layout.simple_spinner_dropdown_item;
	private Button mConnectButton;
	private Button mQuitButton;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private String mReaderName;
	private String mConnectAddress;
//	private CheckBox mChannelCheckBox;
	private RadioButton mRfidButton;
	private RadioButton mBarcodeButton;

	private BluetoothAdapter mAdapter;
	private ReaderHolder mReaderHolder;
	private SharedPreferences mSharedPreferences;
	private static final String SHARED_PREFERENCES_FILE_NAME = "RFID.PAD";
	private ReaderConnectBroadcastReceiver mBroadcastReceiver;
	private CustomBluetoothBroadcastReceiver mCustomBluetoothBroadcastReceiver;

	private static final byte PARAMETER_RFID = (byte) 0x85;
	private static final byte PARAMETER_1D2D = (byte) 0x84;
	private static final byte[] DATA = new byte[]{0x01, 0x01};
	
//	private ListView mBleReaderListView;
//	private ArrayList<BleDeviceEntity> mList = new ArrayList<BleDeviceEntity>();
//	private static final long DELAY_MILLIS = 10 * 1000;
//	private BleDeviceEntity mCurrentBleDevice = null;
//	private int mLastPosition = -1;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setTitle(R.string.app_name_XC2600);
		
		mReaderHolder = ReaderHolder.getInstance();
		mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
		// Set up the connect form.
		mDeviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
//		mBleReaderListView = (ListView) findViewById(R.id.list_reader_ble_id);
//		BleDeviceArrayAdapter adapter = new BleDeviceArrayAdapter(this, R.layout.list_reader_frequency_detail, mList);
//		mBleReaderListView.setAdapter(adapter);
		
		mDeviceName = (EditText) findViewById(R.id.deviceName);
		mConnectButton = (Button) findViewById(R.id.connect_button);
		mQuitButton = (Button) findViewById(R.id.quite_button);
//		mChannelCheckBox = (CheckBox) findViewById(R.id.check_rfid_1d2d);
		mRfidButton = (RadioButton) findViewById(R.id.radio_rfid_id);
		mBarcodeButton = (RadioButton) findViewById(R.id.radio_1d2d_id);
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = bluetoothManager.getAdapter();
		if(!mAdapter.isEnabled()){
			openBluetooth();
		}
		
		mConnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mAdapter.isEnabled()) {
					InvengoUtils.showToast(LoginActivity.this, getString(R.string.toast_bluetooth_close));
					return;
				}
				
//				if(mCurrentBleDevice == null){
//					InvengoUtils.showToast(LoginActivity.this, getString(R.string.toast_bluetooth_bound));
//					return;
//				}
				
				if(TextUtils.isEmpty(mDeviceName.getText().toString())){
					InvengoUtils.showToast(LoginActivity.this, getString(R.string.toast_name_empty));
					return;
				}
				
				attemptConnect();
			}
		});
		mQuitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				attemptQuit();
			}
		});
//		mChannelCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				if(isChecked){
//					mChannelCheckBox.setText(R.string.check_rfid_label);
//				}else{
//					mChannelCheckBox.setText(R.string.check_1d2d_label);
//				}
//			}
//		});
		
//		mBleReaderListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////				if(mLastPosition == position){
////					return;
////				}
////				mCurrentBleDevice = ((BleDeviceArrayAdapter)mBleReaderListView.getAdapter()).getItem(position);
////				mCurrentBleDevice.setCheck(true);
////				if(mLastPosition != -1){
////					BleDeviceEntity lastDevice = ((BleDeviceArrayAdapter)mBleReaderListView.getAdapter()).getItem(position);
////					lastDevice.setCheck(false);
////				}
////				mLastPosition = position;
//				
//				BleDeviceArrayAdapter.ViewHolder viewHolder = (BleDeviceArrayAdapter.ViewHolder) view.getTag();
//				viewHolder.checkView.toggle();
//			}
//		});
	}
	
	private void registerReaderConnectBroadcastReceiver() {
		IntentFilter connectFilter = new IntentFilter(ReaderOperationTask.ACTION_TASK_BROADCAST);
		mBroadcastReceiver = new ReaderConnectBroadcastReceiver();
		registerReceiver(mBroadcastReceiver, connectFilter);
		IntentFilter bluetoothFilter = new IntentFilter(ACTION_CUSTOM_BLUETOOTH_BROADCAST);
		mCustomBluetoothBroadcastReceiver = new CustomBluetoothBroadcastReceiver();
		registerReceiver(mCustomBluetoothBroadcastReceiver, bluetoothFilter);
	}
	
	private void unregisterReaderConnectBroadcastReceiver(){
		unregisterReceiver(mBroadcastReceiver);
		unregisterReceiver(mCustomBluetoothBroadcastReceiver);
	}

	
	private void initBleDevice() {
		Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		spinnerArrayAdapter.setDropDownViewResource(SPINNER_STYLE);
		int selectedPosition = 0;
		String deviceName = "reader1";
		if(devices.isEmpty()){//no-bonded devices
			spinnerArrayAdapter.add(getString(R.string.device_not_found));
			InvengoUtils.showToast(this, getString(R.string.toast_bluetooth_close));
		}else{//
			Iterator<BluetoothDevice> deviceIterator = devices.iterator();
			String deviceNo = mSharedPreferences.getString("device", "");
			deviceName = mSharedPreferences.getString("reader", "reader1");
			while (deviceIterator.hasNext()) {
				BluetoothDevice device = deviceIterator.next();
				String deviceInfo = device.getName() + "|" + device.getAddress();
				spinnerArrayAdapter.add(deviceInfo);
				if(deviceNo.equals(deviceInfo)){
					selectedPosition = spinnerArrayAdapter.getPosition(deviceInfo);
				}
			}
		}
		mDeviceSpinner.setSelection(selectedPosition);
		mDeviceSpinner.setAdapter(spinnerArrayAdapter);
		mDeviceName.setText(deviceName);
	}
	
//	private void startScanBleDevice(boolean enable){
//		if(enable){
//			mLoginHandle.postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					mAdapter.stopLeScan(mLeScanCallback);
//				}
//			}, DELAY_MILLIS);
//			clear();
//			mAdapter.startLeScan(mLeScanCallback);
//		}else{
//			mAdapter.stopLeScan(mLeScanCallback);
//		}
//	}
	
//	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//		
//		@Override
//		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					String name = device.getName();
//					String address = device.getAddress();
//					if(!TextUtils.isEmpty(address) && !TextUtils.isEmpty(name)){
//						BleDeviceEntity newDevice = new BleDeviceEntity();
//						newDevice.setAddress(address);
//						newDevice.setReaderName(name);
//						newDevice.setRssi(rssi);
//						addDevice(newDevice);
//					}
//				}
//			});
//		}
//	};
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReaderConnectBroadcastReceiver();
		initBleDevice();
//		startScanBleDevice(true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReaderConnectBroadcastReceiver();
//		startScanBleDevice(false);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void openBluetooth() {
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.myspinner);
		spinnerArrayAdapter.setDropDownViewResource(SPINNER_STYLE);
		spinnerArrayAdapter.add(getString(R.string.device_not_found));
		mDeviceSpinner.setAdapter(spinnerArrayAdapter);
		new AlertDialog.Builder(this)
				.setTitle(R.string.alert_dialog_title)
				.setMessage(R.string.alert_dialog_message)
				.setPositiveButton(R.string.alert_dialog_button_yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent openBluetoothIntent = new Intent(
										Settings.ACTION_BLUETOOTH_SETTINGS);
								startActivity(openBluetoothIntent);
							}
						})
				.setNegativeButton(R.string.alert_dialog_button_NO,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
	}

	private static final int BLUETOOTH_CLOSED = 1;
	private static final int BLUETOOTH_OPEN = 2;
	private static final int ACTIVITY_START = 3;
	private static final int CONNECT_FAILURE = 4;
	@SuppressLint("HandlerLeak")
	
	private Handler mLoginHandle = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case BLUETOOTH_OPEN:
				initBleDevice();
//				startScanBleDevice(true);
				break;
			case BLUETOOTH_CLOSED:
				initBleDevice();
//				startScanBleDevice(true);
				break;
			case ACTIVITY_START:
//				showProgress(false);
				boolean success = (Boolean) msg.obj;
				if(success){
					Editor editor = mSharedPreferences.edit();
					editor.putString("device", (String) mDeviceSpinner.getSelectedItem());
					editor.putString("reader", mDeviceName.getText().toString().trim());
					editor.commit();
					
					Log.i(getLocalClassName(), "activity.start");
					mReaderHolder.setConnected(success);
					if(mRfidButton.isChecked()){
						mReaderHolder.setChannelType(ReaderHolder.RFID_CHANNEL_TYPE);
					}else if(mBarcodeButton.isChecked()){
						mReaderHolder.setChannelType(ReaderHolder.BARCODE_CHANNEL_TYPE);
					}
					Intent newIntent = new Intent(LoginActivity.this, ReaderMainActivity.class);
					startActivity(newIntent);
				}
//				else{
//					InvengoUtils.showToast(LoginActivity.this, getString(R.string.toast_channel_open_failure_connect_failure));
//					mReaderHolder.disConnect();
//					mReaderHolder.disposeReader();
//				}
				break;
			case CONNECT_FAILURE:
//				showProgress(false);
				
				mReaderHolder.setConnected(false);
				mReaderHolder.disposeReader();
				InvengoUtils.showToast(LoginActivity.this, R.string.toast_bluetooth_disconnect);
				break;
			default:
				break;
			}
		};
	};
	
	public void attemptQuit(){
		finish();
	}
	
//	protected void attemptSelectChannel() {
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				SysConfig_800 message = null;
//				if(mChannelCheckBox.isChecked()){
//					message = new SysConfig_800(PARAMETER_RFID, DATA);
//				}else {
//					message = new SysConfig_800(PARAMETER_1D2D, DATA);
//				}
//				boolean success = mReaderHolder.getCurrentReader().send(message);
//				Message msg = new Message();
//				msg.what = ACTIVITY_START;
//				msg.obj = success;
////				msg.obj = true;
//				mLoginHandle.sendMessage(msg);
//			}
//		}).start();
//	}

	/**
	 * Attempts to connect reader by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptConnect() {
		if (mAuthTask != null) {
			return;
		}

		mReaderName = mDeviceName.getText().toString().trim();
		mConnectAddress = ((String) mDeviceSpinner.getSelectedItem()).split("\\|")[1];
//		mConnectAddress = this.mCurrentBleDevice.getAddress();
		Reader mNewReader = mReaderHolder.createBluetoothReader(mReaderName, mConnectAddress);
//		Reader mBleReader = new Reader(mReaderName, "BluetoothLE", mConnectAddress, this);
//		Reader mBleReader = new Reader(mReaderName, "BluetoothLET", mConnectAddress, this, mChannelCheckBox.isChecked() ? BaseReader.ReaderChannelType.RFID_CHANNEL_TYPE : BaseReader.ReaderChannelType.BARCODE_CHANNEL_TYPE);
		mNewReader.setChannelType(mRfidButton.isChecked() ? BaseReader.ReaderChannelType.RFID_CHANNEL_TYPE : BaseReader.ReaderChannelType.BARCODE_CHANNEL_TYPE);
		mReaderHolder.setCurrentReader(mNewReader);
		mReaderHolder.setReaderName(mReaderName);
		mReaderHolder.setDeviceName(mConnectAddress);
		
		// Show a progress bar, and kick off a background task to
		// perform the reader connect attempt.
		mLoginStatusMessageView.setText(R.string.progress_bar_hint);
		showProgress(true);
		mAuthTask = new ReaderOperationTask(this);
		mAuthTask.execute(mNewReader);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private class CustomBluetoothBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String result = intent.getStringExtra(NAME);
			Log.i(getLocalClassName(), "onReceive()." + result);
			if(BLUETOOTH_ON.equals(result)){
				Message openMsg = new Message();
				openMsg.what = BLUETOOTH_OPEN;
				mLoginHandle.sendMessage(openMsg);
			}else if(BLUETOOTH_OFF.equals(result)){
				Message closeMsg = new Message();
				closeMsg.what = BLUETOOTH_CLOSED;
				mLoginHandle.sendMessage(closeMsg);
			}else if(BLUETOOTH_CONNECTED.equals(result)){
				mReaderHolder.setConnected(true);
//				Message msg = new Message();
//				msg.what = ACTIVITY_START;
//				msg.obj = true;
//				mLoginHandle.sendMessage(msg);
				
//				attemptSelectChannel();
			}else if(BLUETOOTH_DISCONNECTED.equals(result)){
				Message disconnectMsg = new Message();
				disconnectMsg.what = CONNECT_FAILURE;
				mLoginHandle.sendMessage(disconnectMsg);
			}
		}
	}
	
	private class ReaderConnectBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			mAuthTask = null;
			showProgress(false);

			boolean success = intent.getBooleanExtra(ReaderOperationTask.RESULT, false);
			if (success) {
//				attemptSelectChannel();
				Message msg = new Message();
				msg.what = ACTIVITY_START;
				msg.obj = true;
				mLoginHandle.sendMessage(msg);
			}else{
				InvengoUtils.showToast(LoginActivity.this, getString(R.string.toast_connect_failure));
			}
		}
		
	}
	
//	private class BleDeviceEntity{
//		private boolean check = false;
//		private int rssi;
//		private String readerName;
//		private String address;
//		
//		public boolean isCheck() {
//			return check;
//		}
//		public void setCheck(boolean check) {
//			this.check = check;
//		}
//		public int getRssi() {
//			return rssi;
//		}
//		public void setRssi(int rssi) {
//			this.rssi = rssi;
//		}
//		public String getReaderName() {
//			return readerName;
//		}
//		public void setReaderName(String readerName) {
//			this.readerName = readerName;
//		}
//		public String getAddress() {
//			return address;
//		}
//		public void setAddress(String address) {
//			this.address = address;
//		}
//		
//		@Override
//		public boolean equals(Object o) {
//			if(o == this){
//				return true;
//			}
//			
//			if(!(o instanceof BleDeviceEntity)){
//				return false;
//			}
//			
//			BleDeviceEntity entity = (BleDeviceEntity) o;
//			
//			return (entity.getAddress()).equals(this.address);
//		}
//		
//		@Override
//		public int hashCode() {
//			int result = 17;
//			result = result * 31 + this.address.hashCode();
//			result = result * 31 + this.readerName.hashCode();
//			result = result * 31 + this.rssi;
//			return result;
//		}
//	}
	
//	private void clear(){
//		mList.clear();
//		((BleDeviceArrayAdapter)mBleReaderListView.getAdapter()).notifyDataSetChanged();
//	}
	
//	private void addDevice(BleDeviceEntity device){
//		if(!mList.contains(device)){
//			mList.add(device);
//			((BleDeviceArrayAdapter)mBleReaderListView.getAdapter()).notifyDataSetChanged();
//		}
//	}
	
//	private class BleDeviceArrayAdapter extends ArrayAdapter<BleDeviceEntity>{
//
//		private int tempId = -1;
//		private int resourceId;
//		private Activity context;
//		public BleDeviceArrayAdapter(Activity context, int textViewResourceId,
//				ArrayList<BleDeviceEntity> objects) {
//			super(context, textViewResourceId, objects);
//			this.resourceId = textViewResourceId;
//			this.context = context;
//		}
//		
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			BleDeviceEntity entity = getItem(position);
//			
//			ViewHolder holder;
//			if(null == convertView){
//				convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
//				holder = new ViewHolder();
//				
//				holder.checkView = (CheckBox) convertView.findViewById(R.id.check_reader_frequency_id);
//				holder.nameView = (TextView) convertView.findViewById(R.id.text_reader_frequency_id);
//				
//				convertView.setTag(holder);
//			}else{
//				holder = (ViewHolder) convertView.getTag();
//			}
//			
//			holder.nameView.setText(entity.getReaderName());
//			holder.checkView.setId(position);
//			holder.checkView.setChecked(entity.isCheck());
//			holder.checkView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				@Override
//				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//					int currentId = buttonView.getId();
//					BleDeviceEntity currentEntity = BleDeviceArrayAdapter.this.getItem(currentId);
//					if(isChecked){
//						InvengoLog.i(TAG, "INFO.Before tempId {%s} & BLE MAC {%s}", String.valueOf(tempId), currentEntity.getReaderName());
//						if(tempId != -1){
//							CheckBox tempCheckBox = (CheckBox) context.findViewById(tempId);
//							if(null != tempCheckBox){
//								tempCheckBox.setChecked(!isChecked);
//							}
//						}
//						tempId = buttonView.getId();
//						currentEntity.setCheck(true);
//						mCurrentBleDevice = currentEntity;
//						InvengoLog.i(TAG, "INFO.After tempId {%s}", String.valueOf(tempId));
//					}
//					else {
//						tempId = -1;
//						currentEntity.setCheck(false);
//						mCurrentBleDevice = null;
//					}
//				}
//			});
//			
//			return convertView;
//		}
//		
//		class ViewHolder{
//			CheckBox checkView;
//			TextView nameView;
//		}
//	}
}
