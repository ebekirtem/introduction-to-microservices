package com.introms.util;


import com.introms.exception.InvalidIdCsvException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static boolean isValidIds(String ids){
        String[] parts = ids.split(",");
        for(String part:parts){
            if(!isIdValid(part))
                return false;
        }
        return true;
    }

    public static List<Integer> validateAndParse(String csv, int maxLength){
        if(csv==null || csv.isBlank()){
            throw new InvalidIdCsvException("CSV string format is invalid or missing");
        }
        if(csv.length()>maxLength){
            throw new InvalidIdCsvException(String.format("CSV string is too long: received %d characters, maximum allowed is %d",csv.length(),maxLength));
        }

        if(!csv.matches("^[0-9]+(?:,[0-9]+)*+")){
            String[] parts = csv.split(",");
            for(int i=0;i<parts.length;i++){
                String p=parts[i];
                if(p.isBlank()){
                    throw new InvalidIdCsvException("Invalid empty token at position "+i);
                }
                if(!p.matches("^\\d++")){
                    throw new InvalidIdCsvException(String.format("Invalid ID %s at position %d",p,i));
                }
            }
            throw new InvalidIdCsvException("CSV string format is invalid");
        }

        List<Integer> idList=new ArrayList<>();

        String[] parts = csv.split(",");
        for(int i=0;i<parts.length;i++){
            String p = parts[i];
            try {
                int pp = Integer.parseInt(p);
                if(pp<=0){
                    throw new InvalidIdCsvException(String.format("Invalid ID format: '%s'. Only positive integers are allowed",p));
                }
                idList.add(pp);
            } catch (NumberFormatException e) {
                throw new InvalidIdCsvException(String.format("Invalid ID %s at position %d; too large for 32 bit integer integer",p,i));
            }
        }
        return idList.stream().distinct().collect(Collectors.toList());
    }

}
