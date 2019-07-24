package com.invengo.rfidpad.other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.entity.TagScanInfoEntity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.InvengoUtils;

import java.util.ArrayList;
import java.util.List;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.SelectTag_6C;

/**
 * 查找标签
 */
public class FoundTagActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private TagFoundBroadcastReceiver mReceiver;
	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;

	private RadioButton mTidRadioButton;
	private RadioButton mEpcRadioButton;
	private EditText mAddressEditText;
	private EditText mMatchedDataEditText;
	private ListView mTagInfoListView;
	private List<TagScanInfoEntity> mList = new ArrayList<TagScanInfoEntity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_found_tag);
		setTitle(R.string.title_found_tag);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();

		mTidRadioButton = (RadioButton) findViewById(R.id.radio_found_tag__type_tid);
		mEpcRadioButton = (RadioButton) findViewById(R.id.radio_found_tag__type_epc);
		mAddressEditText = (EditText) findViewById(R.id.edit_found_tag_address);
		mMatchedDataEditText = (EditText) findViewById(R.id.edit_found_tag_data);

		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		mTagInfoListView = (ListView) findViewById(R.id.list_tag_found_detail);
		TagInfoArrayAdapter adapter = new TagInfoArrayAdapter(this, R.layout.list_tag_scan_detail_item, mList);
		mTagInfoListView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
		//		if(null != mHolder.getCurrentReader()){
		//			mHolder.getCurrentReader().onMessageNotificationReceived.add(this);//注册
		//		}
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagFoundBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
		//		if(null != mHolder.getCurrentReader()){
		//			mHolder.getCurrentReader().onMessageNotificationReceived.remove(this);//取消注册
		//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopScanTag();
	}

	private boolean reading = false;
	private void stopScanTag() {
		reading = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mHolder.isConnected()){
					mHolder.getCurrentReader().send(new PowerOff_800());
				}
			}
		}).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_found_tag, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_found_tag_find_id:
				if(!mHolder.isConnected()){
					InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
					break;
				}
				//			clearScanTag();
				if(!reading){//start reading tags
					if(TextUtils.isEmpty(mAddressEditText.getText().toString())){
						InvengoUtils.showToast(this, R.string.toast_found_tag_address_null);
						break;
					}
					if(TextUtils.isEmpty(mMatchedDataEditText.getText().toString())){
						InvengoUtils.showToast(this, R.string.toast_found_tag_data_null);
						break;
					}

					findTag();
					item.setTitle(R.string.menu_found_tag_stop_title);
				}else{//stop reading tags
					stopScanTag();
					item.setTitle(R.string.menu_found_tag_find_title);
				}
				break;
			case R.id.menu_found_tag_clear_id:
				clearScanTag();
				break;
			default:
				break;
		}
		return true;
	}

	private void findTag() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		String address = mAddressEditText.getText().toString();
		//		if(TextUtils.isEmpty(address)){
		//			InvengoUtils.showToast(this, R.string.toast_found_tag_address_null);
		//			return;
		//		}
		String data = mMatchedDataEditText.getText().toString();
		//		if(TextUtils.isEmpty(data)){
		//			InvengoUtils.showToast(this, R.string.toast_found_tag_data_null);
		//			return;
		//		}

		MemoryBank memoryBank = null;
		if(mTidRadioButton.isChecked()){
			memoryBank = MemoryBank.TIDMemory;
		}else if(mEpcRadioButton.isChecked()){
			memoryBank = MemoryBank.EPCMemory;
		}

		int ptr = Integer.parseInt(address);
		byte[] tagData = Util.convertHexStringToByteArray(data);
		byte length = (byte) (tagData.length * 8);

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_found_tag_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		SelectTag_6C msg = new SelectTag_6C(memoryBank, ptr, length,  tagData);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private class TagFoundBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(FoundTagActivity.this, R.string.toast_found_tag_success);
				Message message = new Message();
				message.what = FOUND_TAG;
				handler.sendMessage(message);
			}else{
				InvengoUtils.showToast(FoundTagActivity.this, R.string.toast_found_tag_failure);
			}
		}
	}

	private static final int FOUND_TAG = 0;
	private static final int REFRESH = 1;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case FOUND_TAG:
					scanTag();
					break;
				case REFRESH:
					Bundle data = msg.getData();
					String type = getString(R.string.label_tag_scan_type_6c);
					String epc = data.getString("epc");
					String tid = data.getString("tid");
					String userData = data.getString("userData");
					boolean isExists = false;
					for(TagScanInfoEntity entity : mList){
						String oldData = entity.getEpc() + entity.getTid() + entity.getUserdata();
						if((epc + tid + userData).equals(oldData)){
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
						newEntity.setTid(tid);
						newEntity.setUserdata(userData);
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

	private void clearScanTag() {
		mList.clear();
		((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
	}


	//	01H～04H	（1字节）	00/01/02	(1字节)	EVB	1字节
	//	81       03         01          06      0001
	protected void scanTag() {
		reading = true;
		final ReadTag msg = new ReadTag(ReadMemoryBank.EPC_TID_UserData_6C_2);
		msg.setType((byte) 0x01);
		msg.setUserDataLen_6C((byte) 0x01);
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mHolder.isConnected()){
					mHolder.getCurrentReader().send(msg);
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
			holder.tidLayout.setVisibility(View.VISIBLE);
			holder.userdataLayout.setVisibility(View.VISIBLE);
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

	@Override
	public void handleNotificationMessage(BaseReader reader,
										  IMessageNotification msg) {
		super.handleNotificationMessage(reader, msg);
		if(reading){
			if(mHolder.isConnected()){
				if(msg instanceof RXD_TagData){
					//					android.util.Log.i("TagScanActivity", Util.convertByteArrayToHexString(((RXD_TagData)msg).getReceivedMessage().getEPC()));
					//					if(mSettingsCollection.isVoiced()){
					//						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
					//					}
					RXD_TagData data = (RXD_TagData) msg;
					String type = data.getReceivedMessage().getTagType();
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());
					String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
					String userData = Util.convertByteArrayToHexString(data.getReceivedMessage().getUserData());

					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("epc", epc);
					bundle.putString("tid", tid);
					bundle.putString("userData", userData);
					notifyMessage.setData(bundle);
					notifyMessage.what = REFRESH;
					handler.sendMessage(notifyMessage);
				}
			}
		}
	}

	//	@Override
	//	public void messageNotificationReceivedHandle(BaseReader reader,
	//			IMessageNotification msg) {
	//		if(reading){
	//			if(mHolder.isConnected()){
	//				if(msg instanceof RXD_TagData){
	////					android.util.Log.i("TagScanActivity", Util.convertByteArrayToHexString(((RXD_TagData)msg).getReceivedMessage().getEPC()));
	////					if(mSettingsCollection.isVoiced()){
	////						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
	////					}
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
	//					bundle.putString("tid", tid);
	//					bundle.putString("userData", userData);
	//					notifyMessage.setData(bundle);
	//					notifyMessage.what = REFRESH;
	//					handler.sendMessage(notifyMessage);
	//				}
	//			}
	//		}
	//	}
}
