package com.mars.miuifilemanager.ui;

import java.io.File;
import java.net.InetAddress;

import org.swiftp.Defaults;
import org.swiftp.Globals;
import org.swiftp.UiUpdater;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.wooboo.adlib_android.WoobooAdView;
import com.mars.miuifilemanager.service.FTPServerService;
import com.mars.miuifilemanager.utils.ActivitiesManager;
import com.mars.miuifilemanager.R;

public class ServerControlActivity extends Activity {
	private static final String TAG = "ServerControlActivity";
	
	private TextView mIpText;
	
	private TextView mInstructionText;
	
	private TextView mInstructionTextPre;
	
	private View mStartStopButton;
	
	//private WoobooAdView mAdView;
	
	private static final int MSG_ID_UPDATE 	= 0;
	private static final int MSG_ID_IDLE 	= 1;
	private static final int MSG_ID_AD		= 2;
	public Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ID_UPDATE:
				removeMessages(MSG_ID_UPDATE);
				updateUi();
				break;
			case MSG_ID_IDLE:
				break;
			case MSG_ID_AD:
				/*
		        LinearLayout layout = (LinearLayout)findViewById(R.id.server_pannel);
		        mAdView = new WoobooAdView(ServerControlActivity.this, "e7bcf32c331b4727b2990aeccb5a0c8e",0xFF000000, 0xFFFFFFFF, false, 60, null);
				LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				mAdView.setLayoutParams(params);
				layout.addView(mAdView);	
				*/			
				break;
			}
		}
	};
	
	OnClickListener mStartStopListener = new OnClickListener(){
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			Globals.setLastError(null);
			
			File file = new File(Defaults.chrootDir);
			if (!file.isDirectory()) {
				return;
			}
			
			Context context = getApplicationContext();
			Intent intent = new Intent(context, FTPServerService.class);
			Globals.setChrootDir(file);
			
			if (!FTPServerService.isRunning()) {
				warnIfNoExternalStorage();
				context.startService(intent);
				
				return;
			}
			
			context.stopService(intent);
			
			FileViewActivity activity = (FileViewActivity)ActivitiesManager.getInstance().getActivity("FileView");
			if (activity != null) {
				activity.refresh();
			}
		}
	};
	
	BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			updateUi();
		}
		
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.e(TAG, "onCreate!");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		if (Globals.getContext() == null)
		{
			Context context = getApplicationContext();
			if (context == null)
				throw new NullPointerException("Null context!?!?!?");
			Globals.setContext(context);
		}
		
        setContentView(R.layout.server_control_activity);
        
        mIpText = (TextView)findViewById(R.id.ip_address); // 0x7f08004f
        
        mInstructionText = (TextView)findViewById(R.id.instruction); // 0x7f08004e
        
        mInstructionTextPre = (TextView)findViewById(R.id.instruction_pre); // 0x7f08004d
        
        mStartStopButton = findViewById(R.id.start_stop_button);
        
        mStartStopButton.setOnClickListener(mStartStopListener);
        
        updateUi();
        
        mHandler.sendEmptyMessageDelayed(MSG_ID_AD, 1000);
    }
    
    protected void onResume() {
    	super.onResume();
    	
		UiUpdater.registerClient(mHandler);
		updateUi();
		
		IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION );
		registerReceiver(mWifiReceiver, filter);
		
		Log.e(TAG, "onResume");
    }
    
	protected void onDestroy() {
		super.onDestroy();
		
		Log.e(TAG, "onDestroy!");
		UiUpdater.unregisterClient(mHandler);
	}
	
	protected void onPause() {
		super.onPause();
		
		UiUpdater.unregisterClient(mHandler);
		
		if (mWifiReceiver != null) {
			unregisterReceiver(mWifiReceiver);
		}
		
		Log.e(TAG, "onPause");
	}
	
	protected void onStart() {
		super.onStart();
		
		UiUpdater.registerClient(mHandler);
		updateUi();		
		
		Log.e(TAG, "onStart");
	}
	
	protected void onStop() {
		super.onStop();
		
		UiUpdater.unregisterClient(mHandler);
		
		Log.e(TAG, "onStop");
	}
    
    public boolean onSearchRequested() {
    	return false;
    }
    
    public void updateUi() {
    	WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    	int state = wifiManager.getWifiState();
    	String ssid = null;
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	if (wifiInfo != null) {
    		ssid = wifiInfo.getSSID();
    	}
    	
    	boolean wifiEnabled = FTPServerService.isWifiEnabled();
    	if (!wifiEnabled) {
    		setText(R.id.wifi_state,getString(R.string.no_wifi)); //0x7f08004b, 0x7f060028
    	} else {
    		setText(R.id.wifi_state,ssid);
    	}
    	
    	ImageView image = (ImageView)findViewById(R.id.wifi_state_image); //0x7f08004c
    	if (wifiEnabled) {
    		image.setImageResource(R.drawable.wifi_state4); // 0x7f020062
    	} else {
    		image.setImageResource(R.drawable.wifi_state0); //0x7f020061
    	}
    	
    	boolean isRunning = FTPServerService.isRunning();
    	if (isRunning) {
    		InetAddress address = FTPServerService.getWifiIp();
    		if (address != null) {
    			if (FTPServerService.getPort() != 21) {
    				Log.v(TAG,"address[" + address + "]");
    				mIpText.setText("ftp://" + address.getHostAddress() + ":" + FTPServerService.getPort());
    			} else {
    				mIpText.setText("ftp://" + address.getHostAddress());
    			}
    		} else {
    			Context context = getApplicationContext();
    			Intent intent = new Intent(context, FTPServerService.class);
    			context.stopService(intent);
    			mIpText.setText("");
    		}
    	}
    	
    	mStartStopButton.setEnabled(wifiEnabled);
    	
    	TextView textView = (TextView)findViewById(R.id.start_stop_button_text); //0x7f080051
    	if (wifiEnabled) {
    		if (isRunning) {
    			textView.setText(R.string.stop_server);
    			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disconnect, 0, 0, 0);
    			textView.setTextColor(getResources().getColor(R.color.black));
    		} else {
    			textView.setText(R.string.start_server);
    			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.connect, 0, 0, 0);
    			textView.setTextColor(getResources().getColor(R.color.red));
    		}
    	} else {
    		if (FTPServerService.isRunning()) {
    			Context context = getApplicationContext();
    			Intent intent = new Intent(context, FTPServerService.class);
    			context.stopService(intent);
    		}
    		
    		textView.setText(R.string.no_wifi); // 0x7f060028
    		textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    		textView.setTextColor(0xff888888);
    	}
    	
    	if (FTPServerService.isRunning()) {
    		mIpText.setVisibility(View.VISIBLE);
    		mInstructionText.setVisibility(View.VISIBLE);
    		mInstructionTextPre.setVisibility(View.GONE);
    	} else {
    		mIpText.setVisibility(View.INVISIBLE);
    		mInstructionText.setVisibility(View.GONE);
    		mInstructionTextPre.setVisibility(View.VISIBLE);
    	}
    }

    private void setText(int id, String text) {
    	((TextView)findViewById(id)).setText(text);
    }
    
    private void warnIfNoExternalStorage() {
    	if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		return;
    	}
    	
    	Toast toast = Toast.makeText(this, R.string.storage_warning, Toast.LENGTH_LONG);
    	
    	toast.setGravity(17, 0, 0);
    	toast.show();
    	return;
    }
}