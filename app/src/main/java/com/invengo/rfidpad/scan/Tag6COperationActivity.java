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
import com.invengo.rfidpad.base.DebugManager;
import com.invengo.rfidpad.base.OperationArrayAdapter;
import com.invengo.rfidpad.base.PowerManagerActivity;
import com.invengo.rfidpad.debug.RepeatRWDebugActivity;
import com.invengo.rfidpad.entity.OperationEntity;
import com.invengo.rfidpad.scan.tag6c.TagBankClearActivity;
import com.invengo.rfidpad.scan.tag6c.TagBankWriteActivity;
import com.invengo.rfidpad.scan.tag6c.TagEPCWriteActivity;
import com.invengo.rfidpad.scan.tag6c.TagEasFlagConfigurateActivity;
import com.invengo.rfidpad.scan.tag6c.TagEasMonitorActivity;
import com.invengo.rfidpad.scan.tag6c.TagEpcFilterActivity;
import com.invengo.rfidpad.scan.tag6c.TagInactivateActivity;
import com.invengo.rfidpad.scan.tag6c.TagLockStatusConfigurateActivity;
import com.invengo.rfidpad.scan.tag6c.TagPasswordConfigurateActivity;
import com.invengo.rfidpad.scan.tag6c.TagUserDataReadActivity;
import com.invengo.rfidpad.scan.tag6c.TagUserDataWriteActivity;
import com.invengo.rfidpad.utils.Contants;

/**
 * 6C操作
 */
public class Tag6COperationActivity extends PowerManagerActivity {

	private DebugManager mDebugManager;
	private TagScanSettingsCollection mSettingsCollection;
	private TextView mLabelTagData;
	private TextView mTagData;
	private ListView mOperationListView;
	private List<OperationEntity> mList = new ArrayList<OperationEntity>();
	public static final String TAG_6C_OPERATION_DATA = "TAG_6C_OPERATION_DATA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_6c_operation);
		setTitle(R.string.title_tag_operation);

		mDebugManager = DebugManager.getInstance();
		mSettingsCollection = TagScanSettingsCollection.getInstance();

		mLabelTagData = (TextView) findViewById(R.id.text_label_tag_operation_tag_data);
		mTagData = (TextView) findViewById(R.id.text_tag_operation_tag_data);

		initializeListEntity();
		mOperationListView = (ListView) findViewById(R.id.list_tag_operation_detail);
		OperationArrayAdapter adapter = new OperationArrayAdapter(this, R.layout.list_reader_main, mList);
		mOperationListView.setAdapter(adapter);
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				OperationEntity entity = (OperationEntity) mOperationListView.getAdapter().getItem(position);
				Intent newIntent = new Intent(Tag6COperationActivity.this, entity.getCls());
				newIntent.putExtra(TAG_6C_OPERATION_DATA, mTagData.getText().toString());
				startActivity(newIntent);
			}

		});
	}

	private void initializeListEntity() {
		OperationEntity writeEpcEntity = new OperationEntity(getString(R.string.text_tag_operation_write_epc));
		writeEpcEntity.setCls(TagEPCWriteActivity.class);
		mList.add(writeEpcEntity);
		OperationEntity readUserDataEntity = new OperationEntity(getString(R.string.text_tag_operation_read_userdata));
		readUserDataEntity.setCls(TagUserDataReadActivity.class);
		mList.add(readUserDataEntity);
		OperationEntity writeUserDataEntity = new OperationEntity(getString(R.string.text_tag_operation_write_userdata));
		writeUserDataEntity.setCls(TagUserDataWriteActivity.class);
		mList.add(writeUserDataEntity);
		OperationEntity writeBankEntity = new OperationEntity(getString(R.string.text_tag_operation_write_bank));
		writeBankEntity.setCls(TagBankWriteActivity.class);
		mList.add(writeBankEntity);
		OperationEntity clearBankEntity = new OperationEntity(getString(R.string.text_tag_operation_clear_bank));
		clearBankEntity.setCls(TagBankClearActivity.class);
		mList.add(clearBankEntity);
		OperationEntity accessPasswordEntity = new OperationEntity(getString(R.string.text_tag_operation_password_configurate));
		accessPasswordEntity.setCls(TagPasswordConfigurateActivity.class);
		mList.add(accessPasswordEntity);
		OperationEntity lockStatusEntity = new OperationEntity(getString(R.string.text_tag_operation_lock_status));
		lockStatusEntity.setCls(TagLockStatusConfigurateActivity.class);
		mList.add(lockStatusEntity);
		OperationEntity inactivateTagEntity = new OperationEntity(getString(R.string.text_tag_operation_inactivate_tag));
		inactivateTagEntity.setCls(TagInactivateActivity.class);
		mList.add(inactivateTagEntity);
		OperationEntity easFlagConfigurateEntity = new OperationEntity(getString(R.string.text_tag_operation_easflag_configurate));
		easFlagConfigurateEntity.setCls(TagEasFlagConfigurateActivity.class);
		mList.add(easFlagConfigurateEntity);
		OperationEntity easMonitorEntity = new OperationEntity(getString(R.string.text_tag_operation_eas_monitor));
		easMonitorEntity.setCls(TagEasMonitorActivity.class);
		mList.add(easMonitorEntity);
		OperationEntity epcFilterEntity = new OperationEntity(getString(R.string.text_tag_operation_epc_filter));
		epcFilterEntity.setCls(TagEpcFilterActivity.class);
		mList.add(epcFilterEntity);
//		OperationEntity tagFilterEntity = new OperationEntity(R.drawable.settings, getString(R.string.text_tag_operation_tag_filter));
//		tagFilterEntity.setCls(TagFilteByTimeActivity.class);
//		mList.add(tagFilterEntity);
		if(mDebugManager.isDebug()){//debug
			OperationEntity tagFilterEntity = new OperationEntity(getString(R.string.title_reader_debug_repeat_rw));
			tagFilterEntity.setCls(RepeatRWDebugActivity.class);
			mList.add(tagFilterEntity);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeTagData();
	}

	private void initializeTagData() {
		if (mSettingsCollection.getEpcChecked() == Contants.CHECKED
				|| mSettingsCollection.getUserdataChecked() == Contants.CHECKED) {
			mLabelTagData.setText(R.string.label_tag_operation_tag_data_epc);
		}else if(mSettingsCollection.getTidChecked() == Contants.CHECKED){
			mLabelTagData.setText(R.string.label_tag_operation_tag_data_tid);
		}
		Intent intent = getIntent();
		String data = intent.getStringExtra(TagScanActivity.TAG_DATA);
		mTagData.setText(data);
	}

}
