package com.invengo.rfidpad.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * for inputType = hexString
 */
public class HexTextWatcher implements TextWatcher {

	private Pattern hexPattern = Pattern.compile("^[0-9a-fA-F]+$");
	private int nextStart = 0;
	private EditText mView;
	
	public HexTextWatcher() {
		//
	}
	
	public HexTextWatcher(EditText view){
		this.mView = view;
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String oldText = s.toString();
		if(TextUtils.isEmpty(oldText)){
			return;
		}
		if(before == 0){//input
			nextStart = start + 1;
			Matcher matcher = hexPattern.matcher(s.subSequence(start, nextStart));//new input
			if(matcher.matches()){//hex
				mView.setSelection(nextStart);
//				mEpcDataEditText.setText(oldText.toUpperCase());
			}else{//non-hex
				StringBuffer charBuffer = new StringBuffer();
				for(int i = 0; i < s.length(); i++){
					if(i != start){
						charBuffer.append(s.charAt(i));
					}
				}
				nextStart = start;
				mView.setText(charBuffer.toString());
			}
		}else if(before == 1){//delete
			//do nothing
		}else if(before > 1){//for setText() method
			mView.setSelection(nextStart);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		
	}

}
