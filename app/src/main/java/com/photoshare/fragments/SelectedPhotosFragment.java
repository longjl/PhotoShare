package com.photoshare.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.photoshare.Constants;
import com.photoshare.PhotoApplication;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.activities.PhotoViewerActivity;
import com.photoshare.adapters.RecordAdapter;
import com.photoshare.adapters.SelectedPhotosBaseAdapter;
import com.photoshare.adapters.ShareAdapter;
import com.photoshare.dao.RecordDatabaseHelper;
import com.photoshare.events.PhotoSelectionAddedEvent;
import com.photoshare.events.PhotoSelectionRemovedEvent;
import com.photoshare.events.RecordEvent;
import com.photoshare.events.ShareEvent;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;
import com.photoshare.tasks.RecordsAsyncTask;
import com.photoshare.util.ShareUtils;
import com.photoshare.util.Utils;

import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by longjianlin on 15/3/19.
 */
public class SelectedPhotosFragment extends SherlockFragment
        implements AdapterView.OnItemClickListener,
        SwipeDismissListViewTouchListener.OnDismissCallback,
        View.OnClickListener, RecordsAsyncTask.RecordResultListener {
    private static SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
    private DisplayMetrics dm;                          //获取当前屏幕的密度

    private GridView mGridView;
    private EditText et_content;
    private SelectedPhotosBaseAdapter mAdapter;
    private PhotoController mPhotoSelectionController;
    PhotoApplication app;

    private PopupWindow popupWindow;
    private View popupWindowView;
    private GridView gv_share;
    private ShareAdapter shareAdapter;

    private PopupWindow recordPopupWindow;
    private View recordPopupWindowView;
    private ListView recordListView;
    private Button btn_cancel;
    private RecordAdapter recordAdapter;
    private List<Record> records = new ArrayList<Record>();

    private View view;

    @Override
    public void onAttach(Activity activity) {
        mPhotoSelectionController = PhotoController.getFromContext(activity);
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dm = getResources().getDisplayMetrics();        //获取屏幕分辨率
        app = PhotoApplication.getApplication(getActivity());
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_selected_photos, container, false);
        mGridView = (GridView) view.findViewById(R.id.gv_photos);
        mGridView.setOnItemClickListener(this);

        et_content = (EditText) view.findViewById(R.id.et_content);

        final boolean swipeToDismiss = getResources().getBoolean(R.bool.swipe_selected);
        // Check if we're set to swipe
        if (swipeToDismiss) {
            SwipeDismissListViewTouchListener swipeListener = new SwipeDismissListViewTouchListener(
                    mGridView, this);
            mGridView.setOnTouchListener(swipeListener);
            mGridView.setOnScrollListener(swipeListener.makeScrollListener());
        }

        mAdapter = new SelectedPhotosBaseAdapter(getActivity(), !swipeToDismiss);
        mGridView.setAdapter(mAdapter);
        mGridView.setEmptyView(view.findViewById(android.R.id.empty));
        return view;
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mGridView != null && parent.getId() == mGridView.getId()) {
            Bundle b = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeThumbnailScaleUpAnimation(view,
                                Utils.drawViewOntoBitmap(view), 0, 0);
                b = options.toBundle();
                Intent intent = new Intent(getActivity(), PhotoViewerActivity.class);
                intent.putExtra(Constants.EXTRA_POSITION, position);
                intent.putExtra(Constants.EXTRA_MODE, PhotoViewerActivity.MODE_SELECTED_VALUE);
                ActivityCompat.startActivity(getActivity(), intent, b);
            }
        } else if (gv_share != null && parent.getId() == gv_share.getId()) {
            String platform = shareAdapter.getItem(position);
            if (null != platform) {
                uninstallSoftware(platform);
            }
        } else if (recordListView != null && parent.getId() == recordListView.getId()) {
            dismissRecordPopupWindow();
            Record record = recordAdapter.getItem(position);
            et_content.setText(record.content);
        }
    }

    private void uninstallSoftware(String platform) {
        PhotoApplication app = PhotoApplication.getApplication(getActivity());
        if (mPhotoSelectionController.getSelectedCount() == 0) {
            showToast(R.string.no_photo);
            return;
        }
        if (platform.equals(Constants.SINA_WEIBO)) {
            if (app.uninstallSoftware(getActivity(), Constants.SINA_WEIBO_APP)) {
                handler.sendEmptyMessage(0);

                Message message = new Message();
                message.obj = platform;
                message.what = 200;
                shareHandler.sendMessage(message);
            } else {
                Toast.makeText(getActivity(), R.string.sina_weibo_exception, Toast.LENGTH_SHORT).show();
            }
        } else if (platform.equals(Constants.WEB_CHAT_MOMENTS)) {
            if (app.uninstallSoftware(getActivity(), Constants.WEB_CHAT_MOMENTS_APP)) {
                handler.sendEmptyMessage(0);

                Message message = new Message();
                message.obj = platform;
                message.what = 200;
                shareHandler.sendMessage(message);
            } else {
                Toast.makeText(getActivity(), R.string.WebChat_Moments_exception, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //分享Handler
    private Handler shareHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final String platform = msg.obj.toString();
            switch (msg.what) {
                case 200:
                    ShareUtils.share(getActivity(), platform, mPhotoSelectionController.getSelected(), getContentForText());
                    break;
            }
        }
    };

    //将Photo 数据保存到数据库 Handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Record record = new Record();
            record.acc_id = app.account.acc_id;
            record.content = getContentForText();
            record.date = format.format(new Date());

            List<History> histories = new ArrayList<History>();
            for (Photo photo : mPhotoSelectionController.getSelected()) {
                History history = new History();
                history.mAccountId = photo.mAccountId;
                history.mCompletedDetection = photo.mCompletedDetection;
                history.mUserRotation = photo.mUserRotation;
                history.mFilter = photo.mFilter;
                history.mCropLeft = photo.mCropLeft;
                history.mCropTop = photo.mCropTop;
                history.mCropRight = photo.mCropRight;
                history.mCropBottom = photo.mCropBottom;
                history.mAccountId = photo.mAccountId;
                history.mTargetId = photo.mTargetId;
                history.mQuality = photo.mQuality;
                history.mResultPostId = photo.mResultPostId;
                history.mState = photo.mState;
                history.mFullUriString = photo.mFullUriString;
                histories.add(history);
            }
            //保存记录
            RecordDatabaseHelper.saveRecordToDatabase(getActivity(), record, histories);
        }
    };


    private String getContentForText() {
        if (et_content.getText() != null && !et_content.getText().toString().isEmpty()) {
            return et_content.getText().toString();
        }
        return "";
    }


    public boolean canDismiss(AbsListView listView, int position) {
        // All can be swiped
        return true;
    }

    public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
        try {
            for (int i = 0, z = reverseSortedPositions.length; i < z; i++) {
                Photo upload = (Photo) listView
                        .getItemAtPosition(reverseSortedPositions[i]);
                mPhotoSelectionController.removeSelection(upload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }

    /* **************************************分享PopupWindow****************************************/

    /**
     * 弹出分享PopupWindow
     */
    private void showSharePopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popupWindowView = inflater.inflate(R.layout.fragment_popupwindow_share, null);
        popupWindow = new PopupWindow(popupWindowView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
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

        app.hideSoftInputFormWindow(getActivity(), view);
        popupWindow.showAtLocation(gv_share, Gravity.BOTTOM, 0, 0);
    }


 /* **************************************分享PopupWindow****************************************/

    /**
     * 弹出记录PopupWindow
     */
    private void showRecordPopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        recordPopupWindowView = inflater.inflate(R.layout.fragment_popupwindow_record, null);
        recordPopupWindow = new PopupWindow(recordPopupWindowView, LayoutParams.MATCH_PARENT, dm.heightPixels / 2 + 300, true);
        ColorDrawable cd = new ColorDrawable(0x000000);
        recordPopupWindow.setBackgroundDrawable(cd);

        //产生背景变暗效果
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.4f;
        getActivity().getWindow().setAttributes(lp);
        //设置PopupWindow的弹出和消失效果
        recordPopupWindow.setAnimationStyle(R.style.popupAnimation);
        recordPopupWindow.update();
        recordPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });

        btn_cancel = (Button) recordPopupWindowView.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        recordListView = (ListView) recordPopupWindowView.findViewById(R.id.lv_record);
        recordListView.setOnItemClickListener(this);
        if (recordAdapter == null) {
            recordAdapter = new RecordAdapter(getActivity(), records);
        }
        recordListView.setAdapter(recordAdapter);
        app.hideSoftInputFormWindow(getActivity(), view);
        recordPopupWindow.showAtLocation(recordListView, Gravity.BOTTOM, 0, 0);
    }


    /**
     * 关闭PopupWindow
     */
    private void dismissRecordPopupWindow() {
        if (recordPopupWindow != null) {
            recordPopupWindow.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == btn_cancel.getId()) {
            dismissRecordPopupWindow();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        RecordsAsyncTask.execute(getActivity(), this, app.account.acc_id);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onEvent(PhotoSelectionAddedEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    public void onEvent(PhotoSelectionRemovedEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 分享事件
     *
     * @param event
     */
    public void onEvent(ShareEvent event) {
        showSharePopupWindow();
    }

    /**
     * 记录事件
     *
     * @param event
     */
    public void onEvent(RecordEvent event) {
        showRecordPopupWindow();
        recordAdapter.notifyDataSetChanged();
    }

    private ProgressDialog dialog;

    private void showProgressDialog() {
        dialog = ProgressDialog.show(getActivity(), "", "数据传输中. 请稍等...", true, false);
    }

    @Override
    public void onRecordsLoaded(List<Record> records) {
        this.records.clear();
        this.records.addAll(records);
    }

    /**
     * 显示Toast
     *
     * @param resId
     */
    private void showToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }
}
