package com.jki.hananeelcinta.stepper.contract;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jki.hananeelcinta.stepper.HancinStepperAdapter;
import com.jki.hananeelcinta.stepper.HancinStepperLayout;

import java.util.HashMap;

public interface HancinStepperContract {

    interface View {
        void attachToLayout();
        void show (HancinStepperLayout.Direction direction, Fragment currentFragment, Fragment nextFragment);
        void hide (Fragment mFragment);
        void setStepperProgress (int stepperNumber, boolean isForceSubmit);
        void setMaxStepperProgress (int stepperTotal);
        void showSubmitButton ();
        void showStepperButton ();
        void setStepperButtonEnabled (boolean i);
        void setSubmitButtonEnabled (boolean i);
        void showProgressView(boolean show);
        void showProgressView(boolean show, String message);
        void setStepperTextButton (int res);
        void setSubmitTextButton (int res);
        void setAdapter (HancinStepperAdapter adapter);
        HancinStepperAdapter getAdapter ();
        void setListener (Event mListener);
        Event getListener ();
    }

    interface Adapter {
        void put (Fragment mFragment);
        void put(Fragment mFragment,boolean isAllowed);
        FragmentManager getFragmentManager ();
        int getItemCount ();
        Fragment getSection (int key);
        Fragment getSelectedSection ();
        int getStepperNumber();
        void setStepperNumber (int s);
        HashMap<Integer, Fragment> getSections ();
        boolean prev (HancinStepperLayout mLayout);
        boolean next (HancinStepperLayout mLayout, android.view.View v);
        boolean move (HancinStepperLayout mLayout, android.view.View v, HancinStepperLayout.Direction dir);
        boolean move (HancinStepperLayout mLayout, android.view.View v, HancinStepperLayout.Direction dir, boolean isForceSubmit);
        void setLayout (HancinStepperLayout mLayout);
        void notifyViewHasChanged ();
    }

    interface Event {
        void onSubmitClicked ();
        void setResult (HancinStepperLayout.Direction direction);
        void onSectionLoaded(HancinStepperLayout.Direction direction);
    }

}
