package com.photoshare.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.photoshare.PhotoApplication;
import com.photoshare.R;
import com.photoshare.adapters.HistoryAdapter;
import com.photoshare.adapters.ShareAdapter;
import com.photoshare.dao.RecordDatabaseHelper;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;
import com.photoshare.tasks.HistoryAsyncTask;
import com.photoshare.util.ShareUtils;
import com.photoshare.views.PhotoListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 往事
 * Created by longjianlin on 15/4/11.
 */
public class HistoryFragment extends SherlockFragment implements HistoryAsyncTask.HistoryResultListener,
        PullToRefreshBase.OnRefreshListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private PullToRefreshScrollView mPullRefreshScrollView;
    private PhotoListView mPhotoListView;

    private PopupWindow popupWindow;
    private View popupWindowView;
    private GridView gv_share;
    private ShareAdapter shareAdapter;

    private HistoryAdapter adapter;
    private LinkedList<Record> records = new LinkedList<Record>();
    private boolean isInit; // 是否可以开始加载数据
    private int gesture = 0;//如果gesture=1表示下拉 ,gesture=0表示上推,gesture=200表示自由查询
    private String currentDate;

    private static Context mContext;
    private Map<Integer, Record> map = new HashMap<Integer, Record>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean flag = true;//表示第一次刷新

    private static PhotoApplication app;

    public static HistoryFragment newInstance(Context context) {
        mContext = context;
        return new HistoryFragment();
    }

    public HistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * 初始化组件
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        app = PhotoApplication.getApplication(mContext);
        isInit = true;
        currentDate = format.format(new Date());

        mPullRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.pull_refresh_scrollview);
        mPullRefreshScrollView.setOnRefreshListener(this);//刷新监听
        mPhotoListView = (PhotoListView) view.findViewById(R.id.photo_list_view);
        mPhotoListView.setOnItemClickListener(this);
        mPhotoListView.setOnItemLongClickListener(this);
        adapter = new HistoryAdapter(getActivity(), records);
        mPhotoListView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * 当ViewPager每次切换的时候都会执行setUserVisibleHint()方法
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        gesture = 200;
        // 每次切换fragment时调用的方法
        if (isVisibleToUser) {
            //Toast.makeText(mContext, "刷新数据", Toast.LENGTH_SHORT).show();
            initData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 判断当前fragment是否显示
        if (getUserVisibleHint()) {
            initData();
        }
    }

    /**
     * 初始化数据
     *
     * @date 2014-1-16
     */
    private void initData() {
        if (isInit) {
            isInit = false;//加载数据完成
            sendMessage(currentDate, gesture);
        }
    }

    /**
     * 发送消息
     *
     * @param date
     * @param gesture
     */
    private void sendMessage(String date, int gesture) {
        Message msg = new Message();
        msg.what = 200;
        msg.obj = date;
        msg.arg1 = gesture;
        queryHandler.sendMessage(msg);
    }

    /**
     * 从数据库中获取Record数据
     */
    private void getRecordToDatabase(String date, int gesture) {
        HistoryAsyncTask.execute(mContext, this, app.account.acc_id, date, gesture);
    }

    private Handler queryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 200) {
                getRecordToDatabase(msg.obj.toString(), msg.arg1);
            }
        }
    };

    /**
     * 数据回调函数
     *
     * @param records
     */
    @Override
    public void onRecordsLoaded(List<Record> records) {
        if (records != null && records.size() > 0) {
            for (Record record : records) {
                if (!map.containsKey(record._id)) {
                    if (gesture == 1) {
                        this.records.addFirst(record);
                    } else if (gesture == 0) {
                        this.records.addLast(record);
                    } else {
                        if (flag) {
                            this.records.add(record);
                        } else {
                            this.records.addFirst(record);
                        }
                    }
                    map.put(record._id, record);
                }
            }
            adapter.notifyDataSetChanged();
            flag = false;
        }
        handler.sendEmptyMessage(200);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        if (records == null || records.size() == 0) {
            handler.sendEmptyMessage(200);
            return;
        }
        Record record = null;
        if (refreshView.getCurrentMode().showHeaderLoadingLayout()) {//下拉
            gesture = 1;
            record = records.getFirst();
            currentDate = record.date;
        } else if (refreshView.getCurrentMode().showFooterLoadingLayout()) {//上推
            gesture = 0;
            record = records.getLast();
            currentDate = record.date;
        } else {
            currentDate = format.format(new Date());
        }
        sendMessage(currentDate, gesture);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 200) {
                mPullRefreshScrollView.onRefreshComplete();
            }
        }
    };

    /**
     * 弹出分享PopupWindow
     */
    private void showSharePopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popupWindowView = inflater.inflate(R.layout.fragment_popupwindow_share, null);
        popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        ColorDrawable cd = new ColorDrawable(0x000000);
        popupWindow.setBackgroundDrawable(cd);

        //产生背景变暗效果
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.4f;
        getActivity().getWindow().setAttributes(lp);

        //设置PopupWindow的弹出和消失效果
        popupWindow.setAnimationStyle(R.style.popupAnimation);
        popupWindow.update();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });
        gv_share = (GridView) popupWindowView.findViewById(R.id.gv_share);
        if (shareAdapter == null) {
            shareAdapter = new ShareAdapter(getActivity());
        }
        gv_share.setAdapter(shareAdapter);
        gv_share.setOnItemClickListener(this);

        popupWindow.showAtLocation(gv_share, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mPhotoListView.getId() == parent.getId()) {
            showSharePopupWindow();
        } else if (gv_share != null && gv_share.getId() == parent.getId()) {
            String platform = shareAdapter.getItem(position);
            uninstallSoftware(platform, adapter.getItem(position));
        }
    }

    private void uninstallSoftware(String platform, Record record) {
        PhotoApplication app = PhotoApplication.getApplication(getActivity());
        if (record.histories.size() == 0) {
            Toast.makeText(mContext, "没有照片可分享", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Photo> photos = new ArrayList<Photo>();
        for (History history : record.histories) {
            Photo photo = new Photo();
            photo.mCompletedDetection = history.mCompletedDetection;
            photo.mUserRotation = history.mUserRotation;
            photo.mFilter = history.mFilter;
            photo.mCropLeft = history.mCropLeft;
            photo.mCropTop = history.mCropTop;
            photo.mCropRight = history.mCropRight;
            photo.mCropBottom = history.mCropBottom;
            photo.mAccountId = history.mAccountId;
            photo.mTargetId = history.mTargetId;
            photo.mQuality = history.mQuality;
            photo.mResultPostId = history.mResultPostId;
            photo.mState = history.mState;
            photo.mFullUriString = history.mFullUriString;
            photos.add(photo);
        }
        if (platform.equals("SinaWeibo")) {
            if (app.uninstallSoftware(mContext, "com.sina.weibo")) {
                ShareUtils.share(mContext, platform, photos, record.content);
            } else {
                Toast.makeText(mContext, R.string.sina_weibo_exception, Toast.LENGTH_SHORT).show();
            }
        } else if (platform.equals("WebChatMoments")) {
            if (app.uninstallSoftware(mContext, "com.tencent.mm")) {
                ShareUtils.share(mContext, platform, photos, record.content);
            } else {
                Toast.makeText(mContext, R.string.WebChat_Moments_exception, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Message msg = new Message();
        msg.what = 200;
        msg.arg1 = adapter.getItem(position)._id;
        msg.arg2 = position;
        //Toast.makeText(mContext, "没有照片可分享" + adapter.getItem(position)._id + "--" + msg.arg1, Toast.LENGTH_SHORT).show();
        deleteHandler.sendMessage(msg);
        return true;
    }

    private Handler deleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final int record_id = msg.arg1;
            final int position = msg.arg2;
            if (msg.what == 200) {
                new AlertDialog.Builder(mContext)
                        .setMessage("您确定要删除这条数据吗?")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRecord(record_id, position);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();
            }
        }
    };

    /**
     * 删除记录
     *
     * @param record_id
     * @param position
     */
    private void deleteRecord(final int record_id, final int position) {
        RecordDatabaseHelper.deleteRecord(mContext, record_id);
        Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
        records.remove(position);
        adapter.notifyDataSetChanged();
    }

}
