package com.csi.csi_organiser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

public class ViewMembersActivity extends AppCompatActivity {
    Toolbar toolbar;
    TaskModel taskmodel;
    ListView presentmembers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);
        taskmodel= (TaskModel) getIntent().getSerializableExtra("taskmodel");
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(taskmodel.getTasktitle());
        getSupportActionBar().setSubtitle("Current Members of this task...");
        presentmembers=(ListView)findViewById(R.id.presentmembers);
    }
}
