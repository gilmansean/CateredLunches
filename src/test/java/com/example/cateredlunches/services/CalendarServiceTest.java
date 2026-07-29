package com.example.cateredlunches.services;

import com.example.cateredlunches.model.CateredWeek;
import com.example.cateredlunches.model.CateredWeekEntry;
import com.example.cateredlunches.model.DayOfWeekIndicator;
import com.example.cateredlunches.model.MenuDay;
import com.example.cateredlunches.services.repositories.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private DisplayService displayService;

    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarService(menuRepository, displayService);
    }

    // ---------- showCalendar ----------

    @Test
    void showCalendar_emptyRepository_returnsEmptyString() {
        when(menuRepository.getAllEntries()).thenReturn(List.of());

        String result = calendarService.showCalendar();

        assertEquals("", result);
    }

    @Test
    void showCalendar_singleWeekWithItems_formatsDayAndItemLines() {
        CateredWeek week = new CateredWeek();
        week.addDay(new MenuDay(DayOfWeekIndicator.MONDAY, "Tacos"));
        week.addDay(new MenuDay(DayOfWeekIndicator.TUESDAY, "Pasta"));
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        when(menuRepository.getAllEntries()).thenReturn(List.of(new CateredWeekEntry(weekStart, week)));

        String result = calendarService.showCalendar();

        assertTrue(result.contains("Week 1: 2026-07-13"));
        assertTrue(result.contains("Mon"));
        assertTrue(result.contains("Tue"));
        assertTrue(result.contains("Tacos"));
        assertTrue(result.contains("Pasta"));
    }

    @Test
    void showCalendar_nullMenuItem_handledGracefully() {
        CateredWeek week = new CateredWeek();
        week.addDay(new MenuDay(DayOfWeekIndicator.MONDAY, null));
        when(menuRepository.getAllEntries()).thenReturn(List.of(new CateredWeekEntry(LocalDate.of(2026, 7, 13), week)));

        String result = calendarService.showCalendar();

        assertTrue(result.contains("Mon"));
        assertFalse(result.contains("null"));
    }

    @Test
    void showCalendar_itemLongerThanDayLabel_padsToItemWidth() {
        CateredWeek week = new CateredWeek();
        String longItem = "Grilled Chicken Sandwich";
        week.addDay(new MenuDay(DayOfWeekIndicator.MONDAY, longItem));
        when(menuRepository.getAllEntries()).thenReturn(List.of(new CateredWeekEntry(LocalDate.of(2026, 7, 13), week)));

        String result = calendarService.showCalendar();

        assertTrue(result.contains(longItem));
    }

    @Test
    void showCalendar_multipleWeeks_numbersWeeksSequentiallyAndShowsDates() {
        CateredWeek week1 = new CateredWeek();
        week1.addDay(new MenuDay(DayOfWeekIndicator.MONDAY, "A"));
        CateredWeek week2 = new CateredWeek();
        week2.addDay(new MenuDay(DayOfWeekIndicator.TUESDAY, "B"));
        when(menuRepository.getAllEntries()).thenReturn(List.of(
                new CateredWeekEntry(LocalDate.of(2026, 7, 13), week1),
                new CateredWeekEntry(LocalDate.of(2026, 7, 20), week2)
        ));

        String result = calendarService.showCalendar();

        assertTrue(result.contains("Week 1: 2026-07-13"));
        assertTrue(result.contains("Week 2: 2026-07-20"));
    }

    // ---------- addWeek ----------

    @Test
    void addWeek_happyPath_upsertsWeekWithAllWeekdays() {
        when(displayService.waitForInput()).thenReturn(
                "2026-07-13", // week start date
                "Tacos", "Pasta", "Salad", "Soup", "Sandwich" // Mon-Fri
        );

        calendarService.addWeek();

        ArgumentCaptor<CateredWeek> weekCaptor = ArgumentCaptor.forClass(CateredWeek.class);
        verify(menuRepository).upsert(eq(LocalDate.of(2026, 7, 13)), weekCaptor.capture());

        List<MenuDay> days = weekCaptor.getValue().getDays();
        assertEquals(5, days.size());
        assertEquals(DayOfWeekIndicator.MONDAY, days.get(0).getDayOfTheWeek());
        assertEquals("Tacos", days.get(0).getMenuItem());
        assertEquals(DayOfWeekIndicator.FRIDAY, days.get(4).getDayOfTheWeek());
        assertEquals("Sandwich", days.get(4).getMenuItem());
        // Ensure no weekend days were included
        assertTrue(days.stream().noneMatch(d -> d.getDayOfTheWeek().isWeekend()));

        verify(displayService, never()).displayErrorOnScreen(anyString());
    }

    @Test
    void addWeek_invalidDate_showsErrorAndDoesNotUpsert() {
        when(displayService.waitForInput()).thenReturn("not-a-date");

        calendarService.addWeek();

        verify(displayService).displayErrorOnScreen(contains("Invalid date"));
        verify(menuRepository, never()).upsert(any(), any());
    }

    @Test
    void addWeek_upsertThrowsValidationError_showsError() {
        when(displayService.waitForInput()).thenReturn(
                "2026-07-13",
                "Tacos", "Pasta", "Salad", "Soup", "Sandwich"
        );
        when(menuRepository.upsert(any(), any()))
                .thenThrow(new IllegalArgumentException("menuItem length must be <= 200"));

        calendarService.addWeek();

        verify(displayService).displayErrorOnScreen(contains("Problem saving week to menu"));
    }

    // ---------- viewWeek ----------

    @Test
    void viewWeek_invalidDate_showsErrorAndDoesNotQueryRepository() {
        when(displayService.waitForInput()).thenReturn("not-a-date");

        calendarService.viewWeek();

        verify(displayService).displayErrorOnScreen(contains("Invalid date"));
        verify(menuRepository, never()).get(any());
    }

    @Test
    void viewWeek_weekNotFound_showsError() {
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        when(displayService.waitForInput()).thenReturn("2026-07-13");
        when(menuRepository.get(weekStart)).thenReturn(java.util.Optional.empty());

        calendarService.viewWeek();

        verify(displayService).displayErrorOnScreen(contains("No week found starting " + weekStart));
    }

    @Test
    void viewWeek_weekFound_displaysFormattedWeek() {
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        CateredWeek week = new CateredWeek();
        week.addDay(new MenuDay(DayOfWeekIndicator.MONDAY, "Tacos"));
        when(displayService.waitForInput()).thenReturn("2026-07-13");
        when(menuRepository.get(weekStart)).thenReturn(java.util.Optional.of(week));

        calendarService.viewWeek();

        verify(displayService).displayOnScreen(contains("Week of " + weekStart));
        verify(displayService, never()).displayErrorOnScreen(anyString());
    }


}
