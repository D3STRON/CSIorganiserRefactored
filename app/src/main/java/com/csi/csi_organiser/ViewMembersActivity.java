package com.csi.csi_organiser;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ViewMembersActivity extends ListActivity{
    Toolbar toolbar;
    TaskModel taskmodel;
    ListView presentmembers;
    ArrayList<String> presentmemstring, idstring, text;
    MyCustomAdapter arrayAdapter;
    boolean flag=false;
    DatabaseReference firemembers,firecsi;
    ChildEventListener childEventListener;
    Button dtwithattendence,dtwithnoattendence;
    SQLiteHelper db;
    HashMap<String, String> users;
    ArrayList<Boolean> attendencelist;
    DatabaseReference temp;

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
         CheckBox b=(CheckBox)v.findViewById(R.id.checkBoxes);
        Toast.makeText(ViewMembersActivity.this,Integer.toString(position),Toast.LENGTH_SHORT).show();
        // if(b.isChecked())
         //{
           //  attendencelist.add(true);
             //Toast.makeText(ViewMembersActivity.this,Integer.toString(position),Toast.LENGTH_SHORT).show();
        // }
         //else
         //    attendencelist.add(false);
    }*/



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);
        presentmembers=getListView();
        presentmemstring = new ArrayList<>();
        idstring = new ArrayList<>();
        text= new ArrayList<>();
        db=new SQLiteHelper(this);
        attendencelist=new ArrayList<>();
        users=db.getAllValues();
        taskmodel = (TaskModel) getIntent().getSerializableExtra("taskmodel");
        firemembers = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id).child("Members");
        firecsi=FirebaseDatabase.getInstance().getReference("CSI Members");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        dtwithattendence=(Button)findViewById(R.id.dtwithattendence);
        dtwithnoattendence=(Button)findViewById(R.id.dtwithnoattendence);
        //setSupportActionBar(toolbar);
       toolbar.setTitle(taskmodel.getTasktitle());
       toolbar.setSubtitle("Current Members of this task...");
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setSubtitleTextColor(0xFFFFFFFF);
        arrayAdapter = new MyCustomAdapter(presentmemstring,this);
        presentmembers.setAdapter(arrayAdapter);
        temp=FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam"));



        presentmembers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                firecsi.child(idstring.get(position)).child("currenttask").setValue("null");
                firecsi.child(idstring.get(position)).child("teamtask").setValue("");
                firemembers.child(idstring.get(position)).removeValue();
                return true;
            }
        });



       presentmembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

               final DatabaseReference temp= FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id);
               temp.child("Members").child(idstring.get(position)).child("Attended").addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                      String attend=(String) dataSnapshot.getValue();
                       if(attend.isEmpty())
                       {
                           temp.child("Members").child(idstring.get(position)).child("Attended").setValue("yes");
                       }
                       else
                       {
                           temp.child("Members").child(idstring.get(position)).child("Attended").setValue("");
                       }
                                          }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
           }
       });

        dtwithnoattendence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConformationDialouge(false);
                flag=true;
            }
        });
        dtwithattendence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConformationDialouge(true);
               flag=true;
                ///
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
                  attendencelist.clear();
                 int a;
                       for (DataSnapshot fire : dataSnapshot.getChildren()) {

                           String reason = (String) fire.child("Backout Request").getValue();
                          String attended = (String) fire.child("Attended").getValue();
                         if(fire.child("Backout Request").getValue()==null && fire.child("Attended").getValue()==null)
                         {
                             arrayAdapter.add("Name: " + fire.child("Name").getValue() + "\nIs ready for the task.");
                             attendencelist.add(false);
                         }else if(fire.child("Backout Request").getValue()==null)
                         {
                             firemembers.child(fire.getKey()).removeValue();
                         }
                           else if (reason.matches("") && attended.matches("")) {
                              arrayAdapter.add("Name: " + fire.child("Name").getValue() + "\nIs ready for the task.");
                              attendencelist.add(false);
                          } else if (attended.matches("") && !reason.isEmpty()) {
                              arrayAdapter.add("Name: " + fire.child("Name").getValue() + "\nBack out request: " + fire.child("Backout Request").getValue());
                              attendencelist.add(false);
                          } else if (attended.matches("yes")) {
                              arrayAdapter.add("Name: " + fire.child("Name").getValue() + "\nAttended the task");
                              attendencelist.add(true);
                          }
                         //  arrayAdapter.getView(1,R.layout.row);
                          idstring.add(fire.getKey());
                      }
                      arrayAdapter.setIdString(idstring);
                      arrayAdapter.notifyDataSetChanged();
              }
              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });
    }

    public void destroyTask(final boolean attendence)
    {
        Date currentLocalTime = Calendar.getInstance().getTime();
        Long dat = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
        String datestring = sdf.format(dat);
        DatabaseReference date=FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child("Days").child(datestring);
                for(int  i=0; i<idstring.size();i++)
                {

                    if(attendence && attendencelist.get(i))
                    {
                        date.child(idstring.get(i)).child("Details").setValue(arrayAdapter.getItem(i));
                        date.child(idstring.get(i)).child("Tasks Performed").child(taskmodel.Id).child("task details").setValue("Task title: "+taskmodel.getTasktitle()+"\nTask Details: "+taskmodel.getTaskdetails());
                    }
                    //  Toast.makeText(ViewMembersActivity.this,"",Toast.LENGTH_SHORT).show();
                   FirebaseDatabase.getInstance().getReference("CSI Members").child(idstring.get(i)).child("currenttask").setValue("null");
                    FirebaseDatabase.getInstance().getReference("CSI Members").child(idstring.get(i)).child("teamtask").setValue("");

                }

             FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child(taskmodel.Id).removeValue();
         toolbar.setTitle("Task "+taskmodel.getTasktitle()+" is Inactive");
        toolbar.setTitleTextColor(0xFFFFFFFF);
        dtwithnoattendence.setVisibility(View.GONE);
        dtwithattendence.setVisibility(View.GONE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        flag=false;
        if(childEventListener != null)
        {
            firemembers.removeEventListener(childEventListener);
        }
       temp.child(taskmodel.Id).addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(DataSnapshot dataSnapshot, String s) {

           }

           @Override
           public void onChildChanged(DataSnapshot dataSnapshot, String s) {

           }

           @Override
           public void onChildRemoved(DataSnapshot dataSnapshot) {


              if(dataSnapshot.getKey().matches("Id"))
              {
                  toolbar.setTitle("Task "+taskmodel.getTasktitle()+" is Inactive");
                  toolbar.setTitleTextColor(0xFFFFFFFF);
                  dtwithnoattendence.setVisibility(View.GONE);
                  dtwithattendence.setVisibility(View.GONE);
                  flag=true;
              }

           }

           @Override
           public void onChildMoved(DataSnapshot dataSnapshot, String s) {

           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });


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
            conformationmessage.setText("The task will be destroyed and the attended will be given attendence for task "+taskmodel.getTasktitle());
        }
        else if(!attendence)
        {
            conformationmessage.setText("The task will be destroyed and no members will be given attendence for this task "+taskmodel.getTasktitle());
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
                alertDialog.dismiss();
            }
        });
    }

        @Override
    public void onBackPressed() {
        super.onBackPressed();
        firemembers.removeEventListener(childEventListener);
            if(flag)
            {
                Intent intent = new Intent(ViewMembersActivity.this, NotifyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
    }
/////////////////////////////////////////////
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
                    firecsi.child(idstring.get(position)).child("currenttask").setValue("null");
                    firecsi.child(idstring.get(position)).child("teamtask").setValue("");
                    firemembers.child(idstring.get(position)).removeValue();
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
}
/*

  Intent intent= new Intent(Intent.ACTION_SEND);
       String[] to={users.get("email")};
       intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_SUBJECT, taskmodel.getTasktitle()+"-"+taskmodel.getTasksubtitle());
      intent.putExtra(Intent.EXTRA_TEXT, "on "+datestring+"\ntask: "+taskmodel.getTasktitle()+"\n"+text);
        intent.setType("text/plain");
      startActivity(Intent.createChooser(intent, "Send email"));

            ///////////////////////////////sending mail
      if(attendence && !text.isEmpty()){

      }
        /////////////////////////
 */