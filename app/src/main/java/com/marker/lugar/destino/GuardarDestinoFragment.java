package com.marker.lugar.destino;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.marker.lugar.history.History;

import java.util.ArrayList;


public class GuardarDestinoFragment extends DialogFragment {

    private History history;
    private DestinoManager destinoManager;
    private ArrayList<Destino> destinos;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle bundle = getArguments();
        if(bundle != null){
            history = bundle.getParcelable("history");
            destinoManager = bundle.getParcelable("destinoManager");
            destinos = bundle.getParcelableArrayList("destinos");
        }

        if(destinoYaExistente())
            return this.existentDestinationDialogBuilder();
        else
            return this.saveDialogBuilder();
    }

    private boolean destinoYaExistente() {
        for(Destino destino : destinos){
            if(history.posicion.isEquivalentTo(destino.posicion))
                return true;
        }
        return false;
    }

    private AlertDialog saveDialogBuilder(){
        final EditText nombreDestino = new EditText(getContext());

        AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(String.format("¿Guardar %s en \"Mis Destinos\"?", history.nombre))
                .setMessage("Puede cambiarle el nombre:")
                .setView(nombreDestino)
                .setCancelable(true)
                .setPositiveButton("Guardar", null);

        final AlertDialog saveDialog = saveDialogBuilder.show();

        saveDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(nombreDestinoYaExistente(nombreDestino.getText()))
                    Toast.makeText(getContext(), "Ya existe un destino con ese nombre", Toast.LENGTH_LONG).show();
                else {
                    guardarDestino(history, nombreDestino.getText());
                    saveDialog.dismiss();
                    Toast.makeText(getContext(), "Destino guardado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return saveDialog;
    }

    private boolean nombreDestinoYaExistente(Editable nombreDestino) {
        for(Destino destino : destinos){
            if(destino.nombre.equals(nombreDestino.toString()))
                return true;
        }
        return false;
    }

    private AlertDialog existentDestinationDialogBuilder() {
        return new AlertDialog.Builder(getContext())
                .setTitle("¡Destino ya guardado!")
                .setCancelable(true)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void guardarDestino(History history, Editable nombreDestino) {
        String nombre = TextUtils.isEmpty(nombreDestino) ? history.nombre : nombreDestino.toString();
        this.destinoManager.writeLugar(nombre, history.posicion);
    }

}
