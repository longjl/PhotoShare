package com.tintinshare;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.tintinshare.base.PhotoFragmentActivity;
import com.tintinshare.fragments.CircleFragment;
import com.tintinshare.fragments.PhotosFragment;
import com.tintinshare.fragments.SelectedPhotosFragment;
import com.tintinshare.views.PagerSlidingTabStrip;


public class TintinShareActivity extends PhotoFragmentActivity {

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;

    private DisplayMetrics dm;                          //获取当前屏幕的密度
    private TabPagerAdapter mTabAdapter;                    //Pager 数据适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dm = getResources().getDisplayMetrics();        //获取屏幕分辨率
        setContentView(R.layout.activity_tintin_share);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);

        mTabAdapter = new TabPagerAdapter(getSupportFragmentManager());
        tabsHandler.sendEmptyMessage(0);
    }

    //tabs handler
    private Handler tabsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                pager.setAdapter(mTabAdapter);
                tabs.setViewPager(pager);
                setTabsValue();
            }
        }
    };
    /**
     * 对PagerSlidingTabStrip的各项属性进行赋值。
     */
    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(getResources().getColor(R.color.indicatorcolor));
        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
        tabs.setSelectedTextColor(getResources().getColor(R.color.selectedtextcolor));
        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);
    }


    /**
     * tab adapter
     */
    private class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private final String[] titles = getApplicationContext().getResources().getStringArray(R.array.tab_titles);

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    return new PhotosFragment();
                case 1:
                    return new SelectedPhotosFragment();
                case 2:
                    return new CircleFragment();
                default:
                    return null;
            }
        }
    }


}
