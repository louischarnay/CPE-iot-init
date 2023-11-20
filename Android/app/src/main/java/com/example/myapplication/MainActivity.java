package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private List<Map.Entry<String, String>> items;
    private MyAdapter adapter;
    private String IP="192.168.43.157"; // Remplacer par l'IP de votre interlocuteur
    private final int PORT=10000; // Constante arbitraire du sujet
    private MyThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize map data
        Map<String, String> yourMap = new HashMap<>();
        yourMap.put("Température", "");
        yourMap.put("Luminosité", "");

        items = new ArrayList<>(yourMap.entrySet());

        //Initialize recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Initialize adapter
        adapter = new MyAdapter(items);
        recyclerView.setAdapter(adapter);

        //Initialize touch helper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //listeners
        findViewById(R.id.button).setOnClickListener(this);

        thread = new MyThread(IP, PORT);
        thread.sendGetValues();
        receive();
    }

    public void receive(){
        String temperture = null, luminosity = null;

        while(true){
            //Get and parse JSON from microbit
            try{
                JSONObject jsonObject = thread.receive();
                if (jsonObject == null || !jsonObject.has("data")) {
                    thread.setError(true);
                    Log.e("Application", "No data received. Stopping the loop.");
                    break; // Break out of the loop if no data is received
                }
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                JSONObject temp = jsonArray.getJSONObject(0);
                JSONObject light = jsonArray.getJSONObject(1);
                temperture = temp.getString("value");
                luminosity = light.getString("value");
            } catch (JSONException e) {
                Log.e("Application", String.valueOf(e));
            }
            Log.d("Application", "temp: " + temperture);
            Log.d("Application", "lum: " + luminosity);
            if (items.get(0).getKey() == "Température") {
                items.get(0).setValue("Temptérature : " + temperture);
                items.get(1).setValue("Luminosité : " + luminosity);
            } else {
                items.get(0).setValue("Luminosité : " + luminosity);
                items.get(1).setValue("Temptérature : " + temperture);
            }
            //Update layout
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
        }
    }

    @Override
    public void onClick(View v) {
        //send LT or TL to microbit
        TextInputEditText textInputEditText = findViewById(R.id.textInputEditText3);
        if (textInputEditText.getText() != null) {
            IP = textInputEditText.getText().toString();
            thread.setIP(IP);
            Character c1 = items.get(0).getKey().charAt(0);
            Character c2 = items.get(1).getKey().charAt(0);
            String message = c1 + "" + c2;
            if (v.getId() == R.id.button) {
                thread.send(message);
            }
            thread.setError(false);
            receive();
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
        }
    }
}