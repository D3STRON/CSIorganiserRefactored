package com.csi.csi_organiser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class NotifyActivity extends AppCompatActivity {
    TextView tasktitle;
    Toolbar toolbar;
    TaskModel taskmodel;
    ListView notifications;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
         taskmodel= (TaskModel) getIntent().getSerializableExtra("taskmodel");
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        notifications=(ListView)findViewById(R.id.notifications);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(taskmodel.getTasktitle());
        getSupportActionBar().setSubtitle("Click here to view members..");
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(NotifyActivity.this,ViewMembersActivity.class);
                intent.putExtra("taskmodel",taskmodel);
                startActivity(intent);
            }
        });
    }
}
