package com.csi.csi_organiser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.HashMap;

public class DateActivity extends AppCompatActivity {
    ListView list;
    ArrayAdapter<String> dateAdapter,taskAdapter;
    Button backtodates,emailnames;
    CustomAdapter  membersAdapter;
    ArrayList<String> datestring, membersstring,membersstring2,taskString, idString;
    DatabaseReference dates;
    ValueEventListener ve;
    Toolbar toolbar;
    SQLiteHelper db;
    String d;
    HashMap<String,String> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        datestring = new ArrayList<>();
        db=new SQLiteHelper(this);
        users=db.getAllValues();
        membersstring = new ArrayList<>();
        idString=new ArrayList<>();
        membersstring2 = new ArrayList<>();
        taskString = new ArrayList<>();
        backtodates= (Button)findViewById(R.id.backtodates);
        backtodates.setVisibility(View.GONE);
        emailnames= (Button)findViewById(R.id.emailnames);
        emailnames.setVisibility(View.GONE);
        taskAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, taskString);
        dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datestring);
        membersAdapter = new CustomAdapter(membersstring,this);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(dateAdapter);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attended Dates");
        getSupportActionBar().setSubtitle("Tap on the dates to view the members..");
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.getAdapter()==dateAdapter)
                {
                    //Toast.makeText(DateActivity.this,"Here",Toast.LENGTH_SHORT).show();
                    fetchmembers(dateAdapter.getItem(position));
                    list.setAdapter(membersAdapter);
                    backtodates.setVisibility(View.VISIBLE);
                    emailnames.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("Members");
                    getSupportActionBar().setSubtitle("members attended task on date "+dateAdapter.getItem(position));
                    d=dateAdapter.getItem(position);
                }

            }
        });

        backtodates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setAdapter(dateAdapter);
                backtodates.setVisibility(View.GONE);
                emailnames.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Attendence Dates");
                getSupportActionBar().setSubtitle("Tap on the dates to view the members..");
                d="";
            }
        });
        emailnames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(Intent.ACTION_SEND);
                String[] to={users.get("email")};
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, to);
                intent.putExtra(Intent.EXTRA_SUBJECT,d);
                intent.putExtra(Intent.EXTRA_TEXT,membersstring2.toString());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "Send email"));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        dates = FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("currentteam")).child("Days");
        if(ve!=null)
        {
            dates.removeEventListener(ve);
        }
        ve = dates.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dateAdapter.clear();
                for (DataSnapshot fire : dataSnapshot.getChildren()) {
                    dateAdapter.add(fire.getKey());
                }
                dateAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public  void  fetchmembers(String date)
    {
        dates.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                membersAdapter.clear();
                membersstring2.clear();
                idString.clear();
                for (DataSnapshot fire: dataSnapshot.getChildren())
                {
                    idString.add(fire.getKey());
                    String s=(String) fire.child("Details").getValue();
                    membersAdapter.add(s);
                    membersstring2.add("\n\n"+s);
                }
                membersAdapter.setIdstring(idString);
                membersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showtaskDialog(String s)
    {
        final AlertDialog.Builder dialogbuilder2 = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View createtaskview2 = layoutInflater.inflate(R.layout.core_member_view, null);
        dialogbuilder2.setView(createtaskview2);
        dialogbuilder2.setTitle(s);
        final AlertDialog createtaskdialog2 = dialogbuilder2.create();
        final ListView tasklist;
        tasklist = (ListView) createtaskview2.findViewById(R.id.memlist);
        final Button cancel=(Button)createtaskview2.findViewById(R.id.cancel);
        tasklist.setAdapter(taskAdapter);
        createtaskdialog2.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createtaskdialog2.dismiss();
            }
        });
    }
    public class CustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> list = new ArrayList<String>();
        private ArrayList<String> idstring= new ArrayList<>();
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
            TextView nooftask=(TextView)view.findViewById(R.id.noofmembers);
            final Button viewtasks = (Button)view.findViewById(R.id.move);
           final Button removemembers=(Button) view.findViewById(R.id.removemember);
            viewtasks.setVisibility(View.VISIBLE);
            viewtasks.setText("REMOVE");
           removemembers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dates.child(d).child(idstring.get(position)).removeValue();
                    fetchmembers(d);
                }
            });
            viewtasks.setText("VIEW TASKS");
            nooftask.setText("");
            //Handle buttons and add onClickListeners
            viewtasks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchtasks(idString.get(position));
                    showtaskDialog(list.get(position));
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
        public void setIdstring(ArrayList<String> idstring){this.idstring=idString;};

    }
    public void fetchtasks(String memberid)
    {
        dates.child(d).child(memberid).child("Tasks Performed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskAdapter.clear();
                for(DataSnapshot fire : dataSnapshot.getChildren())
                {
                    taskAdapter.add((String) fire.child("task details").getValue());
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}