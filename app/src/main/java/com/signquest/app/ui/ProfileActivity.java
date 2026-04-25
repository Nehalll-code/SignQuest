package com.signquest.app.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;

/**
 * ProfileActivity — Gamified Login / Profile Creation Screen.
 *
 * Children:
 *   1. Type their name
 *   2. Choose an avatar buddy (Panda, Lion, Rabbit)
 *   3. Choose a sign language (ASL, ISL, BSL)
 *   4. Tap "Play!" to begin → WorldMapActivity
 */
public class ProfileActivity extends AppCompatActivity {

    private ProfileManager profileManager;
    private String userId;

    // Views
    private TextView tvLogoEmoji, tvWelcomeTitle, tvWelcomeSubtitle;
    private MaterialCardView cardMain, cardPikachu, cardCharmander, cardBulbasaur, cardSquirtle;
    private MaterialCardView cardASL, cardISL, cardBSL;
    private TextInputLayout tilName;
    private EditText etName;
    private LinearLayout layoutAvatars, layoutLanguages;
    private com.google.android.material.materialswitch.MaterialSwitch switchMute;
    private MaterialButton btnPlay;

    private int selectedAvatarId = ProfileManager.AVATAR_NONE;
    private String selectedLanguage = "ASL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get user ID from intent (for new or existing users)
        userId = getIntent().getStringExtra("user_id");
        if (userId == null || userId.isEmpty()) {
            userId = ProfileManager.generateUserId();
        }
        profileManager = new ProfileManager(this, userId);
        bindViews();

        if (profileManager.hasProfile()) {
            etName.setText(profileManager.getChildName());
            selectAvatar(profileManager.getAvatarId());
            selectLanguage(profileManager.getSignLanguage());
        }
        
        switchMute.setChecked(profileManager.isSoundMuted());
        switchMute.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileManager.setSoundMuted(isChecked);
        });

        setupAvatarSelection();
        setupLanguageSelection();
        setupPlayButton();
        playEntranceAnimations();
    }

    private void bindViews() {
        tvLogoEmoji       = findViewById(R.id.tvLogoEmoji);
        tvWelcomeTitle    = findViewById(R.id.tvWelcomeTitle);
        tvWelcomeSubtitle = findViewById(R.id.tvWelcomeSubtitle);
        cardMain          = findViewById(R.id.cardMain);
        tilName           = findViewById(R.id.tilName);
        etName            = findViewById(R.id.etName);
        layoutAvatars     = findViewById(R.id.layoutAvatars);
        cardPikachu       = findViewById(R.id.cardPikachu);
        cardCharmander    = findViewById(R.id.cardCharmander);
        cardBulbasaur     = findViewById(R.id.cardBulbasaur);
        cardSquirtle      = findViewById(R.id.cardSquirtle);
        switchMute        = findViewById(R.id.switchMute);
        btnPlay           = findViewById(R.id.btnPlay);

        // Language cards
        cardASL  = findViewById(R.id.cardASL);
        cardISL  = findViewById(R.id.cardISL);
        cardBSL  = findViewById(R.id.cardBSL);
        layoutLanguages = findViewById(R.id.layoutLanguages);
    }

    // ── Avatar Selection ──

    private void setupAvatarSelection() {
        cardPikachu.setOnClickListener(v -> selectAvatar(ProfileManager.AVATAR_PIKACHU));
        cardCharmander.setOnClickListener(v -> selectAvatar(ProfileManager.AVATAR_CHARMANDER));
        cardBulbasaur.setOnClickListener(v -> selectAvatar(ProfileManager.AVATAR_BULBASAUR));
        cardSquirtle.setOnClickListener(v -> selectAvatar(ProfileManager.AVATAR_SQUIRTLE));
    }

    private void selectAvatar(int avatarId) {
        this.selectedAvatarId = avatarId;

        int selectedColor = ContextCompat.getColor(this, R.color.avatarSelectedStroke);
        int defaultColor  = ContextCompat.getColor(this, R.color.avatarDefaultStroke);

        cardPikachu.setStrokeColor(defaultColor);
        cardCharmander.setStrokeColor(defaultColor);
        cardBulbasaur.setStrokeColor(defaultColor);
        cardSquirtle.setStrokeColor(defaultColor);
        cardPikachu.setStrokeWidth(dpToPx(3));
        cardCharmander.setStrokeWidth(dpToPx(3));
        cardBulbasaur.setStrokeWidth(dpToPx(3));
        cardSquirtle.setStrokeWidth(dpToPx(3));

        MaterialCardView selectedCard = getCardForAvatar(avatarId);
        if (selectedCard != null) {
            selectedCard.setStrokeColor(selectedColor);
            selectedCard.setStrokeWidth(dpToPx(4));
            bounceView(selectedCard);
        }
    }

    private MaterialCardView getCardForAvatar(int avatarId) {
        switch (avatarId) {
            case ProfileManager.AVATAR_PIKACHU:  return cardPikachu;
            case ProfileManager.AVATAR_CHARMANDER:   return cardCharmander;
            case ProfileManager.AVATAR_BULBASAUR: return cardBulbasaur;
            case ProfileManager.AVATAR_SQUIRTLE: return cardSquirtle;
            default: return null;
        }
    }

    // ── Language Selection ──

    private void setupLanguageSelection() {
        cardASL.setOnClickListener(v -> selectLanguage("ASL"));
        cardISL.setOnClickListener(v -> selectLanguage("ISL"));
        cardBSL.setOnClickListener(v -> selectLanguage("BSL"));
    }

    private void selectLanguage(String lang) {
        this.selectedLanguage = lang;

        int selectedColor = ContextCompat.getColor(this, R.color.avatarSelectedStroke);
        int defaultColor  = ContextCompat.getColor(this, R.color.avatarDefaultStroke);

        cardASL.setStrokeColor(defaultColor);
        cardISL.setStrokeColor(defaultColor);
        cardBSL.setStrokeColor(defaultColor);
        cardASL.setStrokeWidth(dpToPx(3));
        cardISL.setStrokeWidth(dpToPx(3));
        cardBSL.setStrokeWidth(dpToPx(3));

        MaterialCardView selectedCard;
        switch (lang) {
            case "ISL": selectedCard = cardISL; break;
            case "BSL": selectedCard = cardBSL; break;
            default:    selectedCard = cardASL; break;
        }
        selectedCard.setStrokeColor(selectedColor);
        selectedCard.setStrokeWidth(dpToPx(4));
        bounceView(selectedCard);
    }

    // ── Play Button ──

    private void setupPlayButton() {
        btnPlay.setOnClickListener(v -> {
            String name = etName.getText() != null
                    ? etName.getText().toString().trim()
                    : "";

            if (name.isEmpty()) {
                tilName.setError(getString(R.string.error_empty_name));
                shakeView(tilName);
                return;
            } else {
                tilName.setError(null);
            }

            if (selectedAvatarId == ProfileManager.AVATAR_NONE) {
                Toast.makeText(this, R.string.error_no_avatar, Toast.LENGTH_SHORT).show();
                shakeView(layoutAvatars);
                return;
            }

            profileManager.saveProfile(name, selectedAvatarId, selectedLanguage);

            Toast.makeText(this,
                    "Welcome, " + name + "! Let's learn some signs!",
                    Toast.LENGTH_LONG).show();

            // Navigate to World Map
            Intent intent = new Intent(this, WorldMapActivity.class);
            startActivity(intent);
            finish(); // prevent back to profile screen
        });
    }

    // ── Animations ──

    private void playEntranceAnimations() {
        View[] views = { tvLogoEmoji, tvWelcomeTitle,
                tvWelcomeSubtitle, cardMain, btnPlay };

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

    private void bounceView(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(350);
        set.setInterpolator(new OvershootInterpolator(2.5f));
        set.start();
    }

    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX",
                0f, -12f, 12f, -8f, 8f, -4f, 4f, 0f);
        shake.setDuration(400);
        shake.start();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
