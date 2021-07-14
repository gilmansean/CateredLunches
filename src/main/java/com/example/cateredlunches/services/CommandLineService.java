package com.example.cateredlunches.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Commandline runner.  This is the main entrypoint to the app.
 */
@Component
public class CommandLineService implements CommandLineRunner {
    private CalendarService calendarService;
    private DisplayService displayService;
    private MenuService menuService;

    public CommandLineService(CalendarService calendarService, DisplayService displayService, MenuService menuService) {
        this.calendarService = calendarService;
        this.displayService = displayService;
        this.menuService = menuService;
    }

    @Override
    public void run(String... args) throws Exception {
        String choice = "";
        boolean running = true;
        while(running){
            displayService.showHeader();
            displayService.displayOnScreen(calendarService.showCalendar());
            menuService.showMenu();
            displayService.showFooter();
            running = menuService.processMenuChoice(displayService.waitForInput());
        }
    }

}
