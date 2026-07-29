package com.example.cateredlunches.model;

import java.time.LocalDate;

/**
 * Pairs a {@link CateredWeek} with its external key (week start date).
 * Used when a caller needs to display or reason about the key alongside
 * the week's contents, without making the key part of {@link CateredWeek} itself.
 */
public class CateredWeekEntry {
    private final LocalDate weekStartKey;
    private final CateredWeek weekValue;

    public CateredWeekEntry(LocalDate weekStartKey, CateredWeek weekValue) {
        this.weekStartKey = weekStartKey;
        this.weekValue = weekValue;
    }

    public LocalDate getWeekStartKey() {
        return weekStartKey;
    }

    public CateredWeek getWeekValue() {
        return weekValue;
    }

}
