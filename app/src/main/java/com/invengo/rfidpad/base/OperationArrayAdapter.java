package com.invengo.rfidpad.base;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.entity.OperationEntity;

public class OperationArrayAdapter extends ArrayAdapter<OperationEntity> {

	private int resourceId;
	public OperationArrayAdapter(Context context, int textViewResourceId,
			List<OperationEntity> objects) {
		super(context, textViewResourceId, objects);
		this.resourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OperationEntity entity = getItem(position);
		
		ViewHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
			holder = new ViewHolder();
//			holder.operationImage = (ImageView) convertView.findViewById(R.id.image_operation);
			holder.operationDescription = (TextView) convertView.findViewById(R.id.text_operation);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
//		holder.operationImage.setImageResource(entity.getImageId());
		holder.operationDescription.setText(entity.getOperationName());
		
		return convertView;
	}
	
	class ViewHolder{
//		ImageView operationImage;
		TextView operationDescription;
	}

}
