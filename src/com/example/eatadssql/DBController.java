package com.example.eatadssql;

import java.util.ArrayList;
import java.util.HashMap;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 
public class DBController  extends SQLiteOpenHelper {
 
    public DBController(Context applicationcontext) {
        super(applicationcontext, "eatadsqlite.db", null, 1);
    }
    //Creates Table
    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE info ( Id BIGINT PRIMARY KEY , site TEXT , camp TEXT , lat DOUBLE , lon DOUBLE , createdtime TIMESTAMP , img TEXT , updateStatus TEXT)";
        database.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        Log.e("updated", "db");
        query = "DROP TABLE IF EXISTS info";
        database.execSQL(query);
        onCreate(database);
    }
    /**
     * Inserts data into SQLite DB
     * @param queryValues
     */
    public void insertUser(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Id", queryValues.get("Id"));
        values.put("site", queryValues.get("site"));
        values.put("camp", queryValues.get("camp"));
        values.put("lat", queryValues.get("lat"));
        values.put("lon", queryValues.get("lon"));
        values.put("createdtime", queryValues.get("createdtime"));
        values.put("img", queryValues.get("img"));
        values.put("updateStatus", "no");
        Log.e("eeeeeeeee",values.toString());
        database.insert("info", null, values);
        database.close();
    }
 
    /**
     * Get data from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM info";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("site", cursor.getString(1));
                map.put("camp", cursor.getString(2));
                map.put("lat", cursor.getString(3));
                map.put("lon", cursor.getString(4));
                map.put("createdtime", cursor.getString(5));
                map.put("img", cursor.getString(6));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }
 
    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM info where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("site", cursor.getString(1));
                map.put("camp", cursor.getString(2));
                map.put("lat", cursor.getString(3));
                map.put("lon", cursor.getString(4));
                map.put("createdtime", cursor.getString(5));
                map.put("img", cursor.getString(6));
                
                
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }
 
    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync needed\n";
        }
        return msg;
    }
 
    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM info where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }
 
    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();     
        String updateQuery = "Update info set updateStatus = '"+ status +"' where Id="+"'"+ id +"'";
        Log.d("query",updateQuery);        
        database.execSQL(updateQuery);
        database.close();
    }
}