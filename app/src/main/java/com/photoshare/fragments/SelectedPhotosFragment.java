package com.photoshare.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.photoshare.PhotoApplication;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.activities.PhotoViewerActivity;
import com.photoshare.adapters.SelectedPhotosBaseAdapter;
import com.photoshare.adapters.ShareAdapter;
import com.photoshare.dao.RecordDatabaseHelper;
import com.photoshare.events.PhotoSelectionAddedEvent;
import com.photoshare.events.PhotoSelectionRemovedEvent;
import com.photoshare.events.ShareEvent;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.dao.PhotoDatabaseHelper;
import com.photoshare.model.Record;
import com.photoshare.tasks.PhotoThreadRunnable;
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
        SwipeDismissListViewTouchListener.OnDismissCallback, View.OnClickListener {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private GridView mGridView;
    private EditText et_content;
    private SelectedPhotosBaseAdapter mAdapter;
    private PhotoController mPhotoSelectionController;

    private PopupWindow popupWindow;
    private View popupWindowView;
    private GridView gv_share;
    private ShareAdapter shareAdapter;

    private View view;

    @Override
    public void onAttach(Activity activity) {
        mPhotoSelectionController = PhotoController.getFromContext(activity);
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        initPopupWindow();
        return view;
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == mGridView.getId()) {
            Bundle b = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeThumbnailScaleUpAnimation(view,
                                Utils.drawViewOntoBitmap(view), 0, 0);
                b = options.toBundle();
                Intent intent = new Intent(getActivity(), PhotoViewerActivity.class);
                intent.putExtra(PhotoViewerActivity.EXTRA_POSITION, position);
                intent.putExtra(PhotoViewerActivity.EXTRA_MODE, PhotoViewerActivity.MODE_SELECTED_VALUE);
                ActivityCompat.startActivity(getActivity(), intent, b);
            }
        } else if (parent.getId() == gv_share.getId()) {
            String platform = shareAdapter.getItem(position);
            if (null != platform) {
                uninstallSoftware(platform);
            }
        }
    }

    private void uninstallSoftware(String platform) {
        PhotoApplication app = PhotoApplication.getApplication(getActivity());
        if (mPhotoSelectionController.getSelectedCount() == 0) {
            Toast.makeText(getActivity(), "不能没有图片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (platform.equals("SinaWeibo")) {
            if (app.uninstallSoftware(getActivity(), "com.sina.weibo")) {
                handler.sendEmptyMessage(0);

                Message message = new Message();
                message.obj = platform;
                message.what = 200;
                shareHandler.sendMessage(message);
            } else {
                Toast.makeText(getActivity(), R.string.sina_weibo_exception, Toast.LENGTH_SHORT).show();
            }
        } else if (platform.equals("WebChatMoments")) {
            if (app.uninstallSoftware(getActivity(), "com.tencent.mm")) {
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
            record.acc_id = "1234567890";
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
            //RecordDatabaseHelper.saveRecordToDatabase(getActivity(), record, histories);
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

    /**
     * 初始化
     */
    private void initPopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popupWindowView = inflater.inflate(R.layout.fragment_popupwindow_share, null);
        popupWindow = new PopupWindow(popupWindowView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置PopupWindow的弹出和消失效果
        popupWindow.setAnimationStyle(R.style.popupAnimation);
        gv_share = (GridView) popupWindowView.findViewById(R.id.gv_share);
        if (shareAdapter == null) {
            shareAdapter = new ShareAdapter(getActivity());
        }
        gv_share.setAdapter(shareAdapter);
        gv_share.setOnItemClickListener(this);
    }

    /**
     * 分享弹出框
     */
    private void showPopupWindow() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        popupWindow.showAtLocation(gv_share, Gravity.BOTTOM, 0, 0);
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
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

    public void onEvent(ShareEvent event) {
        showPopupWindow();
    }

    private ProgressDialog dialog;

    private void showProgressDialog() {
        dialog = ProgressDialog.show(getActivity(), "", "数据传输中. 请稍等...", true, false);
    }
}
