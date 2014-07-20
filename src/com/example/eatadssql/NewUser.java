package com.example.eatadssql;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
 
public class NewUser extends Activity {
    EditText userName;
    EditText campaign;
    TextView lat,lon,tstamp;
    ImageView img;
    Button load;
    private static int RESULT_LOAD_IMAGE = 1;
    GPSTracker gps;

    DBController controller = new DBController(this);
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new);
        userName = (EditText) findViewById(R.id.userName);
        campaign=(EditText) findViewById(R.id.campData);
        lat=(TextView) findViewById(R.id.lat);
        lon=(TextView) findViewById(R.id.lon);
        tstamp=(TextView) findViewById(R.id.tstamp);
        img=(ImageView) findViewById(R.id.imgView);
        load =(Button) findViewById(R.id.button1);
        
        load.setOnClickListener(new View.OnClickListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/* Intent i = new Intent(
	                        Intent.ACTION_PICK,
	                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				 System.out.println("intent not working check ");
			     
	                startActivityForResult(i, RESULT_LOAD_IMAGE); */
				long unixTime = System.currentTimeMillis();
				 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                  
			        Date resultdate = new Date(unixTime);
			        System.out.println(Long.toString(unixTime*1000L)+"   ###"+sdf.format(resultdate));
			        tstamp.setText(resultdate.toLocaleString());
				 gps = new GPSTracker(NewUser.this);
		    	 
	                // check if GPS enabled     
	                if(gps.canGetLocation()){
	                     
	                    double latitude = gps.getLatitude();
	                    double longitude = gps.getLongitude();
	                     lat.setText(Double.toString(latitude));
	                     lon.setText(Double.toString(longitude));
	                    // \n is for new line
	                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();    
	                }else{
	                    // can't get location
	                    // GPS or Network is not enabled
	                    // Ask user to enable GPS/network in settings
	                    gps.showSettingsAlert();
	                }
			}
		});
    }
 
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	 Toast.makeText(getApplicationContext(), "successfully  in result",
  			   Toast.LENGTH_LONG).show();
       
		super.onActivityResult(requestCode, resultCode, data);
		
		 Toast.makeText(getApplicationContext(), " xxxx",
  			   Toast.LENGTH_LONG).show();
       
		 if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	            Uri selectedImage = data.getData();
	            String[] filePathColumn = { MediaStore.Images.Media.DATA };
	            Toast.makeText(getApplicationContext(), "successfully got in image",
		    			   Toast.LENGTH_LONG).show();
	             
	            Cursor cursor = getContentResolver().query(selectedImage,
	                    filePathColumn, null, null, null);
	            cursor.moveToFirst();
	 
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String picturePath = cursor.getString(columnIndex);
	            cursor.close();
	            Toast.makeText(getApplicationContext(), "Image aa gyi"+picturePath,
		    			   Toast.LENGTH_LONG).show();
	            
	            
	            
	            img.setImageBitmap(BitmapFactory.decodeFile(picturePath));
	         
	           
	            BitmapFactory.Options options = new BitmapFactory.Options();
	            
	            // downsizing image as it throws OutOfMemory Exception for larger
	            // images
	            BitmapFactory.Options options1 = new BitmapFactory.Options();
	            
	            options1.inSampleSize = 8;
	 
	            final Bitmap bitmap = BitmapFactory.decodeFile(picturePath,
	                    options1);
	             
	               ExifInterface exif1;
	 			try {
	 				exif1 = new ExifInterface(picturePath);
	 				exifToGeo n= new exifToGeo(exif1);
	 				
	 			   String lalong=n.toString();
	 			  String m[] = lalong.split(",");
	 			  lat.setText(m[0]);
	 			  lon.setText(m[1]);
	 			  
	 			  if(m[0].equals("0.0") || m[1].equals("0.0")){
	 				 Toast.makeText(getApplicationContext(), "No geodata in this image using current location instead",
			    			   Toast.LENGTH_LONG).show();
	 				 gps = new GPSTracker(NewUser.this);
			    	 
		                // check if GPS enabled     
		                if(gps.canGetLocation()){
		                     
		                    double latitude = gps.getLatitude();
		                    double longitude = gps.getLongitude();
		                     lat.setText(Double.toString(latitude));
		                     lon.setText(Double.toString(longitude));
		                    // \n is for new line
		                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();    
		                }else{
		                    // can't get location
		                    // GPS or Network is not enabled
		                    // Ask user to enable GPS/network in settings
		                    gps.showSettingsAlert();
		                }
	 			  }
	 				//ShowExif(exif1);
	 			} catch (IOException e) {
	 				// TODO Auto-generated catch block
	 				e.printStackTrace();
	 			}
	            
	        } 
		     else {
	        	
		    	 Toast.makeText(getApplicationContext(), "some error loading image"+resultCode,
		    			   Toast.LENGTH_LONG).show();
		    	 gps = new GPSTracker(NewUser.this);
		    	 
	                // check if GPS enabled     
	                if(gps.canGetLocation()){
	                     
	                    double latitude = gps.getLatitude();
	                    double longitude = gps.getLongitude();
	                    
	                     lat.setText(Double.toString(latitude));
	                     lon.setText(Double.toString(longitude));
	                     
	                    // \n is for new line
	                     
	                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();    
	                }else{
	                    // can't get location
	                    // GPS or Network is not enabled
	                    // Ask user to enable GPS/network in settings
	                    gps.showSettingsAlert();
	                }
	        }
		 
	}

	/**
     * Called when Save button is clicked 
     * @param view
     */
    public void addNewUser(View view) {
    	Log.e("navjottt", "got in add user");
    	long unixTime = System.currentTimeMillis() / 1000L;

        HashMap<String, String> queryValues = new HashMap<String, String>();
        queryValues.put("Id", Long.toString(unixTime));
        queryValues.put("site", userName.getText().toString());
        queryValues.put("camp", campaign.getText().toString());
        queryValues.put("lat", lat.getText().toString());
        queryValues.put("lon", lon.getText().toString());
        queryValues.put("createdtime", Long.toString(unixTime));
        Log.e("got values",queryValues.toString());
        
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.images);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object   
        byte[] b = baos.toByteArray(); 
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        queryValues.put("img", encodedImage);
        

        
        if (userName.getText().toString() != null
                && userName.getText().toString().trim().length() != 0) {
            controller.insertUser(queryValues);
            this.callHomeActivity(view);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter User name",
                    Toast.LENGTH_LONG).show();
        }
    }
 
    /**
     * Navigate to Home Screen 
     * @param view
     */
    public void callHomeActivity(View view) {
        Intent objIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(objIntent);
    }
 
    /**
     * Called when Cancel button is clicked
     * @param view
     */
    public void cancelAddUser(View view) {
        this.callHomeActivity(view);
    }
}