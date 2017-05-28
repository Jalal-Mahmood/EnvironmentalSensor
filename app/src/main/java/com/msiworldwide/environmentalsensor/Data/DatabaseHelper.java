package com.msiworldwide.environmentalsensor.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    // Log tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Data";

    // Table Names
    private static final String TABLE_SENSOR_DATA = "SensorData";
    private static final String TABLE_FIELD = "FieldData";
    private static final String TABLE_MEASUREMENT_IDENTIFIERS = "MeasurementIdentifiers";
    private static final String TABLE_WATER = "WaterSourceData";

    // Common column names
    public static final String KEY_MeasurementId = "MeasurementNumberId";
    public static final String KEY_FieldId = "FieldId";
    public static final String KEY_Date = "Date";

    // SensorData Column names
    public static final String KEY_Time = "Time";
    public static final String KEY_Latitude = "Latitude";
    public static final String KEY_Longitude = "Longitude";
    public static final String KEY_Moisture = "Moisture";
    public static final String KEY_Sunlight = "Sunlight";
    public static final String KEY_Temperature = "Temperature";
    public static final String KEY_Humidity = "Humidity";

    // FieldData column names
    public static final String KEY_Coordinates = "Coordinates";

    // WaterSourceData
    public static final String KEY_WaterSourceId = "WaterSourceId";

    // Table Create Statements
    // SensorData table create
    private static final String CREATE_TABLE_SensorData = "CREATE TABLE " + TABLE_SENSOR_DATA
            + "(" + KEY_MeasurementId + " INTEGER," + KEY_FieldId + " TEXT," + KEY_Date
            + "TEXT," + KEY_Time + "TEXT," + KEY_Latitude + " INTEGER," + KEY_Longitude + " INTEGER,"
            + KEY_Moisture + " INTEGER," + KEY_Sunlight + " INTEGER," + KEY_Temperature + " INTEGER,"
            + KEY_Humidity + " INTEGER" + ")";

    private static final String CREATE_TABLE_FieldData = "CREATE TABLE " + TABLE_FIELD +
            "(" + KEY_FieldId + " TEXT PRIMARY KEY," + KEY_Coordinates + " TEXT" + ")";

    private static final String CREATE_TABLE_MeasurementIdentifiers = "CREATE TABLE " + TABLE_MEASUREMENT_IDENTIFIERS +
            "(" + KEY_MeasurementId + " INTEGER PRIMARY KEY," + KEY_FieldId + " TEXT," + KEY_Date + " TEXT" + ")";

    private static final String CREATE_TABLE_WaterSourceData = "CREATE TABLE " + TABLE_WATER +
            "(" + KEY_WaterSourceId + " TEXT PRIMARY KEY," + KEY_Coordinates + " TEXT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating required tables
        db.execSQL(CREATE_TABLE_SensorData);
        db.execSQL(CREATE_TABLE_FieldData);
        db.execSQL(CREATE_TABLE_MeasurementIdentifiers);
        db.execSQL(CREATE_TABLE_WaterSourceData);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_SENSOR_DATA);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_FIELD);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_MEASUREMENT_IDENTIFIERS);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_WATER);

        // create new tables
        onCreate(db);
    }

    // Creating a single SensorData
    public long createSensorData(SensorData sensorData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MeasurementId, sensorData.measurement_number_id);
        values.put(KEY_FieldId, sensorData.FieldId);
        values.put(KEY_Date, sensorData.Date);
        values.put(KEY_Time, sensorData.Time);
        values.put(KEY_Latitude, sensorData.lat);
        values.put(KEY_Longitude, sensorData.lng);
        values.put(KEY_Moisture, sensorData.moisture);
        values.put(KEY_Sunlight, sensorData.sunlight);
        values.put(KEY_Temperature, sensorData.temperature);
        values.put(KEY_Humidity, sensorData.humidity);

        // insert row
        long SensorData_id = db.insert(TABLE_SENSOR_DATA, null, values);

        return SensorData_id;
    }

    // Getting a single SensorData
    public SensorData getSensorData(long SensorData_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR_DATA + " WHERE "
                + KEY_MeasurementId + " = " + SensorData_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        SensorData sd = new SensorData();
        sd.setMeasurementNumberId(c.getInt(c.getColumnIndex(KEY_MeasurementId)));
        sd.setFieldId(c.getString(c.getColumnIndex(KEY_FieldId)));
        sd.setDate(c.getString(c.getColumnIndex(KEY_Date)));
        sd.setTime(c.getString(c.getColumnIndex(KEY_Time)));
        sd.setlat(c.getInt(c.getColumnIndex(KEY_Latitude)));
        sd.setlng(c.getInt(c.getColumnIndex(KEY_Longitude)));
        sd.setmoisture(c.getInt(c.getColumnIndex(KEY_Moisture)));
        sd.setsunlight(c.getInt(c.getColumnIndex(KEY_Sunlight)));
        sd.settemperature(c.getInt(c.getColumnIndex(KEY_Temperature)));
        sd.sethumidity(c.getInt(c.getColumnIndex(KEY_Humidity)));

        return sd;
    }

    // Getting all SensorData
    public List<SensorData> getAllSensorData() {
        List<SensorData> sensorDatas = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR_DATA;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c.moveToFirst()) {
            do {
                SensorData sd = new SensorData();
                sd.setMeasurementNumberId(c.getInt(c.getColumnIndex(KEY_MeasurementId)));
                sd.setFieldId(c.getString(c.getColumnIndex(KEY_FieldId)));
                sd.setDate(c.getString(c.getColumnIndex(KEY_Date)));
                sd.setTime(c.getString(c.getColumnIndex(KEY_Time)));
                sd.setlat(c.getInt(c.getColumnIndex(KEY_Latitude)));
                sd.setlng(c.getInt(c.getColumnIndex(KEY_Longitude)));
                sd.setmoisture(c.getInt(c.getColumnIndex(KEY_Moisture)));
                sd.setsunlight(c.getInt(c.getColumnIndex(KEY_Sunlight)));
                sd.settemperature(c.getInt(c.getColumnIndex(KEY_Temperature)));
                sd.sethumidity(c.getInt(c.getColumnIndex(KEY_Humidity)));

                // adding to list
                sensorDatas.add(sd);
            } while (c.moveToNext());
        }
        return sensorDatas;
    }

    // Get all SensorData under a single date
    public List<SensorData> getAllSensorDatabyDate(String FieldId, String Date) {
        List<SensorData> sensorDatas = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR_DATA + " WHERE "
                + KEY_Date + " = '" + Date + "' AND " + KEY_FieldId + " = '" +
                FieldId + "'";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                SensorData sd = new SensorData();
                sd.setMeasurementNumberId(c.getInt(c.getColumnIndex(KEY_MeasurementId)));
                sd.setFieldId(c.getString(c.getColumnIndex(KEY_FieldId)));
                sd.setDate(c.getString(c.getColumnIndex(KEY_Date)));
                sd.setTime(c.getString(c.getColumnIndex(KEY_Time)));
                sd.setlat(c.getInt(c.getColumnIndex(KEY_Latitude)));
                sd.setlng(c.getInt(c.getColumnIndex(KEY_Longitude)));
                sd.setmoisture(c.getInt(c.getColumnIndex(KEY_Moisture)));
                sd.setsunlight(c.getInt(c.getColumnIndex(KEY_Sunlight)));
                sd.settemperature(c.getInt(c.getColumnIndex(KEY_Temperature)));
                sd.sethumidity(c.getInt(c.getColumnIndex(KEY_Humidity)));

                // adding to list
                sensorDatas.add(sd);
            } while(c.moveToNext());
        }

        return sensorDatas;
    }

    // Deleting SensorData
    public void deleteSensorData(long SensorData_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SENSOR_DATA, KEY_MeasurementId + "= ?",
                new String[] {String.valueOf(SensorData_id)});
    }

    // Creating Field
    public long createFieldData(FieldData fieldData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FieldId, fieldData.getFieldId());
        values.put(KEY_Coordinates, fieldData.getCoordinates());

        // insert row
        long field_id = db.insert(TABLE_FIELD, null, values);

        return field_id;
    }

    // Get all FieldData
    public List<FieldData> getAllFieldData() {
        List<FieldData> fieldDatas = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FIELD;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c.moveToFirst()) {
            do {
                FieldData fd = new FieldData();
                fd.setFieldId(c.getString(c.getColumnIndex(KEY_FieldId)));
                fd.setCoordinates(c.getString(c.getColumnIndex(KEY_Coordinates)));

                // adding to list
                fieldDatas.add(fd);
            } while(c.moveToNext());
        }
        return fieldDatas;
    }

    // Updating FieldData
    public int updateFieldData(FieldData fieldData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_Coordinates, fieldData.getCoordinates());

        // updating row
        return db.update(TABLE_FIELD, values, KEY_FieldId + " = ?",
                new String[] {String.valueOf(fieldData.getFieldId())});
    }

    // Deleting FieldData under Field Name
    public void deleteField(FieldData fieldData) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete
        db.delete(TABLE_FIELD, KEY_FieldId + " = ?",
                new String[] {String.valueOf(fieldData.getFieldId())});
    }

    // Creating MeasurementId
    public long createMeasurementId(MeasurementIdentifiers measurementIdentifiers) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MeasurementId, measurementIdentifiers.getMeasurement_number_id());
        values.put(KEY_FieldId, measurementIdentifiers.getFieldId());
        values.put(KEY_Date, measurementIdentifiers.getDate());

        // insert row
        long measurement_id = db.insert(TABLE_MEASUREMENT_IDENTIFIERS, null, values);

        return measurement_id;
    }

    // Get all MeasurementId
    public List<MeasurementIdentifiers> getAllMeasurementId() {
        List<MeasurementIdentifiers> measurementIdentifierses = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_MEASUREMENT_IDENTIFIERS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c.moveToFirst()) {
            do {
                MeasurementIdentifiers mid = new MeasurementIdentifiers();
                mid.setMeasurementNumberId(c.getInt(c.getColumnIndex(KEY_MeasurementId)));
                mid.setFieldId(c.getString(c.getColumnIndex(KEY_FieldId)));
                mid.setDate(c.getString(c.getColumnIndex(KEY_Date)));

                // adding to list
                measurementIdentifierses.add(mid);
            } while(c.moveToNext());
        }
        return measurementIdentifierses;
    }

    // Updating MeasurementIdentifiers
    public int updateMeasurementIdentifiers(MeasurementIdentifiers measurementIdentifiers) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FieldId, measurementIdentifiers.getFieldId());
        values.put(KEY_Date, measurementIdentifiers.getDate());

        // updating row
        return db.update(TABLE_MEASUREMENT_IDENTIFIERS, values, KEY_MeasurementId + " = ?",
                new String[] {String.valueOf(measurementIdentifiers.getMeasurement_number_id())});
    }

    // Deleting MeasurementIdentifiers
    public void deleteMeasurementId(MeasurementIdentifiers measurementIdentifiers) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete
        db.delete(TABLE_MEASUREMENT_IDENTIFIERS, KEY_MeasurementId + " = ?",
                new String[] {String.valueOf(measurementIdentifiers.getMeasurement_number_id())});
    }

    // Creating WaterSource
    public long createWaterSourceData(WaterSourceData waterSourceData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WaterSourceId, waterSourceData.getWaterSourceId());
        values.put(KEY_Coordinates, waterSourceData.getCoordinates());

        // insert row
        long watersource_id = db.insert(TABLE_WATER, null, values);

        return watersource_id;
    }

    // Get all WaterSourceData
    public List<WaterSourceData> getAllWaterSourceData() {
        List<WaterSourceData> waterSourceDatas = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_WATER;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c.moveToFirst()) {
            do {
                WaterSourceData wsd = new WaterSourceData();
                wsd.setWaterSourceId(c.getString(c.getColumnIndex(KEY_WaterSourceId)));
                wsd.setCoordinates(c.getString(c.getColumnIndex(KEY_Coordinates)));

                // adding to list
                waterSourceDatas.add(wsd);
            } while(c.moveToNext());
        }
        return waterSourceDatas;
    }

    // Updating WaterSourceData
    public int updateWaterSourceData(WaterSourceData waterSourceData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_Coordinates, waterSourceData.getCoordinates());

        // updating row
        return db.update(TABLE_WATER, values, KEY_WaterSourceId + " = ?",
                new String[] {String.valueOf(waterSourceData.getWaterSourceId())});
    }

    // Deleting WaterSourceData under Water Source Name
    public void deleteWaterSource(WaterSourceData waterSourceData) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete
        db.delete(TABLE_WATER, KEY_WaterSourceId + " = ?",
                new String[] {String.valueOf(waterSourceData.getWaterSourceId())});
    }

    // close database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null && db.isOpen())
            db.close();
    }
}
