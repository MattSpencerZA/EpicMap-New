package com.example.EpicMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class RouteActivity extends AppCompatActivity {


    DBHelper db;
    private FirebaseAuth mAuth;
    private Context context;
    private Geocoder geocoder;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        context = this;
        mAuth = FirebaseAuth.getInstance();
        db = new DBHelper(context);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(RouteActivity.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        });

        FirebaseUser user = mAuth.getCurrentUser();
        String uuid = user.getUid();

        Cursor cursor = db.getTrips(uuid);

        if (cursor.getCount() != 0) {

            ArrayList<String> datetime = new ArrayList<>();
            ArrayList<String> transport = new ArrayList<>();
            ArrayList<String> startAddress = new ArrayList<>();
            ArrayList<String> endAddress = new ArrayList<>();

            while(cursor.moveToNext()) {

                datetime.add(cursor.getString(1));

                transport.add(cursor.getString(2));

                String[] separated1 = cursor.getString(3).split(" ");
                double latitudeE61 = Double.parseDouble(separated1[0]);
                double longitudeE61 = Double.parseDouble(separated1[1]);
                GeoPoint gp1 = new GeoPoint(latitudeE61, longitudeE61);

                String[] separated2 = cursor.getString(4).split(" ");
                double latitudeE62 = Double.parseDouble(separated2[0]);
                double longitudeE62 = Double.parseDouble(separated2[1]);
                GeoPoint gp2 = new GeoPoint(latitudeE62, longitudeE62);

                String originAddress = "Address cannot be found!";
                String destinationAddress = "Address cannot be found!";

                geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    originAddress = geocoder.getFromLocation(
                            gp1.getLatitude(), gp1.getLongitude(), 1)
                            .get(0).getAddressLine(0);
                    destinationAddress = geocoder.getFromLocation(
                            gp2.getLatitude(), gp2.getLongitude(), 1)
                            .get(0).getAddressLine(0);

                    startAddress.add(originAddress);
                    endAddress.add(destinationAddress);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            recycleView(datetime, transport, startAddress, endAddress);

        }
        else {

            Toast.makeText(this, "Cannot add items to the recycler view", Toast.LENGTH_SHORT).show();

        }

    }

    public void recycleView(ArrayList<String> ids, ArrayList<String> transportMethods, ArrayList<String> originAddresses, ArrayList<String> destinationAddresses) {

        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.recyclerView);

        RouteAdapter routeAdapter = new RouteAdapter(context, ids, transportMethods, originAddresses, destinationAddresses);

        recyclerView.setAdapter(routeAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

}