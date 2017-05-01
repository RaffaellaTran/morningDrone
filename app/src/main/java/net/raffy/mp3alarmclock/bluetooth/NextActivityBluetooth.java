package net.raffy.mp3alarmclock.bluetooth;

import android.app.Activity;
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

import net.raffy.mp3alarmclock.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class NextActivityBluetooth extends Activity{

    Spinner sp;
    ArrayAdapter<String> adp1;
    String str1;
    private String upstr= "fly \\r\\n";
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final String TAG = "MonitorActivity";
    Button btnsend,btnmain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity_next);

         Intent i1 = getIntent();
        Intent i = getIntent();
        str1 = i.getStringExtra("str11").toString();

        btnsend = (Button) findViewById(R.id.btnsenddata);
        btnmain = (Button) findViewById(R.id.btnmainmenu);

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

        btnsend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                byte bytes[] = (upstr).getBytes();
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
        });

        btnmain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent imain = new Intent(NextActivityBluetooth.this,MonitorActivityBluetooth.class);
                startActivity(imain);

            }
        });

    }


//	//////////////////////////////
//	public void onButtonClickclear(View view) throws IOException {
//		hexString = new StringBuffer();
//
//	}

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
