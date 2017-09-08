package com.csi.csi_organiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private Button mSignUpBtn;
    private Button mAlready;

   SQLiteHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db=new SQLiteHelper(this);
        HashMap<String,String> users=db.getAllValues();

        if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }

      else if(!users.isEmpty())
        {
            Toast.makeText(MainActivity.this,"There is a current User!",Toast.LENGTH_LONG).show();
            if(users.get("priority").matches("1"))
            {
                //Intent intent= new Intent(HomeActivity.this,CoreActivity.class);
                //startActivity(intent);

            }
            else if(users.get("priority").matches("2"))
            {
                Intent intent= new Intent(MainActivity.this,JcActivity.class);
                startActivity(intent);
            }
            else
            {

                Intent intent =new Intent(MainActivity.this,Members.class);
                startActivity(intent);
            }
        }

        mSignUpBtn = (Button) findViewById(R.id.SignUpBtn);
        mAlready = (Button)findViewById(R.id.button);
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent DataEntry = new Intent(MainActivity.this,HomeActivity.class);
                startActivity(DataEntry);
            }
        });
        mAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Signin = new Intent(MainActivity.this,GSignin.class);
                db.deleteUsers();
                startActivity(Signin);

            }
        });

    }

    }





