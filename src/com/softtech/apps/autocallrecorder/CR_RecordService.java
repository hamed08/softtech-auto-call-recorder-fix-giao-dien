package com.softtech.apps.autocallrecorder;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

// This class to excute record incomming call when receiver receive an incomming call
public class CR_RecordService extends Service {

	public static final String LISTEN_ENABLED = "ListenEnabled";
	public static final String FILE_DIRECTORY = "softtech";
	private MediaRecorder recorder = null;
	private String phoneNumber = null;
	public static final int STATE_INCOMING_NUMBER = 0;
	public static final int STATE_CALL_START = 1;
	public static final int STATE_CALL_END = 2;
	//public static int AUDIO_MODE = AudioManager.MODE_NORMAL;

	private NotificationManager manger;

	private String myFileName;

	private Boolean is_offhook = false;

	private int notificationID = 100;

	private int numMessages = 0;

	String tag = "AUTO_ANSWER_PHONE_CALL";

	AudioManager am; // Audio manager
	//int old_mode;
	public CR_RecordService() {
		// TODO Auto-generated constructor stub
		Log.d(tag, "Da start service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		int commandType = 0;
		if(intent != null){
			commandType = intent.getIntExtra("commandType",
				STATE_INCOMING_NUMBER);
		}
		if (commandType == STATE_INCOMING_NUMBER) {
			if (phoneNumber == null)
				phoneNumber = intent.getStringExtra("phoneNumber");
		} else if (commandType == STATE_CALL_START) {
			if (phoneNumber == null)
				phoneNumber = intent.getStringExtra("phoneNumber");
			Log.d(tag,"########## State CALL start ###########");
			
			Toast toast = Toast.makeText(this,this.getString(R.string.reciever_start_call),
					Toast.LENGTH_SHORT);
					toast.show();
			
			 Thread recordThread = new Thread(new Runnable(){

				    @Override
				    public void run() {
				    Log.d(tag, "Thread record started !!!!!!!!! ");
				    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				     startRecord();
				    }
				    
			});
				   
				   recordThread.start();

		} else if (commandType == STATE_CALL_END) {
			Log.d(tag, "Nhan command ket thuc");
			try {
				if (is_offhook == true) {
					stopRecord();

				}
				Toast toast = Toast.makeText(this,
						this.getString(R.string.reciever_end_call),
						Toast.LENGTH_SHORT);
				toast.show();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			if (manger != null)
				manger.cancel(0);
			stopForeground(true);
			this.stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void startRecord() {
		
		try {
			Log.d("RECORD", "------> Chuan bi moi thu den ghi am");
			if(recorder == null){
				Log.d(tag, "Recorder = null ");
			}else{
				Log.d(tag, "Recorder # null ");
			}
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setMaxFileSize(1024 * 1024 * 250); // 250 MB
			myFileName = getFilename();
			recorder.setOutputFile(myFileName);
			am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			//AUDIO_MODE = am.getMode(); // old mode to restore
			//old_mode = am.getMode();
			//am.setMode(AudioManager.MODE_IN_CALL);
			int volume_level = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
			int max_volume = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
			if(volume_level < max_volume){
				am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,max_volume, AudioManager.FLAG_SHOW_UI);
			}
			Log.d(tag, "Duong dan file = " + myFileName);
		} catch (IllegalStateException e) {
			// Log.e("Call recorder IllegalStateException: ", "");
			terminateAndEraseFile();
		} catch (Exception e) {
			// Log.e("Call recorder Exception: ", "");
			terminateAndEraseFile();
		}
		
		OnErrorListener errorListener = new OnErrorListener() {

			public void onError(MediaRecorder arg0, int arg1, int arg2) {
				Log.e("Call recorder OnErrorListener: ", arg1 + "," + arg2);
				arg0.stop();
				arg0.reset();
				arg0.release();
				arg0 = null;
				terminateAndEraseFile();
			}

		};
		recorder.setOnErrorListener(errorListener);
		OnInfoListener infoListener = new OnInfoListener() {

			public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
				Log.e("Call recorder OnInfoListener: ", arg1 + "," + arg2);
				arg0.stop();
				arg0.reset();
				arg0.release();
				arg0 = null;
				terminateAndEraseFile();
			}

		};
		recorder.setOnInfoListener(infoListener);

		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			recorder.start();
			is_offhook = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		createNotification(phoneNumber);
		Log.d(tag, "bat dau ghi am");
	}

	private void stopRecord() {
		if(recorder != null){
			Log.d(tag, "Ro rang recorder khong null -> stop");
		}else{
			Log.d(tag, "Recorder null khong the stop duoc");
		}
		
		if (recorder != null) {
			recorder.setOnErrorListener(null);
			recorder.setOnInfoListener(null);
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
			//am.setMode(old_mode);
			is_offhook = false;
		}
		// System.gc();
	}

	/**
	 * in case it is impossible to record Check device can surport record
	 */
	private void terminateAndEraseFile() {
		try {

			stopRecord();

			Toast toast = Toast.makeText(this,
					this.getString(R.string.reciever_end_call),
					Toast.LENGTH_SHORT);
			toast.show();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		File file = new File(myFileName);

		if (file.exists()) {
			file.delete();

		}
		Toast toast = Toast.makeText(this,
				this.getString(R.string.record_impossible), Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private String getFilename() {
		// String filepath = getFilesDir().getAbsolutePath();
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, FILE_DIRECTORY);

		if (!file.exists()) {
			file.mkdirs();
		}

		String myDate = new String();
		myDate = (String) DateFormat.format("yyyyMMddkkmmss", new Date());

		return (file.getAbsolutePath() + "/allcalls/" + myDate + "-"
				+ phoneNumber + "-isSync0-.mp3");
	}
	@SuppressLint("NewApi")
	public void createNotification(String phoneNumber) {

		RemoteViews remoteViews = new RemoteViews(getPackageName(),
				R.layout.widget);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.icon).setContent(remoteViews);
		remoteViews.setTextViewText(R.id.tvNotificationTitle, "New recorded");
		String content = "Just record a call with " + phoneNumber;
		remoteViews.setTextViewText(R.id.tvNotificationContent, content);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder
				.create(getApplicationContext());
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.tvNotificationTitle,
				resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(100, mBuilder.build());
	}

	    public void cancelNotification(int notificationId){

	        if (Context.NOTIFICATION_SERVICE!=null) {
	            String ns = Context.NOTIFICATION_SERVICE;
	            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
	            nMgr.cancel(notificationId);
	        }
	    }

}
