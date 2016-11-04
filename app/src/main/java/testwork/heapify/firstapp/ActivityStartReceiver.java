package testwork.heapify.firstapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ActivityStartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (getResultCode() != Activity.RESULT_OK) {
			return;
		}

		int pId = intent.getIntExtra(MainActivity.EXTRA_PID, -1);
		int screenState = intent.getIntExtra(MainActivity.EXTRA_SCREEN_STATE, -1);
		context.startActivity(MainActivity.newIntent(context, pId, screenState));
	}
}
