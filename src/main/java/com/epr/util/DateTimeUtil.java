package com.epr.util;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Central Date-Time Utility for the entire application
 * Handles IST (UTC+5:30) ↔ UTC conversion
 * Server runs in AWS America → All DB timestamps are in UTC
 * But business logic & display must be in IST (Indian Standard Time)
 */
@Component
public class DateTimeUtil {


    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");     // UTC +05:30
    private static final ZoneId UTC = ZoneId.of("UTC");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ======================== TO IST (For Display / Business Logic) ========================

    /** Convert UTC LocalDateTime → IST LocalDateTime */
    public LocalDateTime utcToIst(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return null;
        return utcDateTime.atZone(UTC).withZoneSameInstant(IST).toLocalDateTime();
    }

    /** Convert UTC ZonedDateTime → IST ZonedDateTime */
    public ZonedDateTime utcToIst(ZonedDateTime utcZoned) {
        if (utcZoned == null) return null;
        return utcZoned.withZoneSameInstant(IST);
    }

    /** Get current time in IST */
    public LocalDateTime nowIst() {
        return LocalDateTime.now(IST);
    }

    /** Get current date in IST */
    public LocalDate todayIst() {
        return LocalDate.now(IST);
    }

    // ======================== TO UTC (For Saving to DB) ========================

    /** Convert IST LocalDateTime → UTC LocalDateTime (Use before saving) */
    public LocalDateTime istToUtc(LocalDateTime istDateTime) {
        if (istDateTime == null) return null;
        return istDateTime.atZone(IST).withZoneSameInstant(UTC).toLocalDateTime();
    }

    /** Convert IST ZonedDateTime → UTC ZonedDateTime */
    public ZonedDateTime istToUtc(ZonedDateTime istZoned) {
        if (istZoned == null) return null;
        return istZoned.withZoneSameInstant(UTC);
    }

    /** Get current time in UTC (for DB insert) */
    public LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC);
    }

    // ======================== Formatting (IST Display) ========================

    public String formatDateTimeIst(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return utcToIst(dateTime).format(DATE_TIME_FORMATTER);
    }

    public String formatDateIst(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return utcToIst(dateTime).format(DATE_FORMATTER);
    }

    public String formatTimeIst(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return utcToIst(dateTime).format(TIME_FORMATTER);
    }

    // ======================== Current Timestamps (Most Used) ========================

    /** Use this in @PrePersist → saves in UTC */
    public LocalDateTime getCurrentUtcTime() {
        return LocalDateTime.now(UTC);
    }

    /** Use this in entity for display → convert to IST */
    public String getCurrentIstTimeFormatted() {
        return LocalDateTime.now(IST).format(DATE_TIME_FORMATTER);
    }
}