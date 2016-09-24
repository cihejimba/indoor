package com.bupt.indoorpostion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.bupt.indoorPosition.bean.Beacon;
import com.bupt.indoorPosition.bean.Sim;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.uti.BeaconUtil;
import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorPosition.uti.MessageUtil;

import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TrainDataCollectionActivity extends Activity {

	private TextView tvBeacon;
	private EditText etMarker;
	private EditText inputXET;
	private EditText inputYET;
	private Button btnRecord;
	private Button btnUpload;
	private Handler handler;
	private BluetoothAdapter bAdapter;

	private Set<Beacon> beaconSet;

	private Timer findLostBeaconTimer;

	private boolean isLoading = false;
	Map<String, Integer> map = new HashMap<String, Integer>();
	// 重启BLE扫描计时
	private int scanCount = 0;
	// 蓝牙没有反应计时
	private int bleNoReactCount = 0;
	private long timeZero;
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.train_data_collection);
		handler = new MAHandler();
		tvBeacon = (TextView) this.findViewById(R.id.beaconTV);
		etMarker = (EditText) this.findViewById(R.id.markET);
		inputXET = (EditText) this.findViewById(R.id.inputXET);
		inputYET = (EditText) this.findViewById(R.id.inputYET);
		btnRecord = (Button) this.findViewById(R.id.recordBTN);
		btnUpload = (Button) this.findViewById(R.id.collectionUpload);
		initEnviroment();
		btnRecord.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String marker = etMarker.getText().toString();

				if (marker == null || marker.trim().length() == 0) {
					return;
				}

				btnRecord.setText("正在采集");
				btnRecord.setEnabled(false);
				final Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					private int count = 0;

					public void run() {
						count++;
						if (count < 60) {
							ModelService.recordPositionTrainData(
									TrainDataCollectionActivity.this, marker,
									beaconSet);
							Log.d("count", "!!!!count!!!!");
						} else {
							count = 0;
							this.cancel();
							Message msg = new Message();
							msg.what = 0x03;
							TrainDataCollectionActivity.this.handler
									.sendMessage(msg);
							Log.d("记录成功", "结束计时任务");
						}
					}
				}, 300, 1050);
				// ModelService.recordPositionTrainData(
				// TrainDataCollectionActivity.this, marker, beaconSet);
				// Toast.makeText(TrainDataCollectionActivity.this, "记录成功",
				// Toast.LENGTH_SHORT).show();

			}
		});
		btnUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLoading == true) {
					Toast.makeText(TrainDataCollectionActivity.this,
							"正在上传，请耐心等待", Toast.LENGTH_SHORT).show();
					return;
				}
				btnUpload.setText("正在上传");
				if (!MessageUtil.checkLogin(TrainDataCollectionActivity.this
						.getApplicationContext())) {
					String message = "请先登录再上传";
					Message msg = new Message();
					msg.what = 0x02;
					Bundle b = new Bundle();
					b.putString("msg", message);
					msg.setData(b);
					handler.sendMessage(msg);
					return;

				}
				final String marker = etMarker.getText().toString();
				final String Y = inputXET.getText().toString();
				final String X = inputYET.getText().toString();
				new Thread(new Runnable() {

					@Override
					public void run() {
						String message = null;

						message = ModelService
								.uploadPositionTrainData(TrainDataCollectionActivity.this);
						ModelService.uploadPositionXY(
								TrainDataCollectionActivity.this, marker, X, Y);
						Message msg = new Message();
						msg.what = 0x02;
						Bundle b = new Bundle();
						b.putString("msg", message);
						msg.setData(b);
						handler.sendMessage(msg);
					}
				}).start();
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (bAdapter != null)
			bAdapter.stopLeScan(mLeScanCallback);
		if (findLostBeaconTimer != null)
			findLostBeaconTimer.cancel();
		super.onDestroy();
	}

	private void initEnviroment() {
		// init bluetooth
		beaconSet = new HashSet<Beacon>();
		// 打开蓝牙
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(TrainDataCollectionActivity.this,
					R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			// finish();
		}
		Log.d("bluetooth", "ok");
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bAdapter = bluetoothManager.getAdapter();
		bAdapter.enable();
		timeZero = System.currentTimeMillis();
		if (bAdapter != null) {
			if (!bAdapter.isEnabled())
				bAdapter.enable();
			Log.d("bluetooth", "start scaning");
			bAdapter.startLeScan(mLeScanCallback);
			// startTime = new Timestamp(System.currentTimeMillis());
			//
			// positionTimer = new Timer();
			// positionTimer.schedule(new TimerTask() {
			//
			// @Override
			// public void run() {
			//
			// }
			// }, 3000, 1000);
			findLostBeaconTimer = new Timer();
			findLostBeaconTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Log.i("traindataactivity", "run");

					int invalidBeacon = BeaconUtil.scanLostBeacon(beaconSet);
					Beacon max = BeaconUtil.getMax(beaconSet);
					Log.i("traindataactivity", max == null ? "无" : max.getMac());
					Bundle b = new Bundle();
					b.putString("beacon", max == null ? "无" : max.getMac());
					Message msg = new Message();
					msg.setData(b);
					msg.what = 0x01;
					handler.sendMessage(msg);

					// 每20* Beacon.TRANSMIT_PERIOD的时间重启一次BLE
					int restartThreshold = 20;
					int noReactThreashold = 1;
					int total = beaconSet.size();
					if (bleNoReactCount == noReactThreashold - 1) {
						/**
						 * 蓝牙在noReactThreashold*TRANSMIT_PERIOD时间内没有触发任何回调，
						 * 针对小米等机型，可能通过重启一次蓝牙来缓解这种问题。
						 * 对于华为等机型，蓝牙回调非常频繁，在有Beacon的范围内,
						 * noReactThreashold可能永远保持为零
						 */
						bleRestart();
						Log.i("ScansService", "蓝牙未响应重启");
					} else {
						if (total == 0 || invalidBeacon == total
								|| scanCount == restartThreshold - 1) {
							/**
							 * 蓝牙有响应,但可能没有潜在位置的Beacon可能没有被更新。
							 * 没有检测到蓝牙，或者蓝牙全部失效，或者时间达到restartThreshold
							 * *TRANSMIT_PERIOD ， 重启一次蓝牙
							 */
							bleRestart();
							Log.i("ScansService", "beacon失效或周期性重启");

						}
					}
					bleNoReactCount = (bleNoReactCount + 1) % noReactThreashold;
					scanCount = (scanCount + 1) % restartThreshold;
				}
			}, 3000, Beacon.TRANSMIT_PERIOD);
		}

	}

	/**
	 * 对于小米手机，每个Beacon可能只会被扫描一次，此时需要重启扫描
	 */
	private void bleRestart() {
		bAdapter.stopLeScan(mLeScanCallback);
		bAdapter.startLeScan(mLeScanCallback);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			// Log.d("callback", device.getAddress() + "\n" + rssi);
			// int sec = (int) ((System.currentTimeMillis() - timeZero) / 1000);
			// Log.e("onLeScan", "第" + sec + "s ,发现" + device.getAddress() +
			// "\n有"
			// + beaconSet.size() + "个");
			if (device.getAddress() != null && rssi <= 0) {
				// if(device.getAddress().equals("98:7B:F3:72:23:C5")){
				// System.out.println(123456789);
				// }
				bleNoReactCount = 0;
				int txPower = BeaconUtil.getBeaconTxPower(scanRecord);
				if (device.getAddress().equals("98:7B:F3:72:23:C5")) {
					System.out.println(123456789 + " txPower " + txPower);
				}
				// 针对某些没有设置txpower的蓝牙芯片，设置默认的参考发射功率
				if (txPower > 0) {
					txPower = -65;
				}
				int dis = BeaconUtil.calculateAccuracy(txPower, rssi);
				ModelService.updateBeacon(TrainDataCollectionActivity.this,
						beaconSet, new Beacon(device.getAddress(), rssi,
								txPower, dis));

			}
		}
	};

	/**
	 * @author Wentao
	 * 
	 */
	private class MAHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == 0x01) {
				Bundle b = msg.getData();
				tvBeacon.setText(b.getString("beacon"));
			} else if (msg.what == 0x02) {
				Bundle b = msg.getData();
				String message = b.getString("msg");
				Toast.makeText(TrainDataCollectionActivity.this, message,
						Toast.LENGTH_SHORT).show();
				btnUpload.setText("上传");
			} else if (msg.what == 0x03) {
				btnRecord.setEnabled(true);
				btnRecord.setText("记录");
				Toast.makeText(TrainDataCollectionActivity.this, "记录成功",
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
