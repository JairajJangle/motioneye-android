package com.jairaj.janglegmail.motioneye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Devices.db";
    private static final String TABLE_NAME = "device_detail_table";
    //private static final String COL_1 = "ID";
    private static final String COL_2 = "LABEL";
    private static final String COL_3 = "URL";
    private static final String COL_4 = "PORT";
    private static final String COL_5 = "DRIVE";
    private static final String COL_6 = "PREV";

    DataBase(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME
                +"(ID INTEGER PRIMARY KEY AUTOINCREMENT,LABEL TEXT,URL TEXT,PORT TEXT,DRIVE TEXT,PREV TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);

        if (newVersion > oldVersion)
        {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_6 + " TEXT");
        }
    }

    void insertNewColumn()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        if(!existsColumnInTable(db, TABLE_NAME, COL_6))
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_6 + " TEXT " + " DEFAULT 0" );
    }

    boolean insertData(String label,String url,String port, String drive, String prev)
    {
        insertNewColumn();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_2,label);
        contentValues.put(COL_3,url);
        contentValues.put(COL_4,port);
        contentValues.put(COL_5,drive);
        contentValues.put(COL_6,prev);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        return result != -1;
    }

    private boolean existsColumnInTable(SQLiteDatabase inDatabase, String inTable, String columnToCheck)
    {
        Cursor mCursor = null;
        try
        {
            // Query 1 row
            mCursor = inDatabase.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
            Log.d("existsColumnInTable", "When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }

    Cursor getAllData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME,null);
    }

    String getUrl_from_Label(String sch_label)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String url = "";
        try
        {
            cursor = db.rawQuery("select URL from " + TABLE_NAME + " where LABEL=?", new String[]{sch_label + ""});
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                url = cursor.getString(cursor.getColumnIndex("URL"));
            }
            return url;
        }
        finally
        {
            if(cursor != null)
            cursor.close();
        }
    }

    String getPort_from_Label(String sch_label)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String port = "";
        try
        {
            cursor = db.rawQuery("select PORT from " + TABLE_NAME + " where LABEL=?", new String[]{sch_label + ""});
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                port = cursor.getString(cursor.getColumnIndex("PORT"));
            }

            if(port != null)
                return port;
            else
                return "";
        }
        finally
        {
            if(cursor != null)
                cursor.close();
        }
    }

    String getDrive_from_Label(String sch_label)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String drive_link = "";
        try
        {
            cursor = db.rawQuery("select * from "+TABLE_NAME,null);
            if(cursor.getColumnIndex("DRIVE") != -1)
            {
                cursor = db.rawQuery("select DRIVE from " + TABLE_NAME + " where LABEL=?", new String[]{sch_label + ""});
                if (cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    drive_link = cursor.getString(cursor.getColumnIndex("DRIVE"));
                }
                if (drive_link != null)
                    return drive_link;
                else
                    return "";
            }
            else {
                return "";
            }
        }
        finally
        {
            if(cursor != null)
                cursor.close();
        }
    }

    String getPrevStat_from_Label(String sch_label)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String prev = "";
        try
        {
            cursor = db.rawQuery("select PREV from " + TABLE_NAME + " where LABEL=?", new String[]{sch_label + ""});
            if (cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                prev = cursor.getString(cursor.getColumnIndex("PREV"));
            }

            if(prev != null)
                return prev;
            else
                return "";
        }
        finally
        {
            if(cursor != null)
                cursor.close();
        }
    }

    boolean updateData(String key_label, String label,String url,String port, String drive)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,label);
        contentValues.put(COL_3,url);
        contentValues.put(COL_4,port);
        contentValues.put(COL_5,drive);
        db.update(TABLE_NAME, contentValues, "LABEL = ?",new String[] { key_label });
        return true;
    }

    boolean updatePrevStat(String key_label, String prev_stat)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_6,prev_stat);
        db.update(TABLE_NAME, contentValues, "LABEL = ?",new String[] { key_label });
        return true;
    }

    Integer deleteData (String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "LABEL = ?",new String[] {id});
    }
}
