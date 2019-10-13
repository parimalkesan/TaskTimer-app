package parimal.examples.tasktimer;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
    private static final String TAG = "CursorRecyclerViewAdapt";
    private Cursor mCursor;
    private onTaskClickListener mListener;

    interface onTaskClickListener{
        void onEditClick(Task task);
        void onDeleteClick(Task task);
    }

    public CursorRecyclerViewAdapter(Cursor cursor,onTaskClickListener listener) {
        Log.d(TAG, "CursorRecyclerViewAdapter: Constructor called");
        mCursor = cursor;
        mListener=listener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder: new view requested");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_task_items, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder: starts");

        if ((mCursor == null) || (mCursor.getCount() == 0)) {
            Log.d(TAG, "onBindViewHolder: providing instructions");

            holder.name.setText("Instructions");

            holder.description.setText("Use the add button (+) in the toolbar above to create new tasks." +
                    "\n\nTasks with lower sort orders will be placed higher up the list." +
                    "Tasks with the same sort order will be sorted alphabetically." +
                    "\n\nTapping a task will start the timer for that task (and will stop the timer for any previous task that was being timed)." +
                    "\n\nEach task has Edit and Delete buttons if you eant to change the details or remove the task.");

            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("Couldn't move cursor to position " + position);
            }

            final Task task=new Task(mCursor.getLong(mCursor.getColumnIndex(TasksContract.Columns._ID)),
            mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_DESCRIPTION)),
            mCursor.getInt(mCursor.getColumnIndex(TasksContract.Columns.TASKS_SORTORDER)));


            holder.name.setText(task.getName());
            holder.description.setText(task.getDescription());
            holder.editButton.setVisibility(View.VISIBLE);  // TODO add onClick listener
            holder.deleteButton.setVisibility(View.VISIBLE); // TODO add onClick listener

            View.OnClickListener buttonListener=new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: starts");
                    switch(view.getId()){
                        case R.id.lti_edit:
                            if(mListener!=null){
                            mListener.onEditClick(task);
                            }
                            break;
                        case R.id.lti_delete:
                            if (mListener != null) {
                                mListener.onDeleteClick(task);
                            }
                            break;
                        default:
                            Log.d(TAG, "onClick: found unexpected button id");
                    }

                    //Log.d(TAG, "onClick: button with id "+view.getId()+" clicked");
                    //Log.d(TAG, "onClick: task name is "+task.getName());
                }
            };

            holder.editButton.setOnClickListener(buttonListener);
            holder.deleteButton.setOnClickListener(buttonListener);
        }
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "getItemCount: starts");
        if(mCursor==null||mCursor.getCount()==0){
            return 1;//we populate a single one with instructions
        }
        else {
            return mCursor.getCount();
        }
    }

    Cursor swapCursor(Cursor newCursor)
    {
        if(newCursor==mCursor){
            return null;
        }
        final Cursor oldCursor=mCursor;
        mCursor=newCursor;
        if(newCursor!=null){
            notifyDataSetChanged();
        }
        else{
            notifyItemRangeRemoved(0,getItemCount());
        }
        return oldCursor;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "TaskViewHolder";

        TextView name = null;
        TextView description = null;
        ImageButton editButton = null;
        ImageButton deleteButton = null;

        public TaskViewHolder(View itemView) {
            super(itemView);
            //Log.d(TAG, "TaskViewHolder: starts");

            this.name = (TextView) itemView.findViewById(R.id.lti_name);
            this.description = (TextView) itemView.findViewById(R.id.lti_description);
            this.editButton = (ImageButton) itemView.findViewById(R.id.lti_edit);
            this.deleteButton = (ImageButton) itemView.findViewById(R.id.lti_delete);
        }
    }
}
