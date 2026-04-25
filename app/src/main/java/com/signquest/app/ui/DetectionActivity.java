package com.signquest.app.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.content.Intent;
import android.content.Context;
import com.signquest.app.utils.SoundPlayer;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
import com.signquest.app.R;
import com.signquest.app.data.ProfileManager;
import com.signquest.app.data.SignDataProvider;
import com.signquest.app.ml.GestureClassifier;
import com.signquest.app.ml.HandLandmarkerHelper;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DetectionActivity — Real-time sign detection with CameraX + MediaPipe.
 *
 * Receives a specific sign to practice via intent extras.
 * Shows reference instructions and detects if the user signs it correctly.
 */
public class DetectionActivity extends AppCompatActivity
        implements HandLandmarkerHelper.HandLandmarkerListener {

    private static final String TAG = "DetectionActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;

    // Views
    private PreviewView previewView;
    private HandOverlayView handOverlay;
    private TextView tvTargetLetter, tvDetectedSign, tvInstructions, tvTargetReference;
    private ProgressBar progressConfidence;
    private MaterialButton btnDone;
    private ImageView btnBack;
    private MaterialCardView cardInstructions;
    // Sound & Haptics
    private SoundPlayer soundPlayer;
    private android.os.Vibrator vibrator;
    private long incorrectHoldStartTimeMs = 0;
    private boolean neutralTonePlayed = false;
    private long sessionStartTimeMs = 0;

    // ML
    private HandLandmarkerHelper handLandmarkerHelper;
    private GestureClassifier gestureClassifier;
    private ExecutorService cameraExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Sign data from extras
    private String language;
    private int levelId;
    private String signKey;
    private String signLabel;
    private String signInstructions;

    // Game state
    private boolean signCompleted = false;
    private int correctStreakCount = 0;
    private static final int REQUIRED_STREAK = 3;

    private ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        profileManager = new ProfileManager(this);
        soundPlayer = new SoundPlayer(this);
        vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Get sign data from extras
        language = getIntent().getStringExtra("language");
        levelId = getIntent().getIntExtra("level_id", SignDataProvider.LEVEL_ALPHABETS);
        signKey = getIntent().getStringExtra("sign_key");
        signLabel = getIntent().getStringExtra("sign_label");
        signInstructions = getIntent().getStringExtra("sign_instructions");

        if (language == null) language = profileManager.getSignLanguage();
        if (signKey == null) signKey = "A";
        if (signLabel == null) signLabel = signKey;
        if (signInstructions == null) signInstructions = "";

        bindViews();
        setupUI();

        gestureClassifier = new GestureClassifier();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void bindViews() {
        previewView = findViewById(R.id.previewView);
        handOverlay = findViewById(R.id.handOverlay);
        tvTargetLetter = findViewById(R.id.tvTargetLetter);
        tvDetectedSign = findViewById(R.id.tvDetectedSign);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvTargetReference = findViewById(R.id.tvTargetReference);
        progressConfidence = findViewById(R.id.progressConfidence);
        btnDone = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        cardInstructions = findViewById(R.id.cardInstructions);
    }

    private void setupUI() {
        tvTargetLetter.setText(signLabel);
        tvDetectedSign.setText(getString(R.string.detection_hint));

        // Show reference instructions
        if (signInstructions != null && !signInstructions.isEmpty()) {
            tvTargetReference.setText(signLabel);
            tvInstructions.setText("📖 " + signInstructions);
            cardInstructions.setVisibility(View.VISIBLE);
        } else {
            cardInstructions.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComprehensionActivity.class);
            intent.putExtra(ComprehensionActivity.EXTRA_TARGET_KEY, signKey);
            intent.putExtra(ComprehensionActivity.EXTRA_LEVEL_ID, levelId);
            intent.putExtra(ComprehensionActivity.EXTRA_LANGUAGE, language);
            startActivity(intent);
            finish();
        });

        // Bounce animation on target letter
        tvTargetLetter.setScaleX(0.5f);
        tvTargetLetter.setScaleY(0.5f);
        tvTargetLetter.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Camera Setup
    // ═══════════════════════════════════════════════════════════════════

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (handLandmarkerHelper != null && handLandmarkerHelper.isReady()) {
                        handLandmarkerHelper.detectLiveStream(imageProxy, true);
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e(TAG, "Camera startup failed: " + e.getMessage(), e);
                Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void onResume() {
        super.onResume();
        sessionStartTimeMs = System.currentTimeMillis();
        try {
            handLandmarkerHelper = new HandLandmarkerHelper(this, this);
            handLandmarkerHelper.setMaxNumHands(1);
            handLandmarkerHelper.setupHandLandmarker();
        } catch (Exception e) {
            Log.e(TAG, "HandLandmarker setup failed: " + e.getMessage(), e);
            Toast.makeText(this, "Hand detection error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sessionStartTimeMs > 0) {
            long elapsed = System.currentTimeMillis() - sessionStartTimeMs;
            int minutes = Math.round(elapsed / 60000.0f);
            if (minutes > 0) profileManager.addPracticeTime(minutes);
        }
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
    public void onResults(HandLandmarkerResult result, long inferenceTimeMs,
                          int inputImageWidth, int inputImageHeight) {
        mainHandler.post(() -> {
            handOverlay.setResults(result, inputImageWidth, inputImageHeight);

            if (signCompleted) return;

            GestureClassifier.GestureResult gesture = gestureClassifier.classify(result, signKey);

            if (gesture != null) {
                String detected = gesture.getSign();
                float confidence = gesture.getConfidence();
                int percent = Math.round(confidence * 100);

                tvDetectedSign.setText("Detected: " + detected + " (" + percent + "%)");
                progressConfidence.setProgress(percent);

                if (detected.equals(signKey) && confidence >= 0.8f) {
                    correctStreakCount++;
                    incorrectHoldStartTimeMs = 0;
                    neutralTonePlayed = false;
                    if (correctStreakCount >= REQUIRED_STREAK) {
                        onSignCorrect();
                    }
                } else {
                    correctStreakCount = 0;
                    if (incorrectHoldStartTimeMs == 0) {
                        incorrectHoldStartTimeMs = System.currentTimeMillis();
                        neutralTonePlayed = false;
                    } else if (!neutralTonePlayed && (System.currentTimeMillis() - incorrectHoldStartTimeMs >= 3000)) {
                        soundPlayer.playNeutral();
                        neutralTonePlayed = true;
                    }
                }
            } else {
                tvDetectedSign.setText(getString(R.string.no_hand_detected));
                progressConfidence.setProgress(0);
                correctStreakCount = 0;
                incorrectHoldStartTimeMs = 0;
                neutralTonePlayed = false;
            }
        });
    }

    @Override
    public void onError(String error) {
        mainHandler.post(() -> tvDetectedSign.setText("Error: " + error));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Completion
    // ═══════════════════════════════════════════════════════════════════

    private void onSignCorrect() {
        signCompleted = true;

        try {
            // Mark completed in ProfileManager
            profileManager.markSignCompleted(language, levelId, signKey);
            profileManager.awardBadge();
        } catch (Exception e) {
            Log.e(TAG, "Error saving progress: " + e.getMessage());
        }

        try {
            soundPlayer.playSuccess();
        } catch (Exception e) {
            Log.w(TAG, "Sound playback error: " + e.getMessage());
        }

        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    vibrator.vibrate(
                        android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_TICK)
                    );
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                    );
                } else {
                    vibrator.vibrate(50);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Vibration error: " + e.getMessage());
        }

        // Celebrate in UI
        tvDetectedSign.setText("✅ Perfect! You signed \"" + signLabel + "\"! 🌟");
        progressConfidence.setProgress(100);

        try {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvTargetLetter, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvTargetLetter, "scaleY", 1f, 1.3f, 1f);
            scaleX.setDuration(500);
            scaleY.setDuration(500);
            scaleX.start();
            scaleY.start();
        } catch (Exception e) {
            Log.w(TAG, "Animation error: " + e.getMessage());
        }

        // Show Congratulations Popup
        if (!isFinishing() && !isDestroyed()) {
            try {
                new AlertDialog.Builder(DetectionActivity.this)
                    .setTitle("🎉 Congratulations! 🎉")
                    .setMessage("Amazing job! You correctly signed the letter \"" + signLabel + "\"!\n\n⭐ Keep going, you're a signing superstar! ⭐")
                    .setPositiveButton("Next →", (dialog, which) -> {
                        dialog.dismiss();
                        navigateToComprehension();
                    })
                    .setCancelable(false)
                    .show();
            } catch (Exception e) {
                Log.e(TAG, "Dialog error: " + e.getMessage());
                // Fallback: navigate directly
                navigateToComprehension();
            }
        }
    }

    private void navigateToComprehension() {
        try {
            Intent intent = new Intent(DetectionActivity.this, ComprehensionActivity.class);
            intent.putExtra(ComprehensionActivity.EXTRA_TARGET_KEY, signKey);
            intent.putExtra(ComprehensionActivity.EXTRA_LEVEL_ID, levelId);
            intent.putExtra(ComprehensionActivity.EXTRA_LANGUAGE, language);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Navigation error: " + e.getMessage());
            finish();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Permissions
    // ═══════════════════════════════════════════════════════════════════

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is needed!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
