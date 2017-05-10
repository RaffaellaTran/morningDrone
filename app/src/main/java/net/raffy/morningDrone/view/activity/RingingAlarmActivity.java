package net.raffy.morningDrone.view.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import net.raffy.morningDrone.R;
import net.raffy.morningDrone.model.Alarm;
import net.raffy.morningDrone.morning_drone.AlarmsManager;
import net.raffy.morningDrone.view.AlarmNotificationService;
import net.raffy.morningDrone.view.MediaPlayerService;

import static android.content.ContentValues.TAG;


public class RingingAlarmActivity extends Activity {
	private Window window;
	private AlarmsManager alarmsManger;
	public static final int SNOOZING_NOTIFICATION_ID = 222;
	private int snooze;
	//public Intent intent_music;
	public Vibrator vi;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		window = this.getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		// Make sure this window always shows over the lock screen.
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		window.requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();

		setContentView(R.layout.ringing_alarm_activity);

		long alarmId = getIntent().getLongExtra(Alarm.INTENT_ID, -1);
		alarmsManger = new AlarmsManager(this);
		final Alarm alarm = alarmsManger.getAlarmById(alarmId);

		TextView alarmTime = (TextView) findViewById(R.id.alarm_time);
		alarmTime.setText(alarm.getTime());

		TextView alarmName = (TextView) findViewById(R.id.alarm_name);
		alarmName.setText(alarm.getName());

		final long alarmid = getIntent().getLongExtra(
				AlarmNotificationService.ALARM_ID, DbUtil.Settings.DEFAULTS_ID);
		Log.i(TAG, "Alarm notification intent " + alarmid);


		// Pull snooze from saved state or options database.


	//	final TextView snooze_text = (TextView)findViewById(R.id.snooze_text);
//		snooze_text.setText(getString(R.string.minutes, snooze));



		/*findViewById(R.id.snooze_minus_five).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						snooze -= 5;
						if (snooze <= 0) snooze = 5;
						snooze_text.setText(getString(R.string.minutes, snooze));
					}
				});

		findViewById(R.id.snooze_plus_five).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						snooze += 5;
						if (snooze >= 60) snooze = 60;
						snooze_text.setText(getString(R.string.minutes, snooze));
					}
				});

		findViewById(R.id.snooze_alarm).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						AlarmNotificationService.snoozeAllAlarms(
								getApplicationContext(),
								TimeUtil.nextMinute(snooze).getTimeInMillis());
						finish();
					}
				});*/


		Button snoozeBtn = (Button) findViewById(R.id.btn_snooze_alarm);
		snoozeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final Intent intent= new Intent(getApplicationContext(),
						MediaPlayerService.class);
				intent.putExtra(Alarm.INTENT_ID, alarm.getId());
				stopService(intent);

				alarmsManger.setAlarmSnooze(alarm.getId() + 1000);
				finish();

				final Intent restartRingingActivityIntent = new Intent(
						getApplicationContext(), RingingAlarmActivity.class);
				restartRingingActivityIntent
						.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				restartRingingActivityIntent.putExtra(Alarm.INTENT_ID,
						alarm.getId());
				PendingIntent restartRingingActivityPenInt = PendingIntent
						.getActivity(getApplicationContext(),
								(int) System.currentTimeMillis(),
								restartRingingActivityIntent, 0);

				Notification.Builder notiBuilder = new Notification.Builder(
						RingingAlarmActivity.this)
						.setTicker("Snoozing for 5 more minutes")
						.setSmallIcon(R.drawable.ic_action_onoff_pressed)
						.setContentIntent(restartRingingActivityPenInt)
						.setContentTitle("mp3 alarm")
						.setContentText("Snoozing for 5 more minutes")
						.setOngoing(true);

				NotificationManager notiMng = (NotificationManager) RingingAlarmActivity.this
						.getSystemService(Context.NOTIFICATION_SERVICE);

				notiMng.cancel(MediaPlayerService.RINGING_NOTIFICATION_ID);
				notiMng.cancel(SNOOZING_NOTIFICATION_ID);

				notiMng.notify(SNOOZING_NOTIFICATION_ID, notiBuilder.build());

			}
		});

		Intent intent = new Intent(getApplicationContext(),
				MediaPlayerService.class);
		intent.putExtra(MediaPlayerService.START_PLAY, true);
		intent.putExtra(Alarm.INTENT_ID, alarmId);
		vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Start without a delay
		// Vibrate for 100 milliseconds
		// Sleep for 1000 milliseconds
		long[] pattern = {0, 100, 1000};

		// The '0' here means to repeat indefinitely
		// '0' is actually the index at which the pattern keeps repeating from (the start)
		// To repeat the pattern from any other point, you could increase the index, e.g. '1'
		vi.vibrate(pattern, 0);

		startService(intent);

		Button stopBtn = (Button) findViewById(R.id.btn_stop_alarm);
		stopBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			final	Intent intent = new Intent(getApplicationContext(),
						MediaPlayerService.class);

				stopService(intent);
				//Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vi.cancel();
				alarmsManger.cancelAlarm(alarm.getId() + 1000);

				if (alarm.getMo() || alarm.getTu() || alarm.getWe()
						|| alarm.getTh() || alarm.getFr() || alarm.getSa()
						|| alarm.getSu()) {
					alarmsManger.setAlarm(alarm, true, false);
				} else {
					alarmsManger.setAlarm(alarm, false, false);
				}

				NotificationManager notiMng = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notiMng.cancel(SNOOZING_NOTIFICATION_ID);
				notiMng.cancel(MediaPlayerService.RINGING_NOTIFICATION_ID);
				finish();
			}
		});

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		alarmsManger = new AlarmsManager(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		alarmsManger.close();
		finish();
	}
}
