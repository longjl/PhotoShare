package com.photoshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.photoshare.R;

/**
 * 圈子
 * Created by longjianlin on 15/3/19.
 */
public class CircleFragment extends SherlockFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circle, null);
        return view;
    }
}