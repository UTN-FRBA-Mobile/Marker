package com.marker.lugar.destino;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.marker.app.GestorSesion;
import com.marker.lugar.history.History;


public class GuardarDestinoFragment extends DialogFragment {

    private History history;
    private DestinoManager destinoManager;

    private Context context;

    private EditText nombreDestino;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        nombreDestino = new EditText(getContext());

        Bundle bundle = getArguments();
        if(bundle != null){
            history = bundle.getParcelable("history");
        }

        if(savedInstanceState != null) {
            nombreDestino.setText(savedInstanceState.getString("nombreDestino"));
        }

        context = getContext();

        initializeDestinoManager();

        return saveDialogBuilder().create();
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        if(nombreDestino.getText() != null) {
            outState.putString("nombreDestino", nombreDestino.getText().toString());
        }

        super.onSaveInstanceState(outState);
    }

    private void initializeDestinoManager() {
        destinoManager = new DestinoManager(GestorSesion.getInstancia(getContext()).getUsuarioLoggeado()) {
            @Override
            protected void onCheckDestino(boolean result) {
                if(result == true) {
                    //No existe destino
                    String nombre = TextUtils.isEmpty(nombreDestino.getText()) ? history.nombre : nombreDestino.getText().toString();
                    destinoManager.writeDestino(nombre, history.posicion);
                    Toast.makeText(context, "Destino guardado", Toast.LENGTH_LONG).show();
                } else {
                    //Destino repetido
                    Toast.makeText(context, "Ya existe un destino con ese nombre", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private AlertDialog.Builder saveDialogBuilder(){
        AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(String.format("¿Guardar %s en \"Mis Destinos\"?", history.nombre))
                .setMessage("Puede cambiarle el nombre:")
                .setView(nombreDestino)
                .setCancelable(true)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String nombre = TextUtils.isEmpty(nombreDestino.getText()) ? history.nombre : nombreDestino.getText().toString();
                        destinoManager.checkDestino(nombre);
                    }
                });

        return saveDialogBuilder;
    }
}
