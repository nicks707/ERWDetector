package com.example.eatadssql;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import java.util.ArrayList;
import java.util.HashMap;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
 
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends Activity implements SensorEventListener{
	
	DBController controller = new DBController(this);
    //Progress Dialog Object
    ProgressDialog prgDialog;
    
    private ProgressBar mProgress;
	
    
    private SensorManager sensorManager = null;
	private float[] geomag = new float[3];
	public int calib = 40;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		setContentView(R.layout.activity_main);
		
		 ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
	        //
		 System.out.println("userlist mai values ----->>>>");
		
		 mProgress = (ProgressBar) findViewById(R.id.progress_bar);
			mProgress.setMax(100);
		 
		 
		 System.out.println("userlist mai values ----->>>>");
			
	        if(userList.size()!=0){
	            //Set the User Array list in ListView
	            ListAdapter adapter = new SimpleAdapter( MainActivity.this,userList, R.layout.view_entry, new String[] { "Id","site","camp"}, new int[] {R.id.userId, R.id.userName,R.id.viewCamp});
	            ListView myList=(ListView)findViewById(android.R.id.list);
	            myList.setAdapter(adapter);
	            //Display Sync status of SQLite DB
	            Toast.makeText(getApplicationContext(), controller.getSyncStatus(), Toast.LENGTH_LONG).show();
	        }
	        //Initialize Progress Dialog properties
	        prgDialog = new ProgressDialog(this);
	        prgDialog.setMessage("Synching SQLite Data with Remote MySQL DB. Please wait...");
	        prgDialog.setCancelable(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle action bar item clicks here. The action bar will
	        // automatically handle clicks on the Home/Up button, so long
	        // as you specify a parent activity in AndroidManifest.xml.
	        int id = item.getItemId();
	        //When Sync action button is clicked
	        if (id == R.id.action_settings) {
	            //Sync SQLite DB data to remote MySQL DB
	            syncSQLiteMySQLDB();
	            return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }
	 
	 public void addUser(View view) {
	        Intent objIntent = new Intent(getApplicationContext(), NewUser.class);
	        startActivity(objIntent);
	    }
	 
	 public void syncSQLiteMySQLDB(){
	        //Create AsycHttpClient object
	        AsyncHttpClient client = new AsyncHttpClient();
	        RequestParams params = new RequestParams();
	        ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
	        if(userList.size()!=0){
	            if(controller.dbSyncCount() != 0){
	                prgDialog.show();
	                System.out.println("Userslist mai poooop==");
	                params.put("usersJSON", controller.composeJSONfromSQLite());
	               
	               
	                System.out.println("JSON vals"+controller.composeJSONfromSQLite().toString().length());
	              
	                 client.post("http://bluemoon.3space.info/insertuser.php",params ,new AsyncHttpResponseHandler() {
	                    @Override
	                    public void onSuccess(String response) {
	                    	System.out.println("fuck successsssss in responseee");
	                        System.out.println(response);
	                        prgDialog.hide();
	                        try {
	                            JSONArray arr = new JSONArray(response);
	                            System.out.println(arr.length());
	                            for(int i=0; i<arr.length();i++){
	                                JSONObject obj = (JSONObject)arr.get(i);
	                                System.out.println(obj.get("id"));
	                                System.out.println(obj.get("status"));
	                                
	                                controller.updateSyncStatus(obj.get("id").toString(),obj.get("status").toString());
	                            }
	                            Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
	                        } catch (JSONException e) {
	                            // TODO Auto-generated catch block
	                            Toast.makeText(getApplicationContext(), "Error Occured Json!" , Toast.LENGTH_LONG).show();
	                            e.printStackTrace();
	                        }
	                    }
	 
	                    @Override
	                    public void onFailure(int statusCode, Throwable error,
	                        String content) {
	                        // TODO Auto-generated method stub
	                    	System.out.println("fuck fail in responseee");
	                        
	                        prgDialog.hide();
	                        if(statusCode == 404){
	                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
	                        }else if(statusCode == 500){
	                            Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
	                        }else{
	                            Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
	                        }
	                    }
	                });
	            }else{
	                Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
	            }
	        }else{
	                Toast.makeText(getApplicationContext(), "No data in SQLite DB, please do enter User name to perform Sync action", Toast.LENGTH_LONG).show();
	        }
	    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		synchronized (this) {
			geomag = sensorEvent.values.clone();

			if (geomag != null) {

				
				
				mProgress.setProgress(getProgressBarValue(geomag[0]));
				
				//Metal detected.
				if( Math.abs(geomag[0]) > calib){
					playAlarm();
					Toast.makeText(this, "Metal detected", Toast.LENGTH_LONG).show();
					//Report.setVisibility(View.VISIBLE);
				}
			}
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Register this class as a listener for the sensors.
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		// Unregister the listener
		sensorManager.unregisterListener(this);
	}
	
	
	private void playAlarm() {
		MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.buzzer);
		
		if(!mp.isPlaying()){ //If mediaplayer is not already playing.
			
			mp.start();
			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}

			});
		}

	}
	
	private int getProgressBarValue(float sensorValue){
		sensorValue = Math.abs(sensorValue);
		
		if(sensorValue >= 10 &&  sensorValue < 30)
			return 15;
		else if(sensorValue >= 30 &&  sensorValue < 50)
			return 30;
		else if(sensorValue >= 30 &&  sensorValue < 50)
			return 30;
		else if(sensorValue >= 50 &&  sensorValue < 70)
			return 50;
		else if(sensorValue >= 70 &&  sensorValue < 100)
			return 75;
		else if(sensorValue >= 100)
			return 99;
		else 
			return 0;
	}

}
