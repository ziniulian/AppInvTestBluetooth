package com.invengo.rfidpad.base;

import invengo.javaapi.protocol.IRP1.Reader;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class ReaderOperationTask extends AsyncTask<Reader, Void, Boolean> {

	public static final String ACTION_TASK_BROADCAST = "com.invengo.rfidpad.core.ReaderOperationTask.READER_CONNECT";
	public static final String RESULT = "RESULT";
	private Context mContext;
	public ReaderOperationTask(Context context) {
		this.mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(Reader... params) {
		Reader reader = params[0];
		if(!reader.connect()){
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Intent resultIntent = new Intent(ACTION_TASK_BROADCAST);
		resultIntent.putExtra(RESULT, result);
		mContext.sendBroadcast(resultIntent);
	}
}
