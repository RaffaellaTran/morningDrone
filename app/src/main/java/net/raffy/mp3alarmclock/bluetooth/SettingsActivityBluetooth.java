package net.raffy.mp3alarmclock.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.raffy.mp3alarmclock.R;

/**
 * Created by raffy on 01/05/17.
 */

public class SettingsActivityBluetooth extends Activity {

        EditText ed1;
        Button btnSave;
        String str1;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bluetooth_activity_settings);

            ed1 = (EditText) findViewById(R.id.ed11);

            btnSave = (Button) findViewById(R.id.btnSave);

            btnSave.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    str1 = ed1.getEditableText().toString();

                    Intent i = new Intent(String.valueOf(NextActivityBluetooth.class));
                    i.putExtra("str11", str1);
                        startActivity(i);


                }
            });




        }

    }


