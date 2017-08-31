package com.csi.csi_organiser;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;


public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "CSI_ORGANISER";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USER = "user";

    public SQLiteHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE =
                "CREATE TABLE IF NOT EXISTS user ( currentTask TEXT, name TEXT, email TEXT UNIQUE, phone TEXT, station TEXT, nooftask INTEGER, pref1 TEXT, pref2 TEXT, pref3 TEXT, priority TEXT, rollno TEXT, uuid TEXT);";
        db.execSQL(CREATE_USER_TABLE);
        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

    public void addInfo(String currentTask, String name,String email, String phone, String station, int nooftasks, String pref1, String pref2, String pref3, String priority, String rollno){
        SQLiteDatabase db = this.getWritableDatabase();
        String INSERT = "INSERT INTO user VALUES('"+currentTask+"','"+name+"','"+email+"','"+phone+
                "','"+station+"',"+nooftasks+",'"+pref1+"','"+pref2+"','"+pref3+"','"+priority+"','"+rollno+"');";
        db.execSQL(INSERT);
        db.close();
        Log.d(TAG, "New user inserted into sqlite");
    }

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, null, null);
        db.close();
        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public HashMap<String,String> getAllValues(){
        HashMap<String,String> values = new HashMap<>();
        String query = "SELECT * FROM user;";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            values.put("currentTask",cursor.getString(0));
            values.put("name",cursor.getString(1));
            values.put("email",cursor.getString(2));
            values.put("phone",cursor.getString(3));
            values.put("station",cursor.getString(4));
            values.put("nooftasks",cursor.getString(5));
            values.put("pref1",cursor.getString(6));
            values.put("pref2",cursor.getString(7));
            values.put("pref3",cursor.getString(8));
            values.put("priority",cursor.getString(9));
            values.put("rollno",cursor.getString(10));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetching user from Sqlite: " + values.toString());
        return  values;
    }
}
