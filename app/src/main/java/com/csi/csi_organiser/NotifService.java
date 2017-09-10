package com.csi.csi_organiser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class NotifService extends Service {

    String currenttask = "",teamtask = "";
    NotificationManager manager;
    SQLiteHelper db;
    DatabaseReference monitor, firetask, notificationdata;
    private static HashMap<String ,String> users;
    public NotifService() {
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new SQLiteHelper(this);
        users = db.getAllValues();
        if(isNetworkAvailable()) {
            if(users.isEmpty())
            {}
           else if(!users.get("taskteam").isEmpty() && !users.get("currentTask").matches("null"))
            {
                firetask=FirebaseDatabase.getInstance().getReference(users.get("taskteam"));
                firetask.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot fire: dataSnapshot.getChildren())
                            if(fire.getKey().matches(users.get("currentTask"))){
                        Log.e("SERVICE","Notified about new task!");
                        notifyThem("New Task",(String) dataSnapshot.child(users.get("currentTask")).child("tasktitle").getValue());
                         break;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("SERVICE",databaseError.getMessage());
                    }
                });
                notificationdata=FirebaseDatabase.getInstance().getReference(users.get("taskteam"))
                        .child(users.get("currentTask")).child("Notification");
                notificationdata.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.e("SERVICE","Notified about new message!");
                        notifyThem("New message",dataSnapshot.child("Message").getValue().toString());
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
        else Toast.makeText(getApplicationContext(),"No net connection",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }


    private void notifyThem(String title, String message){
        manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent ni = new Intent(getApplicationContext(),GSignin.class);
        ni.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,ni,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.photo)
                .setVibrate(new long[]{})
                .setWhen(System.currentTimeMillis());
        manager.notify(545, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("no"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
   /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new SQLiteHelper(getApplicationContext());
        users = db.getAllValues();
        monitor = FirebaseDatabase.getInstance().getReference("CSI Members").child("UUID");
        monitor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                db.updateValues((String) dataSnapshot.child("teamtask").getValue(),(String) dataSnapshot.child("currenttask")
                        .getValue());
                users = db.getAllValues();
                try {
                    String s = dataSnapshot.child("teamtask").getValue().toString();
                    if(!s.isEmpty()){
                        firetask = FirebaseDatabase.getInstance().getReference(users.get("taskteam"));
                        firetask.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                notifyThem("New Task",dataSnapshot.getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                        notificationdata = FirebaseDatabase.getInstance().getReference(users.get("taskteam"))
                                .child(users.get("currentTask")).child("Notification");
                        notificationdata.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                notifyThem("New Message",dataSnapshot.child("Message").getValue().toString());
                            }

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }catch (Exception r){
                    Toast.makeText(getApplicationContext(),r.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
        monitor.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (currenttask.isEmpty() && dataSnapshot.getKey().matches("currenttask"))
                    currenttask = dataSnapshot.getValue().toString();
                else if (dataSnapshot.getKey().matches("teamtask")) {
                    teamtask = dataSnapshot.getValue().toString();
                    db.updateValues(teamtask, currenttask);
                    users = db.getAllValues();
                    if (!teamtask.isEmpty()) {
                        firetask = FirebaseDatabase.getInstance().getReference(teamtask);
                        notifyThem("New Task","You've got a new task!");
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
        return START_STICKY;
    }
*/