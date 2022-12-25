package com.example.screengrabber;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.screengrabber.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.media.projection.MediaProjectionManager;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final int REQUEST_MEDIA_PROJECTION = 1;
    private final int REQUEST_ACCESSIBILITY_SERVICE = 2;

    private DisplayMetrics _displayMetrics = new DisplayMetrics();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, ScreenerService.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button btnOk = (Button) findViewById(R.id.btnStart);
        Button btnStop = (Button) findViewById(R.id.btnStop);

        // создаем обработчик нажатия
        OnClickListener oclBtnOk = new OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService
                        (Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }
        };

        OnClickListener oclBtnStop = new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
            }
        };

        btnOk.setOnClickListener(oclBtnOk);
        btnStop.setOnClickListener(oclBtnStop);

        this.getWindowManager().getDefaultDisplay().getRealMetrics(_displayMetrics);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK){
            Intent intent = new Intent(this, ScreenerService.class)
                    .putExtra("MEDIA_PROJECTION_DATA", data)
                    .putExtra("IP", ((EditText)findViewById(R.id.editText)).toString())
                    .putExtra("WIDTH_PIXELS", _displayMetrics.widthPixels)
                    .putExtra("HEIGHT_PIXELS", _displayMetrics.heightPixels)
                    .putExtra("DENSITY_DPI", _displayMetrics.densityDpi);

            startForegroundService(intent);
        }

        //TODO: включить службу специальных возможностей, для кликов
        //startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_ACCESSIBILITY_SERVICE);
    }
}