package com.remarea.alitariq.remarea;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoTimeoutException;

import java.util.ArrayList;
import java.util.Date;

public class viewReminders extends ListActivity {
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        ListView lv = getListView();
        registerForContextMenu(lv);

        new getReminders().execute(userHome.username);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //if (v.getId()==R.id.list_view) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        //}
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.view:
                Intent myIntent = new Intent(this, viewReminderonMap.class);
                myIntent.putExtra("lat", (Double) userHome.list_data[info.position].get("lat"));
                myIntent.putExtra("lon", (Double) userHome.list_data[info.position].get("lon"));
                myIntent.putExtra("address", (String) userHome.list_data[info.position].get("address"));
                myIntent.putExtra("radius", (Integer) userHome.list_data[info.position].get("radius"));
                this.startActivity(myIntent);

                return true;
            case R.id.delete:
                adapter.remove(adapter.getItem(info.position));
                new deleteReminders().execute(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent myIntent = new Intent(this, userHome.class);
        this.startActivity(myIntent);
    }

    public void removeListElement(int del) {
        DBObject[] newList = new DBObject[userHome.list_data.length-1];

        for(int i=0,j=0; i<userHome.list_data.length; i++){
            if(i != del){
                newList[j] = userHome.list_data[i];
                j++;
            }
        }
        userHome.list_data = newList;
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
                        Toast.makeText(viewReminders.this, "Error! Could'nt retrieve Reminders.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        protected void onPostExecute(DBObject[] rows) {
            userHome.list_data = rows;

            for(int i=0; i<rows.length; i++){

                Date date = new Date((Long)rows[i].get("time"));
                Double lat = (double)Math.round((Double)rows[i].get("lat") * 1000000d) / 1000000d;
                Double lon = (double)Math.round((Double)rows[i].get("lon") * 1000000d) / 1000000d;
                adapter.add("\n" + "Location: " + (String)rows[i].get("address") + "\n"
                                + "Coords: " + lat + "," + lon + "\n"
                                + "Radius: " + (Integer)rows[i].get("radius") + "\n"
                                + "Text: " + (String)rows[i].get("text") + "\n"
                                + "Added on: " + date.toLocaleString() + "\n");
            }
            adapter.notifyDataSetChanged();
        }
    }

    class deleteReminders extends AsyncTask<Integer, Void, Integer>{

        @Override
        protected Integer doInBackground(Integer... params) {
            DBCollection coll = MainActivity.database.getCollection("reminders");
            coll.remove(userHome.list_data[params[0]]);
            return params[0];
        }

        protected void onPostExecute(Integer i){
            removeListElement(i);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(viewReminders.this, "Reminder Deleted Successfully!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
