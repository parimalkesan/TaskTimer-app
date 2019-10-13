package parimal.examples.tasktimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class AppProvider extends ContentProvider {
    private static final String TAG = "AppProvider";

    private AppDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher=buildUriMatcher();

    static final String CONTENT_AUTHORITY = "parimal.examples.tasktimer.provider";
    public static final Uri CONTENT_AUTHORITY_URI=Uri.parse("content://"+CONTENT_AUTHORITY);

    private static final int TASKS=100;
    private static final int TASKS_ID=101;

    private static final int TIMINGS=200;
    private static final int TIMINGD_ID=201;

    //private static final int TASKS=300;
    //private static final int TASKS_ID=301;

    private static final int TASK_DURATIONS=400;
    private static final int TASK_DURATIONS_ID=401;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //  eg. content://package parimal.examples.tasktimer/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS);
        // e.g. content://package parimal.examples.tasktimer/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID);

//        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS);
//        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME + "/#", TIMINGS_ID);
//
//        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS);
//        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME + "/#", TASK_DURATIONS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper=AppDatabase.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri,String[] projection,String selection,String[] selectionArgs,String sortOrder) {
        Log.d(TAG, "query:called with URI "+uri);
        final int match = sUriMatcher.match(uri);
        Log.d(TAG, "query: match is " + match);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(match) {
            case TASKS:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                break;
            case TASKS_ID:
                queryBuilder.setTables(TasksContract.TABLE_NAME);
                long taskId = TasksContract.getTaskId(uri);
                queryBuilder.appendWhere(TasksContract.Columns._ID + " = " + taskId);
                break;

//            case TIMINGS:
//                queryBuilder.setTables(TimingsContract.TABLE_NAME);
//                break;
//            case TIMINGS_ID:
//                queryBuilder.setTables(TimingsContract.TABLE_NAME);
//                long timingId = TimingsContract.getTimingId(uri);
//                queryBuilder.appendWhere(TimingsContract.Columns._ID + " = " + timingId);
//                break;
//
//            case TASK_DURATIONS:
//                queryBuilder.setTables(DurationsContract.TABLE_NAME);
//                break;
//            case TASK_DURATIONS_ID:
//                queryBuilder.setTables(DurationsContract.TABLE_NAME);
//                long durationId = DurationsContract.getDuration(uri);
//                queryBuilder.appendWhere(DurationsContract.Columns._ID + " = " + durationId);
//                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor=queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        Log.d(TAG, "query: called");
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }



    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return TasksContract.CONTENT_TYPE;

            case TASKS_ID:
                return TasksContract.CONTENT_ITEM_TYPE;

//            case TIMINGS:
//                return TimingsContract.Timings.CONTENT_TYPE;
//
//            case TIMINGS_ID:
//                return TimingsContract.Timings.CONTENT_ITEM_TYPE;
//
//            case TASK_DURATIONS:
//                return DurationsContract.TaskDurations.CONTENT_TYPE;
//
//            case TASK_DURATIONS_ID:
//                return DurationsContract.TaskDurations.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri,ContentValues values) {
        Log.d(TAG, "insert: entering insert,called with uri: "+uri);
        final int match=sUriMatcher.match(uri);
        Log.d(TAG, "insert: match is "+match);

        final SQLiteDatabase db;

        Uri returnUri =null;
        long recordId;

        switch(match) {
            case TASKS:
                db = mOpenHelper.getWritableDatabase();
                recordId = db.insert(TasksContract.TABLE_NAME, null, values);
                if (recordId >= 0) {
                    returnUri = TasksContract.buildTaskUri(recordId);
                } else {
                    throw new android.database.SQLException("Failed to insert into " + uri.toString());
                }
                break;
//            case TIMINGS:
//                db = mOpenHelper.getWritableDatabase();
//                recordId = db.insert(TimingsContract.TABLE_NAME, null, values);
//                if (recordId >= 0) {
//                    returnUri = TimingsContract.buildTimingsUri(recordId);
//                } else {
//                    throw new android.database.SQLException("Failed to insert into " + uri.toString());
//                }
//                break;
            default:
                throw new IllegalArgumentException("unknown uri "+uri);
        }
        if(recordId>=0) {
            //something has changed
            Log.d(TAG, "insert: setting notifychange with " + uri);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        else{
            Log.d(TAG, "insert: nothing inserted");
        }

        Log.d(TAG, "insert: exiting insert,returning "+returnUri);
        return returnUri;
    }

    @Override
    public int delete(Uri uri,String selection,String[] selectionArgs) {
        Log.d(TAG, "delete: delete called with uri "+uri);
        final int match=sUriMatcher.match(uri);
        Log.d(TAG, "delete: match is "+match);

        final SQLiteDatabase db;
        int count;

        String selectionCriteria;

        switch(match){
            case TASKS:
                db=mOpenHelper.getWritableDatabase();
                count=db.delete(TasksContract.TABLE_NAME,selection,selectionArgs);
                break;

            case TASKS_ID:
                db=mOpenHelper.getWritableDatabase();
                long taskId=TasksContract.getTaskId(uri);
                selectionCriteria=TasksContract.Columns._ID+"="+taskId;

                if(selection!=null &&  selection.length()>0){
                    selectionCriteria+="AND ("+selection+")";
                }
                count=db.delete(TasksContract.TABLE_NAME,selectionCriteria,selectionArgs);
                break;

//            case TIMINGS:
//                db=mOpenHelper.getWritableDatabase();
//                count=db.delete(TimingsContract.TABLE_NAME,selection,selectionArgs);
//                break;
//
//            case TIMINGD_ID:
//                db=mOpenHelper.getWritableDatabase();
//                long timingsId=TimingsContract.getTaskId(uri);
//                selectionCriteria=TimingsContract.Columns._ID+"="+timingsId;
//
//                if(selection!=null &&  selection.length()>0){
//                    selectionCriteria+="AND ("+selection+")";
//                }
//                count=db.delete(TimingsContract.TABLE_NAME,selectionCriteria,selectionArgs);
//                break;
            default:
                throw new IllegalArgumentException("Unknown uri "+uri);
        }

        if(count>0){
            //something is deleted
            Log.d(TAG, "delete: setting notifychange with uri "+uri);
            getContext().getContentResolver().notifyChange(uri,null);
        }
        else{
            Log.d(TAG, "delete: nothing deleted");
        }
        Log.d(TAG, "delete: exiting delete, deleted "+count);
        return count;
    }

    @Override
    public int update(Uri uri,ContentValues values,String selection,String[] selectionArgs) {
        Log.d(TAG, "update: update called with uri "+uri);
        final int match=sUriMatcher.match(uri);
        Log.d(TAG, "update: match is "+match);
        
        final SQLiteDatabase db;
        int count;
        
        String selectionCriteria;
        
        switch(match){
            case TASKS:
                db=mOpenHelper.getWritableDatabase();
                count=db.update(TasksContract.TABLE_NAME,values,selection,selectionArgs);
                break;

            case TASKS_ID:
                db=mOpenHelper.getWritableDatabase();
                long taskId=TasksContract.getTaskId(uri);
                selectionCriteria=TasksContract.Columns._ID+"="+taskId;

                if(selection!=null &&  selection.length()>0){
                    selectionCriteria+="AND ("+selection+")";
                }
                count=db.update(TasksContract.TABLE_NAME,values,selectionCriteria,selectionArgs);
                break;

//            case TIMINGS:
//                db=mOpenHelper.getWritableDatabase();
//                count=db.update(TimingsContract.TABLE_NAME,values,selection,selectionArgs);
//                break;
//
//            case TIMINGD_ID:
//                db=mOpenHelper.getWritableDatabase();
//                long timingsId=TimingsContract.getTaskId(uri);
//                selectionCriteria=TimingsContract.Columns._ID+"="+timingsId;
//
//                if(selection!=null &&  selection.length()>0){
//                    selectionCriteria+="AND ("+selection+")";
//                }
//                count=db.update(TimingsContract.TABLE_NAME,values,selectionCriteria,selectionArgs);
//                break;

             default:
                 throw new IllegalArgumentException("Unknown uri "+uri);
        }

        if(count>0){
            //something is updated
            Log.d(TAG, "update: setting notifychange with uri "+uri);
            getContext().getContentResolver().notifyChange(uri,null);
        }
        else {
            Log.d(TAG, "update: nothing updated");
        }
        Log.d(TAG, "update: exiting update, updated "+count);
        return count;
    }
}


