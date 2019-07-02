package com.invengo.rfidpad.debug;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff_800;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.base.StaticReadTimer;
import com.invengo.rfidpad.base.VoiceManager;
import com.invengo.rfidpad.entity.TagScanInfoEntity;
import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * 静态读EPC调试
 */
public class ReaderStaticDebugActivity extends PowerManagerActivity {

	private ReaderHolder mHolder;
	private TagScanSettingsCollection mSettingsCollection;
	private VoiceManager mVoiceManager;
	private CheckBox mTimeCheckBox;
	private EditText mTimeEditText;
	private CheckBox mNumberCheckBox;
	private EditText mNumberEditText;
	private boolean isReading = false;
	private MenuItem mStartMenuItem;
	private StaticReadTimer mReadTimer;
	private StaticReadTimer mCountTimer;
	private int mNumber;
	private View mCountView;
	private TextView mCountTextView;

	private ListView mTagInfoListView;
	private List<TagScanInfoEntity> mList = new ArrayList<TagScanInfoEntity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_static_read);
		setTitle(R.string.title_reader_debug_static_read);

		mHolder = ReaderHolder.getInstance();
		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mVoiceManager = VoiceManager.getInstance(this);

		mTimeCheckBox = (CheckBox) findViewById(R.id.checkBox_reader_debug_static_read_time);
		mTimeEditText = (EditText) findViewById(R.id.edit_reader_debug_static_read_time);
		mNumberCheckBox = (CheckBox) findViewById(R.id.checkBox_reader_debug_static_read_number);
		mNumberEditText = (EditText) findViewById(R.id.edit_reader_debug_static_read_number);
		mCountView = findViewById(R.id.layout_reader_debug_static_read_count);
		mCountTextView = (TextView) findViewById(R.id.text_reader_debug_static_read_count);

		mTagInfoListView = (ListView) findViewById(R.id.list_tag_static_read_detail);
		TagInfoArrayAdapter adapter = new TagInfoArrayAdapter(this, R.layout.list_tag_scan_detail_item, mList);
		mTagInfoListView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_debug_static_read, menu);
		this.mStartMenuItem = menu.getItem(0);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
			case R.id.menu_reader_debug_static_read_start_id:
				if(!mHolder.isConnected()){
					InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
					break;
				}
				if(!isReading){
					if(!mTimeCheckBox.isChecked() && !mNumberCheckBox.isChecked()){
						InvengoUtils.showToast(this, R.string.toast_reader_debug_static_read_select);
						break;
					}
					if(mTimeCheckBox.isChecked()){
						if(TextUtils.isEmpty(mTimeEditText.getText().toString())){
							InvengoUtils.showToast(this, R.string.toast_reader_debug_static_read_times);
							break;
						}
					}
					if(mNumberCheckBox.isChecked()){
						if(TextUtils.isEmpty(mNumberEditText.getText().toString())){
							InvengoUtils.showToast(this, R.string.toast_reader_debug_static_read_numbers);
							break;
						}
					}

					clearScanTag();
					startStaticReadDebug();
					item.setTitle(R.string.menu_reader_debug_static_read_stop_title);
				}else{
					stopStaticReadDebug();
					item.setTitle(R.string.menu_reader_debug_static_read_start_title);
				}
				break;
			default:
				break;
		}
		return true;
	}

	private void clearScanTag() {
		mCountTextView.setText("0s");
		mList.clear();
		((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
	}

	/**
	 * 停止读卡
	 */
	private void stopStaticReadDebug() {
		isReading = false;
		if(null != this.mReadTimer){
			if(this.mReadTimer.getStatus() == StaticReadTimer.RUNNING){
				this.mReadTimer.cancel();
			}
			this.mReadTimer = null;
		}
		if(null != this.mCountTimer){
			if(this.mCountTimer.getStatus() == StaticReadTimer.RUNNING){
				this.mCountTimer.cancel();
			}
			this.mCountTimer = null;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mHolder.isConnected()){
					mHolder.getCurrentReader().send(new PowerOff_800());
					Message message = new Message();
					message.what = MENU_REFRESH;
					message.obj = getString(R.string.menu_reader_debug_static_read_start_title);
					handler.sendMessage(message);
				}

			}
		}).start();
	}

	/**
	 * 开始读卡
	 */
	private long mStartTime = 0;
	private void startStaticReadDebug() {
		isReading = true;
		String times = "";
		int maxTime = -1;
		if(mTimeCheckBox.isChecked()){
			times = mTimeEditText.getText().toString();
			maxTime = Integer.parseInt(times);
		}

		String numbers = "";
		if(mNumberCheckBox.isChecked()){
			numbers = mNumberEditText.getText().toString();
			this.mNumber = Integer.parseInt(numbers);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				if(mHolder.isConnected()){
					ReadTag message = new ReadTag(ReadMemoryBank.EPC_6C);
					message.setAntenna((byte) mSettingsCollection.getAntenna());
					mStartTime = System.currentTimeMillis();
					mHolder.getCurrentReader().send(message);
				}
			}
		}).start();

		if(mTimeCheckBox.isChecked()){
			//maxTime == 0,默认无限次读取
			this.mCountTimer = new StaticReadTimer(new TimeCountTask(maxTime), 1000l);
			this.mCountTimer.start(1000l);
//			this.mReadTimer = new StaticReadTimer(new TerminationTask(), maxTime * 1000l);//Timer接收毫秒为单位的时间
//			this.mReadTimer.start();
		}
	}

	private class TimeCountTask extends TimerTask{//时间计数器

		private int period = 0;
		private int maxTime = 0;
		public TimeCountTask(int maxTime){
			this.maxTime = maxTime;
		}

		@Override
		public void run() {
			period += 1;
//			Log.i(getLocalClassName(), String.valueOf(period));
			Message message = new Message();
			message.what = COUNTER_REFRESH;
			message.obj = String.format("%ss", period);
			handler.sendMessage(message);
			if(maxTime != 0){//maxTime == 0,默认无限次读取
				if(period == this.maxTime){
					stopStaticReadDebug();
				}
			}
		}

	}

//	private class TerminationTask extends TimerTask{//满足读取时间后终止读卡
//
//		@Override
//		public void run() {
//			stopStaticReadDebug();
//		}
//
//	}

	@Override
	protected void onResume() {
		super.onResume();
//		addListener();
//		if(null != mHolder.getCurrentReader()){
//			mHolder.getCurrentReader().onMessageNotificationReceived.add(this);//注册
//		}
	}

	private void addListener() {
		mTimeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mTimeCheckBox.isChecked()){
					mCountView.setVisibility(View.VISIBLE);
				}else{
					mCountView.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if(null != mHolder.getCurrentReader()){
//			mHolder.getCurrentReader().onMessageNotificationReceived.remove(this);//注册
//		}
	}

	private static final int MENU_REFRESH = 0;
	private static final int LIST_REFRESH = 1;
	private static final int COUNTER_REFRESH = 2;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case LIST_REFRESH:
					Bundle data = msg.getData();
					String type = getString(R.string.label_tag_scan_type_6c);
					String epc = data.getString("epc");
					boolean isExists = false;
					for(TagScanInfoEntity entity : mList){
						String oldData = entity.getEpc();
						if(epc.equals(oldData)){
							isExists = true;
							int oldNumber = entity.getNumber();
							entity.setNumber(oldNumber + 1);
							break;
						}
					}

					//在mNumberCheckBox勾选的情况下，读到限制的标签张数后则停止读卡
					if(mNumberCheckBox.isChecked()){
						if(mList.size() >= mNumber){
							long endTime = System.currentTimeMillis();
							mCountTextView.setText(String.format("%sms", endTime - mStartTime));
							stopStaticReadDebug();
							break;
						}
					}

					if(!isExists){
						TagScanInfoEntity newEntity = new TagScanInfoEntity();
						newEntity.setType(type);
						newEntity.setEpc(epc);
						newEntity.setNumber(1);
						mList.add(newEntity);
					}
					((TagInfoArrayAdapter)mTagInfoListView.getAdapter()).notifyDataSetChanged();
					break;
				case MENU_REFRESH:
					String title = (String) msg.obj;
					mStartMenuItem.setTitle(title);
					break;
				case COUNTER_REFRESH:
					String counter = (String) msg.obj;
					mCountTextView.setText(counter);
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
//					android.util.Log.i("TagScanActivity", Util.convertByteArrayToHexString(((RXD_TagData)msg).getReceivedMessage().getEPC()));
					if(mSettingsCollection.isVoiced()){
						mVoiceManager.playSound(Contants.SUCCESS_SOUND, 0);
					}
					RXD_TagData data = (RXD_TagData) msg;
					String type = data.getReceivedMessage().getTagType();
					String epc = Util.convertByteArrayToHexString(data.getReceivedMessage().getEPC());

					Message notifyMessage = new Message();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					bundle.putString("epc", epc);
					notifyMessage.setData(bundle);
					notifyMessage.what = LIST_REFRESH;
					handler.sendMessage(notifyMessage);
				}
			}
		}
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
				holder.barcodeLayout = (LinearLayout) convertView.findViewById(R.id.layout_barcode);

				holder.typeView = (TextView) convertView.findViewById(R.id.text_tag_scan_type);
				holder.timesView = (TextView) convertView.findViewById(R.id.text_tag_scan_times);
				holder.epcView = (TextView) convertView.findViewById(R.id.text_tag_scan_epc);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.typeView.setText(entity.getType());
			holder.timesView.setText(String.valueOf(entity.getNumber()));
			holder.epcView.setText(entity.getEpc());
			holder.epcLayout.setVisibility(View.VISIBLE);
			holder.tidLayout.setVisibility(View.GONE);
			holder.userdataLayout.setVisibility(View.GONE);
			holder.barcodeLayout.setVisibility(View.GONE);

			return convertView;
		}

		class ViewHolder{
			LinearLayout epcLayout;
			LinearLayout tidLayout;
			LinearLayout userdataLayout;
			LinearLayout barcodeLayout;
			TextView typeView;
			TextView timesView;
			TextView epcView;
		}
	}

}
