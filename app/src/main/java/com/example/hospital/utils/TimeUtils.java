package com.example.hospital.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    
    public static boolean esHoraValida(String horaStr) {
        if (horaStr == null || horaStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(horaStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    public static Date parsearHora(String horaStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.parse(horaStr);
    }
    
    public static String formatearHora(Date hora) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(hora);
    }
    
    public static DayOfWeek convertirDia(String diaStr) {
        String diaLower = diaStr.toLowerCase().trim();
        switch (diaLower) {
            case "lunes":
            case "monday":
                return DayOfWeek.MONDAY;
            case "martes":
            case "tuesday":
                return DayOfWeek.TUESDAY;
            case "miércoles":
            case "miercoles":
            case "wednesday":
                return DayOfWeek.WEDNESDAY;
            case "jueves":
            case "thursday":
                return DayOfWeek.THURSDAY;
            case "viernes":
            case "friday":
                return DayOfWeek.FRIDAY;
            case "sábado":
            case "sabado":
            case "saturday":
                return DayOfWeek.SATURDAY;
            case "domingo":
            case "sunday":
                return DayOfWeek.SUNDAY;
            default:
                return DayOfWeek.MONDAY;
        }
    }
    
    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}