package com.bupt.indoorpostion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bupt.indoorPosition.adapter.Sys_Setting_Adapter;
import com.bupt.indoorPosition.adapter.Sys_Setting_Adapter.ViewHolder;
import com.bupt.indoorPosition.bean.SpeedTestStatus;
import com.bupt.indoorPosition.bean.UserSetting;
import com.bupt.indoorPosition.dao.DBManager;
import com.bupt.indoorPosition.model.UserService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SystemSettingActivity extends Activity {
	private UserSetting status;
	private boolean isTicked;
	private Sys_Setting_Adapter mAdapter;
	private ArrayList<String> list;
	private LinearLayout lr;
	CheckBox checkbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systemsetting);
		status = UserService.getUserSetting(this);
		isTicked = false;
		if (status.isTestSpeed()) {
			isTicked = status.isTestSpeed();
		}
		Log.d("getUserSetting", "" + isTicked);
		ListView listView = (ListView) findViewById(R.id.systemsetting_listView);
		// 要显示的数据
		String[] strs = { "是否开启测速"};
		list = new ArrayList<String>();
		initDate(strs);
		mAdapter = new Sys_Setting_Adapter(list, this, isTicked);
		// // 创建ArrayAdapter
		// List<Map<String, Object>> listitem = new ArrayList<Map<String,
		// Object>>();
		// for (int i = 0; i < strs.length; i++) {
		// Map<String, Object> showitem = new HashMap<String, Object>();
		// showitem.put("content", strs[i]);
		// listitem.add(showitem);
		// }
		// mAdapter = new Sys_Setting_Adapter(listitem,this);

		// 创建SimpleAdapter
		// SimpleAdapter myAdapter = new SimpleAdapter(getApplicationContext(),
		// listitem, R.layout.system_setting_item,
		// new String[] { "content" },
		// new int[] { R.id.item_tv });
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int index, long id) {

				lr = (LinearLayout) view;
				checkbox = (CheckBox) lr.getChildAt(1);
				ViewHolder holder = (ViewHolder) view.getTag();

				// Log.d("selected", arrName[index]);
				switch (index) {
				case 0:
					doSpeedTest();;
					break;
				}
			}

		});

	}

	private void doSpeedTest() {
		if (isTicked) {
			new AlertDialog.Builder(this)
					.setTitle("测速开关")
					.setMessage("确定关闭测速功能？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									changeCheckBox();
									Toast.makeText(SystemSettingActivity.this,
											"测速关闭", 3000).show();
								}
							}).setNegativeButton("否", null).show();
		}
		else {
			new AlertDialog.Builder(this)
			.setTitle("测速开关")
			.setMessage("确定开启测速功能？（可能会耗费较多流量）")
			.setPositiveButton("是",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							changeCheckBox();
							Toast.makeText(SystemSettingActivity.this,
									"测速开启", 3000).show();
						}
					}).setNegativeButton("否", null).show();
		}
	}

	private void changeCheckBox(){
		isTicked = !isTicked;
		// isTicked=checkbox.isChecked();
		Log.d("statuslist22222", "" + isTicked + "    " + !isTicked);
		checkbox.setChecked(isTicked);

		status.setTestSpeed(isTicked);

		Toast.makeText(SystemSettingActivity.this, "" + isTicked,
				3000).show();
		UserService.updateUserSetting(status, false,
				SystemSettingActivity.this);
	}
	private void initDate(String[] str) {
		for (int i = 0; i < str.length; i++) {
			list.add(str[i]);
		}
	}
}