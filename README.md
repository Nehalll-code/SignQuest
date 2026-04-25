<div align="center">

<img src="screenshots/app_icon.png" alt="SignQuest Logo" width="120" height="120" />

# SignQuest

### *Learn. Sign. Quest.*

**An offline-first, AI-powered gamified mobile learning app that teaches children sign language — privately, interactively, and joyfully.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![AI](https://img.shields.io/badge/AI-MediaPipe%20On--Device-FF6F00?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/edge/mediapipe)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-green?style=for-the-badge)](https://developer.android.com)

---

</div>

## 📸 Screenshots

> **Note:** Place your screenshots in the `screenshots/` folder at the root of the repository.

<div align="center">

| Onboarding & Profile | World Map | Sign Detection |
|:---:|:---:|:---:|
| ![Onboarding](screenshots/onboarding.png) | ![World Map](screenshots/world_map.png) | ![Detection](screenshots/detection.png) |
| *Multi-profile selection and avatar setup* | *Gamified 2D progression hub* | *Real-time AI gesture recognition* |

| AR Map | Comprehension Quiz | Parent Dashboard |
|:---:|:---:|:---:|
| ![AR Map](screenshots/ar_map.png) | ![Quiz](screenshots/comprehension.png) | ![Dashboard](screenshots/parent_dashboard.png) |
| *Immersive 3D AR learning environment* | *Post-detection retention quiz* | *Progress analytics for guardians* |

</div>

> **Tip:** For best presentation, use screenshots with device frames at `1080 × 2340px`. Tools like [Previewed](https://previewed.app) or Figma can help generate clean mockups.

---

## 🌟 What is SignQuest?

SignQuest is an **offline-first, gamified mobile learning (M-Learning) application** built for Android that teaches children sign language — supporting **ASL, ISL, and BSL** — through an interactive, adventure-based progression system.

What makes it different: all AI inference runs **entirely on-device** using Google MediaPipe, meaning zero cloud dependency, zero latency, and zero compromise on child privacy. Whether in a classroom, at home, or in an area with no internet, SignQuest works.

---

## ✨ Features

- 🤖 **Real-Time On-Device Gesture Recognition** — MediaPipe Hand Landmark detection with custom vector-based gesture classification, fully offline
- 🗺️ **Gamified World Map** — 10 progressively unlocked sign language categories: Alphabets, Numbers, Greetings, Family, Groceries, Nature, Pets, Breakfast, Weather, and Salutations
- 🥽 **Augmented Reality Mode** — ARCore-powered 3D navigation map for immersive spatial learning on supported devices
- 👥 **Multi-Profile Support** — Isolated user profiles for shared devices, ideal for classrooms and low-income households
- 🧠 **Comprehension Testing** — Post-learning retention quizzes automatically tracked via SQLite
- 📊 **Parent Dashboard** — Detailed learning analytics with one-tap progress sharing
- 🎨 **Avatar Customisation** — Personalised buddy characters to drive engagement
- 🔒 **Privacy-First Architecture** — No network calls, no accounts, no data leaves the device

---

## 🏗️ Architecture

SignQuest follows the **MVC (Model-View-Controller)** pattern with a clean separation of concerns across three layers:

```
┌──────────────────────────────────────────────────────────┐
│                        VIEW LAYER                        │
│  XML Layouts (Material Design 3) · HandOverlayView       │
│  Hardware-accelerated Canvas for ML skeleton rendering   │
└──────────────────────────────┬───────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────┐
│                    CONTROLLER LAYER                      │
│  DetectionActivity · WorldMapActivity · ARMapActivity    │
│  Lifecycle management · Camera/AR session orchestration  │
└──────────────────────────────┬───────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────┐
│                      MODEL LAYER                         │
│  SignDataProvider (Static Dictionary)                    │
│  HandLandmarkerHelper + GestureClassifier (Async ML)     │
│  ProfileManager (SharedPreferences)                      │
│  DatabaseHelper (SQLite Analytics)                       │
└──────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Java 17 |
| **Platform** | Android SDK · minSdk 24 · compileSdk 35 |
| **Computer Vision / AI** | Google MediaPipe Tasks Vision v0.10.21 |
| **Camera** | CameraX v1.4.1 |
| **Augmented Reality** | Sceneview (arsceneview) v1.2.3 · ARCore |
| **Local Storage** | SharedPreferences · SQLite |
| **Version Control** | Git · GitHub |

---

## 🗂️ Project Structure

```
SignQuest/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/signquest/
│   │   │   ├── activities/
│   │   │   │   ├── UserSelectActivity.java
│   │   │   │   ├── ProfileActivity.java
│   │   │   │   ├── WorldMapActivity.java
│   │   │   │   ├── ARMapActivity.java
│   │   │   │   ├── DetectionActivity.java
│   │   │   │   ├── ComprehensionActivity.java
│   │   │   │   └── ParentDashboardActivity.java
│   │   │   ├── ml/
│   │   │   │   ├── HandLandmarkerHelper.java
│   │   │   │   └── GestureClassifier.java
│   │   │   ├── data/
│   │   │   │   ├── SignDataProvider.java
│   │   │   │   ├── ProfileManager.java
│   │   │   │   └── DatabaseHelper.java
│   │   │   └── views/
│   │   │       └── HandOverlayView.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
│
├── screenshots/           ← Place your screenshots here
├── README.md
└── build.gradle
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio **Hedgehog** (2023.1.1) or newer
- Android SDK **API 24+** installed
- A physical Android device is **strongly recommended** for camera and AR features

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/your-username/SignQuest.git
cd SignQuest
```

**2. Open in Android Studio**

`File → Open → Select the SignQuest folder`

**3. Sync Gradle**

Android Studio will prompt you to sync Gradle dependencies automatically. Let it complete.

**4. Run the app**

Connect your Android device with USB debugging enabled and press **Run ▶** or use:
```bash
./gradlew installDebug
```

> ⚠️ **AR Mode** requires a device with [ARCore support](https://developers.google.com/ar/devices). The app gracefully falls back to the 2D World Map on unsupported devices.

---

## 📱 Application Flow

```
Launch
  └── UserSelectActivity        (Select or create a profile)
        └── ProfileActivity     (Name · Avatar · Sign Language Track)
              └── WorldMapActivity / ARMapActivity
                    └── DetectionActivity     (Live AI gesture recognition)
                          └── ComprehensionActivity  (Retention quiz)
                                └── WorldMapActivity  (Progress updated)

(Guardian Access)
  └── ParentDashboardActivity   (Analytics · Share progress)
```

---

## 🧠 How the AI Works

SignQuest uses **Google MediaPipe Hand Landmark** detection to track 21 key points on the user's hand in real-time. These landmarks are streamed from **CameraX** on a background thread to avoid blocking the UI.

The `GestureClassifier` then applies **vector-based geometric analysis** — computing angles, distances, and directional relationships between landmarks — against a library of generic gesture primitives:

```
checkOpenHand()  ·  checkClaw()  ·  checkPinch()  · ...
```

This modular primitive system means new signs can be added to the curriculum with **minimal code changes** — the vocabulary is practically infinite.

A custom `HandOverlayView` renders the skeleton in real-time over the camera feed using a hardware-accelerated **Canvas**, giving users immediate visual feedback on their hand position.

---

## 🔒 Privacy & Security

SignQuest was built with child safety as a non-negotiable design constraint:

- **Zero network requests** — no data is ever transmitted off the device
- **No authentication required** — no accounts, no emails, no passwords
- **No cloud AI** — all ML inference runs locally via MediaPipe edge models
- **Isolated profiles** — each user's data is stored independently via SharedPreferences + SQLite

---

## 🌍 Novelty & Innovation

| Innovation | Details |
|---|---|
| **Edge AI for Children's Privacy** | Full ML inference on-device — no cloud, no latency, no data risk |
| **Shared Device Architecture** | Multi-profile system designed for classrooms and shared family devices without requiring cloud accounts |
| **Gamified Progression** | Sequential level unlocking, avatar customisation, and dynamic progress bars transform rote learning into a quest |
| **Spatial AR Learning** | ARCore integration brings vocabulary nodes into the user's physical environment |
| **Infinitely Expandable Curriculum** | Primitive-based gesture system scales to thousands of words with minimal engineering effort |

---

## 📊 Supported Sign Languages

| Language | Status |
|---|---|
| ASL — American Sign Language | ✅ Supported |
| ISL — Indian Sign Language | ✅ Supported |
| BSL — British Sign Language | ✅ Supported |

---

## 📦 Key Dependencies

```gradle
// AI & Computer Vision
implementation 'com.google.mediapipe:tasks-vision:0.10.21'

// Camera
implementation 'androidx.camera:camera-camera2:1.4.1'
implementation 'androidx.camera:camera-lifecycle:1.4.1'
implementation 'androidx.camera:camera-view:1.4.1'

// Augmented Reality
implementation 'io.github.sceneview:arsceneview:1.2.3'

// UI
implementation 'com.google.android.material:material:1.x.x'
```

---


---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

Made with ❤️ for children who deserve to learn without barriers.

**[⬆ Back to top](#signquest)**

</div>
