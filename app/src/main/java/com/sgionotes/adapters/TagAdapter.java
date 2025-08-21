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
        if (position < 0 || position >= listaTags.size()) {
            return;
        }
        Tag tag = listaTags.get(position);
        if (tag == null) {
            return;
        }
    holder.etTagText.setText(tag.getEtiquetaDescripcion());

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
        if (position < 0 || position >= listaTags.size()) {
            return;
        }
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

        if (position >= listaTags.size()) {
            popupWindow.dismiss();
            return;
        }

        Tag currentTag = listaTags.get(position);
        if (currentTag == null) {
            popupWindow.dismiss();
            return;
        }
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
            if (currentTag.getId() != null && !currentTag.getId().isEmpty()) {
                GenerarData.getInstance().getFirestoreRepository()
                        .setTagFavorite(currentTag.getId(), newFavoriteStatus,
                                new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(context, (newFavoriteStatus ? "'" + tagName + "' añadida a favoritas" : "'" + tagName + "' quitada de favoritas"), Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(context, "Error al actualizar favorito: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
            }
            popupWindow.dismiss();
        });
        menuEdit.setOnClickListener(v -> {
            if (position < listaTags.size()) {
                enableEditing(position);
            }
            popupWindow.dismiss();
        });
        menuDelete.setOnClickListener(v -> {
            String tagName = currentTag.getEtiquetaDescripcion();
            String tagId = currentTag.getId();

            // ValidarID
            if (tagId == null || tagId.isEmpty()) {
                Toast.makeText(context, "Error: ID de etiqueta inválido", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                return;
            }
            GenerarData.getInstance().removeTag(tagId,
                new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Etiqueta '" + tagName + "' eliminada", Toast.LENGTH_SHORT).show();
                        updateTagsListSafely();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error al eliminar etiqueta: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
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
    public void updateTagsList() {
        this.listaTags = GenerarData.getInstance().getListaEtiquetas();
        sortTags();
        notifyDataSetChanged();
    }
    private void updateTagsListSafely() {
        try {
            List<Tag> newTagsList = GenerarData.getInstance().getListaEtiquetas();
            if (newTagsList != null) {
                this.listaTags.clear();
                this.listaTags.addAll(newTagsList);
                sortTags();
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        notifyDataSetChanged();

                        // ActualizacionDeVentanas
                        GenerarData.getInstance().forceNotifyDataChanged();
                    });
                }
            }
        } catch (Exception e) {
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Error actualizando lista de etiquetas", Toast.LENGTH_SHORT).show();
                    GenerarData.getInstance().refreshData();
                });
            }
        }
    }

    private void finishEditing(TagViewHolder holder, int position) {
        if (position < 0 || position >= listaTags.size()) {
            return;
        }

        String newText = holder.etTagText.getText().toString().trim();
        Tag tag = listaTags.get(position);

        if (tag == null) {
            return;
        }

        if (!newText.isEmpty() && !newText.equals(tag.getEtiquetaDescripcion())) {
        String original = tag.getEtiquetaDescripcion();
        Tag updated = new Tag(tag.getId(), newText, tag.getUserId());
        updated.setFavorite(tag.isFavorite());
        updated.setFavoriteTimestamp(tag.getFavoriteTimestamp());
        GenerarData.getInstance().getFirestoreRepository()
            .saveTag(updated, new com.sgionotes.repository.FirestoreRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (context instanceof android.app.Activity) {
                                ((android.app.Activity) context).runOnUiThread(() -> {
                                    Toast.makeText(context, "Etiqueta actualizada", Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(position);
                                    GenerarData.getInstance().forceNotifyDataChanged();
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (context instanceof android.app.Activity) {
                                ((android.app.Activity) context).runOnUiThread(() -> {
                                    // RevertirCambio
                                    holder.etTagText.setText(original);
                                    Toast.makeText(context, "Error al actualizar etiqueta: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(holder.etTagText.getWindowToken(), 0);
        }

        holder.etTagText.clearFocus();
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
            ivFavoriteIcon = itemView.findViewById(R.id.ivFavoriteIcon);
            ivTagIcon = itemView.findViewById(R.id.ivTagIcon);
        }
    }
}
