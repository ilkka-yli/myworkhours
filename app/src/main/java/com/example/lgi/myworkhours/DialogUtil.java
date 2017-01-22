package com.example.lgi.myworkhours;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by lgi on 21.1.2017.
 */

public class DialogUtil {
    public static void showAlertDialog(AppCompatActivity act, String title, String text) {
        AlertDialog alertDialog = new AlertDialog.Builder(act).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, act.getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
