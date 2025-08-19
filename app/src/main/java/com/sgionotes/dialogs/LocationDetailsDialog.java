package com.sgionotes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.sgionotes.R;
import com.sgionotes.utils.LocationHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationDetailsDialog {

    public interface LocationDetailsListener {
        void onDeleteLocation();
        void onClose();
    }
    private Dialog dialog;
    private LocationDetailsListener listener;
    private TextView txtCoordinates;
    private TextView txtFechaGuardada;

    public LocationDetailsDialog(Context context, String coordinates, long timestamp, LocationDetailsListener listener) {
        this.listener = listener;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_location_details, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }

        initViews(dialogView);
        setupData(coordinates, timestamp);
        setupButtons(dialogView);

        //SalirDialog
        dialog.setOnCancelListener(dialogInterface -> {
            if (listener != null) {
                listener.onClose();
            }
        });
    }

    private void initViews(View dialogView) {
        txtCoordinates = dialogView.findViewById(R.id.txtCoordinates);
        txtFechaGuardada = dialogView.findViewById(R.id.txtFechaGuardada);
    }

    private void setupData(String coordinates, long timestamp) {
        if (coordinates != null && !coordinates.trim().isEmpty()) {
            txtCoordinates.setText(LocationHelper.getReadableLocation(coordinates));
        } else {
            txtCoordinates.setText("Coordenadas no disponibles");
        }

        if (timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaFormateada = sdf.format(new Date(timestamp));
            txtFechaGuardada.setText(fechaFormateada);
        } else {
            txtFechaGuardada.setText("Fecha no disponible");
        }
    }

    private void setupButtons(View dialogView) {
        MaterialButton btnEliminarUbicacion = dialogView.findViewById(R.id.btnEliminarUbicacion);
        MaterialButton btnCerrar = dialogView.findViewById(R.id.btnCerrar);
        btnEliminarUbicacion.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteLocation();
            }
            dismiss();
        });
        btnCerrar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClose();
            }
            dismiss();
        });
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
