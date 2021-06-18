package com.first.safety;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationSettingsRequest;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    Button b1,b2;
    private FusedLocationProviderClient client;
    DatabaseHandler myDB;
    private final int REQUEST_CHECK_CODE=8989;
    private LocationSettingsRequest.Builder builder;
    String x=" ", y=" ";
    private static final int REQUEST_LOCATION=1;
    private String[]permission={"android.permission.CALL_PHONE"};
    private String[]permission1={"android.permission.ACCESS_FINE_LOCATION"};
    LocationManager locationManager;
    Intent mIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        b1 = findViewById( R.id.button );
        b2= findViewById( R.id.button2);
        myDB = new DatabaseHandler(this);
        final MediaPlayer mp= MediaPlayer.create( getApplicationContext(),R.raw.siren);

        locationManager = (LocationManager) getSystemService( LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            onGPS();
        }
        else {
            startTrack();
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent( getApplicationContext(),Registr.class );
                startActivity(i);
            }
        });

        b2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mp.start();
                Toast.makeText( getApplicationContext(),"PANIC BUTTON STARTED",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadData(){

        ArrayList<String> thelist = new ArrayList<>();
        Cursor data = myDB.getlistContents();
        if (data.getCount()==0){
            Toast.makeText( this,"NO CONTENT TO SHOW",Toast.LENGTH_SHORT).show();
        }
        else {
            String msg="I NEED HELP LATITUDE :"+x+"LONGITUDE:"+y;
            String number = " ";

            while (data.moveToNext()){

                thelist.add(data.getString(1));
                number=number+data.getString( 1)+(data.isLast()?"":";");
                call();

            }
            if (!thelist.isEmpty()){
                sendSms(number,msg,true);
            }
        }

    }

    private void sendSms(String number, String msg, boolean b) {

        Intent smsIntent = new Intent( Intent.ACTION_SENDTO);
        Uri.parse("smsto:"+number);
        smsIntent.putExtra("smsbody",msg);
        startActivity(smsIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void call() {
        Intent i = new Intent( Intent.ACTION_CALL );
        Intent intent = i.setData(Uri.parse("tell:1000"));

        if (ContextCompat.checkSelfPermission(permission)== PackageManager.PERMISSION_GRANTED) {
            startActivity(i);
        }
        else {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES){

                requestPermissions(permission,1);
            }
        }


    }



    private void startTrack() {
        if (ContextCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity2.this,
                permission1)!=PakageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions( this,permission1,REQUEST_LOCATION);
        }
        else{
            Location locationGPS= locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
            if (locationGPS!=null){
                double lat = locationGPS.getLatitude();
                double lon = locationGPS.getLongitude();
                x= String.valueOf(lat);
                y=String.valueOf(lon);
            }
            else {
                Toast.makeText( this,"UNABLE TO FIND LOCATION", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onGPS() {
        final AlertDialog.Builder builder= new AlertDialog.Builder( this);
        builder.setMessage("Enable GPS").setCancelable( false).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final  AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}