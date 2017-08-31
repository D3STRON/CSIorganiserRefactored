package com.csi.csi_organiser;

import java.util.ArrayList;

/**
 * Created by Anurag on 25-08-2017.
 */

public class TaskModel {
     public String tasktitle, tasksubtitle, taskdetails;
     public ArrayList<String> volunteers;
    public void setValues(String tasktitle, String tasksubtitle, String taskdetails)
    {
        this.taskdetails=taskdetails;
        this.tasktitle=tasktitle;
        this.tasksubtitle=tasksubtitle;
    }

    public String getTasktitle() {
        return tasktitle;
    }

    public String getTasksubtitle() {
        return tasksubtitle;
    }

    public String getTaskdetails() {
        return taskdetails;
    }
}
