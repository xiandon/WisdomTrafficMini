package com.enlern.pen.wt.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enlern.pen.wt.R;
import com.enlern.pen.wt.fragment.BusStopFragment;
import com.enlern.pen.wt.fragment.ETCHeightWayFragment;
import com.enlern.pen.wt.fragment.ETCParkingFragment;
import com.enlern.pen.wt.fragment.SmartCarFragment;
import com.enlern.pen.wt.fragment.StreetLightsFragment;
import com.enlern.pen.wt.fragment.TrafficLightFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pen on 2017/11/30.
 */

public class TrafficWisdomActivity extends BaseActivity {

    @BindView(R.id.tl_traffic_wisdom)
    TabLayout tlTrafficWisdom;
    @BindView(R.id.vp_traffic_wisdom)
    ViewPager vpTrafficWisdom;
    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    private Context context;

    public static boolean bTraffic = false;

    private BusStopFragment busStopFragment = new BusStopFragment();
    private ETCHeightWayFragment etcHeightWayFragment = new ETCHeightWayFragment();
    private ETCParkingFragment etcParkingFragment = new ETCParkingFragment();
    private SmartCarFragment smartCarFragment = new SmartCarFragment();
    private StreetLightsFragment streetLightsFragment = new StreetLightsFragment();
    private TrafficLightFragment trafficLightFragment = new TrafficLightFragment();

    private SimpleFragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private String tabTitles[] = new String[]{"停车收费", "高速ETC", "红绿灯", "公交车站", "路灯", "智能小车"};

    private int curTab = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_traffic_wisdom);
        ButterKnife.bind(this);
        context = TrafficWisdomActivity.this;
        initView();
    }

    private void initView() {
        bTraffic = true;
        tvPublicTitle.setText("智慧交通");
        mFragments.add(etcParkingFragment);
        mFragments.add(etcHeightWayFragment);
        mFragments.add(trafficLightFragment);
        mFragments.add(busStopFragment);
        mFragments.add(streetLightsFragment);
        mFragments.add(smartCarFragment);

        mAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        vpTrafficWisdom.setAdapter(mAdapter);
        tlTrafficWisdom.setupWithViewPager(vpTrafficWisdom);
        tlTrafficWisdom.setSmoothScrollingEnabled(true);
        //mTabLayout.setTabMode(TabLayout.MODE_FIXED);//全部放下，不滑动
        tlTrafficWisdom.setTabMode(TabLayout.MODE_SCROLLABLE);//超过长度可滑动
        //设置当前显示哪个标签页
        vpTrafficWisdom.setCurrentItem(curTab);
        vpTrafficWisdom.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //滑动监听加载数据，一次只加载一个标签页
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public SimpleFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        //防止fragment自动销毁
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TrafficWisdomActivity.bTraffic = false;
    }
}
