package com.example.cateredlunches.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private DisplayService displayService;
    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private MenuService menuService;

    @Test
    void showMenu_displaysExpectedText() {
        menuService.showMenu();
        verify(displayService).displayOnScreen(contains("Add Week"));
    }

    @Test
    void processMenuChoice_add_lowercase_callsAddWeek() {
        boolean result = menuService.processMenuChoice("a");
        verify(calendarService).addWeek();
        assertTrue(result);
    }

    @Test
    void processMenuChoice_add_uppercase_callsAddWeek() {
        boolean result = menuService.processMenuChoice("A");
        verify(calendarService).addWeek();
        assertTrue(result);
    }

    @Test
    void processMenuChoice_delete_lowercase_callsDeleteWeek() {
        boolean result = menuService.processMenuChoice("d");
        verify(calendarService).deleteWeek();
        assertTrue(result);
    }

    @Test
    void processMenuChoice_delete_uppercase_callsDeleteWeek() {
        boolean result = menuService.processMenuChoice("D");
        verify(calendarService).deleteWeek();
        assertTrue(result);
    }

    @Test
    void processMenuChoice_view_lowercase_callsViewWeek() {
        boolean result = menuService.processMenuChoice("v");
        verify(calendarService).viewWeek();
        assertTrue(result);
    }

    @Test
    void processMenuChoice_view_uppercase_callsViewWeek() {
        boolean result = menuService.processMenuChoice("V");
        verify(calendarService).viewWeek();
        assertTrue(result);
    }

    @Test
    void processMenuChoice_quit_lowercase_returnsFalse() {
        boolean result = menuService.processMenuChoice("q");
        assertFalse(result);
        verifyNoInteractions(calendarService);
    }

    @Test
    void processMenuChoice_quit_uppercase_returnsFalse() {
        boolean result = menuService.processMenuChoice("Q");
        assertFalse(result);
    }

    @Test
    void processMenuChoice_invalidChoice_showsErrorAndReturnsTrue() {
        boolean result = menuService.processMenuChoice("z");
        verify(displayService).displayErrorOnScreen(contains("not a valid choice"));
        assertTrue(result);
        verifyNoInteractions(calendarService);
    }

    @Test
    void processMenuChoice_emptyString_treatedAsInvalid() {
        boolean result = menuService.processMenuChoice("");
        verify(displayService).displayErrorOnScreen(anyString());
        assertTrue(result);
    }
}
