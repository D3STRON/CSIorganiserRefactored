package com.csi.csi_organiser;

import java.io.Serializable;

/**
 * Created by montur on 8/27/2017.
 */

public class Model implements Serializable {
    public String Id;
    public String name;
    public String email;
    public String number;
    public String rollno;
    public String neareststation;
    public String preference1;
    public String preference2;
    public String preference3;
    public String priority="0";
    public String currenttask="null";
    public int numberoftasks=0;

    public void setValue(String name, String email, String number, String neareststation, String rollno, String preference1, String preference2, String preference3)
    {
        this.email=email;
        this.name=name;
        this.number=number;
        this.rollno=rollno;
        this.preference1=preference1;
        this.preference2=preference2;
        this.preference3=preference3;
        this.neareststation=neareststation;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    public String getRollno() {
        return rollno;
    }

    public String getNeareststation() {
        return neareststation;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPreference1() {
        return preference1;
    }

    public String getPriority() {
        return priority;
    }

    public String getCurrenttask() {
        return currenttask;
    }

    public int getNumberoftasks() {
        return numberoftasks;
    }

    public String getPreference2() {
        return preference2;
    }

    public String getPreference3() {
        return preference3;
    }

    public void setValue(String name, String email, String number, String neareststation, String rollno, String preference1, String preference2, String preference3, String currenttask, int numberoftasks)
    {
        this.email=email;
        this.name=name;
        this.number=number;
        this.rollno=rollno;
        this.preference1=preference1;
        this.preference2=preference2;
        this.preference3=preference3;
        this.neareststation=neareststation;
        this.currenttask=currenttask;
        this.numberoftasks=numberoftasks;
    }
}
