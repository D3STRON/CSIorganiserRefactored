package com.csi.csi_organiser;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
    EditText firstname, lastname, email, number, rollno, neareststation,division;
    Spinner team1, team2, team3,year;
    String preference1;
    String preference2;
    String preference3;
    String Year;
    Editable Division;
    Button update, delete;
    SQLiteHelper db;
    DatabaseReference firebase;
    HashMap<String, String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        firebase = FirebaseDatabase.getInstance().getReference("CSI Members");
        email = (EditText) findViewById(R.id.email);
        email.setEnabled(false);
        rollno = (EditText) findViewById(R.id.rollno);
        rollno.setEnabled(false);
        number = (EditText) findViewById(R.id.number);
        neareststation = (EditText) findViewById(R.id.neareststation);
        division = (EditText) findViewById(R.id.division);
        year = (Spinner) findViewById(R.id.year);
        db = new SQLiteHelper(this);
        users = db.getAllValues();
        team1 = (Spinner) findViewById(R.id.team1);
        team2 = (Spinner) findViewById(R.id.team2);
        team3 = (Spinner) findViewById(R.id.team3);
        update = (Button) findViewById(R.id.update);
        delete = (Button) findViewById(R.id.deleteaccount);
        ArrayAdapter<String> years=new ArrayAdapter<String>(EditProfile.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.year));
        years.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(years);
        ArrayAdapter<String> teams = new ArrayAdapter<>(EditProfile.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.teams));
        teams.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        team1.setAdapter(teams);
        team2.setAdapter(teams);
        team3.setAdapter(teams);
        int a = users.get("name").indexOf(" ");
        firstname.setText(users.get("name").substring(0, a));
        lastname.setText(users.get("name").substring(a + 1));
        email.setText(users.get("email"));
        rollno.setText(users.get("rollno"));
        number.setText(users.get("phone").toString().substring(0,10));
        neareststation.setText(users.get("station"));
        division.setText(users.get("phone").toString().substring(13));
        year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Year = year.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
        team1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preference1 = team1.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        team2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preference2 = team2.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        team3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preference3 = team3.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = email.getText().toString().length();

                if (firstname.getText().toString().isEmpty() || lastname.getText().toString().isEmpty() || email.getText().toString().isEmpty() || number.getText().toString().isEmpty() || neareststation.getText().toString().isEmpty() || rollno.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfile.this, "Could not submit:\nOne or multiple empty fields.", Toast.LENGTH_LONG).show();
                } else if (number.getText().toString().length() != 10) {
                    Toast.makeText(EditProfile.this, "Invalid Number:", Toast.LENGTH_LONG).show();
                }
                else if(division.getText().toString()==""){
                    Toast.makeText(EditProfile.this,"Enter Division",Toast.LENGTH_LONG).show();
                }
                else if(Year == ""){
                    Toast.makeText(EditProfile.this,"Enter Year",Toast.LENGTH_LONG).show();
                }
                else if (preference1.matches(preference2) || preference2.matches(preference3) || preference3.matches(preference1)) {
                    Toast.makeText(EditProfile.this, "Two Similar Preferences!", Toast.LENGTH_LONG).show();
                } else {
                    showConformationDialouge();
                }

            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Id = users.get("UUID");
                firebase.child(Id).removeValue();
                boolean connection = isConnected(EditProfile.this);
                if (!connection) {
                    Toast.makeText(EditProfile.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                } else if (users.get("priority").matches("0") && connection) {
                    db.deleteUsers();
                    Intent intent = new Intent(EditProfile.this, Members.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("EXIT", true);
                    startActivity(intent);
                }
                else if(users.get("priority").matches("1") && connection)
                {
                    db.deleteUsers();
                    Intent intent = new Intent(EditProfile.this,CoreActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("EXIT", true);
                    startActivity(intent);
                }else {
                    db.deleteUsers();
                    Intent intent = new Intent(EditProfile.this, JcActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("EXIT", true);
                    startActivity(intent);
                }

                finish();
            }
        });


    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) {
                return true;
            } else
                return false;
        } else
            return false;
    }


    public void showConformationDialouge() {
        final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutinflater = getLayoutInflater();
        final View confirmationview = layoutinflater.inflate(R.layout.conformation, null);
        dialogbuilder.setView(confirmationview);
        dialogbuilder.setTitle("CONFORMATION");
        final Button yes, no;
        yes = (Button) confirmationview.findViewById(R.id.yes);
        no = (Button) confirmationview.findViewById(R.id.no);
        final AlertDialog alertDialog = dialogbuilder.create();
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

                Model model = new Model();
                model.setValue(firstname.getText().toString().replaceAll(" ", "").toLowerCase() + " " + lastname.getText().toString().replaceAll(" ", "").toLowerCase(),
                        email.getText().toString(), number.getText().toString()+Year+" "+division.getText().toString().replaceAll(" ",""), neareststation.getText().toString(),
                        rollno.getText().toString().toUpperCase(), preference1, preference2, preference3 ,db.getAllValues().get("priority"),db.getAllValues().get("currentTask"),db.getAllValues().get("taskteam"));

                boolean result = isConnected(EditProfile.this);

                if (!result) {
                    Toast.makeText(EditProfile.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
                else if (result) {
                    String Id = users.get("UUID");
                    /////////////////
                    switch(Integer.parseInt(users.get("priority"))){
                        case (0):
                            break;
                        case(2):
                            model.setPreference1(team1.getItemAtPosition(1).toString());
                            break;
                        case(3):
                            model.setPreference1(team1.getItemAtPosition(2).toString());
                            break;
                        case(4):
                            model.setPreference1(team1.getItemAtPosition(3).toString());
                            break;
                        case(5):
                            model.setPreference1(team1.getItemAtPosition(4).toString());
                            break;
                    }
                    ////////////////
                    firebase.child(Id).setValue(model);
                    db.updateValues(model.getName(),model.getNeareststation(),model.getPreference1(), model.getPreference2(), model.getPreference3(), model.getNumber());
                    alertDialog.dismiss();
                    goBack();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        goBack();
    }
    public void goBack()
    {
        Intent intent;
        if(users.get("priority").matches("0"))
        {
            intent= new Intent(EditProfile.this,Members.class);
        }
        else if(users.get("priority").matches("1"))
        {
            intent= new Intent(EditProfile.this,CoreActivity.class);
        }
        else
        {
            intent= new Intent(EditProfile.this,JcActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
/*



 */