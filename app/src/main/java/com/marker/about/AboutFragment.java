package com.marker.about;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class AboutFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Acerca");
        alertDialog.setMessage(
                "App para Desarrollo de aplicaciones móbiles." + "\n\n" +
                        "Desarrollado por: " + "\n" +
                        "Ezequiel Ayzenberg" + "\n" +
                        "Fernando Velcic"  + "\n" +
                        "Francisco Bravo"  + "\n" +
                        "Sandro Damilano"  + "\n"
        );

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Continuar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialog;
    }
}
