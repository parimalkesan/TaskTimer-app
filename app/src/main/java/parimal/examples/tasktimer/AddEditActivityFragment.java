package parimal.examples.tasktimer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.awt.font.TextAttribute;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddEditActivityFragment extends Fragment {
    private static final String TAG = "AddEditActivityFragment";

    public enum FragmentEditMode{EDIT,ADD};
    private FragmentEditMode mMode;

    private EditText mNameTextView;
    private EditText mDescriptionTextView;
    private EditText mSortorderTextView;
    private Button mSaveButton;
    private OnSaveClicked mSaveClicked=null;

    interface OnSaveClicked {
        void onSaveClicked();
    }


    public AddEditActivityFragment() {
        Log.d(TAG, "AddEditActivityFragment: constructor called");
    }

    public boolean canClose(){
        return false;
    }
    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: starts");
        super.onAttach(context);

        //Activities containing this fragment must implement its callbacks.
        Activity activity=getActivity();
        if(!(activity instanceof OnSaveClicked)){
            throw new ClassCastException(activity.getClass().getSimpleName()+
                    " must implement the AddEditActivityFragment.OnSaveClicked interface");
        }
        mSaveClicked=(OnSaveClicked) activity;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar=((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: starts");
        super.onDetach();
        mSaveClicked=null;
        ActionBar actionBar=((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        View view=inflater.inflate(R.layout.fragment_add_edit,container,false);

        mNameTextView=(EditText)view.findViewById(R.id.addedit_name);
        mDescriptionTextView=(EditText)view.findViewById(R.id.addedit_description);
        mSortorderTextView=(EditText)view.findViewById(R.id.addedit_sortorder);
        mSaveButton=(Button)view.findViewById(R.id.addedit_save);

        Bundle arguments=getArguments();

        final Task task;
        if(arguments!=null){
            Log.d(TAG, "onCreateView: retrieving task details");

            task=(Task) arguments.getSerializable(Task.class.getSimpleName());
            if(task!=null){
                Log.d(TAG, "onCreateView: task details found,editing");
                mNameTextView.setText(task.getName());
                mDescriptionTextView.setText(task.getDescription());
                mSortorderTextView.setText(Integer.toString(task.getSortOrder()));
                mMode=FragmentEditMode.EDIT;
            }
            else{
                //no task,adding a new task
                mMode=FragmentEditMode.ADD;
            }
        }
        else{
            task=null;
            Log.d(TAG, "onCreateView: no arguments,adding new record");
            mMode=FragmentEditMode.ADD;
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update the database if at least one field is changed
                //no database use if no field is changed

                int so;//to save repeated conversion to int

                if(mSortorderTextView.length()>0){
                    so=Integer.parseInt(mSortorderTextView.getText().toString());
                }
                else{
                    so=0;
                }

                ContentResolver contentResolver=getActivity().getContentResolver();
                ContentValues values=new ContentValues();

                switch(mMode) {
                    case EDIT:
                        if (!mNameTextView.getText().toString().equals(task.getName())) {
                            values.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                        }
                        if (!mDescriptionTextView.getText().toString().equals(task.getDescription())) {
                            values.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                        }
                        if (so != task.getSortOrder()) {
                            values.put(TasksContract.Columns.TASKS_SORTORDER, so);
                        }
                        if (values.size() != 0) {
                            Log.d(TAG, "onClick: updating task");
                            contentResolver.update(TasksContract.buildTaskUri(task.getId()), values, null, null);
                        }
                        break;
                    case ADD:
                        if (mNameTextView.length() > 0) {
                            Log.d(TAG, "onClick: adding new task");
                            values.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                            values.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                            values.put(TasksContract.Columns.TASKS_SORTORDER, so);
                            contentResolver.insert(TasksContract.CONTENT_URI, values);
                        }
                        break;

                }

                Log.d(TAG, "onClick: Done editing");
                if(mSaveClicked!=null){
                    mSaveClicked.onSaveClicked();
                }
            }
        });
        Log.d(TAG, "onCreateView: Exiting...");

        return view;
    }
}
