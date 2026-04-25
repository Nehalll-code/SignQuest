package com.signquest.app.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

/**
 * HandLandmarkerHelper — Robust MediaPipe Hand Landmark detection helper.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  Responsibilities:                                          │
 * │  1. Initialize MediaPipe HandLandmarker from .task model    │
 * │  2. Accept CameraX ImageProxy frames                       │
 * │  3. Convert frames to MPImage and run detection             │
 * │  4. Return HandLandmarkerResult via callback (21 landmarks) │
 * │  5. Manage lifecycle (setup / teardown)                     │
 * └──────────────────────────────────────────────────────────────┘
 *
 * The ML layer is separated from the UI layer (Clean Architecture).
 * The Activity only interacts with this helper through the
 * {@link HandLandmarkerListener} interface.
 *
 * Usage:
 *   1. Create instance → new HandLandmarkerHelper(context, listener)
 *   2. Call setupHandLandmarker() in onResume
 *   3. Feed frames → detectLiveStream(imageProxy, isFrontCamera)
 *   4. Call close() in onPause
 */
public class HandLandmarkerHelper {

    private static final String TAG = "HandLandmarkerHelper";

    // ── Configuration Defaults ──

    /** Minimum confidence to consider a hand detected. */
    private static final float DEFAULT_MIN_DETECTION_CONFIDENCE = 0.5f;

    /** Minimum confidence for tracking a detected hand across frames. */
    private static final float DEFAULT_MIN_TRACKING_CONFIDENCE = 0.5f;

    /** Minimum confidence for hand presence (after detection). */
    private static final float DEFAULT_MIN_PRESENCE_CONFIDENCE = 0.5f;

    /** Maximum number of hands to detect simultaneously. */
    private static final int DEFAULT_MAX_HANDS = 2;

    /**
     * ┌─────────────────────────────────────────────────────────────────┐
     * │  PLACEHOLDER: Replace with the actual MediaPipe hand landmark  │
     * │  .task model file placed in app/src/main/assets/               │
     * │                                                                │
     * │  Download from:                                                │
     * │  https://developers.google.com/mediapipe/solutions/            │
     * │               vision/hand_landmarker#models                    │
     * │                                                                │
     * │  Recommended: hand_landmarker.task                             │
     * └─────────────────────────────────────────────────────────────────┘
     */
    private static final String MODEL_HAND_LANDMARKER = "hand_landmarker.task";

    // ── State ──

    private final Context context;
    private final HandLandmarkerListener listener;

    private HandLandmarker handLandmarker;
    private long lastTimestampMs = 0;

    private float minDetectionConfidence  = DEFAULT_MIN_DETECTION_CONFIDENCE;
    private float minTrackingConfidence   = DEFAULT_MIN_TRACKING_CONFIDENCE;
    private float minPresenceConfidence   = DEFAULT_MIN_PRESENCE_CONFIDENCE;
    private int   maxNumHands             = DEFAULT_MAX_HANDS;

    // ═══════════════════════════════════════════════════════════════════
    //  Listener Interface
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Callback interface for receiving hand landmark results.
     * Implemented by the hosting Activity or Fragment.
     */
    public interface HandLandmarkerListener {

        /** Called when landmarks are successfully detected. */
        void onResults(HandLandmarkerResult result, long inferenceTimeMs,
                       int inputImageWidth, int inputImageHeight);

        /** Called when an error occurs during detection. */
        void onError(String error);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════════════

    public HandLandmarkerHelper(Context context, HandLandmarkerListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Setup & Teardown
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Initialize the MediaPipe HandLandmarker.
     * Must be called before any frames are processed.
     *
     * Running mode is LIVE_STREAM for real-time CameraX integration.
     */
    public void setupHandLandmarker() {
        try {
            BaseOptions baseOptions = BaseOptions.builder()
                    .setModelAssetPath(MODEL_HAND_LANDMARKER)
                    .setDelegate(Delegate.CPU)       // Use CPU for broad device support
                    .build();

            HandLandmarker.HandLandmarkerOptions options =
                    HandLandmarker.HandLandmarkerOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setRunningMode(RunningMode.LIVE_STREAM)
                            .setNumHands(maxNumHands)
                            .setMinHandDetectionConfidence(minDetectionConfidence)
                            .setMinTrackingConfidence(minTrackingConfidence)
                            .setMinHandPresenceConfidence(minPresenceConfidence)
                            .setResultListener(this::onResultReceived)
                            .setErrorListener(this::onErrorReceived)
                            .build();

            handLandmarker = HandLandmarker.createFromOptions(context, options);

            Log.i(TAG, "HandLandmarker initialized successfully.");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize HandLandmarker: " + e.getMessage(), e);
            if (listener != null) {
                listener.onError("Failed to load hand detection model: " + e.getMessage());
            }
        }
    }

    /** Release resources. Call in Activity's onPause or onDestroy. */
    public void close() {
        if (handLandmarker != null) {
            handLandmarker.close();
            handLandmarker = null;
            Log.i(TAG, "HandLandmarker closed.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Live Stream Detection
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Process a CameraX frame for hand landmark detection.
     *
     * Steps:
     *   1. Convert ImageProxy → Bitmap
     *   2. Mirror if front camera
     *   3. Wrap in MPImage
     *   4. Send to HandLandmarker.detectAsync()
     *
     * @param imageProxy    The camera frame from CameraX analysis.
     * @param isFrontCamera Whether the front-facing camera is in use.
     */
    public void detectLiveStream(@NonNull ImageProxy imageProxy, boolean isFrontCamera) {
        if (handLandmarker == null) {
            imageProxy.close();
            return;
        }

        long timestamp = SystemClock.uptimeMillis();
        // MediaPipe requires strictly monotonically increasing timestamps
        if (timestamp <= lastTimestampMs) {
            timestamp = lastTimestampMs + 1;
        }
        lastTimestampMs = timestamp;

        // Step 1: Convert ImageProxy → Bitmap
        Bitmap bitmap = imageProxyToBitmap(imageProxy);
        if (bitmap == null) {
            imageProxy.close();
            return;
        }

        // Step 2 & 3: Rotate to upright, then mirror for front camera
        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        Matrix matrix = new Matrix();
        if (rotation != 0) {
            matrix.postRotate(rotation);
        }
        if (isFrontCamera) {
            matrix.postScale(-1.0f, 1.0f);
        }
        
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, 
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Step 4: Wrap bitmap as MPImage and detect asynchronously
        MPImage mpImage = new BitmapImageBuilder(bitmap).build();

        try {
            handLandmarker.detectAsync(mpImage, timestamp);
        } catch (Exception e) {
            Log.w(TAG, "detectAsync error: " + e.getMessage());
        }

        imageProxy.close();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Result Callbacks
    // ═══════════════════════════════════════════════════════════════════

    /** Called by MediaPipe when landmarks are detected. */
    private void onResultReceived(HandLandmarkerResult result, MPImage input) {
        long inferenceTime = SystemClock.uptimeMillis();
        if (listener != null) {
            listener.onResults(result, inferenceTime,
                    input.getWidth(), input.getHeight());
        }
    }

    /** Called by MediaPipe when an error occurs. */
    private void onErrorReceived(RuntimeException error) {
        Log.e(TAG, "MediaPipe error: " + error.getMessage());
        if (listener != null) {
            listener.onError(error.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Image Conversion Utilities
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Convert a CameraX {@link ImageProxy} to a standard Android {@link Bitmap}.
     *
     * NOTE: This uses ARGB_8888 format for compatibility with MPImage.
     * For production, consider using ImageProxy.toBitmap() (API 28+)
     * or YUV → RGB conversion for better performance.
     */
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        try {
            // Use the built-in toBitmap (available on recent CameraX versions)
            Bitmap bmp = imageProxy.toBitmap();
            return bmp.copy(Bitmap.Config.ARGB_8888, true);
        } catch (Exception e) {
            Log.w(TAG, "imageProxyToBitmap fallback: " + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Configuration Setters (optional tuning)
    // ═══════════════════════════════════════════════════════════════════

    public void setMinDetectionConfidence(float value) {
        this.minDetectionConfidence = value;
    }

    public void setMinTrackingConfidence(float value) {
        this.minTrackingConfidence = value;
    }

    public void setMinPresenceConfidence(float value) {
        this.minPresenceConfidence = value;
    }

    public void setMaxNumHands(int value) {
        this.maxNumHands = value;
    }

    /** Check whether the HandLandmarker is ready. */
    public boolean isReady() {
        return handLandmarker != null;
    }
}
