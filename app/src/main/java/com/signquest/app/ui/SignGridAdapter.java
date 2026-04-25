package com.signquest.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;
import com.signquest.app.data.SignDataProvider;

import java.util.List;

/**
 * SignGridAdapter — RecyclerView adapter for the level detail sign grid.
 */
public class SignGridAdapter extends RecyclerView.Adapter<SignGridAdapter.ViewHolder> {

    public interface OnSignClickListener {
        void onSignClick(SignDataProvider.SignItem sign);
    }

    private final List<SignDataProvider.SignItem> signs;
    private final ProfileManager profileManager;
    private final OnSignClickListener listener;

    public SignGridAdapter(List<SignDataProvider.SignItem> signs,
                           ProfileManager profileManager,
                           OnSignClickListener listener) {
        this.signs = signs;
        this.profileManager = profileManager;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sign_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SignDataProvider.SignItem sign = signs.get(position);
        boolean completed = profileManager.isSignCompleted(
                sign.getLanguage(), sign.getLevelId(), sign.getKey());

        holder.tvEmoji.setText(sign.getEmoji());
        holder.tvLabel.setText(sign.getDisplayLabel());

        if (completed) {
            holder.tvStatus.setText("⭐");
            holder.tvStatus.setTextSize(16);
            holder.card.setAlpha(1f);
        } else {
            holder.tvStatus.setText("○");
            holder.tvStatus.setTextSize(16);
            holder.card.setAlpha(0.85f);
        }

        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSignClick(sign);
            }
        });
    }

    @Override
    public int getItemCount() {
        return signs.size();
    }

    /** Refresh completion states. */
    public void refreshStates() {
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvEmoji;
        final TextView tvLabel;
        final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card     = itemView.findViewById(R.id.cardSign);
            tvEmoji  = itemView.findViewById(R.id.tvSignEmoji);
            tvLabel  = itemView.findViewById(R.id.tvSignLabel);
            tvStatus = itemView.findViewById(R.id.tvSignStatus);
        }
    }
}
