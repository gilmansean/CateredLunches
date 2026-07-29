package com.example.cateredlunches.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandLineServiceTest {

    @Mock
    private CalendarService calendarService;
    @Mock
    private DisplayService displayService;
    @Mock
    private MenuService menuService;

    @InjectMocks
    private CommandLineService commandLineService;

    @Test
    void run_singleIteration_thenQuits() throws Exception {
        when(displayService.waitForInput()).thenReturn("q");
        when(menuService.processMenuChoice("q")).thenReturn(false);

        commandLineService.run();

        verify(displayService, times(1)).showHeader();
        verify(calendarService, times(1)).showCalendar();
        verify(menuService, times(1)).showMenu();
        verify(displayService, times(1)).showFooter();
        verify(displayService, times(1)).waitForInput();
        verify(menuService, times(1)).processMenuChoice("q");
    }

    @Test
    void run_loopsMultipleTimesUntilQuit() throws Exception {
        when(displayService.waitForInput()).thenReturn("a", "d", "q");
        when(menuService.processMenuChoice("a")).thenReturn(true);
        when(menuService.processMenuChoice("d")).thenReturn(true);
        when(menuService.processMenuChoice("q")).thenReturn(false);

        commandLineService.run();

        verify(displayService, times(3)).showHeader();
        verify(displayService, times(3)).showFooter();
        verify(menuService, times(3)).showMenu();
        verify(menuService).processMenuChoice("a");
        verify(menuService).processMenuChoice("d");
        verify(menuService).processMenuChoice("q");
    }

    @Test
    void run_displaysCalendarOutputFromCalendarService() throws Exception {
        when(calendarService.showCalendar()).thenReturn("CALENDAR_VIEW");
        when(displayService.waitForInput()).thenReturn("q");
        when(menuService.processMenuChoice("q")).thenReturn(false);

        commandLineService.run();

        verify(displayService).displayOnScreen("CALENDAR_VIEW");
    }
}
