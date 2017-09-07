package com.csi.csi_organiser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Members extends AppCompatActivity {
    private EditText mReasonBox;
    private TextView mTaskDesc;
    private Button cancel;
    private Button mNoBtn;
    ListView notificationList;
    private Button mSubmitBtn;
    Toolbar toolbar;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String>  notificationstringlist;
    SQLiteHelper db;
    String currenttask="",teamtask="";
    HashMap<String ,String> users;
    ChildEventListener ce;
    DatabaseReference monitor,firetask, notificationdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        mReasonBox = (EditText) findViewById(R.id.reasonBox);
        cancel=(Button)findViewById(R.id.cancel);
        mTaskDesc = (TextView) findViewById(R.id.taskDesc);
        notificationstringlist=new ArrayList<>();

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notificationstringlist);
        notificationList=(ListView)findViewById(R.id.notificationList);
        notificationList.setAdapter(arrayAdapter);
        db = new SQLiteHelper(this);
        users =db.getAllValues();

        if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }
        else {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("TASK MANAGER");
            db = new SQLiteHelper(this);
            mNoBtn = (Button) findViewById(R.id.noBtn);
            mSubmitBtn = (Button) findViewById(R.id.submitBtn);
            mTaskDesc= (TextView)findViewById(R.id.taskDesc);
            monitor= FirebaseDatabase.getInstance().getReference("CSI Members").child(users.get("UUID"));

            mSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference(users.get("taskteam")).child(users.get("currentTask")).child("Members").child(users.get("UUID")).child("Backout Request").setValue(mReasonBox.getText().toString());
                    mReasonBox.setText("");
                    mReasonBox.setVisibility(View.GONE);
                    mSubmitBtn.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    mNoBtn.setVisibility(View.VISIBLE);
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReasonBox.setVisibility(View.GONE);
                    mSubmitBtn.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    mNoBtn.setVisibility(View.VISIBLE);
                }
            });
            mNoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReasonBox.setVisibility(View.VISIBLE);
                    mSubmitBtn.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    mNoBtn.setVisibility(View.GONE);
                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Members.this,GSignin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                db.deleteUsers();
                finish();
                return true;
            case R.id.editprofile:
                Intent intenteditprofile= new Intent(Members.this,EditProfile.class);
                startActivity(intenteditprofile);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       taskVerify();
        monitor.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
               if( dataSnapshot.getKey().matches("currenttask")){
                currenttask=dataSnapshot.getValue().toString();
               }
               else if(dataSnapshot.getKey().matches("teamtask")) {
                   teamtask = dataSnapshot.getValue().toString();
                   db.updateValues(teamtask,currenttask);
                   users=db.getAllValues();
                   if(!teamtask.isEmpty() )
                   {
                       firetask= FirebaseDatabase.getInstance().getReference(teamtask);
                       notificationList.setVisibility(View.VISIBLE);
                       mNoBtn.setVisibility(View.VISIBLE);
                     //  Toast.makeText(Members.this, arrayAdapter.getItem(1),Toast.LENGTH_SHORT).show();
                       addChildlistenerofNotifications(firetask);
                       addtaskListener(firetask,currenttask);
                   }
                   else
                   {
                       getSupportActionBar().setTitle("TASK MANAGER");
                       mTaskDesc.setText("THERE IS NO CURRENT TASK REQUEST...");
                       mNoBtn.setVisibility(View.INVISIBLE);
                       db.updateValues("","null");
                       users=db.getAllValues();
                       notificationList.setVisibility(View.INVISIBLE);
                       mReasonBox.setVisibility(View.GONE);
                       mSubmitBtn.setVisibility(View.GONE);
                       cancel.setVisibility(View.GONE);
                       arrayAdapter.clear();

                   }

               }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

       public void addtaskListener(final DatabaseReference firetask, final String k)
       {
          firetask.child(k).addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  String senderdetails = (String) dataSnapshot.child("jcrollno").getValue();
                  senderdetails=senderdetails.substring(8)+"("+users.get("taskteam").substring(6)+")";
                  mTaskDesc.setText("TASK DETAILS: " + (String) dataSnapshot.child("taskdetails").getValue() + "\n-" + senderdetails);
                  getSupportActionBar().setTitle((String) dataSnapshot.child("tasktitle").getValue());
                  mNoBtn.setVisibility(View.VISIBLE);
                  notificationList.setVisibility(View.VISIBLE);
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });
       }

       public void taskVerify()
       {
           monitor.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   db.updateValues((String) dataSnapshot.child("teamtask").getValue(),(String) dataSnapshot.child("currenttask").getValue());
                  users=db.getAllValues();
                   String s=(String) dataSnapshot.child("teamtask").getValue();
                   if(!s.isEmpty())
                   {
                       addChildlistenerofNotifications(firetask);
                   }
                   else
                   {
                       mTaskDesc.setText("THERE IS NO CURRENT TASK REQUEST...");
                       mNoBtn.setVisibility(View.INVISIBLE);
                       notificationList.setVisibility(View.INVISIBLE);
                   }
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });
       }

       public void addChildlistenerofNotifications(DatabaseReference firetask)
       {
           firetask=FirebaseDatabase.getInstance().getReference(users.get("taskteam"));
           addtaskListener(firetask,users.get("currentTask"));
           notificationdata=FirebaseDatabase.getInstance().getReference(users.get("taskteam")).child(users.get("currentTask")).child("Notification");
         if(ce != null){
             notificationdata.removeEventListener(ce);
         }
           ce= notificationdata.addChildEventListener(new ChildEventListener() {
               @Override
               public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                   arrayAdapter.add(dataSnapshot.child("Message").getValue().toString());
                   arrayAdapter.notifyDataSetChanged();
                   //////////Notiffication required hreeee!!!!!.///////////////////////////
               }

               @Override
               public void onChildChanged(DataSnapshot dataSnapshot, String s) {

               }

               @Override
               public void onChildRemoved(DataSnapshot dataSnapshot) {

               }

               @Override
               public void onChildMoved(DataSnapshot dataSnapshot, String s) {

               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });
       }
}
/*

  for(DataSnapshot fire :dataSnapshot.getChildren()) {
                     if(fire.getKey().matches(k)) {
                          String senderdetails = (String) fire.child("jcrollno").getValue();
                          senderdetails=senderdetails.substring(8)+"("+users.get("taskteam").substring(6)+")";
                          mTaskDesc.setText("TASK DETAILS: " + (String) fire.child("taskdetails").getValue() + "\n-" + senderdetails);
                          getSupportActionBar().setTitle((String) fire.child("tasktitle").getValue());
                          mNoBtn.setVisibility(View.VISIBLE);
                          notificationList.setVisibility(View.VISIBLE);
                          break;
                      }
                  }

 */