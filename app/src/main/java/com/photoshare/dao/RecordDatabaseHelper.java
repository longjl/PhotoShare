package com.photoshare.dao;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.photoshare.Flags;
import com.photoshare.PhotoApplication;
import com.photoshare.model.History;
import com.photoshare.model.Record;
import com.photoshare.tasks.PhotoThreadRunnable;
import com.photoshare.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by longjianlin on 15/4/6.
 */
public class RecordDatabaseHelper {

    private static DatabaseHelper getHelper(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    /**
     * 保存图片记录
     *
     * @param context
     */
    public static void saveRecordToDatabase(final Context context, final Record record, final List<History> histories) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {
                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            final Dao<Record, Integer> dao = helper.getRecordDao();
                            dao.callBatchTasks(new Callable<Void>() {
                                public Void call() throws Exception {
                                    Record r = dao.createIfNotExists(record);
                                    if (null != r) {
                                        saveHistoryToDatabase(context, histories, r._id);
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            if (Flags.DEBUG) {
                                e.printStackTrace();
                            }
                        } finally {
                            OpenHelperManager.releaseHelper();
                        }
                    }
                });
    }

    /**
     * 保存历史记录
     *
     * @param context
     * @param histories
     * @param record_id
     */
    public static void saveHistoryToDatabase(final Context context, List<History> histories, final int record_id) {
        List<History> h = new ArrayList<History>();
        for (History history : histories) {
            history.record_id = record_id;
            h.add(history);
        }
        HistoryDatabaseHelper.saveToDatabase(context, h);
    }

}
