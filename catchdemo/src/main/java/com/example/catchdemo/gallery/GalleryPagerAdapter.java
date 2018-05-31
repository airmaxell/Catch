package com.example.catchdemo.gallery;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class GalleryPagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public GalleryPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ExteriorFragment exteriorFragment = new ExteriorFragment();
                return exteriorFragment;
            case 1:
                InteriorFragment interiorFragment = new InteriorFragment();
                return interiorFragment;
            case 2:
                DetailsFragment detailsFragment = new DetailsFragment();
                return detailsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return this.mNumOfTabs;
    }
}
