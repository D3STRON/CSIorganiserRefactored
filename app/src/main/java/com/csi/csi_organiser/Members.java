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
            if(!users.get("taskteam").isEmpty())
            {
               addChildlistenerofNotifications(ce,firetask);
            }
            else
            {
                mTaskDesc.setText("THERE IS NO CURRENT TASK REQUEST...");
                mNoBtn.setVisibility(View.INVISIBLE);
                notificationList.setVisibility(View.INVISIBLE);
            }
       /* mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTaskDesc.setText(dataSnapshot.getValue().toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/
            mSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReasonBox.setVisibility(View.GONE);
                    mSubmitBtn.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    mNoBtn.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String[] to = {"9769084086"};
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, to);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "To Back Out From task :"+toolbar.getTitle());
                   intent.putExtra(Intent.EXTRA_TEXT, mReasonBox.getText().toString());
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, "Send email"));
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
        monitor.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
               if(currenttask.isEmpty() && dataSnapshot.getKey().matches("currenttask"))
                currenttask=dataSnapshot.getValue().toString();
               else if(dataSnapshot.getKey().matches("teamtask")) {
                   teamtask = dataSnapshot.getValue().toString();
                   db.updateValues(teamtask,currenttask);
                   users=db.getAllValues();
                   if(!teamtask.isEmpty())
                   {
                       firetask= FirebaseDatabase.getInstance().getReference(teamtask);
                       Toast.makeText(Members.this,users.get("taskteam"),Toast.LENGTH_SHORT).show();
                       notificationList.setVisibility(View.VISIBLE);
                       mNoBtn.setVisibility(View.VISIBLE);
                       addtaskListener(firetask,currenttask);
                       addChildlistenerofNotifications(ce,firetask);
                       /////Notification required here!!!!////////////////////
                   }
                   else
                   {
                       getSupportActionBar().setTitle("TASK MANAGER");
                       mTaskDesc.setText("THERE IS NO CURRENT TASK REQUEST...");
                       mNoBtn.setVisibility(View.INVISIBLE);
                       notificationList.setVisibility(View.INVISIBLE);
                       mReasonBox.setVisibility(View.GONE);
                       mSubmitBtn.setVisibility(View.GONE);
                       cancel.setVisibility(View.GONE);
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
          firetask.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  String senderdetails= (String) dataSnapshot.child(k).child("jcrollno").getValue();
                  senderdetails=senderdetails.substring(8)+"("+users.get("taskteam").substring(6)+")";
                  mTaskDesc.setText("TASK DETAILS: "+(String) dataSnapshot.child(k).child("taskdetails").getValue()+"\n-"+senderdetails);
                  getSupportActionBar().setTitle((String) dataSnapshot.child(k).child("tasktitle").getValue());
                  mNoBtn.setVisibility(View.VISIBLE);
                  notificationList.setVisibility(View.VISIBLE);

              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });
       }

       public void addChildlistenerofNotifications(ChildEventListener ce,DatabaseReference firetask)
       {
           firetask=FirebaseDatabase.getInstance().getReference(users.get("taskteam"));
           addtaskListener(firetask,users.get("currentTask"));
           notificationdata=FirebaseDatabase.getInstance().getReference(users.get("taskteam")).child(users.get("currentTask")).child("Notification");
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
 */