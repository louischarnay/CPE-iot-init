package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private List<Map.Entry<String, String>> items;
    private MyAdapter adapter;
    private BlockingQueue<String> queue;

    private String IP="192.168.43.157"; // Remplacer par l'IP de votre interlocuteur
    private final int PORT=10000; // Constante arbitraire du sujet
    private InetAddress address; // Structure Java décrivant une adresse résolue
    private DatagramSocket UDPSocket; // Structure Java permettant d'accéder au réseau (UDP)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> yourMap = new HashMap<>();
        yourMap.put("Température", "");
        yourMap.put("Luminosité", "");

        items = new ArrayList<>(yourMap.entrySet());

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(items);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        findViewById(R.id.button).setOnClickListener(this);

        try {
            UDPSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        TextInputEditText textInputEditText = findViewById(R.id.textInputEditText3);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        address = InetAddress.getByName(IP);
                        Log.d("UDP", "run: " + IP);
                        String message = "getValues()";
                        Log.d("UDP", "run: " + message);
                        byte[] data = message.getBytes();
                        DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                        UDPSocket.send(packet);
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("UDP", "run: " + e.getMessage());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] data = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        UDPSocket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        Log.d("UDP", "message: " + message);
                        JSONObject jsonObject = new JSONObject(message);
                        Log.d("UDP", "run: " + jsonObject.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        JSONObject temp = jsonArray.getJSONObject(0);
                        JSONObject light = jsonArray.getJSONObject(1);
                        String temperture = temp.getString("value");
                        String luminosity = light.getString("value");
                        Log.d("UDP", "temp: " + temperture);
                        Log.d("UDP", "lum: " + luminosity);
                        if (items.get(0).getKey() == "Température") {
                            items.get(0).setValue("Temptérature : " + temperture);
                            items.get(1).setValue("Luminosité : " + luminosity);
                        } else {
                            items.get(0).setValue("Luminosité : " + luminosity);
                            items.get(1).setValue("Temptérature : " + temperture);
                        }
                        TextView textView = findViewById(R.id.textView7);
                        textView.setText(items.get(0).getValue());
                        TextView textView2 = findViewById(R.id.textView8);
                        textView2.setText(items.get(1).getValue());
                        items.get(0).setValue(temperture);
                        items.get(1).setValue(luminosity);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (IOException e) {
                        Log.e("UDP", "run: " + e.getMessage());
                    } catch (JSONException e) {
                        Log.e("UDP", "run: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        TextInputEditText textInputEditText = findViewById(R.id.textInputEditText3);
        if (textInputEditText.getText() != null) {
            IP = textInputEditText.getText().toString();
            if (v.getId() == R.id.button) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            address = InetAddress.getByName(IP);
                            Log.d("UDP", "run: " + IP);
                            Character c1 = items.get(0).getKey().charAt(0);
                            Character c2 = items.get(1).getKey().charAt(0);
                            String message = c1 + "" + c2;
                            Log.d("UDP", "run: " + message);
                            byte[] data = message.getBytes();
                            DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                            UDPSocket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("UDP", "run: " + e.getMessage());
                        }
                    }
                }).start();
            }
        }
    }

    public class DragItemTouchHelperCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // Handle swipe actions if needed
        }
    }
}