package com.marker.lugar.destino;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.marker.R;
import com.marker.app.GestorSesion;
import com.marker.lugar.history.History;


public class GuardarDestinoFragment extends DialogFragment {

    private History history;
    private DestinoManager destinoManager;
    EditText nombreDestino;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        Bundle bundle = getArguments();
        if(bundle != null){
            history = bundle.getParcelable("history");
        }

        initializeDestinoManager();

        return saveDialogBuilder().create();
    }



    private void initializeDestinoManager() {
        destinoManager = new DestinoManager(GestorSesion.getInstancia(getContext()).getUsuarioLoggeado()) {
            @Override
            protected void onCheckDestino(boolean result) {
                if(result == true) {
                    //No existe destino
                    String nombre = TextUtils.isEmpty(nombreDestino.getText()) ? history.nombre : nombreDestino.getText().toString();
                    destinoManager.writeDestino(nombre, history.posicion);
                    Snackbar.make(getView().findViewById(R.id.rv_histories), "Destino guardado", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getContext(), "Destino guardado", Toast.LENGTH_SHORT).show();
                } else {
                    //Destino repetido
                    Snackbar.make(getView().findViewById(R.id.rv_histories), "Ya existe un destino con ese nombre", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getContext(), "Ya existe un destino con ese nombre", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private AlertDialog.Builder saveDialogBuilder(){
        nombreDestino = new EditText(getContext());

        AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(String.format("¿Guardar %s en \"Mis Destinos\"?", history.nombre))
                .setMessage("Puede cambiarle el nombre:")
                .setView(nombreDestino)
                .setCancelable(true)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        destinoManager.checkDestino(history);
                    }
                });

        return saveDialogBuilder;
    }
}
