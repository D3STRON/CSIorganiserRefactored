package com.csi.csi_organiser;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
    EditText firstname, lastname,email, number,rollno, neareststation;
    Spinner team1, team2, team3;
    String preference1, preference2, preference3;
    Button update;
    SQLiteHelper db;
    HashMap<String,String> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        firstname= (EditText)findViewById(R.id.firstname);
        lastname= (EditText)findViewById(R.id.lastname);
        email= (EditText)findViewById(R.id.email);
        rollno=(EditText)findViewById(R.id.rollno);
        number= (EditText)findViewById(R.id.number);
        neareststation=(EditText)findViewById(R.id.neareststation);
        db= new SQLiteHelper(this);
        users= db.getAllValues();
        team1=(Spinner)findViewById(R.id.team1);
        team2=(Spinner)findViewById(R.id.team2);
        team3=(Spinner)findViewById(R.id.team3);
        update=(Button)findViewById(R.id.update);
        ArrayAdapter<String> teams=new ArrayAdapter<String>(EditProfile.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.teams));
        teams.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        team1.setAdapter(teams);
        team2.setAdapter(teams);
        team3.setAdapter(teams);
        email.setText(users.get("email"));
        firstname.setText(users.get("name"));
        lastname.setText(users.get("name"));
        rollno.setText(users.get("rollno"));
        number.setText(users.get("phone"));
        neareststation.setText(users.get("station"));
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length=email.getText().toString().length();

                if(firstname.getText().toString().isEmpty() || lastname.getText().toString().isEmpty() || email.getText().toString().isEmpty() || number.getText().toString().isEmpty() || neareststation.getText().toString().isEmpty() || rollno.getText().toString().isEmpty())
                {
                    Toast.makeText(EditProfile.this,"Could not submit:\nOne or multiple empty fields.",Toast.LENGTH_LONG).show();
                }
                else if(length<=10 || !email.getText().toString().substring(length-10,length).matches("@gmail.com"))
                {
                    Toast.makeText(EditProfile.this,"Invalid Email:",Toast.LENGTH_LONG).show();
                }
                else if(number.getText().toString().length()!=10)
                {
                    Toast.makeText(EditProfile.this,"Invalid Number:",Toast.LENGTH_LONG).show();
                }
                else if(rollno.getText().toString().length()!=8)
                {
                    Toast.makeText(EditProfile.this,"Invalid Roll Number:",Toast.LENGTH_LONG).show();
                }
                else if(preference1.matches(preference2)|| preference2.matches(preference3) || preference3.matches(preference1))
                {
                    Toast.makeText(EditProfile.this,"Two Similar Preferences!",Toast.LENGTH_LONG).show();
                }
                else {
                  //  showConformationDialouge();
                }

            }
        });

    }

    public boolean isConnected(Context context)
    {

        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo= cm.getActiveNetworkInfo();
        if(netinfo!=null && netinfo.isConnectedOrConnecting())
        {
            android.net.NetworkInfo wifi= cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile=cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile!=null && mobile.isConnectedOrConnecting())|| (wifi!=null && wifi.isConnectedOrConnecting()))
            {
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
}
