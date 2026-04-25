package com.signquest.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;
import com.signquest.app.data.SignDataProvider;

import java.util.List;

/**
 * LevelDetailActivity — Shows a grid of all signs in a level.
 *
 * Each cell shows the sign's emoji + label + completion star.
 * Tapping a cell launches DetectionActivity for that specific sign.
 */
public class LevelDetailActivity extends AppCompatActivity
        implements SignGridAdapter.OnSignClickListener {

    private ProfileManager profileManager;
    private String language;
    private int levelId;
    private SignGridAdapter adapter;

    private TextView tvLevelTitle, tvLevelProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_detail);

        profileManager = new ProfileManager(this);
        language = getIntent().getStringExtra("language");
        levelId = getIntent().getIntExtra("level_id", SignDataProvider.LEVEL_ALPHABETS);

        if (language == null) language = profileManager.getSignLanguage();

        bindViews();
        setupGrid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.refreshStates();
        updateProgress();
    }

    private void bindViews() {
        tvLevelTitle    = findViewById(R.id.tvLevelTitle);
        tvLevelProgress = findViewById(R.id.tvLevelProgress);
        ImageView btnBack = findViewById(R.id.btnLevelBack);
        btnBack.setOnClickListener(v -> finish());

        // Set title
        List<SignDataProvider.LevelInfo> levels = SignDataProvider.getLevels();
        for (SignDataProvider.LevelInfo info : levels) {
            if (info.getId() == levelId) {
                tvLevelTitle.setText(info.getEmoji() + " " + info.getTitle());
                break;
            }
        }
    }

    private void setupGrid() {
        RecyclerView rvSigns = findViewById(R.id.rvSigns);

        List<SignDataProvider.SignItem> signs = SignDataProvider.getSigns(language, levelId);

        // 3 columns for alphabets/numbers, 2 for category levels (longer labels)
        int spanCount = (levelId <= SignDataProvider.LEVEL_NUMBERS) ? 3 : 2;
        rvSigns.setLayoutManager(new GridLayoutManager(this, spanCount));

        adapter = new SignGridAdapter(signs, profileManager, this);
        rvSigns.setAdapter(adapter);

        updateProgress();
    }

    private void updateProgress() {
        int completed = profileManager.getCompletedCount(language, levelId);
        int total = SignDataProvider.getTotalSignCount(levelId);
        tvLevelProgress.setText(completed + "/" + total);
    }

    @Override
    public void onSignClick(SignDataProvider.SignItem sign) {
        Intent intent = new Intent(this, DetectionActivity.class);
        intent.putExtra("language", language);
        intent.putExtra("level_id", levelId);
        intent.putExtra("sign_key", sign.getKey());
        intent.putExtra("sign_label", sign.getDisplayLabel());
        intent.putExtra("sign_instructions", sign.getInstructions());
        startActivity(intent);
    }
}
