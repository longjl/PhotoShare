package com.photoshare;

/**
 * Created by longjianlin on 15/3/19.
 */
public class Constants {

    public static final int FACE_DETECTOR_MAX_FACES = 8;
    public static final float IMAGE_CACHE_HEAP_PERCENTAGE = 1f / 6f;

    public static final String PREF_SELECTED_MEDIA_BUCKET_ID = "selected_media_store_bucket";
    public static final long SCALE_ANIMATION_DURATION_FULL_DISTANCE = 800;

    public static final String EXTRA_POSITION = "extra_position";
    public static final String RECORD_ID = "record_id";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_BUCKET_ID = "extra_bucket_id";


    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String SINA_WEIBO = "SinaWeibo";
    public static final String SINA_WEIBO_APP = "com.sina.weibo";

    public static final String WEB_CHAT_MOMENTS = "WebChatMoments";
    public static final String WEB_CHAT_MOMENTS_APP = "com.tencent.mm";


    public static final String PREF_MOBILE = "mobile";
    public static final String PREF_PWD = "pwd";

    public static final int CLIENT_TIMEOUT = 60 * 1000;//连接超时时间 1分钟

    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    public static final String GROUP_USERNAME = "item_groups";
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";
    public static final String ACCOUNT_REMOVED = "account_removed";
}
