package com.example.marie.sosappyoustiti;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapActivity extends AppCompatActivity implements LocationListener {
    private static final int LOCATION_PERMISSION_CODE = 1;
    private static final int DOCUMENT_PERMISSION_CODE = 2;
    private static final int TEL_PERMISSION_CODE = 3;

    private boolean locPermGranted, docPermGranted, telPermGranted = false;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;
    private FusedLocationProviderClient fusedLocClient;
    private Location currentLocation;
    private LocationManager locManager;
    private MapController sosMapCtrl;

    private Button locBtn;
    private MapView sosMap;
    private TextView latitude;
    private TextView longitude;
    private Button safeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locBtn      = (Button) findViewById(R.id.centerLocBtn);
        sosMap      = (MapView) findViewById(R.id.map);
        safeBtn     = (Button) findViewById(R.id.safeBtn);
        latitude    = (TextView) findViewById(R.id.latitudeTV);
        longitude   = (TextView) findViewById(R.id.longitudeTV);

        final Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Appui sur bouton de re-centrage du marqueur
        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sosMapCtrl.animateTo(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        });

        // Appui sur bouton d'appel de secours
        safeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        getLocationPermission();

//        String locationProvider = LocationManager.NETWORK_PROVIDER;

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            currentLocation = locManager.getLastKnownLocation(locationProvider);
//        }

    }

    /*
    * Initialiser OpenStreetMap
    */
    public void initMap() {
        sosMap.setTileSource(TileSourceFactory.MAPNIK);

        sosMapCtrl = (MapController) sosMap.getController();
        sosMapCtrl.setZoom(18);

        try {
            if (locPermGranted) {
                if(docPermGranted){
                    getDeviceLocation();
                }
                else {
                    Toast.makeText(getApplicationContext(),"pas de perm document", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(),"pas de perm localisation", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Log.e("security", "getDeviceLocation: security " + e.getMessage());
        }
    }

    /*
    * Récupérer la position de l'appareil
    */
    private void getDeviceLocation() {
        //Version Google
//        fusedLocClient = LocationServices.getFusedLocationProviderClient(this);
//        try {
//            if (locPermGranted) {
////                Task location = fusedLocClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            currentLocation = (Location) task.getResult();
//                            latitude.setText(""+currentLocation.getLatitude());
//                            longitude.setText(""+currentLocation.getLongitude());
//                            locBtn.setEnabled(true);
//                            addMarker(new GeoPoint(currentLocation.getLatitude(),currentLocation.getLongitude()));
//                        } else {
//                            locBtn.setEnabled(false);
//                            Toast.makeText(MapActivity.this, "Impossible d'obtenir la position de l'appareil", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.e("security", "getDeviceLocation: security " + e.getMessage());
//        }

        try {
            if (locPermGranted) {
                locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                gpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                networkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(gpsEnabled) {
                    locManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            60000,
                            5, this);
                    if(locManager != null) {
                        currentLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(currentLocation != null){
                            latitude.setText(""+currentLocation.getLatitude());
                            longitude.setText(""+currentLocation.getLongitude());
                            locBtn.setEnabled(true);
                            addMarker(new GeoPoint(currentLocation.getLatitude(),currentLocation.getLongitude()));
                        }
                    }
                    Toast.makeText(getApplicationContext(), "loc gps", Toast.LENGTH_LONG).show();
                }else{
                    if (networkEnabled){
                        locManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                60000,
                                5, this);
                        if(locManager != null) {
                            currentLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if(currentLocation != null) {
                                latitude.setText("" + currentLocation.getLatitude());
                                longitude.setText("" + currentLocation.getLongitude());
                                locBtn.setEnabled(true);
                                addMarker(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                            }
                        }
                        Toast.makeText(getApplicationContext(), "loc network", Toast.LENGTH_LONG).show();
                    }
                    else {
                        locBtn.setEnabled(false);
                        Toast.makeText(getApplicationContext(),"pas de connexion", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        } catch (SecurityException e) {
            Log.e("security", "getDeviceLocation: security " + e.getMessage());
        }


    }

    /*
    * Placer le marqueur de la position actuelle, au centre
    */
    public void addMarker(GeoPoint center) {
        Marker marker = new Marker(sosMap);
        marker.setPosition(center);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        sosMap.getOverlays().clear();
        sosMap.getOverlays().add(marker);
        sosMap.invalidate();
        sosMapCtrl.animateTo(center);
    }

    /*
     * Demander permissions de géolocalisation
     */
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locPermGranted = true;
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    docPermGranted = true;
                    initMap();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOCUMENT_PERMISSION_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        locPermGranted = true;
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        initMap();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOCUMENT_PERMISSION_CODE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"La localisation est nécessaire.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case DOCUMENT_PERMISSION_CODE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    docPermGranted = true;
                    initMap();
                } else {
                    Toast.makeText(getApplicationContext(),"L'accès aux documents est nécessaire.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case TEL_PERMISSION_CODE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    telPermGranted = true;
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+33602367992"));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),"L'accès aux appels est nécessaire.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    /*
     * Boîte de dialogue pour appeler le numéro de secours
     */
    public void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

        builder.setTitle("Contacter secours");
        builder.setMessage("Voulez-vous contacter le numéro de secours ?");

        builder.setPositiveButton("Oui",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+33644086941"));
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.CALL_PHONE},TEL_PERMISSION_CODE);
                }
                else
                {
                    startActivity(intent);
                }
            }
        })
                .setNegativeButton("Annuler",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(),"loc changed", Toast.LENGTH_SHORT).show();
        getDeviceLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        String newStatus = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }
        Toast.makeText(this, provider + " : " + newStatus, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getApplicationContext(), provider + " est activé.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), provider + " est désactivé.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(locManager != null){
            locManager.removeUpdates(this);
        }
    }

}
