package com.invengo.test.bltrfiddemo.entity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.invengo.test.bltrfiddemo.Ma;
import com.invengo.test.bltrfiddemo.enums.EmUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;
import static tk.ziniulian.util.Str.Bytes2Hexstr;
import static tk.ziniulian.util.Str.Hexstr2Bytes;

/**
 * 蓝牙BLE测试
 * Created by 李泽荣 on 2019/7/2.
 */

public class TestBlt {
	private Activity ma;
	private BluetoothAdapter ba = null;

	private ArrayList<BluetoothDevice> ds = new ArrayList<BluetoothDevice>();	// 扫描到的设备列表
	// 扫描设备的回调函数
	private BluetoothAdapter.LeScanCallback cbScan = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			// device ： 设备对象
			// rssi ： 信号强度
			// scanRecord ： 扫描记录
			if (device.getType() == 2 && "Feasycom".equals(device.getName())) {
				Log.i("--------", device.getAddress() + " , " + device.getBondState());
				stopScanDevice();
				connectDevice(device);
			}
//			if (!ds.contains(device)){
//				ds.add(device);
//				Log.i("--- 扫描到一个设备 ---", device.toString() + " , " + device.getAddress() + " , " + device.getName() + " , " + device.getBondState() + " , " + device.getType());
//				// mAdapter.notifyDataSetChanged();
//			}
		}
	};
	private boolean isScaning = false;	// 是否处于扫描状态

	private BluetoothGatt bg = null;
	// Feasycom 设备的UUID
	private UUID srvBleId = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");	// Service UUID
	private UUID wrtBleId = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");	// 写入 UUID
	private UUID ntfBleId = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");	// 监听 UUID
	private UUID ccc = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");		// 监听的 Descriptor UUID
	private BluetoothGattCallback cbGatt = new BluetoothGattCallback() {
		/**
		 * 断开或连接 状态发生变化时调用
		 * */
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			Log.i("--------", "onConnectionStateChange()");
			if (status == BluetoothGatt.GATT_SUCCESS){
				//连接成功
				if (newState == BluetoothGatt.STATE_CONNECTED){
					Log.i("--------", "连接成功");
					//发现服务
					gatt.discoverServices();
				}
			}else{
				//连接失败
				Log.i("--------", "失败 == "+status);
				close();
			}
		}
		/**
		 * 发现设备（真正建立连接）
		 * */
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			//直到这里才是真正建立了可通信的连接
			isConnecting = true;
			Log.i("--------", "onServicesDiscovered()---建立连接");

			// 获取初始化服务和特征值
			// initServiceAndChara();

Log.i("----------", "新的线程");		// 若 writeDescriptor 和 writeCharacteristic 都在这里执行，会导致事件阻塞。
			// 订阅通知
			new Thread() {
				@Override
				public void run() {
					BluetoothGattCharacteristic ntfBgc = bg.getService(srvBleId).getCharacteristic(ntfBleId);
					if (ntfBgc != null) {
						if (bg.setCharacteristicNotification(ntfBgc, true)) {
Log.i("--------", "监听成功");

//					List<BluetoothGattDescriptor> ds = wrtBgc.getDescriptors();
//					Log.i("------", ds.size() + "");
//					Log.i("------", ds.get(0).getUuid().toString());

							BluetoothGattDescriptor bgd = ntfBgc.getDescriptor(ccc);
							if (bgd != null) {
Log.i("--------", "获得bgd , " + Bytes2Hexstr(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
								bgd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
								if (bg.writeDescriptor(bgd)) {
Log.i("--------", "监听设置完成");
									((Ma)ma).sendUrl(EmUrl.Test);
								}
							}
						}
					}
Log.i("--------", "OK！");
				}
			}.start();

//			// 发送测试信息
//			new Thread() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep(5000);	// 五秒后再发送数据，防止订阅通知线程未准备就绪
//Log.i("-----------", "wrt _ S");
////						wrt("5500026000402E");	// 开功放
////						wrt("5500026001C02B");	// 赖工的开功放
//						wrt("550001610746");	// 赖工的关功放
////						wrt("5500026009402E");
////						wrt("5500");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}.start();
		}
		/**
		 * 读操作的回调
		 * */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			Log.i("--------", "onCharacteristicRead()");
		}
		/**
		 * 写操作的回调
		 * */
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			Log.i("--------", "onCharacteristicWrite()  status=" + status + ",value=" + Bytes2Hexstr(characteristic.getValue()));
		}
		/**
		 * 接收到硬件返回的数据
		 * */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			Log.i("--------", "onCharacteristicChanged() : " + Bytes2Hexstr(characteristic.getValue()));
		}
	};
	private boolean isConnecting = false;	// 是否已连接

/*******************************************/

	public TestBlt (Activity a) {
		this.ma = a;
	}

	// 蓝牙初始化
	public boolean init () {
		if (ba == null) {
			BluetoothManager bm = (BluetoothManager) ma.getSystemService(BLUETOOTH_SERVICE);
			ba = bm.getAdapter();
			if (ba==null || !ba.isEnabled()) {
Log.i("------", "蓝牙未打开！请求打开蓝牙。");
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				ma.startActivityForResult(intent, 0);
				// TODO: 2019/7/2 此处需要监听 intent 的返回结果，决定程序是继续还是终止。
			} else {
Log.i("------", "蓝牙已打开！");
//				ds = ba.getBondedDevices();
				return true;
			}
		}
		return false;
	}

	// 扫描设备
	public void scanDevice() {
		isScaning = true;
		ba.startLeScan(cbScan);	// 开始扫描

		// 设置定时器：定时扫描 10 秒，10秒后自动停止扫描。
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {	//结束扫描
				stopScanDevice();
			}
		}, 10000);
	}

	// 停止扫描设备
	public void stopScanDevice () {
		if (isScaning) {
			ba.stopLeScan(cbScan);	// 结束扫描
			isScaning = false;
		}
	}

	// 连接设备
	public void connectDevice (BluetoothDevice d) {
//		if (Build.VERSION.SDK_INT >= 23) {
//			bg = d.connectGatt(ma, true, cbGatt, TRANSPORT_LE);
//		} else {
			bg = d.connectGatt(ma, true, cbGatt);
//		}
	}

	// 关闭连接
	public void close () {
		bg.disconnect();
		bg.close();
		isConnecting = false;
	}

	// 获取UUID
	private void initServiceAndChara(){
		UUID read_UUID_chara, read_UUID_service, write_UUID_chara, write_UUID_service, notify_UUID_chara, notify_UUID_service, indicate_UUID_chara, indicate_UUID_service;

		List<BluetoothGattService> bluetoothGattServices= bg.getServices();
		for (BluetoothGattService bluetoothGattService:bluetoothGattServices){
			List<BluetoothGattCharacteristic> characteristics=bluetoothGattService.getCharacteristics();
			for (BluetoothGattCharacteristic characteristic:characteristics){
				int charaProp = characteristic.getProperties();
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					read_UUID_chara=characteristic.getUuid();
					read_UUID_service=bluetoothGattService.getUuid();
					Log.i("------", "read_chara="+read_UUID_chara+"----read_service="+read_UUID_service);
				}
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
					write_UUID_chara=characteristic.getUuid();
					write_UUID_service=bluetoothGattService.getUuid();
					Log.i("------", "write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);
				}
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
					write_UUID_chara=characteristic.getUuid();
					write_UUID_service=bluetoothGattService.getUuid();
					Log.i("------", "write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);
				}
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					notify_UUID_chara=characteristic.getUuid();
					notify_UUID_service=bluetoothGattService.getUuid();
					Log.i("------", "notify_chara="+notify_UUID_chara+"----notify_service="+notify_UUID_service);
				}
				if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
					indicate_UUID_chara=characteristic.getUuid();
					indicate_UUID_service=bluetoothGattService.getUuid();
					Log.i("------", "indicate_chara="+indicate_UUID_chara+"----indicate_service="+indicate_UUID_service);
				}
			}
		}
	}

	// 写数据
	public void wrt (String hex) {
		BluetoothGattService service = bg.getService(srvBleId);
		BluetoothGattCharacteristic charaWrite = service.getCharacteristic(wrtBleId);
		byte[] data = Hexstr2Bytes (hex);
		if (data.length>20){	// 数据大于20个字节 分批次写入
Log.i("------", "writeData: length = " + data.length);
			int num=0;
			if (data.length%20!=0){
				num=data.length/20+1;
			}else{
				num=data.length/20;
			}
			for (int i=0;i<num;i++){
				byte[] tempArr;
				if (i==num-1){
					tempArr=new byte[data.length-i*20];
					System.arraycopy(data,i*20,tempArr,0,data.length-i*20);
				}else{
					tempArr=new byte[20];
					System.arraycopy(data,i*20,tempArr,0,20);
				}
				charaWrite.setValue(tempArr);
				bg.writeCharacteristic(charaWrite);
			}
		}else{
			charaWrite.setValue(data);
			bg.writeCharacteristic(charaWrite);
		}
	}
}
