package net.raffy.mp3alarmclock.view.activity;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import net.raffy.mp3alarmclock.R;
import net.raffy.mp3alarmclock.model.Alarm;
import net.raffy.mp3alarmclock.morning_drone.AlarmsManager;
import net.raffy.mp3alarmclock.view.AlarmNotificationService;
import net.raffy.mp3alarmclock.view.MediaPlayerService;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
//import R.id.action_settings;
import net.raffy.mp3alarmclock.model.HomeKeyLocker;


public class RingingAlarmActivity extends Activity  {

	//private ToggleButton mTbLock;
	//private HomeKeyLocker mHomeKeyLocker;


	private Window window;
	private AlarmsManager alarmsManger;
	public static final int SNOOZING_NOTIFICATION_ID = 222;
	private int snooze;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//mHomeKeyLocker = new HomeKeyLocker();
		//mTbLock = (ToggleButton) findViewById(R.id.tb_lock);
		//mTbLock.setOnCheckedChangeListener(this);

		window = this.getWindow();

		// Make sure this window always shows over the lock screen.
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		window.requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();

		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

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

		/*findViewById(R.id.snooze_alarm).setOnClickListener(
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
			}
		});

	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				if (action == KeyEvent.ACTION_DOWN) {
					//TODO
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (action == KeyEvent.ACTION_DOWN) {
					//TODO
				}
				return true;
			/*case KeyEvent.KEYCODE_HOME:
				if (action == KeyEvent.ACTION_DOWN) {
					//TODO
				}
				return true;
							//return true;
*/
			default:
				return super.dispatchKeyEvent(event);
		}
	}

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_HOME)
		{
			onStop();
		}
		return super.onKeyDown(keyCode, event);
	}*/


	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		//finish();
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mTbLock) {
			if (isChecked) {
				mHomeKeyLocker.lock(this);
			} else {
				mHomeKeyLocker.unlock();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHomeKeyLocker.unlock();
		mHomeKeyLocker = null;
	}
*/

	/*@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_HOME)
		{
			//The Code Want to Perform.
		}
		return super.onKeyDown(keyCode, event);
	}


*/
/*	@Override
	protected void onUserLeaveHint()
	{
		Log.d("onUserLeaveHint","Home button pressed");
		super.onUserLeaveHint();
	}
*/

////
@Override
public void onWindowFocusChanged(boolean hasFocus) {
	super.onWindowFocusChanged(hasFocus);
	if(!hasFocus) {
		// Close every kind of system dialog
		Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		sendBroadcast(closeDialog);
	}
}
////

	@Override
	protected void onResume() {
		super.onResume();
		//alarmsManger = new AlarmsManager(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//alarmsManger.close();
		//finish();
        SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString("lastActivity", getClass().getName());
        editor.commit();

	}
	@Override
	protected void onStop() {

		super.onStop();
	}
}
