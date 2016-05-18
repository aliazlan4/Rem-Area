package com.remarea.alitariq.remarea;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoTimeoutException;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class userHome extends Activity implements View.OnClickListener {
    public static String username;
    public static String name;
    public static DBObject[] list_data;

    Button addReminder, checkReminders;

    TextView welcomeUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        welcomeUsername = (TextView) findViewById(R.id.welcomeUsername);
        addReminder = (Button) findViewById(R.id.addReminder);
        checkReminders = (Button) findViewById(R.id.checkReminders);

        addReminder.setOnClickListener(this);
        checkReminders.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            name = extras.getString("name");
        }

        welcomeUsername.setText("Welcome: " + name);

        new getReminders().execute(username);
    }

    @Override
    public void onClick(View v) {
        if (v == addReminder){
            this.startActivity(new Intent(this, addReminderMap.class));
        }
        else if(v == checkReminders){
            Intent myIntent = new Intent(this, viewReminders.class);
            this.startActivity(myIntent);
        }
    }

    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Do you want to Logout?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                username = null;

                SharedPreferences myPrefs = getSharedPreferences("RemAreaPrefs", MODE_PRIVATE);
                SharedPreferences.Editor e = myPrefs.edit();
                e.remove("username");
                e.remove("name");
                e.commit();

                Intent myIntent = new Intent(userHome.this, MainActivity.class);
                userHome.this.startActivity(myIntent);

                dialog.dismiss();
            }

        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    class getReminders extends AsyncTask<String, Void, DBObject[]> {

        @Override
        protected DBObject[] doInBackground(String... params) {
            try {
                DBCursor cursor = MainActivity.database.getCollection("reminders")
                        .find(new BasicDBObject("username", params[0]));

                DBObject[] rows = new DBObject[cursor.count()];
                for(int i=0; i<rows.length; i++){
                    rows[i] = cursor.next();
                }

                return rows;
            } catch (MongoTimeoutException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(userHome.this, "Error! Could'nt retrieve Reminders.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        protected void onPostExecute(DBObject[] rows) {
            list_data = rows;

            Intent in = new Intent(getApplicationContext(), alarmService.class);
            startService(in);
        }
    }
}
