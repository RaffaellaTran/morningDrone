package net.raffy.morningDrone.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import net.raffy.morningDrone.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;




public class MonitorActivityBluetooth extends MyActivityBluetooth{
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button btnclear,button1,button15,btnStart;
    private final String TAG = "MonitorActivity";
    Spinner spnsettings;
    String str1="",str2,str3="",str4,str5="",str6,str7="",str8,str9="",str10;
    String strpos,str,finalstr,finalstrsetting,finalstring;
    ArrayAdapter<String> adp1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_monitor);

        button1=(Button) findViewById(R.id.button1);

        BluetoothDevice finalDevice = this.getIntent().getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE);

        SocketApplicationBluetooth app = (SocketApplicationBluetooth) getApplicationContext();
        device = app.getDevice();
        if (finalDevice == null) {
            if (device == null) {
                Intent intent = new Intent(this, SearchDeviceActivityBluetooth.class);
                startActivity(intent);
                finish();
                return;
            }
        } else if (finalDevice != null) {
            app.setDevice(finalDevice);
            device = app.getDevice();
        }
        new Thread() {
            public void run() {
                connect(device);
            };
        }.start();
    }


    ///////////////////////////////////////////////

    public void onButtonClicksend1(View view) {
        String editText =   "*5";//SMSes.getText().toString();
        byte bytes[] = editText.getBytes();
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
            } else {
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.wait),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, ">>", e);
            e.printStackTrace();
        }
    }

    /* after select, connect to device */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (requestCode != REQUEST_DISCOVERY) {
//			finish();
//			return;
//		}
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }
        final BluetoothDevice device = data
                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        new Thread() {
            public void run() {
                connect(device);
            };
        }.start();
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, ">>", e);
        }
    }

    protected void connect(BluetoothDevice device) {
        try {
            // Create a Socket connection: need the server's UUID number of
            // registered
            socket = device.createRfcommSocketToServiceRecord(UUID
                    .fromString("00001101-0000-1000-8000-00805F9B34FB"));

            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            int read;
            final byte[] bytes = new byte[2048];
            for (; (read = inputStream.read(bytes)) > 0;) {

            }

        } catch (IOException e) {
            Log.e(TAG, ">>", e);
            Toast.makeText(getBaseContext(),
                    getResources().getString(R.string.ioexception),
                    Toast.LENGTH_SHORT).show();
            return;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    finish();
                    return;
                } catch (IOException e) {
                    Log.e(TAG, ">>", e);
                }
            }
        }
    }
}