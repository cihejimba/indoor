package com.bupt.indoorpostion;

import java.sql.Timestamp;
import java.util.Date;

import com.bupt.indoorPosition.bean.Inspector;
import com.bupt.indoorPosition.bean.Sim;
import com.bupt.indoorPosition.location.LocationProvider;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.model.UserService;
import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorpostion.MainActivity;
import com.bupt.indoorpostion.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private EditText userNameText;// 用户名
	private EditText passWordText;// 密码
	private EditText passWordText_1;// 确认密码
	private EditText companyNameText;// 公司名称
	private Button btnRegCancel;// 取消按钮
	private Button btnReg;// 注册按钮
	private TextView nameWarn;// 用户名验证消息
	private TextView passwordLength;// 密码是否合法
	private TextView passwordAgain;// 再次验证密码
	private TelephonyManager telephonyManager;
	private RegisterActivityReceiver receiver;
	public String testMessage;
	public String userName;
	public String password;
	public String password_1;
	public String company;
	public String province;
	public String city;
	public String imsi;
	public String imei;
	public String phoneNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		IntentFilter intentFilter = new IntentFilter(
				Constants.ACTIONURL.REGISTER_ACTIVITY_ACTION);
		receiver = new RegisterActivityReceiver();
		registerReceiver(receiver, intentFilter);

		Log.d("start", "register_activity");
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		userNameText = (EditText) findViewById(R.id.editText1);
		userNameText
				.setOnFocusChangeListener(new AddressOnFocusChanageListener());
		userNameText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				userNameText.setText("");
			}

		}

		);
		nameWarn = (TextView) findViewById(R.id.textView5);

		passWordText = (EditText) findViewById(R.id.editText2);
		passWordText
				.setOnFocusChangeListener(new AddressOnFocusChanageListener());
		passWordText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passWordText.setText("");
				passwordLength.setVisibility(View.GONE);

			}
		});
		passwordLength = (TextView) findViewById(R.id.textView6);

		passWordText_1 = (EditText) findViewById(R.id.editText3);
		passWordText_1
				.setOnFocusChangeListener(new AddressOnFocusChanageListener());
		passWordText_1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				passWordText_1.setText("");
				passwordAgain.setVisibility(View.GONE);

			}
		});
		passwordAgain = (TextView) findViewById(R.id.textView7);

		companyNameText = (EditText) findViewById(R.id.editText4);

		btnRegCancel = (Button) findViewById(R.id.btnRegCancel);
		btnRegCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(RegisterActivity.this,
						HomeActivity.class));
			}

		});

		btnReg = (Button) findViewById(R.id.btnReg);
		btnReg.setOnClickListener(new RegisterListener());

	}

	@Override
	protected void onDestroy() {
		if (receiver != null)
			unregisterReceiver(receiver);
		super.onDestroy();
	}

	class RegisterListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			company = companyNameText.getText().toString();
			Sim sim = ModelService.getPhoneInfo(telephonyManager);
			imsi = sim.getImsi();
			imei = sim.getImei();
			phoneNumber = sim.getPhoneNumber();
			city = LocationProvider.getCity();
			province = LocationProvider.getProvince();
			if (userName == null || userName.trim().equals("")) {
				Toast toast = Toast.makeText(RegisterActivity.this,
						"注册失败，用户名不合法", Toast.LENGTH_SHORT);
				toast.show();
				userNameText.requestFocus();// 很可能有问题 改
			} else if (password == null || password.trim().length() < 6
					|| password.trim().length() > 16) {
				Toast toast = Toast.makeText(RegisterActivity.this,
						"注册失败，密码不合法", Toast.LENGTH_SHORT);
				toast.show();
				passWordText.requestFocus();
			} else if (password_1 == null || (!password.equals(password_1))) {
				Toast toast = Toast.makeText(RegisterActivity.this,
						"注册失败！密码不一致", Toast.LENGTH_SHORT);
				toast.show();
				passWordText_1.requestFocus();
			} else if (company == null) {
				Toast toast = Toast.makeText(RegisterActivity.this,
						"注册失败！请输入公司名", Toast.LENGTH_SHORT);
				toast.show();
				companyNameText.requestFocus();
			} else {
				new Thread() {
					@Override
					public void run() {
						Inspector inspector = new Inspector(userName,
								phoneNumber, imsi, imei, province, city,
								company, new Timestamp(
										System.currentTimeMillis()), password);
						UserService
								.insertUser(RegisterActivity.this, inspector);
						UserService.sendUser(RegisterActivity.this, inspector);
						RegisterActivity.this.finish();
						// startActivity(new Intent(RegisterActivity.this,
						// LoginActivity.class));
					}

				}.start();

			}
		}
		// new Thread() {
		// @Override
		// public void run() {

		// }
		// }.start();

	}

	class AddressOnFocusChanageListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View view, boolean hasFocus) {

			if (view.getId() == userNameText.getId()) {
				Log.i("focusChange", "userName");
				userName = userNameText.getText().toString();
				if (userName.trim().length() != 0) {
					new Thread() {
						@Override
						public void run() {
							// 把网络访问的代码放在这里
							Log.i("userName", "run");
							UserService.userNameToConf(RegisterActivity.this,
									userName);
						}
					}.start();
				}
			} else if (view.getId() == passWordText.getId()) {
				Log.d("focusChange", "passWordText");
				password = passWordText.getText().toString();
				Log.d("pass", password);
				if (!password.equals("")) {
					Log.i("password", "run");
					passwordLength.setVisibility(View.GONE);
					if (password.length() < 6 || password.length() > 16) {
						passwordLength.setText("密码不符合规范，请重新输入");
						passwordLength.setVisibility(View.VISIBLE);
					} else {
						passwordLength.setText("密码符合规范");
						passwordLength.setVisibility(View.VISIBLE);
					}

				}
			} else if (view.getId() == passWordText_1.getId()) {
				Log.d("focusChange", "passWordText_1");
				password_1 = passWordText_1.getText().toString();
				if (!password_1.equals("")) {
					Log.i("password_1", "run");
					passwordAgain.setVisibility(View.GONE);
					if (!password_1.equals(password)) {
						passwordAgain.setText("密码不一致");
						passwordAgain.setVisibility(View.VISIBLE);
					} else {
						passwordAgain.setText("密码设置成功");
						passwordAgain.setVisibility(View.VISIBLE);
					}
				}

			}
		}
	}

	public class RegisterActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			Bundle b = intent.getExtras();
			String type = intent.getStringExtra("type");
			if (Constants.INTENT_TYPE.REGISTRY_SUCCESS.equals(type)) {
				String s = b.getString("reason");
				Log.d("register s",s);
				if ("Ok".equals(s)) {
					Log.d("register OK", s);
					Toast.makeText(RegisterActivity.this, "注册成功", 3000).show();
					RegisterActivity.this.finish();
				} else {
				    Log.d("register nonOK",s);
					Toast.makeText(RegisterActivity.this, s, 3000).show();
				}
			} else if (Constants.INTENT_TYPE.USERNAME_AVAIABLE.equals(type)) {
				String message = b.getString("reason");
				Log.d("receive_mess", message);
				if (message.equals("Ok")) {
					testMessage = "用户名可用";
				} else {
					testMessage = message;
					userName = "";
				}
				nameWarn.setVisibility(View.GONE);
				nameWarn.setText(testMessage);
				nameWarn.setVisibility(View.VISIBLE);
			}
		}

	}

}
