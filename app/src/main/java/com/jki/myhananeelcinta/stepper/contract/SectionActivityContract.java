package com.jki.myhananeelcinta.stepper.contract;

public interface SectionActivityContract {

    interface View {
        void exit ();
        boolean isOffline ();
        void showActionBar ();
        void attachHandlerToUI (Runnable r);
    }

    interface Presenter {
        void init ();
    }

}
