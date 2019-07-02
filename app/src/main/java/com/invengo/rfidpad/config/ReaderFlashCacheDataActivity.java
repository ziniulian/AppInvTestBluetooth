package com.invengo.rfidpad.config;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.RXD_BARCODE;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.entity.TagScanInfoEntity;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * XC2600-Flash Cache Data
 */
public class ReaderFlashCacheDataActivity extends PowerManagerActivity {

	private ReaderHolder mReaderHolder;
	private View mTagNumberLayout;
	private TextView mTagNumberView;
	private ListView mTagInfoListView;
	private View mUploadProgressBarLayout;
	private ProgressBar mUploadProgressBar;
	private TextView mUploadPercentTextView;
	private TextView mUploadTotalTextView;
	private List<TagScanInfoEntity> mList = new ArrayList<TagScanInfoEntity>();
	private boolean reading = false;
	private static final String TAG = ReaderFlashCacheDataActivity.class.getSimpleName();
	private static final byte PARAMETER = (byte) 0x86;
	private static final String TAG_6C = "6C";
	private static final String TAG_GB = "GB";
	
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_flash_cache_data);
		setTitle(R.string.title_reader_flash_cache_data);
		
		mReaderHolder = ReaderHolder.getInstance();
		mTagNumberLayout = findViewById(R.id.layout_reader_flash_cache_data_number_id);
		mTagNumberView = (TextView) findViewById(R.id.textview_reader_flash_cache_data_number_id);
		mUploadProgressBarLayout = findViewById(R.id.layout_reader_flash_cache_data_progressbar_id);
		mUploadProgressBar = (ProgressBar) findViewById(R.id.progress_bar_reader_flash_cache_data_id);
		mUploadPercentTextView = (TextView) findViewById(R.id.textview_reader_flash_cache_data_percent_id);
		mUploadTotalTextView = (TextView) findViewById(R.id.textview_reader_flash_cache_data_total_id);
		mTagInfoListView = (ListView) findViewById(R.id.list_reader_flash_cache_data_detail);
		mTagInfoListView.setEmptyView(findViewById(R.id.text_reader_flash_cache_data_empty));
		TagInfoArrayAdapter adapter = new TagInfoArrayAdapter(this, R.layout.list_tag_scan_detail_item, mList);
		mTagInfoListView.setAdapter(adapter);	
		
		mProgressBarManager = ProgressBarManager.getInstance();
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
		
		InvengoLog.i(TAG, "INFO.onCreate().");
	}
	
	private Menu mOperationMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mOperationMenu = menu;
		getMenuInflater().inflate(R.menu.menu_reader_flash_cache_data, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
		case R.id.menu_reader_flash_cache_data_export:
			if(!mReaderHolder.isConnected()){
				InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
				break;
			}
			if(!reading){//start reading tags
				startScanTag();
				item.setTitle(R.string.menu_reader_flash_cache_data_stop_title);
			}else{//stop reading tags
				stopScanTag();
				item.setTitle(R.string.menu_reader_flash_cache_data_export_title);
			}
			break;
		case R.id.menu_reader_flash_cache_data_clear:
			if(reading == true){
				InvengoUtils.showToast(ReaderFlashCacheDataActivity.this, R.string.toast_reader_flash_cache_data_clear_refuse);
				break;
			}
			clearScanTag();
			mTagNumberLayout.setVisibility(View.GONE);
			mUploadProgressBarLayout.setVisibility(View.GONE);
			break;
		default:
			break;
		}		
		return true;
	}
	
	private void startScanTag() {
		reading = true;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SysQuery_800 msg = new SysQuery_800(PARAMETER);
				msg.setIsReturn(false);
				mReaderHolder.getCurrentReader().send(msg);
				Message message = new Message();
				message.what = START;
				scanHandle.sendMessage(message);
			}
		}).start();
	}
	
	private void stopScanTag() {
		reading = false;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SysQuery_800 msg = new SysQuery_800(PARAMETER, (byte) 0x01);
				msg.setIsReturn(false);
				mReaderHolder.getCurrentReader().send(msg);
				Message message = new Message();
				message.what = STOP;
				scanHandle.sendMessage(message);
			}
		}).start();
	}
	
	private static final int START = 0;
	private static final int STOP = 1;
	private static final int REFRESH = 2;
	private static final int PROGRESSBAR_TOTAL_REFRESH = 3;
	private static final int CLEAR = 4;
	private long count = 0l;
	private long total = 0l;
	private Handler scanHandle = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case START:
				count = 0l;
				mList.clear();
				((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
				if(mTagNumberLayout.getVisibility() == View.VISIBLE){
					mTagNumberLayout.setVisibility(View.GONE);
				}
				if(mUploadProgressBarLayout.getVisibility() == View.VISIBLE){
					mUploadProgressBarLayout.setVisibility(View.GONE);
				}

				InvengoUtils.showToast(ReaderFlashCacheDataActivity.this, R.string.toast_reader_flash_cache_data_export_start);
				break;
			case STOP:
				InvengoUtils.showToast(ReaderFlashCacheDataActivity.this, R.string.toast_reader_flash_cache_data_export_stop);
				break;
			case CLEAR:
				int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
				mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
				InvengoUtils.showToast(ReaderFlashCacheDataActivity.this, R.string.toast_reader_flash_cache_data_export_clear);
				break;
			case REFRESH:
				if(mTagNumberLayout.getVisibility() == View.GONE){
					mTagNumberLayout.setVisibility(View.VISIBLE);
				}
				if(mUploadProgressBarLayout.getVisibility() == View.GONE){
					mUploadProgressBarLayout.setVisibility(View.VISIBLE);
				}
				Bundle data = msg.getData();
				String type = data.getString("type");
				
				count++;
				
				boolean isExists = false;
				
				if(type.equals(getString(R.string.label_barcode_scan_type))){//barcode
					String barcode = data.getString("barcode");
					String barcodeUtc = data.getString("utc");
					for(TagScanInfoEntity entity : mList){
						if (entity.getType().equals(getString(R.string.label_tag_scan_type_6b))
								|| entity.getType().equals(getString(R.string.label_tag_scan_type_6c))
								|| entity.getType().equals(getString(R.string.label_tag_scan_type_gb))) {
							continue;
						}
						
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
						newEntity.setType(type);
						newEntity.setBarcode(barcode);
						newEntity.setUtc(barcodeUtc);
						newEntity.setNumber(1);
						mList.add(newEntity);
					}
				}else{//rfid
					String epc = data.getString("epc");
					String tid = data.getString("tid");
					String userData = data.getString("userData");
					String rssi = data.getString("rssi");
					String utc = data.getString("utc");
					for(TagScanInfoEntity entity : mList){
						if (entity.getType().equals(getString(R.string.label_barcode_scan_type))) {
							continue;
						}
						
						String oldEpc = entity.getEpc();
						String oldTid = entity.getTid();
						String oldUserData = entity.getUserdata();
						if((oldEpc + oldTid + oldUserData).equals(epc + tid + userData)){
							isExists = true;
							int oldNumber = entity.getNumber();
							entity.setNumber(oldNumber + 1);
							entity.setUtc(utc);
							break;
						}
					}
					
					if(!isExists){
						TagScanInfoEntity newEntity = new TagScanInfoEntity();
						newEntity.setType(type);
						newEntity.setEpc(epc);
						newEntity.setTid(tid);
						newEntity.setUserdata(userData);
						newEntity.setNumber(1);
						newEntity.setRssi(rssi);
						newEntity.setUtc(utc);
						mList.add(newEntity);
					}
				}
				
				((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
				mTagNumberView.setText(String.valueOf(mList.size()));
				mUploadProgressBar.setProgress((int) count);
				mUploadTotalTextView.setText(String.format("%s/%s", count, total));
				mUploadPercentTextView.setText(String.format("%s", getPercent(count, total)));
				if(count == total){
					reading = false;
					InvengoUtils.showToast(ReaderFlashCacheDataActivity.this, R.string.toast_reader_flash_cache_data_export_complete);
					mOperationMenu.findItem(R.id.menu_reader_flash_cache_data_export).setTitle(R.string.menu_reader_flash_cache_data_export_title);
				}
				break;
			case PROGRESSBAR_TOTAL_REFRESH:
				total = (long) msg.obj;
				if(total == 0){
					reading = false;
					InvengoUtils.showToast(ReaderFlashCacheDataActivity.this, R.string.toast_reader_flash_cache_data_export_empty);
					mOperationMenu.findItem(R.id.menu_reader_flash_cache_data_export).setTitle(R.string.menu_reader_flash_cache_data_export_title);
					break;
				}
				mUploadProgressBar.setMax((int) total);
				mUploadTotalTextView.setText(String.format("%s/%s", 0, total));
				mUploadPercentTextView.setText(String.format("%s", 0) + "%");
				break;
			default:
				break;
			}
			
		};
	};
	
	private String getPercent(float divisor,  float dividend){
		String percent = "";
		NumberFormat percentFormat = NumberFormat.getInstance();
		percentFormat.setMaximumFractionDigits(2);
		percent = percentFormat.format(divisor / dividend * 100) + "%";
		return percent;
	}
	
	private void clearScanTag() {
//		if(reading == true){
//			stopScanTag(true);
//		}
		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_clear_default_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		
		mList.clear();
		((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
		clearTagCache();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopScanTag(true);
	}
	
	private void clearTagCache(){
		reading = false;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					SysQuery_800 msg = new SysQuery_800(PARAMETER, (byte) 0x02);
					msg.setIsReturn(false);
					mReaderHolder.getCurrentReader().send(msg);
//					if(true){
//						Message message = new Message();
//						message.what = CLEAR;
//						scanHandle.sendMessage(message);
//					}
				}
			}
		}).start();
	}

	private void stopScanTag(boolean immediately){
		reading = false;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(mReaderHolder.isConnected()){
					SysQuery_800 msg = new SysQuery_800(PARAMETER, (byte) 0x01);
					msg.setIsReturn(false);
					mReaderHolder.getCurrentReader().send(msg);
				}
			}
		}).start();
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
			
//			holder.tidLayout.setVisibility(View.GONE);
//			holder.tidView.setText(entity.getTid());
//			holder.userdataLayout.setVisibility(View.GONE);
//			holder.userDataView.setText(entity.getUserdata());
			holder.rssiLayout.setVisibility(View.GONE);
			holder.rssiView.setText(entity.getRssi());
			
			holder.typeView.setText(entity.getType());
			holder.timesView.setText(String.valueOf(entity.getNumber()));
			if(entity.getType().equals(getString(R.string.label_barcode_scan_type))){//barcode
				holder.epcLayout.setVisibility(View.GONE);
				holder.epcView.setText(entity.getEpc());
				holder.tidLayout.setVisibility(View.GONE);
				holder.tidView.setText(entity.getTid());
				holder.userdataLayout.setVisibility(View.GONE);
				holder.userDataView.setText(entity.getUserdata());
				holder.barcodeLayout.setVisibility(View.VISIBLE);
				holder.barcodeView.setText(entity.getBarcode());
			}else{//rfid
				holder.epcLayout.setVisibility(View.VISIBLE);
				holder.epcView.setText(entity.getEpc());
				holder.tidLayout.setVisibility(View.VISIBLE);
				holder.tidView.setText(entity.getTid());
				holder.userdataLayout.setVisibility(View.VISIBLE);
				holder.userDataView.setText(entity.getUserdata());
				holder.barcodeLayout.setVisibility(View.GONE);
				holder.barcodeView.setText(entity.getBarcode());
			}
			
			holder.utcLayout.setVisibility(View.VISIBLE);
			holder.utcView.setText(entity.getUtc());
			
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

	@Override
	public void handleNotificationMessage(BaseReader reader,
			IMessageNotification msg) {
		super.handleNotificationMessage(reader, msg);
		if(mReaderHolder.isConnected()){
			if(msg instanceof RXD_TagData){
				RXD_TagData data = (RXD_TagData) msg;
				if(null != data.getReceivedMessage()){
					
					String type = data.getReceivedMessage().getTagType();
					type = type.toUpperCase().equals(TAG_6C) ? getString(R.string.label_tag_scan_type_6c)
									: (type.toUpperCase().equals(TAG_GB) ? getString(R.string.label_tag_scan_type_gb)
											: getString(R.string.label_tag_scan_type_6b));
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
					String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
					String userData = Util.convertByteArrayToHexString(data.getReceivedMessage().getUserData());
					String utc = "1970-01-01 00:00:00";
					byte[] utcData = data.getReceivedMessage().getRXDTime();
//					InvengoLog.i(TAG, "utcHexString {%s}", Util.convertByteArrayToHexString(utcData));
					if(null != utcData){
						utc = Util.getUtc(utcData);
					}
					
					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("epc", epc);
					bundle.putString("tid", tid);
					bundle.putString("userData", userData);
					bundle.putString("utc", utc);
					notifyMessage.setData(bundle);
					notifyMessage.what = REFRESH;
					scanHandle.sendMessage(notifyMessage);
				}
			}else if(msg instanceof RXD_BARCODE){
				RXD_BARCODE response = (RXD_BARCODE) msg;
				if(response.getReceivedMessage() != null){
					String barcode = "";
					try {
						barcode = new String(response.getReceivedMessage().getBarcodeData(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String utc = Util.getUtc(response.getReceivedMessage().getUtc());
					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", getString(R.string.label_barcode_scan_type));
					bundle.putString("barcode", barcode);
					bundle.putString("utc", utc);
					notifyMessage.setData(bundle);
					notifyMessage.what = REFRESH;
					scanHandle.sendMessage(notifyMessage);
				}
			}else if(msg instanceof SysQuery_800){
				SysQuery_800 response = (SysQuery_800) msg;
				if(response.getReceivedMessage() != null){
					SysQuery800ReceivedInfo info = response.getReceivedMessage();
					if(response.getStatusCode() == 0){
						byte[] data = info.getQueryData();
//						if(null == data){
//							Message message = new Message();
//							message.what = CLEAR;
//							scanHandle.sendMessage(message);
//							return;
//						}
						long dataLength = ((0x000000FF & data[0]) << 24) | ((0x000000FF & data[1]) << 16) | ((0x000000FF & data[2]) << 8) | ((0x000000FF & data[3]) & 0xFFFFFFFFL);
						Message notifyMessage = new Message();
						notifyMessage.obj = dataLength;
						notifyMessage.what = PROGRESSBAR_TOTAL_REFRESH;
						scanHandle.sendMessage(notifyMessage);
					}
					
				}else{
					if(response.getStatusCode() == 0){
						Message message = new Message();
						message.what = CLEAR;
						scanHandle.sendMessage(message);
					}
				}
			}
		}
	}
	
}
