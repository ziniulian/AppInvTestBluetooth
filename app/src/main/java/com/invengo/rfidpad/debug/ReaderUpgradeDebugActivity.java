package com.invengo.rfidpad.debug;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.FirmwareUpgrade_ARM9;
import invengo.javaapi.protocol.IRP1.SysQuery_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationTask;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.base.ProgressBarManager;
import com.invengo.rfidpad.base.ReaderHolder;
import com.invengo.rfidpad.utils.InvengoUtils;

/**
 * XC2600-读写器升级
 */
public class ReaderUpgradeDebugActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mFileText;
	private EditText mFileDataText;
	private ReaderHolder mReaderHolder;
	private static final String TAG = ReaderUpgradeDebugActivity.class.getSimpleName();
	private static final int FILE_SELECT_REQUEST_CODE = 0;
	private File mSelect = null;
	private static final String REGEX = "^XC-[\\w]*svn\\d{3}_upg$";
	private static final String SUFFIX = "bin";
	private Pattern mPattern = Pattern.compile(REGEX);
	private static final byte PARAMETER = (byte) 0x87;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug_upgrade);
		setTitle(R.string.title_reader_debug_upgrade);

		mProgressBarManager = ProgressBarManager.getInstance();
		mReaderHolder = ReaderHolder.getInstance();

		mFileText = (EditText) findViewById(R.id.edit_text_reader_debug_upgrade_file);
		mFileDataText = (EditText) findViewById(R.id.edit_reader_debug_upgrade_file_data);
		mProgressBarStatusView = findViewById(R.id.progress_bar_status);
		mProgressBarMessageView = (TextView) findViewById(R.id.progress_bar_status_message);

		InvengoLog.i(TAG, "INFO.onCreate()");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_reader_debug_upgrade, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_reader_debug_upgrade_upgrade_id:
				attemptQueryVoltage();
				break;
			case R.id.menu_reader_debug_upgrade_select_id:
				openFileChooser();
				break;
			default:
				break;
		}
		return true;
	}

	private void attemptQueryVoltage(){
		if(!mReaderHolder.isConnected()){
			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
			return;
		}

		if(null == mSelect){
			InvengoUtils.showToast(this, R.string.toast_reader_debug_upgrade_file_null);
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				SysQuery_800 msg = new SysQuery_800(PARAMETER);
				boolean result = mReaderHolder.getCurrentReader().send(msg);
				if(result){
					if (null != msg.getReceivedMessage()) {
						Message message = new Message();
						message.what = QUERY_VOLTAGE_SUCCESS;
						message.obj = msg.getReceivedMessage();
						handler.sendMessage(message);
					}else {
						Message message = new Message();
						message.what = QUERY_VOLTAGE_FAILURE;
						handler.sendMessage(message);
					}
				}else {
					Message message = new Message();
					message.what = QUERY_VOLTAGE_FAILURE;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	private void attemptUpgrade() {
		InvengoLog.i(TAG, "INFO.attemptUpgrade().");
		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_upgrade_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		FirmwareUpgrade_ARM9 msg = new FirmwareUpgrade_ARM9(mSelect.getAbsolutePath());
		OperationTask task = new OperationTask(this);
		task.execute(msg);
	}

	private static final int QUERY_VOLTAGE_SUCCESS = 0;
	private static final int QUERY_VOLTAGE_FAILURE = 1;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case QUERY_VOLTAGE_SUCCESS:
					SysQuery800ReceivedInfo response = (SysQuery800ReceivedInfo) msg.obj;
					byte[] voltageByte = response.getQueryData();
					int voltage = voltageByte[0] & 0xFF;
					if(voltage <= 30){//电量不足
						InvengoUtils.showToast(ReaderUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_voltage_not_enough);
						break;
					}
					attemptUpgrade();
					break;
				case QUERY_VOLTAGE_FAILURE:
					InvengoUtils.showToast(ReaderUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_voltage_not_enough);
					break;
				default:
					break;
			}
		};
	};

	@Override
	protected void onResume() {
		super.onResume();
		initializeData();
	}

	private void initializeData() {
		IntentFilter filter = new IntentFilter(OperationTask.ACTION_TAG_TASK_BROADCAST);
		mReceiver = new ReaderOperationBroadcastReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	protected void openFileChooser() {
		Intent fileIntent = new Intent();
		fileIntent.setAction(Intent.ACTION_GET_CONTENT);
		fileIntent.setType("application/octet-stream");
		fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		if(fileIntent.resolveActivity(getPackageManager()) != null){
			startActivityForResult(Intent.createChooser(fileIntent, "Select file"), FILE_SELECT_REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(FILE_SELECT_REQUEST_CODE == requestCode && resultCode == RESULT_OK){
			Uri uri = data.getData();
			if("file".equals(uri.getScheme())){
				InvengoLog.i(TAG, "INFO.select upgrade-file.");
				String path = uri.getPath();
				String fileName = String.valueOf(path.subSequence(path.lastIndexOf(File.separator) + 1, path.length()));
				String prefix = fileName.substring(0, fileName.indexOf("."));
				Matcher matcher = mPattern.matcher(prefix);
				if(matcher.matches() && fileName.endsWith(SUFFIX)){
					mSelect = new File(path);
					if(mSelect.exists()){
						mFileText.setText(path);
						FileInputStream fis = null;
						try {
							fis = new FileInputStream(path);
							int count = fis.available();
							byte[] bs = new byte[count];
							fis.read(bs, 0, count);
							StringBuffer sb = new StringBuffer(count);
							for(int i = 0; i < bs.length; i++){
								sb.append(Util.convertByteToHexWordString(bs[i]));
								sb.append(" ");
								if((i % 10) == 0 && i > 0){
									sb.append("\n");
								}
							}
							mFileDataText.setText(sb.toString());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}finally{
							if(null != fis){
								try {
									fis.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}else {
						mSelect = null;
					}
				}else{
					InvengoUtils.showToast(ReaderUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_file_wrong);
				}
			}
		}
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
//			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(ReaderUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_success);
			}else{
				InvengoUtils.showToast(ReaderUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_failure);
			}
		}
	}
}
