package com.invengo.rfidpad.base;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import invengo.javaapi.core.BaseReader;

public abstract class AbstractBaseActivity extends PowerManagerActivity {

	private BluetoothAdapter mAdapter;
	private IntentFilter mStateChangeFilter;
	private IntentFilter mAclConnectFilter;
	private IntentFilter mAclDisconnectFilter;
	private IntentFilter mBleConnectFilter;
	private IntentFilter mBleDisconnectFilter;
	private BluetoothChangeReceiver mBluetoothChangeReceiver;
	public static final String NAME = "BROADCAST_RESULT";
	public static final String BLUETOOTH_ON = "BLUETOOTH_ON";
	public static final String BLUETOOTH_OFF = "BLUETOOTH_OFF";
	public static final String BLUETOOTH_CONNECTED = "BLUETOOTH_CONNECTED";
	public static final String BLUETOOTH_DISCONNECTED = "BLUETOOTH_DISCONNECTED";
	
	public static final String ACTION_CUSTOM_BLUETOOTH_BROADCAST = "com.invengo.rfidpad.base.CUSTOM_BLUETOOTH_BROADCAST";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		registerBluetoothBroadcastReceiver();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterBluetoothBroadcastReceiver();
	}
	
	/**
	 * register the bluetooth broadcast/BLE broadcast
	 */
	private void registerBluetoothBroadcastReceiver() {
		mStateChangeFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mAclConnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
		mAclDisconnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		mBleConnectFilter = new IntentFilter(BaseReader.ACTION_READER_CONNECTED);
		mBleDisconnectFilter = new IntentFilter(BaseReader.ACTION_READER_DISCONNECTED);
//		mAclConnectFilter = new IntentFilter(BaseReader.ACTION_READER_CONNECTED);
//		mAclDisconnectFilter = new IntentFilter(BaseReader.ACTION_READER_DISCONNECTED);
		mBluetoothChangeReceiver = new BluetoothChangeReceiver();
		registerReceiver(mBluetoothChangeReceiver, mStateChangeFilter);
		registerReceiver(mBluetoothChangeReceiver, mAclConnectFilter);
		registerReceiver(mBluetoothChangeReceiver, mAclDisconnectFilter);
		registerReceiver(mBluetoothChangeReceiver, mBleConnectFilter);
		registerReceiver(mBluetoothChangeReceiver, mBleDisconnectFilter);

	}
	
	/**
	 * unregister the bluetooth broadcast
	 */
	private void unRegisterBluetoothBroadcastReceiver(){
		unregisterReceiver(mBluetoothChangeReceiver);
	}
	
	private class BluetoothChangeReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String result = null;
			if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){//bluetooth open_close
				if(mAdapter.getState() == BluetoothAdapter.STATE_ON){
					result = BLUETOOTH_ON;
				}else if(mAdapter.getState() == BluetoothAdapter.STATE_OFF){
					result = BLUETOOTH_OFF;
				}
			}else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){//low-level connect device,only when actually connecting the bluetoothLE device
				result = BLUETOOTH_CONNECTED;
				Log.i("AbstractBaseActivity", BLUETOOTH_CONNECTED);
			}else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){//low-level disconnect device,only when actually disconnecting the bluetoothLE device
				result = BLUETOOTH_DISCONNECTED;
				Log.i("AbstractBaseActivity", BLUETOOTH_DISCONNECTED);
			}else if(BaseReader.ACTION_READER_CONNECTED.equals(action)) {//BLE connected
				result = BLUETOOTH_CONNECTED;
				Log.i("AbstractBaseActivity", BLUETOOTH_CONNECTED);
			}else if(BaseReader.ACTION_READER_DISCONNECTED.equals(action)) {//BLE disconnected
				result = BLUETOOTH_DISCONNECTED;
				Log.i("AbstractBaseActivity", BLUETOOTH_DISCONNECTED);
			}
			
			if(null == result){
				return;
			}
			Intent broadcastIntent = new Intent(ACTION_CUSTOM_BLUETOOTH_BROADCAST);
			broadcastIntent.putExtra(NAME, result);
			sendBroadcast(broadcastIntent);
		}
	}
}
