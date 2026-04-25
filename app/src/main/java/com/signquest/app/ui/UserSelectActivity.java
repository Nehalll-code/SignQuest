package com.signquest.app.ui;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;

import java.util.List;

/**
 * UserSelectActivity — "Who's Playing?" screen.
 *
 * Shows existing user profiles and lets:
 *   - Tap a user card → log in as that user → WorldMapActivity
 *   - Tap "New Explorer" → ProfileActivity with a fresh user ID
 *   - Long-press a card → delete that user (with confirmation)
 *
 * If no users exist yet, auto-redirects to ProfileActivity.
 */
public class UserSelectActivity extends AppCompatActivity {

    private LinearLayout layoutUserCards;
    private MaterialButton btnNewUser;
    private ProfileManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select);

        pm = new ProfileManager(this, "global_bootstrap");
        layoutUserCards = findViewById(R.id.layoutUserCards);
        btnNewUser = findViewById(R.id.btnNewUser);

        btnNewUser.setOnClickListener(v -> createNewUser());

        playEntranceAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserCards();
    }

    private void loadUserCards() {
        layoutUserCards.removeAllViews();

        List<String> userIds = pm.getAllUserIds();

        // If no users exist, go straight to profile creation
        if (userIds.isEmpty()) {
            createNewUser();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < userIds.size(); i++) {
            final String uid = userIds.get(i);

            View card = inflater.inflate(R.layout.item_user_card, layoutUserCards, false);

            ImageView ivAvatar   = card.findViewById(R.id.ivUserAvatar);
            TextView tvName      = card.findViewById(R.id.tvUserName);
            TextView tvLanguage  = card.findViewById(R.id.tvUserLanguage);

            String name = ProfileManager.getUserName(this, uid);
            int avatarId = ProfileManager.getUserAvatar(this, uid);
            String language = ProfileManager.getUserLanguage(this, uid);

            tvName.setText(name);
            tvLanguage.setText(formatLanguage(language));
            ivAvatar.setImageResource(getAvatarDrawable(avatarId));

            // Tap → login as this user
            card.setOnClickListener(v -> loginAsUser(uid));

            // Long press → delete
            card.setOnLongClickListener(v -> {
                confirmDeleteUser(uid, name);
                return true;
            });

            // Stagger animation
            card.setAlpha(0f);
            card.setTranslationX(-80f);
            card.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setStartDelay(i * 100L)
                    .setInterpolator(new OvershootInterpolator(1.0f))
                    .start();

            layoutUserCards.addView(card);
        }
    }

    private void loginAsUser(String userId) {
        pm.setActiveUserId(userId);
        Intent intent = new Intent(this, WorldMapActivity.class);
        startActivity(intent);
    }

    private void createNewUser() {
        String newId = ProfileManager.generateUserId();
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", newId);
        startActivity(intent);
    }

    private void confirmDeleteUser(String userId, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Remove " + name + "'s profile and all their progress? This can't be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    pm.deleteUser(userId);
                    loadUserCards();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatLanguage(String lang) {
        switch (lang) {
            case "ISL": return "ISL 🇮🇳";
            case "BSL": return "BSL 🇬🇧";
            default:    return "ASL 🇺🇸";
        }
    }

    private int getAvatarDrawable(int avatarId) {
        switch (avatarId) {
            case ProfileManager.AVATAR_CHARMANDER: return R.drawable.ic_charmander;
            case ProfileManager.AVATAR_BULBASAUR:  return R.drawable.ic_bulbasaur;
            case ProfileManager.AVATAR_SQUIRTLE:   return R.drawable.ic_squirtle;
            default:                               return R.drawable.ic_pikachu;
        }
    }

    private void playEntranceAnimation() {
        View[] views = {
                findViewById(R.id.tvLogo),
                findViewById(R.id.tvTitle),
                findViewById(R.id.tvSubtitle),
                btnNewUser
        };
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            view.setAlpha(0f);
            view.setTranslationY(60f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(i * 120L)
                    .setInterpolator(new OvershootInterpolator(1.0f))
                    .start();
        }
    }
}
