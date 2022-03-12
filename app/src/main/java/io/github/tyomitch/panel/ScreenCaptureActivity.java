package io.github.tyomitch.panel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


public class ScreenCaptureActivity extends Activity {

    private static final String TAG = "ScreenCaptureActivity";
    private static final int REQUEST_CODE = 100;
    private static final int PORT = 32550;

    private Socket panel;

    /****************************************** Activity Lifecycle methods ************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start projection
        Button startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);
        startButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startProjection();
            }
        });

        // stop projection
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                stopProjection();
            }
        });

        // broadcast & connect
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (!ni.isLoopback()) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        if (ia.getNetworkPrefixLength() <= 32 && ia.getBroadcast() != null) {
                            new BroadcastThread(ia, this).start();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void connect(InetAddress panel) {
        try {
            this.panel = new Socket(panel, PORT);
            runOnUiThread(new EnableStart());
        }
        catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    private class EnableStart implements Runnable {
        @Override
        public void run() {
            findViewById(R.id.startButton).setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data.putExtra("PANEL", panel.getRemoteSocketAddress()); // cannot pass socket object :(
                try { panel.close(); } catch (IOException e) { }
                startService(ScreenCaptureService.getStartIntent(this, resultCode, data));
                findViewById(R.id.stopButton).setEnabled(true);
            }
        }
    }

    /****************************************** UI Widget Callbacks *******************************/
    private void startProjection() {
        findViewById(R.id.startButton).setEnabled(false);
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        findViewById(R.id.stopButton).setEnabled(false);
        startService(ScreenCaptureService.getStopIntent(this));
        findViewById(R.id.startButton).setEnabled(true);
    }
}