package com.venomdino.exonetworkstreamer.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.venomdino.exonetworkstreamer.R;

import java.net.URL;

public class CustomMethods {

    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean isValidURL(String link) {
        try {
            URL url = new URL(link);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static String getFileName(String url) {

        try{
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            int index = path.lastIndexOf("/");
            if (index != -1) {
                return path.substring(index + 1);
            } else {
                return null;
            }
        }
        catch (Exception e){
            return "Unknown";
        }
    }


    public static void errorAlert(Activity activity, String errorTitle, String errorBody, String actionButton, boolean shouldGoBack) {

        if (!activity.isFinishing()){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(errorTitle);
            builder.setMessage(errorBody);
            builder.setIcon(R.drawable.warning_24);
            builder.setPositiveButton(actionButton, (dialogInterface, i) -> {
                if (shouldGoBack){
                    activity.finish();
                }
                else {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Copy", (dialog, which) -> {
                activity.finish();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }



    public static void hideSystemUI(Activity activity) {

        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
