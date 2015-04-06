package com.photoshare.dao;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.photoshare.Flags;
import com.photoshare.PhotoApplication;
import com.photoshare.model.History;
import com.photoshare.tasks.PhotoThreadRunnable;
import com.photoshare.util.DatabaseHelper;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by longjianlin on 15/4/6.
 */
public class HistoryDatabaseHelper {
    private static DatabaseHelper getHelper(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    /**
     * 保存历史信息到数据库
     *
     * @param context
     * @param histories
     */
    public static void saveToDatabase(final Context context, final List<History> histories) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService().submit(
                new PhotoThreadRunnable() {
                    @Override
                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            final Dao<History, Integer> dao = helper.getHistoryDao();
                            dao.callBatchTasks(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    for (History history : histories) {
                                        dao.createOrUpdate(history);
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            if (Flags.DEBUG) {
                                e.printStackTrace();
                            }
                        } finally {

                        }
                    }
                }
        );
    }
}
