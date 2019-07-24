package com.invengo.rfidpad.scan;

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
import com.invengo.rfidpad.base.ReaderMainActivity;
import com.invengo.rfidpad.entity.OperationEntity;
import com.invengo.rfidpad.scan.taggb.GBTagInactivateActivity;
import com.invengo.rfidpad.scan.taggb.GBTagModeConfigurationActivity;
import com.invengo.rfidpad.scan.taggb.GBTagReadActivity;
import com.invengo.rfidpad.scan.taggb.GBTagWriteActivity;
import com.invengo.rfidpad.utils.Contants;

import java.util.ArrayList;
import java.util.List;

/**
 * GB操作
 */
public class TagGBOperationActivity extends PowerManagerActivity {

	private TagScanSettingsCollection mSettingsCollection;
	private TextView mTagData;
	private TextView mLabelTagData;
	private ListView mOperationListView;
	private List<OperationEntity> mList = new ArrayList<OperationEntity>();
	public static final String TAG_GB_OPERATION_DATA = "TAG_GB_OPERATION_DATA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6b_operation);
		setTitle(R.string.title_tag_gb_operation);

		mSettingsCollection = TagScanSettingsCollection.getInstance();

		mLabelTagData = (TextView) findViewById(R.id.text_tag_operation_tag_data_id);
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
				Intent newIntent = new Intent(TagGBOperationActivity.this, entity.getCls());
				newIntent.putExtra(TAG_GB_OPERATION_DATA, mTagData.getText().toString());
				startActivity(newIntent);
			}

		});
	}

	private void initializeListEntity() {
		OperationEntity readUserdataEntity = new OperationEntity(getString(R.string.text_tag_gb_operation_read_userdata));
		readUserdataEntity.setCls(GBTagReadActivity.class);
		mList.add(readUserdataEntity);
		OperationEntity writeUserDataEntity = new OperationEntity(getString(R.string.text_tag_gb_operation_write_userdata));
		writeUserDataEntity.setCls(GBTagWriteActivity.class);
		mList.add(writeUserDataEntity);
		OperationEntity modeConfigurationEntity = new OperationEntity(getString(R.string.text_tag_gb_operation_mode_configuration));
		modeConfigurationEntity.setCls(GBTagModeConfigurationActivity.class);
		mList.add(modeConfigurationEntity);
		OperationEntity tagInactivateEntity = new OperationEntity(getString(R.string.text_tag_gb_operation_inactivate));
		tagInactivateEntity.setCls(GBTagInactivateActivity.class);
		mList.add(tagInactivateEntity);

	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeTagData();
	}

	private void initializeTagData() {
		if (mSettingsCollection.getGbInventoryChecked() == Contants.CHECKED
				|| mSettingsCollection.getGbCombinationReadChecked() == Contants.CHECKED
				|| mSettingsCollection.getGbAllReadChecked() == Contants.CHECKED) {//盘存、组合读、全区域读为EPC
			mLabelTagData.setText(R.string.label_tag_operation_tag_data_epc);
		}else if(mSettingsCollection.getGbAccessReadChecked() == Contants.CHECKED){
			if(mSettingsCollection.isGbEpc()){
				mLabelTagData.setText(R.string.label_tag_operation_tag_data_epc);
			}else if(mSettingsCollection.isGbTid()){
				mLabelTagData.setText(R.string.label_tag_operation_tag_data_tid);
			}else if(mSettingsCollection.isGbUserdata()){
				mLabelTagData.setText(R.string.label_tag_operation_tag_data_userdata);
			}
		}
		Intent intent = getIntent();
		String data = intent.getStringExtra(ReaderMainActivity.TAG_DATA);
		mTagData.setText(data);
	}

}
