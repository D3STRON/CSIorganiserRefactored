package com.csi.csi_organiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class NotifyActivity extends AppCompatActivity {
    Toolbar toolbar;
    TaskModel taskmodel;
    DatabaseReference notificationdata;
    ListView notifications;
    EditText message;
    Button notify;
    HashMap<String, String> obj;
    ArrayAdapter<String> arrayAdapter;
    boolean flag=false;
    ArrayList<String> messagelist;
    DatabaseReference temp;
    SQLiteHelper db;
    HashMap<String, String> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
       if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }
        else {
            obj = new HashMap<>();
            messagelist = new ArrayList<>();
            db=new SQLiteHelper(this);
            users=db.getAllValues();
            taskmodel = (TaskModel) getIntent().getSerializableExtra("taskmodel");
            notificationdata = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id).child("Notification");
            arrayAdapter = new ArrayAdapter<String>(NotifyActivity.this, android.R.layout.simple_list_item_1, messagelist);
            temp=FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id);
            setContentView(R.layout.activity_notify);
            notify = (Button) findViewById(R.id.notify);
            message = (EditText) findViewById(R.id.message);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            notifications = (ListView) findViewById(R.id.notifications);
            notifications.setAdapter(arrayAdapter);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(taskmodel.getTasktitle());
            getSupportActionBar().setSubtitle("Click here to view members or destroy task..");
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NotifyActivity.this, ViewMembersActivity.class);
                    intent.putExtra("taskmodel", taskmodel);
                    intent.putExtra("currentteam", getIntent().getStringExtra("currentteam"));
                    startActivity(intent);
                }
            });
            notify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!message.getText().toString().isEmpty()) {
                        Date currentLocalTime = Calendar.getInstance().getTime();
                        Long dat = System.currentTimeMillis();
                        DateFormat date = new SimpleDateFormat("HH:mm");
                        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
                        String localTime = date.format(currentLocalTime);
                        obj.put("Message", message.getText().toString()+"\n-"+users.get("name")+".."+localTime);
                        final DatabaseReference checker=FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id);
                        checker.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("jcnumber").getValue()!=null)
                                {
                                    String id = notificationdata.push().getKey();
                                    notificationdata.child(id).setValue(obj);
                                    message.setText("");
                                    notificationlistener();
                                    obj.clear();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       notificationlistener();

        temp.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                   if(dataSnapshot.getKey().matches("Notification"))
                   {
                       notificationlistener();
                   }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().matches("Notification"))
                {
                    notificationlistener();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getKey().matches("Id"))
                {toolbar.setTitle("Task "+taskmodel.getTasktitle()+" is inactive!");
                    toolbar.setTitleTextColor(0xFFFFFFFF);
                    message.setVisibility(View.GONE);
                    notify.setVisibility(View.GONE);
                    toolbar.setClickable(false);
                   temp.removeValue();
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void notificationlistener()
    {
        notificationdata.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        arrayAdapter.clear();
                        for(DataSnapshot fire: dataSnapshot.getChildren())
                        {
                            String S=(String)fire.child("Message").getValue();
                           arrayAdapter.add(S);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

}
