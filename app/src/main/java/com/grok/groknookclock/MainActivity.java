package com.grok.groknookclock;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout tl = (TableLayout)findViewById(R.id.time_table);
        LayoutInflater inflater = getLayoutInflater();

        for(int i = 0; i < 9; i++) {
            TableRow row = (TableRow)inflater.inflate(R.layout.time_table_row,
                    tl, false);

            TextView entry_num = (TextView)row.findViewById(R.id.entry_number);
            TextView entry_time = (TextView)row.findViewById(R.id.entry_time);

            if (i == 0) {
                // title row
                row.setBackgroundColor(Color.GRAY);
                entry_num.setText("ID");
                entry_time.setText("Time");
            } else {
                entry_num.setText("" + i);
                entry_time.setText("00:00:0" + i); // set the text for the header
            }
            // other customizations to the row

            tl.addView(row);
        }

    }
}
