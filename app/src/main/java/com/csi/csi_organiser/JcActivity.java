package com.csi.csi_organiser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class JcActivity extends AppCompatActivity {
    Button createtask, exit;
    ListView tasklist;
    //    ArrayList<TaskModel> tasks;
    //  ArrayList<Model> members;
    ArrayList<TaskModel> tasks;
    ArrayList<Model> mempref1, mempref2, mempref3, memmore, currentmemlist, allmembers;
    TextView welcome;
    Toolbar toolbar;
    ArrayList<String> tasksstring, memberstringpref1, memberstringpref2, memberstringpref3, memberstringmore;
    HashMap<String, String> users;
    DatabaseReference firebasetask, firebasemembers, temp;
    SQLiteHelper db;
    ProgressBar progressbar4;
    Long timerforprogressbar;
    String taskid, searchedmember = "", AddId, AddName, AddRollNo, tasktitle, currentteam, searchedname, searchedrollno;
    ArrayAdapter<String> arrayAdapter, arrayAdaptermemberspref1, arrayAdaptermemberspref2, arrayAdaptermemberspref3, arrayAdaptermembersmore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timerforprogressbar = (long) 3000;
        new MyProgressBar().execute((Void) null);
        setContentView(R.layout.activity_jc);
        tasks = new ArrayList<>();
        tasksstring = new ArrayList<>();
        mempref1 = new ArrayList<>();
        mempref2 = new ArrayList<>();
        mempref3 = new ArrayList<>();
        memmore = new ArrayList<>();
        allmembers = new ArrayList<>();
        memberstringpref1 = new ArrayList<>();
        memberstringpref2 = new ArrayList<>();
        memberstringpref3 = new ArrayList<>();
        memberstringmore = new ArrayList<>();

        arrayAdaptermemberspref1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberstringpref1);
        arrayAdaptermemberspref2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberstringpref2);
        arrayAdaptermemberspref3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberstringpref3);
        arrayAdaptermembersmore = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberstringmore);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TASK MANAGER");

        db = new SQLiteHelper(this);
        users = db.getAllValues();
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        } else {
            createtask = (Button) findViewById(R.id.createtask);
            tasklist = (ListView) findViewById(R.id.tasklist);
            exit = (Button) findViewById(R.id.exit);
            progressbar4 = (ProgressBar) findViewById(R.id.progressBar4);
            welcome = (TextView) findViewById(R.id.welcome);
            welcome.setText("WELCOME " + users.get("name").toUpperCase());

            switch (Integer.parseInt(users.get("priority"))) {
                case (2):
                    firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-Technical");
                    currentteam = "Tasks-Technical";
                    break;
                case (3):
                    firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-Creative");
                    currentteam = "Tasks-Creative";
                    break;
                case (4):
                    firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-GOT");
                    currentteam = "Tasks-GOT";
                    break;
                case (5):
                    firebasetask = FirebaseDatabase.getInstance().getReference("Tasks-Publicity");
                    currentteam = "Tasks-Publicity";
                    break;
            }
            firebasemembers = FirebaseDatabase.getInstance().getReference("CSI Members");
            temp = FirebaseDatabase.getInstance().getReference("CSI Members").child(users.get("UUID"));


            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasksstring);
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
                    tasktitle = tasks.get(position).getTasktitle();
                    showEditTaskDialog(taskid);
                    return true;
                }
            });

            tasklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(JcActivity.this, NotifyActivity.class);
                    intent.putExtra("taskmodel", tasks.get(position));
                    intent.putExtra("currentteam", currentteam);
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
        //new LongOperation().execute("");
    }

    public void showCreateTaskDialog() {
        final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View createtaskview = layoutInflater.inflate(R.layout.taskcreate, null);
        dialogbuilder.setView(createtaskview);
        dialogbuilder.setTitle("CREATE TASK");
        final EditText tasktitle, tasksubtitle, taskdetails;
        final Button create, cancel;
        tasktitle = (EditText) createtaskview.findViewById(R.id.tasktitle);
        tasksubtitle = (EditText) createtaskview.findViewById(R.id.tasksubtitle);
        taskdetails = (EditText) createtaskview.findViewById(R.id.taskdetails);
        create = (Button) createtaskview.findViewById(R.id.create);
        cancel = (Button) createtaskview.findViewById(R.id.cancel);
        final AlertDialog createtaskdialog = dialogbuilder.create();
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
                TaskModel taskModel = new TaskModel();

                boolean connection = isConnected(JcActivity.this);
                if (connection) {
                    String Id = firebasetask.push().getKey();
                    Date currentLocalTime = Calendar.getInstance().getTime();
                    Long dat = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
                    String datestring = sdf.format(dat);
                    DateFormat date = new SimpleDateFormat("HH:mm");
                    date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
                    String localTime = date.format(currentLocalTime);
                    taskModel.setValues(tasktitle.getText().toString(), tasksubtitle.getText().toString(), taskdetails.getText().toString(), users.get("rollno") + " " + users.get("name"), users.get("phone"), Id);
                    taskModel.setTime(localTime + ".." + datestring);
                    firebasetask.child(Id).setValue(taskModel);

                    if (!arrayAdapter.isEmpty())
                        Toast.makeText(JcActivity.this, "New Task Created!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(JcActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                createtaskdialog.dismiss();
            }
        });
    }

    /////////////////
    public void showEditTaskDialog(final String taskid) {
        final AlertDialog.Builder dialogbuilder2 = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View createtaskview2 = layoutInflater.inflate(R.layout.task_editor, null);
        dialogbuilder2.setView(createtaskview2);
        dialogbuilder2.setTitle("EDIT TASK: " + tasktitle);
        final ListView memlist;
        final EditText firstname, lastname;
        final Button preference1, preference2, preference3, more, cancel, serach;
        firstname = (EditText) createtaskview2.findViewById(R.id.firstname);
        lastname = (EditText) createtaskview2.findViewById(R.id.lastname);
        preference1 = (Button) createtaskview2.findViewById(R.id.preference1);
        preference2 = (Button) createtaskview2.findViewById(R.id.preference2);
        preference3 = (Button) createtaskview2.findViewById(R.id.preference3);
        more = (Button) createtaskview2.findViewById(R.id.more);
        serach = (Button) createtaskview2.findViewById(R.id.search);
        cancel = (Button) createtaskview2.findViewById(R.id.cancel);
        memlist = (ListView) createtaskview2.findViewById(R.id.memlist);
        ////initial
        memlist.setAdapter(arrayAdaptermemberspref1);
        currentmemlist = mempref1;
        ////
        final AlertDialog createtaskdialog2 = dialogbuilder2.create();
        createtaskdialog2.show();
        createtaskdialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                searchedmember = "";
            }
        });

        Toast.makeText(JcActivity.this, "" +
                "Tap on any Members to add them to this task!", Toast.LENGTH_LONG).show();
        memlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> dataMap = new HashMap<String, String>();
                if (searchedmember.matches("")) {
                    AddId = currentmemlist.get(position).getId();
                    AddName = currentmemlist.get(position).getName();
                    AddRollNo = currentmemlist.get(position).getRollno();
                    dataMap.put("Name", AddName + " " + AddRollNo);
                    dataMap.put("Backout Request", "");
                    dataMap.put("Attended","");
                    firebasetask = FirebaseDatabase.getInstance().getReference(currentteam);
                    firebasetask.child(taskid).child("Members").child(AddId).setValue(dataMap);
                    taskVerify(taskid,AddId,AddName);

                } else {
                   dataMap.put("Name", searchedname + " " + searchedrollno);
                    dataMap.put("Backout Request", "");
                    dataMap.put("Attended","");
                    firebasetask = FirebaseDatabase.getInstance().getReference(currentteam);
                    firebasetask.child(taskid).child("Members").child(searchedmember).setValue(dataMap);
                    taskVerify(taskid,searchedmember,searchedname);
                    memlist.setAdapter(arrayAdaptermemberspref1);
                    searchedmember = "";
                   }

            }
        });

        serach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firstname.getText().toString().isEmpty() && !lastname.getText().toString().isEmpty()) {
                    String name = firstname.getText().toString().toLowerCase().replace(" ", "") + " " + lastname.getText().toString().toLowerCase().replace(" ", "");
                    for (int i = 0; i < allmembers.size(); i++) {
                        if (allmembers.get(i).getName().matches(name)) {
                            ArrayList<String> temp = new ArrayList<String>();
                            searchedmember = allmembers.get(i).getId();
                            searchedname = allmembers.get(i).getName();
                            searchedrollno = allmembers.get(i).getRollno();
                            temp.add("Name: " + searchedname + "\nNearest Station: " + allmembers.get(i).getNeareststation());
                            ArrayAdapter<String> tempaa = new ArrayAdapter<String>(JcActivity.this, android.R.layout.simple_list_item_1, temp);
                            memlist.setAdapter(tempaa);
                            break;
                        }
                    }
                    if(searchedmember.matches(""))
                    {
                        Toast.makeText(JcActivity.this, "No such member found..", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    searchedmember = "";
                    searchedname = "";
                    memlist.setAdapter(arrayAdaptermemberspref1);
                }
            }
        });
        preference1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentmemlist = mempref1;
                memlist.setAdapter(arrayAdaptermemberspref1);
            }
        });

        preference2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentmemlist = mempref2;
                memlist.setAdapter(arrayAdaptermemberspref2);
            }
        });

        preference3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentmemlist = mempref3;
                memlist.setAdapter(arrayAdaptermemberspref3);
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentmemlist = memmore;
                memlist.setAdapter(arrayAdaptermembersmore);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createtaskdialog2.dismiss();
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        firebasetask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    arrayAdapter.clear();
                    tasks.clear();
                    for (DataSnapshot fire : dataSnapshot.getChildren()) {
                        if(!fire.getKey().matches("Days")) {
                        TaskModel taskModel = fire.getValue(TaskModel.class);
                        arrayAdapter.add("\nTask title: " + taskModel.tasktitle + "\nTask description: " + taskModel.taskdetails + "\nAt: " + taskModel.getTime());
                        tasks.add(taskModel);
                        }
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
                arrayAdaptermemberspref1.clear();
                arrayAdaptermemberspref2.clear();
                arrayAdaptermemberspref3.clear();
                arrayAdaptermembersmore.clear();
                mempref1.clear();
                mempref2.clear();
                mempref3.clear();
                memmore.clear();
                allmembers.clear();
                for (DataSnapshot fire : dataSnapshot.getChildren()) {
                    Model model = fire.getValue(Model.class);

                    if (!model.getRollno().equals(users.get("rollno")) && model.getCurrenttask().equals("null") && model.getPriority().matches("0")) {
                        if (model.getPreference1().matches(users.get("pref1"))) {
                            arrayAdaptermemberspref1.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation() + "\nPreference1: " + model.getPreference1());
                            model.setId(fire.getKey());
                            mempref1.add(model);
                        } else if (model.getPreference2().matches(users.get("pref1"))) {
                            arrayAdaptermemberspref2.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation() + "\nPreference2: " + model.getPreference2());
                            model.setId(fire.getKey());
                            mempref2.add(model);
                        } else if (model.getPreference3().matches(users.get("pref1"))) {
                            arrayAdaptermemberspref3.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation() + "\nPreference3: " + model.getPreference3());
                            model.setId(fire.getKey());
                            mempref3.add(model);
                        } else {
                            arrayAdaptermembersmore.add("\nRoll No: " + model.getRollno() + "\nName: " + model.getName() + "\nNearest Station: " + model.getNeareststation());
                            model.setId(fire.getKey());
                            memmore.add(model);
                        }
                    }
                }
                allmembers.addAll(mempref1);
                allmembers.addAll(mempref2);
                allmembers.addAll(mempref3);
                allmembers.addAll(memmore);
                arrayAdaptermemberspref1.notifyDataSetChanged();
                arrayAdaptermemberspref2.notifyDataSetChanged();
                arrayAdaptermemberspref3.notifyDataSetChanged();
                arrayAdaptermembersmore.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ///////////////////////////


    }

    @Override
    public void onBackPressed() {

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.jcmenu, menu);
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
                Intent intenteditprofile = new Intent(JcActivity.this, EditProfile.class);
                startActivity(intenteditprofile);
                finish();
                return true;
            case  R.id.viewattendence:
                Intent intentattendence = new Intent(JcActivity.this,DateActivity.class);
                intentattendence.putExtra("currentteam",currentteam);
                startActivity(intentattendence);
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////////////////////////////
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) {
                return true;
            } else
                return false;
        } else
            return false;
    }


    class MyProgressBar extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub

            try {
                Thread.sleep(timerforprogressbar);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progressbar4.setVisibility(View.GONE);
        }
    }
    public void taskVerify(final String k, final String memid, final String name)
    {
        final DatabaseReference Verifier=FirebaseDatabase.getInstance().getReference(currentteam).child(k);
        Verifier.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("jcnumber").getValue()==null)
                {
                   Verifier.removeValue();
                    Toast.makeText(JcActivity.this,"This Task is Removed!", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    firebasemembers.child(memid).child("currenttask").setValue(taskid);
                    firebasemembers.child(memid).child("teamtask").setValue(currentteam);
                    Toast.makeText(JcActivity.this,name+" is added to this task!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class MyCustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> list = new ArrayList<String>();
        private ArrayList<String> idString = new ArrayList<String>();
        private Context context;
        public MyCustomAdapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row, null);
            }

            //Handle TextView and display string from your list
            TextView listItemText = (TextView)view.findViewById(R.id.nameView);
            listItemText.setText(list.get(position));

            //Handle buttons and add onClickListeners
            final Button removemember = (Button)view.findViewById(R.id.removemember);
            removemember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,idString.get(position),Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
        public void clear()
        {
            list.clear();
        }
        public void add(String s)
        {
            list.add(s);
        }
        public void setIdString(ArrayList<String> idString)
        {
            this.idString=idString;
        }
    }

    public class CustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> list = new ArrayList<String>();
        private ArrayList<TaskModel> taskModel = new ArrayList<>();
        private Context context;
        public CustomAdapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row, null);
            }

            //Handle TextView and display string from your list
            TextView listItemText = (TextView)view.findViewById(R.id.nameView);
            listItemText.setText(list.get(position));

            //Handle buttons and add onClickListeners
            final Button removemember = (Button)view.findViewById(R.id.removemember);
            removemember.setText("Add Members");
            removemember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return view;
        }
        public void clear()
        {
            list.clear();
        }
        public void add(String s)
        {
            list.add(s);
        }
        public void setIdString(ArrayList<TaskModel> taskModel)
        {
            this.taskModel=taskModel;
        }
    }
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
                */

