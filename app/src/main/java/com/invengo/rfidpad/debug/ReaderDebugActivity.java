package com.invengo.rfidpad.debug;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationArrayAdapter;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.entity.OperationEntity;

public class ReaderDebugActivity extends PowerManagerActivity {

	private ListView mOperationListView;
	private List<OperationEntity> mList = new ArrayList<OperationEntity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader_debug);
		setTitle(R.string.title_reader_debug_detail);
		
		initializeListEntity();
		mOperationListView = (ListView) findViewById(R.id.list_reader_debug_detail);
		OperationArrayAdapter adapter = new OperationArrayAdapter(this, R.layout.list_reader_main, mList);
		mOperationListView.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		addListener();
	}

	private void addListener() {
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				OperationEntity entity = (OperationEntity) mOperationListView.getAdapter().getItem(position);
				Intent newIntent = new Intent(ReaderDebugActivity.this, entity.getCls());
				startActivity(newIntent);
			}
		});
	}
	
	private void initializeListEntity() {
		OperationEntity tagFilterEntity = new OperationEntity(getString(R.string.title_reader_debug_repeat_rw));
		tagFilterEntity.setCls(RepeatRWDebugActivity.class);
		mList.add(tagFilterEntity);
//		OperationEntity writeUserDataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_write_userdata));
//		writeUserDataEntity.setCls(TagUserdataWriteActivity.class);
//		mList.add(writeUserDataEntity);
//		OperationEntity lockUserdataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_lock));
//		lockUserdataEntity.setCls(TagUserdataLockActivity.class);
//		mList.add(lockUserdataEntity);
//		OperationEntity queryLockStatusEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_lock_status_query));
//		queryLockStatusEntity.setCls(TagLockStatusQueryActivity.class);
//		mList.add(queryLockStatusEntity);
//		OperationEntity readNonFixedUserdataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_non_fixed__read_userdata));
//		readNonFixedUserdataEntity.setCls(TagNonFixedUserdataReadActivity.class);
//		mList.add(readNonFixedUserdataEntity);
//		OperationEntity writeNonFixedUserdataEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_6b_operation_non_fixed_write_userdata));
//		writeNonFixedUserdataEntity.setCls(TagNonFixedUserdataWriteActivity.class);
//		mList.add(writeNonFixedUserdataEntity);
	}
	
	
}
