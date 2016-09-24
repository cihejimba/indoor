package com.bupt.indoorpostion;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.bupt.indoorPosition.bean.Beacon;
import com.bupt.indoorPosition.bean.IndoorRecord;
import com.bupt.indoorPosition.bean.Neighbor;
import com.bupt.indoorPosition.bean.Sim;
import com.bupt.indoorPosition.location.LocationProvider;
import com.bupt.indoorPosition.model.ModelService;

import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorPosition.uti.Global;
import com.bupt.indoorPosition.uti.HttpUtil;
import com.bupt.indoorPosition.uti.BeaconUtil;
import com.bupt.indoorPosition.uti.SignalUtil;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button btnUpdate;
	private Button btnStart;
	private Button btnEnd;
	private Button btnShow;
	ImageView btnimage;
	int[] images = new int[] { R.drawable.green, R.drawable.red, };
	private Handler handler;
	private BluetoothAdapter bAdapter;
	private boolean startScanning = false;
	private TelephonyManager telephonyManager;
	private Timer keepAliveTimer;// 登录保活定时器

	private Button btnTest;// you can delete it is for testing reglogin

	// private MyPhonestateListener myPhonestateListener;
	// private MediaPlayer mp3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		IntentFilter intentFilter = new IntentFilter(
				Constants.ACTIONURL.MAIN_ACTIVITY_ACTION);
		MainActivityReceiver receiver = new MainActivityReceiver();

		registerReceiver(receiver, intentFilter);
		initComponent();
		new LocationProvider(this);// 初始化定位
		Log.d("onCreate", "start");
		btnEnd.setOnClickListener(new EndListener());

		btnTest = (Button) this.findViewById(R.id.test);
		btnTest.setOnClickListener(new TestListener());

	}

	class TestListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Log.i("changeActivity", "register");
			startActivity(new Intent(MainActivity.this, LoginActivity.class));

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void initComponent() {
		btnUpdate = (Button) this.findViewById(R.id.btnUpdate);
		btnStart = (Button) this.findViewById(R.id.btnStart);
		btnimage = (ImageView) this.findViewById(R.id.btnImage);
		btnShow = (Button) this.findViewById(R.id.btnShow);
		btnUpdate.setOnClickListener(new UpdateListener());
		btnStart.setOnClickListener(new StartListener());
		btnShow.setOnClickListener(new ShowBeacon());
		btnEnd = (Button) this.findViewById(R.id.btnEnd);
		// mp3=new MediaPlayer();
		// init media player
		// telephonyManager = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// ModelService M=new ModelService();

		handler = new MainHandler();
		// init phonestate
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// myPhonestateListener = new MyPhonestateListener();
		// telephonyManager.listen(myPhonestateListener,
		// PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		// // telephonyManager.listen(myPhonestateListener,
		// // PhoneStateListener.LISTEN_CELL_LOCATION);
		// // if (telephonyManager.getCellLocation() != null) {
		// // // 获取当前基站信息
		// // myPhonestateListener.onCellLocationChanged(telephonyManager
		// // .getCellLocation());
		// // }
		//
		// // init bluetooth
		// beaconSet = new HashSet<Beacon>();
		// 打开蓝牙
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(MainActivity.this, R.string.ble_not_supported,
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
				System.out.println(LocationProvider.getProvince() + " "
						+ LocationProvider.getCity() + " "
						+ LocationProvider.getLongditude() + " "
						+ LocationProvider.getLatitidue() + " "
						+ LocationProvider.getPoi() + " "
						+ LocationProvider.getTime());
				switch (Global.loginStatus) {
				case NON_LOGINED:
					break;
				case LOGINED:
					boolean statusNow = ModelService.keepAlive(
							MainActivity.this, LocationProvider.getLocation());
					if (!statusNow) {
						Global.loginStatus = Global.LoginStatus.NON_LOGINED;
					}
					break;
				}
			}
		}, 10000, 7000);
	}

	@Override
	protected void onDestroy() {
		if (keepAliveTimer != null)
			keepAliveTimer.cancel();
		super.onDestroy();
	}

	class ShowBeacon implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(MainActivity.this,
					com.bupt.indoorPosition.show.ShowBeacon.class);
			MainActivity.this.startActivity(intent);
		}
	}

	class UpdateListener implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			Toast.makeText(MainActivity.this.getApplicationContext(), "开始更新",
					3000).show();
			btnUpdate.setText(R.string.btnUpdatding);
			btnUpdate.setClickable(false);
			new Thread(new Runnable() {

				@Override
				public void run() {
					Log.d("update", "开始更新");
					Sim sim = ModelService.getPhoneInfo(telephonyManager);
					boolean status = ModelService.updateDb(MainActivity.this,
							sim);
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putBoolean("status", status);
					msg.setData(b);
					msg.what = Constants.MSG.UPDATE;
					MainActivity.this.handler.sendMessage(msg);
				}
			}).start();
		}

	}

	class StartListener implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			final Intent intent = new Intent();
			intent.setAction("com.bupt.indoorpostion.ScanService");

			if (startScanning == false) {
				startScanning = true;
				btnStart.setText(R.string.btnStarting);
				btnimage.setImageResource(images[0]);
				startService(intent);
			} else {
				startScanning = false;
				btnStart.setText(R.string.btnStartContent);
				// bAdapter.disable();
				btnimage.setImageResource(images[1]);
				stopService(intent);
			}
		}
	}

	class EndListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {
			if (startScanning == true)
				return;
			btnEnd.setText(R.string.btnUploadding);
			btnEnd.setClickable(false);
			new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d("upload", "开始上传报告");
					boolean status = ModelService
							.uploadRecord(MainActivity.this);
					// boolean beaconinfo = ModelService
					// .uploadBeaconinfolist(MainActivity.this);
					// boolean speed = ModelService
					// .uploadspeedlist(MainActivity.this);
					boolean neighbor = ModelService
							.uploadNeighbor(MainActivity.this);
					boolean inspection = ModelService
							.uploadInspection(MainActivity.this);
					Message msg = new Message();
					msg.what = Constants.MSG.UPLOAD;
					Bundle b = new Bundle();
					b.putBoolean("status", status && neighbor && inspection);
					msg.setData(b);
					Log.d("上传测试", "" + status);

					MainActivity.this.handler.sendMessage(msg);
				}
			}).start();
		}
	}

	class MainHandler extends Handler {
		// private WeakReference<MainActivity> mActivity;

		// public MainHandler(Activity a) {
		// mActivity = new WeakReference<MainActivity>(a);
		// }

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Constants.MSG.UPDATE) {
				btnUpdate.setText(R.string.btnUpdateContent);
				btnUpdate.setClickable(true);
			} else if (msg.what == Constants.MSG.UPLOAD) {
				Bundle b = msg.getData();
				boolean status = b.getBoolean("status");
				if (status) {
					Toast.makeText(MainActivity.this, "上传成功", 3000).show();
				} else {
					Toast.makeText(MainActivity.this, "上传失败", 3000).show();
				}
				btnEnd.setText(R.string.btnStartUpload);
				btnEnd.setClickable(true);

			}
			super.handleMessage(msg);
		}
	}

	public class MainActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			Bundle b = intent.getExtras();
			String s = b.getString("reason");
			int code = b.getInt("code");
			Toast.makeText(MainActivity.this,
					"server code: " + code + "\n" + s, 3000).show();

		}

	}

}
