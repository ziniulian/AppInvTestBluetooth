package com.invengo.rfidpad.config;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.Buzzer_500;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.entity.TagScanInfoEntity;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * XC2600-蜂鸣器
 */
public class ReaderBuzzerConfigurationActivity extends PowerManagerActivity {

	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private ListView mTagInfoListView;
	private List<TagScanInfoEntity> mList = new ArrayList<TagScanInfoEntity>();
	private boolean isReading = false;
	private static final String TAG = ReaderBuzzerConfigurationActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_buzzer_configure);
		setTitle(R.string.title_reader_configuration_buzzer_configuration);

		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		mTagInfoListView = (ListView) findViewById(R.id.list_buzzer_test);
		mTagInfoListView.setEmptyView(findViewById(R.id.text_buzzer_test_empty));
		TagInfoArrayAdapter adapter = new TagInfoArrayAdapter(this, R.layout.list_tag_scan_detail_item, mList);
		mTagInfoListView.setAdapter(adapter);

		InvengoLog.i(TAG, "INFO.onCreate().");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_configuration_buzzer_configuration, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_configuration_buzzer_open_id:
				attemptConfigureBuzzer((byte) 0x00, R.string.progress_bar_configuration_buzzer_configuration_open_message);
				break;
			case R.id.menu_reader_configuration_buzzer_close_id:
				attemptConfigureBuzzer((byte) 0x01, R.string.progress_bar_configuration_buzzer_configuration_close_message);
				break;
			case R.id.menu_reader_configuration_buzzer_test_id:
				if(!mHolder.isConnected()){
					InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
					break;
				}
				if(!isReading){//start reading tags
					clearScanTag();
					scanTag();
					item.setTitle(R.string.menu_buzzer_test_stop_title);
				}else{//stop reading tags
					stopScanTag();
					item.setTitle(R.string.menu_buzzer_test_find_title);
//				clearScanTag();
				}
				break;
			default:
				break;
		}
		return true;
	}

	private void clearScanTag() {
		mList.clear();
		((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
	}

	protected void scanTag() {
		isReading = true;
		final ReadTag msg = new ReadTag(ReadMemoryBank.EPC_6C);
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mHolder.isConnected()){
					mHolder.getCurrentReader().send(msg);
				}
			}
		}).start();
	}

	private void attemptConfigureBuzzer(byte type, int resId) {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		InvengoLog.i(TAG, "INFO.configure buzzer.");

		setMenuItemEnabled(false);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, resId);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		Buzzer_500 msg= new Buzzer_500(type);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void setMenuItemEnabled(boolean enable){
		findViewById(R.id.menu_reader_configuration_buzzer_open_id).setEnabled(enable);
		findViewById(R.id.menu_reader_configuration_buzzer_close_id).setEnabled(enable);
	}

	@Override
	protected void onResume() {
		super.onResume();

		initializeData();

//		if(null != mHolder.getCurrentReader()){
//			mHolder.getCurrentReader().onMessageNotificationReceived.add(this);//注册
//		}
		InvengoLog.i(TAG, "INFO.onResume().");
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			setMenuItemEnabled(true);
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(ReaderBuzzerConfigurationActivity.this, R.string.toast_reader_configuration_buzzer_configuration_success);
			}else{
				//配置/查询失败
				InvengoUtils.showToast(ReaderBuzzerConfigurationActivity.this, R.string.toast_reader_configuration_buzzer_configuration_failure);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if(null != mHolder.getCurrentReader()){
//			mHolder.getCurrentReader().onMessageNotificationReceived.remove(this);//取消注册
//		}
		unregisterReceiver(mReceiver);
		InvengoLog.i(TAG, "INFO.onPause().");
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopScanTag();
	}

	private void stopScanTag() {
		isReading = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(null != mHolder.getCurrentReader()){
					mHolder.getCurrentReader().send(new PowerOff_800());
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

				holder.epcLayout = (LinearLayout) convertView.findViewById(R.id.layout_epc);
				holder.tidLayout = (LinearLayout) convertView.findViewById(R.id.layout_tid);
				holder.userdataLayout = (LinearLayout) convertView.findViewById(R.id.layout_userdata);
				holder.barcodeLayout = (LinearLayout) convertView.findViewById(R.id.layout_barcode);
				holder.rssiLayout = (LinearLayout) convertView.findViewById(R.id.layout_tag_scan_rssi);
				holder.utcLayout = (LinearLayout) convertView.findViewById(R.id.layout_tag_scan_utc);

				holder.typeView = (TextView) convertView.findViewById(R.id.text_tag_scan_type);
				holder.timesView = (TextView) convertView.findViewById(R.id.text_tag_scan_times);
				holder.epcView = (TextView) convertView.findViewById(R.id.text_tag_scan_epc);
				holder.tidView = (TextView) convertView.findViewById(R.id.text_tag_scan_tid);
				holder.userDataView = (TextView) convertView.findViewById(R.id.text_tag_scan_userdata);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.typeView.setText(entity.getType());
			holder.timesView.setText(String.valueOf(entity.getNumber()));
			holder.epcView.setText(entity.getEpc());
			holder.tidView.setText(entity.getTid());
			holder.userDataView.setText(entity.getUserdata());
			holder.epcLayout.setVisibility(View.VISIBLE);
			holder.tidLayout.setVisibility(View.GONE);
			holder.userdataLayout.setVisibility(View.GONE);
			holder.barcodeLayout.setVisibility(View.GONE);
			holder.rssiLayout.setVisibility(View.GONE);
			holder.utcLayout.setVisibility(View.GONE);

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
		}
	}

	private static final int FOUND_TAG = 0;
	private static final int REFRESH = 1;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case FOUND_TAG:

					break;
				case REFRESH:
					Bundle data = msg.getData();
					String type = getString(R.string.label_tag_scan_type_6c);
					String epc = data.getString("epc");
//				String tid = data.getString("tid");
//				String userData = data.getString("userData");
					boolean isExists = false;
					for(TagScanInfoEntity entity : mList){
						String oldData = entity.getEpc();
						if((epc).equals(oldData)){
							isExists = true;
							int oldNumber = entity.getNumber();
							entity.setNumber(oldNumber + 1);
							break;
						}
					}

					if(!isExists){
						TagScanInfoEntity newEntity = new TagScanInfoEntity();
						newEntity.setType(type);
						newEntity.setEpc(epc);
//					newEntity.setTid(tid);
//					newEntity.setUserdata(userData);
						newEntity.setNumber(1);
						mList.add(newEntity);
					}
					((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
					break;
				default:
					break;
			}
		};
	};

	@Override
	public void handleNotificationMessage(BaseReader reader, IMessageNotification msg) {
		if(isReading){
			if(mHolder.isConnected()){
				if(msg instanceof RXD_TagData){
					RXD_TagData data = (RXD_TagData) msg;
					String type = data.getReceivedMessage().getTagType();
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
					String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
					String userData = Util.convertByteArrayToHexString(data.getReceivedMessage().getUserData());

					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("epc", epc);
//					bundle.putString("tid", tid);
//					bundle.putString("userData", userData);
					notifyMessage.setData(bundle);
					notifyMessage.what = REFRESH;
					handler.sendMessage(notifyMessage);
				}
			}
		}
	};

//	@Override
//	public void messageNotificationReceivedHandle(BaseReader reader,
//			IMessageNotification msg) {
//		if(isReading){
//			if(mHolder.isConnected()){
//				if(msg instanceof RXD_TagData){
//					RXD_TagData data = (RXD_TagData) msg;
//					String type = data.getReceivedMessage().getTagType();
//					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
//					String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
//					String userData = Util.convertByteArrayToHexString(data.getReceivedMessage().getUserData());
//					
//					Message notifyMessage = new Message();
//					Bundle bundle = new Bundle();
//					bundle.putString("type", type);
//					bundle.putString("epc", epc);
////					bundle.putString("tid", tid);
////					bundle.putString("userData", userData);
//					notifyMessage.setData(bundle);
//					notifyMessage.what = REFRESH;
//					handler.sendMessage(notifyMessage);
//				}
//			}
//		}
//	}

}
