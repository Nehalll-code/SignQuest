package com.signquest.app.ml;

import android.util.Log;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.List;

/**
 * GestureClassifier — Translates MediaPipe hand landmarks into ASL sign meanings.
 *
 * Uses geometric analysis of the 21 hand landmarks to identify specific
 * finger positions, angles, and spatial relationships that define each
 * ASL letter. Each letter has its own dedicated check method.
 */
public class GestureClassifier {

    private static final String TAG = "GestureClassifier";

    // MediaPipe Hand Landmark Indices
    private static final int WRIST         = 0;
    private static final int THUMB_CMC     = 1;
    private static final int THUMB_MCP     = 2;
    private static final int THUMB_IP      = 3;
    private static final int THUMB_TIP     = 4;
    private static final int INDEX_MCP     = 5;
    private static final int INDEX_PIP     = 6;
    private static final int INDEX_DIP     = 7;
    private static final int INDEX_TIP     = 8;
    private static final int MIDDLE_MCP    = 9;
    private static final int MIDDLE_PIP    = 10;
    private static final int MIDDLE_DIP    = 11;
    private static final int MIDDLE_TIP    = 12;
    private static final int RING_MCP      = 13;
    private static final int RING_PIP      = 14;
    private static final int RING_DIP      = 15;
    private static final int RING_TIP      = 16;
    private static final int PINKY_MCP     = 17;
    private static final int PINKY_PIP     = 18;
    private static final int PINKY_DIP     = 19;
    private static final int PINKY_TIP     = 20;

    // ═══════════════════════════════════════════════════════════════════
    //  Result Data Class
    // ═══════════════════════════════════════════════════════════════════

    public static class GestureResult {
        private final String sign;
        private final float confidence;

        public GestureResult(String sign, float confidence) {
            this.sign = sign;
            this.confidence = confidence;
        }

        public String getSign()       { return sign; }
        public float  getConfidence() { return confidence; }

        @Override
        public String toString() {
            return sign + " (" + String.format("%.0f%%", confidence * 100) + ")";
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Main Classification Method
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Classify the hand landmarks to determine if they match the target sign.
     * Only returns a positive result if the hand actually matches the sign geometry.
     */
    public GestureResult classify(HandLandmarkerResult result, String targetSignKey) {
        if (result == null || result.landmarks().isEmpty()) {
            return null;
        }

        List<NormalizedLandmark> landmarks = result.landmarks().get(0);
        if (landmarks.size() < 21) {
            return null;
        }

        float[][] pts = extractLandmarkArray(landmarks);
        boolean[] fingers = measureFingerStates(pts);

        // Check if the hand matches the specific target sign
        float confidence = checkSpecificSign(targetSignKey, fingers, pts);

        if (confidence >= 0.80f) {
            return new GestureResult(targetSignKey, confidence);
        }

        // Also try to identify what sign IS being shown (for feedback)
        String detected = identifyCurrentSign(fingers, pts);
        float detectedConf = 0.0f;
        if (detected != null) {
            detectedConf = checkSpecificSign(detected, fingers, pts);
        }

        if (detected != null && detectedConf >= 0.75f) {
            return new GestureResult(detected, detectedConf);
        }

        // Hand detected but no recognizable sign
        return new GestureResult("?", 0.3f);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Per-Sign Geometric Checks
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if the current hand pose matches a specific sign.
     * Returns confidence 0.0 to 1.0.
     */
    private float checkSpecificSign(String sign, boolean[] fingers, float[][] pts) {
        if (sign == null) return 0f;

        switch (sign.toUpperCase()) {
            case "A": return checkA(fingers, pts);
            case "B": return checkB(fingers, pts);
            case "C": return checkC(fingers, pts);
            case "D": return checkD(fingers, pts);
            case "E": return checkE(fingers, pts);
            case "F": return checkF(fingers, pts);
            case "G": return checkG(fingers, pts);
            case "H": return checkH(fingers, pts);
            case "I": return checkI(fingers, pts);
            case "J": return checkJ(fingers, pts);
            case "K": return checkK(fingers, pts);
            case "L": return checkL(fingers, pts);
            case "M": return checkM(fingers, pts);
            case "N": return checkN(fingers, pts);
            case "O": return checkO(fingers, pts);
            case "P": return checkP(fingers, pts);
            case "Q": return checkQ(fingers, pts);
            case "R": return checkR(fingers, pts);
            case "S": return checkS(fingers, pts);
            case "T": return checkT(fingers, pts);
            case "U": return checkU(fingers, pts);
            case "V": return checkV(fingers, pts);
            case "W": return checkW(fingers, pts);
            case "X": return checkX(fingers, pts);
            case "Y": return checkY(fingers, pts);
            case "Z": return checkZ(fingers, pts);
            // Numbers
            case "0": return check0(fingers, pts);
            case "1": return check1(fingers, pts);
            case "2": return check2(fingers, pts);
            case "3": return check3(fingers, pts);
            case "4": return check4(fingers, pts);
            case "5": return check5(fingers, pts);
            case "6": return check6(fingers, pts);
            case "7": return check7(fingers, pts);
            case "8": return check8(fingers, pts);
            case "9": return check9(fingers, pts);

            // ── Greetings ──
            case "HELLO":       return checkOpenHand(fingers, pts);   // open palm wave
            case "HI":          return checkOpenHand(fingers, pts);   // open palm wave
            case "GOOD_MORNING":return checkB(fingers, pts) > 0 ? 0.85f : 0f; // flat hand at chin
            case "GOOD_NIGHT":  return checkB(fingers, pts) > 0 ? 0.85f : 0f; // flat hand curves down
            case "HOW_ARE_YOU": return checkThumbsUp(fingers, pts);  // both thumbs up
            case "WELCOME":     return checkOpenHand(fingers, pts);  // open hand sweeps in

            // ── Family ──
            case "MOTHER":      return checkOpenHand(fingers, pts);  // open 5-hand at chin
            case "FATHER":      return checkOpenHand(fingers, pts);  // open 5-hand at forehead
            case "SISTER":      return checkL(fingers, pts) > 0 ? 0.85f : 0f;  // thumb traces jaw
            case "BROTHER":     return checkL(fingers, pts) > 0 ? 0.85f : 0f;  // thumb on forehead
            case "BABY":        return checkCradleArms(fingers, pts); // cradled arms
            case "FRIEND":      return checkX(fingers, pts) > 0 ? 0.85f : 0f;  // hooked index fingers

            // ── Groceries ──
            case "APPLE":       return checkA(fingers, pts) > 0 ? 0.85f : 0f;  // knuckle twist on cheek
            case "MILK":        return checkMilk(fingers, pts);      // squeeze fist open/close
            case "BREAD":       return checkB(fingers, pts) > 0 ? 0.85f : 0f;  // slice motion
            case "WATER":
            case "WATER_N":     return checkW(fingers, pts) > 0 ? 0.85f : 0f;  // W-hand taps chin
            case "EGG":         return checkH(fingers, pts) > 0 ? 0.85f : 0f;  // H-hands break apart
            case "RICE":        return checkC(fingers, pts) > 0 ? 0.85f : 0f;  // cupped hands scoop

            // ── Nature ──
            case "TREE":        return checkOpenHand(fingers, pts);  // spread fingers like branches
            case "FLOWER":      return checkFlower(fingers, pts);    // pinched fingers at nose
            case "SUN":         return checkOpenHand(fingers, pts);  // open hand radiating
            case "MOUNTAIN":    return checkS(fingers, pts) > 0 ? 0.85f : 0f;  // fists tap
            case "RIVER":       return checkW(fingers, pts) > 0 ? 0.85f : 0f;  // W-hand wiggles

            // ── Pets ──
            case "DOG":         return checkB(fingers, pts) > 0 ? 0.85f : 0f;  // pat thigh (flat hand)
            case "CAT":         return checkF(fingers, pts) > 0 ? 0.85f : 0f;  // pinch at cheek
            case "BIRD":        return checkBird(fingers, pts);      // pinch at mouth = beak
            case "FISH":        return checkB(fingers, pts) > 0 ? 0.85f : 0f;  // flat hand wiggles
            case "RABBIT":      return checkU(fingers, pts) > 0 ? 0.85f : 0f;  // two fingers = ears
            case "TURTLE":      return checkA(fingers, pts) > 0 ? 0.85f : 0f;  // fist with thumb out

            // ── Breakfast ──
            case "EAT":         return checkFlower(fingers, pts);    // bunched fingertips to mouth
            case "DRINK":       return checkC(fingers, pts) > 0 ? 0.85f : 0f;  // C-hand tilts
            case "CEREAL":      return checkC(fingers, pts) > 0 ? 0.85f : 0f;  // curved finger scoops
            case "JUICE":       return checkC(fingers, pts) > 0 ? 0.85f : 0f;  // C-hand to mouth
            case "TOAST":       return checkV(fingers, pts) > 0 ? 0.85f : 0f;  // V-hand stabs
            case "COFFEE":      return checkS(fingers, pts) > 0 ? 0.85f : 0f;  // grinding S-fists

            // ── Weather ──
            case "RAIN":        return checkOpenHand(fingers, pts);  // 5-hands move down
            case "SNOW":        return checkOpenHand(fingers, pts);  // 5-hands wiggle down
            case "WIND":        return checkOpenHand(fingers, pts);  // open hands sweep side
            case "HOT":         return checkClaw(fingers, pts);      // claw hand at mouth
            case "COLD":        return checkS(fingers, pts) > 0 ? 0.85f : 0f;  // fists tremble
            case "CLOUD":       return checkClaw(fingers, pts);      // claw hands roll

            // ── Salutations ──
            case "THANK_YOU":   return checkB(fingers, pts) > 0 ? 0.85f : 0f;  // flat hand from chin
            case "PLEASE":      return checkB(fingers, pts) > 0 ? 0.85f : 0f;  // flat hand on chest
            case "SORRY":       return checkA(fingers, pts) > 0 ? 0.85f : 0f;  // A-fist circles chest
            case "GOODBYE":     return checkOpenHand(fingers, pts);  // open palm fold
            case "YES":         return checkS(fingers, pts) > 0 ? 0.85f : 0f;  // fist nods
            case "NO":          return checkNo(fingers, pts);

            default: return 0f;
        }
    }

    // -- A: Fist with thumb alongside (thumb tip near index MCP side) --
    private float checkA(boolean[] f, float[][] pts) {
        // All fingers curled, thumb tip beside index finger (not tucked under)
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        // Thumb should be alongside, not tucked under fingers
        float thumbToIndex = distance2D(pts[THUMB_TIP], pts[INDEX_MCP]);
        boolean thumbBeside = thumbToIndex < 0.08f;
        // Thumb tip should be above (lower y) the index PIP
        boolean thumbUp = pts[THUMB_TIP][1] < pts[INDEX_PIP][1];
        if (thumbBeside && thumbUp) return 0.90f;
        if (!f[0] && !f[1] && !f[2] && !f[3] && !f[4]) return 0.82f;
        return 0f;
    }

    // -- B: Four fingers extended straight up, thumb curled across palm --
    private float checkB(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2] || !f[3] || !f[4]) return 0f;
        // Thumb should be curled (not extended out)
        if (f[0]) return 0f;
        // Fingers should be close together (not spread)
        float indexMiddle = Math.abs(pts[INDEX_TIP][0] - pts[MIDDLE_TIP][0]);
        float middleRing = Math.abs(pts[MIDDLE_TIP][0] - pts[RING_TIP][0]);
        boolean together = indexMiddle < 0.06f && middleRing < 0.06f;
        return together ? 0.92f : 0.84f;
    }

    // -- C: Curved hand, all fingers together forming a C shape --
    private float checkC(boolean[] f, float[][] pts) {
        // Fingers partially curled in a curve
        float thumbToIndex = distance2D(pts[THUMB_TIP], pts[INDEX_TIP]);
        float thumbToPinky = distance2D(pts[THUMB_TIP], pts[PINKY_TIP]);
        // Thumb and index should have a gap (the C opening)
        boolean cGap = thumbToIndex > 0.06f && thumbToIndex < 0.20f;
        // All fingertips relatively close vertically
        float yRange = Math.abs(pts[INDEX_TIP][1] - pts[PINKY_TIP][1]);
        boolean curvedTogether = yRange < 0.12f;
        if (cGap && curvedTogether) return 0.88f;
        return 0f;
    }

    // -- D: Index finger extended, other fingers curled touching thumb --
    private float checkD(boolean[] f, float[][] pts) {
        if (!f[1]) return 0f; // Index must be up
        if (f[2] || f[3] || f[4]) return 0f; // Others curled
        // Middle, ring, pinky tips should be near thumb tip (forming circle)
        float midToThumb = distance2D(pts[MIDDLE_TIP], pts[THUMB_TIP]);
        boolean circle = midToThumb < 0.08f;
        return circle ? 0.90f : 0.82f;
    }

    // -- E: All fingertips curled down touching palm, thumb curled in front --
    private float checkE(boolean[] f, float[][] pts) {
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        // Fingertips should be near palm (very curled)
        float indexCurl = distance2D(pts[INDEX_TIP], pts[WRIST]);
        float middleCurl = distance2D(pts[MIDDLE_TIP], pts[WRIST]);
        boolean tightCurl = indexCurl < 0.18f && middleCurl < 0.18f;
        // Thumb tip should be in front of other fingers
        boolean thumbFront = pts[THUMB_TIP][1] > pts[INDEX_MCP][1];
        if (tightCurl && thumbFront) return 0.88f;
        if (!f[0] && tightCurl) return 0.82f;
        return 0f;
    }

    // -- F: Index+thumb touching (OK shape), middle/ring/pinky extended --
    private float checkF(boolean[] f, float[][] pts) {
        if (!f[2] || !f[3] || !f[4]) return 0f; // Middle, ring, pinky up
        // Index tip and thumb tip should be touching
        float indexThumb = distance2D(pts[INDEX_TIP], pts[THUMB_TIP]);
        boolean touching = indexThumb < 0.05f;
        if (touching) return 0.90f;
        return 0f;
    }

    // -- G: Index pointing sideways, thumb parallel --
    private float checkG(boolean[] f, float[][] pts) {
        if (!f[1]) return 0f; // Index extended
        if (f[2] || f[3] || f[4]) return 0f; // Others curled
        // Index should be pointing more horizontally than vertically
        float indexDx = Math.abs(pts[INDEX_TIP][0] - pts[INDEX_MCP][0]);
        float indexDy = Math.abs(pts[INDEX_TIP][1] - pts[INDEX_MCP][1]);
        boolean sideways = indexDx > indexDy;
        return sideways ? 0.88f : 0.3f;
    }

    // -- H: Index + middle extended sideways --
    private float checkH(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        // Fingers pointing sideways
        float indexDx = Math.abs(pts[INDEX_TIP][0] - pts[INDEX_MCP][0]);
        float indexDy = Math.abs(pts[INDEX_TIP][1] - pts[INDEX_MCP][1]);
        boolean sideways = indexDx > indexDy;
        return sideways ? 0.88f : 0.3f;
    }

    // -- I: Only pinky extended --
    private float checkI(boolean[] f, float[][] pts) {
        if (!f[4]) return 0f; // Pinky must be up
        if (f[1] || f[2] || f[3]) return 0f; // Others curled
        // Pinky should be pointing up
        boolean pinkyUp = pts[PINKY_TIP][1] < pts[PINKY_MCP][1];
        return pinkyUp ? 0.92f : 0.82f;
    }

    // -- J: Like I (pinky up) + motion trace (accept same as I for static detection) --
    private float checkJ(boolean[] f, float[][] pts) {
        // J is I + motion. For static detection, accept same pose as I.
        if (!f[4]) return 0f;
        if (f[1] || f[2] || f[3]) return 0f;
        boolean pinkyUp = pts[PINKY_TIP][1] < pts[PINKY_MCP][1];
        return pinkyUp ? 0.85f : 0f;
    }

    // -- K: Index+middle extended in V, thumb between them --
    private float checkK(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        // Thumb should be between index and middle
        float thumbX = pts[THUMB_TIP][0];
        float indexX = pts[INDEX_TIP][0];
        float middleX = pts[MIDDLE_TIP][0];
        boolean thumbBetween = (thumbX > Math.min(indexX, middleX) && thumbX < Math.max(indexX, middleX));
        return thumbBetween ? 0.88f : 0.4f;
    }

    // -- L: Index extended up + thumb extended sideways (L shape) --
    private float checkL(boolean[] f, float[][] pts) {
        if (!f[0] || !f[1]) return 0f; // Thumb and index up
        if (f[2] || f[3] || f[4]) return 0f; // Others curled
        // Angle between thumb and index should be roughly 90 degrees
        float thumbAngle = (float) Math.atan2(
            pts[THUMB_TIP][1] - pts[WRIST][1],
            pts[THUMB_TIP][0] - pts[WRIST][0]);
        float indexAngle = (float) Math.atan2(
            pts[INDEX_TIP][1] - pts[WRIST][1],
            pts[INDEX_TIP][0] - pts[WRIST][0]);
        float angleDiff = Math.abs(thumbAngle - indexAngle);
        boolean lShape = angleDiff > 0.8f && angleDiff < 2.5f;
        return lShape ? 0.90f : 0.82f;
    }

    // -- M: Fist with thumb under three fingers --
    private float checkM(boolean[] f, float[][] pts) {
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        // Thumb tip should be below (higher y) the ring finger MCP
        boolean thumbUnder = pts[THUMB_TIP][1] > pts[RING_MCP][1];
        // Thumb should be visible between ring and pinky area
        float thumbToRing = distance2D(pts[THUMB_TIP], pts[RING_MCP]);
        boolean nearRing = thumbToRing < 0.10f;
        if (thumbUnder && nearRing) return 0.88f;
        return 0f;
    }

    // -- N: Fist with thumb under two fingers (between middle and ring) --
    private float checkN(boolean[] f, float[][] pts) {
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        // Thumb tip between middle and ring MCPs
        boolean thumbUnder = pts[THUMB_TIP][1] > pts[MIDDLE_MCP][1];
        float thumbToMiddle = distance2D(pts[THUMB_TIP], pts[MIDDLE_MCP]);
        boolean nearMiddle = thumbToMiddle < 0.10f;
        if (thumbUnder && nearMiddle) return 0.88f;
        return 0f;
    }

    // -- O: All fingers curved to touch thumb tip (circle/oval) --
    private float checkO(boolean[] f, float[][] pts) {
        float indexToThumb = distance2D(pts[INDEX_TIP], pts[THUMB_TIP]);
        float middleToThumb = distance2D(pts[MIDDLE_TIP], pts[THUMB_TIP]);
        boolean circle = indexToThumb < 0.06f && middleToThumb < 0.08f;
        // Fingers should be curved, not fully extended
        boolean notExtended = !f[1] || !f[2];
        if (circle) return 0.90f;
        return 0f;
    }

    // -- P: Like K but pointing down --
    private float checkP(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        // Index and middle pointing downward
        boolean pointingDown = pts[INDEX_TIP][1] > pts[INDEX_MCP][1];
        return pointingDown ? 0.88f : 0.3f;
    }

    // -- Q: Like G but pointing down (index + thumb pinch downward) --
    private float checkQ(boolean[] f, float[][] pts) {
        if (f[2] || f[3] || f[4]) return 0f;
        // Index pointing downward
        boolean pointingDown = pts[INDEX_TIP][1] > pts[INDEX_MCP][1];
        // Thumb roughly parallel
        float thumbToIndex = distance2D(pts[THUMB_TIP], pts[INDEX_TIP]);
        boolean pinch = thumbToIndex < 0.10f;
        if (pointingDown && pinch) return 0.85f;
        return 0f;
    }

    // -- R: Index + middle crossed --
    private float checkR(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        // Index and middle tips should be very close (crossed)
        float tipDist = distance2D(pts[INDEX_TIP], pts[MIDDLE_TIP]);
        boolean crossed = tipDist < 0.04f;
        return crossed ? 0.90f : 0.4f;
    }

    // -- S: Fist with thumb in front of curled fingers --
    private float checkS(boolean[] f, float[][] pts) {
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        // Thumb in front (lower z or specific position)
        // Thumb tip should be near the middle of the curled fingers
        float thumbToMiddlePip = distance2D(pts[THUMB_TIP], pts[MIDDLE_PIP]);
        boolean thumbInFront = thumbToMiddlePip < 0.08f;
        if (thumbInFront) return 0.88f;
        // Generic fist as fallback for S
        return 0.75f;
    }

    // -- T: Fist with thumb between index and middle --
    private float checkT(boolean[] f, float[][] pts) {
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        // Thumb tip near index PIP (tucked between index and middle)
        float thumbToIndexPip = distance2D(pts[THUMB_TIP], pts[INDEX_PIP]);
        boolean thumbTucked = thumbToIndexPip < 0.06f;
        if (thumbTucked) return 0.88f;
        return 0f;
    }

    // -- U: Index + middle extended together, pointing up --
    private float checkU(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        // Fingers close together and pointing up
        float tipDist = Math.abs(pts[INDEX_TIP][0] - pts[MIDDLE_TIP][0]);
        boolean together = tipDist < 0.04f;
        boolean pointingUp = pts[INDEX_TIP][1] < pts[INDEX_MCP][1];
        if (together && pointingUp) return 0.90f;
        if (pointingUp) return 0.82f;
        return 0f;
    }

    // -- V: Index + middle extended in V shape (peace sign) --
    private float checkV(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        // Fingers spread apart (V shape)
        float tipDist = Math.abs(pts[INDEX_TIP][0] - pts[MIDDLE_TIP][0]);
        boolean spread = tipDist > 0.04f;
        boolean pointingUp = pts[INDEX_TIP][1] < pts[INDEX_MCP][1];
        if (spread && pointingUp) return 0.92f;
        if (pointingUp) return 0.82f;
        return 0f;
    }

    // -- W: Index + middle + ring extended, spread --
    private float checkW(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2] || !f[3]) return 0f;
        if (f[4]) return 0f; // Pinky curled
        boolean pointingUp = pts[INDEX_TIP][1] < pts[INDEX_MCP][1];
        return pointingUp ? 0.90f : 0.4f;
    }

    // -- X: Index finger bent into a hook --
    private float checkX(boolean[] f, float[][] pts) {
        if (f[2] || f[3] || f[4]) return 0f; // Others curled
        // Index DIP bent (tip curled back toward PIP)
        float tipToPip = distance2D(pts[INDEX_TIP], pts[INDEX_PIP]);
        float pipToMcp = distance2D(pts[INDEX_PIP], pts[INDEX_MCP]);
        boolean hooked = tipToPip < pipToMcp * 0.7f;
        // Index MCP should still be extended from wrist
        boolean mcpUp = pts[INDEX_MCP][1] < pts[WRIST][1];
        if (hooked && mcpUp) return 0.88f;
        return 0f;
    }

    // -- Y: Thumb + pinky extended, others curled --
    private float checkY(boolean[] f, float[][] pts) {
        if (!f[0] || !f[4]) return 0f; // Thumb and pinky must be out
        if (f[1] || f[2] || f[3]) return 0f; // Others curled
        return 0.92f;
    }

    // -- Z: Index pointing up + motion trace (accept index extended for static) --
    private float checkZ(boolean[] f, float[][] pts) {
        // Z is drawn in air with index finger. Accept index extended pointing up.
        if (!f[1]) return 0f;
        if (f[2] || f[3] || f[4]) return 0f;
        boolean pointingUp = pts[INDEX_TIP][1] < pts[INDEX_MCP][1];
        return pointingUp ? 0.85f : 0f;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Numbers & Words Checks
    // ═══════════════════════════════════════════════════════════════════

    // -- 0: O shape --
    private float check0(boolean[] f, float[][] pts) { return checkO(f, pts); }
    // -- 1: Index up --
    private float check1(boolean[] f, float[][] pts) {
        if (!f[1] || f[2] || f[3] || f[4]) return 0f;
        return 0.90f;
    }
    // -- 2: V shape --
    private float check2(boolean[] f, float[][] pts) { return checkV(f, pts); }
    // -- 3: Thumb, index, middle up --
    private float check3(boolean[] f, float[][] pts) {
        if (!f[0] || !f[1] || !f[2]) return 0f;
        if (f[3] || f[4]) return 0f;
        return 0.90f;
    }
    // -- 4: Four fingers up, thumb curled --
    private float check4(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2] || !f[3] || !f[4]) return 0f;
        if (f[0]) return 0f;
        return 0.90f;
    }
    // -- 5: All five spread --
    private float check5(boolean[] f, float[][] pts) {
        if (!f[0] || !f[1] || !f[2] || !f[3] || !f[4]) return 0f;
        return 0.90f;
    }
    // -- 6: Thumb and pinky touch --
    private float check6(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2] || !f[3]) return 0f;
        if (distance2D(pts[THUMB_TIP], pts[PINKY_TIP]) < 0.08f) return 0.90f;
        return 0f;
    }
    // -- 7: Thumb and ring touch --
    private float check7(boolean[] f, float[][] pts) {
        if (!f[1] || !f[2] || !f[4]) return 0f;
        if (distance2D(pts[THUMB_TIP], pts[RING_TIP]) < 0.08f) return 0.90f;
        return 0f;
    }
    // -- 8: Thumb and middle touch --
    private float check8(boolean[] f, float[][] pts) {
        if (!f[1] || !f[3] || !f[4]) return 0f;
        if (distance2D(pts[THUMB_TIP], pts[MIDDLE_TIP]) < 0.08f) return 0.90f;
        return 0f;
    }
    // -- 9: Thumb and index touch (F shape) --
    private float check9(boolean[] f, float[][] pts) { return checkF(f, pts); }

    // ═══════════════════════════════════════════════════════════════════
    //  Reusable Gesture Primitives for Category Signs
    // ═══════════════════════════════════════════════════════════════════

    // Open hand / 5-hand — all fingers extended outward (B with thumb out)
    private float checkOpenHand(boolean[] f, float[][] pts) {
        if (!f[0] || !f[1] || !f[2] || !f[3] || !f[4]) return 0f;
        return 0.88f;
    }

    // Thumbs up — only thumb extended
    private float checkThumbsUp(boolean[] f, float[][] pts) {
        if (!f[0]) return 0f;  // Thumb must be out
        if (f[1] || f[2] || f[3] || f[4]) return 0f;  // Others curled
        return 0.88f;
    }

    // Cradled arms — all fingers curled loosely (relaxed fist)
    private float checkCradleArms(boolean[] f, float[][] pts) {
        // Accept any mostly-closed hand shape
        if (f[1] || f[2] || f[3] || f[4]) return 0f;
        return 0.85f;
    }

    // Milk — squeeze motion; for static, accept alternating fist/open (check S-fist)
    private float checkMilk(boolean[] f, float[][] pts) {
        // Accept either fist (squeezing) or open hand (releasing)
        if (checkS(f, pts) > 0f) return 0.85f;
        if (checkOpenHand(f, pts) > 0f) return 0.82f;
        return 0f;
    }

    // Flower / Eat — pinched fingertips (all tips near each other)
    private float checkFlower(boolean[] f, float[][] pts) {
        float indexToThumb = distance2D(pts[THUMB_TIP], pts[INDEX_TIP]);
        float midToThumb = distance2D(pts[THUMB_TIP], pts[MIDDLE_TIP]);
        float ringToThumb = distance2D(pts[THUMB_TIP], pts[RING_TIP]);
        boolean pinched = indexToThumb < 0.07f && midToThumb < 0.09f && ringToThumb < 0.11f;
        return pinched ? 0.88f : 0f;
    }

    // Bird — index + thumb pinch (beak shape), others curled
    private float checkBird(boolean[] f, float[][] pts) {
        float pinch = distance2D(pts[INDEX_TIP], pts[THUMB_TIP]);
        if (pinch < 0.06f && !f[2] && !f[3] && !f[4]) return 0.88f;
        if (pinch < 0.06f) return 0.82f;
        return 0f;
    }

    // Claw — fingers curved outward (partially extended, tips curled)
    private float checkClaw(boolean[] f, float[][] pts) {
        // Fingers partially extended but tips are curled back
        float indexCurl = distance2D(pts[INDEX_TIP], pts[INDEX_PIP]);
        float middleCurl = distance2D(pts[MIDDLE_TIP], pts[MIDDLE_PIP]);
        boolean partialCurl = indexCurl < 0.08f && middleCurl < 0.08f;
        // But MCP should still show some extension
        boolean extended = pts[INDEX_MCP][1] < pts[WRIST][1];
        if (partialCurl && extended) return 0.85f;
        // Fallback: accept open hand (close enough for weather signs)
        if (checkOpenHand(f, pts) > 0f) return 0.82f;
        return 0f;
    }

    // NO — snap fingers to thumb
    private float checkNo(boolean[] f, float[][] pts) {
        if (!f[0] || !f[1] || !f[2]) {
            float pinch = distance2D(pts[THUMB_TIP], pts[INDEX_TIP]);
            if (pinch < 0.15f && !f[3] && !f[4]) return 0.85f;
            return 0f;
        }
        return 0.85f;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Identify what sign is currently being shown (for feedback)
    // ═══════════════════════════════════════════════════════════════════

    private String identifyCurrentSign(boolean[] fingers, float[][] pts) {
        String[] signs = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N",
                          "O","P","Q","R","S","T","U","V","W","X","Y","Z",
                          "0","1","2","3","4","5","6","7","8","9"};
        String best = null;
        float bestConf = 0f;

        for (String s : signs) {
            float conf = checkSpecificSign(s, fingers, pts);
            if (conf > bestConf) {
                bestConf = conf;
                best = s;
            }
        }

        return (bestConf >= 0.75f) ? best : null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Landmark Extraction & Finger State Measurement
    // ═══════════════════════════════════════════════════════════════════

    private float[][] extractLandmarkArray(List<NormalizedLandmark> landmarks) {
        float[][] points = new float[21][3];
        for (int i = 0; i < 21; i++) {
            NormalizedLandmark lm = landmarks.get(i);
            points[i][0] = lm.x();
            points[i][1] = lm.y();
            points[i][2] = lm.z();
        }
        return points;
    }

    /**
     * Determine which fingers are extended vs. curled.
     * [thumb, index, middle, ring, pinky]
     */
    private boolean[] measureFingerStates(float[][] pts) {
        boolean[] states = new boolean[5];
        float threshold = 1.25f;

        // Thumb
        float thumbTipDist = distance2D(pts[THUMB_TIP], pts[WRIST]);
        float thumbMcpDist = distance2D(pts[THUMB_MCP], pts[WRIST]);
        states[0] = thumbTipDist > thumbMcpDist * threshold;

        // Index
        states[1] = isFingerExtended(pts, INDEX_TIP, INDEX_PIP, INDEX_MCP);

        // Middle
        states[2] = isFingerExtended(pts, MIDDLE_TIP, MIDDLE_PIP, MIDDLE_MCP);

        // Ring
        states[3] = isFingerExtended(pts, RING_TIP, RING_PIP, RING_MCP);

        // Pinky
        states[4] = isFingerExtended(pts, PINKY_TIP, PINKY_PIP, PINKY_MCP);

        return states;
    }

    private boolean isFingerExtended(float[][] pts, int tipIdx, int pipIdx, int mcpIdx) {
        // Finger is extended if tip is farther from wrist than MCP
        // AND tip is above (lower y) PIP (not curled back)
        float tipToWrist = distance2D(pts[tipIdx], pts[WRIST]);
        float mcpToWrist = distance2D(pts[mcpIdx], pts[WRIST]);
        boolean farEnough = tipToWrist > mcpToWrist * 1.2f;
        boolean tipAbovePip = pts[tipIdx][1] < pts[pipIdx][1];
        return farEnough && tipAbovePip;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Utility
    // ═══════════════════════════════════════════════════════════════════

    private float distance2D(float[] a, float[] b) {
        float dx = a[0] - b[0];
        float dy = a[1] - b[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
