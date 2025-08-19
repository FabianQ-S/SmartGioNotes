package com.sgionotes.dialogs;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;
import com.sgionotes.R;

public class LocationConfirmationDialog {

    public interface LocationConfirmationListener {
        void onConfirmLocation();
        void onCancel();
    }

    private Dialog dialog;
    private LocationConfirmationListener listener;

    public LocationConfirmationDialog(Context context, LocationConfirmationListener listener) {
        this.listener = listener;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_location_confirmation, null);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }
        setupButtons(dialogView);

        dialog.setOnCancelListener(dialogInterface -> {
            if (listener != null) {
                listener.onCancel();
            }
        });
    }

    private void setupButtons(View dialogView) {
        MaterialButton btnAgregarUbicacion = dialogView.findViewById(R.id.btnAgregarUbicacion);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        btnAgregarUbicacion.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmLocation();
            }
            dismiss();
        });

        btnCancelar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
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
