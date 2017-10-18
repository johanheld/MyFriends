package com.example.johan.myfriends;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by johan on 2017-10-10.
 */

public class TCPConnection extends Service
{
//    private final IBinder mBinder = new LocalBinder();
    public static final String IP="195.178.227.53";//,PORT="7117";
    public static final int PORT = 7117;
    private Socket socket;
    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;
    private InetAddress address;

    private RunOnThread thread;

    private Receive receive;
    private Buffer receiveBuffer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        thread = new RunOnThread();
        receiveBuffer = new Buffer<String>();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new LocalBinder();

    }

    /** The client receives the Binder and can use it to directly access
     *  public methods available in either the Binder implementation or the Service
     */

    public class LocalBinder extends Binder
    {
        TCPConnection getService() {
            return TCPConnection.this; //Returnerar current Service instant
        }
    }

    public void connect()
    {
        thread.start();
        thread.execute(new Connect());
    }

    public String receive() throws InterruptedException {
        return (String) receiveBuffer.get();
    }

    public void send(String json)
    {
        thread.execute(new Send(json));
    }

    private class Connect implements Runnable
    {
        public void run()
        {
            try
            {
                address = InetAddress.getByName(IP);
                socket = new Socket(address, PORT);
                inputStream = socket.getInputStream();
                dataInputStream = new DataInputStream(inputStream);
                outputStream = socket.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);

                receiveBuffer.put("CONNECTED");
                receive = new Receive();
                receive.start();

                //TODO Stänga stream när appen avslutas
            }catch (Exception e)
            {

            }
        }
    }

    private class Send implements Runnable
    {
        private String json;

        public Send(String json){
            this.json = json;
        }

        @Override
        public void run()
        {
            try
            {
                dataOutputStream.writeUTF(json);
                dataOutputStream.flush();

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class Receive extends Thread {
        public void run() {
            String result;
            try {
                while (receive != null) {
                    result = dataInputStream.readUTF();
                    receiveBuffer.put(result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
            }
        }
    }
}
