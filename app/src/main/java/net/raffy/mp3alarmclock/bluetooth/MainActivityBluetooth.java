package net.raffy.mp3alarmclock.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import net.raffy.mp3alarmclock.R;


public class MainActivityBluetooth extends MyActivityBluetooth {
    Button btnSetting;
    Button btnMonitor;
    Button btnExit;
    /* Get Default Adapter */
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_main);
        //Setting
         /*  btnSetting = (Button) findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {

            @Override
         public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });*/


        //Monitor
        btnMonitor = (Button) findViewById(R.id.btnMonitor);
        btnMonitor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                _bluetooth.enable();
                Intent intent = new Intent();
                intent.setClass(MainActivityBluetooth.this, MonitorActivityBluetooth.class);
                startActivity(intent);
            }
        });

        //Exit
        btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });
    }

    /**
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            dialog();
            return false;
        }
        return false;
    }

    /**
     * ��ʾ��
     */
    protected void dialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(MainActivityBluetooth.this);
        build.setTitle(R.string.message);
        build.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (_bluetooth.isEnabled()) {
                            _bluetooth.disable();
                        }
                        SocketApplicationBluetooth app = (SocketApplicationBluetooth) getApplicationContext();
                        app.setDevice(null);
                        MainActivityBluetooth.this.finish();
                    }
                });
        build.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        build.create().show();
    }

    @Override
    /**
     * �˳�ʱ��ս���
     */
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}