package com.invengo.rfidpad.scan;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.invengo.rfidpad.R;
import com.invengo.rfidpad.base.OperationArrayAdapter;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.entity.OperationEntity;
import com.invengo.rfidpad.scan.tag6b.TagUserdataReadActivity;
import com.invengo.rfidpad.scan.tag6b.TagUserdataWriteActivity;

/**
 * 6B操作
 */
public class Tag6BOperationActivity extends PowerManagerActivity {

	private TextView mTagData;
	private ListView mOperationListView;
	private List<OperationEntity> mList = new ArrayList<OperationEntity>();
	public static final String TAG_6B_OPERATION_DATA = "TAG_6B_OPERATION_DATA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6b_operation);
		setTitle(R.string.title_tag_6b_operation);

		mTagData = (TextView) findViewById(R.id.text_tag_6b_operation_tag_data);

		initializeListEntity();
		mOperationListView = (ListView) findViewById(R.id.list_tag_6b_operation_detail);
		OperationArrayAdapter adapter = new OperationArrayAdapter(this, R.layout.list_reader_main, mList);
		mOperationListView.setAdapter(adapter);
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				OperationEntity entity = (OperationEntity) mOperationListView.getAdapter().getItem(position);
				Intent newIntent = new Intent(Tag6BOperationActivity.this, entity.getCls());
				newIntent.putExtra(TAG_6B_OPERATION_DATA, mTagData.getText().toString());
				startActivity(newIntent);
			}

		});
	}

	private void initializeListEntity() {
		OperationEntity readUserdataEntity = new OperationEntity(getString(R.string.text_tag_6b_operation_read_userdata));
		readUserdataEntity.setCls(TagUserdataReadActivity.class);
		mList.add(readUserdataEntity);
		OperationEntity writeUserDataEntity = new OperationEntity(getString(R.string.text_tag_6b_operation_write_userdata));
		writeUserDataEntity.setCls(TagUserdataWriteActivity.class);
		mList.add(writeUserDataEntity);
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

	@Override
	protected void onResume() {
		super.onResume();
		initializeTagData();
	}

	private void initializeTagData() {
		Intent intent = getIntent();
		String data = intent.getStringExtra(TagScanActivity.TAG_DATA);
		mTagData.setText(data);
	}

}
