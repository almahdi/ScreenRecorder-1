package com.joshuaglenlee.screenrecorder;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 1;

    private MediaProjectionManager mediaProjectionManager;
    private ScreenRecorder screenRecorder;

    private int screenWidth;
    private int screenHeight;
    private int bitrate;

    private String mName = "ScreenRecorder_";
    private String mFilePath = Environment.getExternalStorageDirectory()+"/ScreenRecorder/";
    private final DateFormat mDate = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");

    private Context context = this;
    private CharSequence start = "Recording... Tap again to STOP!";
    private CharSequence stop = "Recording saved...";
    private int duration = Toast.LENGTH_SHORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        if (toolbar != null) {
            setActionBar(toolbar);
        }

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection != null) {
	    File directory = new File(mFilePath);
	    directory.mkdirs();
	    String mFileName = mFilePath + mName + mDate.format(new Date()) + ".mp4";
            File file = new File(mFileName);
            bitrate = 6000000;
            screenRecorder = new ScreenRecorder(screenWidth, screenHeight, bitrate, 1, 
						    mediaProjection, file.getAbsolutePath());
            screenRecorder.start();
            moveTaskToBack(true);
	    Toast toast = Toast.makeText(context, start, duration);
	    toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recorder:
                if (screenRecorder != null) {
                    screenRecorder.quit();
                    screenRecorder = null;
		    Toast toast = Toast.makeText(context, stop, duration);
		    toast.show();
                } else {
                    Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, REQUEST_CODE);
		    Toast toast = Toast.makeText(context, start, duration);
		    toast.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (screenRecorder != null) {
            screenRecorder.quit();
            screenRecorder = null;
        }
    }

    public static class MainFragment extends Fragment {
	public MainFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
				 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

	    return rootView;
	}
    }
}
