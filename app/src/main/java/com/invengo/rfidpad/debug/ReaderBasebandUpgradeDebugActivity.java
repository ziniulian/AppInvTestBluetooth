package com.invengo.rfidpad.debug;

import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.FirmwareUpgrade_800;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
 * XC2910-A-读写器升级
 */
public class ReaderBasebandUpgradeDebugActivity extends PowerManagerActivity {

	private ProgressBarManager mProgressBarManager;
	private View mProgressBarStatusView;
	private TextView mProgressBarMessageView;
	private ReaderOperationBroadcastReceiver mReceiver;

	private EditText mFileText;
	private EditText mFileDataText;
	private ReaderHolder mReaderHolder;
	private static final String TAG = ReaderBasebandUpgradeDebugActivity.class.getSimpleName();
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
		setTitle(R.string.title_reader_debug_baseband_upgrade);

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
				//			attemptQueryVoltage();

				if(!mReaderHolder.isConnected()){
					InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
					break;
				}

				if(null == mSelect){
					InvengoUtils.showToast(this, R.string.toast_reader_debug_baseband_upgrade_file_null);
					break;
				}

				attemptUpgrade();

				break;
			case R.id.menu_reader_debug_upgrade_select_id:
				openFileChooser();
				break;
			default:
				break;
		}
		return true;
	}

	//	private void attemptQueryVoltage(){
	//		if(!mReaderHolder.isConnected()){
	//			InvengoUtils.showToast(this, R.string.toast_reader_disconnected);
	//			return;
	//		}
	//
	//		if(null == mSelect){
	//			InvengoUtils.showToast(this, R.string.toast_reader_debug_baseband_upgrade_file_null);
	//			return;
	//		}
	//
	//		new Thread(new Runnable() {
	//
	//			@Override
	//			public void run() {
	//				SysQuery_800 msg = new SysQuery_800(PARAMETER);
	//				boolean result = mReaderHolder.getCurrentReader().send(msg);
	//				if(result){
	//					if (null != msg.getReceivedMessage()) {
	//						Message message = new Message();
	//						message.what = QUERY_VOLTAGE_SUCCESS;
	//						message.obj = msg.getReceivedMessage();
	//						handler.sendMessage(message);
	//					}else {
	//						Message message = new Message();
	//						message.what = QUERY_VOLTAGE_FAILURE;
	//						handler.sendMessage(message);
	//					}
	//				}else {
	//					Message message = new Message();
	//					message.what = QUERY_VOLTAGE_FAILURE;
	//					handler.sendMessage(message);
	//				}
	//			}
	//		}).start();
	//	}

	private void attemptUpgrade() {
		InvengoLog.i(TAG, "INFO.attemptUpgrade().");
		int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		mProgressBarManager.setProgressBarMessage(mProgressBarMessageView, R.string.progress_bar_reader_debug_baseband_upgrade_message);
		mProgressBarManager.showProgressBar(true, mProgressBarStatusView, shortAnimTime);
		FirmwareUpgrade_800 msg = new FirmwareUpgrade_800(mSelect.getAbsolutePath());
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
						InvengoUtils.showToast(ReaderBasebandUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_voltage_not_enough);
						break;
					}
					attemptUpgrade();
					break;
				case QUERY_VOLTAGE_FAILURE:
					InvengoUtils.showToast(ReaderBasebandUpgradeDebugActivity.this, R.string.toast_reader_debug_upgrade_voltage_not_enough);
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
		//		fileIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
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
			String path = null;
			if("file".equalsIgnoreCase(uri.getScheme())){
				path = uri.getPath();
			}else{
				path = getPath(this, uri);
			}

			InvengoLog.i(TAG, "INFO.select upgrade-file.");
			//			String path = uri.getPath();
			String fileName = String.valueOf(path.subSequence(path.lastIndexOf(File.separator) + 1, path.length()));
			String prefix = fileName.substring(0, fileName.indexOf("."));
			Matcher matcher = mPattern.matcher(prefix);
			//				if(matcher.matches() && fileName.endsWith(SUFFIX)){
			if(fileName.endsWith(SUFFIX)){
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
				InvengoUtils.showToast(ReaderBasebandUpgradeDebugActivity.this, R.string.toast_reader_debug_baseband_upgrade_file_wrong);
			}
		}
	}

	private String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];

				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);

			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);

			}

		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);

		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			uri.getPath();

		}
		return null;
	}

	private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	private boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	private boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	private boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	private class ReaderOperationBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//			//progressBar visiable = false;
			int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			mProgressBarManager.showProgressBar(false, mProgressBarStatusView, shortAnimTime);
			int resultCode = intent.getIntExtra(OperationTask.RESULT_STATUS_CODE_EXTRA, -1);
			if(resultCode == 0){
				InvengoUtils.showToast(ReaderBasebandUpgradeDebugActivity.this, R.string.toast_reader_debug_baseband_upgrade_success);
			}else{
				InvengoUtils.showToast(ReaderBasebandUpgradeDebugActivity.this, R.string.toast_reader_debug_baseband_upgrade_failure);
			}
		}
	}
}
