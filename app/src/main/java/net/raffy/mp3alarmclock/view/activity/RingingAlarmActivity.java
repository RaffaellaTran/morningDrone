package net.raffy.mp3alarmclock.view.activity;

import android.app.AlarmManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import net.raffy.mp3alarmclock.R;
import net.raffy.mp3alarmclock.model.Alarm;
import net.raffy.mp3alarmclock.morning_drone.AlarmsManager;
import net.raffy.mp3alarmclock.view.AlarmNotificationService;
import net.raffy.mp3alarmclock.view.MediaPlayerService;
import static android.content.ContentValues.TAG;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RingingAlarmActivity extends Activity  {

	//private Window window;
	private AlarmsManager alarmsManger;
	public static final int SNOOZING_NOTIFICATION_ID = 222;
	private int snooze;
	private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preventStatusBarExpansion(getApplicationContext());
		Window window = this.getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		// Make sure this window always shows over the lock screen.
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		window.requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();


		setContentView(R.layout.ringing_alarm_activity);
		final View view = (View) findViewById(android.R.id.content);
		if (view != null) {
			//"hides" back, home and return button on screen.
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE |
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
					View.SYSTEM_UI_FLAG_IMMERSIVE |
					View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
					View.SYSTEM_UI_FLAG_FULLSCREEN);
			view.setOnSystemUiVisibilityChangeListener
					(new View.OnSystemUiVisibilityChangeListener() {
						@Override
						public void onSystemUiVisibilityChange(int visibility) {
							// Note that system bars will only be "visible" if none of the
							// LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
							if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
								view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE |
										View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
										View.SYSTEM_UI_FLAG_IMMERSIVE |
										View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
										View.SYSTEM_UI_FLAG_FULLSCREEN);
							}
						}
					});
		}



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


		final TextView snooze_text = (TextView)findViewById(R.id.snooze_text);
		snooze_text.setText(getString(R.string.minutes, snooze));



		findViewById(R.id.snooze_minus_five).setOnClickListener(
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

		Button snoozeBtn = (Button) findViewById(R.id.btn_snooze_alarm);
		snoozeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(getApplicationContext(),
						MediaPlayerService.class);
				intent.putExtra(Alarm.INTENT_ID, alarm.getId());
				stopService(intent);

				alarmsManger.setAlarmSnooze(alarm.getId() + 1000);
				finish();
				Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vi.cancel();

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
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Start without a delay
		// Vibrate for 100 milliseconds
		// Sleep for 1000 milliseconds
		long[] pattern = {0, 100, 1000};

		// The '0' here means to repeat indefinitely
		// '0' is actually the index at which the pattern keeps repeating from (the start)
		// To repeat the pattern from any other point, you could increase the index, e.g. '1'
		v.vibrate(pattern, 0);

		startService(intent);

		Button stopBtn = (Button) findViewById(R.id.btn_stop_alarm);
		stopBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(getApplicationContext(),
						MediaPlayerService.class);
				stopService(intent);
				Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
				vi.cancel();
			}
		});

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (blockedKeys.contains(event.getKeyCode())) {
			return true;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	public void onBackPressed() {
		//DO NOTHING
		//... REALLY
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(!hasFocus) {
			// Close every kind of system dialog
			Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
			sendBroadcast(closeDialog);
		}
	}

	@Override
	protected void onUserLeaveHint() {
		long alarmId = getIntent().getLongExtra(Alarm.INTENT_ID, -1);
		alarmsManger = new AlarmsManager(this);
		final Alarm alarm = alarmsManger.getAlarmById(alarmId);

		Intent intent = new Intent(getApplicationContext(),
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
			forceHome(this, restartRingingActivityIntent);

		super.onUserLeaveHint();
	}

	public static void forceHome(Context paramContext, Intent paramIntent) {
		((AlarmManager) paramContext.getSystemService(Context.ALARM_SERVICE)).set(1,
				System.currentTimeMillis(),
				PendingIntent.getActivity(paramContext, 0, paramIntent, 0));
		/*try {
			PendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}*/

	}

	public static void preventStatusBarExpansion(Context context) {
		WindowManager manager = ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE));

		WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
		localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		localLayoutParams.gravity = Gravity.TOP;
		localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

		localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

		int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		int result = 0;
		if (resId > 0) {
			result = context.getResources().getDimensionPixelSize(resId);
		} else {
			// Use Fallback size:
			result = 60; // 60px Fallback
		}

		localLayoutParams.height = result;
		localLayoutParams.format = PixelFormat.TRANSPARENT;

		CustomViewGroup view = new CustomViewGroup(context);
		manager.addView(view, localLayoutParams);
	}

	public static class CustomViewGroup extends ViewGroup {
		public CustomViewGroup(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			// Intercepted touch!
			return true;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
