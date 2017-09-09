package com.csi.csi_organiser;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DateActivity extends AppCompatActivity {
    ListView list;
    ArrayAdapter<String> dateAdapter, membersAdapter;
    Button backtodates,emailnames;
    ArrayList<String> datestring, membersstring,membersstring2;
    DatabaseReference dates;
    ValueEventListener ve;
    Toolbar toolbar;
    SQLiteHelper db;
    String d;
    HashMap<String,String> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        datestring = new ArrayList<>();
        db=new SQLiteHelper(this);
        users=db.getAllValues();
        membersstring = new ArrayList<>();
        membersstring2 = new ArrayList<>();
        backtodates= (Button)findViewById(R.id.backtodates);
        backtodates.setVisibility(View.GONE);
        emailnames= (Button)findViewById(R.id.emailnames);
        emailnames.setVisibility(View.GONE);
        dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datestring);
        membersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, membersstring);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(dateAdapter);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attended Dates");
        getSupportActionBar().setSubtitle("Tap on the dates to view the members..");
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.getAdapter()==dateAdapter)
                {
                  //Toast.makeText(DateActivity.this,"Here",Toast.LENGTH_SHORT).show();
                  fetchmembers(dateAdapter.getItem(position));
                    list.setAdapter(membersAdapter);
                  backtodates.setVisibility(View.VISIBLE);
                    emailnames.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("Members");
                    getSupportActionBar().setSubtitle("members attended task on date "+dateAdapter.getItem(position));
                    d=dateAdapter.getItem(position);
                }
            }
        });

        backtodates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setAdapter(dateAdapter);
                backtodates.setVisibility(View.GONE);
                emailnames.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Attendence Dates");
                getSupportActionBar().setSubtitle("Tap on the dates to view the members..");
                d="";
            }
        });
        emailnames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(Intent.ACTION_SEND);
                String[] to={users.get("email")};
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, to);
                intent.putExtra(Intent.EXTRA_SUBJECT,d);
                intent.putExtra(Intent.EXTRA_TEXT,membersstring2.toString());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "Send email"));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        dates = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child("Days");
        if(ve!=null)
        {
            dates.removeEventListener(ve);
        }
        ve = dates.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dateAdapter.clear();
                for (DataSnapshot fire : dataSnapshot.getChildren()) {
                    dateAdapter.add(fire.getKey());
                }
                dateAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public  void  fetchmembers(String date)
    {
      dates.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              membersAdapter.clear();
              membersstring2.clear();
              for (DataSnapshot fire: dataSnapshot.getChildren())
              {
                 String s=(String) fire.child("Details").getValue();
                  membersAdapter.add(s);
                  membersstring2.add("\n"+s);
              }
              membersAdapter.notifyDataSetChanged();
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });
    }
}
