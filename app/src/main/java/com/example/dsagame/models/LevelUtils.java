package com.example.dsagame.models;

public class LevelUtils {
    private static final int[] LEVEL_THRESHOLDS = {0, 100, 250, 500, 1000, 2000, 3500};

    public static int getLevel(int xp) {
        for (int level = LEVEL_THRESHOLDS.length - 1; level >= 0; level--) {
            if (xp >= LEVEL_THRESHOLDS[level]) {
                return level + 1;
            }
        }
        return 1;
    }

    public static int getCurrentLevelProgress(int xp) {
        int level = getLevel(xp);
        int currentThreshold = (level > 1) ? LEVEL_THRESHOLDS[level-2] : 0;
        int nextThreshold = LEVEL_THRESHOLDS[level-1];
        return (int) ((xp - currentThreshold) * 100f / (nextThreshold - currentThreshold));
    }
}