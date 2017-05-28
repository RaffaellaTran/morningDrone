package net.raffy.morningDrone.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import net.raffy.morningDrone.R;
import net.raffy.morningDrone.model.Alarm;
import net.raffy.morningDrone.view.activity.MainActivity;

import java.io.File;
import java.util.Calendar;

public abstract class AlarmFragment extends Fragment implements AdapterView.OnItemSelectedListener {

	public static final int READ_REQUEST_CODE = 42;

	protected TimePicker timePicker;
	protected CheckBox mo;
	protected CheckBox tu;
	protected CheckBox we;
	protected CheckBox th;
	protected CheckBox fr;
	protected CheckBox sa;
	protected CheckBox su;
	protected Button addMusicBtn;
	protected Button recordBtn;
	protected Button playBtn;
	protected Button playbackChosenMusic;
	protected EditText alarmNameET;
	protected Button saveAlarmBtn;
	private SeekBar alarmVolumeSeekbar = null;
	private AudioManager audioManager = null;
	private Spinner spinner;


	/**
	 * Used to store the chosen mp3 file which the user has selected but not yet
	 * stored in the database. Used only by the player.
	 */
	protected String tempMusicFilePath = null;
	protected boolean tempRecording = false;

	protected abstract OnClickListener getCreateAlarmButtonClickListener();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			final ViewGroup container, Bundle savedInstanceState) {

		final RelativeLayout rootView = (RelativeLayout) inflater.inflate(
				R.layout.add_alarm_fragment, container, false);

		timePicker = (TimePicker) rootView.findViewById(R.id.time_picker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(Calendar.getInstance().get(
				Calendar.HOUR_OF_DAY));

		mo = (CheckBox) rootView.findViewById(R.id.monday);
		tu = (CheckBox) rootView.findViewById(R.id.tuesday);
		we = (CheckBox) rootView.findViewById(R.id.wednesday);
		th = (CheckBox) rootView.findViewById(R.id.thursday);
		fr = (CheckBox) rootView.findViewById(R.id.friday);
		sa = (CheckBox) rootView.findViewById(R.id.saturday);
		su = (CheckBox) rootView.findViewById(R.id.sunday);

		Switch simpleSwitch = (Switch) rootView.findViewById(R.id.drone);

//set the current state of a Switch
		simpleSwitch.setChecked(false);
		//displayed text of the Switch
		simpleSwitch.setText("Drone");
		simpleSwitch.setTextOn("On"); // displayed text of the Switch whenever it is in checked or on state
		simpleSwitch.setTextOff("Off"); // displayed text of the Switch whenever it is in unchecked i.e. off state


        //spinner

        spinner=(Spinner) rootView.findViewById(R.id.height);

        ArrayAdapter adapter= ArrayAdapter.createFromResource(getActivity(), R.array.height, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


		addMusicBtn = (Button) rootView.findViewById(R.id.btn_add_music);
		addMusicBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopRecording();
				stopPlaying();
				performFileSearch();
			}
		});

		recordBtn = (Button) rootView.findViewById(R.id.btn_add_record);
		recordBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopPlaying();

				tempMusicFilePath = null;
				tempRecording = true;

				if (!getMainActivity().getAlarmsManager().getMusicManager()
						.isRecording()) {
					getMainActivity().getAlarmsManager().getMusicManager()
							.recordStart();
					recordBtn.setText("Stop");
					recordBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
							0, R.drawable.ic_action_stop, 0, 0);
				} else {
					stopRecording();
				}
			}
		});

		playbackChosenMusic = (Button) rootView
				.findViewById(R.id.btn_play_selected_ringtone);
		playbackChosenMusic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				stopRecording();

				String result = null;

				if (new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/mp3alarm.3gp").exists()) {

					result = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/mp3alarm.3gp";

				} else if (tempMusicFilePath != null) {
					result = tempMusicFilePath;
				}

				// player.play(musicFilePath);
				// if arguments, then we are in the EditAlarmFragment and can
				// play music

				if (getArguments() != null) {

					int alarmPosition = getArguments().getInt("alarmPosition");
					Alarm alarmForEdit = getMainActivity().getAlarmsManager()
							.getAlarmByPosition(alarmPosition);

					if (!new File(Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/mp3alarm.3gp").exists()
							&& tempMusicFilePath == null) {

						if (alarmForEdit.getMusicPath() == null
								|| alarmForEdit.getMusicPath().length() == 0) {
							result = "file://"
									+ Environment.getExternalStorageDirectory()
											.getAbsolutePath() + "/mp3alarm_"
									+ alarmForEdit.getId() + ".3gp";

						} else {
							result = alarmForEdit.getMusicPath();
						}
					}
				}

				if (result != null) {

					if (!getMainActivity().getAlarmsManager().getMusicManager()
							.isPlaying()) {
						getMainActivity().getAlarmsManager().getMusicManager()
								.playStart(result);
						playbackChosenMusic
								.setCompoundDrawablesRelativeWithIntrinsicBounds(
										0, R.drawable.ic_action_pause, 0, 0);
					} else {
						getMainActivity().getAlarmsManager().getMusicManager()
								.playStop();
						playbackChosenMusic
								.setCompoundDrawablesRelativeWithIntrinsicBounds(
										0, R.drawable.ic_action_play, 0, 0);
					}

				}
			}
		});

		alarmVolumeSeekbar = (SeekBar) rootView
				.findViewById(R.id.seekbar_alarm_volume);
		audioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);
		alarmVolumeSeekbar.setMax(audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		alarmVolumeSeekbar.setProgress(audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC));

		alarmVolumeSeekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
					}

					@Override
					public void onProgressChanged(SeekBar arg0, int progress,
							boolean arg2) {
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
								progress, 0);
					}
				});

		alarmNameET = (EditText) rootView.findViewById(R.id.alarm_name);

		saveAlarmBtn = (Button) rootView.findViewById(R.id.save_alarm_btn);
		saveAlarmBtn.setOnClickListener(getCreateAlarmButtonClickListener());

		return rootView;

	}

	private void stopRecording() {
		getMainActivity().getAlarmsManager().getMusicManager().recordStop();
		recordBtn.setText("Record");
		recordBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
				R.drawable.ic_action_mic, 0, 0);
	}

	private void stopPlaying() {
		getMainActivity().getAlarmsManager().getMusicManager().playStop();
		playbackChosenMusic.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
				R.drawable.ic_action_play, 0, 0);
	}

	public MainActivity getMainActivity() {
		return (MainActivity) super.getActivity();
	}

	public void performFileSearch() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("audio/*");
		startActivityForResult(intent, READ_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// The ACTION_GET_CONTENT intent was sent with the request code
		// READ_REQUEST_CODE. If the request code seen here doesn't match, it's
		// the
		// response to some other intent, and the code below shouldn't run at
		// all.

		if (requestCode == AlarmFragment.READ_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {

			Uri uri = null;
			if (data != null) {
				uri = data.getData();

				tempMusicFilePath = getImagePath(uri);
				tempRecording = false;

				File tempRecordingFile = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/mp3alarm.3gp");
				if (tempRecordingFile.exists()) {
					tempRecordingFile.delete();
				}
			}
		}
	}

	public String getImagePath(Uri uri) {
		Cursor cursor = getActivity().getContentResolver().query(uri, null,
				null, null, null);
		try {
			cursor.moveToFirst();
			String document_id = cursor.getString(0);
			document_id = document_id
					.substring(document_id.lastIndexOf(":") + 1);
			cursor.close();

			cursor = getActivity()
					.getContentResolver()
					.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							null, MediaStore.Images.Media._ID + " = ? ",
							new String[] { document_id }, null);
			cursor.moveToFirst();
			String path = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			return path;
		} finally {
			cursor.close();
		}
	}

	protected void refreshAndClose() {
		getMainActivity().refreshListAdapter();

		FragmentManager fm = getFragmentManager();
		fm.popBackStackImmediate();
	}

	@Override
	/**
	 * Handle onPause to release the media Recorder and Player instances.
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
		getMainActivity().getAlarmsManager().getMusicManager().close();
	}

	@Override
	public void onResume() {
		super.onResume();
	}


	public void setDrone() {


	}

	@Override
    public void onItemSelected(AdapterView <?> adapterView, View view, int i, long l){
        TextView myText = (TextView) view;
        if (myText.getText()==getResources().getStringArray(R.array.height)[1]){

			Toast.makeText(getActivity(), " è giustooooo "+myText.getText(), Toast.LENGTH_SHORT).show();
		}
		else{
		Toast.makeText(getActivity(), "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();}



    }
    @Override
    public void onNothingSelected(AdapterView <?> adapterView){


    }

}

