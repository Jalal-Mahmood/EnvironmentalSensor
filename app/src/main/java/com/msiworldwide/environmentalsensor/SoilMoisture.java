package com.msiworldwide.environmentalsensor;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.msiworldwide.environmentalsensor.Data.CurrentSelections;
import com.msiworldwide.environmentalsensor.Data.DatabaseHelper;
import com.msiworldwide.environmentalsensor.Data.FieldData;
import com.msiworldwide.environmentalsensor.Data.MeasurementIdentifiers;
import com.msiworldwide.environmentalsensor.Data.SensorData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoilMoisture extends AppCompatActivity implements OnMapReadyCallback,
        AdapterView.OnItemSelectedListener{

    private GoogleMap mMap;

    DatabaseHelper db;
    CurrentSelections currentSelections = new CurrentSelections();
    long Field_id;
    FieldData fieldData = new FieldData();
    List<MeasurementIdentifiers> measurementIdentifiers;
    List<SensorData> DataList;
    String field_coords_str;
    String[] field_coords_array;
    ArrayList<LatLng> boundary = new ArrayList<>();
    ArrayList<Double> lats = new ArrayList<>();
    ArrayList<Double> lngs = new ArrayList<>();
    LatLng mNortheast;
    LatLng mSouthwest;
    int measurement_id;

    private Spinner date_spinner;
    private ArrayList<String> dateString = new ArrayList<>(Arrays.asList("Select Date"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_moisture);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Soil Moisture");
        mToolbar.setTitleTextColor(Color.WHITE);

        db = new DatabaseHelper(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager() .findFragmentById(R.id.soil_map);
        if (mapFragment!=null){
            mapFragment.getMapAsync(this);
        }

        currentSelections = db.getCurrentSelections();
        Field_id = currentSelections.getField_id();
        fieldData = db.getFieldData(Field_id);
        measurementIdentifiers = db.getMeasurementIdbyField(fieldData.getFieldId());

        // Spinner Drop Down for Field Selection
        date_spinner = (Spinner)findViewById(R.id.Date_Select_Soil);

        for (int i = 0; i < measurementIdentifiers.size(); i++) {
            MeasurementIdentifiers m_id = measurementIdentifiers.get(i);
            dateString.add(m_id.getDate());
        }
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(SoilMoisture.this,
                android.R.layout.simple_spinner_item,dateString);

        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date_spinner.setAdapter(dateAdapter);
        date_spinner.setOnItemSelectedListener(this);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.result_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.moisture_id:
                //Intent mIntent = new Intent(this, SoilMoisture.class);
                //startActivity(mIntent);
                break;
            case R.id.nutrition_id:
                Intent nIntent = new Intent(this, SoilNutrition.class);
                startActivity(nIntent);
                this.finish();
                break;
            case R.id.temperature_id:
                Intent tIntent = new Intent(this, Temperature.class);
                startActivity(tIntent);
                this.finish();
                break;
            case R.id.humidity_id:
                Intent hIntent = new Intent(this, Humidity.class);
                startActivity(hIntent);
                this.finish();
                break;
            case R.id.sunlight_id:
                Intent sIntent = new Intent(this, Sunlight.class);
                startActivity(sIntent);
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

                if (position == 0) {
                } else {
                    measurement_id = measurementIdentifiers.get(position - 1).getMeasurement_number_id();
                    DataList = db.getAllSensorDatabyId(measurement_id);
                    addHeatMap(DataList);
                }
    }

    public void onNothingSelected(AdapterView<?> parent){

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Add a marker in Blacksburg and move the camera
/*        LatLng blacksburg = new LatLng(37.229572,  -80.413940);
        mMap.addMarker(new MarkerOptions().position(blacksburg).title("Marker in Blacksburg"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(blacksburg));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));*/
        if (!boundary.isEmpty()) {
            PolygonOptions opts = new PolygonOptions();
            for (LatLng location : boundary) {
                opts.add(location);
            }
            Polygon polygon = mMap.addPolygon(opts.strokeColor(Color.RED).fillColor(Color.BLUE));
            Collections.sort(lats);
            Collections.sort(lngs);
            mNortheast = new LatLng(lats.get(lats.size() - 1), lngs.get(lngs.size() - 1));
            mSouthwest = new LatLng(lats.get(0), lngs.get(0));
            LatLngBounds field_bound = new LatLngBounds(mSouthwest,mNortheast);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(field_bound,0));
        }

    }

    private void addHeatMap(List<SensorData> DataList) {
        //ArrayList<LatLng> points = new ArrayList<>();
        ArrayList<WeightedLatLng> data = new ArrayList<>();
        for(int i = 0; i<DataList.size();i++) {
            SensorData sensorData = DataList.get(i);
            double lat = sensorData.getLat();
            double lng = sensorData.getLng();
            int moisture = sensorData.getmoisture();
            data.add(new WeightedLatLng(new LatLng(lat,lng),(double)moisture));
            //points.add(new LatLng(lat,lng));
        }

        data.add(new WeightedLatLng(new LatLng(-90,0),100));
        data.add(new WeightedLatLng(new LatLng(-90,0),0));

        /*Toast complete = Toast.makeText(getApplicationContext(), String.valueOf(data), Toast.LENGTH_SHORT);
        complete.show();*/

        // Gradient
        int[] colors = {
                Color.rgb(255,0,0),     //red
                Color.rgb(102,225,0),   // green
                Color.rgb(0,0,255)      // blue
        };

        float[] startPoints = {
                0.1f, 0.5f, 1f
        };

        Gradient gradient = new Gradient(colors,startPoints);

        // Create a heat map tile provider
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().weightedData(data).radius(30).gradient(gradient).build();
        mProvider.setOpacity(0.6);
        // Add a tile overlay to the map
        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }
}
