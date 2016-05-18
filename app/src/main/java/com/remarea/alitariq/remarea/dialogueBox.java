package com.remarea.alitariq.remarea;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import java.util.Date;

public class dialogueBox extends Activity {
    int notif_id = 0;
    String alarm_id=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alarmService.mp.stop();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            alarm_id = extras.getString("alarm_id");
            notif_id = (int) extras.getInt("notif_id");
        }

        int length = userHome.list_data.length;

        for(int i=0; i<length; i++){
            String id = userHome.list_data[i].get("_id").toString();
            if(id.equals(alarm_id)){
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(notif_id);

                Date date = new Date((Long)userHome.list_data[i].get("time"));
                showDialogue((String)userHome.list_data[i].get("text"), (String)userHome.list_data[i].get("address"), date.toLocaleString());
                break;
            }
        }
    }

    public void showDialogue(String text, String location, String time)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("RemArea - Alarm");
        builder.setMessage("Alarm text: " + text + "\n\n" + "Location: " + location + "\n\n" + "Added on: " + time);

        builder.setPositiveButton("Thanks", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                new deleteReminder().execute();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }

        });

        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                new turnOnSnooze().execute();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private class deleteReminder extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {
            DBCollection coll = MainActivity.database.getCollection("reminders");

            DBObject temp = null;
            for(int i=0; i<userHome.list_data.length; i++){
                if(userHome.list_data[i].get("_id").toString().equals(alarm_id)){
                    temp = userHome.list_data[i];
                    break;
                }
            }

            try{
                coll.remove(temp);
            }
            catch(MongoException e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dialogueBox.this, "Error in deleting alarm.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }
    }
    private class turnOnSnooze extends AsyncTask<DBObject, Void, Void>{
        @Override
        protected Void doInBackground(DBObject... params) {
            DBCollection coll = MainActivity.database.getCollection("reminders");

            BasicDBObject setQuery = new BasicDBObject();
            setQuery.append("$set", new BasicDBObject("snoozed", true));

            DBObject temp = null;
            for(int i=0; i<userHome.list_data.length; i++){
                if(userHome.list_data[i].get("_id").toString().equals(alarm_id)){
                    temp = userHome.list_data[i];
                    break;
                }
            }

            try{
                coll.update(temp, setQuery);
            }
            catch(MongoException e){
                Log.d("RemArea Exception", "Exception in dialogueBox {" + e.toString() + "}");
            }

            return null;
        }
    }
}
