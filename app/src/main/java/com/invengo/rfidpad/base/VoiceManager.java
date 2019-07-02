package com.invengo.rfidpad.base;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.utils.Contants;

public class VoiceManager {

	private SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	private SparseIntArray mSoundPoolMap = new SparseIntArray();
	private void initSoundPool(Context context) {
		this.context = context;
		mSoundPoolMap.append(Contants.TAG_SOUND, mSoundPool.load(context, R.raw.tag, 1));
		mSoundPoolMap.append(Contants.SUCCESS_SOUND, mSoundPool.load(context, R.raw.success, 1));
		mSoundPoolMap.append(Contants.ERROR_SOUND, mSoundPool.load(context, R.raw.error, 1));
		mSoundPoolMap.append(Contants.BEEP_SOUND, mSoundPool.load(context, R.raw.beep, 1));
	}
	
	private Context context;
	private static VoiceManager mVoiceManager = null;
	private VoiceManager(Context context){
		initSoundPool(context);
	}
	
	public static synchronized VoiceManager getInstance(Context context){
		if(null == mVoiceManager){
			mVoiceManager = new VoiceManager(context);
		}
		return mVoiceManager;
	}
	
	public void playSound(final int sound, final int loop){
		AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		final float volume = streamVolumeCurrent / streamVolumeMax;
//		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
//			@Override
//			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				mSoundPool.play(mSoundPoolMap.get(sound), volume, volume, 1, loop, 1f);
//			}
//		});
	}
	
}
