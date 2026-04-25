package com.signquest.app.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.signquest.app.R;

public class SoundPlayer {
    private static final String TAG = "SoundPlayer";
    private SoundPool soundPool;
    private int successSoundId;
    private int neutralSoundId;
    private boolean isMuted = false;
    private boolean successLoaded = false;
    private boolean neutralLoaded = false;

    public SoundPlayer(Context context) {
        com.signquest.app.data.ProfileManager pm = new com.signquest.app.data.ProfileManager(context);
        this.isMuted = pm.isSoundMuted();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                if (sampleId == successSoundId) successLoaded = true;
                if (sampleId == neutralSoundId) neutralLoaded = true;
            }
        });

        try {
            successSoundId = soundPool.load(context, R.raw.success_chime, 1);
            neutralSoundId = soundPool.load(context, R.raw.neutral_tone, 1);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load sounds: " + e.getMessage());
        }
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public void playSuccess() {
        if (!isMuted && soundPool != null && successLoaded) {
            try {
                soundPool.play(successSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Exception e) {
                Log.w(TAG, "playSuccess error: " + e.getMessage());
            }
        }
    }

    public void playNeutral() {
        if (!isMuted && soundPool != null && neutralLoaded) {
            try {
                soundPool.play(neutralSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Exception e) {
                Log.w(TAG, "playNeutral error: " + e.getMessage());
            }
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
