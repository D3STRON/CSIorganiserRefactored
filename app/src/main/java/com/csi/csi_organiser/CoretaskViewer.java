package com.csi.csi_organiser;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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

public class CoretaskViewer extends AppCompatActivity {
  ListView tasklist;
  Toolbar toolbar;
    ChildEventListener tasklistener;
    ArrayAdapter<String> taskAdapter, memberAdapter;
    ArrayList<String> taskString, memberString;
    ArrayList<TaskModel> taskModels;
    String taskid;
    DatabaseReference firetask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coretask_viewer);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        tasklist=(ListView)findViewById(R.id.tasklist);
        setSupportActionBar(toolbar);
        taskString= new ArrayList<>();
        memberString= new ArrayList<>();
        taskModels= new ArrayList<>();
        memberAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberString);
        taskAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, taskString);
        tasklist.setAdapter(taskAdapter);
        firetask= FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam"));
        getSupportActionBar().setTitle(getIntent().getStringExtra("currentteam").substring(6).toUpperCase()+" TEAM");
        tasklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               taskid=taskModels.get(position).Id;
                fetchMembers();
                Toast.makeText(CoretaskViewer.this,taskid,Toast.LENGTH_SHORT).show();
                showMemberDialog(taskModels.get(position).getTasktitle());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(tasklistener!=null)
        {
            Toast.makeText(CoretaskViewer.this,"Here",Toast.LENGTH_SHORT).show();
            firetask.removeEventListener(tasklistener);
        }
        tasklistener= firetask.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 fetchTask();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchTask();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchTask();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void fetchTask()
    {
        firetask.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskAdapter.clear();
                taskModels.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren()) {
                    if(!fire.getKey().matches("Days")){
                    TaskModel taskModel = new TaskModel();
                    taskModel=fire.getValue(TaskModel.class);
                    taskModel.setMembercount((int) fire.child("Members").getChildrenCount());
                    taskAdapter.add("\nTask title: " + taskModel.tasktitle + "\nTask description: " + taskModel.taskdetails + "\nAt: " + taskModel.getTime()+ "\nMembers: " + taskModel.getMembercount());
                    taskModels.add(taskModel);}
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
            firetask.removeEventListener(tasklistener);
    }
    public void showMemberDialog(String tasktitle)
    {
        final AlertDialog.Builder dialogbuilder2 = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View createtaskview2 = layoutInflater.inflate(R.layout.core_member_view, null);
        dialogbuilder2.setView(createtaskview2);
        dialogbuilder2.setTitle("MEMBERS OF TASK: "+tasktitle);
        final AlertDialog createtaskdialog2 = dialogbuilder2.create();
        final ListView memlist;
        memlist = (ListView) createtaskview2.findViewById(R.id.memlist);
        final Button cancel=(Button)createtaskview2.findViewById(R.id.cancel);
        memlist.setAdapter(memberAdapter);
        createtaskdialog2.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createtaskdialog2.dismiss();
            }
        });
    }
    public void fetchMembers(){
        firetask.child(taskid).child("Members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memberAdapter.clear();
                for (DataSnapshot fire : dataSnapshot.getChildren()) {

                    String reason = (String) fire.child("Backout Request").getValue();
                    String attended = (String) fire.child("Attended").getValue();
                    if(fire.child("Backout Request").getValue()==null || fire.child("Attended").getValue()==null)
                    {
                        memberAdapter.add("Name: " + fire.child("Name").getValue() + "\nIs ready for the task.");
                    }
                    else if (reason.matches("") && attended.matches("")) {
                        memberAdapter.add("Name: " + fire.child("Name").getValue() + "\nIs ready for the task.");

                    } else if (attended.matches("") && !reason.isEmpty()) {
                        memberAdapter.add("Name: " + fire.child("Name").getValue() + "\nBack out request: " + fire.child("Backout Request").getValue());
                    } else if (attended.matches("yes")) {
                        memberAdapter.add("Name: " + fire.child("Name").getValue() + "\nAttended the task");
                    }
                }

                memberAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
