package com.invengo.rfidpad;

import java.util.ArrayList;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.base.AbstractBaseActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.base.ReaderMainActivity;
import com.invengo.rfidpad.base.ReaderOperationTask;
import com.invengo.rfidpad.entity.BleDeviceEntity;
import com.invengo.rfidpad.utils.InvengoUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import invengo.javaapi.protocol.IRP1.Reader;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginBLEActivity extends AbstractBaseActivity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private ReaderOperationTask mAuthTask = null;
	private static final String TAG = LoginBLEActivity.class.getSimpleName();

	// UI references.
	private static final int SPINNER_STYLE = android.R.layout.simple_spinner_dropdown_item;
	private Button mConnectButton;
	private Button mQuitButton;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private String mReaderName;
	private String mConnectAddress;

	private BluetoothAdapter mAdapter;
	private ReaderHolder mReaderHolder;
	private SharedPreferences mSharedPreferences;
	private static final String SHARED_PREFERENCES_FILE_NAME = "RFID.PAD";
	private ReaderConnectBroadcastReceiver mBroadcastReceiver;
	private CustomBluetoothBroadcastReceiver mCustomBluetoothBroadcastReceiver;
	
	private ListView mBleReaderListView;
	private ArrayList<BleDeviceEntity> mList = new ArrayList<BleDeviceEntity>();
	private static final long DELAY_MILLIS = 10 * 1000;
	private BleDeviceEntity mCurrentBleDevice = null;
	private int mLastPosition = -1;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_ble);
		setTitle(R.string.app_name_XC2600);
		
		mReaderHolder = ReaderHolder.getInstance();
		mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
		// Set up the connect form.
		mBleReaderListView = (ListView) findViewById(R.id.list_reader_ble_id);
		BleDeviceArrayAdapter adapter = new BleDeviceArrayAdapter(this, R.layout.list_reader_frequency_detail, mList);
		mBleReaderListView.setAdapter(adapter);
		
		mConnectButton = (Button) findViewById(R.id.connect_button);
		mQuitButton = (Button) findViewById(R.id.quite_button);
		
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
					InvengoUtils.showToast(LoginBLEActivity.this, getString(R.string.toast_bluetooth_close));
					return;
				}
				
				if(mCurrentBleDevice == null){
					InvengoUtils.showToast(LoginBLEActivity.this, getString(R.string.toast_bluetooth_bound));
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

	private void startScanBleDevice(boolean enable){
		if(enable){
			mLoginHandle.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mAdapter.stopLeScan(mLeScanCallback);
				}
			}, DELAY_MILLIS);
			clear();
			mAdapter.startLeScan(mLeScanCallback);
		}else{
			mAdapter.stopLeScan(mLeScanCallback);
		}
	}
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String name = device.getName();
					String address = device.getAddress();
					if(!TextUtils.isEmpty(address) && !TextUtils.isEmpty(name)){
						BleDeviceEntity newDevice = new BleDeviceEntity();
						newDevice.setAddress(address);
						newDevice.setReaderName(name);
						newDevice.setRssi(rssi);
						if(null != mSavedInstanceState) {
							BleDeviceEntity entity = (BleDeviceEntity) mSavedInstanceState.getSerializable(STATE_KEY);
							if(entity.getAddress().equals(address)) {
								newDevice.setCheck(entity.isCheck());
							}
						}
						addDevice(newDevice);
					}
				}
			});
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReaderConnectBroadcastReceiver();
		startScanBleDevice(true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReaderConnectBroadcastReceiver();
		startScanBleDevice(false);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void openBluetooth() {
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
				startScanBleDevice(true);
				break;
			case BLUETOOTH_CLOSED:
				startScanBleDevice(true);
				break;
			case ACTIVITY_START:
				showProgress(false);
				boolean success = (Boolean) msg.obj;
				if(success){
					Editor editor = mSharedPreferences.edit();
					editor.putString("device", mCurrentBleDevice.getAddress());
					editor.putString("reader", "Reader1");
					editor.commit();
					
					Log.i(getLocalClassName(), "activity.start");
					mReaderHolder.setConnected(success);
					Intent newIntent = new Intent(LoginBLEActivity.this, ReaderMainActivity.class);
					startActivity(newIntent);
				}
				break;
			case CONNECT_FAILURE:
				showProgress(false);
				mReaderHolder.setConnected(false);
				mReaderHolder.disposeReader();
				InvengoUtils.showToast(LoginBLEActivity.this, R.string.toast_bluetooth_disconnect);
				break;
			default:
				break;
			}
		};
	};
	
	public void attemptQuit(){
		finish();
	}
	
	/**
	 * Attempts to connect reader by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptConnect() {
		if (mAuthTask != null) {
			return;
		}

		mReaderName = "Reader1";
		mConnectAddress = mCurrentBleDevice.getAddress();
		Reader mBleReader = new Reader(mReaderName, "BluetoothLE", mConnectAddress, this);
		mReaderHolder.setCurrentReader(mBleReader);
		mReaderHolder.setReaderName(mReaderName);
		mReaderHolder.setDeviceName(mConnectAddress);
		
		// Show a progress bar, and kick off a background task to
		// perform the reader connect attempt.
		mLoginStatusMessageView.setText(R.string.progress_bar_hint);
		showProgress(true);
		mAuthTask = new ReaderOperationTask(this);
		mAuthTask.execute(mBleReader);
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
				Message msg = new Message();
				msg.what = ACTIVITY_START;
				msg.obj = true;
				mLoginHandle.sendMessage(msg);
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
//			showProgress(false);

			boolean success = intent.getBooleanExtra(ReaderOperationTask.RESULT, false);
			if (success) {
//				attemptSelectChannel();
//				Message msg = new Message();
//				msg.what = ACTIVITY_START;
//				msg.obj = true;
//				mLoginHandle.sendMessage(msg);
				mReaderHolder.setConnected(true);
			}else{
				showProgress(false);
				InvengoUtils.showToast(LoginBLEActivity.this, getString(R.string.toast_connect_failure));
			}
		}
		
	}
	
	private void clear(){
		mList.clear();
		((BleDeviceArrayAdapter)mBleReaderListView.getAdapter()).notifyDataSetChanged();
	}
	
	private void addDevice(BleDeviceEntity device){
		if(!mList.contains(device)){
			mList.add(device);
			((BleDeviceArrayAdapter)mBleReaderListView.getAdapter()).notifyDataSetChanged();
		}
	}
	
	private class BleDeviceArrayAdapter extends ArrayAdapter<BleDeviceEntity>{

		private int tempViewId = -1;
		private int resourceId;
		private Activity context;
		public BleDeviceArrayAdapter(Activity context, int textViewResourceId,
				ArrayList<BleDeviceEntity> objects) {
			super(context, textViewResourceId, objects);
			this.resourceId = textViewResourceId;
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BleDeviceEntity entity = getItem(position);
			
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
			
			holder.nameView.setText(entity.getReaderName());
			holder.checkView.setId(position);
			holder.checkView.setChecked(entity.isCheck());
			if(entity.isCheck()) {
				tempViewId = holder.checkView.getId();
			}
			holder.checkView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int currentId = buttonView.getId();
					BleDeviceEntity currentEntity = BleDeviceArrayAdapter.this.getItem(currentId);
					if(isChecked){
						InvengoLog.i(TAG, "INFO.Before tempId {%s} & BLE MAC {%s}", String.valueOf(tempViewId), currentEntity.getReaderName());
						if(tempViewId != -1){
							CheckBox tempCheckBox = (CheckBox) context.findViewById(tempViewId);
							if(null != tempCheckBox){
								tempCheckBox.setChecked(!isChecked);
							}
						}
						tempViewId = buttonView.getId();
						currentEntity.setCheck(true);
						mCurrentBleDevice = currentEntity;
						InvengoLog.i(TAG, "INFO.After tempId {%s}", String.valueOf(tempViewId));
					}
					else {
						tempViewId = -1;
						currentEntity.setCheck(false);
						mCurrentBleDevice = null;
					}
				}
			});
			
			return convertView;
		}
		
		class ViewHolder{
			CheckBox checkView;
			TextView nameView;
		}
	}
	
	private Bundle mSavedInstanceState;
	private static final String STATE_KEY = "SLECTED";
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(null != mCurrentBleDevice) {
			outState.putSerializable(STATE_KEY, mCurrentBleDevice);
			mSavedInstanceState = outState;
		}
	}
}
