package com.example.setcts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Yixi 
 *        Language->English(US) Location -> on (通过) Wifi -> on (V) (通过)
 *         Stay Awake -> on (V) (通过) Allow mock locations -> on (only in Android
 *         5.x and 4.4.x) (通过/实现版本判断) Verify apps over USB -> off (only in
 *         Android 4.2) 打开usb_debug (通过) 安装应用： CtsDeviceAdmin.apk
 *         拷贝媒体文件：copy_media.sh 目标：通过应用setupCTS.apk 自动配置小米盒子跑CTS时需要的以上配置
 */

public class MainActivity extends Activity {
	private Context mContext;
	private TextView mShowTips;
	private String TAG = "xiaoxi";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mShowTips = (TextView) findViewById(R.id.showTips);
		
		String API_LEVEL_NUMBER = "0x2016";//2016 have not meaning
		try {
			float result = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT);
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
			Log.i(TAG, "Settings.System.SCREEN_OFF_TIMEOUT:"
					+ Settings.System.SCREEN_OFF_TIMEOUT + "result: " + result);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		// 获取当前系统的版本号
		Log.d(TAG, "Product Model: " + android.os.Build.MODEL
				+ ",   VERSION.SDK：" + android.os.Build.VERSION.SDK
				+ ",     Build.VERSION.RELEASE"
				+ android.os.Build.VERSION.RELEASE);
		API_LEVEL_NUMBER = android.os.Build.VERSION.SDK;
		Log.d(TAG, "API_LEVEL_NUMBER:" + API_LEVEL_NUMBER);

		// 1： wifi开关
		WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiManager.setWifiEnabled(true);

		// 2：打开usb调试
		Settings.Global.putInt(getContentResolver(),Settings.Global.ADB_ENABLED, 1);

		// 3：允许模拟位置加API LEVEL 判断
		// 将String 类型转化为int类型
		int api_level = Integer.valueOf(API_LEVEL_NUMBER).intValue();
		Log.d(TAG, "api_level:" + api_level);
		if (api_level >= 19 && api_level <= 22) {
			Log.d(TAG, "level 大于19小于22");
			Settings.Secure.putInt(getContentResolver(),
					Settings.Secure.ALLOW_MOCK_LOCATION, 1);
		} else {
			Toast.makeText(mContext, "当前android 版本 不需要 '开启允许模拟位置' " , Toast.LENGTH_LONG).show();
			Log.d(TAG, "当前android 版本 不需要 开启允许模拟位置");
		}
		// 4:stay awak
		Settings.Global.putInt(getContentResolver(),Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
				BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB);

		// 打开Location
		Settings.Secure.setLocationProviderEnabled(getContentResolver(),LocationManager.GPS_PROVIDER, true);

		// 拷贝文件
			if (copyApkFromAssets(this, "CtsDeviceAdmin.apk", Environment.getExternalStorageDirectory() + "/CtsDeviceAdmin.apk")) {
				Log.d(TAG,"copyApkFromAssets() ==  true");
				System.out.println((Environment.getExternalStorageDirectory()+ "/CtsDeviceAdmin.apk"));
				//弹对话框询问是否安装应用
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setMessage("请问是否安装CtsDeviceAdmin.apk应用?");
				builder.setPositiveButton("Yes", new OnClickListener() {
					@Override
					//当点击确认，安装该应用
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG,"onClick()");
						String fileName = Environment.getExternalStorageDirectory() + "/CtsDeviceAdmin.apk";
						Uri uri = Uri.fromFile(new File(fileName));
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						//type是文件类型，类型为APK
						intent.setDataAndType(uri, "application/vnd.android.package-archive");
						startActivity(intent);
						try {
							Log.d(TAG,"before runApk()");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				builder.show();
			}
		}
		// 设置本地语言默认为英文
		// 待验证
		// IActivityManager am = ActivityManagerNative.getDefault();
		// Configuration conf = am.getConfiguration();
		// config.locale = Locale.SIMPLIFIED_CHINESE;
		// am.updateConfiguration(conf);

		// String str =
		// Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
		// Settings.Secure.putString(getContentResolver(),
		// Settings.Secure.DEFAULT_INPUT_METHOD, str);
		// Log.d("xiaoxi","输入法 str:" + str);

		// 利用反射设置语言为英语
		// updateLanguage(Locale.US);

		// Resources resource = getResources();
		// Configuration config = resource.getConfiguration();
		// Locale locale = getResources().getConfiguration().locale;
		// String country = locale.getCountry();
		// Log.d("xiaoxi","country:" + country);
		// config.locale = Locale.ENGLISH;
		// config.locale = Locale.getDefault();
		// getBaseContext().getResources().updateConfiguration(config, null);

		// Locale locale = new Locale("en-US");
		// Locale.setDefault(locale);
		// String country = locale.getCountry();
		// Configuration config = getResources().getConfiguration();
		// DisplayMetrics metrics = getResources().getDisplayMetrics();
		// config.locale = Locale.ENGLISH;
		// Log.d("xiaoxi","country:" + country);
		// getResources().updateConfiguration(config, metrics);
	
//	private void updateLanguage(Locale locale) {
//		try {
//			Object objIActMag, objActMagNative;
//			Class clzIActMag = Class.forName("android.app.IActivityManager");
//			Class clzActMagNative = Class
//					.forName("android.app.ActivityManagerNative");
//			Method mtdActMagNative$getDefault = clzActMagNative
//					.getDeclaredMethod("getDefault");
//			// IActivityManager iActMag = ActivityManagerNative.getDefault();
//			objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
//			// Configuration config = iActMag.getConfiguration();
//			Method mtdIActMag$getConfiguration = clzIActMag
//					.getDeclaredMethod("getConfiguration");
//			Configuration config = (Configuration) mtdIActMag$getConfiguration
//					.invoke(objIActMag);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	// 从assets目录拷贝APK至设备存储
	public boolean copyApkFromAssets(Context context, String fileName,String path) {
		boolean copyIsFinish = false;
		try {
			InputStream is = context.getAssets().open(fileName);
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.close();
			is.close();
			copyIsFinish = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return copyIsFinish;
	}
}
