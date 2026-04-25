package com.signquest.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;

public class ParentDashboardActivity extends AppCompatActivity {

    private ProfileManager profileManager;

    private TextView tvSentenceSummary;
    private TextView tvStatsSigns;
    private TextView tvStatsModules;
    private TextView tvStatsTime;
    private ImageView btnClose;
    private MaterialButton btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        profileManager = new ProfileManager(this);

        tvSentenceSummary = findViewById(R.id.tvSentenceSummary);
        tvStatsSigns = findViewById(R.id.tvStatsSigns);
        tvStatsModules = findViewById(R.id.tvStatsModules);
        tvStatsTime = findViewById(R.id.tvStatsTime);
        btnClose = findViewById(R.id.btnClose);
        btnShare = findViewById(R.id.btnShare);

        btnClose.setOnClickListener(v -> finish());

        loadStats();
    }

    private void loadStats() {
        int minutes = profileManager.getTotalTimeMinutes();
        int signsLearned = profileManager.getSignsLearnedCount();
        int modulesCompleted = signsLearned >= 26 ? 1 : 0; // rough baseline for mock stat

        tvStatsSigns.setText(String.valueOf(signsLearned));
        tvStatsTime.setText(String.valueOf(minutes));
        tvStatsModules.setText(String.valueOf(modulesCompleted));

        String childName = profileManager.getChildName();
        if (childName == null || childName.isEmpty()) childName = "Your child";

        String summary = String.format("%s learned %d new signs this week and practiced for %d minutes!",
                childName, signsLearned, minutes);
        tvSentenceSummary.setText(summary);

        btnShare.setOnClickListener(v -> shareSummary(summary));
    }

    private void shareSummary(String summary) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "SignQuest Weekly Update:\n" + summary);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}
