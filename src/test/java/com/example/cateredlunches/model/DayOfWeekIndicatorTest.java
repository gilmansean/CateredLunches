package com.example.cateredlunches.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DayOfWeekIndicatorTest {

    @Test
    void shortName_returnsThreeLetterCapitalized() {
        assertEquals("Mon", DayOfWeekIndicator.MONDAY.shortName());
        assertEquals("Tue", DayOfWeekIndicator.TUESDAY.shortName());
        assertEquals("Wed", DayOfWeekIndicator.WEDNESDAY.shortName());
        assertEquals("Thu", DayOfWeekIndicator.THURSDAY.shortName());
        assertEquals("Fri", DayOfWeekIndicator.FRIDAY.shortName());
        assertEquals("Sat", DayOfWeekIndicator.SATURDAY.shortName());
        assertEquals("Sun", DayOfWeekIndicator.SUNDAY.shortName());
    }

    @Test
    void longName_returnsFourLetterMix() {
        // Implementation only takes chars 0-3, so "Mond" not "Monday"
        assertEquals("Monday", DayOfWeekIndicator.MONDAY.longName());
        assertEquals("Friday", DayOfWeekIndicator.FRIDAY.longName());
    }

    @Test
    void toString_returnsEnumName() {
        assertEquals("MONDAY", DayOfWeekIndicator.MONDAY.toString());
        assertEquals("SUNDAY", DayOfWeekIndicator.SUNDAY.toString());
    }

    @Test
    void fromString_fullNameCaseInsensitive() {
        assertEquals(DayOfWeekIndicator.MONDAY, DayOfWeekIndicator.fromString("monday"));
        assertEquals(DayOfWeekIndicator.MONDAY, DayOfWeekIndicator.fromString("MONDAY"));
        assertEquals(DayOfWeekIndicator.MONDAY, DayOfWeekIndicator.fromString("  Monday  "));
    }

    @Test
    void fromString_shortNameAllDays() {
        assertEquals(DayOfWeekIndicator.MONDAY, DayOfWeekIndicator.fromString("Mon"));
        assertEquals(DayOfWeekIndicator.TUESDAY, DayOfWeekIndicator.fromString("tue"));
        assertEquals(DayOfWeekIndicator.WEDNESDAY, DayOfWeekIndicator.fromString("WED"));
        assertEquals(DayOfWeekIndicator.THURSDAY, DayOfWeekIndicator.fromString("Thu"));
        assertEquals(DayOfWeekIndicator.FRIDAY, DayOfWeekIndicator.fromString("Fri"));
        assertEquals(DayOfWeekIndicator.SATURDAY, DayOfWeekIndicator.fromString("Sat"));
        assertEquals(DayOfWeekIndicator.SUNDAY, DayOfWeekIndicator.fromString("Sun"));
    }

    @Test
    void fromString_threeLetterButNotAKnownAbbreviation_fallsBackToValueOf() {
        // length==3 but not in switch -> falls through to valueOf, which will throw
        assertThrows(IllegalArgumentException.class, () -> DayOfWeekIndicator.fromString("xyz"));
    }

    @Test
    void fromString_invalidValue_throws() {
        assertThrows(IllegalArgumentException.class, () -> DayOfWeekIndicator.fromString("Notaday"));
    }

    @Test
    void isWeekend_and_isWeekday() {
        assertTrue(DayOfWeekIndicator.SATURDAY.isWeekend());
        assertTrue(DayOfWeekIndicator.SUNDAY.isWeekend());
        assertFalse(DayOfWeekIndicator.SATURDAY.isWeekday());

        for (DayOfWeekIndicator d : new DayOfWeekIndicator[]{
                DayOfWeekIndicator.MONDAY, DayOfWeekIndicator.TUESDAY, DayOfWeekIndicator.WEDNESDAY,
                DayOfWeekIndicator.THURSDAY, DayOfWeekIndicator.FRIDAY}) {
            assertTrue(d.isWeekday());
            assertFalse(d.isWeekend());
        }
    }

    @Test
    void values_containsAllSevenDays() {
        assertEquals(7, DayOfWeekIndicator.values().length);
    }
}
