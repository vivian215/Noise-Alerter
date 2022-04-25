package com.example.noisealerter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private MainView mainView;
    public int SCREENHEIGHT, SCREENWIDTH;
    private boolean permissionToRecord = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    public String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActivityCompat.requestPermissions(this, permissions, 200);

        //get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        SCREENHEIGHT = displayMetrics.heightPixels;
        SCREENWIDTH = displayMetrics.widthPixels;

        filePath = getExternalCacheDir().getAbsolutePath();

        //set view to mainView
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mainView = new MainView(this, getResources());
        setContentView(mainView);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecord) {
            finish();
        }

    }

    @Override
    protected void onStart() {
        Log.d("stuck", "onstart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}