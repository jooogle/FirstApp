package testwork.heapify.firstapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InformationReceiver extends BroadcastReceiver {
	public InformationReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int pId = intent.getIntExtra(MainActivity.EXTRA_PID, -1);
		int screenState = intent.getIntExtra(MainActivity.EXTRA_SCREEN_STATE, -1);

		Intent i = new Intent(MainActivity.ACTION_START_ACTIVITY);
		i.putExtra(MainActivity.EXTRA_PID, pId);
		i.putExtra(MainActivity.EXTRA_SCREEN_STATE, screenState);

		// Launch MainActivity and log info in it or not create MainActivity if it already created and top
		context.sendOrderedBroadcast(i, null, null, null, Activity.RESULT_OK,
				null, null);
	}
}
