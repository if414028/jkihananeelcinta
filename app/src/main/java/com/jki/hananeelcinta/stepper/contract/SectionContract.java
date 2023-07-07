package com.jki.hananeelcinta.stepper.contract;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public interface SectionContract {

    interface View<T> {
        void setPresenter (T presenter);
        void listen (SectionActivityContract.View mParent);
        T getPresenter ();
        TextWatcher getFieldListener (SectionActivityContract.View mParent);
        boolean isEmpty (EditText c, Editable e);
        boolean isSectionVisible ();
    }

    interface Presenter<T> {
        void start ();
        void submit(T t, boolean isAllowed);
        void prepopulate (T t);
        boolean isValidated ();
    }

}
