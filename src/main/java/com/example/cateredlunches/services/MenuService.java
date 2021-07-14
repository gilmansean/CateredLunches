package com.example.cateredlunches.services;

import org.springframework.stereotype.Component;

/**
 * Service to display user interaction menu
 */
@Component
public class MenuService {
    private DisplayService displayService;
    private CalendarService calendarService;

    public MenuService(DisplayService displayService, CalendarService calendarService) {
        this.displayService = displayService;
        this.calendarService = calendarService;
    }

    /**
     * Show the menu choices.
     */
    public void showMenu() {
        displayService.displayOnScreen( "a: Add Week\n" +
                "d: Delete Week\n"+
                "q: Quit");
    }

    /**
     * Takes the choice the user input and processes it.
     * @param choice - User input
     * @return - boolean determining if processing should continue or is all done.
     */
    public boolean processMenuChoice(String choice) {
        boolean processing = true;
        switch(choice){
            case "A":
            case "a":
                calendarService.addWeek();
                break;
            case "D":
            case "d":
                calendarService.deleteWeek();
                break;
            case "Q":
            case "q":
                processing = false;
                break;
            default:
                showInvalidChoice();
                break;
        }
        return processing;
    }

    private void showInvalidChoice() {
        displayService.displayErrorOnScreen("That is not a valid choice");
    }
}
