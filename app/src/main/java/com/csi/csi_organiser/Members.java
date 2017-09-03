package com.csi.csi_organiser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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
    HashMap<String ,String> users;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabase;

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
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("Task");
            mDatabase = FirebaseDatabase.getInstance().getReference();
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
                    mDatabase.child("Reason").setValue(mReasonBox.getText().toString().trim());
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
                Model model = new Model();
                Intent intenteditprofile= new Intent(Members.this,EditProfile.class);
                startActivity(intenteditprofile);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

