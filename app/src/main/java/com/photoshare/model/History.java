package com.photoshare.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 历史记录表
 * Created by longjianlin on 15/4/6.
 */
@DatabaseTable(tableName = "history")
public class History {
    public static final String FIELD_STATE = "state";
    static final String FIELD_URI = "uri";
    static final String FIELD_COMPLETED_DETECTION = "tag_detection";
    static final String FIELD_USER_ROTATION = "user_rotation";
    static final String FIELD_FILTER = "filter";
    static final String FIELD_CROP_L = "crop_l";
    static final String FIELD_CROP_T = "crop_t";
    static final String FIELD_CROP_R = "crop_r";
    static final String FIELD_CROP_B = "crop_b";
    static final String FIELD_ACCOUNT_ID = "acc_id";          //用户编号
    static final String FIELD_TARGET_ID = "target_id";
    static final String FIELD_QUALITY = "quality";          //清晰度
    static final String FIELD_RESULT_POST_ID = "r_post_id";

    @DatabaseField(generatedId = true)
    public int _id;//主键

    @DatabaseField(columnName = "record_id")
    public int record_id;//信息表id

    @DatabaseField(columnName = FIELD_COMPLETED_DETECTION)
    public boolean mCompletedDetection;
    @DatabaseField(columnName = FIELD_USER_ROTATION)
    public int mUserRotation;
    @DatabaseField(columnName = FIELD_FILTER)
    public Filter mFilter;
    @DatabaseField(columnName = FIELD_CROP_L)
    public float mCropLeft;
    @DatabaseField(columnName = FIELD_CROP_T)
    public float mCropTop;
    @DatabaseField(columnName = FIELD_CROP_R)
    public float mCropRight;
    @DatabaseField(columnName = FIELD_CROP_B)
    public float mCropBottom;

    @DatabaseField(columnName = FIELD_ACCOUNT_ID)
    public String mAccountId;
    @DatabaseField(columnName = FIELD_TARGET_ID)
    public String mTargetId;
    @DatabaseField(columnName = FIELD_QUALITY)
    public UploadQuality mQuality;
    @DatabaseField(columnName = FIELD_RESULT_POST_ID)
    public String mResultPostId;
    @DatabaseField(columnName = FIELD_STATE)
    public int mState;
    @DatabaseField(columnName = FIELD_URI)
    public String mFullUriString;
}
