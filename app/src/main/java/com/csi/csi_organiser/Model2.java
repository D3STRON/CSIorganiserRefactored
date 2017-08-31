package com.csi.csi_organiser;

/**
 * Created by montur on 8/27/2017.
 */
public class Model2 {
    public  String rollno;
    public String number;
    public String priority="2";




    public void setValues(String rollno, String number)
    {

        this.rollno=rollno;
        this.number=number;
    }


    public String getPriority() {
        return priority;
    }

    public String getRollno() {return rollno;}

    public void setRollno(String rollno) {
        this.rollno = rollno;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
