package com.example.setcts;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class setCTSService extends Service{

	private ContentResolver mContentResolver ;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
//		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");  
//		wakeLock.acquire(); 
//		wakeLock.release();
		try {
            float result  = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Log.i("xiaoxi", "result = " + result);
//            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 10*60*1000);
          //长亮屏幕
    		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
		unLock();
		
		//亮屏幕
//		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
		//打开usb调试
		//Settings.Global.putInt(getContentResolver(),  Settings.Global.ADB_ENABLED, 1);
		//允许模拟位置       
		Settings.Secure.putInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 1);
	}

	public void unLock(){
        mContentResolver = getContentResolver();  
        setLockPatternEnabled(Settings.Secure.LOCK_PATTERN_ENABLED,false);  
    }
	
	private void setLockPatternEnabled(String systemSettingKey, boolean enabled) {  
        Settings.Secure.putInt(mContentResolver, systemSettingKey,enabled ? 1 : 0);  
   }
}
