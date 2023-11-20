package com.example.myapplication;

import static android.os.SystemClock.sleep;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MyThread {
    private String IP;
    private int PORT;
    private volatile boolean isError = false;
    private InetAddress address; // Structure Java décrivant une adresse résolue
    private DatagramSocket UDPSocket; // Structure Java permettant d'accéder au réseau (UDP)

    public MyThread(String IP, int PORT) {
        this.IP = IP;
        this.PORT = PORT;
        try {
            UDPSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    public void setIP(String IP) {
        this.IP = IP;
    }
    public void setPORT(int PORT) {
        this.PORT = PORT;
    }
    public void setError(boolean error) {
        isError = error;
    }
    public void sendGetValues() {
        //send getValues() to microbit
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        address = InetAddress.getByName(IP);
                        Log.d("UDP", "sendGetValues: " + IP);
                        String message = "getValues()";
                        Log.d("UDP", "sendGetValues: " + message);
                        byte[] data = message.getBytes();
                        DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                        UDPSocket.send(packet);
                        sleep(1000);
                    } catch (IOException e) {
                        Log.e("UDP", "sendGetValues: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    public void send(String message) {
        //send message to microbit
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("UDP", "send: " + IP);
                    Log.d("UDP", "send: " + message);
                    address = InetAddress.getByName(IP);
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                    UDPSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("UDP", "send: " + e.getMessage());
                }
            }
        }).start();
    }

    public JSONObject receive() {

        final JSONObject[] jsonObject = {new JSONObject()};
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] data = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        UDPSocket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        Log.d("UDP", "receive: " + message);
                        jsonObject[0] = new JSONObject(message);
                        Log.d("UDP", "receive: " + jsonObject[0].toString());
                    } catch (IOException e) {
                        Log.e("UDP", "receive: " + e.getMessage());
                    } catch (JSONException e) {
                        Log.e("UDP", "receive: " + e.getMessage());
                    }
                }
            }
        }).start();
        return jsonObject[0];
    }
}