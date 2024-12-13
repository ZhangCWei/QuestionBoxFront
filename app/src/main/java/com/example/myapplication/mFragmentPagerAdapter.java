package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

// Fragment适配器
public class mFragmentPagerAdapter extends FragmentPagerAdapter {
    // 定义Fragment列表来存放Fragment
    private List<Fragment> fragmentList;

    // 定义构造方法
    public mFragmentPagerAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    public mFragmentPagerAdapter(FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    // 显示页面，为数组中的Fragment，必须重写
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    // 获取页面的个数，几位列表的长度，必须重写
    public int getCount() {
        return fragmentList.size();
    }
}
