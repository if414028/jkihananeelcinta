package com.jki.myhananeelcinta.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.jki.myhananeelcinta.R;

public class UIHelper {

    private static final UIHelper ourInstance = new UIHelper();

    public static UIHelper getInstance() {
        return ourInstance;
    }

    private UIHelper() {
    }

    public static OneTimeClickListener getOneTimeClickListener() {
        return new OneTimeClickListener() {
            private long mLastClickedTime;

            @Override
            public void onClickedTime(View view, Runnable runnable, long thresholdTime) {
                if ((SystemClock.elapsedRealtime() - mLastClickedTime) < thresholdTime) {
                    return;
                }

                runnable.run();
                view.setEnabled(false);
                mLastClickedTime = SystemClock.elapsedRealtime();
            }
        };
    }

    private boolean isContextRunning(Context ctx) {
        if (ctx != null && ctx instanceof Activity) {
            Activity act = (Activity) ctx;
            return !act.isFinishing();
        } else {
            return false;
        }
    }

    public void showProgress(Context context, final View view, final View progressView, final boolean show) {

        if (isContextRunning(context) && view != null && progressView != null) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            view.setVisibility(show ? View.GONE : View.VISIBLE);
            view.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    public interface OneTimeClickListener {
        void onClickedTime(View view, Runnable runnable, long thresholdTime);
    }

    public void displayConfirmation(String info, String subInfo, Context context, @NonNull final Runnable onConfirmed,
                                    @Nullable final Runnable onRejected, String confirmText, String rejectText,
                                    Integer imageResource,
                                    boolean isCancelable) {
        if (isContextRunning(context)) {
            final Dialog dialog = new BottomSheetDialog(context, R.style.HancinBottomAlertTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_confirmation);
            dialog.setCancelable(isCancelable);

            ImageView logo = dialog.findViewById(R.id.iv_dialog_logo);
            if (null != imageResource) {
                logo.setImageResource(imageResource);
            } else {
                logo.setImageResource(R.drawable.tick);
            }

            TextView mainText = dialog.findViewById(R.id.tv_dialog_main_text);
            mainText.setText(info);

            TextView subText = dialog.findViewById(R.id.tv_dialog_secondary_text);
            subText.setText(subInfo);

            Button negativeButton = dialog.findViewById(R.id.btn_negative);
            negativeButton.setText(rejectText);
            negativeButton.setOnClickListener(view -> {
                dialog.dismiss();
                if (onRejected != null) onRejected.run();
            });

            Button positiveButton = dialog.findViewById(R.id.btn_positive);
            positiveButton.setText(confirmText);
            positiveButton.setOnClickListener(view -> {
                dialog.dismiss();
                if (onConfirmed != null) onConfirmed.run();
            });

            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (!activity.isFinishing()) {
                    dialog.show();
                }
            } else {
                dialog.show();
            }
        }
    }

    public void displaySuccessDialog(String info, String subInfo, Context context, @NonNull final Runnable onConfirmed,
                                     String confirmText,
                                     boolean isCancelable) {
        if (isContextRunning(context)) {
            final Dialog dialog = new BottomSheetDialog(context, R.style.HancinBottomAlertTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_success);
            dialog.setCancelable(isCancelable);

            TextView mainText = dialog.findViewById(R.id.tv_dialog_main_text);
            mainText.setText(info);

            TextView subText = dialog.findViewById(R.id.tv_dialog_secondary_text);
            subText.setText(subInfo);

            Button positiveButton = dialog.findViewById(R.id.btn_positive);
            positiveButton.setText(confirmText);
            positiveButton.setOnClickListener(view -> {
                dialog.dismiss();
                if (onConfirmed != null) onConfirmed.run();
            });

            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (!activity.isFinishing()) {
                    dialog.show();
                }
            } else {
                dialog.show();
            }
        }
    }
}
