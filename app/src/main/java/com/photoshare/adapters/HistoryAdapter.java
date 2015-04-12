package com.photoshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.photoshare.R;
import com.photoshare.activities.PhotoViewPagerActivity;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;
import com.photoshare.views.PhotoGridView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by longjianlin on 15/4/11.
 */
public class HistoryAdapter extends BaseAdapter {
    private LinkedList<Record> mRecords;
    private Context mContext;
    private LayoutInflater inflater;
    private SimpleDateFormat sdfM = new SimpleDateFormat("M月");
    private SimpleDateFormat sdfD = new SimpleDateFormat("dd");
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String EXTRA_POSITION = "extra_position";
    public static final String RECORD_ID = "record_id";

    public HistoryAdapter(Context context, LinkedList<Record> records) {
        mContext = context;
        mRecords = records;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mRecords == null ? 0 : mRecords.size();
    }

    @Override
    public Record getItem(int position) {
        return mRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_list_history, null);
            holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            holder.tv_d = (TextView) convertView.findViewById(R.id.tv_d);
            holder.tv_m = (TextView) convertView.findViewById(R.id.tv_m);
            holder.historyGridView = (PhotoGridView) convertView.findViewById(R.id.gv_history);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Record record = getItem(position);
        holder.tv_content.setText(record.content);
        try {
            Date date = format.parse(record.date);
            holder.tv_d.setText(sdfD.format(date));
            holder.tv_m.setText(sdfM.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (record.histories != null && record.histories.size() > 0) {
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
            holder.historyGridView.setAdapter(new HistoryGridAdapter(mContext, photos));
            holder.historyGridView.setOnItemClickListener(new MyGridItemClickListener(position));
        }
        return convertView;
    }

    class MyGridItemClickListener implements AdapterView.OnItemClickListener {
        private int mGroupPosition;

        public MyGridItemClickListener(int group_position) {
            mGroupPosition = group_position;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(mContext, PhotoViewPagerActivity.class);
            intent.putExtra(EXTRA_POSITION, position);
            intent.putExtra(RECORD_ID, getItem(mGroupPosition)._id);
            mContext.startActivity(intent);
        }
    }


    class ViewHolder {
        public TextView tv_content;
        public TextView tv_d;
        public TextView tv_m;
        public PhotoGridView historyGridView;
    }
}
