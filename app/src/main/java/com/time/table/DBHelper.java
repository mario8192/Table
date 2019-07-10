package com.time.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "time.db";
    public static final String TABLE_NAME = "time";
    public static final String COL_1 = "DAY";
    public static final String COL_2 = "TIME";
    public static final String COL_3 = "SUBJECT";
    public static final String COL_4 = "TEACHER";
    public static final String COL_5 = "CANCELED";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+" (DAY TEXT, TIME TEXT , SUBJECT TEXT , TEACHER TEXT, CANCELED INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String day, String time,String subject,String teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,day);
        contentValues.put(COL_2,time);
        contentValues.put(COL_3,subject);
        contentValues.put(COL_4,teacher);
        contentValues.put(COL_5,0);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }


    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME + " ORDER BY " + COL_1 + ", TIME(" +COL_2+ ")" ,null);
        return res;
    }

    public boolean updateData(String day,String time,String subject,String teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,day);
        contentValues.put(COL_2,time);
        contentValues.put(COL_3,subject);
        contentValues.put(COL_4,teacher);
        db.update(TABLE_NAME, contentValues, "DAY = ? AND TIME = ?",new String[] { day , time});
        return true;
    }

    public boolean updateTime(String day,String time,String subject,String teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,day);
        contentValues.put(COL_2,time);
        contentValues.put(COL_3,subject);
        contentValues.put(COL_4,teacher);
        db.update(TABLE_NAME, contentValues, "DAY = ? AND SUBJECT = ? AND TEACHER = ?",new String[] { day , subject, teacher});
        return true;
    }

    public boolean updateCancel(String day,String time,String subject,String teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,day);
        contentValues.put(COL_2,time);
        contentValues.put(COL_3,subject);
        contentValues.put(COL_4,teacher);
        db.update(TABLE_NAME, contentValues, "DAY = ? AND TIME = ? AND SUBJECT = ? AND TEACHER = ?",new String[] { day, time, subject, teacher});
        return true;
    }



    public Integer deleteData (String day, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "DAY = ? AND TIME = ?",new String[] { day , time});
    }


    public void execSQL(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }
}
