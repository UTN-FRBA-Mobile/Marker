package com.marker.lugar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class BorrarLugarFragment extends DialogFragment {

    private Lugar lugar;
    private LugarManager lugarManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle bundle = getArguments();
        if(bundle != null){
            lugar = bundle.getParcelable("lugar");
            lugarManager = bundle.getParcelable("lugarManager");
        }

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext())
                .setTitle("Borrar")
                .setMessage(String.format("¿Borrar %s de \"Mis Destinos\"?", lugar.nombre))
                .setCancelable(true)
                .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface deleteDialog, int id) {
                        borrarDestino(lugar);
                        //KILL ME!
                        ((LugarActivity)getActivity()).getAdapter().deleteLugar(lugar);
                    }
                });

        return deleteDialog.create();
    }

    private void borrarDestino(Lugar lugar) {
        this.lugarManager.deleteLugar(lugar.uid);
    }

}
