package com.remarea.alitariq.remarea;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;

import org.bson.Document;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {
    EditText username, password;
    Button login;
    TextView register;

    ProgressDialog progress;

    CallbackManager callbackManager;

    public static DB database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
            return;
        }

        new AsynchronousDatabaseConnector().execute();

        SharedPreferences myPrefs = getSharedPreferences("RemAreaPrefs", MODE_PRIVATE);
        if(myPrefs.getString("username", null) != null && myPrefs.getString("name", null) != null){
            Intent myIntent = new Intent(this, userHome.class);
            myIntent.putExtra("username", myPrefs.getString("username", null));
            myIntent.putExtra("name", myPrefs.getString("name", null));
            this.startActivity(myIntent);
        }
        else {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_main);
            View v = findViewById(R.id.login);
            View root = v.getRootView();
            root.setBackgroundColor(getResources().getColor(android.R.color.white));

            username = (EditText) findViewById(R.id.username);
            password = (EditText) findViewById(R.id.password);
            login = (Button) findViewById(R.id.login);
            register = (TextView) findViewById(R.id.register);
            login.setOnClickListener(this);
            register.setOnClickListener(this);

            callbackManager = CallbackManager.Factory.create();
            final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d("RemArea", "UserID:" + loginResult.getAccessToken().getUserId());

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {

                                    Log.e("response: ", response + "");
                                    try {
                                        progress = ProgressDialog.show(MainActivity.this, "Please Wait", "Logging In!", true);
                                        new AsynchronousRegisteration().execute(object.getString("name").toString(),
                                                object.getString("id").toString(), object.getString("email").toString(), "");

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }

                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    Log.d("RemArea Error", "User cancel Facebook login.");
                }

                @Override
                public void onError(FacebookException e) {
                    Log.d("RemArea Error", "Error on Facebook login.");
                }
            });
        }

    }//OnCreate method ends here

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onBackPressed() {
        turnOff();
    }

    @Override
    public void onClick(View v)
    {
        if (v==findViewById(R.id.login))
        {
            progress = ProgressDialog.show(this, "Please Wait", "Logging In!", true);

            String user = username.getText().toString();
            String pass = password.getText().toString();

            new AsynchronousLogin().execute(user, pass);
        }
        else if (v==findViewById(R.id.register))
        {
            Intent in = new Intent(this, RegisterActivity.class);
            startActivity(in);
        }
    }//OnClick method ends here

    public void turnOff(){
        this.finishAffinity();
    }

    private class AsynchronousDatabaseConnector extends AsyncTask<String, Void, Void> {
        private final String addr = "mongodb://mainuser:seecs123@ds017070.mlab.com:17070/remarea";

        @Override
        protected Void doInBackground(String... params) {
            MongoClientURI uri = new MongoClientURI(addr);
            MongoClient mongoclient =  new MongoClient(uri);
            database = mongoclient.getDB(uri.getDatabase());
//            try {
//                database.command("ping");
//            } catch (MongoTimeoutException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Toast.makeText(MainActivity.this, "Connection Failed!", Toast.LENGTH_LONG).show();
//
//                        turnOff();
//                    }
//                });
//            }
            return null;
        }
    }

    private class AsynchronousLogin extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {
            DBCursor cursor = database.getCollection("users").find(new BasicDBObject("username", params[0]));

            progress.dismiss();

            if(cursor.hasNext()){
                DBObject data = cursor.next();

                if(data.get("password").equals(params[1])){
                    SharedPreferences myPrefs = getSharedPreferences("RemAreaPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor e = myPrefs.edit();
                    e.putString("username", params[0]);
                    e.putString("name", data.get("name").toString());
                    e.commit();

                    Intent myIntent = new Intent(MainActivity.this, userHome.class);
                    myIntent.putExtra("username", params[0]);
                    myIntent.putExtra("name", data.get("name").toString());
                    MainActivity.this.startActivity(myIntent);
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Incorrect Password!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Incorrect Username!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }
    }

    private class AsynchronousRegisteration extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {
            DBCollection col = MainActivity.database.getCollection("users");

            BasicDBObject doc = new BasicDBObject("name", params[0])
                    .append("username", params[1])
                    .append("email", params[2])
                    .append("password", params[3])
                    .append("registeration_time", new Date().getTime());
            try {
                col.insert(doc);
                progress.dismiss();
            }
            catch (MongoTimeoutException e){
                progress.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Error!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            catch(MongoException e){
                progress.dismiss();
            }

            SharedPreferences myPrefs = getSharedPreferences("RemAreaPrefs", MODE_PRIVATE);
            SharedPreferences.Editor e = myPrefs.edit();
            e.putString("username", params[1]);
            e.putString("name", params[0]);
            e.commit();

            Intent myIntent = new Intent(MainActivity.this, userHome.class);
            myIntent.putExtra("username", params[1]);
            myIntent.putExtra("name", params[0]);
            MainActivity.this.startActivity(myIntent);

            return null;
        }
    }
}
