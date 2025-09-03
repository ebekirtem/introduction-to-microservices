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

    public static Integer parseYear(String rawYear){
        if(rawYear==null || rawYear.isBlank()){
            return null;
        }
        try {
            String trimmed=rawYear.trim();
            String strYear=trimmed.substring(0,4);
            return Integer.parseInt(strYear);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Year could not be parsed",e);
        }
    }

    public static boolean isIdValid(String id){
        if(id==null){
            return false;
        }
        if(!id.matches("^[0-9]+$")){
            return false;
        }
        try {
            int pid=Integer.parseInt(id);
            return pid>0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidIds(String ids){
        if(ids==null || ids.isBlank()||ids.length()>200){
            return false;
        }
        String[] parts = ids.split(",");
        for(String part:parts){
            if(!isIdValid(part))
                return false;
        }
        return true;
    }
}
