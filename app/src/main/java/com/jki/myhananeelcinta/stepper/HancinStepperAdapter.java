package com.jki.myhananeelcinta.stepper;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jki.myhananeelcinta.stepper.contract.HancinStepperContract;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HancinStepperAdapter implements HancinStepperContract.Adapter {

    private final HashMap<Integer, Fragment> mSections = new HashMap<>();

    private final AtomicInteger stepperNumber = new AtomicInteger();

    private FragmentManager fm;

    private Fragment currentFragment;

    private HancinStepperLayout mLayout;

    public HancinStepperAdapter (FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void put(Fragment mFragment) {
        put(mFragment, true);
    }

    @Override
    public void put(Fragment mFragment, boolean isAllowed) {
        if (!isAllowed) {
            return;
        }

        int index = mSections.keySet().size() + 1;
        mSections.put(index, mFragment);
    }

    @Override
    public FragmentManager getFragmentManager() {
        return fm;
    }

    @Override
    public int getItemCount() {
        return mSections.size();
    }

    @Override
    public Fragment getSection(int key) {
        return mSections.get(key);
    }

    @Override
    public Fragment getSelectedSection() {
        return currentFragment;
    }

    @Override
    public int getStepperNumber() {
        return stepperNumber.get();
    }

    @Override
    public void setStepperNumber(int s) {
        stepperNumber.set(s);
    }

    @Override
    public HashMap<Integer, Fragment> getSections() {
        return mSections;
    }

    @Override
    public boolean prev(HancinStepperLayout mLayout) {
        return move(mLayout, null, HancinStepperLayout.Direction.PREV);
    }

    @Override
    public boolean next(HancinStepperLayout mLayout, View v) {
        return move(mLayout, v, HancinStepperLayout.Direction.NEXT);
    }

    @Override
    public boolean move(HancinStepperLayout mLayout, View v, HancinStepperLayout.Direction dir) {
        return move(mLayout, v, dir, false);
    }

    @Override
    public boolean move(HancinStepperLayout mLayout, View v, HancinStepperLayout.Direction dir, boolean isForceSubmit) {
        int n;
        boolean isNextStep = dir.equals(HancinStepperLayout.Direction.NEXT);

        if (null != v) {
            v.setEnabled(false);
        }

        mLayout.getListener().setResult(dir);

        if (isNextStep) {
            n = increaseStepperNumber();
        } else {
            n = decreaseStepperNumber();
        }

        if (n < 1) {

            stepperNumber.set(1);
            return false;

        }

        mLayout.setStepperProgress(n, isForceSubmit);
        Fragment mFragment = getSection(n);

        mLayout.show(dir, currentFragment, mFragment);
        currentFragment = mFragment;

        return true;
    }

    @Override
    public void setLayout(HancinStepperLayout mLayout) {
        this.mLayout = mLayout;
    }

    @Override
    public void notifyViewHasChanged() {
        mLayout.attachToLayout();
    }

    private int increaseStepperNumber() {
        return stepperNumber.incrementAndGet();
    }

    private int decreaseStepperNumber() {
        return stepperNumber.decrementAndGet();
    }

}
