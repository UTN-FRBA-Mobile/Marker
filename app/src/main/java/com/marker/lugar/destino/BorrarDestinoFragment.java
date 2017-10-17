package com.marker.lugar.destino;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;


public class BorrarDestinoFragment extends DialogFragment {

    private Destino destino;
    private DestinoManager destinoManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle bundle = getArguments();
        if(bundle != null){
            destino = bundle.getParcelable("destino");
            destinoManager = bundle.getParcelable("destinoManager");
        }

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext())
                .setTitle("Borrar")
                .setMessage(String.format("¿Borrar %s de \"Mis Destinos\"?", destino.nombre))
                .setCancelable(true)
                .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface deleteDialog, int id) {
                        borrarDestino(destino);
                        //KILL ME!
                        ((DestinoActivity)getActivity()).getAdapter().deleteDestino(destino);
                        Toast.makeText(getContext(), "Destino borrado", Toast.LENGTH_SHORT).show();
                    }
                });

        return deleteDialog.create();
    }

    private void borrarDestino(Destino destino) {
        this.destinoManager.deleteLugar(destino.uid);
    }

}
