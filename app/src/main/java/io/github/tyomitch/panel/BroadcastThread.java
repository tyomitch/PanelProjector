package io.github.tyomitch.panel;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;

public class BroadcastThread extends Thread {
    private static final String TAG = "BroadcastThread";
    private static final byte[] id = new byte[] {0, 1, 1, 1};
    private static final int PORT = 10220;

    private final DatagramSocket send, receive;
    private final InetAddress self, broadcast;
    private final ScreenCaptureActivity host;

    public BroadcastThread(InterfaceAddress iface, ScreenCaptureActivity host) throws SocketException {
        self = iface.getAddress();
        broadcast = iface.getBroadcast();
        send = new DatagramSocket(null);
        send.setBroadcast(true);
        send.bind(new InetSocketAddress(self, PORT));
        receive = new DatagramSocket(null);
        //receive.setReuseAddress(true);
        receive.bind(new InetSocketAddress(broadcast, PORT)); // the panel broadcasts back
        this.host = host;
    }

    @Override
    public void run() {
        new Listener().start();
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(id, id.length, broadcast, PORT);
                send.send(packet);
                Thread.sleep(1000);
            }
        }
        catch(IOException e) {
            Log.e(TAG, e.toString());
        }
        catch(InterruptedException e) {
            // Done, nothing to do
        }
    }

    private class Listener extends Thread {
        @Override
        public void run() {
            byte[] buf = new byte[9];
            try {
                while (true) {
                    Thread.sleep(1000);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    receive.receive(packet);
                    InetAddress from = packet.getAddress();
                    if (from.equals(self)) continue;
                    if (packet.getLength() != buf.length) continue;
                    if (buf[0]!=0 || buf[1]!=2 || buf[2]!=6 || buf[7]!=0 || buf[8]!=1) continue;
                    byte[] addr = from.getAddress();
                    if (buf[3]!=addr[0] || buf[4]!=addr[1] || buf[5]!=addr[2] || buf[6]!=addr[3]) continue;
                    host.connect(from);
                    BroadcastThread.this.interrupt();
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            } catch (InterruptedException e) {
                // Done, nothing to do
            }
        }
    }
}
