package com.invengo.rfidpad.base;

import invengo.javaapi.core.ReceivedInfo;
import invengo.javaapi.protocol.IRP1.BaseMessage;
import invengo.javaapi.protocol.IRP1.Reader;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.invengo.rfidpad.scan.TagScanSettingsCollection;
import com.invengo.rfidpad.utils.Contants;

public class OperationTask extends AsyncTask<BaseMessage, Void, BaseMessage> {

	public static final String ACTION_TAG_TASK_BROADCAST = "com.invengo.rfidpad.base.TagOperationTask.TAG_OPERATION";
	public static final String RECEIVED_INFO_EXTRA = "com.invengo.rfidpad.base.RECEIVED_INFO_EXTRA";
	public static final String RESULT_STATUS_CODE_EXTRA = "com.invengo.rfidpad.base.RESULT_STATUS_CODE_EXTRA";
	private Context mContext;
	private ReaderHolder mHolder;
	private TagScanSettingsCollection mSettingsCollection;
	private VoiceManager mVoiceManager;
	public OperationTask(Context context) {
		this.mContext = context;
		mHolder = ReaderHolder.getInstance();
		mSettingsCollection = TagScanSettingsCollection.getInstance();
		mVoiceManager = VoiceManager.getInstance(context);
	}
	
	@Override
	protected BaseMessage doInBackground(BaseMessage... params) {
		if(mHolder.isConnected()){
			BaseMessage msg = params[0];
			Reader reader = mHolder.getCurrentReader();
			if(reader.send(msg)){
				return msg;
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(BaseMessage result) {
		ReceivedInfo receivedInfo;
		int resultCode = -1;
		if(null == result){
			if(mSettingsCollection.isVoiced()){
				mVoiceManager.playSound(Contants.ERROR_SOUND, 0);
			}
			receivedInfo = null;
		}else{
			if(mSettingsCollection.isVoiced()){
				mVoiceManager.playSound(Contants.SUCCESS_SOUND, 0);
			}
			resultCode = result.getStatusCode();
			receivedInfo = result.getReceivedMessage();
		}
		Intent broadcastIntent = new Intent(ACTION_TAG_TASK_BROADCAST);
		broadcastIntent.putExtra(RECEIVED_INFO_EXTRA, receivedInfo);
		broadcastIntent.putExtra(RESULT_STATUS_CODE_EXTRA, resultCode);
		mContext.sendBroadcast(broadcastIntent);
	}

}
