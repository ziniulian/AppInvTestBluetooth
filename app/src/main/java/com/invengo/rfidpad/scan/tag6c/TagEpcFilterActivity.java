package com.invengo.rfidpad.scan.tag6c;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.EpcFilter_6C;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

public class TagEpcFilterActivity extends PowerManagerActivity {

	private ReaderHolder mHolder;
	private ProgressBarManager mProgressBarManager;
	private TagOperationBroadcastReceiver mReceiver;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private EditText mEpcEditText;
	private EditText mEpcMaskEditText;
	private boolean cancel = false;
	private boolean configurate = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation_epc_filter);
		setTitle(R.string.text_tag_operation_epc_filter);
		
		mHolder = ReaderHolder.getInstance();
		mProgressBarManager = ProgressBarManager.getInstance();
		
		mEpcEditText = (EditText) findViewById(R.id.edit_tag_operation_epc_filter_epc);
		mEpcMaskEditText = (EditText) findViewById(R.id.edit_tag_operation_epc_filter_epc_mask);
		
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new TagOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tag_operation_epc_filter, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int selected = item.getItemId();
		switch (selected) {
		case R.id.menu_tag_operation_epc_filter_configurate_id:
			configEpcFilter();
			break;
		case R.id.menu_tag_operation_epc_filter_cancle_id:
			cancelEpcFilter();
			break;
		default:
			break;
		}
		return true;
	}
	
	private void cancelEpcFilter() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		if(configurate || cancel){
			return;
		}
		
		cancel = true;
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_epc_filter_cancel_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		EpcFilter_6C msg = new EpcFilter_6C((byte) 0x01, null, null);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private void configEpcFilter() {
		if(!mHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}
		
		if(configurate || cancel){
			return;
		}
		
		configurate = true;
		String epc = mEpcEditText.getText().toString();
		String mask = mEpcMaskEditText.getText().toString();
		if(TextUtils.isEmpty(epc)){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_epc_filter_epc);
			return;
		}
		if(TextUtils.isEmpty(mask)){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_epc_filter_epc_mask);
			return;
		}
		if(epc.length() > 24){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_epc_filter_epc_out_of_length);
			return;
		}
		if(mask.length() > 24){
			InvengoUtils.showToast(this, R.string.toast_tag_operation_epc_filter_epc_mask_out_of_length);
			return;
		}

		byte[] epcData = Util.convertHexStringToByteArray(epc);
		byte[] epcMaskData = Util.convertHexStringToByteArray(mask);
		
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_epc_filter_configurate_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		EpcFilter_6C msg = new EpcFilter_6C((byte) 0x00, epcData, epcMaskData);
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private class TagOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(cancel){
				cancel = false;
				if(resultCode == 0){
					InvengoUtils.showToast(TagEpcFilterActivity.this, R.string.toast_tag_operation_epc_filter_cancel_success);
				}else{
					InvengoUtils.showToast(TagEpcFilterActivity.this, R.string.toast_tag_operation_epc_filter_cancel_failure);
				}
				return;
			}
			if(configurate){
				configurate = false;
				if(resultCode == 0){
					InvengoUtils.showToast(TagEpcFilterActivity.this, R.string.toast_tag_operation_epc_filter_configurate_success);
				}else{
					InvengoUtils.showToast(TagEpcFilterActivity.this, R.string.toast_tag_operation_epc_filter_configurate_failure);
				}
			}
		}
	}
	
}
