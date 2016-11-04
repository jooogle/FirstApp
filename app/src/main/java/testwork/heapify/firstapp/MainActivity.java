package testwork.heapify.firstapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	private static final String FORMAT_RECEIVE =
			"FirstApp:: Received info at <%s> from process <%d>, ScreenState=<ScreenState(%d)>";
	private static final String FORMAT_SEND =
			"FirstApp:: Sent info at <%s>, WiFiState=<WiFiState(%d)>";

	public static final String EXTRA_SCREEN_STATE = "SCREEN_STATE";
	public static final String EXTRA_PID = "PID";

	public static final String ACTION_START_ACTIVITY =
			"testwork.heapify.firstapp.START_ACTIVITY";

	private static final String ACTION_BIND_SERVICE =
			"testwork.heapify.secondapp.BIND_SERVICE";
	private static final String SERVICE_PACKAGE =
			"testwork.heapify.secondapp";

	private static final int MESSAGE_INFO = 1;

	private Button mStartServiceButton;
	private Button mSendInfoButton;
	private TextView mInfoTextView;

	private Messenger mService;
	private boolean mBounded;

	public static Intent newIntent(Context context, int pId, int screenState) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra(EXTRA_PID, pId);
		intent.putExtra(EXTRA_SCREEN_STATE, screenState);

		return intent;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			mBounded = true;
			mService = new Messenger(iBinder);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBounded = false;
			mService = null;
		}
	};

	private BroadcastReceiver mOnWriteToTextView = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Activity already started, not create new activity in ActivityStartReceiver
			setResultCode(Activity.RESULT_CANCELED);

			logInfoReceive(intent);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mStartServiceButton = (Button) findViewById(R.id.start_service_button);
		mSendInfoButton = (Button) findViewById(R.id.send_info_button);
		mInfoTextView = (TextView) findViewById(R.id.info_text_view);

		mStartServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mBounded)
					return;

				Intent i = new Intent(ACTION_BIND_SERVICE);
				i.setPackage(SERVICE_PACKAGE);

				bindService(i, mConnection, BIND_AUTO_CREATE);
			}
		});

		mSendInfoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mBounded) return;

				int pId = Process.myPid();
				int isWifiEnabled = isWifiEnabled() ? 1 : 0;
				Message msg = Message.obtain(null, MESSAGE_INFO, pId, isWifiEnabled);
				try {
					mService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

				logInfoSend(isWifiEnabled);
			}
		});

		Intent intent = getIntent();
		logInfoReceive(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter intentFilter = new IntentFilter(ACTION_START_ACTIVITY);
		registerReceiver(mOnWriteToTextView, intentFilter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mOnWriteToTextView);
	}

	private boolean isWifiEnabled() {
		WifiManager wifiManager =
				(WifiManager) getSystemService(WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	private void logInfoReceive(Intent intent) {
		int pId = intent.getIntExtra(EXTRA_PID, -1);
		int screenState = intent.getIntExtra(EXTRA_SCREEN_STATE, -1);

		if (pId > 0 && screenState > 0) {
			String logString = String.format(FORMAT_RECEIVE, new Date().toString(), pId, screenState);

			writeLog(logString);
		}
	}

	private void logInfoSend(int wiFiState) {
		String logString = String.format(FORMAT_SEND, new Date().toString(), wiFiState);

		writeLog(logString);
	}

	private void writeLog(String logString) {
		mInfoTextView.setText(logString);
		Log.d(TAG, logString);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBounded) {
			unbindService(mConnection);
			mBounded = false;
		}
	}
}
