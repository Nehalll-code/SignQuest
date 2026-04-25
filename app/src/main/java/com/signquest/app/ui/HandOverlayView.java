package com.signquest.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.List;

/**
 * HandOverlayView — Draws MediaPipe hand landmarks and connections
 * on top of the camera preview.
 *
 * Usage:
 *   handOverlay.setResults(result, imageWidth, imageHeight);
 *   // The view will invalidate and redraw automatically.
 */
public class HandOverlayView extends View {

    // Landmark connections (pairs of indices to draw lines between)
    private static final int[][] HAND_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4},       // Thumb
            {0, 5}, {5, 6}, {6, 7}, {7, 8},       // Index
            {0, 9}, {9, 10}, {10, 11}, {11, 12},   // Middle
            {0, 13}, {13, 14}, {14, 15}, {15, 16}, // Ring
            {0, 17}, {17, 18}, {18, 19}, {19, 20}, // Pinky
            {5, 9}, {9, 13}, {13, 17}              // Palm
    };

    private final Paint dotPaint;
    private final Paint linePaint;
    private final Paint tipPaint;

    private HandLandmarkerResult results;
    private int imageWidth = 1;
    private int imageHeight = 1;

    public HandOverlayView(Context context) {
        this(context, null);
    }

    public HandOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Dot paint — small circles at each landmark
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.FILL);

        // Line paint — connections between landmarks
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xCC6C63FF); // Purple with alpha
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // Fingertip dots — larger and different color
        tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setColor(0xFF00B894); // Teal green
        tipPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Update the overlay with new hand landmark results.
     * Call this from the UI thread.
     */
    public void setResults(HandLandmarkerResult results, int imageWidth, int imageHeight) {
        this.results = results;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

    /** Clear the overlay. */
    public void clear() {
        this.results = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (results == null || results.landmarks().isEmpty()) {
            return;
        }

        float viewW = getWidth();
        float viewH = getHeight();

        // PreviewView uses FILL_CENTER by default:
        // The image is scaled to completely fill the view, then centered (cropping excess).
        // We replicate this to map normalized landmarks → screen coordinates.
        float scaleFactor = Math.max(viewW / (float) imageWidth, viewH / (float) imageHeight);
        float scaledImageW = imageWidth * scaleFactor;
        float scaledImageH = imageHeight * scaleFactor;

        // Offset = how much of the scaled image is cropped on each side
        float offsetX = (scaledImageW - viewW) / 2f;
        float offsetY = (scaledImageH - viewH) / 2f;

        // Fingertip indices for larger dots
        int[] tipIndices = {4, 8, 12, 16, 20};

        for (List<NormalizedLandmark> hand : results.landmarks()) {
            // Draw connections first (lines behind dots)
            for (int[] conn : HAND_CONNECTIONS) {
                NormalizedLandmark a = hand.get(conn[0]);
                NormalizedLandmark b = hand.get(conn[1]);

                // Landmarks are normalized (0–1) in the analyzed image space.
                // The analyzed image was already mirrored by HandLandmarkerHelper,
                // matching PreviewView's mirrored display. NO extra mirroring needed.
                float ax = a.x() * scaledImageW - offsetX;
                float ay = a.y() * scaledImageH - offsetY;
                float bx = b.x() * scaledImageW - offsetX;
                float by = b.y() * scaledImageH - offsetY;

                canvas.drawLine(ax, ay, bx, by, linePaint);
            }

            // Draw landmark dots
            for (int i = 0; i < hand.size(); i++) {
                NormalizedLandmark lm = hand.get(i);
                float cx = lm.x() * scaledImageW - offsetX;
                float cy = lm.y() * scaledImageH - offsetY;

                // Fingertips get a larger, green dot
                boolean isTip = false;
                for (int tip : tipIndices) {
                    if (i == tip) {
                        isTip = true;
                        break;
                    }
                }

                if (isTip) {
                    canvas.drawCircle(cx, cy, 12f, tipPaint);
                } else {
                    canvas.drawCircle(cx, cy, 7f, dotPaint);
                }
            }
        }
    }
}
