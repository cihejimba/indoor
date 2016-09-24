package com.bupt.indoorpostion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.impl.entity.EntitySerializer;

import com.bupt.indoorPosition.bean.Beacon;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.uti.BeaconUtil;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerResultsIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IndoorLocationActivity extends Activity {

	private Set<Beacon> beaconSet;
	private BluetoothAdapter bAdapter;

	private Timer findLostBeaconTimer;
	private Handler handler;

	// 重启BLE扫描计时
	private int scanCount = 0;
	// 蓝牙没有反应计时
	private int bleNoReactCount = 0;
	private Timer LocalizationTimer;
	private long timeZero;
	
	private EditText alfa;
	private EditText sigma;
	private EditText distance;
	
	private Button button;
	private Button button2;
	
	private TextView textView;
	private TextView textView2;
	private double dis_real = 2.0;
	static double al = -50.0;
	static double si = 0;
	static int counter = 0;
	static ArrayList<Integer> Rssi = null;
	static ArrayList<Double> Distance = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.indoor_localization_para);

//		indoorMap = (ImageView) findViewById(R.id.map);
//		myLocation = (ImageView) findViewById(R.id.myLocation);
//		startButton = (Button) findViewById(R.id.start_buuton);
//		stopButton = (Button) findViewById(R.id.stop_button);
//		textView = (TextView) findViewById(R.id.showdetails);
//		width = getDeviceWidth(this);
//		height = getDeviceHeight(this);
//		density = getResources().getDisplayMetrics().density;
//		animX = ObjectAnimator.ofFloat(myLocation, "scaleX", 0.6f, 1f, 0.6f);
//		animX.setDuration(2000);
//		animX.setRepeatCount(Animation.INFINITE);
//		animX.setRepeatMode(Animation.REVERSE);
//		animY = ObjectAnimator.ofFloat(myLocation, "scaleY", 0.6f, 1f, 0.6f);
//		animY.setDuration(2000);
//		animY.setRepeatCount(Animation.INFINITE);
//		animY.setRepeatMode(Animation.REVERSE);
//		
//		
		alfa = (EditText) findViewById(R.id.text_alfa);
		sigma = (EditText) findViewById(R.id.text_sigma);
		distance = (EditText) findViewById(R.id.text_di);
		
		button = (Button) findViewById(R.id.btn);
		button2 = (Button) findViewById(R.id.btn1);
		
		textView = (TextView) findViewById(R.id.text_dis);
		textView2 = (TextView) findViewById(R.id.text_alfa2);
		
		Rssi = new ArrayList<Integer>();
		Distance = new ArrayList<Double>();
		counter = 0;
		
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str1 = alfa.getText().toString();
				String str2 = sigma.getText().toString();
				try{
					al = Double.parseDouble(str1);
					si = Double.parseDouble(str2);
				}catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(IndoorLocationActivity.this, "Input Error", Toast.LENGTH_SHORT).show();
					al = -50;
					si = 0;
				}
			}
		});
		
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					dis_real = Double.parseDouble(distance.getText().toString());
				}catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(IndoorLocationActivity.this, "Input Error", Toast.LENGTH_SHORT).show();
					dis_real = 2.0;
				}
			}
		});
		
		initComponent();

//		startButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				textView.setText(width + " " + height + " " + density + " " + indoorMap.getWidth() + " "
//						+ indoorMap.getHeight());
//				// textView.setText(indoorMap.getX() + " " + indoorMap.getY()+"
//				// "+density+" "+x + " " + y);
//				myLocation.setX((float) (indoorMap.getWidth() * Math.random() - d * density / 2));
//				myLocation.setY((float) (indoorMap.getHeight() * Math.random() - d * density / 2));
//				animX.start();
//				animY.start();
//			}
//		});
//		stopButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				animX.end();
//				animY.end();
//			}
//		});

		LocalizationTimer = new Timer();
		LocalizationTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				localizationFunc();
			}
		}, 1000, 3000);
	}

	/**
	 * 获取设备屏幕的宽
	 * 
	 * @param context
	 * @return
	 */
	public static int getDeviceWidth(Activity context) {
		Display display = context.getWindowManager().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		return p.x;
	}

	/** 获取屏幕的高 */
	public static int getDeviceHeight(Activity context) {
		Display display = context.getWindowManager().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		return p.y;
	}

	@Override
	protected void onDestroy() {
		if (bAdapter != null)
			bAdapter.stopLeScan(mLeScanCallback);
		if (findLostBeaconTimer != null)
			findLostBeaconTimer.cancel();
		if (LocalizationTimer != null)
			LocalizationTimer.cancel();
		super.onDestroy();
	}

	private void initComponent() {
		beaconSet = new HashSet<Beacon>();
		// 打开蓝牙
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(IndoorLocationActivity.this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
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
					int invalidBeacon = BeaconUtil.scanLostBeacon(beaconSet);
					Beacon max = BeaconUtil.getMax(beaconSet);
					// Log.i("traindataactivity", max == null ? "无" :
					// max.getMac());
					// Bundle b = new Bundle();
					// b.putString("beacon", max == null ? "无" : max.getMac());
					// Message msg = new Message();
					// msg.setData(b);
					// msg.what = 0x01;
					// handler.sendMessage(msg);

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
						if (total == 0 || invalidBeacon == total || scanCount == restartThreshold - 1) {
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

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
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
				if (device.getAddress().equals("98:7B:F3:5B:7D:B9")) {
	//				System.out.println(123456789 + " txPower " + txPower);
					
					double dis = BeaconUtil.calculateAccuracy(al, si, rssi);
					DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
					decimalFormat.setMaximumFractionDigits(3);
					if(counter++<30){
						Rssi.add(rssi);
						Distance.add(dis);
					}else{
						int av_rssi = averageRssi(Rssi);
						double av_dis = averageDistance(Distance); 
						double delta_dis = av_dis - dis_real;
						if(!(Math.abs(delta_dis)< 0.5)){
							if(delta_dis>0)
								al -= 1.0;
							else
								al += 1.0;
							textView2.append(" "+Double.toString(al));
						}else{
							textView2.setText("The final alfa is "+ Double.toString(al));
							Log.i("iBeacon","The final alfa is "+ Double.toString(al));
						}
						
						counter = 0;
						Rssi.clear();
						Distance.clear();
						Log.i("iBeacon", "Average Rssi=>"+Integer.toString(av_rssi)+"\nAverage Distance=>"+Double.toString(av_dis));
					}
					textView.setText("\n Dis=>"+decimalFormat.format(dis).toString());
					Log.i("iBeacon",device.getAddress() + " rssi="+Integer.toString(rssi) + " dis="+Double.toString(dis));
					ModelService.updateBeaconForLocal(IndoorLocationActivity.this, beaconSet,
							new Beacon(device.getAddress(), rssi, txPower, (int)dis*100));
				}
				
				Log.i("iBeacon","RSSI=>"+Integer.toString(rssi));
			}
		}
	};
	
	public int averageRssi(ArrayList<Integer> rssi){
		
		int len = rssi.size();
		if(len <= 0) return 0;
		else if(len <= 20) return -1;
		else{
			Collections.sort(rssi);
			int sum = 0;
			for(int i=10;i < len-10;i++){
				sum += rssi.get(i);
			}
			return (int) sum/(len-20);
		}
	}
	
	public double averageDistance(ArrayList<Double> distance){
		int len = distance.size();
		if(len <= 0) return 0;
		else if(len <= 20) return -1;
		else{
			Collections.sort(distance);
			double sum = 0;
			for(int i=10;i < len-10;i++){
				sum += distance.get(i);
			}
			return (double) sum/(len-20);
		}
	}

	public void localizationFunc() {
		Iterator<Beacon> ite = beaconSet.iterator();
		int length = beaconSet.size();
		int original[][] = new int[length][3];
		int temp = 0;
		while (ite.hasNext()) {
			Beacon b = ite.next();
			original[temp][0] = b.getX();
			original[temp][1] = b.getY();
			original[temp][2] = b.getDistance();
			Log.d("genxinshuju", ""+original[temp][0]+" "+original[temp][1]+" "+original[temp][2]);
			temp += 1;
		}
		Log.d("genxinshuju", "" + temp+"   "+beaconSet.size());

		// 要求的坐标为（A的转置乘上A）的逆乘上A的转置乘上B
		if (temp > 1 && temp == length) {
			double A[][] = new double[temp - 1][2];
			double AT[][] = new double[2][temp - 1];
			double B[][] = new double[temp - 1][1];
			for (int i = 0; i < temp - 1; i++) {
				A[i][0] = 2 * (original[i][0] - original[temp - 2][0]);
				A[i][1] = 2 * (original[i][1] - original[temp - 2][1]);
				B[i][0] = (original[i][0]) * (original[i][0]) - (original[temp - 2][0]) * (original[temp - 2][0])
						+ (original[i][1]) * (original[i][1]) - (original[temp - 2][1]) * (original[temp - 2][1])
						+ original[temp - 2][2] * original[temp - 2][2] - original[i][2] * original[i][2];
				AT[0][i] = 2 * (original[i][0] - original[temp - 2][0]);
				AT[1][i] = 2 * (original[i][1] - original[temp - 2][1]);
				
//				Log.d("genxinshuju", ""+A[i][0]+" "+A[i][1]+" "+B[i][0]+" "+AT[0][i]+" "+AT[1][i]);
			}
			double X[][] = multiMetrixAandB(Mrinv(multiMetrixAandB(AT, A), 2), multiMetrixAandB(AT, B));
//			double a [][] = new double[3][3];
//			a[0][0] = 1;
//			a[0][1] = 0;
//			a[0][2] = 0;
//			a[1][0] = 0;
//			a[1][1] = 1;
//			a[1][2] = 0;
//			a[2][0] = 0;
//			a[2][1] = 0;
//			a[2][2] = 1;
//			double b[][] = Mrinv(a,3);
			
		}

	}

	// 矩阵求逆运算
	public static double[][] Mrinv(double[][] a, int n) {
		int i, j, row, col, k;
		double max, temp;
		int[] p = new int[n];
		double[][] b = new double[n][n];
		for (i = 0; i < n; i++) {
			p[i] = i;
			b[i][i] = 1;
		}

		for (k = 0; k < n; k++) {
			// 找主元
			max = 0;
			row = col = i;
			for (i = k; i < n; i++)
				for (j = k; j < n; j++) {
					temp = Math.abs(b[i][j]);
					if (max < temp) {
						max = temp;
						row = i;
						col = j;
					}
				}
			// 交换行列，将主元调整到 k 行 k 列上
			if (row != k) {
				for (j = 0; j < n; j++) {
					temp = a[row][j];
					a[row][j] = a[k][j];
					a[k][j] = temp;
					temp = b[row][j];
					b[row][j] = b[k][j];
					b[k][j] = temp;
				}
				i = p[row];
				p[row] = p[k];
				p[k] = i;
			}
			if (col != k) {
				for (i = 0; i < n; i++) {
					temp = a[i][col];
					a[i][col] = a[i][k];
					a[i][k] = temp;
				}
			}
			// 处理
			for (j = k + 1; j < n; j++)
				a[k][j] /= a[k][k];
			for (j = 0; j < n; j++)
				b[k][j] /= a[k][k];
			a[k][k] = 1;

			for (j = k + 1; j < n; j++) {
				for (i = 0; i < k; i++)
					a[i][j] -= a[i][k] * a[k][j];
				for (i = k + 1; i < n; i++)
					a[i][j] -= a[i][k] * a[k][j];
			}
			for (j = 0; j < n; j++) {
				for (i = 0; i < k; i++)
					b[i][j] -= a[i][k] * b[k][j];
				for (i = k + 1; i < n; i++)
					b[i][j] -= a[i][k] * b[k][j];
			}
			for (i = 0; i < k; i++)
				a[i][k] = 0;
			a[k][k] = 1;
		}
		// 恢复行列次序；
		for (j = 0; j < n; j++)
			for (i = 0; i < n; i++)
				a[p[i]][j] = b[i][j];

		return a;
	}

	// 矩阵乘法
	public static double[][] multiMetrixAandB(double metrixA[][], double metrixB[][]) {
		double result[][] = new double[metrixA.length][metrixB[0].length];
		int x, i, j, tmp = 0;
		for (i = 0; i < metrixA.length; i++) {
			for (j = 0; j < metrixB[0].length; j++) {
				for (x = 0; x < metrixB.length; x++)
					tmp += metrixA[i][x] * metrixB[x][j];// 矩阵AB中a_ij的值等于矩阵A的i行和矩阵B的j列的乘积之和

				result[i][j] = tmp;
				tmp = 0; // 中间变量，每次使用后都得清零
			}
		}
		return result;
	}

}
