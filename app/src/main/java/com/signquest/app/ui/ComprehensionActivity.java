package com.signquest.app.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.signquest.app.R;
import com.signquest.app.data.DatabaseHelper;
import com.signquest.app.data.ProfileManager;
import com.signquest.app.data.SignDataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ComprehensionActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_KEY = "target_key";
    public static final String EXTRA_LEVEL_ID = "level_id";
    public static final String EXTRA_LANGUAGE = "language";

    private String targetKey;
    private int levelId;
    private String language;
    private ProfileManager profileManager;
    private DatabaseHelper databaseHelper;

    private TextView tvPrompt;
    private MaterialCardView[] cards = new MaterialCardView[3];
    private TextView[] tvEmojis = new TextView[3];
    private TextView[] tvLabels = new TextView[3];

    private SignDataProvider.SignItem targetSign;
    private List<SignDataProvider.SignItem> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprehension);
        profileManager = new ProfileManager(this);
        databaseHelper = new DatabaseHelper(this);

        targetKey = getIntent().getStringExtra(EXTRA_TARGET_KEY);
        levelId = getIntent().getIntExtra(EXTRA_LEVEL_ID, -1);
        language = getIntent().getStringExtra(EXTRA_LANGUAGE);

        if (targetKey == null || levelId == -1 || language == null) {
            finish();
            return;
        }

        bindViews();
        loadData();
    }

    private void bindViews() {
        tvPrompt = findViewById(R.id.tvPrompt);

        cards[0] = findViewById(R.id.cardOption1);
        cards[1] = findViewById(R.id.cardOption2);
        cards[2] = findViewById(R.id.cardOption3);

        tvEmojis[0] = findViewById(R.id.tvEmoji1);
        tvEmojis[1] = findViewById(R.id.tvEmoji2);
        tvEmojis[2] = findViewById(R.id.tvEmoji3);

        tvLabels[0] = findViewById(R.id.tvLabel1);
        tvLabels[1] = findViewById(R.id.tvLabel2);
        tvLabels[2] = findViewById(R.id.tvLabel3);
    }

    private void loadData() {
        List<SignDataProvider.SignItem> allSigns = SignDataProvider.getSigns(language, levelId);
        options = new ArrayList<>();

        for (SignDataProvider.SignItem item : allSigns) {
            if (item.getKey().equals(targetKey)) {
                targetSign = item;
            }
        }

        if (targetSign == null) {
            finish();
            return;
        }

        tvPrompt.setText("Which one is '" + targetSign.getDisplayLabel() + "'?");

        // Setup random distractors
        List<SignDataProvider.SignItem> distractors = new ArrayList<>(allSigns);
        distractors.remove(targetSign);
        Collections.shuffle(distractors);

        options.add(targetSign);
        if (distractors.size() >= 2) {
            options.add(distractors.get(0));
            options.add(distractors.get(1));
        }

        Collections.shuffle(options);

        // Bind data
        for (int i = 0; i < 3; i++) {
            if (i < options.size()) {
                SignDataProvider.SignItem opt = options.get(i);
                tvEmojis[i].setText(opt.getEmoji());
                tvLabels[i].setText(opt.getDisplayLabel());

                final int index = i;
                cards[i].setOnClickListener(v -> onCardSelected(index, opt));
            }
        }
    }

    private void onCardSelected(int index, SignDataProvider.SignItem selected) {
        if (selected.getKey().equals(targetKey)) {
            // Correct
            databaseHelper.logComprehensionResult(profileManager.getChildName(), targetKey, true);
            playSuccess(cards[index]);
        } else {
            // Incorrect
            databaseHelper.logComprehensionResult(profileManager.getChildName(), targetKey, false);
            playShake(cards[index]);
        }
    }

    private void playShake(MaterialCardView card) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        card.startAnimation(shake);
    }

    private void playSuccess(MaterialCardView card) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(400);
        scaleY.setDuration(400);
        scaleX.start();
        scaleY.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish(); // Return to world map
        }, 800);
    }
}
