package com.jki.hananeelcinta.stepper;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jki.hananeelcinta.R;
import com.jki.hananeelcinta.stepper.contract.HancinStepperContract;
import com.jki.hananeelcinta.util.UIHelper;

public class HancinStepperLayout extends LinearLayout implements HancinStepperContract.View {

    public enum Direction {
        NEXT, PREV
    }

    private View mView;

    private View mProgressView;

    private View mMainView;

    private TextView mStepperText;

    private ProgressBar mStepperProgress;

    private Button mStepperButton;

    private Button mSubmitButton;

    private HancinStepperAdapter adapter;

    private HancinStepperContract.Event mListener;

    private static final long PROGRESS_DURATION_MS = 1500;

    private boolean isShowProgressWidgetEnabled = true;

    public HancinStepperLayout(Context context) {
        super(context, null);
    }

    public HancinStepperLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init (Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (null == mView) {
            mView = inflater.inflate(R.layout.hancin_stepper_layout, this, true);
        }

        bind();

    }

    private void bind () {

        mProgressView = mView.findViewById(R.id.progress);
        mMainView = mView.findViewById(R.id.body);
        mStepperText = mView.findViewById(R.id.stepperNumber);
        mStepperProgress = mView.findViewById(R.id.stepperNumberComponent);
        mStepperButton = mView.findViewById(R.id.stepperBtn);
        mSubmitButton = mView.findViewById(R.id.submitBtn);

        mStepperButton.setOnClickListener(
                (v) -> adapter.next(this, v)
        );

        mSubmitButton.setOnClickListener(
                (v) -> UIHelper.getOneTimeClickListener().onClickedTime(
                        v, () -> mListener.onSubmitClicked(), 1000
                )
        );

        if (isShowProgressWidgetEnabled) {
            showProgressView(true);
        }

    }

    @Override
    public void attachToLayout () {

        FragmentManager fm = adapter.getFragmentManager();
        FragmentTransaction mTransaction = fm.beginTransaction();

        if (adapter.getSections().size() <= 0) {
            return;
        }

        if (fm.getFragments().size() > 0) {

            for (Fragment mFragment:fm.getFragments()) {
                mTransaction.remove(mFragment);
            }

        }

        for (Fragment mFragment:adapter.getSections().values()) {

            mTransaction
                    .add(R.id.body, mFragment)
                    .hide(mFragment);
        }

        mTransaction.commit();

        setMaxStepperProgress(adapter.getItemCount());
        adapter.setStepperNumber(0);

        adapter.next(this, null);
    }

    public View getStepperButton(){
        return mStepperButton;
    }

    public View getSubmitButton(){
        return mSubmitButton;
    }

    @Override
    public void show(HancinStepperLayout.Direction direction, Fragment currentFragment, Fragment nextFragment) {

        if (isShowProgressWidgetEnabled) {
            showProgressView(true);

            new Handler().postDelayed(
                    () -> attach(direction, currentFragment, nextFragment), PROGRESS_DURATION_MS
            );

            return;
        }

        attach(direction, currentFragment, nextFragment);
    }

    private void attach (HancinStepperLayout.Direction direction, Fragment currentFragment, Fragment nextFragment) {
        if (null != currentFragment) {
            hide(currentFragment);
        }
        try {
            showProgressView(false);
            FragmentTransaction mTransaction = adapter.getFragmentManager().beginTransaction();
            mTransaction.show(nextFragment).commit();
            mListener.onSectionLoaded(direction);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void hide(Fragment mFragment) {
        try {
            FragmentTransaction mTransaction = adapter.getFragmentManager().beginTransaction();
            mTransaction.hide(mFragment).commit();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setStepperProgress(int stepperNumber, boolean isForceSubmit) {

        int totalStepperSection = mStepperProgress.getMax();

        buildStepperInfo(stepperNumber, totalStepperSection);

        if ((stepperNumber < totalStepperSection) && !isForceSubmit) {
            showStepperButton();
            return;
        }

        buildStepperInfo(totalStepperSection, totalStepperSection);
        showSubmitButton();

    }

    private void buildStepperInfo (int stepperNumber, int totalStepperSection) {
        String stepperNumberText = String.format(
                getContext().getString(R.string.stepper_format_txt), stepperNumber, totalStepperSection
        );

        mStepperText.setText(stepperNumberText);
        mStepperProgress.setProgress(stepperNumber);
    }

    @Override
    public void setMaxStepperProgress(int stepperTotal) {
        mStepperProgress.setMax(stepperTotal);
    }

    @Override
    public void showSubmitButton() {
        mSubmitButton.setVisibility(VISIBLE);
        mStepperButton.setVisibility(GONE);
    }

    @Override
    public void showStepperButton() {
        mSubmitButton.setVisibility(GONE);
        mStepperButton.setVisibility(VISIBLE);
    }

    @Override
    public void setStepperButtonEnabled(boolean i) {
        mStepperButton.setEnabled(i);
    }

    @Override
    public void setSubmitButtonEnabled(boolean i) {
        mSubmitButton.setEnabled(i);
    }

    public void setSubmitButtonText(String text){
        mSubmitButton.setText(text);
    }

    @Override
    public void showProgressView(boolean show) {
        showProgressView(show, getContext().getString(R.string.text_pleaseWait));
    }

    @Override
    public void showProgressView(boolean show, String message) {

        if(mProgressView instanceof LinearLayout && message != null) {
            LinearLayout progressLinearLayout = (LinearLayout) mProgressView;
            View textViewCandidate = progressLinearLayout.getChildAt(1);
            if(textViewCandidate instanceof TextView) {
                TextView progressTextView = (TextView) textViewCandidate;
                progressTextView.setText(message);
            }
        }

        UIHelper.getInstance().showProgress(getContext(), mMainView, mProgressView, show);

    }

    @Override
    public void setStepperTextButton(int res) {
        mStepperButton.setText(res);
    }

    @Override
    public void setSubmitTextButton(int res) {
        mSubmitButton.setText(res);
    }

    @Override
    public void setAdapter(HancinStepperAdapter adapter) {
        adapter.setLayout(this);

        this.adapter = adapter;
        this.adapter.notifyViewHasChanged();
    }

    @Override
    public HancinStepperAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setListener(HancinStepperContract.Event mListener) {
        this.mListener = mListener;
    }

    @Override
    public HancinStepperContract.Event getListener() {
        return mListener;
    }

    public boolean isShowProgressWidgetEnabled() {
        return isShowProgressWidgetEnabled;
    }

    public void setShowProgressWidgetEnabled(boolean showProgressWidgetEnabled) {
        isShowProgressWidgetEnabled = showProgressWidgetEnabled;
    }

    public void setStepperText(String text){
        mStepperText.setText(text);
    }

}
