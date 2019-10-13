package parimal.examples.tasktimer;

//basic database class for aplication
//only to be used by AppProvider

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class AppDatabase extends SQLiteOpenHelper {
    private static final String TAG = "AppDatabase";

    public static final String DATABASE_NAME = "TaskTimer.db";
    public static final int DATABASE_VERSION = 1;
    //implement AppDatabase as a singleton
    private static AppDatabase instance = null;

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "AppDatabase: constructor");

    }

    //return a SQLite database helper object
    static AppDatabase getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "getInstance: creating new instance");
            instance = new AppDatabase(context);
            Log.d(TAG, "getInstance: created");
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: starts");
        //String sSQL = " CREATE TABLE Tasks(_id INTEGER PRIMARY KEY NOT NULL,Name TEXT NOT NULL,Description TEXT,SortOrder INTEGER);";
        String sSQL="CREATE TABLE "+TasksContract.TABLE_NAME+" ("+
               TasksContract.Columns._ID+" INTEGER PRIMARY KEY NOT NULL, "
                +TasksContract.Columns.TASKS_NAME+" TEXT NOT NULL, "
                +TasksContract.Columns.TASKS_DESCRIPTION+" TEXT, "+
                TasksContract.Columns.TASKS_SORTORDER+" INTEGER);";
        Log.d(TAG, "onCreate: "+sSQL);
        db.execSQL(sSQL);
        Log.d(TAG, "onCreate: ends");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: starts");
        switch(oldVersion) {
            case 1:
                // upgrade logic from version 1
                break;
            default:
                throw new IllegalStateException("onUpgrade() with unknown newVersion: " + newVersion);
        }
        Log.d(TAG, "onUpgrade: ends");

    }
}
