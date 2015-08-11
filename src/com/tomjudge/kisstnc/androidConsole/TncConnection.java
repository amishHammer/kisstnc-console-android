/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tomjudge.kisstnc.androidConsole;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import com.tomjudge.kisstnc.KissFrame;
import com.tomjudge.kisstnc.KissFrameListener;
import com.tomjudge.kisstnc.MobilinkdTnc;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author amishhammer
 */
public class TncConnection extends Thread {

    private static final String TAG = TncConnection.class.getName();

    private static final UUID SPP_UUID
            = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothDevice device;
    private BluetoothSocket btSocket;
    private MobilinkdTnc tnc;
    private InputStream iStream;
    private OutputStream oStream;
    private final Handler handler;
    
    
    TncConnection(BluetoothDevice device, Handler handler) {
        this.device = device;
        this.handler = handler;
    }
    
    @Override
    public void run() {

   
        Log.d(TAG, "connect to: " + device);

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            Log.e(TAG, "create() failed", e);
            return;
        }

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            btSocket.connect();
            iStream = btSocket.getInputStream();
            oStream = btSocket.getOutputStream();
        } catch (IOException e) {
            //connectionFailed();
            // Close the socket
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }

            return;
        }
        
        tnc = new MobilinkdTnc(iStream, oStream);
        tnc.registerFrameListener(new KissFrameListener() {

            @Override
            public void frameReceived(KissFrame frame) {

                try {
                    if (frame.getType() != KissFrame.FrameType.DATA) {
                        return;
                    }
                    
                    handler.obtainMessage(MainActivity.NEW_MESSAGE, frame.toString()).sendToTarget();
                    
                } catch (Exception e) {
                    Log.e(TAG, "handle frame", e);
                }
            }

        });
        tnc.start();

    }
    protected void sendMessage(String message) {
        Log.d(TAG, "Sending Message: "+message);
        tnc.sendFrame(new KissFrame(message.getBytes(), KissFrame.FrameType.DATA));
    }


}
