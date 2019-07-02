package com.invengo.rfidpad.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

public class ProgressBarManager {

	private ProgressBarManager(){
		
	}
	
	private static ProgressBarManager instance = null;
	
	public static synchronized ProgressBarManager getInstance(){
		if(null == instance){
			instance = new ProgressBarManager();
		}
		return instance;
	}
	
	/**
	 * must in UI thread
	 * 
	 * @param show
	 * @param statusView
	 * @param shortAnimTime
	 */
	public void showProgressBar(final boolean show, final View statusView, int shortAnimTime){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

			statusView.setVisibility(View.VISIBLE);
			statusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							statusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

		} else {
			statusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	public void showProgressBar(final boolean show, final View contentView, final View statusView, int shortAnimTime){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

			statusView.setVisibility(View.VISIBLE);
			statusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							statusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});
			statusView.setBackgroundColor(Color.parseColor("#86222222"));
			
			contentView.setVisibility(View.VISIBLE);
//			contentView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1);
		} else {
			contentView.setVisibility(show ? View.GONE : View.VISIBLE);
			statusView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	/**
	 * must in UI thread
	 * 
	 * @param messageView
	 * @param resid
	 */
	public void setProgressBarMessage(TextView messageView, int resid){
		messageView.setText(resid);
	}
	
	public void setProgressBarMessage(TextView messageView, String msg){
		messageView.setTextColor(Color.RED);
		messageView.setTextSize(20f);
		messageView.setText(msg);
	}
	
}
