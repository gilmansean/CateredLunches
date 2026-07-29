package com.example.cateredlunches.services.validation;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.MenuDay;

import java.util.List;
import java.util.Objects;

/**
 * Validation utilities for menu data.
 * - Does NOT enforce a particular weekStart or start-of-week policy (that is a user preference).
 * - Ensures at least one day is present and each entry is well-formed.
 */
public final class MenuValidation {

    private static final int MAX_TEXT = 200;

    private MenuValidation() {
    }

    /**
     * Validate a list of weeks.
     *
     * @param weeks - List of weeks to validate.
     */
    public static void validateWeeks(List<CateredWeek> weeks) {
        Objects.requireNonNull(weeks, "weeks is required");
        for (CateredWeek w : weeks) {
            validateWeek(w);
        }
    }

    /**
     * Validate a single week.
     *
     * @param week - Week to validate.
     */
    public static void validateWeek(CateredWeek week) {
        Objects.requireNonNull(week, "week is required");

        List<MenuDay> days = week.getDays();
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("at least one day must be provided");
        }
        for (MenuDay d : days) {
            validateDay(d);
        }
    }

    // Validate a single day entry.
    private static void validateDay(MenuDay d) {
        if (d == null) {
            throw new IllegalArgumentException("day entry cannot be null");
        }
        if (d.getDayOfTheWeek() == null) {
            throw new IllegalArgumentException("dayOfTheWeek is required");
        }
        String item = d.getMenuItem();
        if (item != null) {
            String trimmed = item.trim();
            if (trimmed.length() > MAX_TEXT) {
                throw new IllegalArgumentException("menuItem length must be <= " + MAX_TEXT);
            }
        }
    }
}