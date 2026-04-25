package com.signquest.app.data;

import java.util.ArrayList;
import java.util.List;

/**
 * SignDataProvider — Central data source for all sign language content.
 *
 * Provides sign data for 3 languages (ASL, ISL, BSL) across 10 levels:
 *   Level 1: Alphabets (A–Z)
 *   Level 2: Numbers (0–9)
 *   Level 3: Greetings
 *   Level 4: Family
 *   Level 5: Groceries
 *   Level 6: Nature
 *   Level 7: Pets
 *   Level 8: Breakfast
 *   Level 9: Weather
 *   Level 10: Salutations
 */
public class SignDataProvider {

    // Language constants
    public static final String LANG_ASL = "ASL";
    public static final String LANG_ISL = "ISL";
    public static final String LANG_BSL = "BSL";

    // Level constants
    public static final int LEVEL_ALPHABETS   = 1;
    public static final int LEVEL_NUMBERS     = 2;
    public static final int LEVEL_GREETINGS   = 3;
    public static final int LEVEL_FAMILY      = 4;
    public static final int LEVEL_GROCERIES   = 5;
    public static final int LEVEL_NATURE      = 6;
    public static final int LEVEL_PETS        = 7;
    public static final int LEVEL_BREAKFAST   = 8;
    public static final int LEVEL_WEATHER     = 9;
    public static final int LEVEL_SALUTATIONS = 10;

    public static final int MAX_LEVEL = 10;

    // ═══════════════════════════════════════════════════════════════════
    //  Sign Data Model
    // ═══════════════════════════════════════════════════════════════════

    public static class SignItem {
        private final String key;
        private final String displayLabel;
        private final String emoji;
        private final String instructions;
        private final String language;
        private final int levelId;

        public SignItem(String key, String displayLabel, String emoji,
                        String instructions, String language, int levelId) {
            this.key = key;
            this.displayLabel = displayLabel;
            this.emoji = emoji;
            this.instructions = instructions;
            this.language = language;
            this.levelId = levelId;
        }

        public String getKey()          { return key; }
        public String getDisplayLabel() { return displayLabel; }
        public String getEmoji()        { return emoji; }
        public String getInstructions() { return instructions; }
        public String getLanguage()     { return language; }
        public int    getLevelId()      { return levelId; }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Level Metadata
    // ═══════════════════════════════════════════════════════════════════

    public static class LevelInfo {
        private final int id;
        private final String title;
        private final String emoji;
        private final String description;

        public LevelInfo(int id, String title, String emoji, String description) {
            this.id = id;
            this.title = title;
            this.emoji = emoji;
            this.description = description;
        }

        public int    getId()          { return id; }
        public String getTitle()       { return title; }
        public String getEmoji()       { return emoji; }
        public String getDescription() { return description; }
    }

    public static List<LevelInfo> getLevels() {
        List<LevelInfo> levels = new ArrayList<>();
        levels.add(new LevelInfo(LEVEL_ALPHABETS,   "Alphabets",   "🔤", "Learn A–Z in sign language"));
        levels.add(new LevelInfo(LEVEL_NUMBERS,     "Numbers",     "🔢", "Learn 0–9 in sign language"));
        levels.add(new LevelInfo(LEVEL_GREETINGS,   "Greetings",   "👋", "Learn common greetings"));
        levels.add(new LevelInfo(LEVEL_FAMILY,      "Family",      "👨‍👩‍👧", "Learn family member signs"));
        levels.add(new LevelInfo(LEVEL_GROCERIES,   "Groceries",   "🍎", "Learn grocery & food signs"));
        levels.add(new LevelInfo(LEVEL_NATURE,      "Nature",      "🌿", "Learn nature & environment signs"));
        levels.add(new LevelInfo(LEVEL_PETS,        "Pets",        "🐾", "Learn animal & pet signs"));
        levels.add(new LevelInfo(LEVEL_BREAKFAST,   "Breakfast",   "🥞", "Learn breakfast item signs"));
        levels.add(new LevelInfo(LEVEL_WEATHER,     "Weather",     "⛅", "Learn weather-related signs"));
        levels.add(new LevelInfo(LEVEL_SALUTATIONS, "Salutations", "🤝", "Learn formal greetings & farewells"));
        return levels;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Get Signs for a Language + Level
    // ═══════════════════════════════════════════════════════════════════

    public static List<SignItem> getSigns(String language, int levelId) {
        switch (levelId) {
            case LEVEL_ALPHABETS:   return getAlphabets(language);
            case LEVEL_NUMBERS:     return getNumbers(language);
            case LEVEL_GREETINGS:   return getGreetings(language);
            case LEVEL_FAMILY:      return getFamily(language);
            case LEVEL_GROCERIES:   return getGroceries(language);
            case LEVEL_NATURE:      return getNature(language);
            case LEVEL_PETS:        return getPets(language);
            case LEVEL_BREAKFAST:   return getBreakfast(language);
            case LEVEL_WEATHER:     return getWeather(language);
            case LEVEL_SALUTATIONS: return getSalutations(language);
            default: return new ArrayList<>();
        }
    }

    public static int getTotalSignCount(int levelId) {
        switch (levelId) {
            case LEVEL_ALPHABETS: return 26;
            case LEVEL_NUMBERS:   return 10;
            default:              return 6; // All category levels have 6 signs each
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 1: ALPHABETS (A–Z) — Unchanged
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getAlphabets(String language) {
        List<SignItem> signs = new ArrayList<>();
        switch (language) {
            case LANG_ASL: signs = getASLAlphabets(); break;
            case LANG_ISL: signs = getISLAlphabets(); break;
            case LANG_BSL: signs = getBSLAlphabets(); break;
        }
        return signs;
    }

    private static List<SignItem> getASLAlphabets() {
        List<SignItem> s = new ArrayList<>();
        s.add(sign("A", "A", "🤛", "Make a fist with your thumb resting on the side of your index finger", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("B", "B", "🖐", "Hold all four fingers straight up and together, thumb tucked across palm", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("C", "C", "🫲", "Curve your hand into a C shape, like holding a cup", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("D", "D", "☝️", "Touch your thumb to your middle, ring, and pinky fingertips. Index finger points up", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("E", "E", "✊", "Curl all fingers down, thumb tucked under fingertips", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("F", "F", "👌", "Touch your index finger and thumb together, other three fingers spread up", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("G", "G", "👉", "Point index finger sideways, thumb parallel above it, other fingers curled", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("H", "H", "✌️", "Point index and middle fingers sideways together, other fingers curled", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("I", "I", "🤙", "Make a fist, extend only your pinky finger straight up", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("J", "J", "🤙", "Start with 'I' hand, then trace a J shape in the air with your pinky", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("K", "K", "✌️", "Index and middle fingers up in a V, thumb touches middle finger side", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("L", "L", "🤟", "Extend thumb and index finger to form an L shape, other fingers curled", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("M", "M", "✊", "Tuck your thumb under your index, middle, and ring fingers over the thumb", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("N", "N", "✊", "Tuck your thumb under your index and middle fingers", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("O", "O", "👌", "Curl all fingers to touch your thumb, forming an O shape", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("P", "P", "👇", "Like K but point downward — index and middle out, thumb between them", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("Q", "Q", "👇", "Like G but pointing down — thumb and index finger pinch downward", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("R", "R", "✌️", "Cross your middle finger over your index finger, other fingers curled", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("S", "S", "✊", "Make a fist with your thumb across the front of your fingers", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("T", "T", "✊", "Make a fist, tuck your thumb between your index and middle fingers", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("U", "U", "✌️", "Hold index and middle fingers straight up together, other fingers curled", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("V", "V", "✌️", "Spread index and middle fingers apart in a V shape", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("W", "W", "🤟", "Spread index, middle, and ring fingers apart, pinky and thumb curled", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("X", "X", "☝️", "Curl your index finger into a hook shape, other fingers in a fist", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("Y", "Y", "🤙", "Extend thumb and pinky finger, curl other three fingers down", LANG_ASL, LEVEL_ALPHABETS));
        s.add(sign("Z", "Z", "☝️", "Point index finger up, then trace a Z shape in the air", LANG_ASL, LEVEL_ALPHABETS));
        return s;
    }

    private static List<SignItem> getISLAlphabets() {
        List<SignItem> s = new ArrayList<>();
        s.add(sign("A", "A", "🤛", "Make a fist with thumb pointing up alongside your hand", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("B", "B", "🖐", "All fingers extended upward and together, thumb across palm", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("C", "C", "🫲", "Curve hand into a C shape, fingers together, thumb opposite", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("D", "D", "☝️", "Index finger points up, other fingers and thumb form a circle", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("E", "E", "✊", "Fingers curled down to meet thumb, like a claw facing down", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("F", "F", "👌", "Thumb and index form a circle, other fingers spread upward", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("G", "G", "👉", "Index finger and thumb point sideways, parallel to each other", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("H", "H", "✌️", "Index and middle fingers extended sideways together", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("I", "I", "🤙", "Fist with only pinky finger extended upward", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("J", "J", "🤙", "Pinky up, then draw a J curve downward in the air", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("K", "K", "✌️", "Index and middle up, thumb touching middle finger", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("L", "L", "🤟", "Index finger up, thumb out to the side forming an L", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("M", "M", "✊", "Three fingers (index, middle, ring) draped over thumb", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("N", "N", "✊", "Two fingers (index, middle) draped over thumb", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("O", "O", "👌", "All fingertips touch thumb forming a round O", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("P", "P", "👇", "Like K hand shape but pointing downward", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("Q", "Q", "👇", "Thumb and index pinch together pointing down", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("R", "R", "✌️", "Cross middle finger over index, other fingers closed", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("S", "S", "✊", "Fist with thumb wrapped over the front of fingers", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("T", "T", "✊", "Thumb tucked between index and middle fingers in fist", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("U", "U", "✌️", "Index and middle fingers together pointing up", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("V", "V", "✌️", "Index and middle fingers spread in a V", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("W", "W", "🤟", "Index, middle, ring fingers spread apart", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("X", "X", "☝️", "Index finger bent into a hook", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("Y", "Y", "🤙", "Thumb and pinky extended, other fingers curled", LANG_ISL, LEVEL_ALPHABETS));
        s.add(sign("Z", "Z", "☝️", "Draw the letter Z in the air with index finger", LANG_ISL, LEVEL_ALPHABETS));
        return s;
    }

    private static List<SignItem> getBSLAlphabets() {
        List<SignItem> s = new ArrayList<>();
        s.add(sign("A", "A", "✋", "Point index finger up on one hand, touch its tip with other index finger", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("B", "B", "✋", "Flat hand, palm facing out, fingers pointing up together", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("C", "C", "🫲", "Curve one hand into a C shape facing the other person", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("D", "D", "☝️", "Tap your index fingertip with the other hand's index finger", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("E", "E", "✋", "Tap the tips of your extended fingers with other index finger", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("F", "F", "✌️", "Both index and middle fingers together, tapped by other hand", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("G", "G", "👉", "Point index finger sideways, other hand points at it", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("H", "H", "✋", "Flat hand palm down, stroke across with other hand", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("I", "I", "☝️", "Pinky finger up, tapped by other hand's index", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("J", "J", "🤙", "Pinky up, trace J shape moving down", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("K", "K", "✋", "Flat hand out, other hand chops across the middle", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("L", "L", "🤟", "L shape with thumb and index of one hand", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("M", "M", "✋", "Three fingers draped over other hand's index finger", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("N", "N", "✋", "Two fingers draped over other hand's index finger", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("O", "O", "👌", "Form an O with fingers and thumb", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("P", "P", "👉", "Index finger points forward from fist, tap with other hand", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("Q", "Q", "👇", "Index and thumb pinch, point downward", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("R", "R", "✌️", "Cross index and middle fingers", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("S", "S", "✋", "Flat hand facing out, other hand draws S on palm", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("T", "T", "☝️", "Index up, cross with other index finger forming a T", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("U", "U", "✌️", "Two fingers up together from one hand", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("V", "V", "✌️", "V shape with index and middle fingers", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("W", "W", "🤟", "Spread three fingers (index, middle, ring) apart", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("X", "X", "✌️", "Cross both index fingers to form an X", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("Y", "Y", "🤙", "Thumb and pinky extended on one hand", LANG_BSL, LEVEL_ALPHABETS));
        s.add(sign("Z", "Z", "☝️", "Trace Z in the air with your index finger", LANG_BSL, LEVEL_ALPHABETS));
        return s;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 2: NUMBERS (0–9) — Unchanged
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getNumbers(String language) {
        List<SignItem> s = new ArrayList<>();
        String lang = language;
        String[][] numberData;
        switch (language) {
            case LANG_ISL:
                numberData = new String[][] {
                    {"0", "Open hand then close all fingers into an O shape"},
                    {"1", "Index finger pointing up, all others curled"},
                    {"2", "Index and middle fingers up, spread apart"},
                    {"3", "Thumb, index, and middle finger extended"},
                    {"4", "Four fingers up (no thumb), fingers spread"},
                    {"5", "All five fingers spread open"},
                    {"6", "Thumb and pinky touch, other fingers up"},
                    {"7", "Thumb and ring finger touch, others extended"},
                    {"8", "Thumb and middle finger touch, others extended"},
                    {"9", "Thumb and index touch, others extended"},
                };
                break;
            case LANG_BSL:
                numberData = new String[][] {
                    {"0", "Make an O shape with all fingers and thumb"},
                    {"1", "Index finger pointing up from a fist"},
                    {"2", "Index and middle fingers up together"},
                    {"3", "Index, middle, and ring fingers extended"},
                    {"4", "Four fingers up, thumb tucked in"},
                    {"5", "All five fingers spread wide open"},
                    {"6", "Show five fingers, then tap thumb to pinky"},
                    {"7", "Show five fingers, then tap thumb to ring finger"},
                    {"8", "Show five fingers, then tap thumb to middle finger"},
                    {"9", "Show five fingers, then tap thumb to index finger"},
                };
                break;
            default:
                numberData = new String[][] {
                    {"0", "Form an O shape by touching all fingertips to thumb"},
                    {"1", "Point index finger up, all other fingers in a fist"},
                    {"2", "Index and middle fingers up and spread, others curled"},
                    {"3", "Thumb, index, and middle fingers extended, others curled"},
                    {"4", "All four fingers extended up, thumb curled across palm"},
                    {"5", "All five fingers spread wide open, palm facing forward"},
                    {"6", "Thumb touches pinky tip, other three fingers extended up"},
                    {"7", "Thumb touches ring finger tip, other three up"},
                    {"8", "Thumb touches middle finger tip, others extended"},
                    {"9", "Thumb touches index finger tip, others extended"},
                };
                break;
        }

        String[] numEmojis = {"0️⃣","1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣"};
        for (int i = 0; i < numberData.length; i++) {
            s.add(sign(numberData[i][0], numberData[i][0], numEmojis[i],
                    numberData[i][1], lang, LEVEL_NUMBERS));
        }
        return s;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 3: GREETINGS 👋
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getGreetings(String language) {
        String[][] data = {
            {"HELLO",     "Hello",     "👋", "Open hand near forehead, like a salute, then wave outward"},
            {"HI",        "Hi",        "🙋", "Raise open hand and wave side to side"},
            {"GOOD_MORNING", "Good Morning", "🌅", "Flat hand at chin moves forward, then arm rises like the sun"},
            {"GOOD_NIGHT", "Good Night", "🌙", "Dominant hand curves downward over non-dominant flat hand"},
            {"HOW_ARE_YOU", "How Are You", "🤔", "Point both thumbs up, move them forward alternately"},
            {"WELCOME",   "Welcome",   "🤗", "Open hand sweeps inward toward your body"},
        };
        return buildCategory(data, language, LEVEL_GREETINGS);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 4: FAMILY 👨‍👩‍👧
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getFamily(String language) {
        String[][] data = {
            {"MOTHER",  "Mother",  "👩", "Open 5-hand, thumb touches chin"},
            {"FATHER",  "Father",  "👨", "Open 5-hand, thumb touches forehead"},
            {"SISTER",  "Sister",  "👧", "Thumb traces jaw from ear to chin, then both index fingers tap together"},
            {"BROTHER", "Brother", "👦", "Thumb on forehead then both index fingers tap together"},
            {"BABY",    "Baby",    "👶", "Cradle arms and rock them side to side"},
            {"FRIEND",  "Friend",  "🤝", "Hook index fingers together, flip over and hook again"},
        };
        return buildCategory(data, language, LEVEL_FAMILY);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 5: GROCERIES 🍎
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getGroceries(String language) {
        String[][] data = {
            {"APPLE",  "Apple",  "🍎", "Twist the knuckle of your index finger on your cheek"},
            {"MILK",   "Milk",   "🥛", "Squeeze fist open and closed, like milking a cow"},
            {"BREAD",  "Bread",  "🍞", "Slice the back of your non-dominant hand with dominant hand"},
            {"WATER",  "Water",  "💧", "W-hand taps chin twice with index finger"},
            {"EGG",    "Egg",    "🥚", "H-hands break apart and move down, like cracking an egg"},
            {"RICE",   "Rice",   "🍚", "Cup both hands together and scoop upward"},
        };
        return buildCategory(data, language, LEVEL_GROCERIES);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 6: NATURE 🌿
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getNature(String language) {
        String[][] data = {
            {"TREE",   "Tree",   "🌳", "Rest elbow on flat palm, spread fingers and twist forearm"},
            {"FLOWER", "Flower", "🌸", "Pinched fingers touch one side of nose, then the other"},
            {"WATER_N","Water",  "💧", "W-hand taps chin twice with index finger"},
            {"SUN",    "Sun",    "☀️", "Draw a circle in the air, then open hand radiating outward"},
            {"MOUNTAIN","Mountain","⛰️", "Tap fists together then sweep hands upward in a peak shape"},
            {"RIVER",  "River",  "🏞️", "W-hand wiggles forward away from body, like flowing water"},
        };
        return buildCategory(data, language, LEVEL_NATURE);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 7: PETS 🐾
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getPets(String language) {
        String[][] data = {
            {"DOG",    "Dog",    "🐕", "Pat your thigh twice and snap your fingers"},
            {"CAT",    "Cat",    "🐱", "Pinch thumb and index finger at cheek, pull outward (whiskers)"},
            {"BIRD",   "Bird",   "🐦", "Index and thumb pinch at mouth, open and close like a beak"},
            {"FISH",   "Fish",   "🐟", "Flat hand wiggles forward like a fish swimming"},
            {"RABBIT", "Rabbit", "🐰", "H-hand (two fingers) at head, bend up and down like ears"},
            {"TURTLE", "Turtle", "🐢", "Cup dominant hand over non-dominant fist, wiggle thumb out like a head"},
        };
        return buildCategory(data, language, LEVEL_PETS);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 8: BREAKFAST 🥞
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getBreakfast(String language) {
        String[][] data = {
            {"EAT",    "Eat",    "🍽️", "Bunched fingertips tap mouth repeatedly"},
            {"DRINK",  "Drink",  "🥤", "C-hand tilts toward mouth like holding a cup"},
            {"CEREAL", "Cereal", "🥣", "Curved finger scoops from flat palm to mouth repeatedly"},
            {"JUICE",  "Juice",  "🧃", "Fingerspell J then C-hand tilts to mouth"},
            {"TOAST",  "Toast",  "🍞", "V-hand stabs non-dominant flat palm, then flips and stabs other side"},
            {"COFFEE", "Coffee", "☕", "Two S-fists stack and grind in a circle, like a coffee grinder"},
        };
        return buildCategory(data, language, LEVEL_BREAKFAST);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 9: WEATHER ⛅
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getWeather(String language) {
        String[][] data = {
            {"RAIN",   "Rain",   "🌧️", "Open 5-hands move down repeatedly, like rain falling"},
            {"SNOW",   "Snow",   "❄️", "Five fingers wiggle as hands float downward gently"},
            {"WIND",   "Wind",   "💨", "Both open hands sweep sideways together in a swoosh"},
            {"HOT",    "Hot",    "🔥", "Claw hand at mouth rotates outward quickly"},
            {"COLD",   "Cold",   "🥶", "Both fists tremble near body, like shivering"},
            {"CLOUD",  "Cloud",  "☁️", "Both claw hands roll over each other in front of face"},
        };
        return buildCategory(data, language, LEVEL_WEATHER);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEVEL 10: SALUTATIONS 🤝
    // ═══════════════════════════════════════════════════════════════════

    private static List<SignItem> getSalutations(String language) {
        String[][] data = {
            {"THANK_YOU", "Thank You", "🙏", "Touch your chin with fingertips, move hand forward and down"},
            {"PLEASE",    "Please",    "🤲", "Flat hand on chest, rub in a circle"},
            {"SORRY",     "Sorry",     "😔", "Make a fist with A-hand, rub circle on chest"},
            {"GOODBYE",   "Goodbye",   "👋", "Open palm facing out, fingers fold down repeatedly"},
            {"YES",       "Yes",       "✅", "Make a fist and nod it up and down like a nodding head"},
            {"NO",        "No",        "❌", "Snap your index and middle fingers to your thumb quickly"},
        };
        return buildCategory(data, language, LEVEL_SALUTATIONS);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    /** Build a category list from a simple data array. */
    private static List<SignItem> buildCategory(String[][] data, String language, int levelId) {
        List<SignItem> s = new ArrayList<>();
        for (String[] w : data) {
            s.add(sign(w[0], w[1], w[2], w[3], language, levelId));
        }
        return s;
    }

    private static SignItem sign(String key, String label, String emoji,
                                  String instructions, String lang, int level) {
        return new SignItem(key, label, emoji, instructions, lang, level);
    }
}
