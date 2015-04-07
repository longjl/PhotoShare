package com.photoshare.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.photoshare.dao.RecordDatabaseHelper;
import com.photoshare.model.Record;

import java.util.List;

/**
 * Created by longjianlin on 15/4/7.
 */
public class RecordAsyncTask extends AsyncTask<Void, Void, List<Record>> {

    public interface RecordResultListener {//结果

        public void onRecordsLoaded(List<Record> records);
    }

    private Context mContext;
    private String mAccId;
    private RecordResultListener mListener;

    public static void execute(Context context, RecordResultListener listener, String acc_id) {
        new RecordAsyncTask(context, listener, acc_id).execute();
    }

    private RecordAsyncTask(Context context, RecordResultListener listener, String acc_id) {
        mContext = context;
        mListener = listener;
        mAccId = acc_id;
    }

    @Override
    protected List<Record> doInBackground(Void... params) {
        return RecordDatabaseHelper.getRecords(mContext, mAccId);
    }

    @Override
    protected void onPostExecute(List<Record> result) {
        super.onPostExecute(result);
        if (null != mListener) {
            mListener.onRecordsLoaded(result);
        }
    }
}
