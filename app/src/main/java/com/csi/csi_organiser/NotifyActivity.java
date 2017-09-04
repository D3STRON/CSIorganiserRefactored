package com.csi.csi_organiser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NotifyActivity extends AppCompatActivity {
    Toolbar toolbar;
    TaskModel taskmodel;
    DatabaseReference notificationdata;
    ListView notifications;
    EditText message;
    Button notify;
    HashMap<String, String> obj;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> messagelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        obj= new HashMap<>();
        messagelist=new ArrayList<>();
        taskmodel= (TaskModel) getIntent().getSerializableExtra("taskmodel");
        notificationdata= FirebaseDatabase.getInstance().getReference("Tasks-Technical").child(taskmodel.Id).child("Notification");
        arrayAdapter= new ArrayAdapter<String>(NotifyActivity.this, android.R.layout.simple_list_item_1, messagelist);
        setContentView(R.layout.activity_notify);
        notify=(Button)findViewById(R.id.notify);
        message=(EditText)findViewById(R.id.message);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        notifications=(ListView)findViewById(R.id.notifications);
        notifications.setAdapter(arrayAdapter);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(taskmodel.getTasktitle());
        getSupportActionBar().setSubtitle("Click here to view members..");
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(NotifyActivity.this,ViewMembersActivity.class);
                intent.putExtra("taskmodel",taskmodel);
                startActivity(intent);
            }
        });
        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!message.getText().toString().isEmpty())
                {
                    obj.put("Message",message.getText().toString());
                    String id= notificationdata.push().getKey();
                    notificationdata.child(id).setValue(obj);
                    message.setText("");
                    notificationlistener();
                    obj.clear();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
       notificationlistener();
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
