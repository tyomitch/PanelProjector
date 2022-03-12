package io.github.tyomitch.panel;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class ProjectionThread extends Thread {
    private static final String TAG = "ProjectionThread";
    private static final int PACKAGE = 40960;
    private final InetSocketAddress panel;
    private byte[] data;

    public ProjectionThread(InetSocketAddress panel) {
        this.panel = panel;
        start();
    }

    synchronized public void setData(byte[] data) {
        this.data = data;
    }

    synchronized private byte[] takeData() {
        byte[] copy = data;
        data = null;
        return copy;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(panel.getAddress(), panel.getPort());
            OutputStream stream = socket.getOutputStream();
            while(true) {
                byte[] data = takeData();
                if (data!=null) {
                    int len = data.length;
                    stream.write(new byte[]{0, 4, (byte) ((len >> 24) & 255), (byte) ((len >> 16) & 255), (byte) ((len >> 8) & 255), (byte) (len & 255)});

                    int chunks = len / PACKAGE;
                    for (int i = 0; i < chunks; i++) {
                        stream.write(new byte[]{0, 5, (byte) ((PACKAGE >> 24) & 255), (byte) ((PACKAGE >> 16) & 255), (byte) ((PACKAGE >> 8) & 255), (byte) (PACKAGE & 255)});
                        stream.write(Arrays.copyOfRange(data, i * PACKAGE, (i + 1) * PACKAGE));
                    }

                    int rest = len % PACKAGE;
                    if (rest > 0) {
                        stream.write(new byte[]{0, 5, (byte) ((rest >> 24) & 255), (byte) ((rest >> 16) & 255), (byte) ((rest >> 8) & 255), (byte) (rest & 255)});
                        stream.write(Arrays.copyOfRange(data, chunks * PACKAGE, len));
                    }

                    stream.write(new byte[]{0, 6, 65, 117, 116, 111, 73, 79, 67, 111}); // AutoIOCo
                }
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    break;
                }
            }
            stream.close();
            socket.close();
        }
        catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
}
