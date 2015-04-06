package com.photoshare.dao;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.photoshare.Flags;
import com.photoshare.PhotoApplication;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;
import com.photoshare.tasks.PhotoThreadRunnable;
import com.photoshare.util.DatabaseHelper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoDatabaseHelper {
    public static List<Photo> getSelected(Context context) {
        final DatabaseHelper helper = getHelper(context);
        List<Photo> uploads = null;

        try {
            final Dao<Photo, String> dao = helper.getPhotoDao();
            uploads = dao.query(dao.queryBuilder().where()
                    .eq(Photo.FIELD_STATE, Photo.STATE_SELECTED)
                    .prepare());
        } catch (SQLException e) {
            if (Flags.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return uploads;
    }

    private static DatabaseHelper getHelper(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    public static List<Photo> getUploads(Context context) {
        final DatabaseHelper helper = getHelper(context);
        List<Photo> uploads = null;

        try {
            final Dao<Photo, String> dao = helper.getPhotoDao();
            uploads = dao.query(dao.queryBuilder().where()
                    .ge(Photo.FIELD_STATE, Photo.STATE_UPLOAD_WAITING).prepare());
        } catch (SQLException e) {
            if (Flags.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return uploads;
    }


    public static void deleteAllSelected(final Context context) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {

                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            final Dao<Photo, String> dao = helper.getPhotoDao();
                            final DeleteBuilder<Photo, String> deleteBuilder = dao
                                    .deleteBuilder();
                            deleteBuilder.where()
                                    .le(Photo.FIELD_STATE, Photo.STATE_SELECTED);
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

    public static void deleteFromDatabase(final Context context, final Photo upload) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {

                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            Dao<Photo, String> dao = helper.getPhotoDao();
                            dao.delete(upload);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            OpenHelperManager.releaseHelper();
                        }
                    }
                });
    }

    public static void deleteFromDatabase(final Context context, final List<Photo> uploads) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {

                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            Dao<Photo, String> dao = helper.getPhotoDao();
                            for (Photo upload : uploads) {
                                dao.delete(upload);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            OpenHelperManager.releaseHelper();
                        }
                    }
                });
    }

    public static void saveToDatabase(final Context context, final Photo photo) {
        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {

                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            Dao<Photo, String> dao = helper.getPhotoDao();
                            dao.createOrUpdate(photo);
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

    public static void saveToDatabase(final Context context, List<Photo> uploads,
                                      final boolean forceUpdate) {
        final ArrayList<Photo> uploadsCopy = new ArrayList<Photo>();
        uploadsCopy.addAll(uploads);

        PhotoApplication.getApplication(context).getDatabaseThreadExecutorService()
                .submit(new PhotoThreadRunnable() {

                    public void runImpl() {
                        final DatabaseHelper helper = getHelper(context);
                        try {
                            final Dao<Photo, String> dao = helper.getPhotoDao();
                            dao.callBatchTasks(new Callable<Void>() {
                                public Void call() throws Exception {

                                    for (Photo upload : uploadsCopy) {
                                        if (forceUpdate || upload.requiresSaving()) {
                                            dao.createOrUpdate(upload);
                                            upload.resetSaveFlag();
                                        }
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


}
