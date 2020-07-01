package com.felhr.serialportexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SeekBar coklat = (SeekBar)findViewById(R.id.coklat);
        SeekBar merah = (SeekBar)findViewById(R.id.merah);
        SeekBar kuning = (SeekBar)findViewById(R.id.kuning);
        SeekBar hijau = (SeekBar)findViewById(R.id.hijau);
        SeekBar biru = (SeekBar)findViewById(R.id.biru);

        TextView brown = (TextView)findViewById(R.id.view1);
        TextView red = (TextView)findViewById(R.id.view2);
        TextView yellow = (TextView)findViewById(R.id.view3);
        TextView green = (TextView)findViewById(R.id.view4);
        TextView blue = (TextView)findViewById(R.id.view5);

        Button simpan = (Button)findViewById(R.id.simpan);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        int data1 = pref.getInt("data1", -1);
        int data2 = pref.getInt("data2", -1);
        int data3 = pref.getInt("data3", -1);
        int data4 = pref.getInt("data4", -1);
        int data5 = pref.getInt("data5", -1);

        coklat.setProgress(data1);
        merah.setProgress(data2);
        kuning.setProgress(data3);
        hijau.setProgress(data4);
        biru.setProgress(data5);

        coklat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brown.setText("Coklat : " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        merah.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                red.setText("Merah : " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        kuning.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                yellow.setText("kuning : " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hijau.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                green.setText("Hijau : " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        biru.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blue.setText("Biru : " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putInt("data1", coklat.getProgress());
                editor.putInt("data2", merah.getProgress());
                editor.putInt("data3", kuning.getProgress());
                editor.putInt("data4", hijau.getProgress());
                editor.putInt("data5", biru.getProgress());
                editor.apply();
                Intent home = new Intent(Main2Activity.this,MainActivity.class);
                startActivity(home);
            }
        });


    }
}
