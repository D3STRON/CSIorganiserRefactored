package com.csi.csi_organiser;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class HomeActivity extends AppCompatActivity {
    EditText firstname, lastname,email, number,rollno, neareststation;
    Spinner team1, team2, team3;
    String preference1, preference2, preference3;
    Button submit;
    SQLiteHelper db;
    DatabaseReference firebase,firebaserole;
    ArrayList<Model> memlist;
    ArrayList<Model2> rolelist;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ///
        memlist= new ArrayList<>();
        rolelist=new ArrayList<>();
        ///
        if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }
        firstname= (EditText)findViewById(R.id.firstname);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SIGN UP");
        db = new SQLiteHelper(this);
        lastname= (EditText)findViewById(R.id.lastname);
        email= (EditText)findViewById(R.id.email);
        rollno=(EditText)findViewById(R.id.rollno);
        number= (EditText)findViewById(R.id.number);
        firebase=FirebaseDatabase.getInstance().getReference("CSI Members");
        firebaserole= FirebaseDatabase.getInstance().getReference("Roles");
        neareststation=(EditText)findViewById(R.id.neareststation);
        team1=(Spinner)findViewById(R.id.team1);
        team2=(Spinner)findViewById(R.id.team2);
        team3=(Spinner)findViewById(R.id.team3);
        submit=(Button)findViewById(R.id.submit);
        ArrayAdapter<String> teams=new ArrayAdapter<String>(HomeActivity.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.teams));
        teams.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        team1.setAdapter(teams);
        team2.setAdapter(teams);
        team3.setAdapter(teams);
        HashMap<String,String> users=db.getAllValues();

       ////heree put it
        team1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preference1=team1.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        team2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preference2=team2.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        team3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preference3=team3.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        email.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(email.getText().toString().isEmpty())
                    email.setText("@gmail.com");

                return false;
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              int length=email.getText().toString().length();

                if(firstname.getText().toString().isEmpty() || lastname.getText().toString().isEmpty() || email.getText().toString().isEmpty() || number.getText().toString().isEmpty() || neareststation.getText().toString().isEmpty() || rollno.getText().toString().isEmpty())
                {
                    Toast.makeText(HomeActivity.this,"Could not submit:\nOne or multiple empty fields.",Toast.LENGTH_LONG).show();
                }
                else if(length<=10 || !email.getText().toString().substring(length-10,length).matches("@gmail.com"))
                {
                    Toast.makeText(HomeActivity.this,"Invalid Email:",Toast.LENGTH_LONG).show();
                }
                else if(number.getText().toString().length()!=10)
                {
                    Toast.makeText(HomeActivity.this,"Invalid Number:",Toast.LENGTH_LONG).show();
                }
                else if(rollno.getText().toString().length()!=8)
                {
                    Toast.makeText(HomeActivity.this,"Invalid Roll Number:",Toast.LENGTH_LONG).show();
                }
                else if(preference1.matches(preference2)|| preference2.matches(preference3) || preference3.matches(preference1))
                {
                    Toast.makeText(HomeActivity.this,"Two Similar Preferences!",Toast.LENGTH_LONG).show();
                }
                else {
                    showConformationDialouge();
                }

            }
        });

    }

    public void showConformationDialouge()
    {
        final  AlertDialog.Builder dialogbuilder= new AlertDialog.Builder(this);
        LayoutInflater layoutinflater= getLayoutInflater();
        final View confirmationview= layoutinflater.inflate(R.layout.conformation,null);
        dialogbuilder.setView(confirmationview);
        dialogbuilder.setTitle("CONFORMATION");
        final Button yes, no;
        yes=(Button)confirmationview.findViewById(R.id.yes);
        no=(Button)confirmationview.findViewById(R.id.no);
        final AlertDialog alertDialog= dialogbuilder.create();
        alertDialog.show();
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }

        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Model model=new Model();
                model.setValue(firstname.getText().toString()+" "+lastname.getText().toString(),email.getText().toString(),number.getText().toString(),neareststation.getText().toString(),rollno.getText().toString().toUpperCase(),preference1,preference2,preference3);

                boolean result=isConnected(HomeActivity.this);

                if(!memlist.isEmpty())
                {for(int i=0;i<memlist.size();i++)
                {
                    if(model.getRollno().matches(memlist.get(i).getRollno()))
                    {
                        Toast.makeText(HomeActivity.this,"This Entry Already Exists!",Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                        result=false;
                    }
                }}
                if(!result)
                {
                    Toast.makeText(HomeActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
                else if(rolelist.isEmpty())
                {
                    Toast.makeText(HomeActivity.this, "Connecting To Cloud! Please Wait...", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
                else if(result)
                {

                    for(int i=0;i<rolelist.size();i++) {
                        if (model.getRollno().matches(rolelist.get(i).getRollno())) {

                            model.setPriority(rolelist.get(i).getPriority());
                            Toast.makeText(HomeActivity.this, "You are a committee member.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    db.addInfo(model.getCurrenttask(), model.getName(),model.getEmail(),
                            model.getNumber(),model.getNeareststation(),model.getNumberoftasks(),
                            model.getPreference1(),model.getPreference2(),model.getPreference3(),
                            model.getPriority(),model.getRollno());
                    String Id= firebase.push().getKey();
                    firebase.child(Id).setValue(model);

                    if(model.getPriority().matches("1"))
                    {
                        ////put intent to core member activity
                        //Intent intent= new Intent(HomeActivity.this,CoreActivity.class);
                        //startActivity(intent);

                    }
                    else if(model.getPriority().matches("2"))
                    {
                        ///put intent to jc activity
                        Intent intent= new Intent(HomeActivity.this,JcActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        //put intent to intent to normal member activity
                        Intent intent =new Intent(HomeActivity.this,Members.class);
                        startActivity(intent);
                        finish();
                    }
                    memlist.clear();
                   rolelist.clear();
                    alertDialog.dismiss();

                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memlist.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    Model model= fire.getValue(Model.class);
                    memlist.add(model);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //////////////////////
        firebaserole.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rolelist.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    Model2 model2= fire.getValue(Model2.class);
                    rolelist.add(model2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
   //////////////////

    @Override
    public void onBackPressed() {
        memlist.clear();
        rolelist.clear();
        super.onBackPressed();
    }
}



/*

Intent intent = new Intent(Intent.ACTION_SEND);
                        String[] to = {email.getText().toString()};
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, to);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Welcome to Techfest!- New Details");
                        intent.putExtra(Intent.EXTRA_TEXT, "Hello your event is:"+event+"\nYou event details are...");
                        intent.setType("text/plain");
                        startActivity(Intent.createChooser(intent, "Send email"));
/////////////////////////////////
 Model2 model2= new Model2();
              model2.setValues(rollno.getText().toString(),number.getText().toString());
              String Id= firebaserole.push().getKey();
              firebaserole.child(Id).setValue(model2);
              /////////////////////////////////////
 Model model=new Model();
              model.setValue(firstname.getText().toString()+" "+lastname.getText().toString(),email.getText().toString(),number.getText().toString(),neareststation.getText().toString(),rollno.getText().toString().toUpperCase(),preference1,preference2,preference3);
              String Id= firebase.push().getKey();
              firebase.child(Id).setValue(model);
///////////////////////////////////////

                if(!preference3.isEmpty())
                    Toast.makeText(HomeActivity.this,preference3,Toast.LENGTH_SHORT).show();

 //////////////////////////////////////

  else if(!users.isEmpty())
        {
            Toast.makeText(HomeActivity.this,"There is a current User!",Toast.LENGTH_LONG).show();
            if(users.get("priority").matches("1"))
            {
                //Intent intent= new Intent(HomeActivity.this,CoreActivity.class);
                //startActivity(intent);

            }
            else if(users.get("priority").matches("2"))
            {
                Intent intent= new Intent(HomeActivity.this,JcActivity.class);
                startActivity(intent);
            }
            else
            {

                Intent intent =new Intent(HomeActivity.this,Members.class);
                startActivity(intent);
            }
        }

*/