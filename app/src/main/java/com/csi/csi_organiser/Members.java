package com.csi.csi_organiser;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Calendar;
import java.util.HashMap;

public class Members extends AppCompatActivity {
    private static boolean isNotifying = false;
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
    ValueEventListener ve;
    DatabaseReference monitor,firetask, notificationdata;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(this,NotifService.class));
    }


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
        users = db.getAllValues();

        if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }
        else {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("TASK MANAGER");
            db = new SQLiteHelper(this);

            scheduleNotifics();


            if(!isMyServiceRunning(NotifService.class))
                startService(new Intent(this,NotifService.class));

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
                    Toast.makeText(Members.this,"Your Request Has Been Sent!",Toast.LENGTH_LONG).show();
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

    private void scheduleNotifics(){
        if(!isNotifying) {
            Calendar calendar10am = Calendar.getInstance();
            calendar10am.set(Calendar.HOUR_OF_DAY, 10);
            calendar10am.set(Calendar.MINUTE, 0);
            calendar10am.set(Calendar.SECOND, 0);

            Calendar calender4pm = Calendar.getInstance();
            calender4pm.set(Calendar.HOUR_OF_DAY, 16);
            calender4pm.set(Calendar.MINUTE, 0);
            calender4pm.set(Calendar.SECOND, 0);

            Calendar calender10pm = Calendar.getInstance();
            calender10pm.set(Calendar.HOUR_OF_DAY, 22);
            calender10pm.set(Calendar.MINUTE, 0);
            calender10pm.set(Calendar.SECOND, 0);

            Intent intent = new Intent(getApplicationContext(), NotiRec.class);
            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.cancel(pi);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar10am.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calender4pm.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calender10pm.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            isNotifying = true;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Members.this,GSignin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        monitor.removeEventListener(ce);
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
                monitor.removeEventListener(ce);
                finish();
                return true;
            case R.id.editprofile:
                Intent intenteditprofile= new Intent(Members.this,EditProfile.class);
                monitor.removeEventListener(ce);
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
        ce= monitor.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().matches("currenttask"))
                {
                    currenttask=(String) dataSnapshot.getValue();
                }
                else if(dataSnapshot.getKey().matches("teamtask"))
                {
                    teamtask=(String) dataSnapshot.getValue();
                    //Toast.makeText(Members.this,(String)dataSnapshot.getValue(),Toast.LENGTH_LONG).show();
                    db.updateValues(teamtask,currenttask);
                    users=db.getAllValues();
                    if(!teamtask.isEmpty())
                    {
                        firetask=FirebaseDatabase.getInstance().getReference(users.get("taskteam")).child(users.get("currentTask"));
                        //////////////
                        setPageDetails();
                        //////////////
                        addTaskListener();
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

    public void taskVerify()
    {
        monitor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s=(String) dataSnapshot.child("teamtask").getValue();
                if(!s.isEmpty())
                {
                    db.updateValues((String)dataSnapshot.child("teamtask").getValue(),(String) dataSnapshot.child("currenttask").getValue());
                    users=db.getAllValues();
                    firetask=FirebaseDatabase.getInstance().getReference(users.get("taskteam")).child(users.get("currentTask"));
                    //////////////
                    setPageDetails();
                    //////////////
                    addTaskListener();
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void addTaskListener()
    {
        updateNotification();
        firetask.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().matches("Notification"))
                {
                    updateNotification();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().matches("Notification"))
                {
                    updateNotification();
                }
                else if (dataSnapshot.getKey().matches("tasktitle") || dataSnapshot.getKey().matches("tasksubtitle") || dataSnapshot.getKey().matches("taskdetails"))
                {
                    //////////////
                    setPageDetails();
                    //////////////
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
    public void updateNotification()
    {
        firetask.child("Notification").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayAdapter.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    arrayAdapter.add((String) fire.child("Message").getValue());
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setPageDetails()
    {
        firetask.addListenerForSingleValueEvent(new ValueEventListener() {
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

}
/*
 */