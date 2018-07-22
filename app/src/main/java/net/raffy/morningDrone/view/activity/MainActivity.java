package net.raffy.morningDrone.view.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;

import net.raffy.morningDrone.R;
import net.raffy.morningDrone.morning_drone.AlarmsManager;
import net.raffy.morningDrone.morning_drone.Bluetooth2;
import net.raffy.morningDrone.view.AlarmsListArrayAdapter;
import net.raffy.morningDrone.view.fragment.AddAlarmFragment;
import net.raffy.morningDrone.view.fragment.AlarmsListFragment;
import net.raffy.morningDrone.view.fragment.EditAlarmFragment;


public class MainActivity extends Activity implements
		AlarmsListFragment.OnAlarmSelectedListener {

	public static Intent newAlarmsListFragment(Activity activity) {
		Intent intent = new Intent();
		intent.setClass(activity, AlarmsListFragment.class);
		return intent;
	}

	private AlarmsListArrayAdapter alarmAdapter;
	private AlarmsManager alarmsManager;
	private AlarmsListFragment alarmsListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);


		initializeAlarmsManager();
		refreshListAdapter();

		if (savedInstanceState == null) {
			if (alarmsListFragment == null) {

				alarmsListFragment = new AlarmsListFragment();

				refreshListAdapter();

				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.add(R.id.fragment_container, alarmsListFragment,
						"alarms_list");
				ft.commit();
				fm.executePendingTransactions();

			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}


	//for the menu add from the other alarm app

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_about:
				Intent intent = new Intent(this, AboutActivity.class);
				startActivity(intent); 	return true;
			case R.id.bluetooth:
				Intent intent2 = new Intent(this, Bluetooth2.class);
				startActivity(intent2); 	return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		initializeAlarmsManager();
		getAlarmsManager().openAlarmsDbHelper();
		refreshListAdapter();
	}


	@Override
	protected void onStop() {
		super.onStop();
		getAlarmsManager().close();
	}


	public AlarmsManager getAlarmsManager() {
		return alarmsManager;
	}

	private void initializeAlarmsManager() {
		if (alarmsManager == null)
			alarmsManager = new AlarmsManager(this);
	}

	public void openAddAlarmFragment() {

		AddAlarmFragment addAlarmFragment = new AddAlarmFragment();

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.replace(R.id.fragment_container, addAlarmFragment,
				"addAlarmFragment").addToBackStack("addAlarmFragment").commit();
		getFragmentManager().executePendingTransactions();
	}

	@Override
	public void openEditAlarmFragment(int position) {

		EditAlarmFragment editAlarmFragment = new EditAlarmFragment();

		Bundle b = new Bundle();
		b.putInt("alarmPosition", position);

		editAlarmFragment.setArguments(b);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.replace(R.id.fragment_container, editAlarmFragment)
				.addToBackStack(null).commit();
		getFragmentManager().executePendingTransactions();
	}

	public void refreshListAdapter() {
		if (alarmAdapter == null) {
			alarmAdapter = new AlarmsListArrayAdapter(this,
					R.layout.alarm_listview_row, alarmsManager.getAllAlarms(),
					this);
		}
		alarmAdapter.clear();
		alarmAdapter.addAll(getAlarmsManager().getAllAlarms());
		alarmAdapter.notifyDataSetChanged();

	}

	public ListAdapter getListAdaptor() {
		return alarmAdapter;
	}
}
