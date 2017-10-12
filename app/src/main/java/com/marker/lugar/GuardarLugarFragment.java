package com.marker.lugar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;

import com.marker.history.History;

/**
 * Created by sdamilano on 12/10/17.
 */

public class GuardarLugarFragment extends DialogFragment {

    private History history;
    private LugarManager lugarManager;

    public GuardarLugarFragment(){}

    public GuardarLugarFragment(History aHistory, LugarManager unLugarManager){
        history = aHistory;
        lugarManager = unLugarManager;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle bundle = getArguments();
        if(bundle != null){
            history = bundle.getParcelable("history");
            lugarManager = bundle.getParcelable("lugarManager");
        }

        final EditText nombreDestino = new EditText(getContext());

        AlertDialog.Builder saveDialog = new AlertDialog.Builder(getContext());
        saveDialog.setTitle(String.format("¿Guardar %s en \"Mis Destinos\"?", history.location));
        saveDialog.setMessage("Puede cambiarle el nombre:");
        saveDialog.setView(nombreDestino);
        saveDialog.setCancelable(true);
        saveDialog.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface saveDialog, int id) {
                guardarDestino(history, nombreDestino.getText());
            }
        });

        return saveDialog.create();
    }

    private void guardarDestino(History history, Editable nombreDestino) {
        String nombre = TextUtils.isEmpty(nombreDestino) ? history.location : nombreDestino.toString();
        this.lugarManager.writeLugar(nombre, history.position);
    }

}
