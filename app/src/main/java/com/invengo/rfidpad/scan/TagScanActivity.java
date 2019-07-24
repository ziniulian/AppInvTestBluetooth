package com.invengo.rfidpad.scan;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.GBInventoryTag;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.base.VoiceManager;
import com.invengo.rfidpad.entity.TagScanInfoEntity;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

public class TagScanActivity extends PowerManagerActivity {

	private ReaderHolder mReaderHolder;
	private TagScanSettingsCollection mSettingsCollection;
	private VoiceManager mVoiceManager;
	private View mTagNumberLayout;
	private TextView mTagNumberView;
	private ListView mTagInfoListView;
	private List<TagScanInfoEntity> mList = new ArrayList<TagScanInfoEntity>();
	public static final String TAG_DATA = "TAG_DATA1";
	private static final String TAG_6C = "6C";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_scan);
		setTitle(R.string.title_tag_scan);

		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mReaderHolder = ReaderHolder.getInstance();
		mVoiceManager = VoiceManager.getInstance(getApplicationContext());

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
				TagScanInfoEntity selected = (TagScanInfoEntity) mTagInfoListView.getAdapter().getItem(position);
				String tagType = selected.getType();
				Intent newIntent = null;
				if(tagType.startsWith(TAG_6C)){//6C
					newIntent = new Intent(TagScanActivity.this, Tag6COperationActivity.class);
					String transmitData = "";
					if(mSettingsCollection.getEpcChecked() == Contants.CHECKED || mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
						transmitData = selected.getEpc();
					}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
						transmitData = selected.getTid();
					}

					newIntent.putExtra(TAG_DATA, transmitData);
				}else{//6B
					newIntent = new Intent(TagScanActivity.this, Tag6BOperationActivity.class);
					String transmitData = selected.getTid();//ID Data
					newIntent.putExtra(TAG_DATA, transmitData);
				}

				startActivity(newIntent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		//		if(null != mReaderHolder.getCurrentReader()){
		//			mReaderHolder.getCurrentReader().onMessageNotificationReceived.add(this);//注册
		//		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//		if(null != mReaderHolder.getCurrentReader()){
		//			mReaderHolder.getCurrentReader().onMessageNotificationReceived.remove(this);//取消注册
		//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopScanTag(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_scan_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_tag_scan_detail_start:
				if(!mReaderHolder.isConnected()){
					InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
					break;
				}
				if(!reading){//start reading tags
					if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){
						startScanGBTag();
					}else{
						startScanTag();
					}
					item.setTitle(R.string.menu_tag_scan_detail_stop_title);
				}else{//stop reading tags
					stopScanTag();
					item.setTitle(R.string.menu_tag_scan_detail_start_title);
				}
				break;
			//		case R.id.menu_tag_scan_detail_stop:
			//			stopScanTag();
			//			break;
			case R.id.menu_tag_scan_detail_clear:
				clearScanTag();
				break;
			default:
				break;
		}
		return true;
	};

	private void startScanGBTag() {
		reading = true;

		int antenna = mSettingsCollection.getAntenna();
		int target = 0;
		int session = 0;
		int condition = 3;

		final GBInventoryTag message = new GBInventoryTag((byte) antenna, target, session, condition);

		new Thread(new Runnable() {

			@Override
			public void run() {
				mReaderHolder.getCurrentReader().send(message);
				Message message = new Message();
				message.what = START;
				scanHandle.sendMessage(message);
			}
		}).start();

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
				mReaderHolder.getCurrentReader().send(new PowerOff_800());
			}
		});
	}

	private void stopScanTag() {
		//		if(!reading){
		//			return;
		//		}
		//		if(!mReaderHolder.isConnected()){
		//			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
		//			return;
		//		}
		reading = false;
		new Thread(new Runnable() {

			@Override
			public void run() {
				mReaderHolder.getCurrentReader().send(new PowerOff_800());
				Message message = new Message();
				message.what = STOP;
				scanHandle.sendMessage(message);
			}
		}).start();
	}

	private void startScanTag() {
		//		if(reading){
		//			return;
		//		}
		//		if(!mReaderHolder.isConnected()){
		//			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
		//			return;
		//		}
		reading = true;
		//		String antennaEnabled = "1000" + (mSettingsCollection.isAntennaFour() ? "1" : "0")
		//				+ (mSettingsCollection.isAntennaThree() ? "1" : "0")
		//				+ (mSettingsCollection.isAntennaTwo() ? "1" : "0")
		//				+ (mSettingsCollection.isAntennaOne() ? "1" : "0");

		int antenna = mSettingsCollection.getAntenna();
		int q = mSettingsCollection.getQ();
		boolean loop = mSettingsCollection.isLoop();

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
				bank = ReadMemoryBank.EPC_TID_UserData_Reserved_6C_ID_UserData_6B;//读ID&Userdata
			}
		}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){//6C_6B
			bank = ReadMemoryBank.EPC_TID_UserData_Reserved_6C_ID_UserData_6B;//6C6B通用读
		}
		final ReadTag msg = new ReadTag(bank);

		msg.setAntenna((byte) antenna);
		if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){//6C
			msg.setQ((byte) q);
			msg.setLoop(loop);
			if(mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
				msg.setTidLen((byte) mSettingsCollection.getTidLen());
				msg.setUserDataPtr_6C((byte) mSettingsCollection.getUserDataAddress());
				msg.setUserDataLen_6C((byte) mSettingsCollection.getUserDataLen());
			}
		}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){
			if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){
				msg.setQ((byte) q);
				msg.setLoop(loop);
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
			msg.setLoop(loop);
			msg.setTidLen((byte) mSettingsCollection.getTidLen6C6B());
			msg.setUserDataPtr_6C((byte) mSettingsCollection.getUserDataAddress6C6B());
			msg.setUserDataLen_6C((byte) mSettingsCollection.getUserDataLen6C6B());
			msg.setUserDataPtr_6B((byte) mSettingsCollection.getUserDataAddress6C6B());
			msg.setUserDataLen_6B((byte) mSettingsCollection.getUserDataLen6C6B());
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				mReaderHolder.getCurrentReader().send(msg);
				Message message = new Message();
				message.what = START;
				scanHandle.sendMessage(message);
			}
		}).start();
	}

	private boolean reading = false;
	private static final int START = 0;
	private static final int STOP = 1;
	private static final int REFRESH = 2;
	private Handler scanHandle = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case START:
					InvengoUtils.showToast(TagScanActivity.this, R.string.toast_tag_scan_start);
					break;
				case STOP:
					InvengoUtils.showToast(TagScanActivity.this, R.string.toast_tag_scan_stop);
					break;
				case REFRESH:
					Bundle data = msg.getData();
					String type = data.getString("type").toUpperCase().equals(TAG_6C) ? getString(R.string.label_tag_scan_type_6c) : getString(R.string.label_tag_scan_type_6b);
					String epc = data.getString("epc");
					String tid = data.getString("tid");
					String userData = data.getString("userData");
					boolean isExists = false;
					for(TagScanInfoEntity entity : mList){
						if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){//6C
							if(mSettingsCollection.getEpcChecked() == Contants.CHECKED){
								String oldEpc = entity.getEpc();
								if(oldEpc.equals(epc)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									break;
								}
							}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
								String oldTid = entity.getTid();
								if(oldTid.equals(tid)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									break;
								}
							}else if(mSettingsCollection.getUserdataChecked() == Contants.CHECKED){
								String oldData = entity.getEpc() + entity.getTid() + entity.getUserdata();
								if((epc + tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									break;
								}
							}
						}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){//6B
							if(mSettingsCollection.getId6BChecked() == Contants.CHECKED){
								String oldTid = entity.getTid();
								if(oldTid.equals(tid)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									break;
								}
							}else if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){
								String oldData = entity.getTid() + entity.getUserdata();
								if((epc + tid + userData).equals(oldData)){
									isExists = true;
									int oldNumber = entity.getNumber();
									entity.setNumber(oldNumber + 1);
									break;
								}
							}
						}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){//6C & 6B
							String oldData = entity.getEpc() + entity.getTid() + entity.getUserdata();
							if((epc + tid + userData).equals(oldData)){
								isExists = true;
								int oldNumber = entity.getNumber();
								entity.setNumber(oldNumber + 1);
								break;
							}
						}else if(mSettingsCollection.getVisibility_GB() == Contants.CHECKED){//GB
							String oldData = entity.getEpc();
							if(epc.equals(oldData)){
								isExists = true;
								int oldNumber = entity.getNumber();
								entity.setNumber(oldNumber + 1);
								break;
							}
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
					mTagNumberView.setText(String.valueOf(mList.size()));
					break;
				default:
					break;
			}

		};
	};

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
			if(mSettingsCollection.getVisibility_6C() == Contants.CHECKED){
				if (mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
					holder.epcLayout.setVisibility(mSettingsCollection.getUserdataChecked());
					holder.tidLayout.setVisibility(mSettingsCollection.getUserdataChecked());
					holder.userdataLayout.setVisibility(mSettingsCollection.getUserdataChecked());
				}else{
					holder.epcLayout.setVisibility(mSettingsCollection.getEpcChecked());
					holder.tidLayout.setVisibility(mSettingsCollection.getTidChecked());
					holder.userdataLayout.setVisibility(mSettingsCollection.getUserdataChecked());
				}
			}else if(mSettingsCollection.getVisibility_6B() == Contants.CHECKED){
				if(mSettingsCollection.getUserdata6BChecked() == Contants.CHECKED){
					holder.epcLayout.setVisibility(View.GONE);
					holder.tidLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
					holder.userdataLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
				}else{
					holder.epcLayout.setVisibility(View.GONE);
					holder.tidLayout.setVisibility(mSettingsCollection.getId6BChecked());
					holder.userdataLayout.setVisibility(mSettingsCollection.getUserdata6BChecked());
				}
			}else if(mSettingsCollection.getVisibility_6C_6B() == Contants.CHECKED){
				holder.epcLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
				holder.tidLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
				holder.userdataLayout.setVisibility(mSettingsCollection.getVisibility_6C_6B());
			}

			return convertView;
		}

		class ViewHolder{
			LinearLayout epcLayout;
			LinearLayout tidLayout;
			LinearLayout userdataLayout;
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
			if(mReaderHolder.isConnected()){
				if(msg instanceof RXD_TagData){
					//					android.util.Log.i("TagScanActivity", Util.convertByteArrayToHexString(((RXD_TagData)msg).getReceivedMessage().getEPC()));
					if(mSettingsCollection.isVoiced()){
						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
					}
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
					scanHandle.sendMessage(notifyMessage);
				}if(msg instanceof GBInventoryTag){
					if(mSettingsCollection.isVoiced()){
						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
					}
					GBInventoryTag data = (GBInventoryTag) msg;
					String type = "GB";
					String tagData = Util.convertByteArrayToHexString(data.getReceivedMessage().getTagData());
					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("epc", tagData);
					bundle.putString("tid", "");
					bundle.putString("userData", "");
					notifyMessage.setData(bundle);
					notifyMessage.what = REFRESH;
					scanHandle.sendMessage(notifyMessage);

				}
			}
		}
	}

	//	@Override
	//	public void messageNotificationReceivedHandle(BaseReader reader,
	//			IMessageNotification msg) {
	//		if(reading){
	//			if(mReaderHolder.isConnected()){
	//				if(msg instanceof RXD_TagData){
	////					android.util.Log.i("TagScanActivity", Util.convertByteArrayToHexString(((RXD_TagData)msg).getReceivedMessage().getEPC()));
	//					if(mSettingsCollection.isVoiced()){
	//						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
	//					}
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
	//					scanHandle.sendMessage(notifyMessage);
	//				}if(msg instanceof GBInventoryTag){
	//					if(mSettingsCollection.isVoiced()){
	//						mVoiceManager.playSound(Contants.TAG_SOUND, 0);
	//					}
	//					GBInventoryTag data = (GBInventoryTag) msg;
	//					String type = "GB";
	//					String tagData = Util.convertByteArrayToHexString(data.getReceivedMessage().getTagData());
	//					Message notifyMessage = new Message();
	//					Bundle bundle = new Bundle();
	//					bundle.putString("type", type);
	//					bundle.putString("epc", tagData);
	//					bundle.putString("tid", "");
	//					bundle.putString("userData", "");
	//					notifyMessage.setData(bundle);
	//					notifyMessage.what = REFRESH;
	//					scanHandle.sendMessage(notifyMessage);
	//
	//				}
	//			}
	//		}
	//	}

}
