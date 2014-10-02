package com.softtech.apps.autocallrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.ToggleButton;

public class WidgetIntentReceiver extends BroadcastReceiver {
	public static boolean enable = true; // enable app
	ToggleButton btAppWidget;
	private DatabaseHandler dbHandler;
	public WidgetIntentReceiver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals("com.softtech.apps.autocallrecorder.intent.action.CHANGE_PICTURE")) {
			dbHandler = new DatabaseHandler(context);
			boolean extra = intent.getBooleanExtra("update",true);
			updateWidgetPictureAndButtonListener(context,extra);
			
		}
	}
	private void updateWidgetPictureAndButtonListener(Context context, boolean update) {
		Log.d("CLICK", "Da click vao widget");
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.app_widget);
			remoteViews.setImageViewResource(R.id.btAppWidget, getImageToSet(update));
		// re-registering for click listener
		remoteViews.setOnClickPendingIntent(R.id.btAppWidget,
				WidgetProvider.buildButtonPendingIntent(context));
		WidgetProvider.pushWidgetUpdate(context.getApplicationContext(),
				remoteViews);
	}
	private int getImageToSet(boolean update) {
		// Update database here
		Log.d("DATA", "Update to database !!");
		Config cfg = dbHandler.getConfig(1);
		enable = cfg.get_value() == 1 ? true : false;
		if(update){
			enable = !enable;
			int value = (enable == true) ? 1 : 0;
			cfg.set_value(value);
			dbHandler.updateConfig(cfg);
		}
		return enable == true ? R.drawable.ic_syncon : R.drawable.ic_syncoff;
	}
}
