import wave
import struct
import math
import os

def generate_tone(filename, freqs, duration_ms, volume=0.5):
    sample_rate = 44100
    num_samples = int(sample_rate * (duration_ms / 1000.0))
    wave_file = wave.open(filename, 'w')
    wave_file.setnchannels(1)
    wave_file.setsampwidth(2)
    wave_file.setframerate(sample_rate)

    for i in range(num_samples):
        # Envelope to avoid clicks (fade in/out)
        t = i / sample_rate
        envelope = 1.0
        if i < 441: envelope = i / 441
        elif i > num_samples - 441: envelope = (num_samples - i) / 441

        # Synthesize sine waves
        value = 0
        for f in freqs:
            value += math.sin(2 * math.pi * f * t)
        value /= len(freqs)

        # Scale to 16-bit bounds (-32768 to 32767)
        # Apply volume and envelope
        sample = int(value * envelope * volume * 32767)
        if sample > 32767: sample = 32767
        if sample < -32768: sample = -32768
        wave_file.writeframesraw(struct.pack('<h', sample))

    wave_file.close()

# Ensure res/raw directory exists
res_raw_dir = r"c:\Users\Nehal\Desktop\ResumeProjects\Streaming Analytics\SignQuest\app\src\main\res\raw"
if not os.path.exists(res_raw_dir):
    os.makedirs(res_raw_dir)

# C-Major Pentatonic chord (C5, E5, G5)
generate_tone(os.path.join(res_raw_dir, 'success_chime.wav'), [523.25, 659.25, 783.99], 300, volume=0.2)

# Neutral tone (low A)
generate_tone(os.path.join(res_raw_dir, 'neutral_tone.wav'), [220.00], 400, volume=0.1)

print("Generated WAV files successfully in res/raw/")
