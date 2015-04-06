package com.photoshare.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 记录信息表
 * Created by longjianlin on 15/4/5.
 */
@DatabaseTable(tableName = "record")
public class Record {
    @DatabaseField(generatedId = true)
    public int _id;//主键

    @DatabaseField(columnName = "acc_id")
    public String acc_id;

    @DatabaseField(columnName = "content")
    public String content;//分享内容

    @DatabaseField(columnName = "date")
    public String date;//分享日期
}
