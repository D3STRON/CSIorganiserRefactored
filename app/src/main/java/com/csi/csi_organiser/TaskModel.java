package com.csi.csi_organiser;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Anurag on 25-08-2017.
 */

public class TaskModel implements Serializable {
     public String tasktitle, tasksubtitle, taskdetails, jcrollno, Id, jcnumber,time;

    public void setValues(String tasktitle, String tasksubtitle, String taskdetails, String jcrollno, String jcnumber,String Id)
    {
        this.taskdetails=taskdetails;
        this.tasktitle=tasktitle;
        this.tasksubtitle=tasksubtitle;
        this.jcrollno=jcrollno;
        this.Id=Id;
        this.jcnumber=jcnumber;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getJcrollno() {
        return jcrollno;
    }

    public String getJcnumber() {
        return jcnumber;
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
