package parimal.examples.tasktimer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class AddEditActivity extends AppCompatActivity implements AddEditActivityFragment.OnSaveClicked,AppDialog.DialogEvents {
    private static final String TAG = "AddEditActivity";

    public static final int DIALOG_ID_CANCEL_EDIT=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState==null){
            FragmentManager fragmentManager=getSupportFragmentManager();
        //if(fragmentManager.findFragmentById(R.id.fragment)==null){
            AddEditActivityFragment fragment=new AddEditActivityFragment();

            Bundle arguments=getIntent().getExtras();
            fragment.setArguments(arguments);

            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment,fragment);
            fragmentTransaction.commit();
        }
    }


    @Override
    public void onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts");
        finish();
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onPositiveDialogResult: called");

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        Log.d(TAG, "onNegativeDialogResult: called");
        finish();

    }

    @Override
    public void onDialogCancelled(int dialogId) {
        Log.d(TAG, "onDialogCancelled: called");

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddEditActivityFragment fragment = (AddEditActivityFragment) fragmentManager.findFragmentById(R.id.fragment);
        if((fragment.canClose())){
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: on home button pressed");
                AddEditActivityFragment fragment = (AddEditActivityFragment)
                        getSupportFragmentManager().findFragmentById(R.id.fragment);
                if (fragment.canClose()) {
                    return super.onOptionsItemSelected(item);
                } else {
                    showConfirmationDialog();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showConfirmationDialog(){
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
