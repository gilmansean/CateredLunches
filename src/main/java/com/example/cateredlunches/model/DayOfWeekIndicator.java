package com.example.cateredlunches.model;

/**
 * Canonical indicator of days we currently support for catered menus.
 * UI/serialization can map these to any display strings.
 */
public enum DayOfWeekIndicator {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;


    /**
     * Deserialization from a string.
     * Accepts full names (case-insensitive) and short names like "Mon".
     *
     * @param day - String representation of the day of the week.
     * @return - DayOfWeekIndicator
     */
    public static DayOfWeekIndicator fromString(String day) {
        String dayOfWeek = day.trim();
        if (dayOfWeek.length() == 3) {
            // Normalize short name to full enum constant
            String upper = dayOfWeek.toUpperCase();
            switch (upper) {
                case "MON":
                    return MONDAY;
                case "TUE":
                    return TUESDAY;
                case "WED":
                    return WEDNESDAY;
                case "THU":
                    return THURSDAY;
                case "FRI":
                    return FRIDAY;
                case "SAT":
                    return SATURDAY;
                case "SUN":
                    return SUNDAY;
                default:
                    break;
            }
        }
        return DayOfWeekIndicator.valueOf(dayOfWeek.toUpperCase());
    }

    /**
     * A short, human-friendly name like "Mon".
     */
    public String shortName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1, 3).toLowerCase();
    }

    /**
     * A long, human-friendly name like "Monday".
     *
     * @return - String representation of the day of the week.
     */
    public String longName() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }

    /**
     * Display string for the day of the week.
     *
     * @return - String representation of the day of the week.
     */
    @Override
    public String toString() {
        // Keep default enum name for logs/serialization unless mapped explicitly.
        return name();
    }

    /**
     * Is this a weekday?
     *
     * @return true if a weekday, false if a weekend day.
     */
    public boolean isWeekday() {
        return !isWeekend();
    }

    /**
     * Is this a weekend day?
     *
     * @return true if a weekend day, false if a weekday.
     */
    public boolean isWeekend() {
        return this == SATURDAY || this == SUNDAY;
    }

}