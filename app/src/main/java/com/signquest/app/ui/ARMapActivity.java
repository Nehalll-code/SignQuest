package com.signquest.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.signquest.app.R;

// Implementation notes: The full mapping of io.github.sceneview.ar.ArSceneView
// will throw InflateException if the libraries failed to download dynamically
// or dependencies weren't satisfied.
public class ARMapActivity extends AppCompatActivity {

    private TextView tvARInstructions;
    private MaterialButton btnSkipAR;
    private ImageView btnClose;

    private boolean planeDetected = false;
    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_ar_map);
            bindViews();
            startARTimeout();

            // Setup ARSceneView Logic here if the device successfully instantiates it
            // sceneView = findViewById(R.id.sceneView);
            // sceneView.setOnSessionConfiguration((session, config) -> ...);

        } catch (Exception e) {
            // Fallback gracefully in case device AR is unavailable or build lacks binaries
            e.printStackTrace();
            Toast.makeText(this, "AR Core is unavailable on this device. Opening 2D Map.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void bindViews() {
        tvARInstructions = findViewById(R.id.tvARInstructions);
        btnSkipAR = findViewById(R.id.btnSkipAR);
        btnClose = findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> finish());
        btnSkipAR.setOnClickListener(v -> finish());
    }

    private void startARTimeout() {
        timeoutRunnable = () -> {
            if (!planeDetected) {
                tvARInstructions.setText("Having trouble? Feel free to use the regular map.");
                btnSkipAR.setVisibility(View.VISIBLE);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 8000); // 8 seconds
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }
}
