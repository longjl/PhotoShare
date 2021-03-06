package com.photoshare.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.photoshare.Flags;
import com.photoshare.model.Account;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;

import java.sql.SQLException;

/**
 * Created by longjianlin on 15/3/19.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final Class<?>[] DATA_CLASSES = {Photo.class, Record.class, Account.class, History.class};
    public static final String DATABASE_NAME = "photo.db";      //数据库名字/sdcard/photoshare/
    private static final int DATABASE_VERSION = 1;             //版本号

    private Dao<Photo, String> mPhotoDao = null;
    private Dao<Record, Integer> mRecordDao = null;
    private Dao<Account, Integer> mAccountDao = null;
    private Dao<History, Integer> mHistoryDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable
     * statements here to create the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            if (Flags.DEBUG) {
                Log.i(DatabaseHelper.class.getName(), "onCreate");
            }
            for (Class<?> dataClass : DATA_CLASSES) {
                TableUtils.createTable(connectionSource, dataClass);
            }

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows
     * you to adjust the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
                          int newVersion) {
        try {
            if (Flags.DEBUG) {
                Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            }
            for (Class<?> dataClass : DATA_CLASSES) {
                TableUtils.dropTable(connectionSource, dataClass, true);
            }

            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Photo dao
     *
     * @return
     * @throws SQLException
     */
    public Dao<Photo, String> getPhotoDao() throws SQLException {
        if (mPhotoDao == null) {
            mPhotoDao = getDao(Photo.class);
        }
        return mPhotoDao;
    }

    /**
     * Record dao
     *
     * @return
     * @throws SQLException
     */
    public Dao<Record, Integer> getRecordDao() throws SQLException {
        if (mRecordDao == null) {
            mRecordDao = getDao(Record.class);
        }
        return mRecordDao;
    }

    /**
     * Account dao
     *
     * @return
     * @throws SQLException
     */
    public Dao<Account, Integer> getAccountDao() throws SQLException {
        if (mAccountDao == null) {
            mAccountDao = getDao(Account.class);
        }
        return mAccountDao;
    }


    /**
     * History dao
     *
     * @return
     * @throws SQLException
     */
    public Dao<History, Integer> getHistoryDao() throws SQLException {
        if (mHistoryDao == null) {
            mHistoryDao = getDao(History.class);
        }
        return mHistoryDao;
    }


    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        mPhotoDao = null;
        mRecordDao = null;
        mAccountDao = null;
        mHistoryDao = null;
        super.close();
    }
}
