package com.csi.csi_organiser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static java.security.AccessController.getContext;

public class ViewMembersActivity extends AppCompatActivity {
    Toolbar toolbar;
    TaskModel taskmodel;
    ListView presentmembers;
    ArrayList<String> presentmemstring, idstring;
    ArrayAdapter<String> arrayAdapter;
    DatabaseReference firemembers,firecsi;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);
        presentmemstring = new ArrayList<>();
        idstring = new ArrayList<>();
        taskmodel = (TaskModel) getIntent().getSerializableExtra("taskmodel");
        firemembers = FirebaseDatabase.getInstance().getReference("Tasks-Technical").child(taskmodel.Id).child("Members");
        firecsi=FirebaseDatabase.getInstance().getReference("CSI Members");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(taskmodel.getTasktitle());
        getSupportActionBar().setSubtitle("Current Members of this task...");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, presentmemstring);
        presentmembers = (ListView) findViewById(R.id.presentmembers);
        fetchpresentmembers();
        presentmembers.setAdapter(arrayAdapter);

        presentmembers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                firecsi.child(idstring.get(position)).child("currenttask").setValue("null");
                firecsi.child(idstring.get(position)).child("teamtask").setValue("");
                firemembers.child(idstring.get(position)).removeValue();
                fetchpresentmembers();
                return true;
            }
        });
    }
    public void fetchpresentmembers()
    {
        arrayAdapter.clear();
          firemembers.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  for(DataSnapshot fire :dataSnapshot.getChildren())
                  {
                      arrayAdapter.add((String) fire.child("Name").getValue()+" "+(String) fire.child("Roll No").getValue());
                      idstring.add(fire.getKey());
                  }
                  arrayAdapter.notifyDataSetChanged();
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        childEventListener=firemembers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                 fetchpresentmembers();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firemembers.removeEventListener(childEventListener);
    }
}