package com.signquest.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.signquest.app.R;
import com.signquest.app.ml.GestureClassifier;
import com.signquest.app.ml.HandLandmarkerHelper;
import com.signquest.app.utils.SoundPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryActivity extends AppCompatActivity implements HandLandmarkerHelper.HandLandmarkerListener {

    private ViewPager2 viewPager;
    private StoryAdapter adapter;
    private PreviewView previewView;
    private HandOverlayView handOverlay;
    private TextView tvDetectedSign;
    private ProgressBar progressConfidence;
    private MaterialButton btnNextPanel;

    private HandLandmarkerHelper handLandmarkerHelper;
    private GestureClassifier gestureClassifier;
    private ExecutorService cameraExecutor;
    private SoundPlayer soundPlayer;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<StoryPanel> storyPanels;
    private int currentPosition = 0;

    private int correctStreakCount = 0;
    private static final int REQUIRED_STREAK = 2; // Shorter streak for story mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        soundPlayer = new SoundPlayer(this);
        gestureClassifier = new GestureClassifier();
        cameraExecutor = Executors.newSingleThreadExecutor();

        bindViews();
        loadStory();

        if (hasCameraPermission()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }
    }

    private void bindViews() {
        viewPager = findViewById(R.id.viewPagerStory);
        previewView = findViewById(R.id.previewView);
        handOverlay = findViewById(R.id.handOverlay);
        tvDetectedSign = findViewById(R.id.tvDetectedSign);
        progressConfidence = findViewById(R.id.progressConfidence);
        btnNextPanel = findViewById(R.id.btnNextPanel);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnNextPanel.setOnClickListener(v -> {
            if (currentPosition < storyPanels.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1);
            } else {
                finish(); // Story complete
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                resetDetectionState();
            }
        });
        viewPager.setUserInputEnabled(false); // Only swipe when unlocked
    }

    private void loadStory() {
        storyPanels = new ArrayList<>();
        storyPanels.add(new StoryPanel("A", "The [____] was jumping.", "The Apple was jumping.", "🍎"));
        storyPanels.add(new StoryPanel("B", "It found a friendly [____].", "It found a friendly Bear.", "🐻"));
        storyPanels.add(new StoryPanel("C", "They rode in a red [____].", "They rode in a red Car.", "🚗"));
        storyPanels.add(new StoryPanel("D", "And played with a silly [____].", "And played with a silly Dog.", "🐶"));

        adapter = new StoryAdapter(storyPanels);
        viewPager.setAdapter(adapter);
    }

    private void resetDetectionState() {
        correctStreakCount = 0;
        tvDetectedSign.setText("Awaiting sign: " + storyPanels.get(currentPosition).targetKey);
        progressConfidence.setProgress(0);

        if (storyPanels.get(currentPosition).isRevealed) {
            btnNextPanel.setVisibility(currentPosition < storyPanels.size() - 1 ? View.VISIBLE : View.GONE);
            if (currentPosition == storyPanels.size() - 1) {
                btnNextPanel.setText("Finish Story 🏁");
                btnNextPanel.setVisibility(View.VISIBLE);
            }
            viewPager.setUserInputEnabled(true);
        } else {
            btnNextPanel.setVisibility(View.GONE);
            viewPager.setUserInputEnabled(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Camera setup
    // ═══════════════════════════════════════════════════════════════════

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (handLandmarkerHelper != null) {
                        handLandmarkerHelper.detectLiveStream(
                                imageProxy,
                                true
                        );
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handLandmarkerHelper = new HandLandmarkerHelper(this, this);
        handLandmarkerHelper.setMaxNumHands(1);
        handLandmarkerHelper.setupHandLandmarker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handLandmarkerHelper != null) handLandmarkerHelper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (soundPlayer != null) soundPlayer.release();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HandLandmarkerListener
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void onResults(HandLandmarkerResult result, long inferenceTimeMs, int inputImageWidth, int inputImageHeight) {
        mainHandler.post(() -> {
            handOverlay.setResults(result, inputImageWidth, inputImageHeight);
            
            StoryPanel currentPanel = storyPanels.get(currentPosition);
            if (currentPanel.isRevealed) return;

            GestureClassifier.GestureResult gesture = gestureClassifier.classify(result, currentPanel.targetKey);

            if (gesture != null) {
                String detected = gesture.getSign();
                float confidence = gesture.getConfidence();
                int percent = Math.round(confidence * 100);

                tvDetectedSign.setText("Detected: " + detected + " (" + percent + "%)");
                progressConfidence.setProgress(percent);

                if (detected.equals(currentPanel.targetKey) && confidence >= 0.8f) {
                    correctStreakCount++;
                    if (correctStreakCount >= REQUIRED_STREAK) {
                        onWordRevealed(currentPanel);
                    }
                } else {
                    correctStreakCount = 0;
                }
            } else {
                tvDetectedSign.setText("Sign: " + currentPanel.targetKey);
                progressConfidence.setProgress(0);
                correctStreakCount = 0;
            }
        });
    }

    @Override
    public void onError(String error) {
        mainHandler.post(() -> tvDetectedSign.setText("Error: " + error));
    }

    private void onWordRevealed(StoryPanel panel) {
        panel.isRevealed = true;
        soundPlayer.playSuccess();
        adapter.notifyItemChanged(currentPosition);

        resetDetectionState();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Story Data & Adapter
    // ═══════════════════════════════════════════════════════════════════

    class StoryPanel {
        String targetKey;
        String blurredText;
        String unblurredText;
        String emoji;
        boolean isRevealed = false;

        StoryPanel(String targetKey, String blurredText, String unblurredText, String emoji) {
            this.targetKey = targetKey;
            this.blurredText = blurredText;
            this.unblurredText = unblurredText;
            this.emoji = emoji;
        }
    }

    class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

        private final List<StoryPanel> data;

        StoryAdapter(List<StoryPanel> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_panel, parent, false);
            return new StoryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
            StoryPanel panel = data.get(position);
            holder.tvSentence.setText(panel.isRevealed ? panel.unblurredText : panel.blurredText);
            holder.ivImage.setText(panel.emoji);
            holder.ivImage.setAlpha(panel.isRevealed ? 1.0f : 0.2f);
        }

        @Override
        public int getItemCount() { return data.size(); }

        class StoryViewHolder extends RecyclerView.ViewHolder {
            TextView ivImage;
            TextView tvSentence;
            StoryViewHolder(@NonNull View itemView) {
                super(itemView);
                // I reused an ImageView id but it's actually an Emoji placeholder so let's make it TextView
                ivImage = itemView.findViewById(R.id.ivStoryImage);
                tvSentence = itemView.findViewById(R.id.tvStorySentence);
            }
        }
    }
}
