package com.photoshare.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.photoshare.dao.RecordDatabaseHelper;
import com.photoshare.model.Record;

import java.util.List;

/**
 * Created by longjianlin on 15/4/11.
 */
public class HistoryAsyncTask extends AsyncTask<Void, Void, List<Record>> {

    public interface HistoryResultListener {//历史记录

        public void onRecordsLoaded(List<Record> records);
    }


    private Context mContext;
    private String mAccId;
    private String mDate;
    private int mGesture;
    private HistoryResultListener mListener;


    public static void execute(Context context, HistoryResultListener listener, String acc_id, String date, int gesture) {
        new HistoryAsyncTask(context, listener, acc_id, date, gesture).execute();
    }

    private HistoryAsyncTask(Context context, HistoryResultListener listener, String acc_id, String date, int gesture) {
        mContext = context;
        mListener = listener;
        mAccId = acc_id;
        mDate = date;
        mGesture = gesture;
    }

    @Override
    protected List<Record> doInBackground(Void... params) {
        return RecordDatabaseHelper.findRecords(mContext, mAccId, mDate, mGesture);
    }

    @Override
    protected void onPostExecute(List<Record> result) {
        super.onPostExecute(result);
        if (null != mListener) {
            mListener.onRecordsLoaded(result);
        }
    }
}
