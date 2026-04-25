package com.signquest.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;
import com.signquest.app.data.SignDataProvider;

import java.util.List;

/**
 * WorldMapActivity — Level selection screen.
 *
 * Dynamically renders all 10 levels from SignDataProvider in a 2-column grid
 * with emoji icons, progress badges, and lock overlays for unearned levels.
 */
public class WorldMapActivity extends AppCompatActivity {

    private ProfileManager profileManager;
    private String language;
    private LinearLayout layoutLevelsContainer;

    // Header views
    private ImageView ivPlayerAvatar;
    private TextView tvPlayerGreeting, tvPlayerLanguage, tvBadgesCount;

    // Color palette for level cards (cycles through these)
    private static final int[] CARD_COLORS = {
            0xFF4CAF50, // Green
            0xFF2196F3, // Blue
            0xFFFF9800, // Orange
            0xFF9C27B0, // Purple
            0xFFE91E63, // Pink
            0xFF00BCD4, // Cyan
            0xFFFF5722, // Deep Orange
            0xFF3F51B5, // Indigo
            0xFF009688, // Teal
            0xFF795548, // Brown
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_map);

        profileManager = new ProfileManager(this);
        language = profileManager.getSignLanguage();

        bindViews();
        setupPlayerHeader();
        buildLevelGrid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        language = profileManager.getSignLanguage();
        refreshGrid();
        updateBadges();
    }

    private void bindViews() {
        ImageView btnDashboard = findViewById(R.id.btnDashboard);
        btnDashboard.setOnClickListener(v ->
                startActivity(new Intent(this, ParentDashboardActivity.class)));

        ImageView btnStory = findViewById(R.id.btnStory);
        btnStory.setOnClickListener(v ->
                startActivity(new Intent(this, StoryActivity.class)));

        ImageView btnAR = findViewById(R.id.btnAR);
        btnAR.setOnClickListener(v ->
                startActivity(new Intent(this, ARMapActivity.class)));

        ImageView btnSwitchUser = findViewById(R.id.btnSwitchUser);
        btnSwitchUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserSelectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        ivPlayerAvatar   = findViewById(R.id.ivPlayerAvatar);
        tvPlayerGreeting = findViewById(R.id.tvPlayerGreeting);
        tvPlayerLanguage = findViewById(R.id.tvPlayerLanguage);
        tvBadgesCount    = findViewById(R.id.tvBadgesCount);

        layoutLevelsContainer = findViewById(R.id.layoutLevelsGrid);
    }

    private void setupPlayerHeader() {
        String name = profileManager.getChildName();
        int avatarId = profileManager.getAvatarId();

        tvPlayerGreeting.setText("Hey, " + name + "!");

        String langFlag;
        switch (language) {
            case "ISL": langFlag = "ISL 🇮🇳"; break;
            case "BSL": langFlag = "BSL 🇬🇧"; break;
            default:    langFlag = "ASL 🇺🇸"; break;
        }
        tvPlayerLanguage.setText("Learning: " + langFlag);

        int avatarResId;
        switch (avatarId) {
            case ProfileManager.AVATAR_CHARMANDER: avatarResId = R.drawable.ic_charmander; break;
            case ProfileManager.AVATAR_BULBASAUR:  avatarResId = R.drawable.ic_bulbasaur; break;
            case ProfileManager.AVATAR_SQUIRTLE:   avatarResId = R.drawable.ic_squirtle; break;
            default:                                avatarResId = R.drawable.ic_pikachu; break;
        }
        ivPlayerAvatar.setImageResource(avatarResId);
    }

    // ════════════════════════════════════════════════════════════════
    //  Dynamic Level Grid (built with LinearLayout rows)
    // ════════════════════════════════════════════════════════════════

    private void buildLevelGrid() {
        layoutLevelsContainer.removeAllViews();
        List<SignDataProvider.LevelInfo> levels = SignDataProvider.getLevels();

        LinearLayout currentRow = null;

        for (int i = 0; i < levels.size(); i++) {
            SignDataProvider.LevelInfo level = levels.get(i);
            boolean unlocked = profileManager.isLevelUnlocked(language, level.getId());
            int completed = profileManager.getCompletedCount(language, level.getId());
            int total = SignDataProvider.getTotalSignCount(level.getId());

            // Create a new row every 2 items
            if (i % 2 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(2);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.bottomMargin = dpToPx(12);
                currentRow.setLayoutParams(rowParams);
                layoutLevelsContainer.addView(currentRow);
            }

            View card = createLevelCard(level, unlocked, completed, total, i);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            cardParams.setMargins(dpToPx(6), 0, dpToPx(6), 0);
            card.setLayoutParams(cardParams);

            if (currentRow != null) {
                currentRow.addView(card);
            }
        }

        // If odd number of levels, add an invisible spacer to the last row
        if (levels.size() % 2 != 0 && currentRow != null) {
            View spacer = new View(this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    0, 1, 1f);
            spacer.setLayoutParams(spacerParams);
            currentRow.addView(spacer);
        }
    }

    private View createLevelCard(SignDataProvider.LevelInfo level,
                                  boolean unlocked, int completed, int total, int index) {
        int cardColor = CARD_COLORS[index % CARD_COLORS.length];

        // Root frame for the card
        FrameLayout root = new FrameLayout(this);

        // Card background
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(20));
        bg.setColor(unlocked ? cardColor : 0xFF424242);
        root.setBackground(bg);
        root.setElevation(dpToPx(6));
        root.setClipToOutline(true);

        int padH = dpToPx(16);
        int padV = dpToPx(14);

        // Content layout
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(padH, padV, padH, padV);

        // Level number badge
        TextView levelNum = new TextView(this);
        levelNum.setText("Level " + level.getId());
        levelNum.setTextColor(0x99FFFFFF);
        levelNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        levelNum.setTypeface(null, Typeface.BOLD);
        content.addView(levelNum);

        // Emoji
        TextView emoji = new TextView(this);
        emoji.setText(level.getEmoji());
        emoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
        emoji.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.topMargin = dpToPx(4);
        emojiParams.bottomMargin = dpToPx(4);
        emoji.setLayoutParams(emojiParams);
        content.addView(emoji);

        // Title
        TextView title = new TextView(this);
        title.setText(level.getTitle());
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        // Progress text
        TextView progress = new TextView(this);
        progress.setText(completed + "/" + total);
        progress.setTextColor(0xBBFFFFFF);
        progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        progress.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progressParams.topMargin = dpToPx(4);
        progress.setLayoutParams(progressParams);
        content.addView(progress);

        // Progress bar track
        FrameLayout barFrame = new FrameLayout(this);
        LinearLayout.LayoutParams barFrameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6));
        barFrameParams.topMargin = dpToPx(6);
        barFrame.setLayoutParams(barFrameParams);

        View track = new View(this);
        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setCornerRadius(dpToPx(4));
        trackBg.setColor(0x33FFFFFF);
        track.setBackground(trackBg);
        barFrame.addView(track, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Progress fill overlay
        if (total > 0 && completed > 0) {
            View fill = new View(this);
            GradientDrawable fillBg = new GradientDrawable();
            fillBg.setCornerRadius(dpToPx(4));
            fillBg.setColor(0xFFFFD54F);
            fill.setBackground(fillBg);
            FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(
                    0, FrameLayout.LayoutParams.MATCH_PARENT);
            fill.setLayoutParams(fillParams);
            barFrame.addView(fill);

            final int c = completed;
            final int t = total;
            barFrame.post(() -> {
                int tw = barFrame.getWidth();
                if (tw > 0) {
                    int fw = (int) ((float) c / t * tw);
                    fill.getLayoutParams().width = fw;
                    fill.requestLayout();
                }
            });
        }

        content.addView(barFrame);
        root.addView(content);

        // Lock overlay
        if (!unlocked) {
            FrameLayout lockOverlay = new FrameLayout(this);
            GradientDrawable lockBg = new GradientDrawable();
            lockBg.setCornerRadius(dpToPx(20));
            lockBg.setColor(0xCC000000);
            lockOverlay.setBackground(lockBg);

            TextView lockIcon = new TextView(this);
            lockIcon.setText("🔒");
            lockIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
            lockIcon.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams lockParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lockParams.gravity = Gravity.CENTER;
            lockIcon.setLayoutParams(lockParams);
            lockOverlay.addView(lockIcon);

            root.addView(lockOverlay, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }

        // Click handler
        int levelId = level.getId();
        String levelTitle = level.getTitle();
        root.setOnClickListener(v -> {
            if (profileManager.isLevelUnlocked(language, levelId)) {
                openLevel(levelId);
            } else {
                Toast.makeText(this,
                        "🔒 Complete the previous level to unlock " + levelTitle + "!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void openLevel(int levelId) {
        Intent intent = new Intent(this, LevelDetailActivity.class);
        intent.putExtra("level_id", levelId);
        intent.putExtra("language", language);
        startActivity(intent);
    }

    private void refreshGrid() {
        buildLevelGrid();
    }

    private void updateBadges() {
        int totalBadges = profileManager.getBadgesEarned();
        tvBadgesCount.setText(totalBadges + " badges earned");
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
