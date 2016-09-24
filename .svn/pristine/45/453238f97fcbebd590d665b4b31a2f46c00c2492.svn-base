package com.bupt.indoorpostion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bupt.indoorPosition.model.CheckVersionTask;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class AboutSystemActivity extends Activity {
	private ListView listView;
	private String[] arrName = { "检测版本更新" };
	private List<Map<String, Object>> listItems;
	private Bundle savedState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_system);
		initaboutsystem();

	}

	public void initaboutsystem() {
		listView = (ListView) findViewById(R.id.about_system_list_view);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int index, long id) {
				// Log.d("selected", arrName[index]);
				switch (index) {
				case 0:
					CheckVersion();
					break;
				}

			}
		});
		listItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < arrName.length; i++) {
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("text", arrName[i]);
			listItems.add(listItem);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems,
				R.layout.array_test, new String[] { "text" },
				new int[] { R.id.set_itemView });
		listView.setAdapter(simpleAdapter);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	// 版本更新
	private void CheckVersion() {
		CheckVersionTask checkversiontask = new CheckVersionTask(this);
		checkversiontask.run();
	}

}
