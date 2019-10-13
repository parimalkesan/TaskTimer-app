package parimal.examples.tasktimer;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.onTaskClickListener,
                                                               AddEditActivityFragment.OnSaveClicked,
                                                               AppDialog.DialogEvents{
    private static final String TAG = "MainActivity";

    //whether or not activity is in 2-pane
    //i.e running in landscape on a tablet
    private boolean mTwoPane=false;

    private static final String ADD_EDIT_FRAGMENT=" AddEditFragment";
    public static final int DIALOG_ID_DELETE =1;
    public static final int DIALOG_ID_CANCEL_EDIT =2;

    private AlertDialog mDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //if(findViewById(R.id.task_details_container)!=null){
        //    //the detail continer will only be present in large screen layouts
         //   mTwoPane=true;
        //}

        mTwoPane = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        Log.d(TAG, "onCreate: twoPane is " + mTwoPane);

        FragmentManager fragmentManager = getSupportFragmentManager();
        // If the AddEditActivity fragment exists, we're editing
        Boolean editing = fragmentManager.findFragmentById(R.id.task_details_container) != null;
        Log.d(TAG, "onCreate: editing is " + editing);

        // We need references to the containers, so we can show or hide them as necessary.
        // No need to cast them, as we're only calling a method that's available for all views.
        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFragment = findViewById(R.id.fragment);

        if(mTwoPane) {
            Log.d(TAG, "onCreate: twoPane mode");
            mainFragment.setVisibility(View.VISIBLE);
            addEditLayout.setVisibility(View.VISIBLE);
        } else if (editing) {
            Log.d(TAG, "onCreate: single pane, editing");
            // hide the left hand fragment, to make room for editing
            mainFragment.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "onCreate: single pane, not editing");
            // Show left hand fragment
            mainFragment.setVisibility(View.VISIBLE);
            // Hide the editing frame
            addEditLayout.setVisibility(View.GONE);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        switch (id){
            case R.id.menumain_addTask:
                taskEditRequest(null);
                break;
            case R.id.menumain_showDurations:
                break;
            case R.id.menumain_settings:
                break;
            case R.id.menumain_showAbout:
                showAboutDialog();
                break;
            case R.id.menumain_generate:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAboutDialog(){
        View messageView=getLayoutInflater().inflate(R.layout.about,null,false);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(messageView);

        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        mDialog=builder.create();
        mDialog.setCanceledOnTouchOutside(true);

        TextView tv=(TextView)messageView.findViewById(R.id.about_version);
        tv.setText("v"+BuildConfig.VERSION_NAME);

        mDialog.show();
    }


    private void taskEditRequest(Task task){
        Log.d(TAG, "taskEditRequest: starts");
        Log.d(TAG, "taskEditRequest: in two-pane mode(tablet)");
        AddEditActivityFragment fragment=new AddEditActivityFragment();

        Bundle arguments=new Bundle();
        arguments.putSerializable(Task.class.getSimpleName(),task);
        fragment.setArguments(arguments);

        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.task_details_container,fragment);
        fragmentTransaction.commit();

        if(!mTwoPane) {
            Log.d(TAG, "taskEditRequest: in single-pane mode (phone)");
            // Hide the left hand fragment and show the right hand frame
            View mainFragment = findViewById(R.id.fragment);
            View addEditLayout = findViewById(R.id.task_details_container);
            mainFragment.setVisibility(View.GONE);
            addEditLayout.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Exiting taskEditRequest");
    }

    @Override
    public void onEditClick(Task task) {
        Log.d(TAG, "onEditClick: starts");
        taskEditRequest(task);

    }

    @Override
    public void onDeleteClick(Task task) {
        Log.d(TAG, "onDeleteClick: starts");

        AppDialog dialog=new AppDialog();
        Bundle args=new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
        args.putString(AppDialog.DIALOG_MESSAGE,"Deleting will also delete all timing data associated wth the task "+task.getName());
        args.putInt(AppDialog.DIALOG_POSITIVE_RID,R.string.deldiag_postive_caption);

        args.putLong(AppDialog.TASKID,task.getId());

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),null);

        //getContentResolver().delete(TasksContract.buildTaskUri(task.getId()),null,null);

    }

    @Override
    public void onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts");
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=fragmentManager.findFragmentById(R.id.task_details_container);
        if(fragment!=null){
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }
        View addEditLayout = findViewById(R.id.task_details_container);
        View mainFragment = findViewById(R.id.fragment);

        if(!mTwoPane) {
            // We've just removed the editing fragment, so hide the frame
            addEditLayout.setVisibility(View.GONE);

            // and make sure the MainActivityFragment is visible.
            mainFragment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: called");
        switch (dialogId) {
            case DIALOG_ID_DELETE:
                long taskId = args.getLong(AppDialog.TASKID);
                if (BuildConfig.DEBUG && taskId == 0) {
                    throw new AssertionError("Task id is zero");
                }
                getContentResolver().delete(TasksContract.buildTaskUri(taskId), null, null);
                break;
            case DIALOG_ID_CANCEL_EDIT:
                //
                break;
        }
    }


    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeDialogResult: called");
        switch (dialogId){
            case DIALOG_ID_DELETE:
                //no action required
                break;
            case DIALOG_ID_CANCEL_EDIT:
                finish();
                break;
        }
    }

    @Override
    public void onDialogCancelled(int dialogId) {
        Log.d(TAG, "onDialogCancelled: called");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.task_details_container);
        if((fragment == null)||fragment.canClose() ) {
            super.onBackPressed();
        } else {
            // show dialogue to get confirmation to quit editing
            AppDialog dialog = new AppDialog();
            Bundle args = new Bundle();
            args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
            args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancelEditDiag_message));
            args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancelEditDiag_positive_caption);
            args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancelEditDiag_negative_caption);

            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(),null);
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: starts");
        super.onStop();
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.d(TAG, "onAttachFragment: called, fragment is " + fragment.toString());
        super.onAttachFragment(fragment);
    }
}
