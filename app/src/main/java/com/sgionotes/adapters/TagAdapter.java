package com.sgionotes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sgionotes.R;
import com.sgionotes.models.Tag;
import com.sgionotes.models.GenerarData;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<Tag> listaTags;
    private Context context;

    public TagAdapter(List<Tag> listaTags) {
        this.listaTags = listaTags;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_tag_editable, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = listaTags.get(position);
        holder.etTagText.setText(tag.getDisplayText());

        // VerificacionDeFavorito
        if (tag.isFavorite()) {
            holder.ivTagIcon.setVisibility(View.GONE);
            holder.ivFavoriteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivTagIcon.setVisibility(View.VISIBLE);
            holder.ivFavoriteIcon.setVisibility(View.GONE);
        }

        holder.btnMore.setOnClickListener(v -> showPopupMenu(v, position));

        holder.etTagText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                finishEditing(holder, position);
                return true;
            }
            return false;
        });

        holder.etTagText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                finishEditing(holder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTags.size();
    }

    private void showPopupMenu(View anchor, int position) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_tag, null, false);

        // PopupMedir
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();

        PopupWindow popupWindow = new PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT, true);

        LinearLayout menuAddFavorite = popupView.findViewById(R.id.menuAddFavorite);
        LinearLayout menuEdit = popupView.findViewById(R.id.menuEdit);
        LinearLayout menuDelete = popupView.findViewById(R.id.menuDelete);

        Tag currentTag = listaTags.get(position);
        TextView favoriteText = menuAddFavorite.findViewById(android.R.id.text1);
        if (favoriteText == null) {
            favoriteText = (TextView) ((LinearLayout) menuAddFavorite).getChildAt(1);
        }
        if (currentTag.isFavorite()) {
            favoriteText.setText("Quitar de favoritas");
        } else {
            favoriteText.setText("Añadir a favoritas");
        }

        menuAddFavorite.setOnClickListener(v -> {
            String tagName = currentTag.getEtiquetaDescripcion();
            boolean newFavoriteStatus = !currentTag.isFavorite();

            // Actualizar localmente
            currentTag.setFavorite(newFavoriteStatus);

            // Guardar en Firestore
            if (currentTag.getId() != null && !currentTag.getId().isEmpty()) {
                GenerarData.getInstance().getFirestoreRepository()
                        .setTagFavorite(currentTag.getId(), newFavoriteStatus,
                                new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                                    @Override
                                    public void onSuccess() {
                                        if (newFavoriteStatus) {
                                            Toast.makeText(context, "'" + tagName + "' añadida a favoritas", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "'" + tagName + "' quitada de favoritas", Toast.LENGTH_SHORT).show();
                                        }
                                        sortTags();
                                        notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        // Revertir cambio local si falla
                                        currentTag.setFavorite(!newFavoriteStatus);
                                        Toast.makeText(context, "Error al actualizar favorito: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
            }
            popupWindow.dismiss();
        });

        menuEdit.setOnClickListener(v -> {
            enableEditing(position);
            popupWindow.dismiss();
        });

        menuDelete.setOnClickListener(v -> {
            String tagName = currentTag.getEtiquetaDescripcion();

            // Eliminar de Firestore primero
            if (currentTag.getId() != null && !currentTag.getId().isEmpty()) {
                GenerarData.getInstance().getFirestoreRepository()
                        .deleteTag(currentTag.getId(), new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                // Eliminar de la lista local solo si se eliminó exitosamente de Firestore
                                listaTags.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Etiqueta '" + tagName + "' eliminada", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(context, "Error al eliminar etiqueta: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Si no tiene ID, eliminar solo localmente
                listaTags.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Etiqueta '" + tagName + "' eliminada", Toast.LENGTH_SHORT).show();
            }
            popupWindow.dismiss();
        });

        // CalcularPosicion
        int xOffset = anchor.getWidth() - popupWidth;
        popupWindow.showAsDropDown(anchor, xOffset, 0);
    }

    private void sortTags() {
        listaTags.sort((tag1, tag2) -> {
            if (tag1.isFavorite() && !tag2.isFavorite()) {
                return -1;
            } else if (!tag1.isFavorite() && tag2.isFavorite()) {
                return 1;
            } else if (tag1.isFavorite() && tag2.isFavorite()) {
                return Long.compare(tag1.getFavoriteTimestamp(), tag2.getFavoriteTimestamp());
            } else {
                return 0;
            }
        });
    }

    private void enableEditing(int position) {
        notifyItemChanged(position);
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            RecyclerView recyclerView = (RecyclerView) ((android.app.Activity) context).findViewById(R.id.recyclerTags);
            if (recyclerView != null) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder instanceof TagViewHolder) {
                    TagViewHolder holder = (TagViewHolder) viewHolder;
                    holder.etTagText.setFocusable(true);
                    holder.etTagText.setFocusableInTouchMode(true);
                    holder.etTagText.requestFocus();
                    holder.etTagText.selectAll();

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(holder.etTagText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 100);
    }

    private void finishEditing(TagViewHolder holder, int position) {
        String newText = holder.etTagText.getText().toString().trim();
        if (!newText.isEmpty()) {
            Tag tag = listaTags.get(position);
            String oldText = tag.getEtiquetaDescripcion();

            // Actualizar localmente
            tag.setEtiquetaDescripcion(newText);

            // Guardar en Firestore si tiene ID
            if (tag.getId() != null && !tag.getId().isEmpty()) {
                GenerarData.getInstance().getFirestoreRepository()
                        .saveTag(tag, new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(context, "Etiqueta actualizada", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error) {
                                // Revertir cambio local si falla
                                tag.setEtiquetaDescripcion(oldText);
                                Toast.makeText(context, "Error al actualizar etiqueta: " + error, Toast.LENGTH_SHORT).show();
                                notifyItemChanged(position);
                            }
                        });
            } else {
                Toast.makeText(context, "Etiqueta actualizada localmente", Toast.LENGTH_SHORT).show();
            }
        }

        holder.etTagText.setFocusable(false);
        holder.etTagText.setFocusableInTouchMode(false);
        holder.etTagText.clearFocus();

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(holder.etTagText.getWindowToken(), 0);
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        EditText etTagText;
        ImageButton btnMore;
        ImageView ivFavoriteIcon; // IconoDeFavorito
        ImageView ivTagIcon; // IconoDeEtiqueta

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            etTagText = itemView.findViewById(R.id.etTagText);
            btnMore = itemView.findViewById(R.id.btnMore);
            ivFavoriteIcon = itemView.findViewById(R.id.ivFavoriteIcon); // Iicono de favorito
            ivTagIcon = itemView.findViewById(R.id.ivTagIcon); // Icono de etiqueta
        }
    }
}
