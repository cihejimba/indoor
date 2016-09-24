package com.bupt.indoorpostion;

import com.bupt.indoorPosition.bean.Inspector;
import com.bupt.indoorPosition.dao.DBManager;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.model.UserService;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UserCenterActivity extends Activity {

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private TextView name;
	private TextView company;
	private TextView province;
	private TextView city;
	private Button logout;
	private Inspector inspector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usercenter);
		Log.d("center", "haha");
		name = (TextView) findViewById(R.id.Name);
		company = (TextView) findViewById(R.id.company);
		province = (TextView) findViewById(R.id.province);
		city = (TextView) findViewById(R.id.city);
		logout = (Button) findViewById(R.id.logout);

		inspector = UserService.selectAllInspector(UserCenterActivity.this);
		if (inspector != null) {
			name.setText(inspector.getUsername());
			company.setText(inspector.getCompanyName());
			province.setText(inspector.getProvince());
			city.setText(inspector.getCity());
		}

		logout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new Thread() {
					@Override
					public void run() {
						UserService.userLogout(UserCenterActivity.this);
						UserCenterActivity.this.finish();
					}

				}.start();
			}
		});

	}
}
