package com.marineguard.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy";
    private static final String DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";

    private DateUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get current date and time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Get current date
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Convert LocalDateTime to String with default format
     */
    public static String toString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
        return dateTime.format(formatter);
    }

    /**
     * Convert LocalDate to String with default format
     */
    public static String toString(LocalDate date) {
        if (date == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        return date.format(formatter);
    }

    /**
     * Convert String to LocalDateTime with default format
     */
    public static LocalDateTime toLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convert String to LocalDate with default format
     */
    public static LocalDate toLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Format date for display (e.g., "15 Jan 2024")
     */
    public static String formatForDisplay(LocalDate date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT);
        return date.format(formatter);
    }

    /**
     * Format datetime for display (e.g., "15 Jan 2024, 02:30 PM")
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DISPLAY_DATETIME_FORMAT);
        return dateTime.format(formatter);
    }

    /**
     * Convert java.util.Date to LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to java.util.Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate hours between two date-times
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Check if date is within last 24 hours
     */
    public static boolean isWithinLast24Hours(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        return hours <= 24;
    }

    /**
     * Check if date is within last 7 days
     */
    public static boolean isWithinLastWeek(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());
        return days <= 7;
    }

    /**
     * Check if date is within last 30 days
     */
    public static boolean isWithinLastMonth(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());
        return days <= 30;
    }

    /**
     * Get start of day (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay();
    }

    /**
     * Get end of day (23:59:59)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Get start of current month
     */
    public static LocalDate startOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Get end of current month
     */
    public static LocalDate endOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    /**
     * Get start of current year
     */
    public static LocalDate startOfCurrentYear() {
        return LocalDate.now().withDayOfYear(1);
    }

    /**
     * Get end of current year
     */
    public static LocalDate endOfCurrentYear() {
        return LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
    }

    /**
     * Add days to date
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) return null;
        return dateTime.plusDays(days);
    }

    /**
     * Subtract days from date
     */
    public static LocalDateTime subtractDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) return null;
        return dateTime.minusDays(days);
    }

    /**
     * Get age from birthdate
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Check if date is future
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Check if date is past
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Get relative time description (e.g., "2 hours ago", "yesterday")
     */
    public static String getRelativeTimeDescription(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());

        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (days == 1) {
            return "yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        } else {
            long years = days / 365;
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        }
    }

    /**
     * Parse date from various formats
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;

        String[] possibleFormats = {
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "dd-MM-yyyy",
                "dd MMM yyyy",
                "yyyyMMdd"
        };

        for (String format : possibleFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        return null;
    }
}