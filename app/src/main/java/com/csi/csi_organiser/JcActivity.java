package com.csi.csi_organiser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class JcActivity extends AppCompatActivity {
    Button createtask,exit;
    ListView tasklist;
    //    ArrayList<TaskModel> tasks;
    //  ArrayList<Model> members;
    ArrayList<TaskModel> tasks;
    ArrayList<Model> members,mempref2,mempref3,memnopref;
    TextView welcome;
    Toolbar toolbar;
    ArrayList<String> tasksstring,memberstring,colpref3,colpref2,colnopref;
    HashMap<String ,String> users;
    DatabaseReference firebasetask,firebasemembers,temp;
    SQLiteHelper db;
    String taskid,searchedmember="",AddId,AddName,AddRollNo,tasktitle, currentteam,searchedname,searchedrollno;
    ArrayAdapter<String> arrayAdapter,arrayAdaptermembers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jc);
        tasks= new ArrayList<>();
        tasksstring= new ArrayList<>();
        members= new ArrayList<>();
        colpref2=new ArrayList<>();
        colpref3=new ArrayList<>();
        mempref2=new ArrayList<>();
        mempref3=new ArrayList<>();
        memnopref=new ArrayList<>();
        colnopref=new ArrayList<>();
        memberstring=new ArrayList<>();
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TASK MANAGER");

        db = new SQLiteHelper(this);
        users =db.getAllValues();
        if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }
        else {
            createtask = (Button) findViewById(R.id.createtask);
            tasklist = (ListView) findViewById(R.id.tasklist);
            exit = (Button) findViewById(R.id.exit);
            welcome = (TextView) findViewById(R.id.welcome);
            welcome.setText("WELCOME " + users.get("name").toUpperCase());

           switch(Integer.parseInt(users.get("priority"))){
               case(2):
                   firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-Technical");
                   currentteam="Tasks-Technical";
                       break;
               case(3):
                   firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-Creative");
                   currentteam="Tasks-Creative";
                   break;
               case(4):
                   firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-GOT");
                   currentteam="Tasks-GOT";
                   break;
               case(5):
                   firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-Publicity");
                   currentteam="Tasks-Publicity";
                   break;
                   }
            firebasemembers = FirebaseDatabase.getInstance().getReference("CSI Members");
            temp= FirebaseDatabase.getInstance().getReference("CSI Members").child(users.get("UUID"));


            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasksstring);
            arrayAdaptermembers = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberstring);
            tasklist.setAdapter(arrayAdapter);
            createtask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConnected(JcActivity.this)) {
                        showCreateTaskDialog();
                    } else {
                        Toast.makeText(JcActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tasklist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    taskid = tasks.get(position).Id;
                    tasktitle= tasks.get(position).getTasktitle();
                    Toast.makeText(JcActivity.this,taskid, Toast.LENGTH_SHORT).show();
                    showEditTaskDialog(taskid);
                    return true;
                }
            });

            tasklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(JcActivity.this, NotifyActivity.class);
                    intent.putExtra("taskmodel", tasks.get(position));
                    startActivity(intent);
                }
            });
            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(JcActivity.this, GSignin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("EXIT", true);
                    startActivity(intent);
                }
            });
        }
        ///////////////////////////// Important : Here tasks has the list of Tasks and members Has the list of members//////////////////////////////////////
    }
    public void showCreateTaskDialog()
    {
        final AlertDialog.Builder dialogbuilder= new AlertDialog.Builder(this);
        LayoutInflater layoutInflater= getLayoutInflater();
        final View createtaskview = layoutInflater.inflate(R.layout.taskcreate,null);
        dialogbuilder.setView(createtaskview);
        dialogbuilder.setTitle("CREATE TASK");
        final EditText tasktitle, tasksubtitle,taskdetails;
        final Button create,cancel;
        tasktitle=(EditText)createtaskview.findViewById(R.id.tasktitle);
        tasksubtitle=(EditText)createtaskview.findViewById(R.id.tasksubtitle);
        taskdetails=(EditText)createtaskview.findViewById(R.id.taskdetails);
        create=(Button)createtaskview.findViewById(R.id.create);
        cancel=(Button)createtaskview.findViewById(R.id.cancel);
        final AlertDialog createtaskdialog=dialogbuilder.create();
        createtaskdialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createtaskdialog.dismiss();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskModel taskModel= new TaskModel();

                boolean connection=isConnected(JcActivity.this);
                if(connection) {
                    String Id = firebasetask.push().getKey();
                    Date currentLocalTime = Calendar.getInstance().getTime();
                    Long dat = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
                    String datestring = sdf.format(dat);
                    DateFormat date = new SimpleDateFormat("HH:mm");
                    date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
                    String localTime = date.format(currentLocalTime);
                    taskModel.setValues(tasktitle.getText().toString(), tasksubtitle.getText().toString(), taskdetails.getText().toString(),users.get("rollno")+" "+users.get("name"), users.get("phone"), Id);
                    taskModel.setTime(localTime + ".." + datestring);
                   firebasetask.child(Id).setValue(taskModel);

                    if (!arrayAdapter.isEmpty())
                        Toast.makeText(JcActivity.this, "New Task Created!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(JcActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                    createtaskdialog.dismiss();
            }
        });
    }
/////////////////
public void showEditTaskDialog(final String taskid)
{
    final AlertDialog.Builder dialogbuilder2= new AlertDialog.Builder(this);
    LayoutInflater layoutInflater= getLayoutInflater();
    final View createtaskview2 = layoutInflater.inflate(R.layout.task_editor,null);
    dialogbuilder2.setView(createtaskview2);
    dialogbuilder2.setTitle("EDIT TASK: "+tasktitle);
    final ListView memlist;
    final EditText firstname, lastname;
    final Button destroytask,scoutmoremembers,cancel,serach;
    firstname=(EditText)createtaskview2.findViewById(R.id.firstname);
    lastname=(EditText)createtaskview2.findViewById(R.id.lastname);
    destroytask=(Button)createtaskview2.findViewById(R.id.destroytask);
    scoutmoremembers=(Button)createtaskview2.findViewById(R.id.scoutmoremembers);
    serach=(Button)createtaskview2.findViewById(R.id.search);
    cancel=(Button)createtaskview2.findViewById(R.id.cancel);
    memlist=(ListView)createtaskview2.findViewById(R.id.memlist);
    memlist.setAdapter(arrayAdaptermembers);
    final AlertDialog createtaskdialog2=dialogbuilder2.create();
    createtaskdialog2.show();
    createtaskdialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            searchedmember="";
        }
    });
    scoutmoremembers.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    });


    memlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String,String> dataMap = new HashMap<String, String>();
            if(searchedmember.matches("")) {
                AddId = members.get(position).getId();
                AddName = members.get(position).getName();
                AddRollNo = members.get(position).getRollno();
                firebasetask = FirebaseDatabase.getInstance().getReference(currentteam);
                dataMap.put("Name",AddName);
                dataMap.put("Roll No",AddRollNo);
                firebasetask.child(taskid).child("Members").child(AddId).setValue(dataMap);
                firebasemembers.child(AddId).child("currenttask").setValue(taskid);
                firebasemembers.child(AddId).child("teamtask").setValue(currentteam);
                Toast.makeText(JcActivity.this, AddName+" is Added to this task.", Toast.LENGTH_SHORT).show();

            }
            else{
                firebasemembers.child(searchedmember).child("currenttask").setValue(taskid);
                firebasemembers.child(searchedmember).child("teamtask").setValue(currentteam);
                dataMap.put("Name",searchedname);
                dataMap.put("Roll No",searchedrollno);
                firebasetask.child(taskid).child("Members").child(searchedmember).setValue(dataMap);
                memlist.setAdapter(arrayAdaptermembers);
                Toast.makeText(JcActivity.this,"This member is Added to this task.", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
    });

    serach.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!firstname.getText().toString().isEmpty() && !lastname.getText().toString().isEmpty())
            {
                String name= firstname.getText().toString().toLowerCase().replace(" ","")+" "+lastname.getText().toString().toLowerCase().replace(" ","");
                for(int i=0;i<members.size();i++)
                {
                    if(members.get(i).getName().matches(name))
                    {
                        ArrayList<String> temp=new ArrayList<String>();
                        searchedmember=members.get(i).getId();
                        searchedname=members.get(i).getName();
                        searchedrollno=members.get(i).getRollno();
                        temp.add("\nRoll No: "+searchedrollno+"\nName: "+searchedname+"\nNearest Station: "+members.get(i).getNeareststation());
                        ArrayAdapter<String> tempaa= new ArrayAdapter<String>(JcActivity.this, android.R.layout.simple_list_item_1, temp);
                        memlist.setAdapter(tempaa);

                        break;
                    }
                }
            }
            else
            {
                searchedmember="";
                searchedname="";
                searchedrollno="";
                memlist.setAdapter(arrayAdaptermembers);
            }
        }
    });



        destroytask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebasetask.child(taskid).child("Members").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot fire: dataSnapshot.getChildren())
                        {
                            firebasemembers.child(fire.getKey()).child("currenttask").setValue("null");
                            firebasemembers.child(fire.getKey()).child("teamtask").setValue("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                firebasetask.child(taskid).removeValue();
                createtaskdialog2.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    createtaskdialog2.dismiss();
            }
        });;
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebasetask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayAdapter.clear();
                tasks.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    TaskModel taskModel= fire.getValue(TaskModel.class);
                    arrayAdapter.add("\nTask title: "+taskModel.tasktitle+"\nTask description: "+taskModel.taskdetails+"\nAt: "+taskModel.getTime());
                    tasks.add(taskModel);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
/////////////////////////////////////

        firebasemembers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayAdaptermembers.clear();
                colpref2.clear();
                colpref3.clear();
                colnopref.clear();
                members.clear();
                mempref2.clear();
                mempref3.clear();
                memnopref.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    Model model= fire.getValue(Model.class);

                    if(!model.getRollno().equals(users.get("rollno")) && model.getCurrenttask().equals("null")) {
                        if (model.getPreference1().matches(users.get("pref1"))) {
                            arrayAdaptermembers.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation() + "\nPreference1: " + model.getPreference1());
                            model.setId(fire.getKey());
                            members.add(model);
                        } else if (model.getPreference2().matches(users.get("pref1")))
                        {
                            colpref2.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation() + "\nPreference2: " + model.getPreference2());
                            model.setId(fire.getKey());
                            mempref2.add(model);
                        }
                        else if(model.getPreference3().matches(users.get("pref1")))
                        {
                            colpref3.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation() + "\nPreference3: " + model.getPreference3());
                            model.setId(fire.getKey());
                            mempref3.add(model);
                        }
                        else
                        {
                            colnopref.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation());
                            model.setId(fire.getKey());
                            memnopref.add(model);
                        }
                    }
                }
                arrayAdaptermembers.addAll(colpref2);
                arrayAdaptermembers.addAll(colpref3);
                arrayAdaptermembers.addAll(colnopref);
                members.addAll(mempref2);
                members.addAll(mempref3);
                members.addAll(memnopref);
                arrayAdaptermembers.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });

        ///////////////////////////

    }
    @Override
    public void onBackPressed() {

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                db.deleteUsers();
                finish();
                return true;
            case R.id.editprofile:
                Model model = new Model();
                Intent intenteditprofile= new Intent(JcActivity.this,EditProfile.class);
                startActivity(intenteditprofile);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////////////////////////////
    public boolean isConnected(Context context)
    {

        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo= cm.getActiveNetworkInfo();
        if(netinfo!=null && netinfo.isConnectedOrConnecting())
        {
            android.net.NetworkInfo wifi= cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile=cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile!=null && mobile.isConnectedOrConnecting())|| (wifi!=null && wifi.isConnectedOrConnecting()))
            {
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
    //////////////////


}

/*

/////////////////////

//////////////////////////////////////
                 public void showEditTaskDialog()
    {
        final AlertDialog.Builder dialogbuilder2= new AlertDialog.Builder(this);
        LayoutInflater layoutInflater= getLayoutInflater();
        final View createtaskview2 = layoutInflater.inflate(R.layout.task_editor,null);
        dialogbuilder2.setView(createtaskview2);
        dialogbuilder2.setTitle("EDIT TASK");
        final ListView memlist;
        final EditText firstname, lastname;
        final Button destroytask,addmembers,cancel,serach;
        firstname=(EditText)createtaskview2.findViewById(R.id.firstname);
        lastname=(EditText)createtaskview2.findViewById(R.id.lastname);
        destroytask=(Button)createtaskview2.findViewById(R.id.destroytask);
        addmembers=(Button)createtaskview2.findViewById(R.id.addmembers);
        serach=(Button)createtaskview2.findViewById(R.id.search);
        cancel=(Button)createtaskview2.findViewById(R.id.cancel);
        memlist=(ListView)createtaskview2.findViewById(R.id.memlist);
        memlist.setAdapter(arrayAdaptermembers);
        final AlertDialog createtaskdialog2=dialogbuilder2.create();
        createtaskdialog2.show();
        createtaskdialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                searchedmember="";
            }
        });
         memlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 if(searchedmember.matches(""))
                 Toast.makeText(JcActivity.this, members.get(position).getId(), Toast.LENGTH_SHORT).show();
                 else
                     Toast.makeText(JcActivity.this,searchedmember, Toast.LENGTH_SHORT).show();
             }
         });
        serach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!firstname.getText().toString().isEmpty() && !lastname.getText().toString().isEmpty())
                {
                    String name= firstname.getText().toString().toLowerCase().replace(" ","")+" "+lastname.getText().toString().toLowerCase().replace(" ","");
                    for(int i=0;i<members.size();i++)
                    {
                       if(members.get(i).getName().matches(name))
                       {
                           ArrayList<String> temp=new ArrayList<String>();
                           searchedmember=members.get(i).getId();
                           temp.add("\nRoll No: "+members.get(i).getRollno()+"\nName: "+members.get(i).getName()+"\nNearest Station: "+members.get(i).getNeareststation());
                           ArrayAdapter<String> tempaa= new ArrayAdapter<String>(JcActivity.this, android.R.layout.simple_list_item_1, temp);
                           memlist.setAdapter(tempaa);
                           break;
                       }
                    }
                }
                else
                {
                    searchedmember="";
                    memlist.setAdapter(arrayAdaptermembers);
                }
            }
        });

//////////////////////
firebasemembers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayAdaptermembers.clear();
                memberstring.clear();
                members.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    Model model= fire.getValue(Model.class);
                    if(model.getPreference1().matches(users.get("pref1")) && !model.getRollno().equals(users.get("rollno")) && model.getCurrenttask().equals("null")){
                        arrayAdaptermembers.add("\nRoll No: "+model.getRollno()+"\nName: "+model.getName()+"\nNearest Station: "+model.getNeareststation()+"\nPreference1: "+model.getPreference1());
                        model.setId(fire.getKey());
                        members.add(model);
                    }
                }
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    Model model= fire.getValue(Model.class);
                    if(model.getPreference2().matches(users.get("pref1")) && !model.getRollno().equals(users.get("rollno")) && model.getCurrenttask().equals("null")){
                        arrayAdaptermembers.add("\nRoll No: "+model.getRollno()+"\nName: "+model.getName()+"\nNearest Station: "+model.getNeareststation()+"\nPreference2: "+model.getPreference2());
                        model.setId(fire.getKey());
                        members.add(model);
                    }
                }
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    Model model= fire.getValue(Model.class);
                    if(model.getPreference3().matches(users.get("pref1")) && !model.getRollno().equals(users.get("rollno")) && model.getCurrenttask().equals("null")){
                        arrayAdaptermembers.add("\nRoll No: "+model.getRollno()+"\nName: "+model.getName()+"\nNearest Station: "+model.getNeareststation()+"\nPreference3: "+model.getPreference3());
                        model.setId(fire.getKey());
                        members.add(model);
                    }
                }
                arrayAdaptermembers.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });


                */

