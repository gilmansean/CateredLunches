package com.example.cateredlunches.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MenuDayTest {

    @Test
    void noArgConstructor_startsWithNullFields() {
        MenuDay day = new MenuDay();
        assertNull(day.getDayOfTheWeek());
        assertNull(day.getMenuItem());
    }

    @Test
    void allArgsConstructor_setsFields() {
        MenuDay day = new MenuDay(DayOfWeekIndicator.FRIDAY, "Pizza");
        assertEquals(DayOfWeekIndicator.FRIDAY, day.getDayOfTheWeek());
        assertEquals("Pizza", day.getMenuItem());
    }

    @Test
    void settersUpdateFields() {
        MenuDay day = new MenuDay();
        day.setDayOfTheWeek(DayOfWeekIndicator.WEDNESDAY);
        day.setMenuItem("Tacos");

        assertEquals(DayOfWeekIndicator.WEDNESDAY, day.getDayOfTheWeek());
        assertEquals("Tacos", day.getMenuItem());
    }

    @Test
    void menuItem_canBeNull() {
        MenuDay day = new MenuDay(DayOfWeekIndicator.MONDAY, null);
        assertNull(day.getMenuItem());
    }
}
