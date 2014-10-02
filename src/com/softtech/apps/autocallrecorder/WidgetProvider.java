package com.softtech.apps.autocallrecorder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	private DatabaseHandler dbHandler;
	public WidgetProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// initializing widget layout
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.app_widget);
		Log.d("INIT", "############ On widget init package ="+context.getPackageName());
		dbHandler = new DatabaseHandler(context);
		Config cfg = dbHandler.getConfig(1);
		if(cfg.get_value() == 1){
			remoteViews.setImageViewResource(R.id.btAppWidget, R.drawable.ic_syncon);
		}else{
			remoteViews.setImageViewResource(R.id.btAppWidget, R.drawable.ic_syncoff);
		}
		// register for button event
		remoteViews.setOnClickPendingIntent(R.id.btAppWidget,buildButtonPendingIntent(context));
		// request for widget update
		pushWidgetUpdate(context, remoteViews);
	}
	public static PendingIntent buildButtonPendingIntent(Context context) {
		Intent intent = new Intent();
		intent.setAction("com.softtech.apps.autocallrecorder.intent.action.CHANGE_PICTURE");
		return PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context,
				WidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myWidget, remoteViews);
	}
}
