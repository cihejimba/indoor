package com.bupt.indoorpostion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.bupt.indoorPosition.bean.InspectDisplay;
import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indoorPosition.bean.Inspector;
import com.bupt.indoorPosition.callback.FragmentServiceCallback;

import com.bupt.indoorPosition.callback.InspectUpdateCallback;
import com.bupt.indoorPosition.callback.SettingUpdateCallback;
import com.bupt.indoorPosition.fragment.FragmentInspection;
import com.bupt.indoorPosition.fragment.FragmentSetting;
import com.bupt.indoorPosition.fragment.FragmentSettingTest;
import com.bupt.indoorPosition.location.LocationProvider;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.model.UserService;
import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorPosition.uti.Global;
import com.bupt.indoorPosition.uti.Global.LoginStatus;
import com.bupt.indoorpostion.MainActivity.MainActivityReceiver;
import com.bupt.indoorpostion.MainActivity.MainHandler;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class HomeActivity extends Activity implements FragmentServiceCallback {
	private Fragment fragmentInspect;
	private Fragment fragmentSetting;
	private InspectUpdateCallback cbInspect;
	private SettingUpdateCallback cbSetting;
	private RadioGroup myTabRg;
	private RadioButton txt_home;
	public Handler handler;
	private BluetoothAdapter bAdapter;
	public TelephonyManager telephonyManager;
	private Timer keepAliveTimer;// 登录保活定时器
	private HomeActivityReceiver receiver;
	private Map<String, Intent> serviceMap;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Log.i("HomeActivity onCreate", "start");
		setContentView(R.layout.home_layout);
		initComponent();
		initLogin();
		initFragment();
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		return super.onCreateView(name, context, attrs);
	}

	public void initComponent() {
		// 初始化广播接收器
		IntentFilter intentFilter = new IntentFilter(
				Constants.ACTIONURL.MAIN_ACTIVITY_ACTION);
		receiver = new HomeActivityReceiver();
		registerReceiver(receiver, intentFilter);
		//
		serviceMap = new HashMap<String, Intent>();
		// 初始化百度定位
		new LocationProvider(this);
		// 初始化handler
		handler = new HomeHandler();
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 打开蓝牙
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(HomeActivity.this, R.string.ble_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
		}
		// Log.d("bluetooth", "ok");
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bAdapter = bluetoothManager.getAdapter();
		bAdapter.enable();
		keepAliveTimer = new Timer();
		keepAliveTimer.schedule(new TimerTask() {

			/**
			 * 如果登录，定时发送位置信息，保持服务器session的活性
			 */
			@Override
			public void run() {
				// System.out.println(LocationProvider.getProvince() + " "
				// + LocationProvider.getCity() + " "
				// + LocationProvider.getLongditude() + " "
				// + LocationProvider.getLatitidue() + " "
				// + LocationProvider.getPoi() + " "
				// + LocationProvider.getTime());
				switch (Global.loginStatus) {
				case NON_LOGINED:
					break;
				case LOGINED:
					boolean statusNow = ModelService.keepAlive(
							HomeActivity.this, LocationProvider.getLocation());
					if (!statusNow) {
						Global.loginStatus = Global.LoginStatus.NON_LOGINED;
					}
					break;
				}
			}
		}, 10000, 1000 * 60 * 3);
	}

	public void initFragment() {
		myTabRg = (RadioGroup) findViewById(R.id.tab_menu);
		txt_home = (RadioButton) findViewById(R.id.txt_home);
		txt_home.setChecked(true);

		myTabRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.txt_home:
					if (fragmentSetting != null) {
						getFragmentManager().beginTransaction()
								.hide(fragmentSetting).commit();
					}
					if (fragmentInspect == null) {
						fragmentInspect = new FragmentInspection();
						fragmentInspect.setArguments(new Bundle());
						getFragmentManager().beginTransaction()
								.add(R.id.home_content, fragmentInspect)
								.commit();
						cbInspect = (InspectUpdateCallback) fragmentInspect;
					}
					getFragmentManager().beginTransaction()
							.show(fragmentInspect).commit();

					break;
				case R.id.txt_setting:
					if (fragmentInspect != null) {
						getFragmentManager().beginTransaction()
								.hide(fragmentInspect).commit();
					}
					if (fragmentSetting == null) {
						fragmentSetting = new FragmentSettingTest();
						fragmentSetting.setArguments(new Bundle());
						getFragmentManager().beginTransaction()
								.add(R.id.home_content, fragmentSetting)
								.commit();
						cbSetting = (SettingUpdateCallback) fragmentSetting;
					}
					getFragmentManager().beginTransaction()
							.show(fragmentSetting).commit();

					break;
				default:
					break;
				}

			}
		});
		fragmentInspect = new FragmentInspection();
		fragmentInspect.setArguments(new Bundle());
		getFragmentManager().beginTransaction()
				.add(R.id.home_content, fragmentInspect).commit();
		fragmentSetting = new FragmentSettingTest();
		fragmentSetting.setArguments(new Bundle());
		getFragmentManager().beginTransaction()
				.add(R.id.home_content, fragmentSetting).commit();
		cbInspect = (InspectUpdateCallback) fragmentInspect;
		cbSetting = (SettingUpdateCallback) fragmentSetting;
		// 默认显示fragmentInspect
		getFragmentManager().beginTransaction().hide(fragmentSetting)
				.show(fragmentInspect).commit();
	}

	private void initLogin() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Inspector inspector = UserService
						.selectAllInspector(HomeActivity.this);
				ModelService.loadCookie(HomeActivity.this);
				// 尝试询问服务器session是否存在
				boolean statusNow = ModelService.keepAlive(HomeActivity.this,
						LocationProvider.getLocation());
				if (inspector == null) {
					// inspector不存在，用户已经注销
					if (statusNow) {
						// 服务器session存在
						UserService.userLogout(HomeActivity.this);
					}
					Message msg = new Message();
					msg.what = 0xf01;
					handler.sendMessage(msg);
					return;
				} else {
					if (statusNow) {
						// 服务器session存在
						Global.loginStatus = Global.LoginStatus.LOGINED;
					} else {
						// 服务器session不存在，尝试重新登录
						// 重新登录，保存inspector和session
						UserService.userLogin(HomeActivity.this,
								inspector.getUsername(),
								inspector.getPassword());
					}
				}

				if (Global.loginStatus == LoginStatus.LOGINED) {
					System.out.println("logined");
					Message msg = new Message();
					msg.what = Constants.MSG.HAS_LOGINED;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		Log.i("homeActivity onDestroy", "onDestroy");
		whenFinish();
		super.onDestroy();
	}

	private void whenFinish() {
		if (keepAliveTimer != null)
			keepAliveTimer.cancel();
		if (receiver != null)
			unregisterReceiver(receiver);
		// 停止服务
		for (String k : serviceMap.keySet()) {
			Intent i = serviceMap.get(k);
			stopService(i);
		}
		// 保存cookie
		ModelService.saveCookie(this);
	}

	public class HomeActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			// Log.i("HomeActivityReceiver", "HomeActivityReceiver 收到广播");
			String type = intent.getStringExtra("type");
			Bundle b = intent.getExtras();

			if (Constants.INTENT_TYPE.NONE.equals(type)) {
				// 尝试获取服务器响应bundle
				String s = b.getString("reason");
				int code = b.getInt("code");
				if (s != null) {
					if (code < 0) {
						Log.d("code", s);
						Toast.makeText(HomeActivity.this,
								"server code: " + code + "\n" + s, 3000).show();
					}
					return;
				}
			} else if (Constants.INTENT_TYPE.BEACON_LIST_DISPLAY.equals(type)) {
				// 尝试获取showList响应Bundle
				ArrayList<InspectedBeacon> list = (ArrayList<InspectedBeacon>) b
						.getSerializable("showList");
				if (list != null) {
					// Log.i("HomeActivityReceiver", "list not null");
					if (cbInspect != null) {
						Message msg = new Message();
						msg.what = Constants.MSG.SHOW_BEACON;
						msg.setData(b);
						cbInspect.handleUpdateMessage(msg);
					}
					return;
				}
			} else if (Constants.INTENT_TYPE.KEEP_ALIVE.equals(type)) {
				Toast.makeText(HomeActivity.this, "与服务器连接已断开\n请检查网络连接并登录",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	// 转发message
	class HomeHandler extends Handler {
		// private WeakReference<MainActivity> mActivity;

		// public MainHandler(Activity a) {
		// mActivity = new WeakReference<MainActivity>(a);
		// }
		@Override
		public void handleMessage(Message msg) {
			// Log.i("Home_activity HomeHandler", "receive msg.what " +
			// msg.what);
			// Log.i("Home_activity HomeHandler", "" + (msg.what & 0xf0));
			// Log.i("Home_activity HomeHandler", "" + (msg.what & 0x0f));
			if ((msg.what & 0xf0) == ((int) 0xf0)) {
				if (cbInspect != null) {
					cbInspect.handleUpdateMessage(msg);
				}
			} else if ((msg.what & 0x0f) == ((int) 0x0f)) {
				// Log.i("Home_activity HomeHandler", "0x0f");
				if (cbSetting != null) {
					cbSetting.handleUpdateMessage(msg);
				}
			} else if ((msg.what & 0xf00) == ((int) 0xf00)) {
				switch (msg.what) {
				case 0xf01:
					Toast.makeText(HomeActivity.this, "请登录", Toast.LENGTH_SHORT)
							.show();
					break;
				}

			}
			super.handleMessage(msg);
		}
	}

	@Override
	public void startOrStopActivityService(Intent intent, boolean isStart) {
		if (isStart) {
			startService(intent);
			serviceMap.put(intent.getAction(), intent);
		} else {
			stopService(intent);
			serviceMap.remove(intent.getAction());
		}

	}

	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				whenFinish();
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
