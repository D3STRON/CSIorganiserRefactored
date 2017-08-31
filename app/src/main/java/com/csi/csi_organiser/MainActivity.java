package com.csi.csi_organiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

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





