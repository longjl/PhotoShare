package com.photoshare.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 用户信息表
 * Created by longjianlin on 15/3/19.
 */
@DatabaseTable(tableName = "account")
public class Account {
    @DatabaseField(generatedId = true)
    public int _id;  //主键

    @DatabaseField(columnName = "acc_id")
    public String acc_id; //mobile 的MD5

    @DatabaseField(columnName = "nickname")
    public String nickname;//昵称

    @DatabaseField(columnName = "sex")
    public String sex;//性别

    @DatabaseField(columnName = "email")
    public String email;//电子邮件

    @DatabaseField(columnName = "use_data")
    public String use_data;//有效期

    @DatabaseField(columnName = "register_date")
    public String register_date;//注册日期
}
