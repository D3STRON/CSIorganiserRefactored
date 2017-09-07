package com.csi.csi_organiser;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
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

public class ViewMembersActivity extends ListActivity{
    Toolbar toolbar;
    TaskModel taskmodel;
    ListView presentmembers;
    ArrayList<String> presentmemstring, idstring;
    ArrayAdapter<String> arrayAdapter;
    DatabaseReference firemembers,firecsi;
    ChildEventListener childEventListener;
    Button dtwithattendence,dtwithnoattendence;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);
        presentmemstring = new ArrayList<>();
        idstring = new ArrayList<>();
        taskmodel = (TaskModel) getIntent().getSerializableExtra("taskmodel");
        firemembers = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id).child("Members");
        firecsi=FirebaseDatabase.getInstance().getReference("CSI Members");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dtwithattendence=(Button)findViewById(R.id.dtwithattendence);
        dtwithnoattendence=(Button)findViewById(R.id.dtwithnoattendence);
        //setSupportActionBar(toolbar);
       // getSupportActionBar().setTitle(taskmodel.getTasktitle());
       // getSupportActionBar().setSubtitle("Current Members of this task...");
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.row,R.id.nameView);
        ListView presentmembers =this.getListView();
        presentmembers.setAdapter(arrayAdapter);





        presentmembers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                firecsi.child(idstring.get(position)).child("currenttask").setValue("null");
                firecsi.child(idstring.get(position)).child("teamtask").setValue("");
                firemembers.child(idstring.get(position)).removeValue();
                return true;
            }
        });

        dtwithnoattendence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConformationDialouge(false);
            }
        });
        dtwithattendence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConformationDialouge(true);
            }
        });
    }
    public void fetchpresentmembers()
    {

          firemembers.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  arrayAdapter.clear();
                  idstring.clear();
                  for(DataSnapshot fire :dataSnapshot.getChildren())
                  {
                      String reason=(String) fire.child("Backout Request").getValue();
                      if(reason.matches(""))
                      {
                          arrayAdapter.add("Name: "+fire.child("Name").getValue()+"\nIs ready for the task.");
                      }
                      else
                      {
                          arrayAdapter.add("Name: "+fire.child("Name").getValue()+"\nBack out request: "+fire.child("Backout Request").getValue());
                      }

                      idstring.add(fire.getKey());
                  }
                  arrayAdapter.notifyDataSetChanged();
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });
    }

    public void destroyTask(final boolean attendence)
    {

                for(int i=0; i<idstring.size();i++)
                {
                    if(attendence )
                    {
                       //String id= FirebaseDatabase.getInstance().getReference("CSI Members").child(idstring.get(i)).child("Days").push().getKey();
                       //FirebaseDatabase.getInstance().getReference("CSI Members").child(idstring.get(i)).child("Days").child(id).setValue();

                    }
                     else
                    {

                    }
                }

          FirebaseDatabase.getInstance().getReference(taskmodel.Id).removeValue();

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(childEventListener != null)
        {
            firemembers.removeEventListener(childEventListener);
        }
        childEventListener=firemembers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchpresentmembers();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchpresentmembers();
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

    public void showConformationDialouge(final boolean attendence) {
        final AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutinflater = getLayoutInflater();
        final View confirmationview = layoutinflater.inflate(R.layout.conformation, null);
        dialogbuilder.setView(confirmationview);
        dialogbuilder.setTitle("CONFORMATION");
        final Button yes, no;
        final  TextView conformationmessage;
        yes = (Button) confirmationview.findViewById(R.id.yes);
        no = (Button) confirmationview.findViewById(R.id.no);
        conformationmessage=(TextView)confirmationview.findViewById(R.id.conforamtionmessage);
        conformationmessage.setTextSize(20);
        if(attendence)
        {
            conformationmessage.setText("The task will be destroyed and the checked members will be given attendence for this task");
        }
        else if(!attendence)
        {
            conformationmessage.setText("The task will be destroyed and no members will be given attendence for this task");
        }
        final AlertDialog alertDialog = dialogbuilder.create();
        alertDialog.show();
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }

        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destroyTask(attendence);
            }
        });
    }

        @Override
    public void onBackPressed() {
        super.onBackPressed();
        firemembers.removeEventListener(childEventListener);
    }

}
/*

package com.csi.csi_organiser;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.csi.csi_organiser.R.layout.row;

public class ViewMembersActivity extends ListActivity {
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
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(taskmodel.getTasktitle());
        //getSupportActionBar().setSubtitle("Current Members of this task...");
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.row,R.id.nameView);
        ListView presentmembers =this.getListView();
        presentmembers.setAdapter(arrayAdapter);

        presentmembers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                firecsi.child(idstring.get(position)).child("currenttask").setValue("null");
                firecsi.child(idstring.get(position)).child("teamtask").setValue("");
                firemembers.child(idstring.get(position)).removeValue();
                return true;
            }
        });

    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        CheckedTextView check = (CheckedTextView)v;
        check.setChecked(!check.isChecked());
    }



}

 */