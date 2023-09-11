package com.shihab365.getcurrentlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView tvCountry, tvState, tvCity, tvPostal, tvAddress;
    Button btnInsert;
    LocationManager locationManager;
    String strCountry, strState, strCity, strPostal, strAddress;
    FirebaseFirestore dbroot;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbroot = FirebaseFirestore.getInstance();

        getPermission();
        isGPSEnable();
        getLocation();
        dataInsert();

        tvCountry = findViewById(R.id.txt_Country);
        tvState = findViewById(R.id.txt_State);
        tvCity = findViewById(R.id.txt_City);
        tvPostal = findViewById(R.id.txt_Postal);
        tvAddress = findViewById(R.id.txt_Address);
        btnInsert = findViewById(R.id.btnINSERT);
        
    }


    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, (LocationListener) this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void isGPSEnable() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean GPSEnable = false;
        boolean NetworkEnable = false;

        try {
            GPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            NetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!GPSEnable && !NetworkEnable) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Enale GPS Service")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivities(new Intent[]{new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)});
                        }
                    }).setNegativeButton("Cancel", null)
                    .show();
        }

    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            strCountry = addressList.get(0).getCountryName();
            strState = addressList.get(0).getAdminArea();
            strCity = addressList.get(0).getLocality();
            strPostal = addressList.get(0).getPostalCode();
            strAddress = addressList.get(0).getAddressLine(0);

            dataInsert();

            tvCountry.setText(strCountry);
            tvState.setText(strState);
            tvCity.setText(strCity);
            tvPostal.setText(strPostal);
            tvAddress.setText(strAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    private void dataInsert() {
        Map<String, String> loc = new HashMap<>();
        loc.put("Country", strCountry);
        loc.put("State", strState);
        loc.put("City", strCity);
        loc.put("Postal", strPostal);
        loc.put("Address", strAddress);

        dbroot.collection("locationData").add(loc).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Toast.makeText(MainActivity.this, "INSERTED", Toast.LENGTH_SHORT).show();
            }
        });
    }
}