package com.photoshare.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.photoshare.dao.RecordDatabaseHelper;
import com.photoshare.model.Record;

/**
 * Created by longjianlin on 15/4/7.
 */
public class RecordAsyncTask extends AsyncTask<Void, Void, Record> {

    public interface RecordResultListener {//结果

        public void onRecordLoaded(Record record);
    }

    private Context mContext;
    private int record_id = -1;
    private RecordResultListener mListener;

    public static void execute(Context context, RecordResultListener listener, int r_id) {
        new RecordAsyncTask(context, listener, r_id).execute();
    }

    private RecordAsyncTask(Context context, RecordResultListener listener, int r_id) {
        mContext = context;
        mListener = listener;
        record_id = r_id;
    }

    @Override
    protected Record doInBackground(Void... params) {
        return RecordDatabaseHelper.getRecord(mContext, record_id);
    }

    @Override
    protected void onPostExecute(Record result) {
        super.onPostExecute(result);
        if (null != mListener) {
            mListener.onRecordLoaded(result);
        }
    }
}
