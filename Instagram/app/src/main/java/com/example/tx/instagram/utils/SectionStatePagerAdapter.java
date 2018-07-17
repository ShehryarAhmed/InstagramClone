package com.example.tx.instagram.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final HashMap<Fragment,Integer> mFragments = new HashMap<>();
    private final HashMap<String,Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer,String> mFragmentName = new HashMap<>();

    public SectionStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment,String fragmentName){
        mFragmentList.add(fragment);
        mFragments.put(fragment, mFragmentList.size()-1);
        mFragmentNumbers.put(fragmentName, mFragmentList.size()-1);
        mFragmentName.put(mFragmentList.size()-1,fragmentName);
    }

    public Integer getFragmentNumber(String fragmentName){
        if(mFragmentNumbers.containsKey(fragmentName)){
            return mFragmentNumbers.get(fragmentName);
        }
        else {
            return null;
        }
    }

    public Integer getFragment(Fragment fragment){
        if(mFragments.containsKey(fragment)){
            return mFragments.get(fragment);
        }
        else {
            return null;
        }
    }

    public String getFragmentName(Integer fragmentNumber){
        if(mFragmentName.containsKey(fragmentNumber)){
            return mFragmentName.get(fragmentNumber);
        }
        else {
            return null;
        }
    }
}
