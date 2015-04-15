package com.photoshare.dao;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.photoshare.Flags;
import com.photoshare.PhotoApplication;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;
import com.photoshare.tasks.PhotoThreadRunnable;
import com.photoshare.util.DatabaseHelper;

import java.sql.SQLException;
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
                            OpenHelperManager.releaseHelper();
                        }
                    }
                }
        );
    }

    /**
     * 根据record_id查询历史记录
     *
     * @param context
     * @param record_id
     * @return
     */
    public static List<History> findByRecordId(Context context, int record_id) {
        final DatabaseHelper helper = getHelper(context);
        List<History> histories = null;
        try {
            final Dao<History, Integer> dao = helper.getHistoryDao();
            QueryBuilder builder = dao.queryBuilder();
            builder.where().eq("record_id", record_id);
            histories = dao.query(builder.prepare());
        } catch (SQLException e) {
            if (Flags.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return histories;
    }

    /**
     * 根据record_id 删除Record
     *
     * @param context
     * @param record_id
     */
    public static void deleteHistory(final Context context, final int record_id) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {
                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            final Dao<History, Integer> dao = helper.getHistoryDao();
                            final DeleteBuilder<History, Integer> deleteBuilder = dao
                                    .deleteBuilder();
                            deleteBuilder.where().eq("record_id", record_id).prepare();
                            dao.delete(deleteBuilder.prepare());
                        } catch (SQLException e) {
                            if (Flags.DEBUG) {
                                e.printStackTrace();
                            }
                        } finally {
                            OpenHelperManager.releaseHelper();
                        }
                    }
                });
    }
}
