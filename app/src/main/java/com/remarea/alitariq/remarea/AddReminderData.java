package com.remarea.alitariq.remarea;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoTimeoutException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddReminderData extends AppCompatActivity implements View.OnClickListener {
    LatLng marker;
    TextView location;
    EditText radius, text;
    Button addReminderButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder_data);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            marker = new LatLng(extras.getDouble("lat"), extras.getDouble("lon"));
        }

        location = (TextView) findViewById(R.id.locationDetail);
        radius = (EditText) findViewById(R.id.radiusData);
        text = (EditText) findViewById(R.id.textData);
        addReminderButton = (Button) findViewById(R.id.addReminderButton);

        addReminderButton.setOnClickListener(this);
        addReminderButton.setEnabled(false);

        new getLocationData().execute();
    }

    @Override
    public void onClick(View v) {
        String radius_s = radius.getText().toString();
        String location_s = location.getText().toString();
        String text_s = text.getText().toString();

        if (radius_s.equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddReminderData.this, "Enter Reminder Radius!", Toast.LENGTH_LONG).show();
                }
            });
        }
        else if(text_s.equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddReminderData.this, "Enter Reminder Text!", Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            new AsynchronousDatabaseReminderInserter().execute(radius_s, text_s, location_s);
        }
    }

    class getLocationData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getAddress(marker.latitude, marker.longitude);
        }

        public String getAddress(double lat, double lng) {
            Geocoder myLocation = new Geocoder(AddReminderData.this, Locale.getDefault());
            List<Address> myList = null;
            try {
                myList = myLocation.getFromLocation(lat,lng, 1);
                Address address = (Address) myList.get(0);
                String addressStr = "";
                addressStr += address.getAddressLine(0) + ", ";
                addressStr += address.getAddressLine(1) + ", ";
                addressStr += address.getAddressLine(2);

               return addressStr;
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddReminderData.this, "Error! Couldn't retrieve address.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        protected void onPostExecute(String result) {
            location.setText(result);
            addReminderButton.setEnabled(true);
        }
    }

    private class AsynchronousDatabaseReminderInserter extends AsyncTask<String, Void, Void> {
        DBCollection coll= MainActivity.database.getCollection("reminders");
        @Override
        protected Void doInBackground(String... params) {
            try {
                BasicDBObject doc = new BasicDBObject("username", userHome.username)
                        .append("lat", marker.latitude)
                        .append("lon", marker.longitude)
                        .append("radius", Integer.parseInt(params[0]))
                        .append("text", params[1])
                        .append("address", params[2])
                        .append("snoozed", true)
                        .append("time", new Date().getTime());
                coll.insert(doc);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddReminderData.this, "Reminder saved in Database!", Toast.LENGTH_LONG).show();
                    }
                });

                Intent myIntent = new Intent(AddReminderData.this, viewReminders.class);
                AddReminderData.this.startActivity(myIntent);
            }
            catch (MongoTimeoutException e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddReminderData.this, "Error! Connection Error!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }
    }
}
