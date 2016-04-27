package com.remarea.alitariq.remarea;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
    EditText username, password;
    Button login;
    TextView register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        View v = findViewById(R.id.login);
        View root = v.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));

        username= (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }//OnCreate method ends here




    @Override
    public void onClick(View v)
    {
        if (v==findViewById(R.id.login))
        {
            String user = username.getText().toString();
            String pass = password.getText().toString();
        }
        else if (v==findViewById(R.id.register))
        {
            Intent in = new Intent(this, RegisterActivity.class);
            startActivity(in);
        }
    }//OnClick method ends here
}
