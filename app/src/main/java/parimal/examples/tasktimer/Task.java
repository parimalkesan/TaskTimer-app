package parimal.examples.tasktimer;

import android.print.PrinterId;

import java.io.Serializable;

class Task implements Serializable {
    public static final long serialVersionUID=20161120L;

    private long mId;
    private final String mName;
    private final String mDescription;
    private final int mSortOrder;

    public Task(long id,String name, String description, int sortOrder) {
        mId=id;
        mName = name;
        mDescription = description;
        mSortOrder = sortOrder;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getSortOrder() {
        return mSortOrder;
    }

    public void setId(long id) {
        mId = id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mSortOrder=" + mSortOrder +
                '}';
    }
}
