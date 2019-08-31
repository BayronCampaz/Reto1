package edu.icesi.reto1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

public class AddDialog extends AppCompatDialogFragment {

    private EditText nameMarkerEt;
    private AddDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_marker, null);
        builder.setView(view).setTitle("Agregar marcador")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String markerName = nameMarkerEt.getText().toString();
                        listener.getMarkerName(markerName);
                    }
                });
        nameMarkerEt = view.findViewById(R.id.nameMarker);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (AddDialogListener) context;
        }catch (ClassCastException e){
            throw  new ClassCastException(context.toString());
        }

    }

    public interface  AddDialogListener{
        void getMarkerName(String markerName);
    }
}
