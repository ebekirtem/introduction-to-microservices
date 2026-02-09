package com.introms.util;


public class Utility {
    public static String formatDuration(String durationInSeconds) {
        if (durationInSeconds == null || durationInSeconds.isEmpty()) {
            return "00:00";
        }
        try {
            double totalSeconds = Double.parseDouble(durationInSeconds);

            int minutes = (int) totalSeconds / 60;
            int seconds = (int) Math.round(totalSeconds % 60);

            return String.format("%02d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration format: " + durationInSeconds, e);
        }
    }
}
