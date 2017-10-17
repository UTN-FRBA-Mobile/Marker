package com.marker.lugar.destino;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.marker.lugar.history.History;



public class GuardarDestinoFragment extends DialogFragment {

    private History history;
    private DestinoManager destinoManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Bundle bundle = getArguments();
        if(bundle != null){
            history = bundle.getParcelable("history");
            destinoManager = bundle.getParcelable("destinoManager");
        }

        final EditText nombreDestino = new EditText(getContext());

        AlertDialog.Builder saveDialog = new AlertDialog.Builder(getContext())
                .setTitle(String.format("¿Guardar %s en \"Mis Destinos\"?", history.nombre))
                .setMessage("Puede cambiarle el nombre:")
                .setView(nombreDestino)
                .setCancelable(true)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface saveDialog, int id) {
                        guardarDestino(history, nombreDestino.getText());
                        Toast.makeText(getContext(), "Destino guardado", Toast.LENGTH_SHORT).show();
                    }
                });

        return saveDialog.create();
    }

    private void guardarDestino(History history, Editable nombreDestino) {
        String nombre = TextUtils.isEmpty(nombreDestino) ? history.nombre : nombreDestino.toString();
        this.destinoManager.writeLugar(nombre, history.posicion);
    }

}
