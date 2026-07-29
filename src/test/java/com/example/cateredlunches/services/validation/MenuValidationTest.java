package com.example.cateredlunches.services.validation;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.DayOfWeekIndicator;
import com.example.cateredlunches.model.MenuDay;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuValidationTest {

    @Test
    void validateWeek_nullWeek_throwsNPE() {
        assertThrows(NullPointerException.class, () -> MenuValidation.validateWeek(null));
    }

    @Test
    void validateWeek_nullDays_throws() {
        CateredWeek week = new CateredWeek();
        week.setDays(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MenuValidation.validateWeek(week));
        assertTrue(ex.getMessage().contains("at least one day"));
    }

    @Test
    void validateWeek_emptyDays_throws() {
        CateredWeek week = new CateredWeek();
        week.setDays(new ArrayList<>());
        assertThrows(IllegalArgumentException.class, () -> MenuValidation.validateWeek(week));
    }

    @Test
    void validateWeek_validDays_doesNotThrow() {
        CateredWeek week = new CateredWeek();
        week.setDays(List.of(
                new MenuDay(DayOfWeekIndicator.MONDAY, "Tacos"),
                new MenuDay(DayOfWeekIndicator.TUESDAY, null)
        ));
        assertDoesNotThrow(() -> MenuValidation.validateWeek(week));
    }

    @Test
    void validateWeek_nullDayEntry_throws() {
        CateredWeek week = new CateredWeek();
        List<MenuDay> days = new ArrayList<>();
        days.add(null);
        week.setDays(days);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MenuValidation.validateWeek(week));
        assertTrue(ex.getMessage().contains("day entry cannot be null"));
    }

    @Test
    void validateWeek_missingDayOfWeek_throws() {
        CateredWeek week = new CateredWeek();
        week.setDays(List.of(new MenuDay(null, "Tacos")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MenuValidation.validateWeek(week));
        assertTrue(ex.getMessage().contains("dayOfTheWeek is required"));
    }

    @Test
    void validateWeek_menuItemTooLong_throws() {
        CateredWeek week = new CateredWeek();
        String tooLong = "x".repeat(201);
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, tooLong)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MenuValidation.validateWeek(week));
        assertTrue(ex.getMessage().contains("menuItem length"));
    }

    @Test
    void validateWeek_menuItemExactlyMax_isAllowed() {
        CateredWeek week = new CateredWeek();
        String exactlyMax = "x".repeat(200);
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, exactlyMax)));

        assertDoesNotThrow(() -> MenuValidation.validateWeek(week));
    }

    @Test
    void validateWeek_menuItemWithWhitespaceTrimmedForLengthCheck() {
        CateredWeek week = new CateredWeek();
        // Padding whitespace around max-length text; trimmed length should be within bounds
        String padded = "  " + "x".repeat(200) + "  ";
        week.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, padded)));

        assertDoesNotThrow(() -> MenuValidation.validateWeek(week));
    }

    @Test
    void validateWeeks_nullList_throwsNPE() {
        assertThrows(NullPointerException.class, () -> MenuValidation.validateWeeks(null));
    }

    @Test
    void validateWeeks_allValid_doesNotThrow() {
        CateredWeek week1 = new CateredWeek();
        week1.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "A")));
        CateredWeek week2 = new CateredWeek();
        week2.setDays(List.of(new MenuDay(DayOfWeekIndicator.TUESDAY, "B")));

        assertDoesNotThrow(() -> MenuValidation.validateWeeks(List.of(week1, week2)));
    }

    @Test
    void validateWeeks_oneInvalidWeek_throws() {
        CateredWeek valid = new CateredWeek();
        valid.setDays(List.of(new MenuDay(DayOfWeekIndicator.MONDAY, "A")));
        CateredWeek invalid = new CateredWeek();
        invalid.setDays(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> MenuValidation.validateWeeks(List.of(valid, invalid)));
    }

    @Test
    void validateWeeks_emptyList_doesNotThrow() {
        assertDoesNotThrow(() -> MenuValidation.validateWeeks(List.of()));
    }
}
