package net.raffy.mp3alarmclock.bluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.Locale;


public class MyListActivityBluetooth extends ListActivity {
    /* Get Default Adapter */
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refresh();
        _bluetooth.enable();
    }

    public void refresh() {
        SharedPreferences settings = getSharedPreferences("locale", 0);
        int position = settings.getInt("Locale", 1);
        Locale locale2;
        Configuration config = new Configuration();
        if (position == 0) {
            locale2 = Locale.ENGLISH;
            Locale.setDefault(locale2);
            config.locale = locale2;
        }
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }
}
