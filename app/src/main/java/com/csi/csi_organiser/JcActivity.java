package com.csi.csi_organiser;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class JcActivity extends AppCompatActivity {
    Button createtask,exit;
    ListView tasklist;
    ArrayList<TaskModel> tasks;
    TextView welcome;
    Toolbar toolbar;
    ArrayList<String> tasksstring;
    HashMap<String ,String> users;
    DatabaseReference firebase;
    SQLiteHelper db;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jc);
        tasks= new ArrayList<>();
        tasksstring= new ArrayList<>();

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TASK MANAGER");

        db = new SQLiteHelper(this);
        users =db.getAllValues();
        createtask= (Button)findViewById(R.id.createtask);
        tasklist=(ListView)findViewById(R.id.tasklist);
        exit=(Button)findViewById(R.id.exit);
        welcome=(TextView)findViewById(R.id.welcome);
        welcome.setText("WELCOME "+db.getAllValues().get("name").toUpperCase());
        firebase= FirebaseDatabase.getInstance().getReference("Tasks");
        arrayAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tasksstring);
        tasklist.setAdapter(arrayAdapter);
        createtask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected(JcActivity.this))
                {   showCreateTaskDialog();}

                else
                { Toast.makeText(JcActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();}
            }
        });

        tasklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JcActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
        });
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
                taskModel.setValues(tasktitle.getText().toString(),tasksubtitle.getText().toString(),taskdetails.getText().toString());
               /* tasks.add(taskModel);
                arrayAdapter.add("\nTask title: "+taskModel.tasktitle+"\nTask subtutle: "+taskModel.tasksubtitle+"\nTask description: "+taskModel.taskdetails);
                 arrayAdapter.notifyDataSetChanged();
                */
                String Id=firebase.push().getKey();
                firebase.child(Id).setValue(taskModel);

                if(!arrayAdapter.isEmpty())
                    Toast.makeText(JcActivity.this,"New Task Created!",Toast.LENGTH_SHORT).show();

                createtaskdialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayAdapter.clear();
                for(DataSnapshot fire: dataSnapshot.getChildren())
                {
                    TaskModel taskModel= fire.getValue(TaskModel.class);
                    arrayAdapter.add("\nTask title: "+taskModel.tasktitle+"\nTask subtutle: "+taskModel.tasksubtitle+"\nTask description: "+taskModel.taskdetails);
                    tasks.add(taskModel);
                }
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                db.deleteUsers();
                finish();
                Intent intent = new Intent(JcActivity.this,HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
                return true;
            case R.id.editprofile:
                Model model = new Model();
                model.setValue(users.get("name"),users.get("email"),users.get("phone"),users.get("station"),users.get("rollno"),users.get("pref1"),users.get("pref2"),users.get("pref3"),users.get("cuttenttask"),Integer.parseInt(users.get("nooftasks")));
                Toast.makeText(JcActivity.this,model.getName(),Toast.LENGTH_SHORT).show();
                return false;
            default:
                 return super.onOptionsItemSelected(item);
        }
    } public boolean isConnected(Context context)
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

    ////////////////////////////

}

