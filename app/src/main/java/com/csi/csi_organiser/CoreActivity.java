package com.csi.csi_organiser;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CoreActivity extends AppCompatActivity {
   ListView teams;
    ArrayList<String> teamlist;
    CustomAdapter teamAdapter;
    SQLiteHelper db;
    TextView welcome;
    HashMap<String,String>users;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        else {
            teamlist = new ArrayList<>();
            db = new SQLiteHelper(this);
            welcome = (TextView) findViewById(R.id.welcome);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("TEAMS");
            users = db.getAllValues();
            welcome.setText("WELCOME " + users.get("name").toUpperCase());
            teamlist.add("Technical");
            teamlist.add("Creative");
            teamlist.add("GOT");
            teamlist.add("Publicity");
            teamlist.add("Sponsorship");
            teamlist.add("Editorial");
            teamlist.add("Designing");
            teamlist.add("MemberShip");
            teamlist.add("Research-Wing");
            teams = (ListView) findViewById(R.id.teams);
            teamAdapter = new CustomAdapter(teamlist, this);
            teams.setAdapter(teamAdapter);
        }
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
                Intent intenteditprofile= new Intent(CoreActivity.this,EditProfile.class);
                startActivity(intenteditprofile);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class CustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<String> list = new ArrayList<String>();
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
                view = inflater.inflate(R.layout.row_type2, null);
            }

            //Handle TextView and display string from your list
            TextView listItemText = (TextView)view.findViewById(R.id.nameView);
            listItemText.setText(list.get(position));
            TextView nooftasks=(TextView)view.findViewById(R.id.nooftasks);
            nooftasks.setText("");
            //Handle buttons and add onClickListeners
            final Button viewtasks = (Button)view.findViewById(R.id.viewtasks);
            viewtasks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  Intent intent= new Intent(CoreActivity.this,CoretaskViewer.class);
                  intent.putExtra("currentteam","Tasks-"+list.get(position));
                  startActivity(intent);
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CoreActivity.this,GSignin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }
}
