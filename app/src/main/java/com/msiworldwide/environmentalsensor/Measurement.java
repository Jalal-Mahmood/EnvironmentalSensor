package com.msiworldwide.environmentalsensor;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.msiworldwide.environmentalsensor.Data.CurrentSelections;
import com.msiworldwide.environmentalsensor.Data.DatabaseHelper;
import com.msiworldwide.environmentalsensor.Data.FieldData;
import com.msiworldwide.environmentalsensor.Data.MeasurementIdentifiers;
import com.msiworldwide.environmentalsensor.Data.SensorData;
import com.msiworldwide.environmentalsensor.ble.BleManager;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class Measurement extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        BleManager.BleManagerListener {

    private GoogleMap mMap;

    private static final long INTERVAL = 1000*5;
    private static final long FASTEST_INTERVAL = 1000;
    Button BtnMeasurement;
    Button BtnVisualize;
    //TextView tvLocation;
    TextView received_data;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    SensorData MeasuredData = new SensorData();
    MeasurementIdentifiers measurementIdentifiers = new MeasurementIdentifiers();

    CurrentSelections currentSelections = new CurrentSelections();
    long Field_id;
    FieldData fieldData = new FieldData();
    String field_coords_str;
    String[] field_coords_array;
    ArrayList<LatLng> boundary = new ArrayList<>();
    ArrayList<Double> lats = new ArrayList<>();
    ArrayList<Double> lngs = new ArrayList<>();
    LatLng mNortheast;
    LatLng mSouthwest;

    int measurement_id;

    DatabaseHelper db;

    // Service Constants
    private final static String TAG = Measurement.class.getSimpleName();
    public static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final int kTxMaxCharacters = 20;
    private boolean isRxNotificationEnabled = false;


    // Data
    protected BleManager mBleManager;
    protected BluetoothGattService mUartService;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Measurement");
        mToolbar.setTitleTextColor(Color.WHITE);

        db = new DatabaseHelper(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager() .findFragmentById(R.id.map);
        if (mapFragment!=null){
            mapFragment.getMapAsync(this);
        }

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);
       // mUartService = mBleManager.getGattService(UUID_SERVICE);
        // Continue
        mUartService = mBleManager.getGattService(UUID_SERVICE);
        enableRxNotifications();
        //tvLocation = (TextView) findViewById(R.id.tvLocation);
        received_data = (TextView) findViewById(R.id.received_data);
        BtnVisualize = (Button) findViewById(R.id.visualize);
        BtnMeasurement = (Button) findViewById(R.id.take_measurement);
        BtnMeasurement.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (mCurrentLocation == null) {
                    AlertDialog.Builder location = new AlertDialog.Builder(Measurement.this);
                    location.setTitle("No Location Data")
                            .setMessage("Please Check if GPS is enabled")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = location.create();
                    alert.show();
                } else {
                    double lat = mCurrentLocation.getLatitude();
                    double lng = mCurrentLocation.getLongitude();
                    String time = DateFormat.getTimeInstance().format(new Date());
                    String date = DateFormat.getDateInstance().format(new Date());
                    measurementIdentifiers.setDate(date);
                    MeasuredData.setlat(lat);
                    MeasuredData.setlng(lng);
                    MeasuredData.setTime(time);
                    MeasuredData.setDate(date);
                    String text = "M";
                    sendData(text);
                }
            }
        });

        currentSelections = db.getCurrentSelections();
        Field_id = currentSelections.getField_id();
        fieldData = db.getFieldData(Field_id);
        measurementIdentifiers.setFieldId(fieldData.getFieldId());
        MeasuredData.setFieldId(fieldData.getFieldId());
        field_coords_str = fieldData.getCoordinates();
        field_coords_array = field_coords_str.split(",");
        for (int i=0;i<field_coords_array.length/2;i++) {
            double lat = Double.valueOf(field_coords_array[2*i]);
            lats.add(lat);
            double lng = Double.valueOf(field_coords_array[2*i+1]);
            lngs.add(lng);
            LatLng point = new LatLng(lat,lng);
            boundary.add(point);
        }

        measurement_id = db.getNewIdentifier();
        measurementIdentifiers.setMeasurementNumberId(measurement_id);
        MeasuredData.setMeasurementNumberId(measurement_id);
        received_data.setText(String.valueOf(measurement_id));


    }

    public void openVisualizeResults(View view){
        Intent intent = new Intent(this, VisualizeResults.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.deleteCurrentSelections();
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else{
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        mMap.setMyLocationEnabled(true);
        if (!boundary.isEmpty()) {
            PolygonOptions opts = new PolygonOptions();
            for (LatLng location : boundary) {
                opts.add(location);
            }
            Polygon polygon = mMap.addPolygon(opts.strokeColor(Color.RED));
            Collections.sort(lats);
            Collections.sort(lngs);
            mNortheast = new LatLng(lats.get(lats.size() - 1), lngs.get(lngs.size() - 1));
            mSouthwest = new LatLng(lats.get(0), lngs.get(0));
            LatLngBounds field_bound = new LatLngBounds(mSouthwest,mNortheast);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(field_bound,0));
        }
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Add a marker in Blacksburg and move the camera
 /*       LatLng blacksburg = new LatLng(37.229572,  -80.413940);
        mMap.addMarker(new MarkerOptions().position(blacksburg).title("Marker in Blacksburg"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(blacksburg));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));*/
    }

    // region Send Data to UART
    protected void sendData(String text) {
        final byte[] value = text.getBytes(Charset.forName("UTF-8"));
        sendData(value);
    }

    protected void sendData(byte[] data) {
        if (mUartService != null) {
            // Split the value into chunks (UART service has a maximum number of characters that can be written )
            for (int i = 0; i < data.length; i += kTxMaxCharacters) {
                final byte[] chunk = Arrays.copyOfRange(data, i, Math.min(i + kTxMaxCharacters, data.length));
                mBleManager.writeService(mUartService, UUID_TX, chunk);
            }
            received_data.setText("Measuring...");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BluetoothGattCharacteristic characteristic = mUartService.getCharacteristic(UUID.fromString(UUID_RX));
                    byte[] dataread = characteristic.getValue();
                    String data = new String(dataread, Charset.forName("UTF-8"));
                    String[] values = data.split(",");
                    try {
                        int moisture = Integer.parseInt(values[0]);
                        int sunlight = Integer.parseInt(values[1]);
                        double temp = Double.parseDouble(values[2]);
                        double humid = Double.parseDouble(values[3]);
                        MeasuredData.setmoisture(moisture);
                        MeasuredData.setsunlight(sunlight);
                        MeasuredData.settemperature(temp);
                        MeasuredData.sethumidity(humid);
                        db.createMeasurementId(measurementIdentifiers);
                        db.createSensorData(MeasuredData);
                        Toast complete = Toast.makeText(getApplicationContext(), "Measurement Complete", Toast.LENGTH_SHORT);
                        complete.show();
                        received_data.setText("M:" + values[0] + "S:" + values[1] + "T:" + values[2] + "H:" + values[3]);
                        //received_data.setText("Measurement Complete");
                        LatLng loc = new LatLng(MeasuredData.getLat(), MeasuredData.getLng());
                        mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title("M:" + values[0] + ", S:" + values[1] + ", T:" + values[2] + ", H:" + values[3]));
                    } catch (IndexOutOfBoundsException|NumberFormatException ex) {
                        received_data.setText("No Data");
                        AlertDialog.Builder location = new AlertDialog.Builder(Measurement.this);
                        location.setTitle("Data Measurement Failed")
                                .setMessage("Please try measurement again")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        AlertDialog alert = location.create();
                        alert.show();
                    }
                }
            }, 5000);
        } else {
            Log.w(TAG, "Uart Service not discovered. Unable to send data");
            AlertDialog.Builder location = new AlertDialog.Builder(this);
            location.setTitle("No Bluetooth Connection")
                    .setMessage("Please connect to a sensor")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = location.create();
            alert.show();
        }
    }

// BleManager Listener
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onServicesDiscovered() {
        mUartService = mBleManager.getGattService(UUID_SERVICE);
        enableRxNotifications();
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        // UART RX
/*        received_data.setText("Received");
*//*        final byte[] bytes = characteristic.getValue();
        final String data = new String(bytes, Charset.forName("UTF-8"));
        received_data.setText(data);*//*
        // Check if there is a pending sendDataRunnable
        if (sendDataRunnable != null) {
            if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
                if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX)) {

                    Log.d(TAG, "sendData received data");
                    sendDataTimeoutHandler.removeCallbacks(sendDataRunnable);
                    sendDataRunnable = null;

                    if (sendDataCompletionHandler != null) {
                        final byte[] bytes = characteristic.getValue();
                        final String data = new String(bytes, Charset.forName("UTF-8"));
                        received_data.setText(data);

                        final SendDataCompletionHandler dataCompletionHandler =  sendDataCompletionHandler;
                        sendDataCompletionHandler = null;
                        dataCompletionHandler.sendDataResponse(data);
                    }
                }
            }
        }*/
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor){

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    protected void enableRxNotifications() {
        isRxNotificationEnabled = true;
        mBleManager.enableNotification(mUartService, UUID_RX, true);
    }
}
