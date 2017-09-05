package com.csi.csi_organiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Members extends AppCompatActivity {
    private EditText mReasonBox;
    private TextView mTaskDesc;
    private Button mYesBtn;
    private Button mNoBtn;
    private Button mSubmitBtn;
    Toolbar toolbar;
    SQLiteHelper db;
    String currenttask="",taskteam="";
    HashMap<String ,String> users;

    DatabaseReference monitor,firetask, notificationdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        mReasonBox = (EditText) findViewById(R.id.reasonBox);
        mTaskDesc = (TextView) findViewById(R.id.taskDesc);
        mYesBtn = (Button) findViewById(R.id.yesBtn);
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
               firetask=FirebaseDatabase.getInstance().getReference(users.get("taskteam"));
                addtaskListener(firetask,users.get("currentTask"));
                notificationdata=FirebaseDatabase.getInstance().getReference(users.get("taskteam")).child(users.get("currentTask")).child("Notification");
                notificationdata.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Toast.makeText(Members.this,dataSnapshot.child("Message").getValue().toString(),Toast.LENGTH_SHORT).show();
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
                mNoBtn.setVisibility(View.VISIBLE);
                mYesBtn.setVisibility(View.VISIBLE);
            }
            else
            {
                mTaskDesc.setText("THERE IS NO CURRENT TASK REQUEST...");
                mNoBtn.setVisibility(View.INVISIBLE);
                mYesBtn.setVisibility(View.INVISIBLE);
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

                }
            });
            mNoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReasonBox.setVisibility(View.VISIBLE);
                    mSubmitBtn.setVisibility(View.VISIBLE);

                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Members.this,MainActivity.class);
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
        ChildEventListener childEventListener = monitor.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
               if(currenttask.isEmpty())
                currenttask=dataSnapshot.getValue().toString();
               else
                   taskteam=dataSnapshot.getValue().toString();

                if(!taskteam.isEmpty())
                {
                    firetask= FirebaseDatabase.getInstance().getReference(taskteam);
                    db.updateValues(taskteam,currenttask);
                    mNoBtn.setVisibility(View.VISIBLE);
                    mYesBtn.setVisibility(View.VISIBLE);
                    addtaskListener(firetask,currenttask);
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
                 mTaskDesc.setText("THERE IS A NEW TASK REQUEST: "+(String) dataSnapshot.child(k).child("tasktitle").getValue());
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });
       }
}
